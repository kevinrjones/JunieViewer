package com.knowledgespike.junieviewer.domain

/**
 * Represents the Human-provided Search Query for Top-Level Session Search.
 *
 * [normalized] trims leading/trailing whitespace and collapses internal
 * whitespace runs to a single space for deterministic matching behavior.
 */
data class TopLevelSearchQuery(
    val raw: String = ""
) {
    val normalized: String = normalize(raw)
    val isBlank: Boolean = normalized.isEmpty()

    companion object {
        fun normalize(input: String): String =
            input
                .trim()
                .replace(Regex("\\s+"), " ")
    }
}

/**
 * Stable Session identity carried by top-level search results.
 */
data class TopLevelSessionIdentity(
    val sessionId: String,
    val sessionPath: String,
    val sessionTimestampMillis: Long? = null
)

/**
 * Preview snippet for one text match.
 */
data class TopLevelSearchSnippet(
    val preview: String,
    val matchStartInPreview: Int,
    val matchEndExclusiveInPreview: Int,
    val hasLeadingEllipsis: Boolean,
    val hasTrailingEllipsis: Boolean
)

/**
 * Optional per-Session summary to keep UI mapping deterministic and compact.
 */
data class TopLevelSearchMatchSummary(
    val firstSnippet: TopLevelSearchSnippet? = null,
    val additionalSnippetCount: Int = 0
)

/**
 * One Session-level top-level search result.
 */
data class TopLevelSessionSearchResult(
    val session: TopLevelSessionIdentity,
    val matchCount: Int,
    val snippets: List<TopLevelSearchSnippet> = emptyList(),
    val summary: TopLevelSearchMatchSummary = TopLevelSearchMatchSummary()
)

/**
 * Per-Session failure detail. Non-fatal and can coexist with successful results.
 */
data class TopLevelSearchPartialFailure(
    val sessionId: String,
    val sessionPath: String,
    val reason: String
)

enum class TopLevelSearchStatus {
    Idle,
    Running,
    Completed,
    EmptyQuery,
    Failed
}

/**
 * Structured output from a top-level Session search request.
 */
data class TopLevelSearchResults(
    val query: TopLevelSearchQuery = TopLevelSearchQuery(),
    val status: TopLevelSearchStatus = TopLevelSearchStatus.Idle,
    val sessionResults: List<TopLevelSessionSearchResult> = emptyList(),
    val partialFailures: List<TopLevelSearchPartialFailure> = emptyList(),
    val fatalError: String? = null
) {
    val hasResults: Boolean get() = sessionResults.isNotEmpty()
    val isPartial: Boolean get() = partialFailures.isNotEmpty()
}