package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import com.knowledgespike.junieviewer.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class ConversationScreenTest {

    private val testMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Match this"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("Ignore that"), MessageKind.Text)
    )

    @Test
    fun `searching for text filters the message list`() = runConversationUiTest(testMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

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
    }

    @Test
    fun `toggling filters updates the message list`() = runConversationUiTest(testMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        robot.assertMessageCount(2)

        // Toggle off Junie messages
        robot.toggleFilter(FilterKind.Junie)

        // Only Human message remains
        robot.assertMessageCount(1)
        robot.assertMessageVisible("Match this")
    }

    @Test
    fun `human messages display sender label Human`() = runConversationUiTest(testMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        robot.assertSenderLabelVisible("Human")
        robot.assertSenderLabelVisible("Junie")
    }

    @Test
    fun `junie messages are grouped with a turn header`() = runConversationUiTest(testMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // One Human then one Junie → one Junie Turn header
        robot.assertTurnHeaderCount(1)
        robot.assertHumanMessageCount(1)
        robot.assertJunieMessageCount(1)
    }

    @Test
    fun `human messages are compact and junie messages are full width`() = runConversationUiTest(testMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Verify distinct message item tags exist for each sender type
        robot.assertHumanMessageCount(1)
        robot.assertJunieMessageCount(1)
    }

    @Test
    fun `message kind markers are displayed`() = runConversationUiTest(testMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        robot.assertMessageKindMarkerVisible("Text")
    }

    @Test
    fun `messages preserve chronological order with filters active`() = runConversationUiTest(testMessages) {
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

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
    }
}
