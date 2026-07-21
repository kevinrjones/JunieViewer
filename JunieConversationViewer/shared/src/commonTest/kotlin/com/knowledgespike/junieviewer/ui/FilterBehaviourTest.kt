package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.fixtures.RepresentativeFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Tests for Area 4 — Filter Coverage and Top Controls.
 * Verifies ViewModel filtering behaviour for grouped kinds, AlwaysShow kinds,
 * and the Human/Junie Text special case. Also verifies filter bar UI labels.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
class FilterBehaviourTest {

    /** Messages covering all filter groups for behaviour testing */
    private val filterTestMessages = listOf(
        RepresentativeFixtures.humanTextMessage,        // Text, Human sender → Human filter
        RepresentativeFixtures.junieTextMessage,         // Text, Junie sender → Junie filter
        RepresentativeFixtures.junieMarkdownMessage,     // Markdown → Junie filter
        RepresentativeFixtures.junieThoughtMessage,      // Thought → Thoughts filter
        RepresentativeFixtures.junieToolCallMessage,     // Tool → Tools filter
        RepresentativeFixtures.junieStructuredOutputMessage, // StructuredOutput → Tools filter
        RepresentativeFixtures.junieMcpMessage,          // Mcp → Tools filter
        RepresentativeFixtures.subAgentMessage,          // SubAgent → Tools filter
        RepresentativeFixtures.junieDiffMessage,         // Patch → Patches filter
        RepresentativeFixtures.junieTerminalMessage,     // Terminal → Terminal filter
        RepresentativeFixtures.junieTestRunMessage,      // TestRun → Terminal filter
        RepresentativeFixtures.junieErrorMessage,        // Error → AlwaysShow
        RepresentativeFixtures.junieWarningMessage,      // Warning → AlwaysShow
        RepresentativeFixtures.questionMessage,          // Question → AlwaysShow
        RepresentativeFixtures.choiceMessage,            // Choice → AlwaysShow
        RepresentativeFixtures.systemMessage,            // SystemMessage → AlwaysShow
        RepresentativeFixtures.cancelledMessage,         // Cancelled → AlwaysShow
        RepresentativeFixtures.statusMessage,            // Status → AlwaysShow
        RepresentativeFixtures.malformedContentMessage   // Unsupported → AlwaysShow
    )

    // -----------------------------------------------------------------------
    // Tools filter hides Tool, StructuredOutput, Mcp, and SubAgent
    // -----------------------------------------------------------------------

    @Test
    fun `toggling off Tools hides Tool, StructuredOutput, Mcp, and SubAgent messages`() = runConversationStateTest(filterTestMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onAction(ConversationAction.OnToggleFilter(FilterKind.Tool))
        advanceUntilIdle()

        val filtered = vm.state.value.filteredMessages
        val hiddenKinds = setOf(MessageKind.Tool, MessageKind.StructuredOutput, MessageKind.Mcp, MessageKind.SubAgent)
        expectThat(filtered.none { it.kind in hiddenKinds }).isTrue()
    }

    // -----------------------------------------------------------------------
    // Terminal filter hides Terminal and TestRun
    // -----------------------------------------------------------------------

    @Test
    fun `toggling off Terminal hides Terminal and TestRun messages`() = runConversationStateTest(filterTestMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onAction(ConversationAction.OnToggleFilter(FilterKind.Terminal))
        advanceUntilIdle()

        val filtered = vm.state.value.filteredMessages
        expectThat(filtered.none { it.kind == MessageKind.Terminal || it.kind == MessageKind.TestRun }).isTrue()
    }

    // -----------------------------------------------------------------------
    // Junie filter hides Junie Text and Markdown but not Human Text
    // -----------------------------------------------------------------------

    @Test
    fun `toggling off Junie hides Junie text and markdown but keeps Human text`() = runConversationStateTest(filterTestMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onAction(ConversationAction.OnToggleFilter(FilterKind.Junie))
        advanceUntilIdle()

        val filtered = vm.state.value.filteredMessages
        // Human text should still be present
        expectThat(filtered.any { it.sender == Sender.Human && it.kind == MessageKind.Text }).isTrue()
        // Junie text and markdown should be hidden
        expectThat(filtered.none { it.sender == Sender.Junie && it.kind == MessageKind.Text }).isTrue()
        expectThat(filtered.none { it.kind == MessageKind.Markdown }).isTrue()
    }

    // -----------------------------------------------------------------------
    // Human filter hides Human Text but not Junie Text
    // -----------------------------------------------------------------------

    @Test
    fun `toggling off Human hides Human text but keeps Junie text`() = runConversationStateTest(filterTestMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onAction(ConversationAction.OnToggleFilter(FilterKind.Human))
        advanceUntilIdle()

        val filtered = vm.state.value.filteredMessages
        expectThat(filtered.none { it.sender == Sender.Human && it.kind == MessageKind.Text }).isTrue()
        expectThat(filtered.any { it.sender == Sender.Junie && it.kind == MessageKind.Text }).isTrue()
    }

    // -----------------------------------------------------------------------
    // AlwaysShow messages remain visible when all toggleable filters are off
    // -----------------------------------------------------------------------

    @Test
    fun `AlwaysShow messages remain visible when all filters are toggled off`() = runConversationStateTest(filterTestMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        val vm = createViewModel()
        advanceUntilIdle()

        // Toggle off every filter
        FilterKind.entries.forEach { kind ->
            vm.onAction(ConversationAction.OnToggleFilter(kind))
        }
        advanceUntilIdle()

        val filtered = vm.state.value.filteredMessages
        val alwaysShowKinds = setOf(
            MessageKind.Error, MessageKind.Warning, MessageKind.Unsupported,
            MessageKind.Question, MessageKind.Choice, MessageKind.SystemMessage,
            MessageKind.Cancelled, MessageKind.Status
        )
        // All AlwaysShow messages should still be present
        expectThat(filtered.filter { it.kind in alwaysShowKinds }).hasSize(8)
        // Only AlwaysShow messages should remain
        expectThat(filtered.all { it.kind in alwaysShowKinds }).isTrue()
    }

    // -----------------------------------------------------------------------
    // UI — Filter bar renders six chips with canonical labels
    // -----------------------------------------------------------------------

    @Test
    fun `filter bar renders six filter chips with canonical labels`() = runConversationUiTest {
        sessionRepository.messagesToReturn = filterTestMessages
        sessionRepository.sessionInfoProvider = { sessionId, _ -> SessionInfo(sessionId, "/path/$sessionId", 123L) }
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Verify all six filter tags exist
        onNodeWithTag("filter_human").assertExists()
        onNodeWithTag("filter_junie").assertExists()
        onNodeWithTag("filter_thought").assertExists()
        onNodeWithTag("filter_tool").assertExists()
        onNodeWithTag("filter_patch").assertExists()
        onNodeWithTag("filter_terminal").assertExists()

        // Verify canonical label text on filter chips (scoped by tag to avoid sender marker ambiguity)
        onNode(hasTestTag("filter_human") and hasText("Human")).assertExists()
        onNode(hasTestTag("filter_junie") and hasText("Junie")).assertExists()
        onNode(hasTestTag("filter_thought") and hasText("Thoughts")).assertExists()
        onNode(hasTestTag("filter_tool") and hasText("Tools")).assertExists()
        onNode(hasTestTag("filter_patch") and hasText("Patches")).assertExists()
        onNode(hasTestTag("filter_terminal") and hasText("Terminal")).assertExists()
    }

    @Test
    fun `no dedicated SubAgent filter chip exists`() = runConversationUiTest {
        sessionRepository.messagesToReturn = filterTestMessages
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("filter_subagent").assertDoesNotExist()
    }
}
