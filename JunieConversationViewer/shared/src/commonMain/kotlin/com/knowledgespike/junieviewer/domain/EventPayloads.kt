package com.knowledgespike.junieviewer.domain

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// PayloadValue — domain-owned replacement for kotlinx JsonElement
// ---------------------------------------------------------------------------

/**
 * Domain-owned representation of an arbitrary JSON value, used for payloads that are
 * genuinely unstructured (open-ended shapes coming from the Junie log format).
 *
 * This exists so the `domain` layer never leans on kotlinx.serialization's JSON types
 * ([kotlinx.serialization.json.JsonElement] et al.) directly — those are confined to the
 * deserialization boundary in `EventSerializers.kt`. [toString] intentionally reproduces the
 * exact compact JSON rendering of the source [kotlinx.serialization.json.JsonElement] it was
 * decoded from, so callers that used to interpolate a `JsonElement` directly see no change.
 */
@Serializable(with = PayloadValueSerializer::class)
sealed interface PayloadValue {
    /** JSON `null`. */
    data object Null : PayloadValue {
        override fun toString() = "null"
    }

    /** JSON `true`/`false`. */
    data class Bool(val value: Boolean) : PayloadValue {
        override fun toString() = value.toString()
    }

    /** Numeric literal preserved exactly as written in the source JSON. */
    data class Number(val literal: String) : PayloadValue {
        override fun toString() = literal
    }

    /** JSON string value. */
    data class Text(val value: String) : PayloadValue {
        override fun toString() = jsonQuote(value)
    }

    /** JSON array. */
    data class ListValue(val values: List<PayloadValue>) : PayloadValue {
        override fun toString() = values.joinToString(",", "[", "]")
    }

    /** JSON object. Preserves insertion order, matching the source JSON's key order. */
    data class ObjectValue(val entries: Map<String, PayloadValue>) : PayloadValue {
        operator fun get(key: String): PayloadValue? = entries[key]

        /** Returns the string value for [key] when it is a [Text], else null. */
        fun textOrNull(key: String): String? = (entries[key] as? Text)?.value

        override fun toString() = entries.entries.joinToString(",", "{", "}") { (k, v) -> "${jsonQuote(k)}:$v" }
    }
}

/** Escapes [value] as a JSON string literal, matching kotlinx.serialization.json's rendering. */
private fun jsonQuote(value: String): String = buildString {
    append('"')
    for (char in value) {
        when {
            char == '\\' -> append("\\\\")
            char == '"' -> append("\\\"")
            char == '\n' -> append("\\n")
            char == '\r' -> append("\\r")
            char == '\t' -> append("\\t")
            char == '\b' -> append("\\b")
            char.code in 0x00..0x1F -> append("\\u").append(char.code.toString(16).padStart(4, '0'))
            else -> append(char)
        }
    }
    append('"')
}

// ---------------------------------------------------------------------------
// Structured payload models — real-log shapes, all fields nullable/tolerant
// ---------------------------------------------------------------------------

/** Contents of a file involved in a [FileChange]. */
@Serializable
data class FileContent(
    val kind: String? = null,
    val text: String? = null
)

/** A single file addition/modification/deletion, as reported by change-block events. */
@Serializable
data class FileChange(
    val beforeContent: FileContent? = null,
    val beforeRelativePath: String? = null,
    val afterContent: FileContent? = null,
    val afterRelativePath: String? = null
)

/** A single plan step, as reported by plan-update events. */
@Serializable
data class PlanItem(
    val name: String? = null,
    val description: String? = null,
    val status: String? = null
)

/** A single named section of a suggested plan. */
@Serializable
data class PlanSection(
    val name: String? = null,
    val content: String? = null
)

/** Token/cost usage for a single LLM call. */
@Serializable
data class ModelUsage(
    val model: String? = null,
    val cost: Double? = null,
    val inputTokens: Int? = null,
    val cacheInputTokens: Int? = null,
    val cacheCreateTokens: Int? = null,
    val outputTokens: Int? = null,
    val time: Double? = null
)

/** Identifies an agent (main agent or sub-agent) referenced by an event. */
@Serializable
data class AgentIdentity(
    val id: String? = null,
    val kind: String? = null,
    val name: String? = null,
    val type: String? = null
)

/** A single file Junie is viewing/has viewed, with an optional line range. */
@Serializable
data class ViewedFile(
    val relativePath: String? = null,
    val lineFrom: Int? = null,
    val lineTo: Int? = null
)

/** A single suggested next prompt for the user. */
@Serializable
data class PromptSuggestion(
    val text: String? = null
)

/** A single option offered by a [ChoiceRequest]. */
@Serializable
data class ChoiceOption(
    val id: String? = null,
    val description: String? = null,
    val title: String? = null
)

/** A single question/answer pair from a user's async response. */
@Serializable
data class ResponseEntry(
    val question: String? = null,
    val answer: String? = null
)

/** An asynchronous (HITL) request raised by the agent. */
@Serializable
data class AsyncRequest(
    val id: String? = null,
    val name: String? = null,
    val question: String? = null,
    val isRequired: Boolean? = null,
    val allowMultiple: Boolean? = null,
    val options: List<ChoiceOption>? = null
)

// ---------------------------------------------------------------------------
// Ask/Choice payloads — structured shape with an explicit unstructured fallback
// ---------------------------------------------------------------------------

/**
 * Synchronous question payload from the agent. When the source JSON does not match the
 * structured `{id, question}` shape, [unstructuredText] preserves the raw JSON text instead
 * (see [AskRequestSerializer]).
 */
@Serializable(with = AskRequestSerializer::class)
data class AskRequest(
    val id: String? = null,
    val question: String? = null,
    val unstructuredText: String? = null
)

/**
 * Multiple-choice question payload from the agent. When the source JSON does not match the
 * structured `{id, options}` shape, [unstructuredText] preserves the raw JSON text instead
 * (see [ChoiceRequestSerializer]).
 */
@Serializable(with = ChoiceRequestSerializer::class)
data class ChoiceRequest(
    val id: String? = null,
    val options: List<ChoiceOption>? = null,
    val unstructuredText: String? = null
)
