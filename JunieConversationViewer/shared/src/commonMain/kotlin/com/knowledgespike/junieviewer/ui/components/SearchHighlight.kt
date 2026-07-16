package com.knowledgespike.junieviewer.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

/**
 * Builds an [AnnotatedString] with search match highlighting applied to all
 * case-insensitive occurrences of [query] within [text].
 *
 * When [isCurrentMatch] is true the current-match colours are used;
 * otherwise the regular search highlight colours are applied.
 *
 * Returns an unhighlighted [AnnotatedString] when [query] is blank or has no matches.
 * The [query] is treated as literal text, not a regex pattern.
 */
fun highlightSearchMatches(
    text: String,
    query: String,
    isCurrentMatch: Boolean,
    highlightBackground: Color,
    highlightText: Color,
    currentMatchBackground: Color,
    currentMatchText: Color
): AnnotatedString {
    if (query.isBlank() || text.isEmpty()) return AnnotatedString(text)

    val bgColor = if (isCurrentMatch) currentMatchBackground else highlightBackground
    val fgColor = if (isCurrentMatch) currentMatchText else highlightText
    val style = SpanStyle(background = bgColor, color = fgColor)

    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    val queryLen = lowerQuery.length

    // Find all match positions
    val matches = mutableListOf<IntRange>()
    var searchFrom = 0
    while (searchFrom <= lowerText.length - queryLen) {
        val idx = lowerText.indexOf(lowerQuery, searchFrom)
        if (idx < 0) break
        matches.add(idx until idx + queryLen)
        searchFrom = idx + queryLen
    }

    if (matches.isEmpty()) return AnnotatedString(text)

    return buildAnnotatedString {
        append(text)
        matches.forEach { range ->
            addStyle(style, range.first, range.last + 1)
        }
    }
}
