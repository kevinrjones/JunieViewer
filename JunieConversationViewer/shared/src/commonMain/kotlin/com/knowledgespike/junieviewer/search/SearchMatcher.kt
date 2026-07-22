package com.knowledgespike.junieviewer.search

/**
 * Finds all case-insensitive, literal occurrences of [query] within [text].
 *
 * Matching is non-overlapping: once a match is found, the search resumes
 * immediately after the matched range. Returns an empty list when [query]
 * is blank or [text] does not contain it.
 *
 * This is the single source of truth for case-insensitive search matching,
 * shared by the Markdown highlighter and the plain-text search highlighter.
 */
fun findCaseInsensitiveMatches(text: String, query: String): List<IntRange> {
    if (query.isEmpty() || text.isEmpty()) return emptyList()

    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    val queryLen = lowerQuery.length

    val matches = mutableListOf<IntRange>()
    var searchFrom = 0
    while (searchFrom <= lowerText.length - queryLen) {
        val idx = lowerText.indexOf(lowerQuery, searchFrom)
        if (idx < 0) break
        matches.add(idx until idx + queryLen)
        searchFrom = idx + queryLen
    }
    return matches
}
