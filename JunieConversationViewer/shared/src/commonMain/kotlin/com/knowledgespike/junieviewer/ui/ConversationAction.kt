package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.SessionInfo
import com.knowledgespike.junieviewer.ui.theme.ThemeMode

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
    /** Navigate to the next match in the filtered message list. */
    data object OnNextMatch : ConversationAction
    /** Navigate to the previous match in the filtered message list. */
    data object OnPreviousMatch : ConversationAction
    /** Change the application theme mode. */
    data class OnThemeModeChange(val themeMode: ThemeMode) : ConversationAction
    /** Toggle expansion state for a single collapsible block identified by its stable block ID. */
    data class OnToggleBlockExpansion(val blockId: String) : ConversationAction
    /** Reports whether text is currently selected inside a tracked selection container. */
    data class OnTextSelectionChanged(val containerId: String, val hasSelection: Boolean) : ConversationAction
}

enum class FilterKind {
    Human, Junie, Thought, Tool, Patch, Terminal
}
