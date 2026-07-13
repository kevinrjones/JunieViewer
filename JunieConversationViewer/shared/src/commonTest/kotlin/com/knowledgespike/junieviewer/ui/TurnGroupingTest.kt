package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.domain.groupMessagesIntoTurns
import org.junit.Test

/**
 * Unit tests for the Turn grouping logic that groups consecutive same-Sender Messages.
 */
class TurnGroupingTest {

    @Test
    fun `empty message list produces no turns`() {
        val turns = groupMessagesIntoTurns(emptyList())
        assert(turns.isEmpty()) { "Expected no turns for empty list" }
    }

    @Test
    fun `single human message produces one turn`() {
        val messages = listOf(
            Message("1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text)
        )
        val turns = groupMessagesIntoTurns(messages)
        assert(turns.size == 1) { "Expected 1 turn, got ${turns.size}" }
        assert(turns[0].sender == Sender.Human)
        assert(turns[0].messages.size == 1)
    }

    @Test
    fun `alternating senders produce separate turns`() {
        val messages = listOf(
            Message("1", Sender.Human, MessageContent.Text("Q1"), MessageKind.Text, 1L),
            Message("2", Sender.Junie, MessageContent.Text("A1"), MessageKind.Text, 2L),
            Message("3", Sender.Human, MessageContent.Text("Q2"), MessageKind.Text, 3L)
        )
        val turns = groupMessagesIntoTurns(messages)
        assert(turns.size == 3) { "Expected 3 turns, got ${turns.size}" }
        assert(turns[0].sender == Sender.Human)
        assert(turns[1].sender == Sender.Junie)
        assert(turns[2].sender == Sender.Human)
    }

    @Test
    fun `consecutive junie messages are grouped into one turn`() {
        val messages = listOf(
            Message("1", Sender.Human, MessageContent.Text("Go"), MessageKind.Text, 1L),
            Message("2", Sender.Junie, MessageContent.Text("Thought"), MessageKind.Thought, 2L),
            Message("3", Sender.Junie, MessageContent.Text("Tool call"), MessageKind.Tool, 3L),
            Message("4", Sender.Junie, MessageContent.Text("Response"), MessageKind.Text, 4L)
        )
        val turns = groupMessagesIntoTurns(messages)
        assert(turns.size == 2) { "Expected 2 turns, got ${turns.size}" }
        assert(turns[1].sender == Sender.Junie)
        assert(turns[1].messages.size == 3) { "Expected 3 Junie messages in turn, got ${turns[1].messages.size}" }
    }

    @Test
    fun `turn grouping preserves chronological order`() {
        val messages = listOf(
            Message("1", Sender.Human, MessageContent.Text("First"), MessageKind.Text, 1L),
            Message("2", Sender.Junie, MessageContent.Text("Second"), MessageKind.Text, 2L),
            Message("3", Sender.Junie, MessageContent.Text("Third"), MessageKind.Text, 3L),
            Message("4", Sender.Human, MessageContent.Text("Fourth"), MessageKind.Text, 4L)
        )
        val turns = groupMessagesIntoTurns(messages)
        val allMessages = turns.flatMap { it.messages }
        assert(allMessages.map { it.id } == listOf("1", "2", "3", "4")) {
            "Message order not preserved"
        }
    }
}
