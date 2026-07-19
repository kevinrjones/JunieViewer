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
import kotlin.test.assertTrue

/**
 * Tests for Area 5 — Refresh and Auto-Refresh Control.
 * Covers manual refresh, auto-refresh toggle, live tracking wiring, and preference persistence.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RefreshAndAutoRefreshTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("World"), MessageKind.Text)
    )

    private val updatedMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("World"), MessageKind.Text),
        Message("3", Sender.Human, MessageContent.Text("New message"), MessageKind.Text)
    )

    /** Fake repository that tracks load calls and can return different message sets. */
    private val fakeRepository = object : SessionRepository {
        var loadCount = 0
        var messagesToReturn: List<Message> = testMessages

        override fun getMessages(): List<Message> = messagesToReturn
        override fun loadSession(): SessionLoadResult {
            loadCount++
            // Return null eventsFilePath to avoid starting real FileWatcher polling in tests
            return SessionLoadResult(messagesToReturn, null, 0L)
        }
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
        tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "area5-test-${System.currentTimeMillis()}.json"
        preferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakeRepository.loadCount = 0
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

    // -- Manual Refresh --

    @Test
    fun `manual refresh reloads the current Session`() = runTest {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()
        val loadCountAfterSelect = fakeRepository.loadCount

        viewModel.onCommand(ConversationCommand.Refresh)
        advanceUntilIdle()

        assertTrue(fakeRepository.loadCount > loadCountAfterSelect, "Refresh should trigger a reload")
    }

    @Test
    fun `manual refresh is a no-op when no Session is selected`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val loadCountBefore = fakeRepository.loadCount

        viewModel.onCommand(ConversationCommand.Refresh)
        advanceUntilIdle()

        assertEquals(loadCountBefore, fakeRepository.loadCount, "Refresh should not load when no Session selected")
    }

    @Test
    fun `manual refresh preserves Search Query and Filters`() = runTest {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        // Set a search query and toggle a filter
        viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))
        viewModel.onAction(ConversationAction.OnToggleFilter(FilterKind.Thought))
        advanceUntilIdle()

        val queryBefore = viewModel.state.value.searchQuery
        val filterBefore = viewModel.state.value.filter

        viewModel.onCommand(ConversationCommand.Refresh)
        advanceUntilIdle()

        assertEquals(queryBefore, viewModel.state.value.searchQuery, "Search Query should be preserved after refresh")
        assertEquals(filterBefore, viewModel.state.value.filter, "Filters should be preserved after refresh")
    }

    @Test
    fun `manual refresh updates messages when file content changes`() = runTest {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.messages.size)

        // Simulate file content change
        fakeRepository.messagesToReturn = updatedMessages
        viewModel.onCommand(ConversationCommand.Refresh)
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.messages.size, "Messages should reflect updated file content")
    }

    // -- Auto-Refresh Toggle --

    @Test
    fun `toggling auto-refresh off updates state`() = runTest {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isAutoRefreshEnabled)

        viewModel.onCommand(ConversationCommand.ToggleAutoRefresh)
        assertFalse(viewModel.state.value.isAutoRefreshEnabled)
    }

    @Test
    fun `toggling auto-refresh off keeps Messages visible`() = runTest {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()
        val messagesBefore = viewModel.state.value.messages

        viewModel.onCommand(ConversationCommand.ToggleAutoRefresh)
        advanceUntilIdle()

        assertEquals(messagesBefore, viewModel.state.value.messages, "Messages should remain visible after disabling auto-refresh")
    }

    @Test
    fun `toggling auto-refresh on restarts live tracking state`() = runTest {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        // Disable then re-enable
        viewModel.onCommand(ConversationCommand.ToggleAutoRefresh)
        assertFalse(viewModel.state.value.isAutoRefreshEnabled)

        viewModel.onCommand(ConversationCommand.ToggleAutoRefresh)
        assertTrue(viewModel.state.value.isAutoRefreshEnabled)
    }

    // -- Auto-Refresh Preference Persistence --

    @Test
    fun `auto-refresh preference is saved when toggled`() = runTest {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        viewModel.onCommand(ConversationCommand.ToggleAutoRefresh)
        advanceUntilIdle()

        val savedPrefs = preferencesRepository.load()
        assertFalse(savedPrefs.isAutoRefreshEnabled, "Preference should be saved as disabled")
    }

    @Test
    fun `auto-refresh preference is loaded from preferences on init`() = runTest {
        // Save a preference with auto-refresh disabled
        preferencesRepository.save(AppPreferences(isAutoRefreshEnabled = false))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isAutoRefreshEnabled, "Auto-refresh should load as disabled from preferences")
    }

    @Test
    fun `auto-refresh defaults to enabled for missing preference`() = runTest {
        // No preference file exists — defaults should apply
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAutoRefreshEnabled, "Auto-refresh should default to enabled")
    }

    // -- Session Selection and Auto-Refresh Interaction --

    @Test
    fun `selecting a Session with auto-refresh disabled does not start live tracking`() = runTest {
        // Save preference with auto-refresh disabled
        preferencesRepository.save(AppPreferences(isAutoRefreshEnabled = false))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test-session", "/path", 0L)))
        advanceUntilIdle()

        // Messages should still load (one-shot), but auto-refresh remains off
        assertFalse(viewModel.state.value.isAutoRefreshEnabled)
        assertTrue(viewModel.state.value.messages.isNotEmpty(), "Messages should load even with auto-refresh off")
    }

    // -- Command State Reflects Auto-Refresh --

    @Test
    fun `command state reflects auto-refresh active state`() {
        val stateEnabled = ConversationState(isAutoRefreshEnabled = true, selectedSessionId = "s1")
        val cmdEnabled = ConversationCommandState.fromConversationState(stateEnabled)
        assertTrue(cmdEnabled.isAutoRefreshActive)

        val stateDisabled = ConversationState(isAutoRefreshEnabled = false, selectedSessionId = "s1")
        val cmdDisabled = ConversationCommandState.fromConversationState(stateDisabled)
        assertFalse(cmdDisabled.isAutoRefreshActive)
    }

    @Test
    fun `manual refresh preserves auto-refresh disabled state`() = runTest {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        // Disable auto-refresh
        viewModel.onCommand(ConversationCommand.ToggleAutoRefresh)
        assertFalse(viewModel.state.value.isAutoRefreshEnabled)

        // Refresh should not re-enable auto-refresh
        viewModel.onCommand(ConversationCommand.Refresh)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isAutoRefreshEnabled, "Auto-refresh should remain disabled after manual refresh")
    }
}
