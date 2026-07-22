package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MessageContentRegistryTest {

    @Test
    fun everyMessageKindHasADescriptor() {
        MessageKind.entries.forEach { kind ->
            val descriptor = MessageContentRegistry.descriptorFor(kind)
            assertNotNull(descriptor, "Missing descriptor for $kind")
            assertEquals(kind, descriptor.kind)
        }
    }

    @Test
    fun searchableTextExtraction() {
        val textMessage = Message(
            id = "1",
            content = MessageContent.Text("Hello world"),
            kind = MessageKind.Text,
            sender = Sender.Human,
            timestamp = 0L
        )
        assertEquals("Hello world", MessageContentRegistry.searchableText(textMessage))

        val codeMessage = Message(
            id = "2",
            content = MessageContent.Code("println()", "kotlin"),
            kind = MessageKind.Text,
            sender = Sender.Junie,
            timestamp = 0L
        )
        assertEquals("println()", MessageContentRegistry.searchableText(codeMessage))
    }

    @Test
    fun collapsibleBlockIds() {
        val thoughtMessage = Message(
            id = "msg1",
            content = MessageContent.Text("thinking"),
            kind = MessageKind.Thought,
            sender = Sender.Junie,
            timestamp = 0L
        )
        val ids = MessageContentRegistry.collectCollapsibleBlockIds(listOf(thoughtMessage))
        assertTrue(ids.contains("msg1:thought"))

        val textMessage = Message(
            id = "msg2",
            content = MessageContent.Text("hello"),
            kind = MessageKind.Text,
            sender = Sender.Human,
            timestamp = 0L
        )
        val ids2 = MessageContentRegistry.collectCollapsibleBlockIds(listOf(textMessage))
        assertTrue(ids2.contains("msg2:text"))
    }
}
