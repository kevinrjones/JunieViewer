package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.fixtures.RepresentativeFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test

/**
 * Area 7 (Accessibility) and Area 8 (Automated Testing) coverage.
 * Tests semantic labels, match navigation, long responses, and tag coverage.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class AccessibilityAndArea8Test {

    // Three searchable messages for match navigation tests
    private val searchableMessages = listOf(
        Message("s1", Sender.Human, MessageContent.Text("alpha beta"), MessageKind.Text, 1000L),
        Message("s2", Sender.Junie, MessageContent.Text("alpha gamma"), MessageKind.Text, 1001L),
        Message("s3", Sender.Junie, MessageContent.Text("alpha delta"), MessageKind.Text, 1002L),
        Message("s4", Sender.Human, MessageContent.Text("no match here"), MessageKind.Text, 1003L)
    )

    // -- Area 7.2: Semantic labels / content descriptions --

    @Test
    fun `open session button has content description`() = runConversationUiTest(searchableMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        robot.assertContentDescriptionExists("Open Session")
    }

    @Test
    fun `toolbar buttons have content descriptions`() = runConversationUiTest(searchableMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        robot.assertContentDescriptionExists("Refresh")
        robot.assertContentDescriptionExists("Copy")
        robot.assertContentDescriptionExists("Auto-Refresh")
        robot.assertContentDescriptionExists("Sort Order")
        robot.assertContentDescriptionExists("Collapse All")
        robot.assertContentDescriptionExists("Show All")
    }

    @Test
    fun `no session state has content description`() = runConversationUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(selectedSessionId = null),
                onAction = {}
            )
        }

        robot.assertNoSessionStateVisible()
        robot.assertContentDescriptionExists("No Session selected")
    }

    @Test
    fun `loading state has content description`() = runConversationUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(isLoading = true, selectedSessionId = "s1"),
                onAction = {}
            )
        }

        robot.assertLoadingVisible()
        robot.assertContentDescriptionExists("Loading Conversation")
    }

    @Test
    fun `error state has content description`() = runConversationUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    errorMessage = "Something went wrong",
                    selectedSessionId = "s1"
                ),
                onAction = {}
            )
        }

        robot.assertErrorVisible()
        robot.assertContentDescriptionExists("Error loading Conversation")
        robot.assertContentDescriptionExists("Retry loading")
    }

    @Test
    fun `empty conversation state has content description`() = runConversationUiTest {
        setContent {
            ConversationScreen(
                state = ConversationState(
                    selectedSessionId = "s1",
                    messages = emptyList()
                ),
                onAction = {}
            )
        }

        robot.assertEmptyConversationStateVisible()
        robot.assertContentDescriptionExists("Empty Conversation")
    }

    // -- Area 8.5: Human/Junie rendering --

    @Test
    fun `sender markers show Human and Junie`() = runConversationUiTest(searchableMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        robot.assertSenderMarkerVisible("Human")
        robot.assertSenderMarkerVisible("Junie")
    }

    @Test
    fun `message kind markers are visible for first visible fixture kinds`() = runConversationUiTest {
        // Only test kinds that are visible without scrolling (LazyColumn virtualises off-screen items)
        sessionRepository.messagesToReturn = RepresentativeFixtures.allMessageKinds
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // The first few items are always visible: Human text, then Junie turn with Thought, Tool, etc.
        robot.assertMessageOfKindVisible("Text")
        robot.assertMessageOfKindVisible("Thought")
        robot.assertMessageOfKindVisible("Tool")
    }

    @Test
    fun `junie messages are grouped with turn headers`() = runConversationUiTest(searchableMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // searchableMessages has Human, Junie, Junie, Human — so 1 Junie turn header
        robot.assertTagExists("turn_header")
    }

    // -- Area 8: Match navigation --

    @Test
    fun `match navigation buttons appear when multiple matches exist`() = runConversationUiTest(searchableMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Search for "alpha" — 3 matches
        robot.typeSearchQuery("alpha")
        robot.assertMatchIndicator("1 / 3")

        // Navigate forward
        robot.goToNextMatch()
        robot.assertMatchIndicator("2 / 3")

        robot.goToNextMatch()
        robot.assertMatchIndicator("3 / 3")

        // Wrap around
        robot.goToNextMatch()
        robot.assertMatchIndicator("1 / 3")

        // Navigate backward wraps
        robot.goToPreviousMatch()
        robot.assertMatchIndicator("3 / 3")
    }

    @Test
    fun `match navigation buttons have content descriptions`() = runConversationUiTest(searchableMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        robot.typeSearchQuery("alpha")
        robot.assertContentDescriptionExists("Previous match")
        robot.assertContentDescriptionExists("Next match")
    }

    @Test
    fun `clear search button has content description`() = runConversationUiTest(searchableMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        robot.typeSearchQuery("alpha")
        robot.assertContentDescriptionExists("Clear search")
    }

    // -- Area 8.4: Semantic tag coverage --

    @Test
    fun `important controls have stable test tags`() = runConversationUiTest(searchableMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("search_field").assertExists()
        onNodeWithTag("toolbar_open_session").assertExists()
        onNodeWithTag("conversation_toolbar").assertExists()
        onNodeWithTag("filter_human").assertExists()
        onNodeWithTag("filter_junie").assertExists()
        onNodeWithTag("filter_thought").assertExists()
        onNodeWithTag("filter_tool").assertExists()
        onNodeWithTag("filter_patch").assertExists()
        onNodeWithTag("filter_terminal").assertExists()
        onNodeWithTag("message_list").assertExists()
    }

    // -- Area 8.10: Long Junie response --

    @Test
    fun `long junie response renders without crashing and preserves turn grouping`() = runConversationUiTest {
        val longText = "Line of text. ".repeat(500)
        val longMessages = listOf(
            Message("l1", Sender.Human, MessageContent.Text("Start"), MessageKind.Text, 1L),
            Message("l2", Sender.Junie, MessageContent.Text(longText), MessageKind.Text, 2L),
            Message("l3", Sender.Junie, MessageContent.Text("Follow-up"), MessageKind.Text, 3L)
        )
        sessionRepository.messagesToReturn = longMessages
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // LazyColumn virtualises — not all items rendered. Verify it doesn't crash and turn header exists.
        robot.assertTagExists("turn_header")
        robot.assertSenderMarkerVisible("Human")
    }

    // -- Area 7.5: Non-colour-only status indicators --

    @Test
    fun `error and warning messages have text labels not just colour`() = runConversationUiTest {
        val messages = listOf(
            RepresentativeFixtures.junieErrorMessage,
            RepresentativeFixtures.junieWarningMessage
        )
        sessionRepository.messagesToReturn = messages
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Kind markers include emoji+text, not colour alone
        robot.assertMessageOfKindVisible("Error")
        robot.assertMessageOfKindVisible("Warning")
    }

    @Test
    fun `unsupported event has text label and card`() = runConversationUiTest {
        val messages = listOf(RepresentativeFixtures.malformedContentMessage)
        sessionRepository.messagesToReturn = messages
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("unsupported_event_card").assertExists()
    }

    // -- Area 8.4: Themed component tests --

    @Test
    fun `human and junie messages render under light theme`() = runConversationUiTest(searchableMessages) {
        setContent {
            com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme(
                themeMode = com.knowledgespike.junieviewer.ui.theme.ThemeMode.Light
            ) {
                ConversationScreen(
                    state = ConversationState(
                        selectedSessionId = "s1",
                        messages = searchableMessages,
                        filteredMessages = searchableMessages
                    ),
                    onAction = {}
                )
            }
        }

        onAllNodesWithTag("message_item_human").onFirst().assertExists()
        onAllNodesWithTag("message_item_junie").onFirst().assertExists()
    }

    @Test
    fun `human and junie messages render under dark theme`() = runConversationUiTest(searchableMessages) {
        setContent {
            com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme(
                themeMode = com.knowledgespike.junieviewer.ui.theme.ThemeMode.Dark
            ) {
                ConversationScreen(
                    state = ConversationState(
                        selectedSessionId = "s1",
                        messages = searchableMessages,
                        filteredMessages = searchableMessages
                    ),
                    onAction = {}
                )
            }
        }

        onAllNodesWithTag("message_item_human").onFirst().assertExists()
        onAllNodesWithTag("message_item_junie").onFirst().assertExists()
    }

    @Test
    fun `rich content blocks render under dark theme without crashing`() = runConversationUiTest {
        setContent {
            com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme(
                themeMode = com.knowledgespike.junieviewer.ui.theme.ThemeMode.Dark
            ) {
                ConversationScreen(
                    state = ConversationState(
                        selectedSessionId = "s1",
                        messages = RepresentativeFixtures.allMessageKinds,
                        filteredMessages = RepresentativeFixtures.allMessageKinds
                    ),
                    onAction = {}
                )
            }
        }

        onNodeWithTag("message_item_human").assertExists()
        onNodeWithTag("turn_header").assertExists()
    }

    @Test
    fun `state surfaces render under dark theme`() = runConversationUiTest {
        setContent {
            com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme(
                themeMode = com.knowledgespike.junieviewer.ui.theme.ThemeMode.Dark
            ) {
                ConversationScreen(
                    state = ConversationState(isLoading = true, selectedSessionId = "s1"),
                    onAction = {}
                )
            }
        }

        onNodeWithTag("loading_indicator").assertExists()
    }

    @Test
    fun `footer renders with session metadata`() = runConversationUiTest(searchableMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        robot.assertTagExists("session_context_footer")
    }
}
