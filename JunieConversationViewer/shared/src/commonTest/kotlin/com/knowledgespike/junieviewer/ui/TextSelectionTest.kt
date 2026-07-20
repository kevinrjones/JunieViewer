package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.fixtures.RepresentativeFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okio.FileSystem
import org.junit.Test

/**
 * UI tests for Area 2 — Text Selection and Partial Copy.
 * Verifies that SelectionContainer wrappers are present for each content type
 * and that existing copy buttons and collapsible headers remain functional.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class TextSelectionTest {

    // -----------------------------------------------------------------------
    // 2.1 — Human and Junie text content has selectable body
    // -----------------------------------------------------------------------

    @Test
    fun `human plain text message has selectable content`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(RepresentativeFixtures.humanTextMessage)
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("selectable_message_text").assertExists()
        onNodeWithTag("plain_text_content").assertExists()
    }

    @Test
    fun `junie plain text message has selectable content`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTextMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // At least one selectable_message_text tag should exist for Junie text
        val selectableNodes = onAllNodesWithTag("selectable_message_text").fetchSemanticsNodes()
        assert(selectableNodes.size >= 2) { "Expected at least 2 selectable text nodes, got ${selectableNodes.size}" }
    }

    @Test
    fun `junie markdown message has selectable content`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieMarkdownMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        val selectableNodes = onAllNodesWithTag("selectable_message_text").fetchSemanticsNodes()
        assert(selectableNodes.size >= 2) { "Expected at least 2 selectable text nodes, got ${selectableNodes.size}" }
        onNodeWithTag("markdown_content").assertExists()
    }

    // -----------------------------------------------------------------------
    // 2.2 — Code block has selectable content and existing copy button
    // -----------------------------------------------------------------------

    @Test
    fun `code block has selectable content and copy button`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieCodeMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("selectable_code_content").assertExists()
        onNodeWithTag("copy_button").assertExists()
        onNodeWithTag("code_block").assertExists()
    }

    // -----------------------------------------------------------------------
    // 2.3 — Diff block has selectable content and existing copy button
    // -----------------------------------------------------------------------

    @Test
    fun `diff block has selectable content and copy button`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieDiffMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Expanded by default
        onNodeWithTag("selectable_diff_content").assertExists()
        onNodeWithTag("copy_button").assertExists()
    }

    // -----------------------------------------------------------------------
    // 2.4 — Terminal, Structured, Error/Warning blocks have selectable content
    // -----------------------------------------------------------------------

    @Test
    fun `terminal output has selectable content and copy button`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTerminalMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("selectable_terminal_content").assertExists()
        onNodeWithTag("copy_button").assertExists()
    }

    @Test
    fun `structured output has selectable content and copy button`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieStructuredOutputMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("selectable_structured_content").assertExists()
        onNodeWithTag("copy_button").assertExists()
    }

    @Test
    fun `error block has selectable content`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieErrorMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("selectable_error_warning_content").assertExists()
    }

    @Test
    fun `warning block has selectable content`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieWarningMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("selectable_error_warning_content").assertExists()
    }

    // -----------------------------------------------------------------------
    // Collapsible headers remain clickable after selection changes
    // -----------------------------------------------------------------------

    @Test
    fun `thought block has selectable content when expanded by default`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieThoughtMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Expanded by default — selectable content visible
        onNodeWithTag("thought_header").assertExists()
        onNodeWithTag("selectable_thought_content").assertExists()
        onNodeWithTag("thought_body").assertExists()
    }

    @Test
    fun `tool call has selectable content when expanded by default`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieToolCallMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Expanded by default — selectable content visible
        onNodeWithTag("tool_call_header").assertExists()
        onNodeWithTag("selectable_tool_call_content").assertExists()
        onNodeWithTag("tool_call_body").assertExists()
    }
}
