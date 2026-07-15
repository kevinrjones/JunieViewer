package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.*
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.domain.SessionInfo
import kotlin.test.Test

/**
 * Tests for Area 6 — Session Context, Empty, Loading, and Error States.
 * Covers session context header, no-session state, empty conversation,
 * loading indicator, recoverable error with retry, and state distinctness.
 */
class SessionStatesTest {

    private val testSession = SessionInfo(
        id = "session-260709-111457-1utg",
        path = "/home/user/.junie/sessions/session-260709-111457-1utg",
        lastModified = 1720785837000L,
        createdAt = 1720782237000L,
        workingDirectory = "/Users/kevinjones/projects/myapp"
    )

    private val testMessages = listOf(
        Message(id = "1", sender = Sender.Human, content = MessageContent.Text("Hello"), kind = MessageKind.Text),
        Message(id = "2", sender = Sender.Junie, content = MessageContent.Text("Hi there!"), kind = MessageKind.Text),
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `session context footer shows session id when selected`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = testMessages,
                    selectedSessionId = testSession.id,
                    selectedSession = testSession
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertSessionContextVisible(testSession.id)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `session context footer shows timestamp and project`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = testMessages,
                    selectedSessionId = testSession.id,
                    selectedSession = testSession
                ),
                onAction = {}
            )
        }
        // Created timestamp should be shown since createdAt is available
        onAllNodesWithText("Created:", substring = true)
            .onFirst()
            .assertExists()
        // Working directory should be shown
        onAllNodesWithText("Project: /Users/kevinjones/projects/myapp", substring = true)
            .onFirst()
            .assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `session context footer shows last modified when createdAt is null`() = runComposeUiTest {
        val sessionNoCreated = testSession.copy(createdAt = null)
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = testMessages,
                    selectedSessionId = sessionNoCreated.id,
                    selectedSession = sessionNoCreated
                ),
                onAction = {}
            )
        }
        onAllNodesWithText("Last modified:", substring = true)
            .onFirst()
            .assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `no session selected state appears when no session is selected`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(selectedSessionId = null),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertNoSessionStateVisible()
        onAllNodesWithText("No Session selected")
            .onFirst()
            .assertExists()
        onAllNodesWithText("Choose a Session to view its Conversation.")
            .onFirst()
            .assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `empty conversation state appears when session has zero messages`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = emptyList(),
                    filteredMessages = emptyList(),
                    selectedSessionId = testSession.id,
                    selectedSession = testSession
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertEmptyConversationStateVisible()
        onAllNodesWithText("This Session has no Messages")
            .onFirst()
            .assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `loading state shows loading indicator`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    isLoading = true,
                    selectedSessionId = testSession.id,
                    selectedSession = testSession
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertLoadingVisible()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `error state shows error message and retry button`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    errorMessage = "Could not load this Conversation. Check that the Session still exists and try again.",
                    selectedSessionId = testSession.id,
                    selectedSession = testSession
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertErrorVisible()
        onAllNodesWithText("Could not load this Conversation", substring = true)
            .onFirst()
            .assertExists()
        onNodeWithTag("retry_button")
            .assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `no results state is distinct from empty conversation`() = runComposeUiTest {
        // Empty conversation: session selected, messages empty, no search
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = emptyList(),
                    filteredMessages = emptyList(),
                    selectedSessionId = testSession.id,
                    selectedSession = testSession,
                    searchQuery = ""
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertEmptyConversationStateVisible()
        // no_results should NOT be shown
        onNodeWithTag("no_results").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `no results state appears when search filters yield zero from non-empty messages`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = emptyList(),
                    selectedSessionId = testSession.id,
                    selectedSession = testSession,
                    searchQuery = "nonexistent"
                ),
                onAction = {}
            )
        }
        onNodeWithTag("no_results").assertIsDisplayed()
        onNodeWithTag("empty_conversation").assertDoesNotExist()
    }
}
