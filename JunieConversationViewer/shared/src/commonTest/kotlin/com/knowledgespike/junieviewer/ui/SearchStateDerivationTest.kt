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

/**
 * Tests for the Sprint 6 Area 1 quick wins:
 * - F8: a Search Query change produces exactly one atomic state emission.
 * - F10: Turn grouping is derived in the ViewModel alongside filteredMessages,
 *   not recomputed in composition.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchStateDerivationTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("Hello back"), MessageKind.Text),
        Message("3", Sender.Human, MessageContent.Text("Other"), MessageKind.Text),
        Message("4", Sender.Junie, MessageContent.Text("Other reply"), MessageKind.Text)
    )

    private val fakeRepository = object : SessionRepository {
        override fun getMessages(): List<Message> = testMessages
        override fun listSessions(homePath: String): List<SessionInfo> = emptyList()
        override fun setSession(sessionId: String, homePath: String) {}
        override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? =
            SessionInfo(sessionId, "/path/$sessionId", 123L)
    }

    private lateinit var tempPrefsPath: okio.Path
    private lateinit var preferencesRepository: PreferencesRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "test-prefs-derivation-${System.currentTimeMillis()}.json"
        preferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        FileSystem.SYSTEM.delete(tempPrefsPath)
    }

    private fun loadedViewModel(): ConversationViewModel {
        val viewModel = ConversationViewModel(fakeRepository, preferencesRepository, testDispatcher, LiveSessionTracker())
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        return viewModel
    }

    @Test
    fun `search query change produces exactly one state emission`() = runTest {
        val viewModel = loadedViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem() // current state

            viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))

            val emitted = awaitItem()
            assertEquals("Hello", emitted.searchQuery)
            assertEquals(2, emitted.filteredMessages.size)
            assertEquals(0, emitted.currentMatchIndex)

            // No further emissions for this action — the update is atomic
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing the search query produces exactly one state emission`() = runTest {
        val viewModel = loadedViewModel()
        advanceUntilIdle()
        viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem() // current state

            viewModel.onAction(ConversationAction.OnSearchQueryChange(""))

            val emitted = awaitItem()
            assertEquals("", emitted.searchQuery)
            assertEquals(4, emitted.filteredMessages.size)
            assertEquals(-1, emitted.currentMatchIndex)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `turns are derived alongside filtered messages after load`() = runTest {
        val viewModel = loadedViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(groupMessagesIntoTurns(state.filteredMessages), state.turns)
        assertEquals(4, state.turns.sumOf { it.messages.size })
    }

    @Test
    fun `turns are re-derived when the search query changes`() = runTest {
        val viewModel = loadedViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(groupMessagesIntoTurns(state.filteredMessages), state.turns)
        assertEquals(2, state.turns.sumOf { it.messages.size })
    }
}
