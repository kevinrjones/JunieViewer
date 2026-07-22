package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind

/**
 * Descriptor for a [MessageKind], providing shared metadata and logic for searchable text
 * and collapsibility.
 */
data class MessageContentDescriptor(
    val kind: MessageKind,
    val isExpandedByDefault: Boolean = true,
    private val getCollapsibleBlockSuffix: (Message) -> String? = { null },
    private val extractSearchableText: (Message) -> String = { defaultSearchableText(it) }
) {
    /** Whether this kind (or its content) can be collapsed. */
    fun isCollapsible(message: Message): Boolean = getCollapsibleBlockSuffix(message) != null

    /** Returns the stable ID for the collapsible block in this message, if any. */
    fun getCollapsibleBlockId(message: Message): String? {
        val suffix = getCollapsibleBlockSuffix(message)
        return if (suffix != null) "${message.id}:$suffix" else null
    }

    /** Extracts searchable plain text from the message. */
    fun searchableText(message: Message): String = extractSearchableText(message)
}

/**
 * Registry of [MessageContentDescriptor]s for every [MessageKind].
 * Replaces exhaustive 'when' chains in the ViewModel (F1).
 */
object MessageContentRegistry {
    private val contentBasedSuffix: (Message) -> String? = { message ->
        when (message.content) {
            is MessageContent.Text -> "text"
            is MessageContent.Code -> "code"
            is MessageContent.Diff -> "diff"
            is MessageContent.Terminal -> "terminal"
            is MessageContent.Structured -> "structured"
        }
    }

    private val descriptors: Map<MessageKind, MessageContentDescriptor> = listOf(
        // Special kind-based suffixes
        MessageContentDescriptor(MessageKind.Thought, getCollapsibleBlockSuffix = { "thought" }),
        MessageContentDescriptor(MessageKind.Tool, getCollapsibleBlockSuffix = { "tool" }),
        MessageContentDescriptor(MessageKind.Mcp, getCollapsibleBlockSuffix = { "tool" }),
        MessageContentDescriptor(MessageKind.Markdown, getCollapsibleBlockSuffix = { "markdown" }),
        MessageContentDescriptor(MessageKind.SubAgent, getCollapsibleBlockSuffix = { "subagent" }),

        // Content-based suffixes (default behavior for most kinds)
        MessageContentDescriptor(MessageKind.Text, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.Patch, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.Terminal, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.StructuredOutput, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.TestRun, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.Question, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.Choice, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.SystemMessage, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.Cancelled, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.Status, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.Error, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.Warning, getCollapsibleBlockSuffix = contentBasedSuffix),
        MessageContentDescriptor(MessageKind.Unsupported, getCollapsibleBlockSuffix = contentBasedSuffix)
    ).associateBy { it.kind }

    /** Returns the descriptor for the given [MessageKind]. */
    fun descriptorFor(kind: MessageKind): MessageContentDescriptor =
        descriptors[kind] ?: throw IllegalArgumentException("No descriptor registered for MessageKind: $kind")

    /** Extracts searchable text from a [Message]. */
    fun searchableText(message: Message): String =
        descriptorFor(message.kind).searchableText(message)

    /** Returns whether a [Message] is expanded by default. */
    fun isExpandedByDefault(kind: MessageKind): Boolean =
        descriptorFor(kind).isExpandedByDefault

    /** Returns the set of collapsible block IDs for the given messages. */
    fun collectCollapsibleBlockIds(messages: List<Message>): Set<String> {
        val ids = mutableSetOf<String>()
        for (msg in messages) {
            val blockId = descriptorFor(msg.kind).getCollapsibleBlockId(msg)
            if (blockId != null) {
                ids.add(blockId)
            }
        }
        return ids
    }
}

/** Default implementation for extracting searchable text from [MessageContent]. */
private fun defaultSearchableText(message: Message): String = when (val content = message.content) {
    is MessageContent.Text -> content.text
    is MessageContent.Code -> content.code
    is MessageContent.Diff -> content.diff
    is MessageContent.Terminal -> content.output
    is MessageContent.Structured -> content.data
}
