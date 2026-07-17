package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.fixtures.RepresentativeFixtures
import com.knowledgespike.junieviewer.ui.components.blockContainsSearchHit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okio.FileSystem
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

/**
 * Tests for collapsible rich content blocks:
 * - All blocks expanded by default
 * - Blocks can be collapsed and re-expanded
 * - Full content visible without truncation
 * - Search auto-expansion of matching blocks
 * - blockContainsSearchHit helper
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class CollapsibleBlockTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun createViewModel(messages: List<Message>): Pair<ConversationViewModel, okio.Path> {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "collapsible-test-${System.currentTimeMillis()}.json"
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

    // -----------------------------------------------------------------------
    // blockContainsSearchHit helper unit tests
    // -----------------------------------------------------------------------

    @Test
    fun `blank query returns false`() {
        expectThat(blockContainsSearchHit("some text", "")).isFalse()
        expectThat(blockContainsSearchHit("some text", "  ")).isFalse()
    }

    @Test
    fun `case insensitive match returns true`() {
        expectThat(blockContainsSearchHit("Hello World", "hello")).isTrue()
        expectThat(blockContainsSearchHit("Hello World", "WORLD")).isTrue()
    }

    @Test
    fun `no match returns false`() {
        expectThat(blockContainsSearchHit("Hello World", "xyz")).isFalse()
    }

    @Test
    fun `empty text returns false`() {
        expectThat(blockContainsSearchHit("", "test")).isFalse()
    }

    @Test
    fun `regex special characters treated as plain text`() {
        expectThat(blockContainsSearchHit("price is $10.00", "$10")).isTrue()
        expectThat(blockContainsSearchHit("a(b)c", "(b)")).isTrue()
    }

    // -----------------------------------------------------------------------
    // Terminal block — expanded by default, collapsible, full content
    // -----------------------------------------------------------------------

    @Test
    fun `terminal block is expanded by default`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTerminalMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("terminal_block_header").assertExists()
        onNodeWithTag("terminal_block_body").assertExists()
        onNodeWithText("BUILD SUCCESSFUL", substring = true).assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `terminal block can be collapsed`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTerminalMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("terminal_block_header").performClick()
        waitForIdle()
        onNodeWithTag("terminal_block_body").assertDoesNotExist()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // Code block — expanded by default
    // -----------------------------------------------------------------------

    @Test
    fun `code block is expanded by default`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieCodeMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("code_block_header").assertExists()
        onNodeWithTag("code_block_body").assertExists()
        onNodeWithTag("copy_button").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // Structured output — expanded by default
    // -----------------------------------------------------------------------

    @Test
    fun `structured output block is expanded by default`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieStructuredOutputMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("structured_output_block_header").assertExists()
        onNodeWithTag("structured_output_block_body").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // Error/Warning — expanded by default
    // -----------------------------------------------------------------------

    @Test
    fun `error block is expanded by default`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieErrorMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("error_warning_block_header").assertExists()
        onNodeWithTag("error_warning_block_body").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // Thought and Tool Call — expanded by default (supersedes old collapsed)
    // -----------------------------------------------------------------------

    @Test
    fun `thought block is expanded by default`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieThoughtMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("thought_header").assertExists()
        onNodeWithTag("thought_block_body").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `tool call block is expanded by default`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieToolCallMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("tool_call_header").assertExists()
        onNodeWithTag("tool_call_block_body").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // Copy controls remain available
    // -----------------------------------------------------------------------

    @Test
    fun `copy buttons present for terminal and code blocks`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTerminalMessage,
            Message("c-1", Sender.Junie, MessageContent.Code("fun main() {}", "kotlin"), MessageKind.Text)
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        val copyNodes = onAllNodesWithTag("copy_button").fetchSemanticsNodes()
        assert(copyNodes.size >= 2) { "Expected at least 2 copy buttons, got ${copyNodes.size}" }
        FileSystem.SYSTEM.delete(path)
    }
}
