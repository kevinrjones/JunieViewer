package com.knowledgespike.junieviewer.data

import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.domain.*
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Pure transformation from parsed [JunieEvent] instances to UI [Message] objects.
 * Unknown events are mapped to visible unsupported-event indicators.
 * Metadata-only known events are silently skipped (no UI representation needed).
 *
 * Extracted from SessionRepository to separate concerns and enable independent testing.
 */
object EventToMessageMapper {

    private val logger = Logger.withTag("EventToMessageMapper")

    /** Maps a list of parsed events to UI messages, filtering out metadata-only events. */
    fun mapEventsToMessages(events: List<JunieEvent>): List<Message> =
        events.mapIndexedNotNull { index, event -> mapTopLevelEvent(index, event) }

    private fun mapTopLevelEvent(index: Int, event: JunieEvent): Message? = when (event) {
        is UserPromptEvent -> buildMessage(
            index = index,
            tag = event.requestId ?: "prompt-${event.hashCode()}",
            sender = Sender.Human,
            content = MessageContent.Text(event.prompt),
            kind = MessageKind.Text
        )
        is SessionA2uxEvent -> mapAgentEventToMessage(index, event)
        is UnknownJunieEvent -> buildMessage(
            index = index,
            tag = "unknown-${event.timestampMs ?: event.hashCode()}",
            sender = Sender.Junie,
            content = MessageContent.Text("Unsupported event: ${event.kind}"),
            kind = MessageKind.Unsupported
        )
        is SystemMessageEvent -> {
            val text = buildString {
                append(event.text)
                if (!event.details.isNullOrBlank()) append("\n\n${event.details}")
            }
            buildMessage(index, "system-${event.hashCode()}", Sender.Junie, MessageContent.Text(text), MessageKind.SystemMessage)
        }
        is CancelAgentEvent -> buildMessage(index, "cancel", Sender.Human, MessageContent.Text("⛔ Agent cancelled"), MessageKind.Cancelled)
        is TaskContinueStopped -> buildMessage(index, "continue-stopped", Sender.Junie, MessageContent.Text("Continue stopped"), MessageKind.Status)
        is UserResponseEvent -> buildMessage(index, "response", Sender.Human, MessageContent.Text(event.prompt), MessageKind.Text)
        // Metadata-only top-level events — parsed correctly but not rendered
        is TaskStartedEvent -> null
        is TaskState -> null
        is UserMessagesCommittedToHistory -> null
        is UserAsyncResponseEvent -> null
        is SendToAgentEvent -> null
        is SessionTitleSetEvent -> null
        is SkillsStatusEvent -> null
    }

    private fun mapAgentEventToMessage(index: Int, event: SessionA2uxEvent): Message? {
        val agentEvent = event.event.agentEvent
        val ts = event.timestampMs

        return when (agentEvent) {
            is ResultBlockUpdatedEvent -> agentEvent.result.toMessageOrNull(index, ts, "res", Sender.Junie, MessageKind.Text)
            is AgentThoughtBlockUpdatedEvent -> agentEvent.text.toMessageOrNull(index, ts, "thought", Sender.Junie, MessageKind.Thought)
            is AgentPatchCreatedEvent -> agentEvent.patch?.takeIf { it.isNotBlank() }?.let {
                buildAgentMessage(index, ts, "patch", Sender.Junie, MessageContent.Diff(it), MessageKind.Patch)
            }
            is ToolBlockUpdatedEvent -> agentEvent.toolCall?.takeIf { it.isNotBlank() }?.let {
                buildAgentMessage(index, ts, "tool", Sender.Junie, MessageContent.Code(it, "json"), MessageKind.Tool)
            }
            is TerminalBlockUpdatedEvent -> {
                val content = buildString {
                    if (!agentEvent.command.isNullOrBlank()) append("$ ${agentEvent.command}\n")
                    if (!agentEvent.output.isNullOrBlank()) append(agentEvent.output)
                }
                content.takeIf { it.isNotBlank() }?.let {
                    buildAgentMessage(index, ts, "term", Sender.Junie, MessageContent.Terminal(it), MessageKind.Terminal)
                }
            }
            is TestRunBlockUpdatedEvent -> {
                val label = buildString {
                    append("🧪 Test: ${agentEvent.name ?: "unknown"}")
                    if (agentEvent.status != null) append(" [${agentEvent.status}]")
                }
                buildAgentMessage(index, ts, "test", Sender.Junie, MessageContent.Text(label), MessageKind.TestRun)
            }
            is McpBlockUpdatedEvent -> {
                val label = buildString {
                    append("MCP: ${agentEvent.toolName ?: "unknown"}")
                    if (agentEvent.status != null) append(" [${agentEvent.status}]")
                    if (!agentEvent.details.isNullOrBlank()) append("\n${agentEvent.details}")
                }
                buildAgentMessage(index, ts, "mcp", Sender.Junie, MessageContent.Code(label, "json"), MessageKind.Mcp)
            }
            is CustomAgentBlockUpdatedEvent -> buildAgentMessage(
                index, ts, "subagent", Sender.Junie,
                MessageContent.Text("${agentEvent.name ?: "Unnamed sub-agent"} [${agentEvent.status ?: "unknown"}]"),
                MessageKind.SubAgent
            )
            is SubagentSpawnedEvent -> {
                val label = buildString {
                    append("Sub-agent spawned: ${agentEvent.name ?: "unnamed"}")
                    if (!agentEvent.task.isNullOrBlank()) {
                        val preview = if (agentEvent.task.length > 200) agentEvent.task.take(200) + "…" else agentEvent.task
                        append("\nTask: $preview")
                    }
                }
                buildAgentMessage(index, ts, "subagent-spawned", Sender.Junie, MessageContent.Text(label), MessageKind.SubAgent)
            }
            is AgentFailureEvent -> buildAgentMessage(
                index, ts, "failure", Sender.Junie,
                MessageContent.Text(agentEvent.message ?: "Agent failure"),
                MessageKind.Error
            )
            is AgentTaskFailedEvent -> {
                val text = buildString {
                    append("Task Failed")
                    if (!agentEvent.message.isNullOrBlank()) append(": ${agentEvent.message}")
                    if (!agentEvent.errorCode.isNullOrBlank()) append(" [${agentEvent.errorCode}]")
                    if (!agentEvent.taskId.isNullOrBlank()) append("\nTask: ${agentEvent.taskId}")
                    if (!agentEvent.stepId.isNullOrBlank()) append("\nStep: ${agentEvent.stepId}")
                    if (agentEvent.details != null) append("\nDetails: ${agentEvent.details}")
                    if (agentEvent.message.isNullOrBlank() && agentEvent.errorCode.isNullOrBlank() &&
                        agentEvent.taskId.isNullOrBlank() && agentEvent.details == null
                    ) {
                        append("\nJunie task failed with no additional details.")
                    }
                }
                buildAgentMessage(index, ts, "task-failed", Sender.Junie, MessageContent.Text(text), MessageKind.Error)
            }
            is AskRequestUpdatedEvent -> {
                val questionText = buildString {
                    if (!agentEvent.title.isNullOrBlank()) append("${agentEvent.title}\n")
                    val askObj = agentEvent.askRequest
                    if (askObj != null) {
                        try {
                            val q = askObj.jsonObject["question"]?.jsonPrimitive?.content
                            if (!q.isNullOrBlank()) append(q)
                        } catch (e: Exception) {
                            logger.w(e) { "Failed to parse askRequest JSON" }
                            append(askObj.toString())
                        }
                    }
                }
                questionText.takeIf { it.isNotBlank() }?.let {
                    buildAgentMessage(index, ts, "ask", Sender.Junie, MessageContent.Text(it), MessageKind.Question)
                }
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
                        } catch (e: Exception) {
                            logger.w(e) { "Failed to parse choiceRequest JSON" }
                            append(choiceObj.toString())
                        }
                    }
                }
                choiceText.takeIf { it.isNotBlank() }?.let {
                    buildAgentMessage(index, ts, "choice", Sender.Junie, MessageContent.Text(it), MessageKind.Choice)
                }
            }
            is MarkdownBlockUpdatedEvent -> agentEvent.text.toMessageOrNull(index, ts, "md", Sender.Junie, MessageKind.Markdown)
            is UnknownAgentEvent -> buildAgentMessage(
                index, ts, "unknown-agent", Sender.Junie,
                MessageContent.Text("Unsupported event: ${agentEvent.kind}"),
                MessageKind.Unsupported
            )
            // Metadata-only agent events — parsed correctly but not rendered.
            // Listed explicitly so the compiler enforces exhaustiveness when new subclasses are added.
            is AgentCurrentStatusUpdatedEvent -> null
            is AgentTaskNameUpdatedEvent -> null
            is AgentPlanUpdatedEvent -> null
            is AvailablePullRequestsEvent -> null
            is LlmResponseMetadataEvent -> null
            is CurrentDirectoryUpdatedEvent -> null
            is EnvironmentVariablesUpdatedEvent -> null
            is ViewFilesBlockUpdatedEvent -> null
            is ContextWindowReportEvent -> null
            is FileChangesBlockUpdatedEvent -> null
            is TipSuggestionCreatedEvent -> null
            is ShowPlanProgressEvent -> null
            is NextPromptSuggestionEvent -> null
            is AskAsyncRequestUpdatedEvent -> null
            is AuthorizationAvailabilityEvent -> null
            is AgentStartedEvent -> null
            is SuggestPlanEvent -> null
            is AgentStateUpdatedEvent -> null
        }
    }

    // -- Helper functions to eliminate repeated Message construction --

    /** Builds a Message with a deterministic id from index and tag. */
    private fun buildMessage(
        index: Int, tag: String, sender: Sender, content: MessageContent, kind: MessageKind
    ) = Message(id = "$index-$tag", sender = sender, content = content, kind = kind)

    /** Builds a Message for an agent event, using timestampMs for the id when available. */
    private fun buildAgentMessage(
        index: Int, ts: Long?, tag: String, sender: Sender, content: MessageContent, kind: MessageKind
    ) = Message(id = "$index-${ts ?: "$tag-${content.hashCode()}"}", sender = sender, content = content, kind = kind)

    /** Converts a nullable string to a text Message, returning null if blank. */
    private fun String?.toMessageOrNull(
        index: Int, ts: Long?, tag: String, sender: Sender, kind: MessageKind
    ): Message? = this?.takeIf { it.isNotBlank() }?.let {
        buildAgentMessage(index, ts, tag, sender, MessageContent.Text(it), kind)
    }
}
