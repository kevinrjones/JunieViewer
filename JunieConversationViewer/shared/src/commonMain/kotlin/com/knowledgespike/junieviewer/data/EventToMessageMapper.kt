package com.knowledgespike.junieviewer.data

import com.knowledgespike.junieviewer.domain.*

/**
 * Pure transformation from parsed [JunieEvent] instances to UI [Message] objects.
 * Each event self-maps via [JunieEvent.toMessage] (Strategy pattern, F3/Q1);
 * this mapper retains only orchestration: ordering, line-based ID assignment (F9/Q3),
 * and filtering of no-message events.
 *
 * Extracted from SessionRepository to separate concerns and enable independent testing.
 */
object EventToMessageMapper {

    /**
     * Maps a list of parsed events to UI messages, filtering out metadata-only events.
     *
     * @param events the parsed events in file order
     * @param startLineNumber 1-based line number of the first event (for stable IDs)
     */
    fun mapEventsToMessages(events: List<JunieEvent>, startLineNumber: Int = 1): List<Message> =
        events.mapIndexedNotNull { index, event ->
            val context = MappingContext(lineNumber = startLineNumber + index)
            event.toMessage(context)
        }
}
