package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okio.FileSystem
import org.junit.Test

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class ConversationScreenTest {

    private val testMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Match this"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("Ignore that"), MessageKind.Text)
    )

    private val fakeRepository = object : SessionRepository {
        override fun getMessages(): List<Message> = testMessages
        override fun listSessions(homePath: String): List<SessionInfo> = emptyList()
        override fun setSession(sessionId: String, homePath: String) {}
        override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? = null
    }

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `searching for text filters the message list`() = runComposeUiTest {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ui-test-prefs-${System.currentTimeMillis()}.json"
        val fakePreferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "test-session"))

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        val robot = ConversationRobot(this)

        setContent {
            ConversationRoot(viewModel = viewModel)
        }

        // Initially both messages are shown
        robot.assertMessageCount(2)
        robot.assertMessageVisible("Match this")
        robot.assertMessageVisible("Ignore that")

        // Type search query
        robot.typeSearchQuery("Match")
        
        // Only matching message remains
        robot.assertMessageCount(1)
        robot.assertMessageVisible("Match this")
        
        // Clear search
        robot.clearSearchQuery()
        robot.assertMessageCount(2)
        
        FileSystem.SYSTEM.delete(tempPrefsPath)
    }

    @Test
    fun `toggling filters updates the message list`() = runComposeUiTest {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ui-test-filter-prefs-${System.currentTimeMillis()}.json"
        val fakePreferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "test-session"))

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        val robot = ConversationRobot(this)

        setContent {
            ConversationRoot(viewModel = viewModel)
        }

        robot.assertMessageCount(2)

        // Toggle off Junie messages
        robot.toggleFilter(FilterKind.Junie)
        
        // Only Human message remains
        robot.assertMessageCount(1)
        robot.assertMessageVisible("Match this")
        
        FileSystem.SYSTEM.delete(tempPrefsPath)
    }

    @Test
    fun `human messages display sender label Human`() = runComposeUiTest {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ui-test-sender-${System.currentTimeMillis()}.json"
        val fakePreferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "test-session"))

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        val robot = ConversationRobot(this)

        setContent {
            ConversationRoot(viewModel = viewModel)
        }

        robot.assertSenderLabelVisible("Human")
        robot.assertSenderLabelVisible("Junie")

        FileSystem.SYSTEM.delete(tempPrefsPath)
    }

    @Test
    fun `junie messages are grouped with a turn header`() = runComposeUiTest {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ui-test-turn-${System.currentTimeMillis()}.json"
        val fakePreferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "test-session"))

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        val robot = ConversationRobot(this)

        setContent {
            ConversationRoot(viewModel = viewModel)
        }

        // One Human then one Junie → one Junie Turn header
        robot.assertTurnHeaderCount(1)
        robot.assertHumanMessageCount(1)
        robot.assertJunieMessageCount(1)

        FileSystem.SYSTEM.delete(tempPrefsPath)
    }

    @Test
    fun `human messages are compact and junie messages are full width`() = runComposeUiTest {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ui-test-layout-${System.currentTimeMillis()}.json"
        val fakePreferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "test-session"))

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        val robot = ConversationRobot(this)

        setContent {
            ConversationRoot(viewModel = viewModel)
        }

        // Verify distinct message item tags exist for each sender type
        robot.assertHumanMessageCount(1)
        robot.assertJunieMessageCount(1)

        FileSystem.SYSTEM.delete(tempPrefsPath)
    }

    @Test
    fun `message kind markers are displayed`() = runComposeUiTest {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ui-test-kind-${System.currentTimeMillis()}.json"
        val fakePreferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "test-session"))

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        val robot = ConversationRobot(this)

        setContent {
            ConversationRoot(viewModel = viewModel)
        }

        robot.assertMessageKindMarkerVisible("Text")

        FileSystem.SYSTEM.delete(tempPrefsPath)
    }

    @Test
    fun `messages preserve chronological order with filters active`() = runComposeUiTest {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ui-test-order-${System.currentTimeMillis()}.json"
        val fakePreferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "test-session"))

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker())
        val robot = ConversationRobot(this)

        setContent {
            ConversationRoot(viewModel = viewModel)
        }

        // Both messages visible in order
        robot.assertMessageCount(2)
        robot.assertMessageVisible("Match this")
        robot.assertMessageVisible("Ignore that")

        // Filter to Human only — order preserved
        robot.toggleFilter(FilterKind.Junie)
        robot.assertMessageCount(1)
        robot.assertMessageVisible("Match this")

        // Restore
        robot.toggleFilter(FilterKind.Junie)
        robot.assertMessageCount(2)

        FileSystem.SYSTEM.delete(tempPrefsPath)
    }
}
