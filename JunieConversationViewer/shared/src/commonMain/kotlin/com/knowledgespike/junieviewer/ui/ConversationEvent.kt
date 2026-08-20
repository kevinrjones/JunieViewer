package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.TopLevelSearchResults

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
    /** Requests the UI to focus the top-level Search Query input. */
    data object FocusTopLevelSearch : ConversationEvent
    /** Announces that top-level search results were freshly submitted. */
    data class TopLevelSearchSubmitted(val results: TopLevelSearchResults) : ConversationEvent
    /** Announces that a top-level search result was selected. */
    data class TopLevelSearchResultSelected(val sessionId: String) : ConversationEvent
}
