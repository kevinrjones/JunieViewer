package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.*

/**
 * Robot-pattern test helper for ConversationScreen interactions and assertions.
 */
class ConversationRobot(private val semanticMatcher: SemanticsNodeInteractionsProvider) {

    fun typeSearchQuery(query: String) {
        semanticMatcher.onNodeWithTag("search_field")
            .performTextInput(query)
    }

    fun clearSearchQuery() {
        semanticMatcher.onNodeWithTag("search_field")
            .performTextClearance()
    }

    fun toggleFilter(kind: FilterKind) {
        val tag = when (kind) {
            FilterKind.Human -> "filter_human"
            FilterKind.Junie -> "filter_junie"
            FilterKind.Thought -> "filter_thought"
            FilterKind.Tool -> "filter_tool"
            FilterKind.Patch -> "filter_patch"
            FilterKind.Terminal -> "filter_terminal"
        }
        semanticMatcher.onNodeWithTag(tag)
            .performClick()
    }

    fun openSessionPicker() {
        semanticMatcher.onNodeWithTag("session_picker_button")
            .performClick()
    }

    fun openSettings() {
        semanticMatcher.onNodeWithTag("settings_button")
            .performClick()
    }

    // Assertions

    /** Counts Human message items in the list */
    fun assertHumanMessageCount(count: Int) {
        semanticMatcher.onAllNodesWithTag("message_item_human")
            .assertCountEquals(count)
    }

    /** Counts Junie message items in the list */
    fun assertJunieMessageCount(count: Int) {
        semanticMatcher.onAllNodesWithTag("message_item_junie")
            .assertCountEquals(count)
    }

    /** Counts total message items (Human + Junie) */
    fun assertMessageCount(count: Int) {
        val humanCount = semanticMatcher.onAllNodesWithTag("message_item_human").fetchSemanticsNodes().size
        val junieCount = semanticMatcher.onAllNodesWithTag("message_item_junie").fetchSemanticsNodes().size
        assert(humanCount + junieCount == count) {
            "Expected $count messages but found ${humanCount + junieCount} (human=$humanCount, junie=$junieCount)"
        }
    }

    fun assertMessageVisible(text: String) {
        semanticMatcher.onAllNodesWithText(text, substring = true, ignoreCase = true)
            .onFirst()
            .assertExists()
    }

    fun assertSearchText(text: String) {
        semanticMatcher.onNodeWithTag("search_field")
            .assertTextContains(text)
    }

    /** Asserts that a sender label with the given text exists */
    fun assertSenderLabelVisible(senderName: String) {
        semanticMatcher.onAllNodesWithTag("sender_marker")
            .filter(hasText(senderName))
            .onFirst()
            .assertExists()
    }

    /** Asserts that at least one Turn Header is visible */
    fun assertTurnHeaderVisible() {
        semanticMatcher.onAllNodesWithTag("turn_header")
            .onFirst()
            .assertExists()
    }

    /** Counts Turn Headers in the list */
    fun assertTurnHeaderCount(count: Int) {
        semanticMatcher.onAllNodesWithTag("turn_header")
            .assertCountEquals(count)
    }

    /** Asserts that a Message Kind marker with the given text exists */
    fun assertMessageKindMarkerVisible(kindLabel: String) {
        semanticMatcher.onAllNodesWithTag("message_kind_marker")
            .filter(hasText(kindLabel, substring = true))
            .onFirst()
            .assertExists()
    }
}
