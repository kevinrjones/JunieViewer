package com.knowledgespike.junieviewer.ui

import app.cash.turbine.test
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.ui.theme.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationViewModelTest {

    private val testMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("Hello"), MessageKind.Text),
        Message("3", Sender.Human, MessageContent.Text("Other"), MessageKind.Text),
        Message("4", Sender.Junie, MessageContent.Text("Other"), MessageKind.Text)
    )

    @Test
    fun `initial state is correctly loaded from preferences`() = runConversationStateTest {
        preferencesRepository.save(AppPreferences(lastSessionId = "saved-session", junieHomePath = "/custom/path"))
        
        val viewModel = createViewModel()
        
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("/custom/path", state.junieHomePath)
            assertEquals("saved-session", state.selectedSessionId)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search query updates filtered messages`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModel()
        // Ensure a session is selected and messages are loaded
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(4, initialState.filteredMessages.size)

            viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))
            // Single atomic emission: searchQuery and filteredMessages update together (F8)
            val finalState = awaitItem()
            
            assertEquals("Hello", finalState.searchQuery)
            assertEquals(2, finalState.filteredMessages.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `top-level search query change is isolated from conversation search query`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))
        viewModel.onAction(ConversationAction.OnTopLevelSearchQueryChange("  global   query "))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Hello", state.searchQuery)
        assertEquals("global query", state.topLevelSearchQuery.normalized)
        assertEquals(TopLevelSearchStatus.Idle, state.topLevelSearchStatus)
    }

    @Test
    fun `opening and closing top-level search does not clear conversation search query`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))
        viewModel.onAction(ConversationAction.OnToggleTopLevelSearch)
        viewModel.onAction(ConversationAction.OnToggleTopLevelSearch)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Hello", state.searchQuery)
        assertFalse(state.isTopLevelSearchOpen)
    }

    @Test
    fun `blank top-level submit transitions to empty-query status deterministically`() = runConversationStateTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnTopLevelSearchQueryChange("   \t"))
        viewModel.onAction(ConversationAction.OnSubmitTopLevelSearch)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(TopLevelSearchStatus.EmptyQuery, state.topLevelSearchStatus)
        assertEquals("", state.topLevelSearchResults.query.normalized)
    }

    @Test
    fun `cancel top-level search resets top-level state without mutating conversation search query`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))
        viewModel.onAction(ConversationAction.OnTopLevelSearchQueryChange("global"))
        viewModel.onAction(ConversationAction.OnSubmitTopLevelSearch)
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnCancelTopLevelSearch)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Hello", state.searchQuery)
        assertEquals(TopLevelSearchStatus.Idle, state.topLevelSearchStatus)
        assertEquals("global", state.topLevelSearchQuery.normalized)
        assertEquals(0, state.topLevelSearchResults.sessionResults.size)
    }

    @Test
    fun `selecting a top-level search result updates top-level state and emits event`() = runConversationStateTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val selectedResult = TopLevelSessionSearchResult(
            session = TopLevelSessionIdentity("session-42", "/sessions/session-42", 42L),
            matchCount = 3
        )

        viewModel.events.test {
            viewModel.onAction(ConversationAction.OnTopLevelSearchResultSelected(selectedResult))
            val event = awaitItem()

            assertEquals(ConversationEvent.TopLevelSearchResultSelected("session-42"), event)
            assertEquals("session-42", viewModel.state.value.topLevelSelectedResult?.session?.sessionId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggling session picker updates state and loads sessions`() = runConversationStateTest {
        sessionRepository.sessionsToReturn = listOf(
            SessionInfo("test-session", "/path/test-session", 123L)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem() // skip current state
            
            viewModel.onAction(ConversationAction.OnToggleSessionPicker)
            // First item: picker opened
            awaitItem()
            // Second item: sessions loaded
            val state = awaitItem()
            
            assertTrue(state.isSessionPickerOpen)
            assertEquals(1, state.sessions.size)
            assertEquals("test-session", state.sessions[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting session updates preference and loads messages`() = runConversationStateTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val newSession = SessionInfo("new-session", "/path/new-session", 456L)
        viewModel.onAction(ConversationAction.OnSessionSelected(newSession))
        advanceUntilIdle()

        assertEquals("new-session", viewModel.state.value.selectedSessionId)
        assertEquals("new-session", preferencesRepository.load().lastSessionId)
        assertEquals("new-session", sessionRepository.lastSessionId)
    }

    @Test
    fun `toggling filters updates filtered messages`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem() // initial

            // Toggle off Junie messages
            viewModel.onAction(ConversationAction.OnToggleFilter(FilterKind.Junie))
            // Single atomic emission: filter and filteredMessages update together (F8)
            val state = awaitItem()
            
            assertFalse(state.filter.showJunie)
            assertEquals(2, state.filteredMessages.size)
            assertTrue(state.filteredMessages.all { it.sender == Sender.Human })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing home path updates state and preferences`() = runConversationStateTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnHomePathChange("/new/home"))
        
        assertEquals("/new/home", viewModel.state.value.junieHomePath)
        assertEquals("/new/home", preferencesRepository.load().junieHomePath)
    }

    @Test
    fun `selectedSession is populated on startup when preferences contain a saved session`() = runConversationStateTest {
        preferencesRepository.save(AppPreferences(lastSessionId = "startup-session"))
        sessionRepository.sessionInfoProvider = { sessionId, _ ->
            if (sessionId == "startup-session") {
                SessionInfo(sessionId, "/path/$sessionId", 123L, createdAt = 1000L, workingDirectory = "/projects/test")
            } else null
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        // The bug was that selectedSession was null on startup even though messages loaded fine
        assertEquals("startup-session", state.selectedSessionId)
        kotlin.test.assertNotNull(state.selectedSession, "selectedSession must be populated on startup")
        assertEquals("startup-session", state.selectedSession!!.id)
        assertEquals(1000L, state.selectedSession!!.createdAt)
        assertEquals("/projects/test", state.selectedSession!!.workingDirectory)
    }

    @Test
    fun `toggling settings updates state`() = runConversationStateTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ConversationAction.OnToggleSettings)
            assertTrue(awaitItem().isSettingsOpen)
            
            viewModel.onAction(ConversationAction.OnToggleSettings)
            assertFalse(awaitItem().isSettingsOpen)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing theme mode updates state and persists preference`() = runConversationStateTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnThemeModeChange(ThemeMode.Dark))

        assertEquals(ThemeMode.Dark, viewModel.state.value.themeMode)
        assertEquals("Dark", preferencesRepository.load().themeMode)
    }

    @Test
    fun `theme mode is loaded from preferences on startup`() = runConversationStateTest {
        preferencesRepository.save(AppPreferences(themeMode = "Light"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(ThemeMode.Light, viewModel.state.value.themeMode)
    }

    @Test
    fun `invalid theme mode in preferences defaults to System`() = runConversationStateTest {
        preferencesRepository.save(AppPreferences(themeMode = "InvalidValue"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(ThemeMode.System, viewModel.state.value.themeMode)
    }

    @Test
    fun `selecting top level search result applies top level query to opened session and sets currentMatchIndex to 0`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnTopLevelSearchQueryChange("Hello"))
        advanceUntilIdle()

        val searchResult = TopLevelSessionSearchResult(
            session = TopLevelSessionIdentity(sessionId = "test-session", sessionPath = "test/path"),
            matchCount = 2
        )

        viewModel.onAction(ConversationAction.OnTopLevelSearchResultSelected(searchResult))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Hello", state.searchQuery)
        assertEquals(0, state.currentMatchIndex)
        assertEquals("test-session", state.selectedSessionId)
    }
}
