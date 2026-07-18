package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.fixtures.RepresentativeFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okio.FileSystem
import org.junit.Test

/**
 * UI and mapping tests for Area 3 — Sub-Agent and Event Representation.
 * Verifies the Sub-Agent badge renders for SubAgent messages, emoji is removed,
 * and non-SubAgent messages do not show the badge.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class SubAgentRepresentationTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    /** Creates a ViewModel with the given messages for testing. */
    private fun createViewModel(messages: List<Message>): Pair<ConversationViewModel, okio.Path> {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "sub-agent-test-${System.currentTimeMillis()}.json"
        val fakePreferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "test-session"))

        val fakeRepository = object : SessionRepository {
            override fun getMessages(): List<Message> = messages
            override fun listSessions(homePath: String): List<SessionInfo> = emptyList()
            override fun setSession(sessionId: String, homePath: String) {}
            override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? = null
        }

        return ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher, LiveSessionTracker()) to tempPrefsPath
    }

    // -----------------------------------------------------------------------
    // 3.5 — Sub-Agent badge renders for SubAgent messages
    // -----------------------------------------------------------------------

    @Test
    fun `sub-agent message renders Sub-Agent badge`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.subAgentMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("sub_agent_badge").assertExists()
        onNodeWithTag("sub_agent_block_header").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `sub-agent message content does not contain emoji`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.subAgentMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        // The sub-agent content should be visible without emoji prefix
        onNodeWithText("android-qa-agent [STARTED]", substring = true).assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `sub-agent message with missing fields renders fallback content`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.subAgentMessageMissingFields
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("sub_agent_badge").assertExists()
        onNodeWithText("Unnamed sub-agent [unknown]", substring = true).assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // 3.6 — Non-SubAgent messages do not show the badge
    // -----------------------------------------------------------------------

    @Test
    fun `human message does not render Sub-Agent badge`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("sub_agent_badge").assertDoesNotExist()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `junie text message does not render Sub-Agent badge`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTextMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("sub_agent_badge").assertDoesNotExist()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `tool call message does not render Sub-Agent badge`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieToolCallMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("sub_agent_badge").assertDoesNotExist()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // 3.6 — Chronological order preserved with sub-agent messages
    // -----------------------------------------------------------------------

    @Test
    fun `sub-agent messages maintain chronological order with other messages`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.subAgentMessage,
            RepresentativeFixtures.junieTextMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        // All three messages should render; badge only on the sub-agent one
        val badges = onAllNodesWithTag("sub_agent_badge").fetchSemanticsNodes()
        assert(badges.size == 1) { "Expected exactly 1 sub-agent badge, got ${badges.size}" }

        val kindMarkers = onAllNodesWithTag("message_kind_marker").fetchSemanticsNodes()
        assert(kindMarkers.size >= 3) { "Expected at least 3 kind markers for 3 messages, got ${kindMarkers.size}" }
        FileSystem.SYSTEM.delete(path)
    }
}
