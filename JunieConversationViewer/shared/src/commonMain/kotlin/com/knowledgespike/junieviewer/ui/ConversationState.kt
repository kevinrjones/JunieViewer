package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.SessionInfo

data class FilterState(
    val showHuman: Boolean = true,
    val showJunie: Boolean = true,
    val showThoughts: Boolean = true,
    val showTools: Boolean = true,
    val showPatches: Boolean = true,
    val showTerminal: Boolean = true
)

/**
 * Represents the UI state for the conversation screen.
 */
data class ConversationState(
    val messages: List<Message> = emptyList(),
    val filteredMessages: List<Message> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val sessions: List<SessionInfo> = emptyList(),
    val selectedSessionId: String? = null,
    val isSessionPickerOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val junieHomePath: String = "~/.junie",
    val filter: FilterState = FilterState()
)
