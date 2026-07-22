package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.domain.groupMessagesIntoTurns
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Focused unit tests for [MessageVisibilityEngine], the pure filter/sort/match-index
 * derivation engine extracted from [ConversationViewModel]. No ViewModel or coroutine
 * machinery is involved — these tests exercise the engine directly.
 */
class MessageVisibilityEngineTest {

    private val messages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("Hello"), MessageKind.Text),
        Message("3", Sender.Human, MessageContent.Text("Other"), MessageKind.Text),
        Message("4", Sender.Junie, MessageContent.Text("Other"), MessageKind.Text)
    )

    @Test
    fun `returns all messages when filter is default and query is blank`() {
        val result = MessageVisibilityEngine.derive(
            messages = messages,
            filter = FilterState(),
            sortOrder = SortOrder.OldestFirst,
            query = "",
            currentMatchIndex = -1
        )

        assertEquals(messages, result.filteredMessages)
    }

    @Test
    fun `search query filters by case-insensitive substring match`() {
        val result = MessageVisibilityEngine.derive(
            messages = messages,
            filter = FilterState(),
            sortOrder = SortOrder.OldestFirst,
            query = "hello",
            currentMatchIndex = -1
        )

        assertEquals(listOf(messages[0], messages[1]), result.filteredMessages)
    }

    @Test
    fun `hiding human filter excludes human-sent messages`() {
        val result = MessageVisibilityEngine.derive(
            messages = messages,
            filter = FilterState(showHuman = false),
            sortOrder = SortOrder.OldestFirst,
            query = "",
            currentMatchIndex = -1
        )

        assertEquals(listOf(messages[1], messages[3]), result.filteredMessages)
    }

    @Test
    fun `newest first sort order reverses the chronological list`() {
        val result = MessageVisibilityEngine.derive(
            messages = messages,
            filter = FilterState(),
            sortOrder = SortOrder.NewestFirst,
            query = "",
            currentMatchIndex = -1
        )

        assertEquals(messages.asReversed(), result.filteredMessages)
    }

    @Test
    fun `match index resets to zero when a non-blank query first matches`() {
        val result = MessageVisibilityEngine.derive(
            messages = messages,
            filter = FilterState(),
            sortOrder = SortOrder.OldestFirst,
            query = "hello",
            currentMatchIndex = -1
        )

        assertEquals(0, result.currentMatchIndex)
    }

    @Test
    fun `match index resets to negative one when there are no results`() {
        val result = MessageVisibilityEngine.derive(
            messages = messages,
            filter = FilterState(),
            sortOrder = SortOrder.OldestFirst,
            query = "no-such-text",
            currentMatchIndex = 2
        )

        assertEquals(-1, result.currentMatchIndex)
    }

    @Test
    fun `match index clamps back to zero when out of bounds for the new result set`() {
        val result = MessageVisibilityEngine.derive(
            messages = messages,
            filter = FilterState(),
            sortOrder = SortOrder.OldestFirst,
            query = "hello",
            currentMatchIndex = 5
        )

        assertEquals(0, result.currentMatchIndex)
    }

    @Test
    fun `match index is preserved when still within bounds`() {
        val result = MessageVisibilityEngine.derive(
            messages = messages,
            filter = FilterState(),
            sortOrder = SortOrder.OldestFirst,
            query = "hello",
            currentMatchIndex = 1
        )

        assertEquals(1, result.currentMatchIndex)
    }

    @Test
    fun `turns are derived from the filtered and sorted message list`() {
        val result = MessageVisibilityEngine.derive(
            messages = messages,
            filter = FilterState(showHuman = false),
            sortOrder = SortOrder.OldestFirst,
            query = "",
            currentMatchIndex = -1
        )

        assertEquals(groupMessagesIntoTurns(listOf(messages[1], messages[3])), result.turns)
    }
}
