package com.knowledgespike.junieviewer.data

import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.getPlatform
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer

interface SessionRepository {
    fun getMessages(): List<Message>
    fun listSessions(homePath: String): List<SessionInfo>
    fun setSession(sessionId: String, homePath: String)
}

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

    override fun getMessages(): List<Message> {
        val path = currentSessionPath ?: run {
            logger.w { "getMessages called but currentSessionPath is null" }
            return emptyList()
        }
        if (!fileSystem.exists(path)) {
            logger.e { "Session file does not exist: $path" }
            return emptyList()
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

        return mapEventsToMessages(events)
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
                    SessionInfo(
                        id = dir.name,
                        path = dir.toString(),
                        lastModified = fileSystem.metadata(dir).lastModifiedAtMillis ?: 0L
                    )
                }
                .sortedByDescending { it.lastModified }
        } catch (e: Exception) {
            logger.e(e) { "Error listing sessions from $sessionsDir" }
            emptyList()
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

    /**
     * Maps parsed [JunieEvent] instances to UI [Message] objects.
     * Unknown events are mapped to visible unsupported-event indicators.
     * Metadata-only known events are silently skipped (no UI representation needed).
     */
    private fun mapEventsToMessages(events: List<JunieEvent>): List<Message> {
        return events.mapIndexedNotNull { index, event ->
            when (event) {
                is UserPromptEvent -> Message(
                    id = "${index}-${event.requestId}",
                    sender = Sender.Human,
                    content = MessageContent.Text(event.prompt),
                    kind = MessageKind.Text
                )
                is SessionA2uxEvent -> mapAgentEventToMessage(index, event)
                is UnknownJunieEvent -> Message(
                    id = "${index}-unknown-${event.timestampMs ?: event.hashCode()}",
                    sender = Sender.Junie,
                    content = MessageContent.Text("Unsupported event: ${event.kind}"),
                    kind = MessageKind.Unsupported
                )
                // Metadata-only top-level events — parsed correctly but not rendered
                is TaskStartedEvent -> null
                is TaskState -> null
                is UserMessagesCommittedToHistory -> null
                is UserAsyncResponseEvent -> null
            }
        }
    }

    /**
     * Maps a [SessionA2uxEvent] to a UI [Message] based on the nested [AgentEvent] type.
     * UI-relevant events produce messages; metadata-only events return null;
     * unknown events produce visible unsupported-event indicators.
     */
    private fun mapAgentEventToMessage(index: Int, event: SessionA2uxEvent): Message? {
        val agentEvent = event.event.agentEvent
        val ts = event.timestampMs

        return when (agentEvent) {
            is ResultBlockUpdatedEvent -> {
                val content = agentEvent.result
                if (!content.isNullOrBlank()) {
                    Message(
                        id = "${index}-${ts ?: "res-${event.hashCode()}"}",
                        sender = Sender.Junie,
                        content = MessageContent.Text(content),
                        kind = MessageKind.Text
                    )
                } else null
            }
            is AgentThoughtBlockUpdatedEvent -> {
                val content = agentEvent.text
                if (!content.isNullOrBlank()) {
                    Message(
                        id = "${index}-${ts ?: "thought-${event.hashCode()}"}",
                        sender = Sender.Junie,
                        content = MessageContent.Text(content),
                        kind = MessageKind.Thought
                    )
                } else null
            }
            is AgentPatchCreatedEvent -> {
                val content = agentEvent.patch
                if (!content.isNullOrBlank()) {
                    Message(
                        id = "${index}-${ts ?: "patch-${event.hashCode()}"}",
                        sender = Sender.Junie,
                        content = MessageContent.Diff(content),
                        kind = MessageKind.Patch
                    )
                } else null
            }
            is ToolBlockUpdatedEvent -> {
                val content = agentEvent.toolCall
                if (!content.isNullOrBlank()) {
                    Message(
                        id = "${index}-${ts ?: "tool-${event.hashCode()}"}",
                        sender = Sender.Junie,
                        content = MessageContent.Code(content, "json"),
                        kind = MessageKind.Tool
                    )
                } else null
            }
            is TerminalBlockUpdatedEvent -> {
                val content = buildString {
                    if (!agentEvent.command.isNullOrBlank()) append("$ ${agentEvent.command}\n")
                    if (!agentEvent.output.isNullOrBlank()) append(agentEvent.output)
                }
                if (content.isNotBlank()) {
                    Message(
                        id = "${index}-${ts ?: "term-${event.hashCode()}"}",
                        sender = Sender.Junie,
                        content = MessageContent.Terminal(content),
                        kind = MessageKind.Terminal
                    )
                } else null
            }
            is UnknownAgentEvent -> Message(
                id = "${index}-${ts ?: "unknown-agent-${event.hashCode()}"}",
                sender = Sender.Junie,
                content = MessageContent.Text("Unsupported event: ${agentEvent.kind}"),
                kind = MessageKind.Unsupported
            )
            // Metadata-only agent events — parsed correctly but not rendered
            else -> null
        }
    }
}
