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
        semanticMatcher.onNodeWithTag("toolbar_open_session")
            .performClick()
    }

    fun openSettings() {
        // Settings is no longer a toolbar button; it is accessed via menu (Area 4).
        // For tests that need to toggle settings, use the command model directly.
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

    /** Asserts the no-results state is displayed */
    fun assertNoResultsVisible() {
        semanticMatcher.onNodeWithTag("no_results")
            .assertIsDisplayed()
    }

    /** Asserts the no-results state is not displayed */
    fun assertNoResultsNotVisible() {
        semanticMatcher.onNodeWithTag("no_results")
            .assertDoesNotExist()
    }

    /** Asserts the result count text matches the expected value */
    fun assertResultCount(expected: String) {
        semanticMatcher.onNodeWithTag("result_count")
            .assertTextContains(expected, substring = true)
    }

    /** Asserts the result count is not displayed */
    fun assertResultCountNotVisible() {
        semanticMatcher.onNodeWithTag("result_count")
            .assertDoesNotExist()
    }

    /** Asserts a filter chip label is visible */
    fun assertFilterLabelVisible(label: String) {
        semanticMatcher.onAllNodesWithText(label)
            .onFirst()
            .assertExists()
    }

    /** Asserts the session context footer is visible with the given session id */
    fun assertSessionContextVisible(sessionId: String) {
        semanticMatcher.onNodeWithTag("session_context_footer")
            .assertIsDisplayed()
        semanticMatcher.onAllNodesWithText("Session: $sessionId", substring = true)
            .onFirst()
            .assertExists()
    }

    /** Asserts the no-session-selected state is displayed */
    fun assertNoSessionStateVisible() {
        semanticMatcher.onNodeWithTag("no_session_state")
            .assertIsDisplayed()
    }

    /** Asserts the empty conversation state is displayed */
    fun assertEmptyConversationStateVisible() {
        semanticMatcher.onNodeWithTag("empty_conversation")
            .assertIsDisplayed()
    }

    /** Asserts the loading indicator is displayed */
    fun assertLoadingVisible() {
        semanticMatcher.onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
    }

    /** Asserts the error state is displayed */
    fun assertErrorVisible() {
        semanticMatcher.onNodeWithTag("error_state")
            .assertIsDisplayed()
    }

    /** Clicks the retry button in the error state */
    fun clickRetry() {
        semanticMatcher.onNodeWithTag("retry_button")
            .performClick()
    }

    // -- Match navigation --

    /** Clicks the next match button */
    fun goToNextMatch() {
        semanticMatcher.onNodeWithTag("next_match_button")
            .performClick()
    }

    /** Clicks the previous match button */
    fun goToPreviousMatch() {
        semanticMatcher.onNodeWithTag("prev_match_button")
            .performClick()
    }

    /** Asserts the match position indicator shows the expected text (e.g. "1 / 3") */
    fun assertMatchIndicator(expected: String) {
        semanticMatcher.onNodeWithTag("match_position")
            .assertTextContains(expected, substring = true)
    }

    // -- Message kind assertions --

    /** Asserts at least one message with the given kind label is visible */
    fun assertMessageOfKindVisible(kindLabel: String) {
        semanticMatcher.onAllNodesWithTag("message_kind_marker")
            .filter(hasText(kindLabel, substring = true))
            .onFirst()
            .assertExists()
    }

    /** Asserts a sender marker with the given name is visible */
    fun assertSenderMarkerVisible(senderName: String) {
        semanticMatcher.onAllNodesWithTag("sender_marker")
            .filter(hasText(senderName))
            .onFirst()
            .assertExists()
    }

    /** Asserts a turn header with the given text is visible */
    fun assertTurnHeaderVisible(text: String = "Junie Turn") {
        semanticMatcher.onAllNodesWithTag("turn_header")
            .filter(hasText(text, substring = true))
            .onFirst()
            .assertExists()
    }

    // -- Semantic / accessibility assertions --

    /** Asserts a node with the given testTag exists */
    fun assertTagExists(tag: String) {
        semanticMatcher.onNodeWithTag(tag)
            .assertExists()
    }

    /** Asserts a node with the given content description exists */
    fun assertContentDescriptionExists(description: String) {
        semanticMatcher.onAllNodesWithContentDescription(description)
            .onFirst()
            .assertExists()
    }
}
