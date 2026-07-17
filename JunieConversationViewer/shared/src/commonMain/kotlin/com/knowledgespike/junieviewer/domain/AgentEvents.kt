package com.knowledgespike.junieviewer.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

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

// -- Known agent events --

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

/** Available pull requests metadata event. */
@Serializable
data class AvailablePullRequestsEvent(
    val pullRequests: JsonElement? = null,
    val agent: JsonElement? = null
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
    val agent: JsonElement? = null,
    val authorized: Boolean? = null
) : AgentEvent {
    override val kind: String get() = "AuthorizationAvailabilityEvent"
}

/** Agent started indicator (metadata-only). */
@Serializable
data class AgentStartedEvent(
    val agentId: String? = null,
    val agent: JsonElement? = null,
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

/** Subagent spawn event — records when Junie delegates work to a sub-agent. */
@Serializable
data class SubagentSpawnedEvent(
    val name: String? = null,
    val task: String? = null,
    val stepId: String? = null,
    val agent: JsonElement? = null
) : AgentEvent {
    override val kind: String get() = "SubagentSpawnedEvent"
}

/** Task-level failure event — tolerant nullable model since no real payload examples exist. */
@Serializable
data class AgentTaskFailedEvent(
    val message: String? = null,
    val errorCode: String? = null,
    val taskId: String? = null,
    val stepId: String? = null,
    val details: JsonElement? = null
) : AgentEvent {
    override val kind: String get() = "AgentTaskFailedEvent"
}

/**
 * Fallback for any nested agent event kind not yet modelled.
 * Preserves the raw JSON so no data is lost.
 */
data class UnknownAgentEvent(
    override val kind: String,
    val raw: JsonObject
) : AgentEvent
