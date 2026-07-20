package com.knowledgespike.junieviewer.ui

import app.cash.turbine.test
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.PreferencesRepository
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
import kotlin.test.assertTrue

/**
 * Tests for Area 2 — Command/Action Model Design.
 * Covers command enablement state derivation and command dispatch mapping.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConversationCommandTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Hello alpha"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("Hello alpha"), MessageKind.Text),
        Message("3", Sender.Human, MessageContent.Text("Other beta"), MessageKind.Text),
        Message("4", Sender.Junie, MessageContent.Text("Other beta"), MessageKind.Text)
    )

    private val fakeRepository = object : SessionRepository {
        var lastSessionId: String? = null
        override fun getMessages(): List<Message> = testMessages
        override fun listSessions(homePath: String): List<SessionInfo> = listOf(
            SessionInfo("test-session", "/path/test-session", 123L)
        )
        override fun setSession(sessionId: String, homePath: String) {
            lastSessionId = sessionId
        }
        override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? =
            SessionInfo(sessionId, "/path/$sessionId", 123L)
    }

    private lateinit var tempPrefsPath: okio.Path
    private lateinit var fakePreferencesRepository: PreferencesRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "cmd-test-${System.currentTimeMillis()}.json"
        fakePreferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        FileSystem.SYSTEM.delete(tempPrefsPath)
    }

    private fun createViewModel(): ConversationViewModel =
        ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())

    // -- Command State Derivation --

    @Test
    fun `refresh is disabled when no session is selected`() {
        val state = ConversationState(selectedSessionId = null)
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertFalse(cmdState.refreshEnabled)
    }

    @Test
    fun `refresh is enabled when session is selected and not loading`() {
        val state = ConversationState(selectedSessionId = "s1", isLoading = false)
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertTrue(cmdState.refreshEnabled)
    }

    @Test
    fun `refresh is disabled when loading`() {
        val state = ConversationState(selectedSessionId = "s1", isLoading = true)
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertFalse(cmdState.refreshEnabled)
    }

    @Test
    fun `find next and previous are disabled with blank query`() {
        val state = ConversationState(searchQuery = "", messages = testMessages, filteredMessages = testMessages)
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertFalse(cmdState.findNextEnabled)
        assertFalse(cmdState.findPreviousEnabled)
    }

    @Test
    fun `find next and previous are enabled when query has matching results`() {
        val state = ConversationState(
            searchQuery = "alpha",
            messages = testMessages,
            filteredMessages = testMessages.take(2)
        )
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertTrue(cmdState.findNextEnabled)
        assertTrue(cmdState.findPreviousEnabled)
    }

    @Test
    fun `find next and previous are disabled when query has no results`() {
        val state = ConversationState(
            searchQuery = "nonexistent",
            messages = testMessages,
            filteredMessages = emptyList()
        )
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertFalse(cmdState.findNextEnabled)
        assertFalse(cmdState.findPreviousEnabled)
    }

    @Test
    fun `open session and settings are always enabled`() {
        val state = ConversationState()
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertTrue(cmdState.openSessionEnabled)
        assertTrue(cmdState.settingsEnabled)
    }

    @Test
    fun `toggle auto-refresh is disabled without session`() {
        val state = ConversationState(selectedSessionId = null)
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertFalse(cmdState.toggleAutoRefreshEnabled)
    }

    @Test
    fun `toggle auto-refresh is enabled with session`() {
        val state = ConversationState(selectedSessionId = "s1")
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertTrue(cmdState.toggleAutoRefreshEnabled)
    }

    @Test
    fun `sort order and collapse controls are disabled without messages`() {
        val state = ConversationState(messages = emptyList())
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertFalse(cmdState.toggleSortOrderEnabled)
        assertFalse(cmdState.collapseAllEnabled)
        assertFalse(cmdState.showAllEnabled)
    }

    @Test
    fun `sort order and collapse controls are enabled with messages`() {
        val state = ConversationState(messages = testMessages)
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertTrue(cmdState.toggleSortOrderEnabled)
        assertTrue(cmdState.collapseAllEnabled)
        assertTrue(cmdState.showAllEnabled)
    }

    @Test
    fun `copy is enabled when text is selected`() {
        val state = ConversationState(messages = testMessages, hasTextSelection = true)
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertTrue(cmdState.copyEnabled)
    }

    @Test
    fun `copy is disabled when no text is selected`() {
        val state = ConversationState(messages = testMessages, hasTextSelection = false)
        val cmdState = ConversationCommandState.fromConversationState(state)
        assertFalse(cmdState.copyEnabled)
    }

    @Test
    fun `text selection action updates hasTextSelection state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.hasTextSelection)

        viewModel.onAction(ConversationAction.OnTextSelectionChanged("container-1", true))
        assertTrue(viewModel.state.value.hasTextSelection)

        // A second container selecting then clearing keeps selection while any remains
        viewModel.onAction(ConversationAction.OnTextSelectionChanged("container-2", true))
        viewModel.onAction(ConversationAction.OnTextSelectionChanged("container-1", false))
        assertTrue(viewModel.state.value.hasTextSelection)

        viewModel.onAction(ConversationAction.OnTextSelectionChanged("container-2", false))
        assertFalse(viewModel.state.value.hasTextSelection)
    }

    // -- Command Dispatch --

    @Test
    fun `open session command toggles session picker`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCommand(ConversationCommand.OpenSession)

        assertTrue(viewModel.state.value.isSessionPickerOpen)
    }

    @Test
    fun `settings command toggles settings dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCommand(ConversationCommand.Settings)

        assertTrue(viewModel.state.value.isSettingsOpen)
    }

    @Test
    fun `find next command maps to next match navigation`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("alpha"))
        advanceUntilIdle()

        val initialIndex = viewModel.state.value.currentMatchIndex
        viewModel.onCommand(ConversationCommand.FindNext)
        val nextIndex = viewModel.state.value.currentMatchIndex

        assertEquals(initialIndex + 1, nextIndex)
    }

    @Test
    fun `find previous command maps to previous match navigation`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("alpha"))
        advanceUntilIdle()

        // Move forward first, then back
        viewModel.onCommand(ConversationCommand.FindNext)
        viewModel.onCommand(ConversationCommand.FindPrevious)

        assertEquals(0, viewModel.state.value.currentMatchIndex)
    }

    @Test
    fun `toggle auto-refresh command toggles state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAutoRefreshEnabled)

        viewModel.onCommand(ConversationCommand.ToggleAutoRefresh)
        assertFalse(viewModel.state.value.isAutoRefreshEnabled)

        viewModel.onCommand(ConversationCommand.ToggleAutoRefresh)
        assertTrue(viewModel.state.value.isAutoRefreshEnabled)
    }

    @Test
    fun `toggle sort order command cycles between oldest and newest first`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(SortOrder.OldestFirst, viewModel.state.value.sortOrder)

        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        assertEquals(SortOrder.NewestFirst, viewModel.state.value.sortOrder)

        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        assertEquals(SortOrder.OldestFirst, viewModel.state.value.sortOrder)
    }

    // -- Area 8: Copy and Search Integration --

    @Test
    fun `copy command does not crash when no selection exists`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Copy emits a CopyText event — must not throw
        viewModel.onCommand(ConversationCommand.Copy)
        // State should remain unchanged
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `copy command emits CopyText event`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onCommand(ConversationCommand.Copy)
            val event = awaitItem()
            assertTrue(event is ConversationEvent.CopyText)
        }
    }

    @Test
    fun `focus search command emits FocusSearch event`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onCommand(ConversationCommand.FocusSearch)
            val event = awaitItem()
            assertEquals(ConversationEvent.FocusSearch, event)
        }
    }

    @Test
    fun `find next wraps around at end of matches`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("alpha"))
        advanceUntilIdle()

        val matchCount = viewModel.state.value.filteredMessages.size
        // Navigate past the last match — should wrap
        repeat(matchCount + 1) {
            viewModel.onCommand(ConversationCommand.FindNext)
        }
        // After wrapping, index should be valid (0 or 1)
        assertTrue(viewModel.state.value.currentMatchIndex in 0 until matchCount)
    }

    @Test
    fun `find previous wraps around at beginning of matches`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("alpha"))
        advanceUntilIdle()

        val matchCount = viewModel.state.value.filteredMessages.size
        // Navigate backward from index 0 — should wrap to end
        viewModel.onCommand(ConversationCommand.FindPrevious)
        assertEquals(matchCount - 1, viewModel.state.value.currentMatchIndex)
    }

    @Test
    fun `match navigation respects current search query`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("beta"))
        advanceUntilIdle()

        // Only messages containing "beta" should be in filteredMessages
        val filtered = viewModel.state.value.filteredMessages
        assertTrue(filtered.all { msg ->
            val text = (msg.content as? MessageContent.Text)?.text ?: ""
            text.contains("beta", ignoreCase = true)
        })

        // FindNext should work within filtered results
        viewModel.onCommand(ConversationCommand.FindNext)
        assertTrue(viewModel.state.value.currentMatchIndex >= 0)
    }

    @Test
    fun `all command cases are handled without throwing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Dispatch every command — none should throw
        ConversationCommand::class.sealedSubclasses.forEach { subclass ->
            val instance = subclass.objectInstance
            if (instance != null) {
                viewModel.onCommand(instance)
            }
        }
        // If we reach here, no exception was thrown
    }
}
