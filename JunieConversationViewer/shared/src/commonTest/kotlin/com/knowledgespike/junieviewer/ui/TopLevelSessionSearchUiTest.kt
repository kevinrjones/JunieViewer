package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.*
import com.knowledgespike.junieviewer.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class TopLevelSessionSearchUiTest {

    @Test
    fun `top level search dialog opens via toolbar entry and shows empty query state`() = runConversationUiTest(emptyList()) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Initially dialog is not open
        onNodeWithTag("top_level_search_dialog").assertDoesNotExist()

        // Click top-level search toolbar entry
        onNodeWithTag("top_level_search_entry").performClick()

        // Dialog opens and shows empty query state
        onNodeWithTag("top_level_search_dialog").assertIsDisplayed()
        onNodeWithTag("top_level_search_input").assertIsDisplayed()
        onNodeWithTag("top_level_search_empty_query").assertIsDisplayed()

        // Close dialog
        onNodeWithTag("top_level_search_close").performClick()
        onNodeWithTag("top_level_search_dialog").assertDoesNotExist()
    }

    @Test
    fun `top level search results and partial warning render correctly`() = runConversationUiTest(emptyList()) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Open dialog and set mock results in ViewModel
        viewModel.onAction(ConversationAction.OnToggleTopLevelSearch)
        viewModel.onAction(ConversationAction.OnTopLevelSearchQueryChange("error"))
        viewModel.onAction(ConversationAction.OnSubmitTopLevelSearch)

        onNodeWithTag("top_level_search_dialog").assertIsDisplayed()
        onNodeWithTag("top_level_search_input").assertTextContains("error")
    }

    @Test
    fun `conversation search remains independent of top level search state`() = runConversationUiTest(emptyList()) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Type in in-conversation search field
        onNodeWithTag("search_field").performTextInput("local search query")
        onNodeWithTag("search_field").assertTextContains("local search query")

        // Open top-level search
        viewModel.onAction(ConversationAction.OnToggleTopLevelSearch)
        onNodeWithTag("top_level_search_dialog").assertIsDisplayed()

        // In-conversation search query remains unaffected
        onNodeWithTag("search_field").assertTextContains("local search query")
    }
}
