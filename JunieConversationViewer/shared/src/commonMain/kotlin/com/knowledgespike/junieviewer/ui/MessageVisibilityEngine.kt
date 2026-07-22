package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.FilterCategory
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.domain.Turn
import com.knowledgespike.junieviewer.domain.groupMessagesIntoTurns

/**
 * Pure engine that derives the visible message list for the conversation screen.
 *
 * Applies the active kind/sender [FilterState] and search query, honours [SortOrder],
 * regroups the result into [Turn]s, and clamps the current match index to the new list.
 * Contains no [androidx.lifecycle.ViewModel] or [kotlinx.coroutines.flow.StateFlow]
 * dependency, so it is directly unit-testable.
 */
object MessageVisibilityEngine {

    /** Result of deriving the visible messages for a given filter/sort/search combination. */
    data class VisibilityResult(
        val filteredMessages: List<Message>,
        val turns: List<Turn>,
        val currentMatchIndex: Int
    )

    /**
     * Filters [messages] by [filter] and [query], sorts them per [sortOrder], regroups
     * them into turns, and derives a safe [currentMatchIndex] for the resulting list.
     */
    fun derive(
        messages: List<Message>,
        filter: FilterState,
        sortOrder: SortOrder,
        query: String,
        currentMatchIndex: Int
    ): VisibilityResult {
        val filtered = messages.filter { message -> matches(message, filter, query) }

        // Canonical messages list is always chronological; reverse for NewestFirst display order.
        val sorted = when (sortOrder) {
            SortOrder.OldestFirst -> filtered
            SortOrder.NewestFirst -> filtered.asReversed()
        }

        return VisibilityResult(
            filteredMessages = sorted,
            turns = groupMessagesIntoTurns(sorted),
            currentMatchIndex = clampMatchIndex(currentMatchIndex, sorted, query)
        )
    }

    /** Returns true if [message] should be visible under the given [filter] and [query]. */
    private fun matches(message: Message, filter: FilterState, query: String): Boolean {
        val kindMatch = when (message.kind.filterCategory) {
            FilterCategory.Human -> filter.showHuman
            FilterCategory.Junie -> {
                if (message.sender == Sender.Human) filter.showHuman
                else filter.showJunie
            }
            FilterCategory.Thought -> filter.showThoughts
            FilterCategory.Tool -> filter.showTools
            FilterCategory.Patch -> filter.showPatches
            FilterCategory.Terminal -> filter.showTerminal
            FilterCategory.AlwaysShow -> true
        }
        if (!kindMatch) return false
        if (query.isBlank()) return true
        return MessageContentRegistry.searchableText(message).contains(query, ignoreCase = true)
    }

    /** Resets [currentMatchIndex] safely after re-derivation of [sorted]. */
    private fun clampMatchIndex(currentMatchIndex: Int, sorted: List<Message>, query: String): Int =
        when {
            sorted.isEmpty() -> -1
            currentMatchIndex < 0 -> if (query.isNotBlank() && sorted.isNotEmpty()) 0 else -1
            currentMatchIndex >= sorted.size -> 0
            else -> currentMatchIndex
        }
}
