package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.*

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
    fun assertMessageCount(count: Int) {
        // onChildren() only counts currently composed items. 
        // For tests with few messages this is fine.
        semanticMatcher.onNodeWithTag("message_list")
            .onChildren()
            .assertCountEquals(count)
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
}
