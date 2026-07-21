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
}

// -- Known agent events --

/** Junie's internal thought/reasoning block. */
@Serializable
data class AgentThoughtBlockUpdatedEvent(
    val text: String? = null,
    val stepId: String? = null
) : AgentEvent

/** A patch (diff) created by Junie. */
@Serializable
data class AgentPatchCreatedEvent(
    val patch: String? = null
) : AgentEvent

/** Final result block from Junie's response. */
@Serializable
data class ResultBlockUpdatedEvent(
    val result: String? = null,
    val stepId: String? = null,
    val cancelled: Boolean? = null,
    val changes: List<FileChange>? = null,
    val errorCode: String? = null
) : AgentEvent

/** Tool invocation block. */
@Serializable
data class ToolBlockUpdatedEvent(
    val toolCall: String? = null,
    val stepId: String? = null,
    val text: String? = null,
    val status: String? = null,
    val details: String? = null
) : AgentEvent

/** Terminal command execution block. */
@Serializable
data class TerminalBlockUpdatedEvent(
    val command: String? = null,
    val output: String? = null,
    val stepId: String? = null,
    val status: String? = null
) : AgentEvent

/** Agent status update (metadata-only). */
@Serializable
data object AgentCurrentStatusUpdatedEvent : AgentEvent

/** Agent task name update (metadata-only). */
@Serializable
data class AgentTaskNameUpdatedEvent(val name: String? = null) : AgentEvent

/** Agent plan update (metadata-only). */
@Serializable
data class AgentPlanUpdatedEvent(
    val plan: String? = null,
    val items: List<PlanItem>? = null
) : AgentEvent

/** Available pull requests metadata event. */
@Serializable
data class AvailablePullRequestsEvent(
    val pullRequests: PayloadValue? = null,
    val agent: AgentIdentity? = null
) : AgentEvent

/** LLM response metadata (token counts, model info). */
@Serializable
data class LlmResponseMetadataEvent(
    val model: String? = null,
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
    val modelUsage: List<ModelUsage>? = null
) : AgentEvent

/** Current working directory update (metadata-only). */
@Serializable
data class CurrentDirectoryUpdatedEvent(
    // The JSONL emits this field as `currentDirectory`; accept both names.
    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("currentDirectory")
    val directory: String? = null
) : AgentEvent

/** Environment variables update (metadata-only). */
@Serializable
data class EnvironmentVariablesUpdatedEvent(
    val variables: PayloadValue? = null
) : AgentEvent

/** View files block update — files Junie is examining. */
@Serializable
data class ViewFilesBlockUpdatedEvent(
    val files: List<ViewedFile>? = null,
    val stepId: String? = null,
    val status: String? = null
) : AgentEvent

/** Context window usage report (metadata-only). */
@Serializable
data class ContextWindowReportEvent(
    val usedTokens: Int? = null,
    val maxTokens: Int? = null,
    val percentage: Double? = null
) : AgentEvent

/** File changes block update — files Junie has modified. */
@Serializable
data class FileChangesBlockUpdatedEvent(
    val changes: List<FileChange>? = null,
    val stepId: String? = null,
    val status: String? = null
) : AgentEvent

/** Tip suggestion for the user (metadata-only). */
@Serializable
data class TipSuggestionCreatedEvent(
    val tip: String? = null,
    val id: String? = null,
    val description: String? = null
) : AgentEvent

/** Plan progress indicator. */
@Serializable
data class ShowPlanProgressEvent(
    val progress: PayloadValue? = null,
    val items: List<PlanItem>? = null
) : AgentEvent

/** Next prompt suggestion for the user. */
@Serializable
data class NextPromptSuggestionEvent(
    val suggestion: List<PromptSuggestion>? = null
) : AgentEvent

/** Async request update (e.g. HITL approval request). */
@Serializable
data class AskAsyncRequestUpdatedEvent(
    val requestId: String? = null,
    val question: String? = null,
    val stepId: String? = null,
    val title: String? = null,
    val request: AsyncRequest? = null,
    val status: String? = null
) : AgentEvent

/** Authorization availability status (metadata-only). */
@Serializable
data class AuthorizationAvailabilityEvent(
    val available: Boolean? = null,
    val agent: AgentIdentity? = null,
    val authorized: Boolean? = null
) : AgentEvent

/** Agent started indicator (metadata-only). */
@Serializable
data class AgentStartedEvent(
    val agentId: String? = null,
    val agent: AgentIdentity? = null,
    val stepId: String? = null,
    val agentType: String? = null
) : AgentEvent

/** Plan suggestion from the agent. */
@Serializable
data class SuggestPlanEvent(
    val plan: PayloadValue? = null,
    val sections: List<PlanSection>? = null,
    val deliveryPlan: List<PlanItem>? = null,
    val readyForReview: Boolean? = null
) : AgentEvent

/** Test execution block — when Junie runs tests. */
@Serializable
data class TestRunBlockUpdatedEvent(
    val stepId: String? = null,
    val status: String? = null,
    val name: String? = null
) : AgentEvent

/** MCP (Model Context Protocol) tool invocation. */
@Serializable
data class McpBlockUpdatedEvent(
    val stepId: String? = null,
    val toolName: String? = null,
    val status: String? = null,
    val details: String? = null
) : AgentEvent

/** Custom subagent invocation block. */
@Serializable
data class CustomAgentBlockUpdatedEvent(
    val stepId: String? = null,
    val name: String? = null,
    val status: String? = null
) : AgentEvent

/** Agent-level failure (LLM connection issues, errors). */
@Serializable
data class AgentFailureEvent(
    val message: String? = null,
    val errorCode: String? = null
) : AgentEvent

/** Serialized snapshot of the agent's internal state (metadata-only). */
@Serializable
data class AgentStateUpdatedEvent(
    val blob: String? = null
) : AgentEvent

/** Synchronous question from the agent to the user. */
@Serializable
data class AskRequestUpdatedEvent(
    val stepId: String? = null,
    val title: String? = null,
    val askRequest: AskRequest? = null,
    val status: String? = null
) : AgentEvent

/** Presents the user with a set of choices. */
@Serializable
data class ChoiceRequestUpdatedEvent(
    val stepId: String? = null,
    val title: String? = null,
    val choiceRequest: ChoiceRequest? = null,
    val status: String? = null
) : AgentEvent

/** Standalone markdown text block from the agent. */
@Serializable
data class MarkdownBlockUpdatedEvent(
    val stepId: String? = null,
    val text: String? = null
) : AgentEvent

/** Subagent spawn event — records when Junie delegates work to a sub-agent. */
@Serializable
data class SubagentSpawnedEvent(
    val name: String? = null,
    val task: String? = null,
    val stepId: String? = null,
    val agent: AgentIdentity? = null
) : AgentEvent

/** Task-level failure event — tolerant nullable model since no real payload examples exist. */
@Serializable
data class AgentTaskFailedEvent(
    val message: String? = null,
    val errorCode: String? = null,
    val taskId: String? = null,
    val stepId: String? = null,
    val details: PayloadValue? = null
) : AgentEvent

/**
 * Fallback for any nested agent event kind not yet modelled.
 * Preserves the raw JSON so no data is lost.
 */
data class UnknownAgentEvent(
    override val kind: String,
    val raw: PayloadValue.ObjectValue
) : AgentEvent
