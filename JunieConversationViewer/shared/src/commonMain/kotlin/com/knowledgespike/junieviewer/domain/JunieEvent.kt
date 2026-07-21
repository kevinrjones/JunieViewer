package com.knowledgespike.junieviewer.domain

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// MappingContext — shared context parameter for event self-mapping (F3, Q1)
// ---------------------------------------------------------------------------

/**
 * Context passed to [JunieEvent.toMessage] and [AgentEvent.toMessage] so each
 * event can produce its own [Message] without external dispatch.
 *
 * @param lineNumber 1-based line number of the event in the events.jsonl file,
 *                   used as the stable Message ID source (F9, Q3).
 * @param timestampMs optional timestamp from the wrapping [SessionA2uxEvent].
 */
data class MappingContext(
    val lineNumber: Int,
    val timestampMs: Long? = null
)

// ---------------------------------------------------------------------------
// JunieEvent sealed hierarchy — top-level events in events.jsonl
// ---------------------------------------------------------------------------

/**
 * Top-level event from a Junie events.jsonl file.
 * Uses a custom polymorphic serializer that falls back to [UnknownJunieEvent]
 * for any unrecognised `kind` value, preventing deserialization crashes.
 *
 * Concrete event classes are in TopLevelEvents.kt.
 * Serializer dispatch is in EventSerializers.kt.
 */
@Serializable(with = JunieEventSerializer::class)
sealed interface JunieEvent {
    /** Discriminator value — defaults to the simple class name, matching the JSONL `kind` field. */
    val kind: String get() = this::class.simpleName ?: "unknown"

    /**
     * Maps this event to a UI [Message], or null if this event has no UI representation
     * (metadata-only events). Each event type implements its own mapping logic (Strategy pattern, Q1).
     */
    fun toMessage(context: MappingContext): Message?
}
