package com.knowledgespike.junieviewer.domain

/**
 * Represents a single message in the conversation.
 */
data class Message(
    val id: String,
    val sender: Sender,
    val content: MessageContent,
    val kind: MessageKind = MessageKind.Text,
    val timestamp: Long = 0L
)

enum class MessageKind {
    Text, Thought, Tool, Patch, Terminal, Unsupported
}

/**
 * Represents the content of a message.
 */
sealed interface MessageContent {
    data class Text(val text: String) : MessageContent
    data class Code(val code: String, val language: String = "kotlin") : MessageContent
    data class Diff(val diff: String) : MessageContent
}

/**
 * Represents the sender of a message.
 */
sealed interface Sender {
    data object Human : Sender
    data object Junie : Sender
}
