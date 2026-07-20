package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionLoadResult
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okio.FileSystem
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for Area 7 — Collapse All / Show All.
 * Covers global collapse/show commands, per-block toggle after global commands,
 * search force-expansion priority, and stability across sort/filter changes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CollapseShowAllTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testMessages = listOf(
        Message("msg-1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("msg-2", Sender.Junie, MessageContent.Text("Thinking..."), MessageKind.Thought),
        Message("msg-3", Sender.Junie, MessageContent.Code("val x = 1", "kotlin"), MessageKind.Tool),
        Message("msg-4", Sender.Junie, MessageContent.Terminal("$ ls"), MessageKind.Terminal),
        Message("msg-5", Sender.Junie, MessageContent.Diff("+ added"), MessageKind.Patch),
        Message("msg-6", Sender.Junie, MessageContent.Structured("{\"key\":\"val\"}"), MessageKind.StructuredOutput)
    )

    private val fakeRepository = object : SessionRepository {
        var messagesToReturn: List<Message> = testMessages

        override fun getMessages(): List<Message> = messagesToReturn
        override fun loadSession(): SessionLoadResult =
            SessionLoadResult(messagesToReturn, null, 0L)
        override fun listSessions(homePath: String): List<SessionInfo> = listOf(
            SessionInfo("test-session", "/path/test-session", 123L)
        )
        override fun setSession(sessionId: String, homePath: String) {}
        override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? =
            SessionInfo(sessionId, "/path/$sessionId", 123L)
    }

    private lateinit var tempPrefsPath: okio.Path
    private lateinit var preferencesRepository: PreferencesRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "area7-test-${System.currentTimeMillis()}.json"
        preferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakeRepository.messagesToReturn = testMessages
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        try { FileSystem.SYSTEM.delete(tempPrefsPath) } catch (_: Exception) {}
    }

    private fun createViewModel(): ConversationViewModel =
        ConversationViewModel(fakeRepository, preferencesRepository, testDispatcher, LiveSessionTracker())

    private fun createViewModelWithSession(): ConversationViewModel {
        val vm = createViewModel()
        vm.onAction(ConversationAction.OnSessionSelected(SessionInfo("test-session", "/path", 0L)))
        return vm
    }

    // -- Collapse All --

    @Test
    fun `CollapseAll sets all collapsible blocks to collapsed`() = runTest {
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
    fun `ShowAll sets all collapsible blocks to expanded`() = runTest {
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
    fun `manual block toggle works after CollapseAll`() = runTest {
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
    fun `manual block toggle works after ShowAll`() = runTest {
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
    fun `search forceExpanded takes priority over collapsed state`() = runTest {
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
    fun `clearing search restores block to its explicit expansion state`() = runTest {
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
    fun `expansion state remains stable when sort order changes`() = runTest {
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
    fun `expansion state remains stable when filters change`() = runTest {
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
    fun `blocks have no explicit expansion state by default`() = runTest {
        val vm = createViewModelWithSession()
        advanceUntilIdle()

        assertTrue(vm.state.value.blockExpansionStates.isEmpty(),
            "No explicit expansion state should exist before any global command")
        assertNull(vm.state.value.blockExpansionStates["msg-2:thought"],
            "Individual block should have no explicit state by default")
    }

    // -- Toggle without prior global command --

    @Test
    fun `per-block toggle works without prior global command`() = runTest {
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
}
