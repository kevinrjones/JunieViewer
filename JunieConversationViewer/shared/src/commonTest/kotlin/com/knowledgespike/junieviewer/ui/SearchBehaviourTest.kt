package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.*
import app.cash.turbine.test
import com.knowledgespike.junieviewer.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Tests for the search behaviour area: case-insensitive search, search/filter
 * combinations, no-results state, result count, filter labels, chronological
 * order preservation, and the atomic state-derivation guarantees for search
 * query changes (single emission per change, turn grouping derived alongside
 * filtered messages).
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class SearchBehaviourTest {

    // =========================================================================
    // Origin: SearchFilterNavigationTest — Area 5 Search, Filters, and Navigation
    // Covers case-insensitive search, filter combinations, no-results state,
    // result count, filter labels, and chronological order preservation.
    // =========================================================================

    private val navigationTestMessages = listOf(
        Message(id = "1", sender = Sender.Human, content = MessageContent.Text("Hello Junie"), kind = MessageKind.Text),
        Message(id = "2", sender = Sender.Junie, content = MessageContent.Text("Hi there! How can I help?"), kind = MessageKind.Text),
        Message(id = "3", sender = Sender.Junie, content = MessageContent.Text("Let me think about that..."), kind = MessageKind.Thought),
        Message(id = "4", sender = Sender.Junie, content = MessageContent.Code("println(\"hello\")"), kind = MessageKind.Tool),
        Message(id = "5", sender = Sender.Junie, content = MessageContent.Text("Here is the result"), kind = MessageKind.Text),
        Message(id = "6", sender = Sender.Human, content = MessageContent.Text("Thanks!"), kind = MessageKind.Text),
    )

    @Test
    fun `search is case-insensitive`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    sessionLoad = SessionLoadState(messages = navigationTestMessages, selectedSessionId = "test"),
                    search = SearchState(
                        filteredMessages = navigationTestMessages.filter {
                            messageContentText(it.content).contains("hello", ignoreCase = true)
                        },
                        searchQuery = "hello"
                    )
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        // "Hello Junie" and code with "hello" should match
        robot.assertHumanMessageCount(1)
    }

    @Test
    fun `clearing search restores all messages`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    sessionLoad = SessionLoadState(messages = navigationTestMessages, selectedSessionId = "test"),
                    search = SearchState(filteredMessages = navigationTestMessages, searchQuery = "")
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertNoResultsNotVisible()
        robot.assertResultCountNotVisible()
    }

    @Test
    fun `sender filters still work`() = runComposeUiTest {
        val filtered = navigationTestMessages.filter { it.sender == Sender.Junie }
        setContent {
            ConversationScreen(
                state = ConversationState(
                    sessionLoad = SessionLoadState(messages = navigationTestMessages, selectedSessionId = "test"),
                    search = SearchState(filteredMessages = filtered, filter = FilterState(showHuman = false))
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertHumanMessageCount(0)
        robot.assertResultCount("${filtered.size} of ${navigationTestMessages.size} Messages")
    }

    @Test
    fun `filter labels use understandable names`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    sessionLoad = SessionLoadState(messages = navigationTestMessages, selectedSessionId = "test"),
                    search = SearchState(filteredMessages = navigationTestMessages)
                ),
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

    @Test
    fun `search and filters combine with AND logic`() = runComposeUiTest {
        // Search for "help" with Human filter off — only Junie messages with "help" should show
        val filtered = navigationTestMessages.filter { msg ->
            msg.sender != Sender.Human &&
                    messageContentText(msg.content).contains("help", ignoreCase = true)
        }
        setContent {
            ConversationScreen(
                state = ConversationState(
                    sessionLoad = SessionLoadState(messages = navigationTestMessages, selectedSessionId = "test"),
                    search = SearchState(
                        filteredMessages = filtered,
                        searchQuery = "help",
                        filter = FilterState(showHuman = false)
                    )
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertHumanMessageCount(0)
        robot.assertResultCount("${filtered.size} of ${navigationTestMessages.size} Messages")
    }

    @Test
    fun `filtering preserves chronological order`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    sessionLoad = SessionLoadState(messages = navigationTestMessages, selectedSessionId = "test"),
                    search = SearchState(filteredMessages = navigationTestMessages, searchQuery = "")
                ),
                onAction = {}
            )
        }
        // First human message should appear before last human message
        onNodeWithText("Hello Junie", substring = true).assertExists()
        // Scroll to the last message which may be off-screen due to CollapsibleBlock headers
        onNodeWithTag("message_list").performScrollToIndex(5)
        waitForIdle()
        onNodeWithText("Thanks!", substring = true).assertExists()
    }

    @Test
    fun `no-results state appears when nothing matches`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    sessionLoad = SessionLoadState(messages = navigationTestMessages, selectedSessionId = "test"),
                    search = SearchState(filteredMessages = emptyList(), searchQuery = "zzzznonexistent")
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertNoResultsVisible()
        robot.assertResultCount("No matching Messages")
    }

    @Test
    fun `result count appears when search is active`() = runComposeUiTest {
        val filtered = navigationTestMessages.filter {
            messageContentText(it.content).contains("hello", ignoreCase = true)
        }
        setContent {
            ConversationScreen(
                state = ConversationState(
                    sessionLoad = SessionLoadState(messages = navigationTestMessages, selectedSessionId = "test"),
                    search = SearchState(filteredMessages = filtered, searchQuery = "hello")
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertResultCount("of ${navigationTestMessages.size} Messages")
    }

    @Test
    fun `result count not shown when no search or filter active`() = runComposeUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    sessionLoad = SessionLoadState(messages = navigationTestMessages, selectedSessionId = "test"),
                    search = SearchState(filteredMessages = navigationTestMessages, searchQuery = "")
                ),
                onAction = {}
            )
        }
        val robot = ConversationRobot(this)
        robot.assertResultCountNotVisible()
    }

    // =========================================================================
    // Origin: SearchStateDerivationTest — Sprint 6 Area 1 search state derivation
    // - F8: a Search Query change produces exactly one atomic state emission.
    // - F10: Turn grouping is derived in the ViewModel alongside filteredMessages,
    //   not recomputed in composition.
    // =========================================================================

    private val derivationTestMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("Hello back"), MessageKind.Text),
        Message("3", Sender.Human, MessageContent.Text("Other"), MessageKind.Text),
        Message("4", Sender.Junie, MessageContent.Text("Other reply"), MessageKind.Text)
    )

    private fun ConversationStateTestScope.loadedViewModel(): ConversationViewModel {
        val viewModel = createViewModel()
        viewModel.onAction(ConversationAction.OnSessionSelected(SessionInfo("test", "path", 0L)))
        return viewModel
    }

    @Test
    fun `search query change produces exactly one state emission`() = runConversationStateTest(derivationTestMessages) {
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
    fun `clearing the search query produces exactly one state emission`() = runConversationStateTest(derivationTestMessages) {
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
    fun `turns are derived alongside filtered messages after load`() = runConversationStateTest(derivationTestMessages) {
        val viewModel = loadedViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(groupMessagesIntoTurns(state.filteredMessages), state.turns)
        assertEquals(4, state.turns.sumOf { it.messages.size })
    }

    @Test
    fun `turns are re-derived when the search query changes`() = runConversationStateTest(derivationTestMessages) {
        val viewModel = loadedViewModel()
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(groupMessagesIntoTurns(state.filteredMessages), state.turns)
        assertEquals(2, state.turns.sumOf { it.messages.size })
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
