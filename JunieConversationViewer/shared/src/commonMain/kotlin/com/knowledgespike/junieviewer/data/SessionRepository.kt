package com.knowledgespike.junieviewer.data

import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.getPlatform
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
    val totalLineCount: Int = 0
)

interface SessionRepository {
    /** Resolves the session identified by [sessionId] under [homePath] and loads its messages in one atomic call. */
    fun loadSession(sessionId: String, homePath: String): SessionLoadResult
    fun listSessions(homePath: String): List<SessionInfo>
    /** Returns the [SessionInfo] for the currently set session, or null if unavailable. */
    fun getSessionInfo(sessionId: String, homePath: String): SessionInfo?
    /**
     * Searches across discovered Sessions using a normalized top-level Search Query.
     *
     * Area 2 defines the contract and structured result model. Full cross-session scan
     * behavior is implemented in later sprint areas.
     */
    suspend fun searchSessions(query: TopLevelSearchQuery): TopLevelSearchResults
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
        return SessionLoadResult(messages, path, fileSize, totalLineCount)
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

    override suspend fun searchSessions(query: TopLevelSearchQuery): TopLevelSearchResults {
        val normalizedQuery = TopLevelSearchQuery(query.raw)
        if (normalizedQuery.isBlank) {
            return TopLevelSearchResults(
                query = normalizedQuery,
                status = TopLevelSearchStatus.EmptyQuery
            )
        }

        // Area 2 intentionally defines only the contract and deterministic result shape.
        // The full on-demand scan pipeline lands in Area 3.
        return TopLevelSearchResults(
            query = normalizedQuery,
            status = TopLevelSearchStatus.Completed
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
