package com.knowledgespike.junieviewer.ui

/**
 * Pure controller for per-block collapsible expansion state.
 *
 * Owns the single source of truth for which block, if any, is currently force-expanded by
 * the active search match ([currentForceExpandedBlockId]), and derives the ready-to-render
 * per-block expansion map from manual state, search force-expansion, and user dismissals.
 * Operates only on [ConversationState] snapshots — no ViewModel or coroutine dependency.
 */
object BlockExpansionController {

    /**
     * Returns the single collapsible block ID force-expanded by the current search match,
     * or null when no search is active or the current match has no collapsible block.
     * This is the one place that computation is performed — both [isBlockForceExpanded] and
     * [deriveBlockExpansionStates] delegate to it so they can never diverge.
     */
    fun currentForceExpandedBlockId(state: ConversationState): String? {
        if (state.searchQuery.isBlank()) return null
        val matchIndex = state.currentMatchIndex
        if (matchIndex < 0 || matchIndex >= state.filteredMessages.size) return null
        val currentMatchMessage = state.filteredMessages[matchIndex]
        val searchText = MessageContentRegistry.searchableText(currentMatchMessage)
        if (!searchText.contains(state.searchQuery, ignoreCase = true)) return null
        val descriptor = MessageContentRegistry.descriptorFor(currentMatchMessage.kind)
        return descriptor.getCollapsibleBlockId(currentMatchMessage)
    }

    /** Returns true if [blockId] should be force-expanded by the current search query. */
    fun isBlockForceExpanded(state: ConversationState, blockId: String): Boolean =
        currentForceExpandedBlockId(state) == blockId

    /**
     * Derives the final per-block expansion state from manual state, search force-expansion,
     * and user dismissals. The result is ready for UI consumption.
     *
     * Priority rule: `manualExpanded || (forceExpanded && blockId !in dismissedForceExpandedBlockIds)`.
     */
    fun deriveBlockExpansionStates(state: ConversationState): Map<String, Boolean> {
        val allBlockIds = MessageContentRegistry.collectCollapsibleBlockIds(state.messages)
        val forceExpandedId = currentForceExpandedBlockId(state)

        return allBlockIds.associateWith { blockId ->
            val manualExpanded = state.blockExpansionStates[blockId] ?: true
            val forceExpanded = blockId == forceExpandedId
            val dismissed = blockId in state.dismissedForceExpandedBlockIds
            manualExpanded || (forceExpanded && !dismissed)
        }
    }

    /** Collapses every known collapsible block and clears any force-expansion dismissals. */
    fun collapseAll(state: ConversationState): ConversationState {
        val allIds = MessageContentRegistry.collectCollapsibleBlockIds(state.messages)
        return state.copy(
            blockExpansion = state.blockExpansion.copy(
                blockExpansionStates = state.blockExpansionStates + allIds.associateWith { false },
                dismissedForceExpandedBlockIds = emptySet()
            )
        )
    }

    /** Expands every known collapsible block and clears any force-expansion dismissals. */
    fun showAll(state: ConversationState): ConversationState {
        val allIds = MessageContentRegistry.collectCollapsibleBlockIds(state.messages)
        return state.copy(
            blockExpansion = state.blockExpansion.copy(
                blockExpansionStates = state.blockExpansionStates + allIds.associateWith { true },
                dismissedForceExpandedBlockIds = emptySet()
            )
        )
    }

    /**
     * Toggles the expansion state of a single block identified by its stable block ID.
     * If the block is currently force-expanded by search, toggling records a force-dismissal
     * so the block collapses while the query remains active.
     */
    fun toggle(state: ConversationState, blockId: String): ConversationState {
        val derived = state.derivedBlockExpansionStates[blockId]
        val isCurrentlyExpanded = derived ?: true
        val isForceExpanded = isBlockForceExpanded(state, blockId)
        val newDismissals = if (isCurrentlyExpanded && isForceExpanded) {
            state.dismissedForceExpandedBlockIds + blockId
        } else {
            state.dismissedForceExpandedBlockIds
        }
        val newManualExpanded = !isCurrentlyExpanded
        return state.copy(
            blockExpansion = state.blockExpansion.copy(
                blockExpansionStates = state.blockExpansionStates + (blockId to newManualExpanded),
                dismissedForceExpandedBlockIds = newDismissals
            )
        )
    }
}
