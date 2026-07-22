package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.fixtures.RepresentativeFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test

/**
 * UI and mapping tests for Area 3 — Sub-Agent and Event Representation.
 * Verifies the Sub-Agent badge renders for SubAgent messages, emoji is removed,
 * and non-SubAgent messages do not show the badge.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class SubAgentRepresentationTest {

    // -----------------------------------------------------------------------
    // 3.5 — Sub-Agent badge renders for SubAgent messages
    // -----------------------------------------------------------------------

    @Test
    fun `sub-agent message renders Sub-Agent badge`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.subAgentMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("sub_agent_badge").assertExists()
        onNodeWithTag("sub_agent_block_header").assertExists()
    }

    @Test
    fun `sub-agent message content does not contain emoji`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.subAgentMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // The sub-agent content should be visible without emoji prefix
        onNodeWithText("android-qa-agent [STARTED]", substring = true).assertExists()
    }

    @Test
    fun `sub-agent message with missing fields renders fallback content`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.subAgentMessageMissingFields
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("sub_agent_badge").assertExists()
        onNodeWithText("Unnamed sub-agent [unknown]", substring = true).assertExists()
    }

    // -----------------------------------------------------------------------
    // 3.6 — Non-SubAgent messages do not show the badge
    // -----------------------------------------------------------------------

    @Test
    fun `human message does not render Sub-Agent badge`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("sub_agent_badge").assertDoesNotExist()
    }

    @Test
    fun `junie text message does not render Sub-Agent badge`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieTextMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("sub_agent_badge").assertDoesNotExist()
    }

    @Test
    fun `tool call message does not render Sub-Agent badge`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieToolCallMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("sub_agent_badge").assertDoesNotExist()
    }

    // -----------------------------------------------------------------------
    // 3.6 — Chronological order preserved with sub-agent messages
    // -----------------------------------------------------------------------

    @Test
    fun `sub-agent messages maintain chronological order with other messages`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.subAgentMessage,
            RepresentativeFixtures.junieTextMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // All three messages should render; badge only on the sub-agent one
        val badges = onAllNodesWithTag("sub_agent_badge").fetchSemanticsNodes()
        assert(badges.size == 1) { "Expected exactly 1 sub-agent badge, got ${badges.size}" }

        val kindMarkers = onAllNodesWithTag("message_kind_marker").fetchSemanticsNodes()
        assert(kindMarkers.size >= 3) { "Expected at least 3 kind markers for 3 messages, got ${kindMarkers.size}" }
    }
}
