package com.knowledgespike.junieviewer.data

import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.getPlatform
import com.knowledgespike.junieviewer.search.buildTopLevelSearchSnippet
import com.knowledgespike.junieviewer.search.normalizeTopLevelSnippetSource
import com.knowledgespike.junieviewer.search.orderTopLevelSessionResults
import com.knowledgespike.junieviewer.ui.MessageContentRegistry
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer

/**
 * Result of loading a Session's messages, including metadata needed for live tracking.
 */
data class SessionLoadResult(
    val messages: List<Message>,
    val eventsFilePath: Path?,
    val fileSizeAfterLoad: Long,
    /** Total number of lines read from the events.jsonl file (for stable ID continuity in live tracking). */
    val totalLineCount: Int = 0,
    /** Number of lines that failed to parse during scan. */
    val parseErrors: Int = 0
)

interface SessionRepository {
    /** Resolves the session identified by [sessionId] under [homePath] and loads its messages in one atomic call. */
    fun loadSession(sessionId: String, homePath: String): SessionLoadResult
    fun listSessions(homePath: String): List<SessionInfo>
    /** Returns the [SessionInfo] for the currently set session, or null if unavailable. */
    fun getSessionInfo(sessionId: String, homePath: String): SessionInfo?
    /**
     * Searches across discovered Sessions using a normalized top-level Search Query under [homePath].
     */
    suspend fun searchSessions(query: TopLevelSearchQuery, homePath: String = ""): TopLevelSearchResults
}

/**
 * Reads Junie session data from the file system and converts events to UI messages.
 * Delegates event-to-message mapping to [EventToMessageMapper].
 */
class SessionRepositoryImpl(
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) : SessionRepository {
    private val logger = Logger.withTag("SessionRepository")

    override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? {
        val sessionDir = resolveExistingDir(homePath, "sessions", sessionId) ?: run {
            logger.w { "Session directory does not exist for: $sessionId" }
            return null
        }
        return try {
            toSessionInfo(sessionId, sessionDir)
        } catch (e: Exception) {
            logger.e(e) { "Error reading session info for $sessionId" }
            null
        }
    }

    override fun loadSession(sessionId: String, homePath: String): SessionLoadResult {
        val path = try {
            expandPath(homePath).toPath().div("sessions").div(sessionId).div("events.jsonl")
        } catch (e: Exception) {
            logger.e(e) { "Invalid session path for: $sessionId" }
            return SessionLoadResult(emptyList(), null, 0L)
        }
        if (!fileSystem.exists(path)) {
            logger.e { "Session file does not exist: $path" }
            return SessionLoadResult(emptyList(), path, 0L)
        }

        logger.d { "Loading messages from: $path" }
        val events = mutableListOf<JunieEvent>()
        var parseErrors = 0
        val totalLineCount = scanLines(path) { line ->
            if (line.isNotBlank()) {
                JsonlParser.parseLine(line)
                    .onRight { events.add(it) }
                    .onLeft { parseErrors++ }
            }
            true
        }
        val knownCount = events.count { it !is UnknownJunieEvent && (it !is SessionA2uxEvent || it.event.agentEvent !is UnknownAgentEvent) }
        val unknownTopLevel = events.count { it is UnknownJunieEvent }
        val unknownNested = events.count { it is SessionA2uxEvent && it.event.agentEvent is UnknownAgentEvent }
        logger.i { "Session loaded: $totalLineCount lines, ${events.size} events (known=$knownCount, unknownTopLevel=$unknownTopLevel, unknownNested=$unknownNested, parseErrors=$parseErrors)" }
        if (unknownTopLevel + unknownNested > 0) {
            logger.w { "Unknown event kinds found — these will appear as unsupported event indicators in the UI" }
        }

        val messages = EventToMessageMapper.mapEventsToMessages(events)
        val fileSize = try { fileSystem.metadata(path).size ?: 0L } catch (_: Exception) { 0L }
        return SessionLoadResult(messages, path, fileSize, totalLineCount, parseErrors)
    }

    override fun listSessions(homePath: String): List<SessionInfo> {
        if (homePath.isBlank()) {
            logger.w { "listSessions called with blank homePath" }
            return emptyList()
        }

        val sessionsDir = resolveExistingDir(homePath, "sessions") ?: run {
            logger.w { "Sessions directory does not exist for home path: $homePath" }
            return emptyList()
        }

        logger.d { "Listing sessions from: $sessionsDir" }
        return try {
            fileSystem.list(sessionsDir)
                .filter { fileSystem.metadata(it).isDirectory }
                .map { dir -> toSessionInfo(dir.name, dir) }
                .sortedByDescending { it.lastModified }
        } catch (e: Exception) {
            logger.e(e) { "Error listing sessions from $sessionsDir" }
            emptyList()
        }
    }

    override suspend fun searchSessions(query: TopLevelSearchQuery, homePath: String): TopLevelSearchResults {
        val normalizedQuery = TopLevelSearchQuery(query.raw)
        if (normalizedQuery.isBlank) {
            return TopLevelSearchResults(
                query = normalizedQuery,
                status = TopLevelSearchStatus.EmptyQuery
            )
        }

        val sessions = listSessions(homePath)
        val sessionResults = mutableListOf<TopLevelSessionSearchResult>()
        val partialFailures = mutableListOf<TopLevelSearchPartialFailure>()

        for (sessionInfo in sessions) {
            val eventsFile = try {
                expandPath(homePath).toPath().div("sessions").div(sessionInfo.id).div("events.jsonl")
            } catch (e: Exception) {
                logger.w(e) { "Top-level search: Session '${sessionInfo.id}' (path: ${sessionInfo.path}) could not be scanned: failed to resolve events file path. Reason: ${e.message}" }
                partialFailures.add(
                    TopLevelSearchPartialFailure(
                        sessionId = sessionInfo.id,
                        sessionPath = sessionInfo.path,
                        reason = e.message ?: "Invalid session path"
                    )
                )
                continue
            }

            if (!fileSystem.exists(eventsFile)) {
                logger.d { "Top-level search: Session '${sessionInfo.id}' (path: ${sessionInfo.path}) has missing events file, ignoring." }
                continue
            }

            val fileSize = try {
                fileSystem.metadata(eventsFile).size ?: 0L
            } catch (e: Exception) {
                logger.w(e) { "Top-level search: Session '${sessionInfo.id}' (path: ${sessionInfo.path}) encountered warning while reading metadata for events file '$eventsFile': ${e.message}" }
                0L
            }

            if (fileSize == 0L) {
                logger.d { "Top-level search: Session '${sessionInfo.id}' (path: ${sessionInfo.path}) has empty events file (0 bytes), ignoring." }
                continue
            }

            val sessionLoadResult = try {
                loadSession(sessionInfo.id, homePath)
            } catch (e: Exception) {
                logger.w(e) { "Top-level search: Session '${sessionInfo.id}' (path: ${sessionInfo.path}) could not be scanned: error loading/reading session file at path '$eventsFile'. Reason: ${e.message}" }
                partialFailures.add(
                    TopLevelSearchPartialFailure(
                        sessionId = sessionInfo.id,
                        sessionPath = sessionInfo.path,
                        reason = e.message ?: "Unreadable file"
                    )
                )
                continue
            }

            if (sessionLoadResult.totalLineCount == 0) {
                logger.d { "Top-level search: Session '${sessionInfo.id}' (path: ${sessionInfo.path}) has totalLineCount = 0, ignoring empty session." }
                continue
            }

            if (sessionLoadResult.parseErrors > 0) {
                logger.w { "Top-level search: Session '${sessionInfo.id}' (path: ${sessionInfo.path}) contains ${sessionLoadResult.parseErrors} malformed/unparseable line(s) in events file at path '$eventsFile'" }
            }

            if (sessionLoadResult.messages.isEmpty() && sessionLoadResult.totalLineCount > 0) {
                if (sessionLoadResult.parseErrors > 0) {
                    logger.w { "Top-level search: Session '${sessionInfo.id}' (path: ${sessionInfo.path}) was malformed: events file at path '$eventsFile' has totalLineCount=${sessionLoadResult.totalLineCount} and parseErrors=${sessionLoadResult.parseErrors}" }
                    partialFailures.add(
                        TopLevelSearchPartialFailure(
                            sessionId = sessionInfo.id,
                            sessionPath = sessionInfo.path,
                            reason = "Malformed JSONL file or no valid parseable events (totalLineCount=${sessionLoadResult.totalLineCount}, parseErrors=${sessionLoadResult.parseErrors})"
                        )
                    )
                    continue
                } else {
                    continue
                }
            }

            var sessionMatchCount = 0
            val sessionSnippets = mutableListOf<TopLevelSearchSnippet>()

            for (message in sessionLoadResult.messages) {
                val searchText = MessageContentRegistry.searchableText(message)
                if (searchText.isBlank()) continue

                val normalizedText = normalizeTopLevelSnippetSource(searchText)
                val lowerText = normalizedText.lowercase()
                val lowerQuery = normalizedQuery.normalized.lowercase()

                if (lowerQuery.isEmpty()) continue

                var index = lowerText.indexOf(lowerQuery)
                while (index >= 0) {
                    sessionMatchCount++
                    if (sessionSnippets.isEmpty()) {
                        buildTopLevelSearchSnippet(searchText, normalizedQuery)?.let { snippet ->
                            sessionSnippets.add(snippet)
                        }
                    }
                    index = lowerText.indexOf(lowerQuery, index + lowerQuery.length)
                }
            }

            if (sessionMatchCount > 0) {
                val identity = TopLevelSessionIdentity(
                    sessionId = sessionInfo.id,
                    sessionPath = sessionInfo.path,
                    sessionTimestampMillis = sessionInfo.lastModified
                )
                val summary = TopLevelSearchMatchSummary(
                    firstSnippet = sessionSnippets.firstOrNull(),
                    additionalSnippetCount = maxOf(0, sessionSnippets.size - 1)
                )
                sessionResults.add(
                    TopLevelSessionSearchResult(
                        session = identity,
                        matchCount = sessionMatchCount,
                        snippets = sessionSnippets,
                        summary = summary
                    )
                )
            }
        }

        val orderedResults = orderTopLevelSessionResults(sessionResults)

        return TopLevelSearchResults(
            query = normalizedQuery,
            status = TopLevelSearchStatus.Completed,
            sessionResults = orderedResults,
            partialFailures = partialFailures
        )
    }

    /** Builds the [SessionInfo] for the session directory [dir], identified by [id]. */
    private fun toSessionInfo(id: String, dir: Path): SessionInfo {
        val meta = fileSystem.metadata(dir)
        return SessionInfo(
            id = id,
            path = dir.toString(),
            lastModified = meta.lastModifiedAtMillis ?: 0L,
            createdAt = meta.createdAtMillis,
            workingDirectory = extractWorkingDirectory(dir)
        )
    }

    /**
     * Expands [homePath] and appends [segments], returning the resulting [Path] only if it
     * exists on disk. Returns null (logging the reason) on any expansion/resolution failure
     * or when the resolved directory is missing.
     */
    private fun resolveExistingDir(homePath: String, vararg segments: String): Path? {
        val expandedHome = try {
            expandPath(homePath)
        } catch (e: Exception) {
            logger.e(e) { "Error expanding path: $homePath" }
            return null
        }
        val dir = try {
            segments.fold(expandedHome.toPath()) { path, segment -> path.div(segment) }
        } catch (e: Exception) {
            logger.e(e) { "Invalid directory path under: $expandedHome" }
            return null
        }
        return dir.takeIf { fileSystem.exists(it) }
    }

    /**
     * Scans a session's events.jsonl for the first parsed event carrying a working
     * directory and returns it. Lines are parsed with [JsonlParser]; malformed lines
     * are skipped.
     */
    private fun extractWorkingDirectory(sessionDir: Path): String? {
        val eventsFile = sessionDir / "events.jsonl"
        if (!fileSystem.exists(eventsFile)) return null

        var found: String? = null
        try {
            scanLines(eventsFile) { line ->
                // Cheap fast-path: only lines mentioning the field can produce a hit.
                if (line.contains("currentDirectory")) {
                    JsonlParser.parseLine(line)
                        .onLeft { logger.d { "Skipping malformed line while extracting working directory: ${it.message}" } }
                        .getOrNull()
                        ?.let { event -> (event as? SessionA2uxEvent)?.event?.agentEvent?.workingDirectoryOrNull() }
                        ?.let { directory -> found = directory }
                }
                found == null
            }
        } catch (e: Exception) {
            logger.d { "Could not extract working directory from $eventsFile: ${e.message}" }
        }
        return found
    }

    /**
     * Reads [path] line by line, invoking [onLine] with each line and stopping early when it
     * returns false. Returns the total number of lines read. Shared by [loadSession] and
     * [extractWorkingDirectory], which both need a full or partial line-by-line scan.
     */
    private fun scanLines(path: Path, onLine: (String) -> Boolean): Int {
        var totalLineCount = 0
        fileSystem.source(path).buffer().use { source ->
            while (true) {
                val line = source.readUtf8Line() ?: break
                totalLineCount++
                if (!onLine(line)) break
            }
        }
        return totalLineCount
    }

    private fun expandPath(path: String): String {
        return if (path.startsWith("~")) {
            val home = getPlatform().userHome
            path.replaceFirst("~", home)
        } else {
            path
        }
    }
}
