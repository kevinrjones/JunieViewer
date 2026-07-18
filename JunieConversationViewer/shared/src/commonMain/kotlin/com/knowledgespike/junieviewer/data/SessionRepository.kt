package com.knowledgespike.junieviewer.data

import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.getPlatform
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
    val fileSizeAfterLoad: Long
)

interface SessionRepository {
    fun getMessages(): List<Message>
    /** Loads messages and returns metadata needed for live tracking. */
    fun loadSession(): SessionLoadResult = SessionLoadResult(getMessages(), null, 0L)
    fun listSessions(homePath: String): List<SessionInfo>
    fun setSession(sessionId: String, homePath: String)
    /** Returns the [SessionInfo] for the currently set session, or null if unavailable. */
    fun getSessionInfo(sessionId: String, homePath: String): SessionInfo?
}

/**
 * Reads Junie session data from the file system and converts events to UI messages.
 * Delegates event-to-message mapping to [EventToMessageMapper].
 */
class SessionRepositoryImpl(
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) : SessionRepository {
    private val logger = Logger.withTag("SessionRepository")
    private var currentSessionPath: Path? = null

    override fun setSession(sessionId: String, homePath: String) {
        val path = expandPath(homePath).toPath()
            .div("sessions")
            .div(sessionId)
            .div("events.jsonl")
        currentSessionPath = path
        logger.i { "Session set to: $sessionId (Path: $path)" }
    }

    override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? {
        val expandedHome = try { expandPath(homePath) } catch (e: Exception) {
            logger.e(e) { "Error expanding path for session info: $homePath" }
            return null
        }
        val sessionDir = try {
            expandedHome.toPath().div("sessions").div(sessionId)
        } catch (e: Exception) {
            logger.e(e) { "Invalid session directory path" }
            return null
        }
        if (!fileSystem.exists(sessionDir)) {
            logger.w { "Session directory does not exist: $sessionDir" }
            return null
        }
        return try {
            val meta = fileSystem.metadata(sessionDir)
            SessionInfo(
                id = sessionId,
                path = sessionDir.toString(),
                lastModified = meta.lastModifiedAtMillis ?: 0L,
                createdAt = meta.createdAtMillis,
                workingDirectory = extractWorkingDirectory(sessionDir)
            )
        } catch (e: Exception) {
            logger.e(e) { "Error reading session info for $sessionId" }
            null
        }
    }

    override fun getMessages(): List<Message> = loadSession().messages

    override fun loadSession(): SessionLoadResult {
        val path = currentSessionPath ?: run {
            logger.w { "loadSession called but currentSessionPath is null" }
            return SessionLoadResult(emptyList(), null, 0L)
        }
        if (!fileSystem.exists(path)) {
            logger.e { "Session file does not exist: $path" }
            return SessionLoadResult(emptyList(), path, 0L)
        }

        logger.d { "Loading messages from: $path" }
        val events = mutableListOf<JunieEvent>()
        var parseErrors = 0
        fileSystem.source(path).buffer().use { source ->
            var lineCount = 0
            while (true) {
                val line = source.readUtf8Line() ?: break
                if (line.isBlank()) continue
                lineCount++
                JsonlParser.parseLine(line)
                    .onRight { events.add(it) }
                    .onLeft { parseErrors++ }
            }
            val knownCount = events.count { it !is UnknownJunieEvent && (it !is SessionA2uxEvent || it.event.agentEvent !is UnknownAgentEvent) }
            val unknownTopLevel = events.count { it is UnknownJunieEvent }
            val unknownNested = events.count { it is SessionA2uxEvent && it.event.agentEvent is UnknownAgentEvent }
            logger.i { "Session loaded: $lineCount lines, ${events.size} events (known=$knownCount, unknownTopLevel=$unknownTopLevel, unknownNested=$unknownNested, parseErrors=$parseErrors)" }
            if (unknownTopLevel + unknownNested > 0) {
                logger.w { "Unknown event kinds found — these will appear as unsupported event indicators in the UI" }
            }
        }

        val messages = EventToMessageMapper.mapEventsToMessages(events)
        val fileSize = try { fileSystem.metadata(path).size ?: 0L } catch (_: Exception) { 0L }
        return SessionLoadResult(messages, path, fileSize)
    }

    override fun listSessions(homePath: String): List<SessionInfo> {
        if (homePath.isBlank()) {
            logger.w { "listSessions called with blank homePath" }
            return emptyList()
        }

        val expandedHome = try {
            expandPath(homePath)
        } catch (e: Exception) {
            logger.e(e) { "Error expanding path: $homePath" }
            return emptyList()
        }

        val sessionsDir = try {
            expandedHome.toPath().div("sessions")
        } catch (e: Exception) {
            logger.e(e) { "Invalid sessions directory path: $expandedHome" }
            return emptyList()
        }

        logger.d { "Listing sessions from: $sessionsDir" }
        
        if (!fileSystem.exists(sessionsDir)) {
            logger.w { "Sessions directory does not exist: $sessionsDir" }
            return emptyList()
        }

        return try {
            fileSystem.list(sessionsDir)
                .filter { fileSystem.metadata(it).isDirectory }
                .map { dir ->
                    val meta = fileSystem.metadata(dir)
                    SessionInfo(
                        id = dir.name,
                        path = dir.toString(),
                        lastModified = meta.lastModifiedAtMillis ?: 0L,
                        createdAt = meta.createdAtMillis,
                        workingDirectory = extractWorkingDirectory(dir)
                    )
                }
                .sortedByDescending { it.lastModified }
        } catch (e: Exception) {
            logger.e(e) { "Error listing sessions from $sessionsDir" }
            emptyList()
        }
    }

    /**
     * Scans a session's events.jsonl for the first AgentStateUpdatedEvent containing
     * a `currentDirectory` field and returns it.
     */
    private fun extractWorkingDirectory(sessionDir: Path): String? {
        val eventsFile = sessionDir / "events.jsonl"
        if (!fileSystem.exists(eventsFile)) return null

        return try {
            fileSystem.source(eventsFile).buffer().use { source ->
                while (true) {
                    val line = source.readUtf8Line() ?: break
                    if (!line.contains("currentDirectory")) continue
                    try {
                        val root = Json.parseToJsonElement(line).jsonObject
                        val agentEvent = root["event"]?.jsonObject?.get("agentEvent")?.jsonObject

                        val directDir = agentEvent?.get("currentDirectory")?.jsonPrimitive?.content
                        if (!directDir.isNullOrBlank()) return@use directDir

                        val blob = agentEvent?.get("blob")?.jsonPrimitive?.content
                        if (blob != null) {
                            val blobJson = Json.parseToJsonElement(blob).jsonObject
                            val dir = blobJson["currentDirectory"]?.jsonPrimitive?.content
                            if (!dir.isNullOrBlank()) return@use dir
                        }
                    } catch (_: Exception) {
                        // Skip malformed lines
                    }
                }
                null
            }
        } catch (e: Exception) {
            logger.d { "Could not extract working directory from $eventsFile: ${e.message}" }
            null
        }
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
