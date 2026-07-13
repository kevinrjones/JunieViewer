package com.knowledgespike.junieviewer.domain

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

// ---------------------------------------------------------------------------
// JunieEvent sealed hierarchy — top-level events in events.jsonl
// ---------------------------------------------------------------------------

/**
 * Top-level event from a Junie events.jsonl file.
 * Uses a custom polymorphic serializer that falls back to [UnknownJunieEvent]
 * for any unrecognised `kind` value, preventing deserialization crashes.
 */
@Serializable(with = JunieEventSerializer::class)
sealed interface JunieEvent {
    val kind: String
}

// -- Known top-level events --

/** Human prompt submitted to Junie. */
@Serializable
data class UserPromptEvent(
    val prompt: String,
    val requestId: String? = null,
    val presentablePrompt: String? = null,
    val customAttachments: JsonElement? = null
) : JunieEvent {
    override val kind: String get() = "UserPromptEvent"
}

/** Wrapper for nested agent events within a session. */
@Serializable
data class SessionA2uxEvent(
    val event: AgentEventWrapper,
    val timestampMs: Long? = null
) : JunieEvent {
    override val kind: String get() = "SessionA2uxEvent"
}

/** Indicates a Junie task has started. */
@Serializable
data class TaskStartedEvent(
    val taskId: String? = null,
    val timestampMs: Long? = null
) : JunieEvent {
    override val kind: String get() = "TaskStartedEvent"
}

/** Represents a change in Junie task state. */
@Serializable
data class TaskState(
    val taskId: String? = null,
    val state: String? = null,
    val timestampMs: Long? = null
) : JunieEvent {
    override val kind: String get() = "TaskState"
}

/** Records that user messages have been committed to conversation history. */
@Serializable
data class UserMessagesCommittedToHistory(
    val requestId: String? = null,
    val userMessageIds: List<String>? = null,
    val timestampMs: Long? = null
) : JunieEvent {
    override val kind: String get() = "UserMessagesCommittedToHistory"
}

/** Async response event from the user (e.g. HITL approval). */
@Serializable
data class UserAsyncResponseEvent(
    val requestId: String? = null,
    val response: String? = null,
    val entries: JsonElement? = null,
    val timestampMs: Long? = null
) : JunieEvent {
    override val kind: String get() = "UserAsyncResponseEvent"
}

/** System-level message displayed to the user (announcements, notifications). */
@Serializable
data class SystemMessageEvent(
    val text: String,
    val details: String? = null
) : JunieEvent {
    override val kind: String get() = "SystemMessageEvent"
}

/** Signals that a message/task is being sent to the agent. */
@Serializable
data object SendToAgentEvent : JunieEvent {
    override val kind: String get() = "SendToAgentEvent"
}

/** Signals that the user cancelled the agent's current operation. */
@Serializable
data object CancelAgentEvent : JunieEvent {
    override val kind: String get() = "CancelAgentEvent"
}

/** Sets or updates the session title. */
@Serializable
data class SessionTitleSetEvent(
    val name: String,
    val timestampMs: Long? = null
) : JunieEvent {
    override val kind: String get() = "SessionTitleSetEvent"
}

/** Reports which agent skills were newly discovered/loaded. */
@Serializable
data class SkillsStatusEvent(
    val newSkills: List<String>? = null
) : JunieEvent {
    override val kind: String get() = "SkillsStatusEvent"
}

/** Indicates that a "continue" operation on a task was stopped. */
@Serializable
data object TaskContinueStopped : JunieEvent {
    override val kind: String get() = "TaskContinueStopped"
}

/** User's response to a choice or question from the agent. */
@Serializable
data class UserResponseEvent(
    val prompt: String,
    val isChoice: Boolean = false
) : JunieEvent {
    override val kind: String get() = "UserResponseEvent"
}

/**
 * Fallback for any top-level event kind not yet modelled.
 * Preserves the raw JSON so no data is lost.
 */
data class UnknownJunieEvent(
    override val kind: String,
    val timestampMs: Long? = null,
    val raw: JsonObject
) : JunieEvent

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
    val kind: String
}

// -- Known agent events (existing) --

/** Junie's internal thought/reasoning block. */
@Serializable
data class AgentThoughtBlockUpdatedEvent(
    val text: String? = null,
    val stepId: String? = null
) : AgentEvent {
    override val kind: String get() = "AgentThoughtBlockUpdatedEvent"
}

/** A patch (diff) created by Junie. */
@Serializable
data class AgentPatchCreatedEvent(
    val patch: String? = null
) : AgentEvent {
    override val kind: String get() = "AgentPatchCreatedEvent"
}

/** Final result block from Junie's response. */
@Serializable
data class ResultBlockUpdatedEvent(
    val result: String? = null,
    val stepId: String? = null,
    val cancelled: Boolean? = null,
    val changes: JsonElement? = null,
    val errorCode: String? = null
) : AgentEvent {
    override val kind: String get() = "ResultBlockUpdatedEvent"
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
    override val kind: String get() = "ToolBlockUpdatedEvent"
}

/** Terminal command execution block. */
@Serializable
data class TerminalBlockUpdatedEvent(
    val command: String? = null,
    val output: String? = null,
    val stepId: String? = null,
    val status: String? = null
) : AgentEvent {
    override val kind: String get() = "TerminalBlockUpdatedEvent"
}

/** Agent status update (metadata-only). */
@Serializable
data object AgentCurrentStatusUpdatedEvent : AgentEvent {
    override val kind: String get() = "AgentCurrentStatusUpdatedEvent"
}

/** Agent task name update (metadata-only). */
@Serializable
data class AgentTaskNameUpdatedEvent(val name: String? = null) : AgentEvent {
    override val kind: String get() = "AgentTaskNameUpdatedEvent"
}

/** Agent plan update (metadata-only). */
@Serializable
data class AgentPlanUpdatedEvent(
    val plan: String? = null,
    val items: JsonElement? = null
) : AgentEvent {
    override val kind: String get() = "AgentPlanUpdatedEvent"
}

// -- Known agent events (newly added — Phase A) --

/** Available pull requests metadata event. */
@Serializable
data class AvailablePullRequestsEvent(
    val pullRequests: JsonElement? = null,
    val agent: String? = null
) : AgentEvent {
    override val kind: String get() = "AvailablePullRequestsEvent"
}

/** LLM response metadata (token counts, model info). */
@Serializable
data class LlmResponseMetadataEvent(
    val model: String? = null,
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
    val modelUsage: JsonElement? = null
) : AgentEvent {
    override val kind: String get() = "LlmResponseMetadataEvent"
}

/** Current working directory update (metadata-only). */
@Serializable
data class CurrentDirectoryUpdatedEvent(
    val directory: String? = null
) : AgentEvent {
    override val kind: String get() = "CurrentDirectoryUpdatedEvent"
}

/** Environment variables update (metadata-only). */
@Serializable
data class EnvironmentVariablesUpdatedEvent(
    val variables: JsonElement? = null
) : AgentEvent {
    override val kind: String get() = "EnvironmentVariablesUpdatedEvent"
}

/** View files block update — files Junie is examining. */
@Serializable
data class ViewFilesBlockUpdatedEvent(
    val files: JsonElement? = null,
    val stepId: String? = null,
    val status: String? = null
) : AgentEvent {
    override val kind: String get() = "ViewFilesBlockUpdatedEvent"
}

/** Context window usage report (metadata-only). */
@Serializable
data class ContextWindowReportEvent(
    val usedTokens: Int? = null,
    val maxTokens: Int? = null,
    val percentage: JsonElement? = null
) : AgentEvent {
    override val kind: String get() = "ContextWindowReportEvent"
}

/** File changes block update — files Junie has modified. */
@Serializable
data class FileChangesBlockUpdatedEvent(
    val changes: JsonElement? = null,
    val stepId: String? = null,
    val status: String? = null
) : AgentEvent {
    override val kind: String get() = "FileChangesBlockUpdatedEvent"
}

/** Tip suggestion for the user (metadata-only). */
@Serializable
data class TipSuggestionCreatedEvent(
    val tip: String? = null,
    val id: String? = null,
    val description: String? = null
) : AgentEvent {
    override val kind: String get() = "TipSuggestionCreatedEvent"
}

/** Plan progress indicator. */
@Serializable
data class ShowPlanProgressEvent(
    val progress: JsonElement? = null,
    val items: JsonElement? = null
) : AgentEvent {
    override val kind: String get() = "ShowPlanProgressEvent"
}

/** Next prompt suggestion for the user. */
@Serializable
data class NextPromptSuggestionEvent(
    val suggestion: JsonElement? = null
) : AgentEvent {
    override val kind: String get() = "NextPromptSuggestionEvent"
}

/** Async request update (e.g. HITL approval request). */
@Serializable
data class AskAsyncRequestUpdatedEvent(
    val requestId: String? = null,
    val question: String? = null,
    val stepId: String? = null,
    val title: String? = null,
    val request: JsonElement? = null,
    val status: String? = null
) : AgentEvent {
    override val kind: String get() = "AskAsyncRequestUpdatedEvent"
}

/** Authorization availability status (metadata-only). */
@Serializable
data class AuthorizationAvailabilityEvent(
    val available: Boolean? = null,
    val agent: String? = null,
    val authorized: Boolean? = null
) : AgentEvent {
    override val kind: String get() = "AuthorizationAvailabilityEvent"
}

/** Agent started indicator (metadata-only). */
@Serializable
data class AgentStartedEvent(
    val agentId: String? = null,
    val agent: String? = null,
    val stepId: String? = null,
    val agentType: String? = null
) : AgentEvent {
    override val kind: String get() = "AgentStartedEvent"
}

/** Plan suggestion from the agent. */
@Serializable
data class SuggestPlanEvent(
    val plan: JsonElement? = null,
    val sections: JsonElement? = null,
    val deliveryPlan: JsonElement? = null,
    val readyForReview: Boolean? = null
) : AgentEvent {
    override val kind: String get() = "SuggestPlanEvent"
}

// -- Newly added agent events --

/** Test execution block — when Junie runs tests. */
@Serializable
data class TestRunBlockUpdatedEvent(
    val stepId: String? = null,
    val status: String? = null,
    val name: String? = null
) : AgentEvent {
    override val kind: String get() = "TestRunBlockUpdatedEvent"
}

/** MCP (Model Context Protocol) tool invocation. */
@Serializable
data class McpBlockUpdatedEvent(
    val stepId: String? = null,
    val toolName: String? = null,
    val status: String? = null,
    val details: String? = null
) : AgentEvent {
    override val kind: String get() = "McpBlockUpdatedEvent"
}

/** Custom subagent invocation block. */
@Serializable
data class CustomAgentBlockUpdatedEvent(
    val stepId: String? = null,
    val name: String? = null,
    val status: String? = null
) : AgentEvent {
    override val kind: String get() = "CustomAgentBlockUpdatedEvent"
}

/** Agent-level failure (LLM connection issues, errors). */
@Serializable
data class AgentFailureEvent(
    val message: String? = null,
    val errorCode: String? = null
) : AgentEvent {
    override val kind: String get() = "AgentFailureEvent"
}

/** Serialized snapshot of the agent's internal state (metadata-only). */
@Serializable
data class AgentStateUpdatedEvent(
    val blob: String? = null
) : AgentEvent {
    override val kind: String get() = "AgentStateUpdatedEvent"
}

/** Synchronous question from the agent to the user. */
@Serializable
data class AskRequestUpdatedEvent(
    val stepId: String? = null,
    val title: String? = null,
    val askRequest: JsonElement? = null,
    val status: String? = null
) : AgentEvent {
    override val kind: String get() = "AskRequestUpdatedEvent"
}

/** Presents the user with a set of choices. */
@Serializable
data class ChoiceRequestUpdatedEvent(
    val stepId: String? = null,
    val title: String? = null,
    val choiceRequest: JsonElement? = null,
    val status: String? = null
) : AgentEvent {
    override val kind: String get() = "ChoiceRequestUpdatedEvent"
}

/** Standalone markdown text block from the agent. */
@Serializable
data class MarkdownBlockUpdatedEvent(
    val stepId: String? = null,
    val text: String? = null
) : AgentEvent {
    override val kind: String get() = "MarkdownBlockUpdatedEvent"
}

/**
 * Fallback for any nested agent event kind not yet modelled.
 * Preserves the raw JSON so no data is lost.
 */
data class UnknownAgentEvent(
    override val kind: String,
    val raw: JsonObject
) : AgentEvent

// ---------------------------------------------------------------------------
// Custom polymorphic serializers
// ---------------------------------------------------------------------------

/**
 * Custom serializer for [JunieEvent] that inspects the `kind` discriminator
 * and falls back to [UnknownJunieEvent] for unrecognised values.
 */
object JunieEventSerializer : JsonContentPolymorphicSerializer<JunieEvent>(JunieEvent::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<JunieEvent> {
        val kind = element.jsonObject["kind"]?.jsonPrimitive?.content
            ?: return UnknownJunieEventSerializer

        return when (kind) {
            "UserPromptEvent" -> UserPromptEvent.serializer()
            "SessionA2uxEvent" -> SessionA2uxEvent.serializer()
            "TaskStartedEvent" -> TaskStartedEvent.serializer()
            "TaskState" -> TaskState.serializer()
            "UserMessagesCommittedToHistory" -> UserMessagesCommittedToHistory.serializer()
            "UserAsyncResponseEvent" -> UserAsyncResponseEvent.serializer()
            "SystemMessageEvent" -> SystemMessageEvent.serializer()
            "SendToAgentEvent" -> SendToAgentEvent.serializer()
            "CancelAgentEvent" -> CancelAgentEvent.serializer()
            "SessionTitleSetEvent" -> SessionTitleSetEvent.serializer()
            "SkillsStatusEvent" -> SkillsStatusEvent.serializer()
            "TaskContinueStopped" -> TaskContinueStopped.serializer()
            "UserResponseEvent" -> UserResponseEvent.serializer()
            else -> UnknownJunieEventSerializer
        }
    }
}

/**
 * Custom serializer for [AgentEvent] that inspects the `kind` discriminator
 * and falls back to [UnknownAgentEvent] for unrecognised values.
 */
object AgentEventSerializer : JsonContentPolymorphicSerializer<AgentEvent>(AgentEvent::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AgentEvent> {
        val kind = element.jsonObject["kind"]?.jsonPrimitive?.content
            ?: return UnknownAgentEventSerializer

        return when (kind) {
            // Existing
            "AgentThoughtBlockUpdatedEvent" -> AgentThoughtBlockUpdatedEvent.serializer()
            "AgentPatchCreatedEvent" -> AgentPatchCreatedEvent.serializer()
            "ResultBlockUpdatedEvent" -> ResultBlockUpdatedEvent.serializer()
            "ToolBlockUpdatedEvent" -> ToolBlockUpdatedEvent.serializer()
            "TerminalBlockUpdatedEvent" -> TerminalBlockUpdatedEvent.serializer()
            "AgentCurrentStatusUpdatedEvent" -> AgentCurrentStatusUpdatedEvent.serializer()
            "AgentTaskNameUpdatedEvent" -> AgentTaskNameUpdatedEvent.serializer()
            "AgentPlanUpdatedEvent" -> AgentPlanUpdatedEvent.serializer()
            // Phase A additions
            "AvailablePullRequestsEvent" -> AvailablePullRequestsEvent.serializer()
            "LlmResponseMetadataEvent" -> LlmResponseMetadataEvent.serializer()
            "CurrentDirectoryUpdatedEvent" -> CurrentDirectoryUpdatedEvent.serializer()
            "EnvironmentVariablesUpdatedEvent" -> EnvironmentVariablesUpdatedEvent.serializer()
            "ViewFilesBlockUpdatedEvent" -> ViewFilesBlockUpdatedEvent.serializer()
            "ContextWindowReportEvent" -> ContextWindowReportEvent.serializer()
            "FileChangesBlockUpdatedEvent" -> FileChangesBlockUpdatedEvent.serializer()
            "TipSuggestionCreatedEvent" -> TipSuggestionCreatedEvent.serializer()
            "ShowPlanProgressEvent" -> ShowPlanProgressEvent.serializer()
            "NextPromptSuggestionEvent" -> NextPromptSuggestionEvent.serializer()
            "AskAsyncRequestUpdatedEvent" -> AskAsyncRequestUpdatedEvent.serializer()
            "AuthorizationAvailabilityEvent" -> AuthorizationAvailabilityEvent.serializer()
            "AgentStartedEvent" -> AgentStartedEvent.serializer()
            "SuggestPlanEvent" -> SuggestPlanEvent.serializer()
            // Newly added agent events
            "TestRunBlockUpdatedEvent" -> TestRunBlockUpdatedEvent.serializer()
            "McpBlockUpdatedEvent" -> McpBlockUpdatedEvent.serializer()
            "CustomAgentBlockUpdatedEvent" -> CustomAgentBlockUpdatedEvent.serializer()
            "AgentFailureEvent" -> AgentFailureEvent.serializer()
            "AgentStateUpdatedEvent" -> AgentStateUpdatedEvent.serializer()
            "AskRequestUpdatedEvent" -> AskRequestUpdatedEvent.serializer()
            "ChoiceRequestUpdatedEvent" -> ChoiceRequestUpdatedEvent.serializer()
            "MarkdownBlockUpdatedEvent" -> MarkdownBlockUpdatedEvent.serializer()
            else -> UnknownAgentEventSerializer
        }
    }
}

/**
 * Deserializer that wraps any unrecognised top-level event into [UnknownJunieEvent],
 * preserving the raw JSON object.
 */
object UnknownJunieEventSerializer : KSerializer<UnknownJunieEvent> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("UnknownJunieEvent")

    override fun deserialize(decoder: Decoder): UnknownJunieEvent {
        val jsonDecoder = decoder as JsonDecoder
        val obj = jsonDecoder.decodeJsonElement().jsonObject
        val kind = obj["kind"]?.jsonPrimitive?.content ?: "unknown"
        val timestampMs = obj["timestampMs"]?.jsonPrimitive?.longOrNull
        return UnknownJunieEvent(kind = kind, timestampMs = timestampMs, raw = obj)
    }

    override fun serialize(encoder: Encoder, value: UnknownJunieEvent) {
        val jsonEncoder = encoder as JsonEncoder
        jsonEncoder.encodeJsonElement(value.raw)
    }
}

/**
 * Deserializer that wraps any unrecognised nested agent event into [UnknownAgentEvent],
 * preserving the raw JSON object.
 */
object UnknownAgentEventSerializer : KSerializer<UnknownAgentEvent> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("UnknownAgentEvent")

    override fun deserialize(decoder: Decoder): UnknownAgentEvent {
        val jsonDecoder = decoder as JsonDecoder
        val obj = jsonDecoder.decodeJsonElement().jsonObject
        val kind = obj["kind"]?.jsonPrimitive?.content ?: "unknown"
        return UnknownAgentEvent(kind = kind, raw = obj)
    }

    override fun serialize(encoder: Encoder, value: UnknownAgentEvent) {
        val jsonEncoder = encoder as JsonEncoder
        jsonEncoder.encodeJsonElement(value.raw)
    }
}
