package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.SessionInfo

/**
 * Represents user-triggered actions on the conversation screen.
 */
sealed interface ConversationAction {
    data class OnSearchQueryChange(val query: String) : ConversationAction
    data object OnRetryClick : ConversationAction
    data object OnToggleSessionPicker : ConversationAction
    data class OnSessionSelected(val session: SessionInfo) : ConversationAction
    data object OnToggleSettings : ConversationAction
    data class OnHomePathChange(val path: String) : ConversationAction
    data class OnToggleFilter(val kind: FilterKind) : ConversationAction
}

enum class FilterKind {
    Human, Junie, Thought, Tool, Patch, Terminal
}
