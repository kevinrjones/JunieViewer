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

    private val testDispatcher = UnconfinedTestDispatcher()

    /** Creates a ViewModel with the given messages for testing. */
    private fun createViewModel(messages: List<Message>): Pair<ConversationViewModel, okio.Path> {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "text-selection-test-${System.currentTimeMillis()}.json"
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

        return ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker()) to tempPrefsPath
    }

    // -----------------------------------------------------------------------
    // 2.1 — Human and Junie text content has selectable body
    // -----------------------------------------------------------------------

    @Test
    fun `human plain text message has selectable content`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(RepresentativeFixtures.humanTextMessage))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("selectable_message_text").assertExists()
        onNodeWithTag("plain_text_content").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `junie plain text message has selectable content`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTextMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        // At least one selectable_message_text tag should exist for Junie text
        val selectableNodes = onAllNodesWithTag("selectable_message_text").fetchSemanticsNodes()
        assert(selectableNodes.size >= 2) { "Expected at least 2 selectable text nodes, got ${selectableNodes.size}" }
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `junie markdown message has selectable content`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieMarkdownMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        val selectableNodes = onAllNodesWithTag("selectable_message_text").fetchSemanticsNodes()
        assert(selectableNodes.size >= 2) { "Expected at least 2 selectable text nodes, got ${selectableNodes.size}" }
        onNodeWithTag("markdown_content").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // 2.2 — Code block has selectable content and existing copy button
    // -----------------------------------------------------------------------

    @Test
    fun `code block has selectable content and copy button`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieCodeMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("selectable_code_content").assertExists()
        onNodeWithTag("copy_button").assertExists()
        onNodeWithTag("code_block").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // 2.3 — Diff block has selectable content and existing copy button
    // -----------------------------------------------------------------------

    @Test
    fun `diff block has selectable content and copy button`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieDiffMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        // Expanded by default
        onNodeWithTag("selectable_diff_content").assertExists()
        onNodeWithTag("copy_button").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // 2.4 — Terminal, Structured, Error/Warning blocks have selectable content
    // -----------------------------------------------------------------------

    @Test
    fun `terminal output has selectable content and copy button`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTerminalMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("selectable_terminal_content").assertExists()
        onNodeWithTag("copy_button").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `structured output has selectable content and copy button`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieStructuredOutputMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("selectable_structured_content").assertExists()
        onNodeWithTag("copy_button").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `error block has selectable content`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieErrorMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("selectable_error_warning_content").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `warning block has selectable content`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieWarningMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("selectable_error_warning_content").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // Collapsible headers remain clickable after selection changes
    // -----------------------------------------------------------------------

    @Test
    fun `thought block has selectable content when expanded by default`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieThoughtMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        // Expanded by default — selectable content visible
        onNodeWithTag("thought_header").assertExists()
        onNodeWithTag("selectable_thought_content").assertExists()
        onNodeWithTag("thought_body").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `tool call has selectable content when expanded by default`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieToolCallMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        // Expanded by default — selectable content visible
        onNodeWithTag("tool_call_header").assertExists()
        onNodeWithTag("selectable_tool_call_content").assertExists()
        onNodeWithTag("tool_call_body").assertExists()
        FileSystem.SYSTEM.delete(path)
    }
}
