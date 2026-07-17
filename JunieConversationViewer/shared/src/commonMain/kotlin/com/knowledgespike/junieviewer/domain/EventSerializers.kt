package com.knowledgespike.junieviewer.domain

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

// ---------------------------------------------------------------------------
// Custom polymorphic serializers with map-based dispatch
// ---------------------------------------------------------------------------

/** Registry of known top-level event kind strings to their serializers. */
private val topLevelEventRegistry: Map<String, DeserializationStrategy<JunieEvent>> = mapOf(
    "UserPromptEvent" to UserPromptEvent.serializer(),
    "SessionA2uxEvent" to SessionA2uxEvent.serializer(),
    "TaskStartedEvent" to TaskStartedEvent.serializer(),
    "TaskState" to TaskState.serializer(),
    "UserMessagesCommittedToHistory" to UserMessagesCommittedToHistory.serializer(),
    "UserAsyncResponseEvent" to UserAsyncResponseEvent.serializer(),
    "SystemMessageEvent" to SystemMessageEvent.serializer(),
    "SendToAgentEvent" to SendToAgentEvent.serializer(),
    "CancelAgentEvent" to CancelAgentEvent.serializer(),
    "SessionTitleSetEvent" to SessionTitleSetEvent.serializer(),
    "SkillsStatusEvent" to SkillsStatusEvent.serializer(),
    "TaskContinueStopped" to TaskContinueStopped.serializer(),
    "UserResponseEvent" to UserResponseEvent.serializer(),
)

/** Registry of known nested agent event kind strings to their serializers. */
private val agentEventRegistry: Map<String, DeserializationStrategy<AgentEvent>> = mapOf(
    "AgentThoughtBlockUpdatedEvent" to AgentThoughtBlockUpdatedEvent.serializer(),
    "AgentPatchCreatedEvent" to AgentPatchCreatedEvent.serializer(),
    "ResultBlockUpdatedEvent" to ResultBlockUpdatedEvent.serializer(),
    "ToolBlockUpdatedEvent" to ToolBlockUpdatedEvent.serializer(),
    "TerminalBlockUpdatedEvent" to TerminalBlockUpdatedEvent.serializer(),
    "AgentCurrentStatusUpdatedEvent" to AgentCurrentStatusUpdatedEvent.serializer(),
    "AgentTaskNameUpdatedEvent" to AgentTaskNameUpdatedEvent.serializer(),
    "AgentPlanUpdatedEvent" to AgentPlanUpdatedEvent.serializer(),
    "AvailablePullRequestsEvent" to AvailablePullRequestsEvent.serializer(),
    "LlmResponseMetadataEvent" to LlmResponseMetadataEvent.serializer(),
    "CurrentDirectoryUpdatedEvent" to CurrentDirectoryUpdatedEvent.serializer(),
    "EnvironmentVariablesUpdatedEvent" to EnvironmentVariablesUpdatedEvent.serializer(),
    "ViewFilesBlockUpdatedEvent" to ViewFilesBlockUpdatedEvent.serializer(),
    "ContextWindowReportEvent" to ContextWindowReportEvent.serializer(),
    "FileChangesBlockUpdatedEvent" to FileChangesBlockUpdatedEvent.serializer(),
    "TipSuggestionCreatedEvent" to TipSuggestionCreatedEvent.serializer(),
    "ShowPlanProgressEvent" to ShowPlanProgressEvent.serializer(),
    "NextPromptSuggestionEvent" to NextPromptSuggestionEvent.serializer(),
    "AskAsyncRequestUpdatedEvent" to AskAsyncRequestUpdatedEvent.serializer(),
    "AuthorizationAvailabilityEvent" to AuthorizationAvailabilityEvent.serializer(),
    "AgentStartedEvent" to AgentStartedEvent.serializer(),
    "SuggestPlanEvent" to SuggestPlanEvent.serializer(),
    "TestRunBlockUpdatedEvent" to TestRunBlockUpdatedEvent.serializer(),
    "McpBlockUpdatedEvent" to McpBlockUpdatedEvent.serializer(),
    "CustomAgentBlockUpdatedEvent" to CustomAgentBlockUpdatedEvent.serializer(),
    "AgentFailureEvent" to AgentFailureEvent.serializer(),
    "AgentTaskFailedEvent" to AgentTaskFailedEvent.serializer(),
    "AgentStateUpdatedEvent" to AgentStateUpdatedEvent.serializer(),
    "AskRequestUpdatedEvent" to AskRequestUpdatedEvent.serializer(),
    "ChoiceRequestUpdatedEvent" to ChoiceRequestUpdatedEvent.serializer(),
    "MarkdownBlockUpdatedEvent" to MarkdownBlockUpdatedEvent.serializer(),
)

/**
 * Custom serializer for [JunieEvent] that inspects the `kind` discriminator
 * and falls back to [UnknownJunieEventSerializer] for unrecognised values.
 * Uses a map lookup instead of a growing when-expression.
 */
object JunieEventSerializer : JsonContentPolymorphicSerializer<JunieEvent>(JunieEvent::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<JunieEvent> {
        val kind = element.jsonObject["kind"]?.jsonPrimitive?.content
            ?: return UnknownJunieEventSerializer

        return topLevelEventRegistry[kind] ?: UnknownJunieEventSerializer
    }
}

/**
 * Custom serializer for [AgentEvent] that inspects the `kind` discriminator
 * and falls back to [UnknownAgentEventSerializer] for unrecognised values.
 * Uses a map lookup instead of a growing when-expression.
 */
object AgentEventSerializer : JsonContentPolymorphicSerializer<AgentEvent>(AgentEvent::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AgentEvent> {
        val kind = element.jsonObject["kind"]?.jsonPrimitive?.content
            ?: return UnknownAgentEventSerializer

        return agentEventRegistry[kind] ?: UnknownAgentEventSerializer
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
