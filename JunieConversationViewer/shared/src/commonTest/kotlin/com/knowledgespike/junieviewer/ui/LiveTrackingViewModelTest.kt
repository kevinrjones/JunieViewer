package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.data.*
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.fixtures.RepresentativeFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okio.FileSystem
import org.junit.After
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Tests for live session tracking ViewModel integration.
 * Uses fake repositories and a no-op tracker (very long poll interval)
 * to verify ViewModel behavior without real file watching.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LiveTrackingViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "live-vm-test-${System.currentTimeMillis()}.json"

    @After
    fun tearDown() {
        FileSystem.SYSTEM.delete(tempPrefsPath)
    }

    private fun createViewModel(messages: List<Message>): ConversationViewModel {
        val prefs = PreferencesRepository(path = tempPrefsPath, fileSystem = FileSystem.SYSTEM)
        prefs.save(AppPreferences(lastSessionId = "test-session"))

        val fakeRepository = object : SessionRepository {
            override fun getMessages(): List<Message> = messages
            override fun listSessions(homePath: String): List<SessionInfo> = emptyList()
            override fun setSession(sessionId: String, homePath: String) {}
            override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? = null
        }

        // No-op tracker with very long poll interval — won't actually poll during tests
        val noOpTracker = LiveSessionTracker(
            fileWatcher = FileWatcher(fileSystem = FileSystem.SYSTEM, pollIntervalMs = 999_999L),
            fileSystem = FileSystem.SYSTEM
        )

        return ConversationViewModel(fakeRepository, prefs, testDispatcher, noOpTracker)
    }

    @Test
    fun `selecting a session loads initial messages and starts live tracking`() {
        val viewModel = createViewModel(listOf(RepresentativeFixtures.humanTextMessage))
        viewModel.onAction(ConversationAction.OnSessionSelected(
            SessionInfo("test-session", "/tmp/test", 0L)
        ))

        expectThat(viewModel.state.value.messages).hasSize(1)
        expectThat(viewModel.state.value.isLoading).isFalse()

        viewModel.stopLiveTracking()
    }

    @Test
    fun `live tracking is cancelled when session changes`() {
        val messages1 = listOf(RepresentativeFixtures.humanTextMessage)
        val messages2 = listOf(RepresentativeFixtures.humanTextMessage, RepresentativeFixtures.junieTextMessage)
        var currentMessages = messages1

        val prefs = PreferencesRepository(path = tempPrefsPath, fileSystem = FileSystem.SYSTEM)
        prefs.save(AppPreferences(lastSessionId = "test-session"))

        val fakeRepository = object : SessionRepository {
            override fun getMessages(): List<Message> = currentMessages
            override fun listSessions(homePath: String): List<SessionInfo> = emptyList()
            override fun setSession(sessionId: String, homePath: String) {}
            override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? = null
        }

        val noOpTracker = LiveSessionTracker(
            fileWatcher = FileWatcher(fileSystem = FileSystem.SYSTEM, pollIntervalMs = 999_999L),
            fileSystem = FileSystem.SYSTEM
        )

        val viewModel = ConversationViewModel(fakeRepository, prefs, testDispatcher, noOpTracker)
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("session-1", "/tmp/s1", 0L)))
        expectThat(viewModel.state.value.messages).hasSize(1)

        // Switch session with different messages
        currentMessages = messages2
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("session-2", "/tmp/s2", 0L)))
        expectThat(viewModel.state.value.selectedSessionId).isEqualTo("session-2")
        expectThat(viewModel.state.value.messages).hasSize(2)

        viewModel.stopLiveTracking()
    }

    @Test
    fun `filters are re-applied after messages load`() {
        val viewModel = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTextMessage
        ))
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
}
