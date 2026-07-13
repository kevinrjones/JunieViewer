package com.knowledgespike.junieviewer.data

import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.getPlatform
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
     * a `currentDirectory` field and returns it. This is the directory Junie was
     * operating in during the session.
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

                        // Try direct field first (CurrentDirectoryUpdatedEvent)
                        val directDir = agentEvent?.get("currentDirectory")?.jsonPrimitive?.content
                        if (!directDir.isNullOrBlank()) return@use directDir

                        // Try nested blob (AgentStateUpdatedEvent)
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
                is SystemMessageEvent -> Message(
                    id = "${index}-system-${event.hashCode()}",
                    sender = Sender.Junie,
                    content = MessageContent.Text(
                        buildString {
                            append(event.text)
                            if (!event.details.isNullOrBlank()) append("\n\n${event.details}")
                        }
                    ),
                    kind = MessageKind.SystemMessage
                )
                is CancelAgentEvent -> Message(
                    id = "${index}-cancel",
                    sender = Sender.Human,
                    content = MessageContent.Text("⛔ Agent cancelled"),
                    kind = MessageKind.Cancelled
                )
                is TaskContinueStopped -> Message(
                    id = "${index}-continue-stopped",
                    sender = Sender.Junie,
                    content = MessageContent.Text("Continue stopped"),
                    kind = MessageKind.Status
                )
                is UserResponseEvent -> Message(
                    id = "${index}-response",
                    sender = Sender.Human,
                    content = MessageContent.Text(event.prompt),
                    kind = MessageKind.Text
                )
                // Metadata-only top-level events — parsed correctly but not rendered
                is TaskStartedEvent -> null
                is TaskState -> null
                is UserMessagesCommittedToHistory -> null
                is UserAsyncResponseEvent -> null
                is SendToAgentEvent -> null // flow marker, not rendered
                is SessionTitleSetEvent -> null // TODO: use to update session title in app state
                is SkillsStatusEvent -> null // metadata, not rendered
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
            is TestRunBlockUpdatedEvent -> {
                val label = buildString {
                    append("🧪 Test: ${agentEvent.name ?: "unknown"}")
                    if (agentEvent.status != null) append(" [${agentEvent.status}]")
                }
                Message(
                    id = "${index}-${ts ?: "test-${event.hashCode()}"}",
                    sender = Sender.Junie,
                    content = MessageContent.Text(label),
                    kind = MessageKind.TestRun
                )
            }
            is McpBlockUpdatedEvent -> {
                val label = buildString {
                    append("MCP: ${agentEvent.toolName ?: "unknown"}")
                    if (agentEvent.status != null) append(" [${agentEvent.status}]")
                    if (!agentEvent.details.isNullOrBlank()) append("\n${agentEvent.details}")
                }
                Message(
                    id = "${index}-${ts ?: "mcp-${event.hashCode()}"}",
                    sender = Sender.Junie,
                    content = MessageContent.Code(label, "json"),
                    kind = MessageKind.Mcp
                )
            }
            is CustomAgentBlockUpdatedEvent -> Message(
                id = "${index}-${ts ?: "subagent-${event.hashCode()}"}",
                sender = Sender.Junie,
                content = MessageContent.Text("🤖 Subagent: ${agentEvent.name ?: "unknown"} [${agentEvent.status ?: "unknown"}]"),
                kind = MessageKind.SubAgent
            )
            is AgentFailureEvent -> Message(
                id = "${index}-${ts ?: "failure-${event.hashCode()}"}",
                sender = Sender.Junie,
                content = MessageContent.Text(agentEvent.message ?: "Agent failure"),
                kind = MessageKind.Error
            )
            is AskRequestUpdatedEvent -> {
                val questionText = buildString {
                    if (!agentEvent.title.isNullOrBlank()) append("${agentEvent.title}\n")
                    // Extract question from askRequest JsonElement
                    val askObj = agentEvent.askRequest
                    if (askObj != null) {
                        try {
                            val q = askObj.jsonObject["question"]?.jsonPrimitive?.content
                            if (!q.isNullOrBlank()) append(q)
                        } catch (_: Exception) {
                            append(askObj.toString())
                        }
                    }
                }
                if (questionText.isNotBlank()) {
                    Message(
                        id = "${index}-${ts ?: "ask-${event.hashCode()}"}",
                        sender = Sender.Junie,
                        content = MessageContent.Text(questionText),
                        kind = MessageKind.Question
                    )
                } else null
            }
            is ChoiceRequestUpdatedEvent -> {
                val choiceText = buildString {
                    if (!agentEvent.title.isNullOrBlank()) append("${agentEvent.title}\n")
                    val choiceObj = agentEvent.choiceRequest
                    if (choiceObj != null) {
                        try {
                            val options = choiceObj.jsonObject["options"]?.jsonArray
                            options?.forEach { opt ->
                                val desc = opt.jsonObject["description"]?.jsonPrimitive?.content
                                val id = opt.jsonObject["id"]?.jsonPrimitive?.content
                                append("• ${desc ?: id ?: "option"}\n")
                            }
                        } catch (_: Exception) {
                            append(choiceObj.toString())
                        }
                    }
                }
                if (choiceText.isNotBlank()) {
                    Message(
                        id = "${index}-${ts ?: "choice-${event.hashCode()}"}",
                        sender = Sender.Junie,
                        content = MessageContent.Text(choiceText),
                        kind = MessageKind.Choice
                    )
                } else null
            }
            is MarkdownBlockUpdatedEvent -> {
                if (!agentEvent.text.isNullOrBlank()) {
                    Message(
                        id = "${index}-${ts ?: "md-${event.hashCode()}"}",
                        sender = Sender.Junie,
                        content = MessageContent.Text(agentEvent.text),
                        kind = MessageKind.Markdown
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
