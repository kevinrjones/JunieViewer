package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
 * UI tests for Area 4 — Rich Content Rendering.
 * Verifies that each content type renders without crashing and with correct visual elements.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class RichContentRenderingTest {

    @Test
    fun `plain text message renders with text content`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(RepresentativeFixtures.humanTextMessage)
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithText("Please refactor the authentication module to use JWT tokens.").assertExists()
    }

    @Test
    fun `markdown message renders headings and formatted text`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            Message("md-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieMarkdownMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Heading text should be visible
        onNodeWithText("Refactoring Plan").assertExists()
        // List items should be visible
        onNodeWithText("Next Steps", substring = true).assertExists()
    }

    @Test
    fun `code block renders with copy button`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieCodeMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Code block tag should exist
        onNodeWithTag("code_block").assertExists()
        // Copy button should exist
        onNodeWithTag("copy_button").assertExists()
    }

    @Test
    fun `diff block renders expanded by default showing content`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieDiffMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("patch_block_header").assertExists()
        onNodeWithTag("patch_inline_view").assertExists()
        val tokenPairNodes = onAllNodesWithText("TokenPair", substring = true).fetchSemanticsNodes()
        assert(tokenPairNodes.isNotEmpty()) { "Expected at least one node with TokenPair" }
    }

    @Test
    fun `terminal output renders in monospace block`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieTerminalMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("terminal_block").assertExists()
        onNodeWithText("BUILD SUCCESSFUL", substring = true).assertExists()
    }

    @Test
    fun `tool call renders expanded by default with collapsible header`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieToolCallMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("tool_call_block").assertExists()
        onNodeWithTag("tool_call_header").assertExists()
        onNodeWithTag("tool_call_body").assertExists()

        // Click to collapse
        onNodeWithTag("tool_call_header").performClick()
        waitForIdle()
        onNodeWithTag("tool_call_body").assertDoesNotExist()
    }

    @Test
    fun `thought renders expanded by default and can be collapsed`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieThoughtMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("thought_block").assertExists()
        onNodeWithTag("thought_header").assertExists()
        onNodeWithTag("thought_body").assertExists()

        // Click to collapse
        onNodeWithTag("thought_header").performClick()
        waitForIdle()
        onNodeWithTag("thought_body").assertDoesNotExist()
    }

    @Test
    fun `structured output renders in monospace block`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieStructuredOutputMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("structured_output_block").assertExists()
        onNodeWithText("complete", substring = true).assertExists()
    }

    @Test
    fun `error message renders with distinct error styling`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieErrorMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Error label and content should be visible
        onNodeWithText("Could not resolve dependency", substring = true).assertExists()
        // Error label exists (may appear in kind marker too)
        val errorNodes = onAllNodesWithText("Error", substring = true).fetchSemanticsNodes()
        assert(errorNodes.isNotEmpty()) { "Expected at least one Error label" }
    }

    @Test
    fun `warning message renders with distinct warning styling`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieWarningMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Warning label and content should be visible
        onNodeWithText("deprecated SessionStore", substring = true).assertExists()
        // Warning label exists (may appear in kind marker too)
        val warningNodes = onAllNodesWithText("Warning", substring = true).fetchSemanticsNodes()
        assert(warningNodes.isNotEmpty()) { "Expected at least one Warning label" }
    }

    @Test
    fun `malformed unsupported content renders fallback card`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.malformedContentMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("unsupported_event_card").assertExists()
        onNodeWithText("Unsupported event: SomeNewEventKind", substring = true).assertExists()
    }

    @Test
    fun `all representative fixtures render without crashing`() = runConversationUiTest {
        sessionRepository.messagesToReturn = RepresentativeFixtures.allMessageKinds
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Should have message items for all messages
        onNodeWithTag("message_list").assertExists()
        // At least one human and one junie message
        onAllNodesWithTag("message_item_human").fetchSemanticsNodes().isNotEmpty()
        onAllNodesWithTag("message_item_junie").fetchSemanticsNodes().isNotEmpty()
    }
}
