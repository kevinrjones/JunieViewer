package com.knowledgespike.junieviewer.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

// ---------------------------------------------------------------------------
// AgentEventWrapper — bridges SessionA2uxEvent to nested AgentEvent
// ---------------------------------------------------------------------------

@Serializable
data class AgentEventWrapper(
    val state: String? = null,
    val agentEvent: AgentEvent
)

// ---------------------------------------------------------------------------
// AgentEvent sealed hierarchy — nested events inside SessionA2uxEvent
// ---------------------------------------------------------------------------

/**
 * Nested agent event within a [SessionA2uxEvent].
 * Uses a custom polymorphic serializer that falls back to [UnknownAgentEvent]
 * for any unrecognised `kind` value.
 */
@Serializable(with = AgentEventSerializer::class)
sealed interface AgentEvent {
    /** Discriminator value — defaults to the simple class name, matching the JSONL `kind` field. */
    val kind: String get() = this::class.simpleName ?: "unknown"

    /**
     * Maps this agent event to a UI [Message], or null if this event has no UI representation.
     * Each agent event type implements its own mapping logic (Strategy pattern, Q1).
     */
    fun toMessage(context: MappingContext): Message?
}

/** Builds a Message with a stable line-based id (F9, Q3). */
private fun buildMessage(
    context: MappingContext, sender: Sender, content: MessageContent, kind: MessageKind
) = Message(id = "line-${context.lineNumber}", sender = sender, content = content, kind = kind)

// -- Known agent events --

/** Junie's internal thought/reasoning block. */
@Serializable
data class AgentThoughtBlockUpdatedEvent(
    val text: String? = null,
    val stepId: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? =
        text?.takeIf { it.isNotBlank() }?.let {
            buildMessage(context, Sender.Junie, MessageContent.Text(it), MessageKind.Thought)
        }
}

/** A patch (diff) created by Junie. */
@Serializable
data class AgentPatchCreatedEvent(
    val patch: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? =
        patch?.takeIf { it.isNotBlank() }?.let {
            buildMessage(context, Sender.Junie, MessageContent.Diff(it), MessageKind.Patch)
        }
}

/** Final result block from Junie's response. */
@Serializable
data class ResultBlockUpdatedEvent(
    val result: String? = null,
    val stepId: String? = null,
    val cancelled: Boolean? = null,
    val changes: List<FileChange>? = null,
    val errorCode: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? =
        result?.takeIf { it.isNotBlank() }?.let {
            buildMessage(context, Sender.Junie, MessageContent.Text(it), MessageKind.Text)
        }
}

/** Tool invocation block. */
@Serializable
data class ToolBlockUpdatedEvent(
    val toolCall: String? = null,
    val stepId: String? = null,
    val text: String? = null,
    val status: String? = null,
    val details: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? =
        toolCall?.takeIf { it.isNotBlank() }?.let {
            buildMessage(context, Sender.Junie, MessageContent.Code(it, "json"), MessageKind.Tool)
        }
}

/** Terminal command execution block. */
@Serializable
data class TerminalBlockUpdatedEvent(
    val command: String? = null,
    val output: String? = null,
    val stepId: String? = null,
    val status: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? {
        val content = buildString {
            if (!command.isNullOrBlank()) append("$ $command\n")
            if (!output.isNullOrBlank()) append(output)
        }
        return content.takeIf { it.isNotBlank() }?.let {
            buildMessage(context, Sender.Junie, MessageContent.Terminal(it), MessageKind.Terminal)
        }
    }
}

/** Agent status update (metadata-only). */
@Serializable
data object AgentCurrentStatusUpdatedEvent : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Agent task name update (metadata-only). */
@Serializable
data class AgentTaskNameUpdatedEvent(val name: String? = null) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Agent plan update (metadata-only). */
@Serializable
data class AgentPlanUpdatedEvent(
    val plan: String? = null,
    val items: List<PlanItem>? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Available pull requests metadata event. */
@Serializable
data class AvailablePullRequestsEvent(
    val pullRequests: PayloadValue? = null,
    val agent: AgentIdentity? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** LLM response metadata (token counts, model info). */
@Serializable
data class LlmResponseMetadataEvent(
    val model: String? = null,
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
    val modelUsage: List<ModelUsage>? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Current working directory update (metadata-only). */
@Serializable
data class CurrentDirectoryUpdatedEvent(
    // The JSONL emits this field as `currentDirectory`; accept both names.
    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("currentDirectory")
    val directory: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Environment variables update (metadata-only). */
@Serializable
data class EnvironmentVariablesUpdatedEvent(
    val variables: PayloadValue? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** View files block update — files Junie is examining. */
@Serializable
data class ViewFilesBlockUpdatedEvent(
    val files: List<ViewedFile>? = null,
    val stepId: String? = null,
    val status: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Context window usage report (metadata-only). */
@Serializable
data class ContextWindowReportEvent(
    val usedTokens: Int? = null,
    val maxTokens: Int? = null,
    val percentage: Double? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** File changes block update — files Junie has modified. */
@Serializable
data class FileChangesBlockUpdatedEvent(
    val changes: List<FileChange>? = null,
    val stepId: String? = null,
    val status: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Tip suggestion for the user (metadata-only). */
@Serializable
data class TipSuggestionCreatedEvent(
    val tip: String? = null,
    val id: String? = null,
    val description: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Plan progress indicator. */
@Serializable
data class ShowPlanProgressEvent(
    val progress: PayloadValue? = null,
    val items: List<PlanItem>? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Next prompt suggestion for the user. */
@Serializable
data class NextPromptSuggestionEvent(
    val suggestion: List<PromptSuggestion>? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Async request update (e.g. HITL approval request). */
@Serializable
data class AskAsyncRequestUpdatedEvent(
    val requestId: String? = null,
    val question: String? = null,
    val stepId: String? = null,
    val title: String? = null,
    val request: AsyncRequest? = null,
    val status: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Authorization availability status (metadata-only). */
@Serializable
data class AuthorizationAvailabilityEvent(
    val available: Boolean? = null,
    val agent: AgentIdentity? = null,
    val authorized: Boolean? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Agent started indicator (metadata-only). */
@Serializable
data class AgentStartedEvent(
    val agentId: String? = null,
    val agent: AgentIdentity? = null,
    val stepId: String? = null,
    val agentType: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Plan suggestion from the agent. */
@Serializable
data class SuggestPlanEvent(
    val plan: PayloadValue? = null,
    val sections: List<PlanSection>? = null,
    val deliveryPlan: List<PlanItem>? = null,
    val readyForReview: Boolean? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Test execution block — when Junie runs tests. */
@Serializable
data class TestRunBlockUpdatedEvent(
    val stepId: String? = null,
    val status: String? = null,
    val name: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? {
        val label = buildString {
            append("🧪 Test: ${name ?: "unknown"}")
            if (status != null) append(" [$status]")
        }
        return buildMessage(context, Sender.Junie, MessageContent.Text(label), MessageKind.TestRun)
    }
}

/** MCP (Model Context Protocol) tool invocation. */
@Serializable
data class McpBlockUpdatedEvent(
    val stepId: String? = null,
    val toolName: String? = null,
    val status: String? = null,
    val details: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? {
        val label = buildString {
            append("MCP: ${toolName ?: "unknown"}")
            if (status != null) append(" [$status]")
            if (!details.isNullOrBlank()) append("\n$details")
        }
        return buildMessage(context, Sender.Junie, MessageContent.Code(label, "json"), MessageKind.Mcp)
    }
}

/** Custom subagent invocation block. */
@Serializable
data class CustomAgentBlockUpdatedEvent(
    val stepId: String? = null,
    val name: String? = null,
    val status: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = buildMessage(
        context, Sender.Junie,
        MessageContent.Text("${name ?: "Unnamed sub-agent"} [${status ?: "unknown"}]"),
        MessageKind.SubAgent
    )
}

/** Agent-level failure (LLM connection issues, errors). */
@Serializable
data class AgentFailureEvent(
    val message: String? = null,
    val errorCode: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = buildMessage(
        context, Sender.Junie,
        MessageContent.Text(message ?: "Agent failure"),
        MessageKind.Error
    )
}

/** Serialized snapshot of the agent's internal state (metadata-only). */
@Serializable
data class AgentStateUpdatedEvent(
    val blob: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Synchronous question from the agent to the user. */
@Serializable
data class AskRequestUpdatedEvent(
    val stepId: String? = null,
    val title: String? = null,
    val askRequest: AskRequest? = null,
    val status: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? {
        val questionText = buildString {
            if (!title.isNullOrBlank()) append("$title\n")
            val ask = askRequest
            if (ask != null) {
                when {
                    ask.unstructuredText != null -> append(ask.unstructuredText)
                    !ask.question.isNullOrBlank() -> append(ask.question)
                }
            }
        }
        return questionText.takeIf { it.isNotBlank() }?.let {
            buildMessage(context, Sender.Junie, MessageContent.Text(it), MessageKind.Question)
        }
    }
}

/** Presents the user with a set of choices. */
@Serializable
data class ChoiceRequestUpdatedEvent(
    val stepId: String? = null,
    val title: String? = null,
    val choiceRequest: ChoiceRequest? = null,
    val status: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? {
        val choiceText = buildString {
            if (!title.isNullOrBlank()) append("$title\n")
            val choice = choiceRequest
            if (choice != null) {
                if (choice.unstructuredText != null) {
                    append(choice.unstructuredText)
                } else {
                    choice.options?.forEach { opt ->
                        append("• ${opt.description ?: opt.id ?: "option"}\n")
                    }
                }
            }
        }
        return choiceText.takeIf { it.isNotBlank() }?.let {
            buildMessage(context, Sender.Junie, MessageContent.Text(it), MessageKind.Choice)
        }
    }
}

/** Standalone markdown text block from the agent. */
@Serializable
data class MarkdownBlockUpdatedEvent(
    val stepId: String? = null,
    val text: String? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? =
        text?.takeIf { it.isNotBlank() }?.let {
            buildMessage(context, Sender.Junie, MessageContent.Text(it), MessageKind.Markdown)
        }
}

/** Subagent spawn event — records when Junie delegates work to a sub-agent. */
@Serializable
data class SubagentSpawnedEvent(
    val name: String? = null,
    val task: String? = null,
    val stepId: String? = null,
    val agent: AgentIdentity? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? {
        val label = buildString {
            append("Sub-agent spawned: ${name ?: "unnamed"}")
            if (!task.isNullOrBlank()) {
                val preview = if (task.length > 200) task.take(200) + "…" else task
                append("\nTask: $preview")
            }
        }
        return buildMessage(context, Sender.Junie, MessageContent.Text(label), MessageKind.SubAgent)
    }
}

/** Task-level failure event — tolerant nullable model since no real payload examples exist. */
@Serializable
data class AgentTaskFailedEvent(
    val message: String? = null,
    val errorCode: String? = null,
    val taskId: String? = null,
    val stepId: String? = null,
    val details: PayloadValue? = null
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? {
        val text = buildString {
            append("Task Failed")
            if (!message.isNullOrBlank()) append(": $message")
            if (!errorCode.isNullOrBlank()) append(" [$errorCode]")
            if (!taskId.isNullOrBlank()) append("\nTask: $taskId")
            if (!stepId.isNullOrBlank()) append("\nStep: $stepId")
            if (details != null) append("\nDetails: $details")
            if (message.isNullOrBlank() && errorCode.isNullOrBlank() &&
                taskId.isNullOrBlank() && details == null
            ) {
                append("\nJunie task failed with no additional details.")
            }
        }
        return buildMessage(context, Sender.Junie, MessageContent.Text(text), MessageKind.Error)
    }
}

/**
 * Fallback for any nested agent event kind not yet modelled.
 * Preserves the raw JSON so no data is lost.
 */
data class UnknownAgentEvent(
    override val kind: String,
    val raw: PayloadValue.ObjectValue
) : AgentEvent {
    override fun toMessage(context: MappingContext): Message? = buildMessage(
        context, Sender.Junie,
        MessageContent.Text("Unsupported event: $kind"),
        MessageKind.Unsupported
    )
}
