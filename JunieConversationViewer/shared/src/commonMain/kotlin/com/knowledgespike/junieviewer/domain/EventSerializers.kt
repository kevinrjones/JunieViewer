package com.knowledgespike.junieviewer.domain

import co.touchlab.kermit.Logger
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

private val logger = Logger.withTag("EventSerializers")

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
    "SubagentSpawnedEvent" to SubagentSpawnedEvent.serializer(),
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
 * Decodes the JSON object behind [decoder], pulls out its `kind` discriminator, wraps the
 * whole object as a raw [PayloadValue.ObjectValue], and hands both — plus the original
 * [JsonObject] for any event-specific extra fields — to [construct]. Shared by
 * [UnknownJunieEventSerializer] and [UnknownAgentEventSerializer], which otherwise repeat the
 * same decode→pull-kind→wrap-raw→construct steps.
 */
private inline fun <T> decodeUnknownEvent(
    decoder: Decoder,
    construct: (obj: JsonObject, kind: String, raw: PayloadValue.ObjectValue) -> T
): T {
    val jsonDecoder = decoder as JsonDecoder
    val obj = jsonDecoder.decodeJsonElement().jsonObject
    val kind = obj["kind"]?.jsonPrimitive?.content ?: "unknown"
    val raw = obj.toPayloadValue() as PayloadValue.ObjectValue
    return construct(obj, kind, raw)
}

/** Re-encodes [raw] verbatim — the shared serialize step for unknown-event wrappers. */
private fun serializeUnknownEvent(encoder: Encoder, raw: PayloadValue.ObjectValue) {
    val jsonEncoder = encoder as JsonEncoder
    jsonEncoder.encodeJsonElement(raw.toJsonElement())
}

/**
 * Deserializer that wraps any unrecognised top-level event into [UnknownJunieEvent],
 * preserving the raw JSON object.
 */
object UnknownJunieEventSerializer : KSerializer<UnknownJunieEvent> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("UnknownJunieEvent")

    override fun deserialize(decoder: Decoder): UnknownJunieEvent =
        decodeUnknownEvent(decoder) { obj, kind, raw ->
            UnknownJunieEvent(kind = kind, timestampMs = obj["timestampMs"]?.jsonPrimitive?.longOrNull, raw = raw)
        }

    override fun serialize(encoder: Encoder, value: UnknownJunieEvent) = serializeUnknownEvent(encoder, value.raw)
}

/**
 * Deserializer that wraps any unrecognised nested agent event into [UnknownAgentEvent],
 * preserving the raw JSON object.
 */
object UnknownAgentEventSerializer : KSerializer<UnknownAgentEvent> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("UnknownAgentEvent")

    override fun deserialize(decoder: Decoder): UnknownAgentEvent =
        decodeUnknownEvent(decoder) { _, kind, raw -> UnknownAgentEvent(kind = kind, raw = raw) }

    override fun serialize(encoder: Encoder, value: UnknownAgentEvent) = serializeUnknownEvent(encoder, value.raw)
}

// ---------------------------------------------------------------------------
// PayloadValue <-> JsonElement conversion — the JSON boundary for PayloadValue
// ---------------------------------------------------------------------------

/** Recursively converts a [JsonElement] into its domain-owned [PayloadValue] equivalent. */
private fun JsonElement.toPayloadValue(): PayloadValue = when (this) {
    is JsonNull -> PayloadValue.Null
    is JsonArray -> PayloadValue.ListValue(map { it.toPayloadValue() })
    is JsonObject -> PayloadValue.ObjectValue(mapValues { (_, v) -> v.toPayloadValue() })
    is JsonPrimitive -> when {
        isString -> PayloadValue.Text(content)
        content == "true" || content == "false" -> PayloadValue.Bool(content.toBoolean())
        else -> PayloadValue.Number(content)
    }
}

/** Recursively converts a [PayloadValue] back into a [JsonElement] for re-encoding. */
@OptIn(ExperimentalSerializationApi::class)
internal fun PayloadValue.toJsonElement(): JsonElement = when (this) {
    is PayloadValue.Null -> JsonNull
    is PayloadValue.Bool -> JsonPrimitive(value)
    is PayloadValue.Number -> JsonUnquotedLiteral(literal)
    is PayloadValue.Text -> JsonPrimitive(value)
    is PayloadValue.ListValue -> JsonArray(values.map { it.toJsonElement() })
    is PayloadValue.ObjectValue -> JsonObject(entries.mapValues { (_, v) -> v.toJsonElement() })
}

/**
 * Custom serializer for [PayloadValue] — the domain-owned replacement for [JsonElement].
 * Decodes any JSON shape into the [PayloadValue] hierarchy and re-encodes it exactly,
 * preserving numeric literals verbatim via [JsonUnquotedLiteral].
 */
object PayloadValueSerializer : KSerializer<PayloadValue> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PayloadValue")

    override fun deserialize(decoder: Decoder): PayloadValue {
        val jsonDecoder = decoder as JsonDecoder
        return jsonDecoder.decodeJsonElement().toPayloadValue()
    }

    override fun serialize(encoder: Encoder, value: PayloadValue) {
        val jsonEncoder = encoder as JsonEncoder
        jsonEncoder.encodeJsonElement(value.toJsonElement())
    }
}

// ---------------------------------------------------------------------------
// AskRequest / ChoiceRequest — structured shape with unstructured fallback
// ---------------------------------------------------------------------------

/**
 * Attempts to decode [element] via [structuredDecode]; any structural failure (non-object
 * payload, unexpected field shapes) is logged under [label] and falls back to [toUnstructured]
 * with the raw JSON text. Shared by [AskRequestSerializer] and [ChoiceRequestSerializer], which
 * otherwise repeat the same structured-decode-with-unstructured-fallback logic.
 */
private inline fun <T> decodeStructuredOrFallback(
    label: String,
    element: JsonElement,
    structuredDecode: (JsonObject) -> T,
    toUnstructured: (String) -> T
): T = try {
    val obj = element as? JsonObject ?: error("$label is not a JSON object")
    structuredDecode(obj)
} catch (e: Exception) {
    logger.w(e) { "Unstructured $label payload; preserving raw text" }
    toUnstructured(element.toString())
}

/**
 * Encodes [unstructuredText] back to a [JsonElement] when present (re-parsing it, or falling
 * back to a plain string primitive if it isn't valid JSON); otherwise builds the structured
 * shape via [structuredEncode]. Shared by [AskRequestSerializer] and [ChoiceRequestSerializer].
 */
private inline fun encodeStructuredOrFallback(
    unstructuredText: String?,
    structuredEncode: () -> JsonElement
): JsonElement = if (unstructuredText != null) {
    try {
        Json.parseToJsonElement(unstructuredText)
    } catch (e: Exception) {
        JsonPrimitive(unstructuredText)
    }
} else {
    structuredEncode()
}

/**
 * Custom serializer for [AskRequest]. Attempts to decode the structured `{id, question}`
 * shape; any structural failure (non-object payload, unexpected field shapes) is logged and
 * falls back to preserving the raw JSON text in [AskRequest.unstructuredText].
 */
object AskRequestSerializer : KSerializer<AskRequest> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AskRequest")

    override fun deserialize(decoder: Decoder): AskRequest {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        return decodeStructuredOrFallback(
            label = "askRequest",
            element = element,
            structuredDecode = { obj ->
                AskRequest(
                    id = obj["id"]?.jsonPrimitive?.content,
                    question = obj["question"]?.jsonPrimitive?.content
                )
            },
            toUnstructured = { text -> AskRequest(unstructuredText = text) }
        )
    }

    override fun serialize(encoder: Encoder, value: AskRequest) {
        val jsonEncoder = encoder as JsonEncoder
        val element = encodeStructuredOrFallback(value.unstructuredText) {
            buildJsonObject {
                value.id?.let { put("id", it) }
                value.question?.let { put("question", it) }
            }
        }
        jsonEncoder.encodeJsonElement(element)
    }
}

/**
 * Custom serializer for [ChoiceRequest]. Attempts to decode the structured
 * `{id, options: [{id, description, title}]}` shape; any structural failure (non-object
 * payload, non-array/non-object options) is logged and falls back to preserving the raw JSON
 * text in [ChoiceRequest.unstructuredText].
 */
object ChoiceRequestSerializer : KSerializer<ChoiceRequest> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ChoiceRequest")

    override fun deserialize(decoder: Decoder): ChoiceRequest {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        return decodeStructuredOrFallback(
            label = "choiceRequest",
            element = element,
            structuredDecode = { obj ->
                val options = obj["options"]?.let { optionsElement ->
                    val array = optionsElement as? JsonArray ?: error("choiceRequest.options is not a JSON array")
                    array.map { optionElement ->
                        val optionObj = optionElement as? JsonObject ?: error("choiceRequest option is not a JSON object")
                        ChoiceOption(
                            id = optionObj["id"]?.jsonPrimitive?.content,
                            description = optionObj["description"]?.jsonPrimitive?.content,
                            title = optionObj["title"]?.jsonPrimitive?.content
                        )
                    }
                }
                ChoiceRequest(id = obj["id"]?.jsonPrimitive?.content, options = options)
            },
            toUnstructured = { text -> ChoiceRequest(unstructuredText = text) }
        )
    }

    override fun serialize(encoder: Encoder, value: ChoiceRequest) {
        val jsonEncoder = encoder as JsonEncoder
        val element = encodeStructuredOrFallback(value.unstructuredText) {
            buildJsonObject {
                value.id?.let { put("id", it) }
                value.options?.let { options ->
                    putJsonArray("options") {
                        options.forEach { option ->
                            addJsonObject {
                                option.id?.let { put("id", it) }
                                option.description?.let { put("description", it) }
                                option.title?.let { put("title", it) }
                            }
                        }
                    }
                }
            }
        }
        jsonEncoder.encodeJsonElement(element)
    }
}
