package com.knowledgespike.junieviewer.ui.components

import androidx.compose.runtime.*

// ---------------------------------------------------------------------------
// Expansion state helper — encapsulates auto-expand-on-search + manual collapse
// ---------------------------------------------------------------------------

/** Holds the expansion state for a collapsible message card. */
class MessageExpansionState(
    expanded: Boolean,
    private val forceExpanded: Boolean
) {
    var expanded by mutableStateOf(expanded)
        private set
    var userDismissedForce by mutableStateOf(false)
        internal set

    /** Whether the content should be visually expanded. */
    val isVisible: Boolean
        get() = expanded || (forceExpanded && !userDismissedForce)

    /** Toggles the expanded state, tracking user dismissal of force-expand. */
    fun toggle() {
        expanded = !expanded
        if (!expanded && forceExpanded) {
            userDismissedForce = true
        }
    }
}

/**
 * Remembers a [MessageExpansionState] that auto-expands when the message is the
 * current search match and resets the user-dismissed flag when force-expand ends.
 */
@Composable
fun rememberMessageExpansionState(
    isCurrentMatch: Boolean,
    isHuman: Boolean,
    searchQuery: String
): MessageExpansionState {
    val state = remember { MessageExpansionState(expanded = true, forceExpanded = false) }
    val forceExpanded = isCurrentMatch && isHuman && searchQuery.isNotBlank()
    val result = remember(forceExpanded) {
        MessageExpansionState(expanded = state.expanded, forceExpanded = forceExpanded)
    }
    if (!forceExpanded) {
        result.userDismissedForce = false
    }
    return result
}
