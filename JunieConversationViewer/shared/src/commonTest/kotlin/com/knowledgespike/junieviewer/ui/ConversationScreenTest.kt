package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
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

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher)
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

        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher)
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
}
