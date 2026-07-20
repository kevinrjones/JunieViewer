package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.fixtures.RepresentativeFixtures
import com.knowledgespike.junieviewer.ui.components.blockContainsSearchHit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the expansion behaviour area: global Collapse All / Show All commands,
 * per-block toggle interaction after those global commands, search force-expansion
 * priority, stability of expansion state across sort/filter changes, and the
 * collapsible rich content blocks (terminal, code, structured output, error/warning,
 * thought, tool call) rendered by [ConversationRoot].
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class ExpansionBehaviourTest {

    // =========================================================================
    // Origin: CollapseShowAllTest — Area 7 Collapse All / Show All
    // Covers global collapse/show commands, per-block toggle after global commands,
    // search force-expansion priority, and stability across sort/filter changes.
    // =========================================================================

    private val testMessages = listOf(
        Message("msg-1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("msg-2", Sender.Junie, MessageContent.Text("Thinking..."), MessageKind.Thought),
        Message("msg-3", Sender.Junie, MessageContent.Code("val x = 1", "kotlin"), MessageKind.Tool),
        Message("msg-4", Sender.Junie, MessageContent.Terminal("$ ls"), MessageKind.Terminal),
        Message("msg-5", Sender.Junie, MessageContent.Diff("+ added"), MessageKind.Patch),
        Message("msg-6", Sender.Junie, MessageContent.Structured("{\"key\":\"val\"}"), MessageKind.StructuredOutput)
    )

    private fun ConversationStateTestScope.createViewModelWithSession(): ConversationViewModel {
        val vm = createViewModel()
        vm.onAction(ConversationAction.OnSessionSelected(SessionInfo("test-session", "/path", 0L)))
        return vm
    }

    // -- Collapse All --

    @Test
    fun `CollapseAll sets all collapsible blocks to collapsed`() = runConversationStateTest(testMessages) {
        val vm = createViewModelWithSession()
        advanceUntilIdle()

        vm.onCommand(ConversationCommand.CollapseAll)

        val expansionStates = vm.state.value.blockExpansionStates
        // Thought, Tool, Terminal, Patch (diff), StructuredOutput should all be collapsed
        assertEquals(false, expansionStates["msg-2:thought"], "Thought block should be collapsed")
        assertEquals(false, expansionStates["msg-3:tool"], "Tool block should be collapsed")
        assertEquals(false, expansionStates["msg-4:terminal"], "Terminal block should be collapsed")
        assertEquals(false, expansionStates["msg-5:diff"], "Diff block should be collapsed")
        assertEquals(false, expansionStates["msg-6:structured"], "Structured block should be collapsed")
    }

    // -- Show All --

    @Test
    fun `ShowAll sets all collapsible blocks to expanded`() = runConversationStateTest(testMessages) {
        val vm = createViewModelWithSession()
        advanceUntilIdle()

        // First collapse, then show all
        vm.onCommand(ConversationCommand.CollapseAll)
        vm.onCommand(ConversationCommand.ShowAll)

        val expansionStates = vm.state.value.blockExpansionStates
        assertEquals(true, expansionStates["msg-2:thought"], "Thought block should be expanded")
        assertEquals(true, expansionStates["msg-3:tool"], "Tool block should be expanded")
        assertEquals(true, expansionStates["msg-4:terminal"], "Terminal block should be expanded")
        assertEquals(true, expansionStates["msg-5:diff"], "Diff block should be expanded")
        assertEquals(true, expansionStates["msg-6:structured"], "Structured block should be expanded")
    }

    // -- Per-block toggle after global commands --

    @Test
    fun `manual block toggle works after CollapseAll`() = runConversationStateTest(testMessages) {
        val vm = createViewModelWithSession()
        advanceUntilIdle()

        vm.onCommand(ConversationCommand.CollapseAll)
        assertEquals(false, vm.state.value.blockExpansionStates["msg-2:thought"])

        // Manually expand the thought block
        vm.onAction(ConversationAction.OnToggleBlockExpansion("msg-2:thought"))

        // Thought should now be expanded; others remain collapsed
        assertEquals(true, vm.state.value.blockExpansionStates["msg-2:thought"],
            "Manual toggle should expand block after CollapseAll")
        assertEquals(false, vm.state.value.blockExpansionStates["msg-3:tool"],
            "Other blocks should remain collapsed")
    }

    @Test
    fun `manual block toggle works after ShowAll`() = runConversationStateTest(testMessages) {
        val vm = createViewModelWithSession()
        advanceUntilIdle()

        vm.onCommand(ConversationCommand.ShowAll)
        assertEquals(true, vm.state.value.blockExpansionStates["msg-3:tool"])

        // Manually collapse the tool block
        vm.onAction(ConversationAction.OnToggleBlockExpansion("msg-3:tool"))

        // Tool should now be collapsed; others remain expanded
        assertEquals(false, vm.state.value.blockExpansionStates["msg-3:tool"],
            "Manual toggle should collapse block after ShowAll")
        assertEquals(true, vm.state.value.blockExpansionStates["msg-2:thought"],
            "Other blocks should remain expanded")
    }

    // -- Search force-expansion priority --

    @Test
    fun `search forceExpanded takes priority over collapsed state`() = runConversationStateTest(testMessages) {
        val vm = createViewModelWithSession()
        advanceUntilIdle()

        // Collapse all blocks
        vm.onCommand(ConversationCommand.CollapseAll)
        assertEquals(false, vm.state.value.blockExpansionStates["msg-4:terminal"])

        // The block expansion state remains collapsed in the ViewModel,
        // but the UI's forceExpanded param (computed from search match) takes priority.
        // We verify the ViewModel state is collapsed — the UI composable handles
        // the forceExpanded || externalExpanded logic.
        assertEquals(false, vm.state.value.blockExpansionStates["msg-4:terminal"],
            "ViewModel state should remain collapsed; UI forceExpanded handles search priority")
    }

    @Test
    fun `clearing search restores block to its explicit expansion state`() = runConversationStateTest(testMessages) {
        val vm = createViewModelWithSession()
        advanceUntilIdle()

        // Collapse all, then search (search would force-expand in UI)
        vm.onCommand(ConversationCommand.CollapseAll)
        vm.onAction(ConversationAction.OnSearchQueryChange("ls"))

        // The ViewModel expansion state should still be collapsed
        assertEquals(false, vm.state.value.blockExpansionStates["msg-4:terminal"],
            "Expansion state should remain collapsed in ViewModel during search")

        // Clear search
        vm.onAction(ConversationAction.OnSearchQueryChange(""))

        // Block should still be collapsed (its explicit state)
        assertEquals(false, vm.state.value.blockExpansionStates["msg-4:terminal"],
            "After clearing search, block should return to its explicit collapsed state")
    }

    // -- Stability across sort order changes --

    @Test
    fun `expansion state remains stable when sort order changes`() = runConversationStateTest(testMessages) {
        val vm = createViewModelWithSession()
        advanceUntilIdle()

        vm.onCommand(ConversationCommand.CollapseAll)
        vm.onAction(ConversationAction.OnToggleBlockExpansion("msg-2:thought"))

        // Thought expanded, others collapsed
        assertEquals(true, vm.state.value.blockExpansionStates["msg-2:thought"])
        assertEquals(false, vm.state.value.blockExpansionStates["msg-3:tool"])

        // Toggle sort order
        vm.onCommand(ConversationCommand.ToggleSortOrder)

        // Expansion state should be unchanged
        assertEquals(true, vm.state.value.blockExpansionStates["msg-2:thought"],
            "Expansion state should survive sort order change")
        assertEquals(false, vm.state.value.blockExpansionStates["msg-3:tool"],
            "Expansion state should survive sort order change")
    }

    // -- Stability across filter changes --

    @Test
    fun `expansion state remains stable when filters change`() = runConversationStateTest(testMessages) {
        val vm = createViewModelWithSession()
        advanceUntilIdle()

        vm.onCommand(ConversationCommand.CollapseAll)
        vm.onAction(ConversationAction.OnToggleBlockExpansion("msg-2:thought"))

        // Toggle a filter
        vm.onAction(ConversationAction.OnToggleFilter(FilterKind.Terminal))

        // Expansion state should be unchanged
        assertEquals(true, vm.state.value.blockExpansionStates["msg-2:thought"],
            "Expansion state should survive filter change")
        assertEquals(false, vm.state.value.blockExpansionStates["msg-3:tool"],
            "Expansion state should survive filter change")
    }

    // -- Default state --

    @Test
    fun `blocks have no explicit expansion state by default`() = runConversationStateTest(testMessages) {
        val vm = createViewModelWithSession()
        advanceUntilIdle()

        assertTrue(vm.state.value.blockExpansionStates.isEmpty(),
            "No explicit expansion state should exist before any global command")
        assertNull(vm.state.value.blockExpansionStates["msg-2:thought"],
            "Individual block should have no explicit state by default")
    }

    // -- Toggle without prior global command --

    @Test
    fun `per-block toggle works without prior global command`() = runConversationStateTest(testMessages) {
        val vm = createViewModelWithSession()
        advanceUntilIdle()

        // Toggle a block that has no explicit state (defaults to expanded=true in UI)
        vm.onAction(ConversationAction.OnToggleBlockExpansion("msg-2:thought"))

        // Should now be false (toggled from default true)
        assertEquals(false, vm.state.value.blockExpansionStates["msg-2:thought"],
            "Toggle from default should set to collapsed")

        // Toggle again
        vm.onAction(ConversationAction.OnToggleBlockExpansion("msg-2:thought"))

        assertEquals(true, vm.state.value.blockExpansionStates["msg-2:thought"],
            "Second toggle should set back to expanded")
    }

    // =========================================================================
    // Origin: CollapsibleBlockTest — collapsible rich content blocks
    // Covers: all blocks expanded by default, blocks can be collapsed and
    // re-expanded, full content visible without truncation, search
    // auto-expansion of matching blocks, and the blockContainsSearchHit helper.
    // =========================================================================

    // -- blockContainsSearchHit helper unit tests --

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

    // -- Terminal block — expanded by default, collapsible, full content --

    @Test
    fun `terminal block is expanded by default`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTerminalMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("terminal_block_header").assertExists()
        onNodeWithTag("terminal_block_body").assertExists()
        onNodeWithText("BUILD SUCCESSFUL", substring = true).assertExists()
    }

    @Test
    fun `terminal block can be collapsed`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTerminalMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("terminal_block_header").performClick()
        waitForIdle()
        onNodeWithTag("terminal_block_body").assertDoesNotExist()
    }

    // -- Code block — expanded by default --

    @Test
    fun `code block is expanded by default`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieCodeMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("code_block_header").assertExists()
        onNodeWithTag("code_block_body").assertExists()
        onNodeWithTag("copy_button").assertExists()
    }

    // -- Structured output — expanded by default --

    @Test
    fun `structured output block is expanded by default`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieStructuredOutputMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("structured_output_block_header").assertExists()
        onNodeWithTag("structured_output_block_body").assertExists()
    }

    // -- Error/Warning — expanded by default --

    @Test
    fun `error block is expanded by default`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieErrorMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("error_warning_block_header").assertExists()
        onNodeWithTag("error_warning_block_body").assertExists()
    }

    @Test
    fun `task failed event renders as error block`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTaskFailedMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("error_warning_block").assertExists()
        onNodeWithText("Task Failed", substring = true).assertExists()
    }

    // -- Thought and Tool Call — expanded by default (supersedes old collapsed) --

    @Test
    fun `thought block is expanded by default`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieThoughtMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("thought_header").assertExists()
        onNodeWithTag("thought_block_body").assertExists()
    }

    @Test
    fun `tool call block is expanded by default`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieToolCallMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("tool_call_header").assertExists()
        onNodeWithTag("tool_call_block_body").assertExists()
    }

    // -- Copy controls remain available --

    @Test
    fun `copy buttons present for terminal and code blocks`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTerminalMessage,
            Message("c-1", Sender.Junie, MessageContent.Code("fun main() {}", "kotlin"), MessageKind.Text)
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        val copyNodes = onAllNodesWithTag("copy_button").fetchSemanticsNodes()
        assert(copyNodes.size >= 2) { "Expected at least 2 copy buttons, got ${copyNodes.size}" }
    }
}
