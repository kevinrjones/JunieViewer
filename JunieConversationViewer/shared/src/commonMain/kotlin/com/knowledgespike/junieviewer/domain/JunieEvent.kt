package com.knowledgespike.junieviewer.domain

import kotlinx.serialization.Serializable

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
    val kind: String
}
