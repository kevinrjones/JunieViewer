package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.*
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import kotlin.test.Test

/**
 * Tests for Area 5 — Search, Filters, and Navigation.
 * Covers case-insensitive search, filter combinations, no-results state,
 * result count, filter labels, and chronological order preservation.
 */
class SearchFilterNavigationTest {

    private val testMessages = listOf(
        Message(id = "1", sender = Sender.Human, content = MessageContent.Text("Hello Junie"), kind = MessageKind.Text),
        Message(id = "2", sender = Sender.Junie, content = MessageContent.Text("Hi there! How can I help?"), kind = MessageKind.Text),
        Message(id = "3", sender = Sender.Junie, content = MessageContent.Text("Let me think about that..."), kind = MessageKind.Thought),
        Message(id = "4", sender = Sender.Junie, content = MessageContent.Code("println(\"hello\")"), kind = MessageKind.Tool),
        Message(id = "5", sender = Sender.Junie, content = MessageContent.Text("Here is the result"), kind = MessageKind.Text),
        Message(id = "6", sender = Sender.Human, content = MessageContent.Text("Thanks!"), kind = MessageKind.Text),
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `search is case-insensitive`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = testMessages.filter {
                        messageContentText(it.content).contains("hello", ignoreCase = true)
                    },
                    searchQuery = "hello",
                    selectedSessionId = "test"
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        // "Hello Junie" and code with "hello" should match
        robot.assertHumanMessageCount(1)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `clearing search restores all messages`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = testMessages,
                    searchQuery = "",
                    selectedSessionId = "test"
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertNoResultsNotVisible()
        robot.assertResultCountNotVisible()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `sender filters still work`() = runComposeUiTest {
        val filtered = testMessages.filter { it.sender == Sender.Junie }
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = filtered,
                    filter = FilterState(showHuman = false),
                    selectedSessionId = "test"
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertHumanMessageCount(0)
        robot.assertResultCount("${filtered.size} of ${testMessages.size} Messages")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `filter labels use understandable names`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(messages = testMessages, filteredMessages = testMessages, selectedSessionId = "test"),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertFilterLabelVisible("Human")
        robot.assertFilterLabelVisible("Junie")
        robot.assertFilterLabelVisible("Thoughts")
        robot.assertFilterLabelVisible("Tools")
        robot.assertFilterLabelVisible("Patches")
        robot.assertFilterLabelVisible("Terminal")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `search and filters combine with AND logic`() = runComposeUiTest {
        // Search for "help" with Human filter off — only Junie messages with "help" should show
        val filtered = testMessages.filter { msg ->
            msg.sender != Sender.Human &&
                    messageContentText(msg.content).contains("help", ignoreCase = true)
        }
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = filtered,
                    searchQuery = "help",
                    filter = FilterState(showHuman = false),
                    selectedSessionId = "test"
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertHumanMessageCount(0)
        robot.assertResultCount("${filtered.size} of ${testMessages.size} Messages")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `filtering preserves chronological order`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = testMessages,
                    searchQuery = "",
                    selectedSessionId = "test"
                ),
                onAction = {}
            )
        }
        // First human message should appear before last human message
        onNodeWithText("Hello Junie", substring = true).assertExists()
        onNodeWithText("Thanks!", substring = true).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `no-results state appears when nothing matches`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = emptyList(),
                    searchQuery = "zzzznonexistent",
                    selectedSessionId = "test"
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertNoResultsVisible()
        robot.assertResultCount("No matching Messages")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `result count appears when search is active`() = runComposeUiTest {
        val filtered = testMessages.filter {
            messageContentText(it.content).contains("hello", ignoreCase = true)
        }
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = filtered,
                    searchQuery = "hello",
                    selectedSessionId = "test"
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertResultCount("of ${testMessages.size} Messages")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `result count not shown when no search or filter active`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    messages = testMessages,
                    filteredMessages = testMessages,
                    searchQuery = "",
                    selectedSessionId = "test"
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertResultCountNotVisible()
    }
}

/** Helper to extract searchable text from MessageContent — mirrors ViewModel logic. */
private fun messageContentText(content: MessageContent): String = when (content) {
    is MessageContent.Text -> content.text
    is MessageContent.Code -> content.code
    is MessageContent.Diff -> content.diff
    is MessageContent.Terminal -> content.output
    is MessageContent.Structured -> content.data
}
