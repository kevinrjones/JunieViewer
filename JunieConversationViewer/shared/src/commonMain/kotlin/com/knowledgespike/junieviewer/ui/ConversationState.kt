package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.SessionInfo
import com.knowledgespike.junieviewer.domain.Turn
import com.knowledgespike.junieviewer.domain.groupMessagesIntoTurns
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
 * Session loading concern: the Messages for the currently selected Session, load/error
 * status, the available Session list for the picker, and the configured Junie home
 * directory used to discover Sessions on disk.
 */
data class SessionLoadState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    /** User-facing error message when session loading fails, null when no error. */
    val errorMessage: String? = null,
    val sessions: List<SessionInfo> = emptyList(),
    val selectedSessionId: String? = null,
    /** The full SessionInfo for the currently selected session, used for the context header. */
    val selectedSession: SessionInfo? = null,
    val junieHomePath: String = "~/.junie"
)

/**
 * Search/filter/sort concern: the active Search Query and match navigation, the derived
 * visible Messages and Turns, the active kind [FilterState], and the display [SortOrder].
 */
data class SearchState(
    val searchQuery: String = "",
    val filteredMessages: List<Message> = emptyList(),
    /** Visible Messages grouped into Turns, derived alongside [filteredMessages]. */
    val turns: List<Turn> = groupMessagesIntoTurns(filteredMessages),
    /** Zero-based index of the currently focused match in filteredMessages, or -1 if none. */
    val currentMatchIndex: Int = -1,
    val filter: FilterState = FilterState(),
    /** Current sort order for displaying Messages. Persisted across app launches. */
    val sortOrder: SortOrder = SortOrder.OldestFirst
)

/**
 * Modal dialog visibility concern for the Session picker and Settings dialogs.
 */
data class DialogState(
    val isSessionPickerOpen: Boolean = false,
    val isSettingsOpen: Boolean = false
)

/**
 * Per-block collapsible content expansion concern, plus whether any tracked selection
 * container currently holds a text selection (drives the global Copy command).
 */
data class BlockExpansionState(
    /**
     * Per-block expansion state keyed by stable block ID (e.g. "{messageId}:thought").
     * When a block ID is absent from the map, the block uses its default initial expansion state.
     * Entries are set by global Collapse All / Show All commands and per-block manual toggles.
     */
    val blockExpansionStates: Map<String, Boolean> = emptyMap(),
    /**
     * Block IDs where the user has dismissed Search Query force-expansion.
     * Cleared when the Search Query changes or is cleared.
     */
    val dismissedForceExpandedBlockIds: Set<String> = emptySet(),
    /**
     * Ready-to-render per-block expansion state derived from manual state, Search Query
     * force-expansion, and dismissals. UI components consume this directly.
     */
    val derivedBlockExpansionStates: Map<String, Boolean> = emptyMap(),
    /**
     * True while text is selected in any tracked selection container.
     * Drives the enabled state of the global Copy command (Edit menu and toolbar).
     */
    val hasTextSelection: Boolean = false
)

/**
 * Represents the UI state for the conversation screen.
 *
 * Properties are grouped into cohesive nested value objects by concern: [SessionLoadState]
 * (session loading/error), [SearchState] (search/filter/sort), [DialogState] (picker/settings
 * dialogs), and [BlockExpansionState] (per-block collapse/expand and selection). Theme and
 * auto-refresh remain top-level as they are simple, cross-cutting toggles.
 */
data class ConversationState(
    val sessionLoad: SessionLoadState = SessionLoadState(),
    val search: SearchState = SearchState(),
    val dialogs: DialogState = DialogState(),
    val blockExpansion: BlockExpansionState = BlockExpansionState(),
    /** The currently active theme mode. */
    val themeMode: ThemeMode = ThemeMode.System,
    /** Whether live auto-refresh is enabled. Controls live tracking start/stop and is persisted. */
    val isAutoRefreshEnabled: Boolean = true
)

// Read-only convenience accessors mirroring the previous flat property names. These keep the
// many call sites that only read state (UI composables, derivation engines, assertions)
// concise, while the underlying model stays organised by concern.
val ConversationState.messages: List<Message> get() = sessionLoad.messages
val ConversationState.isLoading: Boolean get() = sessionLoad.isLoading
val ConversationState.errorMessage: String? get() = sessionLoad.errorMessage
val ConversationState.sessions: List<SessionInfo> get() = sessionLoad.sessions
val ConversationState.selectedSessionId: String? get() = sessionLoad.selectedSessionId
val ConversationState.selectedSession: SessionInfo? get() = sessionLoad.selectedSession
val ConversationState.junieHomePath: String get() = sessionLoad.junieHomePath

val ConversationState.searchQuery: String get() = search.searchQuery
val ConversationState.filteredMessages: List<Message> get() = search.filteredMessages
val ConversationState.turns: List<Turn> get() = search.turns
val ConversationState.currentMatchIndex: Int get() = search.currentMatchIndex
val ConversationState.filter: FilterState get() = search.filter
val ConversationState.sortOrder: SortOrder get() = search.sortOrder

val ConversationState.isSessionPickerOpen: Boolean get() = dialogs.isSessionPickerOpen
val ConversationState.isSettingsOpen: Boolean get() = dialogs.isSettingsOpen

val ConversationState.blockExpansionStates: Map<String, Boolean> get() = blockExpansion.blockExpansionStates
val ConversationState.dismissedForceExpandedBlockIds: Set<String> get() = blockExpansion.dismissedForceExpandedBlockIds
val ConversationState.derivedBlockExpansionStates: Map<String, Boolean> get() = blockExpansion.derivedBlockExpansionStates
val ConversationState.hasTextSelection: Boolean get() = blockExpansion.hasTextSelection
