package com.knowledgespike.junieviewer.search

import com.knowledgespike.junieviewer.domain.TopLevelSearchQuery
import com.knowledgespike.junieviewer.domain.TopLevelSearchSnippet
import com.knowledgespike.junieviewer.domain.TopLevelSessionSearchResult

private const val MIN_SNIPPET_LENGTH = 12
private const val ELLIPSIS = "…"
const val DEFAULT_TOP_LEVEL_SNIPPET_LENGTH = 120
const val DEFAULT_TOP_LEVEL_SOURCE_LIMIT = 4_096

/**
 * Deterministic ordering for top-level Session search results.
 *
 * 1) Higher match count first
 * 2) More recent Session timestamp first (when available)
 * 3) Stable Session identity/path tie-break
 */
fun orderTopLevelSessionResults(results: List<TopLevelSessionSearchResult>): List<TopLevelSessionSearchResult> {
    return results.sortedWith(
        compareByDescending<TopLevelSessionSearchResult> { it.matchCount }
            .thenByDescending { it.session.sessionTimestampMillis ?: Long.MIN_VALUE }
            .thenBy { it.session.sessionId }
            .thenBy { it.session.sessionPath }
    )
}

/**
 * Caps and normalizes candidate text before match/snippet work.
 */
fun normalizeTopLevelSnippetSource(text: String?, maxLength: Int = DEFAULT_TOP_LEVEL_SOURCE_LIMIT): String {
    if (text.isNullOrBlank()) return ""
    return text
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(maxLength.coerceAtLeast(MIN_SNIPPET_LENGTH))
}

/**
 * Creates a bounded, deterministic snippet around the first case-insensitive match.
 * Returns null when there is no match or the query is blank.
 */
fun buildTopLevelSearchSnippet(
    sourceText: String,
    query: TopLevelSearchQuery,
    maxSnippetLength: Int = DEFAULT_TOP_LEVEL_SNIPPET_LENGTH
): TopLevelSearchSnippet? {
    if (query.isBlank || sourceText.isEmpty()) return null

    val normalizedQuery = query.normalized
    val lowerSource = sourceText.lowercase()
    val lowerQuery = normalizedQuery.lowercase()
    val matchStart = lowerSource.indexOf(lowerQuery)
    if (matchStart < 0) return null

    val snippetLength = maxSnippetLength.coerceAtLeast(normalizedQuery.length).coerceAtLeast(MIN_SNIPPET_LENGTH)
    val matchEndExclusive = matchStart + normalizedQuery.length
    val context = (snippetLength - normalizedQuery.length) / 2

    var start = (matchStart - context).coerceAtLeast(0)
    var end = (matchEndExclusive + context).coerceAtMost(sourceText.length)

    val remaining = snippetLength - (end - start)
    if (remaining > 0) {
        val extendRight = (sourceText.length - end).coerceAtMost(remaining)
        end += extendRight
        val stillRemaining = remaining - extendRight
        if (stillRemaining > 0) {
            start = (start - stillRemaining).coerceAtLeast(0)
        }
    }

    val window = sourceText.substring(start, end)
    val hasLeadingEllipsis = start > 0
    val hasTrailingEllipsis = end < sourceText.length

    val preview = buildString {
        if (hasLeadingEllipsis) append(ELLIPSIS)
        append(window)
        if (hasTrailingEllipsis) append(ELLIPSIS)
    }

    val previewOffset = if (hasLeadingEllipsis) ELLIPSIS.length else 0
    val matchStartInPreview = previewOffset + (matchStart - start)
    val matchEndInPreview = matchStartInPreview + normalizedQuery.length

    return TopLevelSearchSnippet(
        preview = preview,
        matchStartInPreview = matchStartInPreview,
        matchEndExclusiveInPreview = matchEndInPreview,
        hasLeadingEllipsis = hasLeadingEllipsis,
        hasTrailingEllipsis = hasTrailingEllipsis
    )
}