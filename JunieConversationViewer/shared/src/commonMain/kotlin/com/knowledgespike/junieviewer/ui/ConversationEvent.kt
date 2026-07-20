package com.knowledgespike.junieviewer.ui

/**
 * Represents one-time side effects for the conversation screen.
 */
sealed interface ConversationEvent {
    data class ShowError(val message: String) : ConversationEvent
    /** Requests the UI to move keyboard focus to the Search Messages field. */
    data object FocusSearch : ConversationEvent
    /** Requests the UI to show the About dialog. */
    data object ShowAbout : ConversationEvent
    /** Requests the UI to show the How to Use dialog. */
    data object ShowHowToUse : ConversationEvent
    /** Requests the platform layer to perform a native text copy action. */
    data object CopyText : ConversationEvent
}
