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

/** Categories used by the filter bar — each MessageKind maps to exactly one. */
enum class FilterCategory {
    Human, Junie, Thought, Tool, Patch, Terminal, AlwaysShow
}

/** Classifies the semantic kind of a Message for filtering and rendering. */
enum class MessageKind(val filterCategory: FilterCategory) {
    Text(FilterCategory.Junie),
    Markdown(FilterCategory.Junie),
    Thought(FilterCategory.Thought),
    Tool(FilterCategory.Tool),
    Patch(FilterCategory.Patch),
    Terminal(FilterCategory.Terminal),
    StructuredOutput(FilterCategory.Tool),
    Error(FilterCategory.AlwaysShow),
    Warning(FilterCategory.AlwaysShow),
    Unsupported(FilterCategory.AlwaysShow),
    TestRun(FilterCategory.Terminal),
    Mcp(FilterCategory.Tool),
    SubAgent(FilterCategory.Tool),
    Question(FilterCategory.AlwaysShow),
    Choice(FilterCategory.AlwaysShow),
    SystemMessage(FilterCategory.AlwaysShow),
    Cancelled(FilterCategory.AlwaysShow),
    Status(FilterCategory.AlwaysShow)
}

/**
 * Represents the content of a message.
 */
sealed interface MessageContent {
    /** Plain or Markdown-formatted text. */
    data class Text(val text: String) : MessageContent

    /** Syntax-highlighted code block with an optional language hint. */
    data class Code(val code: String, val language: String = "kotlin") : MessageContent

    /** Unified diff / patch content. */
    data class Diff(val diff: String) : MessageContent

    /** Terminal / shell output with preserved whitespace. */
    data class Terminal(val output: String) : MessageContent

    /** Structured output such as JSON or key-value data. */
    data class Structured(val data: String) : MessageContent
}

/**
 * Represents the sender of a message.
 */
sealed interface Sender {
    data object Human : Sender
    data object Junie : Sender
}
