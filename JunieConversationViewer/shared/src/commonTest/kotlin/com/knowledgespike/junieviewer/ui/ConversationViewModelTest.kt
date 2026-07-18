package com.knowledgespike.junieviewer.ui

import app.cash.turbine.test
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okio.FileSystem
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("Hello"), MessageKind.Text),
        Message("3", Sender.Human, MessageContent.Text("Other"), MessageKind.Text),
        Message("4", Sender.Junie, MessageContent.Text("Other"), MessageKind.Text)
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
            SessionInfo(sessionId, "/path/$sessionId", 123L, createdAt = 1000L, workingDirectory = "/projects/test")
    }

    private lateinit var tempPrefsPath: okio.Path
    private lateinit var fakePreferencesRepository: PreferencesRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "test-prefs-${System.currentTimeMillis()}.json"
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

    @Test
    fun `initial state is correctly loaded from preferences`() = runTest {
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "saved-session", junieHomePath = "/custom/path"))
        
        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("/custom/path", state.junieHomePath)
            assertEquals("saved-session", state.selectedSessionId)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search query updates filtered messages`() = runTest {
        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        // Ensure a session is selected and messages are loaded
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.state.test {
            val initialState = awaitItem()
            println("INITIAL SIZE: ${initialState.filteredMessages.size}")
            assertEquals(4, initialState.filteredMessages.size)

            viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))
            // First item: searchQuery changed
            val intermediateState = awaitItem()
            // Second item: filteredMessages updated
            val finalState = awaitItem()
            
            assertEquals("Hello", finalState.searchQuery)
            assertEquals(2, finalState.filteredMessages.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggling session picker updates state and loads sessions`() = runTest {
        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
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
    fun `selecting session updates preference and loads messages`() = runTest {
        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        advanceUntilIdle()

        val newSession = SessionInfo("new-session", "/path/new-session", 456L)
        viewModel.onAction(ConversationAction.OnSessionSelected(newSession))
        advanceUntilIdle()

        assertEquals("new-session", viewModel.state.value.selectedSessionId)
        assertEquals("new-session", fakePreferencesRepository.load().lastSessionId)
        assertEquals("new-session", fakeRepository.lastSessionId)
    }

    @Test
    fun `toggling filters updates filtered messages`() = runTest {
        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem() // initial

            // Toggle off Junie messages
            viewModel.onAction(ConversationAction.OnToggleFilter(FilterKind.Junie))
            // First item: filter changed
            awaitItem()
            // Second item: filteredMessages updated
            val state = awaitItem()
            
            assertFalse(state.filter.showJunie)
            assertEquals(2, state.filteredMessages.size)
            assertTrue(state.filteredMessages.all { it.sender == Sender.Human })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing home path updates state and preferences`() = runTest {
        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnHomePathChange("/new/home"))
        
        assertEquals("/new/home", viewModel.state.value.junieHomePath)
        assertEquals("/new/home", fakePreferencesRepository.load().junieHomePath)
    }

    @Test
    fun `selectedSession is populated on startup when preferences contain a saved session`() = runTest {
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "startup-session"))

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
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
    fun `toggling settings updates state`() = runTest {
        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
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
    fun `changing theme mode updates state and persists preference`() = runTest {
        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnThemeModeChange(ThemeMode.Dark))

        assertEquals(ThemeMode.Dark, viewModel.state.value.themeMode)
        assertEquals("Dark", fakePreferencesRepository.load().themeMode)
    }

    @Test
    fun `theme mode is loaded from preferences on startup`() = runTest {
        fakePreferencesRepository.save(AppPreferences(themeMode = "Light"))

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        advanceUntilIdle()

        assertEquals(ThemeMode.Light, viewModel.state.value.themeMode)
    }

    @Test
    fun `invalid theme mode in preferences defaults to System`() = runTest {
        fakePreferencesRepository.save(AppPreferences(themeMode = "InvalidValue"))

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        advanceUntilIdle()

        assertEquals(ThemeMode.System, viewModel.state.value.themeMode)
    }
}
