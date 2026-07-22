package com.knowledgespike.junieviewer.ui

/**
 * Shared command model for toolbar buttons, menu items, and keyboard shortcuts.
 *
 * Each command represents a user intent that can be triggered from the toolbar,
 * application menu, or keyboard shortcut. Commands are dispatched to the ViewModel
 * which maps them to existing [ConversationAction] values or handles them directly.
 *
 * Inspired by LogViewer's `AppMenuActionKey` pattern but uses a sealed interface
 * for type safety and extensibility.
 *
 * ### Keyboard Shortcuts (macOS primary / Windows-Linux noted)
 *
 * | Command            | macOS           | Windows / Linux       |
 * |--------------------|-----------------|-----------------------|
 * | Copy               | Cmd+C           | Ctrl+C                |
 * | Refresh            | Cmd+R           | Ctrl+R                |
 * | OpenSession        | Cmd+O           | Ctrl+O                |
 * | FocusSearch        | Cmd+F           | Ctrl+F                |
 * | FindNext           | Cmd+G           | Ctrl+G                |
 * | FindPrevious       | Shift+Cmd+G     | Shift+Ctrl+G          |
 * | ToggleAutoRefresh  | Cmd+Shift+R     | Ctrl+Shift+R          |
 * | ToggleSortOrder    | (none initially)| (none initially)      |
 * | CollapseAll        | Cmd+Shift+−     | Ctrl+Shift+−          |
 * | ShowAll            | Cmd+Shift++     | Ctrl+Shift++          |
 * | Settings           | Cmd+,           | Ctrl+,                |
 * | Quit               | Cmd+Q           | Alt+F4 / Ctrl+Q       |
 * | About              | menu only       | menu only             |
 */
sealed interface ConversationCommand {
    /** Copy selected text via OS shortcut passthrough. No-op if nothing is selected. */
    data object Copy : ConversationCommand

    /** Reload the current Session from disk. */
    data object Refresh : ConversationCommand

    /** Open/toggle the Session picker dialog. */
    data object OpenSession : ConversationCommand

    /** Toggle live auto-refresh on/off. Starts/stops live tracking and persists preference. */
    data object ToggleAutoRefresh : ConversationCommand

    /** Toggle sort order between oldest-first and newest-first. Persists preference and re-derives visible Messages. */
    data object ToggleSortOrder : ConversationCommand

    /** Collapse all collapsible content blocks. Full implementation in Area 7. */
    data object CollapseAll : ConversationCommand

    /** Expand all collapsible content blocks. Full implementation in Area 7. */
    data object ShowAll : ConversationCommand

    /** Move keyboard focus to the Search Messages field. */
    data object FocusSearch : ConversationCommand

    /** Navigate to the next search match. */
    data object FindNext : ConversationCommand

    /** Navigate to the previous search match. */
    data object FindPrevious : ConversationCommand

    /** Open the Settings dialog. */
    data object Settings : ConversationCommand

    /** Quit the application. Handled at the UI/platform level. */
    data object Quit : ConversationCommand

    /** Show the About dialog. Handled at the UI/platform level. */
    data object About : ConversationCommand

    /** Show the How to Use dialog. Handled at the UI/platform level. */
    data object HowToUse : ConversationCommand
}

/**
 * Maps this command to the equivalent [ConversationAction] when the command is a direct
 * passthrough to existing action handling, or null when it requires direct handling in the
 * ViewModel (e.g. side-effect emission, live-tracking control, or platform-level commands).
 */
fun ConversationCommand.toActionOrNull(): ConversationAction? = when (this) {
    ConversationCommand.OpenSession -> ConversationAction.OnToggleSessionPicker
    ConversationCommand.FindNext -> ConversationAction.OnNextMatch
    ConversationCommand.FindPrevious -> ConversationAction.OnPreviousMatch
    ConversationCommand.Settings -> ConversationAction.OnToggleSettings
    ConversationCommand.Copy,
    ConversationCommand.Refresh,
    ConversationCommand.ToggleAutoRefresh,
    ConversationCommand.ToggleSortOrder,
    ConversationCommand.CollapseAll,
    ConversationCommand.ShowAll,
    ConversationCommand.FocusSearch,
    ConversationCommand.Quit,
    ConversationCommand.About,
    ConversationCommand.HowToUse -> null
}

/**
 * Represents the sort order for displaying Messages in the Conversation.
 */
enum class SortOrder {
    /** Display Messages in chronological order (oldest at top). */
    OldestFirst,
    /** Display Messages in reverse chronological order (newest at top). */
    NewestFirst
}

/**
 * Tracks the enabled/disabled state of each [ConversationCommand].
 *
 * The toolbar and future menu bar consume this to determine which controls
 * are interactive. Derived from [ConversationState] in the ViewModel.
 */
data class ConversationCommandState(
    val copyEnabled: Boolean = false,
    val refreshEnabled: Boolean = false,
    val openSessionEnabled: Boolean = true,
    val toggleAutoRefreshEnabled: Boolean = false,
    val toggleSortOrderEnabled: Boolean = false,
    val collapseAllEnabled: Boolean = false,
    val showAllEnabled: Boolean = false,
    val focusSearchEnabled: Boolean = true,
    val findNextEnabled: Boolean = false,
    val findPreviousEnabled: Boolean = false,
    val settingsEnabled: Boolean = true,
    val quitEnabled: Boolean = true,
    val aboutEnabled: Boolean = true,
    /** Whether auto-refresh is currently active (for toggle button visual state). */
    val isAutoRefreshActive: Boolean = true,
    /** Current sort order (for toggle button visual state). */
    val sortOrder: SortOrder = SortOrder.OldestFirst
) {
    companion object {
        /**
         * Derives the command enablement state from the current [ConversationState].
         */
        fun fromConversationState(state: ConversationState): ConversationCommandState {
            val hasSession = state.selectedSessionId != null
            val hasMessages = state.messages.isNotEmpty()
            val hasSearchResults = state.searchQuery.isNotBlank() && state.filteredMessages.isNotEmpty()

            return ConversationCommandState(
                // Copy: enabled only while text is actually selected somewhere in the app.
                // Selection state is reported by TrackedSelectionContainer instances.
                copyEnabled = state.hasTextSelection,
                refreshEnabled = hasSession && !state.isLoading,
                openSessionEnabled = true,
                toggleAutoRefreshEnabled = hasSession,
                toggleSortOrderEnabled = hasMessages,
                collapseAllEnabled = hasMessages,
                showAllEnabled = hasMessages,
                focusSearchEnabled = true,
                findNextEnabled = hasSearchResults,
                findPreviousEnabled = hasSearchResults,
                settingsEnabled = true,
                quitEnabled = true,
                aboutEnabled = true,
                isAutoRefreshActive = state.isAutoRefreshEnabled,
                sortOrder = state.sortOrder
            )
        }
    }
}
