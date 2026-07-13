package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.MessageKind
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Returns a short text label for a Message Kind, used as a non-colour-only marker.
 */
fun messageKindLabel(kind: MessageKind): String = when (kind) {
    MessageKind.Text -> "💬 Text"
    MessageKind.Markdown -> "📄 Markdown"
    MessageKind.Thought -> "💭 Thought"
    MessageKind.Tool -> "🔧 Tool"
    MessageKind.Patch -> "📝 Patch"
    MessageKind.Terminal -> "⌨ Terminal"
    MessageKind.StructuredOutput -> "📊 Structured"
    MessageKind.Error -> "❌ Error"
    MessageKind.Warning -> "⚠️ Warning"
    MessageKind.Unsupported -> "⚠ Unsupported"
    MessageKind.TestRun -> "🧪 Test"
    MessageKind.Mcp -> "🔌 MCP"
    MessageKind.SubAgent -> "🤖 SubAgent"
    MessageKind.Question -> "❓ Question"
    MessageKind.Choice -> "🔘 Choice"
    MessageKind.SystemMessage -> "ℹ️ System"
    MessageKind.Cancelled -> "⛔ Cancelled"
    MessageKind.Status -> "📋 Status"
}

/** Formats an epoch-millis timestamp into a human-readable local date/time string. */
fun formatTimestamp(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

/** Heuristic: returns true if the text contains common Markdown formatting markers. */
fun looksLikeMarkdown(text: String): Boolean {
    val markers = listOf("# ", "## ", "**", "__", "```", "- ", "* ", "1. ", "[", "](")
    return markers.any { text.contains(it) }
}
