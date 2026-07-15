package com.knowledgespike.junieviewer.ui

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** Formats an epoch-millis timestamp into a human-readable local date/time string. */
fun formatTimestamp(epochMillis: Long): String {
    return try {
        val tz = try { TimeZone.currentSystemDefault() } catch (_: Throwable) { TimeZone.UTC }
        Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(tz)
            .toString()
            .replace("T", " ")
            .substringBefore(".")
    } catch (_: Throwable) {
        "Unknown"
    }
}

/** Heuristic: returns true if the text contains common Markdown formatting markers. */
fun looksLikeMarkdown(text: String): Boolean {
    val markers = listOf("# ", "## ", "**", "__", "```", "- ", "* ", "1. ", "[", "](")
    return markers.any { text.contains(it) }
}
