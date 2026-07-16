package com.knowledgespike.junieviewer.data

import com.knowledgespike.junieviewer.domain.*
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Tests for EventToMessageMapper sub-agent mapping behaviour.
 * Verifies CustomAgentBlockUpdatedEvent maps correctly to MessageKind.SubAgent
 * with clean content (no emoji prefix) and null-tolerant fallbacks.
 */
class EventToMessageMapperTest {


    /** Helper to create a SessionA2uxEvent wrapping a CustomAgentBlockUpdatedEvent. */
    private fun subAgentEvent(
        name: String? = null,
        status: String? = null,
        stepId: String? = null,
        timestampMs: Long = 100L
    ): SessionA2uxEvent = SessionA2uxEvent(
        event = AgentEventWrapper(
            agentEvent = CustomAgentBlockUpdatedEvent(
                stepId = stepId,
                name = name,
                status = status
            )
        ),
        timestampMs = timestampMs
    )

    @Test
    fun `given a CustomAgentBlockUpdatedEvent with name and status when mapped then kind is SubAgent`() {
        val events = listOf(subAgentEvent(name = "android-qa-agent", status = "STARTED"))
        val messages = EventToMessageMapper.mapEventsToMessages(events)

        expectThat(messages).hasSize(1)
        expectThat(messages.first()) {
            get { kind }.isEqualTo(MessageKind.SubAgent)
            get { sender }.isEqualTo(Sender.Junie)
        }
    }

    @Test
    fun `given a CustomAgentBlockUpdatedEvent with name and status when mapped then content contains name and status`() {
        val events = listOf(subAgentEvent(name = "android-qa-agent", status = "STARTED"))
        val messages = EventToMessageMapper.mapEventsToMessages(events)
        val text = (messages.first().content as MessageContent.Text).text

        expectThat(text).contains("android-qa-agent")
        expectThat(text).contains("STARTED")
    }

    @Test
    fun `given a CustomAgentBlockUpdatedEvent when mapped then content does not contain emoji`() {
        val events = listOf(subAgentEvent(name = "qa-agent", status = "FINISHED"))
        val messages = EventToMessageMapper.mapEventsToMessages(events)
        val text = (messages.first().content as MessageContent.Text).text

        expectThat(text).not().contains("\uD83E\uDD16") // 🤖
        expectThat(text).not().contains("🤖")
    }

    @Test
    fun `given a CustomAgentBlockUpdatedEvent with null name when mapped then content uses fallback`() {
        val events = listOf(subAgentEvent(name = null, status = "STARTED"))
        val messages = EventToMessageMapper.mapEventsToMessages(events)
        val text = (messages.first().content as MessageContent.Text).text

        expectThat(text).contains("Unnamed sub-agent")
        expectThat(text).contains("STARTED")
    }

    @Test
    fun `given a CustomAgentBlockUpdatedEvent with null status when mapped then content uses fallback`() {
        val events = listOf(subAgentEvent(name = "qa-agent", status = null))
        val messages = EventToMessageMapper.mapEventsToMessages(events)
        val text = (messages.first().content as MessageContent.Text).text

        expectThat(text).contains("qa-agent")
        expectThat(text).contains("unknown")
    }

    @Test
    fun `given a CustomAgentBlockUpdatedEvent with all null fields when mapped then content uses all fallbacks`() {
        val events = listOf(subAgentEvent(name = null, status = null))
        val messages = EventToMessageMapper.mapEventsToMessages(events)
        val text = (messages.first().content as MessageContent.Text).text

        expectThat(text).isEqualTo("Unnamed sub-agent [unknown]")
    }

    @Test
    fun `given SubAgent MessageKind then filterCategory is Tool`() {
        expectThat(MessageKind.SubAgent.filterCategory).isEqualTo(FilterCategory.Tool)
    }
}
