package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.SessionInfo
import com.knowledgespike.junieviewer.ui.theme.ThemeMode

/**
 * Tracks which Message Kind Filters are active.
 */
data class FilterState(
    val showHuman: Boolean = true,
    val showJunie: Boolean = true,
    val showThoughts: Boolean = true,
    val showTools: Boolean = true,
    val showPatches: Boolean = true,
    val showTerminal: Boolean = true
) {
    /** Returns true when all filters are at their default (all-showing) state. */
    fun isDefault(): Boolean =
        showHuman && showJunie && showThoughts && showTools && showPatches && showTerminal
}

/**
 * Represents the UI state for the conversation screen.
 */
data class ConversationState(
    val messages: List<Message> = emptyList(),
    val filteredMessages: List<Message> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    /** User-facing error message when session loading fails, null when no error. */
    val errorMessage: String? = null,
    val sessions: List<SessionInfo> = emptyList(),
    val selectedSessionId: String? = null,
    /** The full SessionInfo for the currently selected session, used for the context header. */
    val selectedSession: SessionInfo? = null,
    val isSessionPickerOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val junieHomePath: String = "~/.junie",
    val filter: FilterState = FilterState(),
    /** Zero-based index of the currently focused match in filteredMessages, or -1 if none. */
    val currentMatchIndex: Int = -1,
    /** The currently active theme mode. */
    val themeMode: ThemeMode = ThemeMode.System,
    /** Whether live auto-refresh is enabled. Controls live tracking start/stop and is persisted. */
    val isAutoRefreshEnabled: Boolean = true,
    /** Current sort order for displaying Messages. Persisted across app launches. */
    val sortOrder: SortOrder = SortOrder.OldestFirst,
    /**
     * Per-block expansion state keyed by stable block ID (e.g. "{messageId}:thought").
     * When a block ID is absent from the map, the block uses its default initial expansion state.
     * Entries are set by global Collapse All / Show All commands and per-block manual toggles.
     */
    val blockExpansionStates: Map<String, Boolean> = emptyMap(),
    /**
     * True while text is selected in any tracked selection container.
     * Drives the enabled state of the global Copy command (Edit menu and toolbar).
     */
    val hasTextSelection: Boolean = false
)
