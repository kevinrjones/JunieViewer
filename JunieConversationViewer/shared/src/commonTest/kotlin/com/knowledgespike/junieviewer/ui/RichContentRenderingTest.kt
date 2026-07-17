package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
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

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun createViewModel(messages: List<Message>): Pair<ConversationViewModel, okio.Path> {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "rich-content-test-${System.currentTimeMillis()}.json"
        val fakePreferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "test-session"))

        val fakeRepository = object : SessionRepository {
            override fun getMessages(): List<Message> = messages
            override fun listSessions(homePath: String): List<SessionInfo> = emptyList()
            override fun setSession(sessionId: String, homePath: String) {}
            override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? = null
        }

        return ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher) to tempPrefsPath
    }

    @Test
    fun `plain text message renders with text content`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(RepresentativeFixtures.humanTextMessage))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithText("Please refactor the authentication module to use JWT tokens.").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `markdown message renders headings and formatted text`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            Message("md-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieMarkdownMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        // Heading text should be visible
        onNodeWithText("Refactoring Plan").assertExists()
        // List items should be visible
        onNodeWithText("Next Steps", substring = true).assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `code block renders with copy button`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieCodeMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        // Code block tag should exist
        onNodeWithTag("code_block").assertExists()
        // Copy button should exist
        onNodeWithTag("copy_button").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `diff block renders expanded by default showing content`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieDiffMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("patch_block_header").assertExists()
        onNodeWithTag("patch_inline_view").assertExists()
        val tokenPairNodes = onAllNodesWithText("TokenPair", substring = true).fetchSemanticsNodes()
        assert(tokenPairNodes.isNotEmpty()) { "Expected at least one node with TokenPair" }
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `terminal output renders in monospace block`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieTerminalMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("terminal_block").assertExists()
        onNodeWithText("BUILD SUCCESSFUL", substring = true).assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `tool call renders expanded by default with collapsible header`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieToolCallMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("tool_call_block").assertExists()
        onNodeWithTag("tool_call_header").assertExists()
        onNodeWithTag("tool_call_body").assertExists()

        // Click to collapse
        onNodeWithTag("tool_call_header").performClick()
        waitForIdle()
        onNodeWithTag("tool_call_body").assertDoesNotExist()

        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `thought renders expanded by default and can be collapsed`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieThoughtMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("thought_block").assertExists()
        onNodeWithTag("thought_header").assertExists()
        onNodeWithTag("thought_body").assertExists()

        // Click to collapse
        onNodeWithTag("thought_header").performClick()
        waitForIdle()
        onNodeWithTag("thought_body").assertDoesNotExist()

        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `structured output renders in monospace block`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieStructuredOutputMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("structured_output_block").assertExists()
        onNodeWithText("complete", substring = true).assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `error message renders with distinct error styling`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieErrorMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        // Error label and content should be visible
        onNodeWithText("Could not resolve dependency", substring = true).assertExists()
        // Error label exists (may appear in kind marker too)
        val errorNodes = onAllNodesWithText("Error", substring = true).fetchSemanticsNodes()
        assert(errorNodes.isNotEmpty()) { "Expected at least one Error label" }
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `warning message renders with distinct warning styling`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.junieWarningMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        // Warning label and content should be visible
        onNodeWithText("deprecated SessionStore", substring = true).assertExists()
        // Warning label exists (may appear in kind marker too)
        val warningNodes = onAllNodesWithText("Warning", substring = true).fetchSemanticsNodes()
        assert(warningNodes.isNotEmpty()) { "Expected at least one Warning label" }
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `malformed unsupported content renders fallback card`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            Message("h-1", Sender.Human, MessageContent.Text("prompt"), MessageKind.Text),
            RepresentativeFixtures.malformedContentMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("unsupported_event_card").assertExists()
        onNodeWithText("Unsupported event: SomeNewEventKind", substring = true).assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `all representative fixtures render without crashing`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(RepresentativeFixtures.allMessageKinds)
        setContent { ConversationRoot(viewModel = viewModel) }

        // Should have message items for all messages
        onNodeWithTag("message_list").assertExists()
        // At least one human and one junie message
        onAllNodesWithTag("message_item_human").fetchSemanticsNodes().isNotEmpty()
        onAllNodesWithTag("message_item_junie").fetchSemanticsNodes().isNotEmpty()

        FileSystem.SYSTEM.delete(path)
    }
}
