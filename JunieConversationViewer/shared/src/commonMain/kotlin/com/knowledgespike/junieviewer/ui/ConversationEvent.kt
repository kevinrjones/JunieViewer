package com.knowledgespike.junieviewer.ui

/**
 * Represents one-time side effects for the conversation screen.
 */
sealed interface ConversationEvent {
    data class ShowError(val message: String) : ConversationEvent
    /** Requests the UI to move keyboard focus to the Search Messages field. */
    data object FocusSearch : ConversationEvent
}
