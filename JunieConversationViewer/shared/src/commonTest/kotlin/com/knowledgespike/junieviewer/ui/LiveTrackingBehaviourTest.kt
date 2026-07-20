package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.fixtures.RepresentativeFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for the live tracking behaviour area: live session tracking ViewModel
 * integration (initial load, session switches, filter re-application), and
 * manual refresh / auto-refresh control (toggling, preference persistence, and
 * interaction with session selection).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LiveTrackingBehaviourTest {

    // =========================================================================
    // Origin: LiveTrackingViewModelTest — live session tracking ViewModel integration
    // Uses fake repositories and a no-op tracker (very long poll interval)
    // to verify ViewModel behavior without real file watching.
    // =========================================================================

    @Test
    fun `selecting a session loads initial messages and starts live tracking`() = runConversationStateTest {
        sessionRepository.messagesToReturn = listOf(RepresentativeFixtures.humanTextMessage)
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(
            SessionInfo("test-session", "/tmp/test", 0L)
        ))

        expectThat(viewModel.state.value.messages).hasSize(1)
        expectThat(viewModel.state.value.isLoading).isFalse()

        viewModel.stopLiveTracking()
    }

    @Test
    fun `live tracking is cancelled when session changes`() = runConversationStateTest {
        val messages1 = listOf(RepresentativeFixtures.humanTextMessage)
        val messages2 = listOf(RepresentativeFixtures.humanTextMessage, RepresentativeFixtures.junieTextMessage)
        
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        sessionRepository.messagesToReturn = messages1

        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("session-1", "/tmp/s1", 0L)))
        expectThat(viewModel.state.value.messages).hasSize(1)

        // Switch session with different messages
        sessionRepository.messagesToReturn = messages2
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("session-2", "/tmp/s2", 0L)))
        expectThat(viewModel.state.value.selectedSessionId).isEqualTo("session-2")
        expectThat(viewModel.state.value.messages).hasSize(2)

        viewModel.stopLiveTracking()
    }

    @Test
    fun `filters are re-applied after messages load`() = runConversationStateTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTextMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))

        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(
            SessionInfo("test-session", "/tmp/test", 0L)
        ))

        expectThat(viewModel.state.value.filteredMessages).hasSize(2)

        // Set search filter that won't match
        viewModel.onAction(ConversationAction.OnSearchQueryChange("Non-existent-query-xyz"))
        expectThat(viewModel.state.value.filteredMessages).isEmpty()

        // Clear filter
        viewModel.onAction(ConversationAction.OnSearchQueryChange(""))
        expectThat(viewModel.state.value.filteredMessages).hasSize(2)

        viewModel.stopLiveTracking()
    }

    // =========================================================================
    // Origin: RefreshAndAutoRefreshTest — Area 5 Refresh and Auto-Refresh Control
    // Covers manual refresh, auto-refresh toggle, live tracking wiring, and
    // preference persistence.
    // =========================================================================

    private val testMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("World"), MessageKind.Text)
    )

    private val updatedMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("World"), MessageKind.Text),
        Message("3", Sender.Human, MessageContent.Text("New message"), MessageKind.Text)
    )

    private fun ConversationStateTestScope.createViewModelWithSession(): ConversationViewModel {
        val vm = createViewModel()
        vm.onAction(ConversationAction.OnSessionSelected(SessionInfo("test-session", "/path", 0L)))
        return vm
    }

    // -- Manual Refresh --

    @Test
    fun `manual refresh reloads the current Session`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()
        val loadCountAfterSelect = sessionRepository.loadCount

        viewModel.onCommand(ConversationCommand.Refresh)
        advanceUntilIdle()

        assertTrue(sessionRepository.loadCount > loadCountAfterSelect, "Refresh should trigger a reload")
    }

    @Test
    fun `manual refresh is a no-op when no Session is selected`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val loadCountBefore = sessionRepository.loadCount

        viewModel.onCommand(ConversationCommand.Refresh)
        advanceUntilIdle()

        assertEquals(loadCountBefore, sessionRepository.loadCount, "Refresh should not load when no Session selected")
    }

    @Test
    fun `manual refresh preserves Search Query and Filters`() = runConversationStateTest(testMessages) {
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
    fun `manual refresh updates messages when file content changes`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.messages.size)

        // Simulate file content change
        sessionRepository.messagesToReturn = updatedMessages
        viewModel.onCommand(ConversationCommand.Refresh)
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.messages.size, "Messages should reflect updated file content")
    }

    // -- Auto-Refresh Toggle --

    @Test
    fun `toggling auto-refresh off updates state`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isAutoRefreshEnabled)

        viewModel.onCommand(ConversationCommand.ToggleAutoRefresh)
        assertFalse(viewModel.state.value.isAutoRefreshEnabled)
    }

    @Test
    fun `toggling auto-refresh off keeps Messages visible`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()
        val messagesBefore = viewModel.state.value.messages

        viewModel.onCommand(ConversationCommand.ToggleAutoRefresh)
        advanceUntilIdle()

        assertEquals(messagesBefore, viewModel.state.value.messages, "Messages should remain visible after disabling auto-refresh")
    }

    @Test
    fun `toggling auto-refresh on restarts live tracking state`() = runConversationStateTest(testMessages) {
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
    fun `auto-refresh preference is saved when toggled`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        viewModel.onCommand(ConversationCommand.ToggleAutoRefresh)
        advanceUntilIdle()

        val savedPrefs = preferencesRepository.load()
        assertFalse(savedPrefs.isAutoRefreshEnabled, "Preference should be saved as disabled")
    }

    @Test
    fun `auto-refresh preference is loaded from preferences on init`() = runConversationStateTest(testMessages) {
        // Save a preference with auto-refresh disabled
        preferencesRepository.save(AppPreferences(isAutoRefreshEnabled = false))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isAutoRefreshEnabled, "Auto-refresh should load as disabled from preferences")
    }

    @Test
    fun `auto-refresh defaults to enabled for missing preference`() = runConversationStateTest(testMessages) {
        // No preference file exists — defaults should apply
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAutoRefreshEnabled, "Auto-refresh should default to enabled")
    }

    // -- Session Selection and Auto-Refresh Interaction --

    @Test
    fun `selecting a Session with auto-refresh disabled does not start live tracking`() = runConversationStateTest(testMessages) {
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
    fun `manual refresh preserves auto-refresh disabled state`() = runConversationStateTest(testMessages) {
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
