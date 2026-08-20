package com.knowledgespike.junieviewer.search

import com.knowledgespike.junieviewer.domain.TopLevelSearchPartialFailure
import com.knowledgespike.junieviewer.domain.TopLevelSearchQuery
import com.knowledgespike.junieviewer.domain.TopLevelSearchResults
import com.knowledgespike.junieviewer.domain.TopLevelSearchStatus
import com.knowledgespike.junieviewer.domain.TopLevelSessionIdentity
import com.knowledgespike.junieviewer.domain.TopLevelSessionSearchResult
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TopLevelSessionSearchRulesTest {

    @Test
    fun `query normalization trims and collapses whitespace`() {
        val query = TopLevelSearchQuery("   build    failure   logs  ")

        assertEquals("build failure logs", query.normalized)
        assertFalse(query.isBlank)
    }

    @Test
    fun `blank query is safe and marked blank`() {
        val query = TopLevelSearchQuery(" \n\t ")

        assertEquals("", query.normalized)
        assertTrue(query.isBlank)
    }

    @Test
    fun `results defaults are safe`() {
        val results = TopLevelSearchResults()

        assertTrue(results.sessionResults.isEmpty())
        assertTrue(results.partialFailures.isEmpty())
        assertFalse(results.hasResults)
        assertFalse(results.isPartial)
        assertEquals(TopLevelSearchStatus.Idle, results.status)
    }

    @Test
    fun `partial failure can coexist with successful session results`() {
        val success = TopLevelSessionSearchResult(
            session = TopLevelSessionIdentity("session-a", "/tmp/a", 100L),
            matchCount = 2
        )
        val partialFailure = TopLevelSearchPartialFailure(
            sessionId = "session-b",
            sessionPath = "/tmp/b",
            reason = "events.jsonl unreadable"
        )

        val results = TopLevelSearchResults(
            query = TopLevelSearchQuery("hello"),
            status = TopLevelSearchStatus.Completed,
            sessionResults = listOf(success),
            partialFailures = listOf(partialFailure)
        )

        assertTrue(results.hasResults)
        assertTrue(results.isPartial)
        assertEquals(1, results.sessionResults.size)
        assertEquals(1, results.partialFailures.size)
    }

    @Test
    fun `ordering is deterministic by match count then timestamp then identity`() {
        val lowCountNew = TopLevelSessionSearchResult(
            session = TopLevelSessionIdentity("session-c", "/sessions/c", 3_000L),
            matchCount = 1
        )
        val highCountOld = TopLevelSessionSearchResult(
            session = TopLevelSessionIdentity("session-b", "/sessions/b", 1_000L),
            matchCount = 4
        )
        val highCountNew = TopLevelSessionSearchResult(
            session = TopLevelSessionIdentity("session-a", "/sessions/a", 2_000L),
            matchCount = 4
        )
        val highCountNoTimestamp = TopLevelSessionSearchResult(
            session = TopLevelSessionIdentity("session-d", "/sessions/d", null),
            matchCount = 4
        )

        val ordered = orderTopLevelSessionResults(
            listOf(highCountOld, lowCountNew, highCountNoTimestamp, highCountNew)
        )

        assertEquals("session-a", ordered[0].session.sessionId)
        assertEquals("session-b", ordered[1].session.sessionId)
        assertEquals("session-d", ordered[2].session.sessionId)
        assertEquals("session-c", ordered[3].session.sessionId)
    }

    @Test
    fun `snippet extraction is case insensitive and bounded`() {
        val text = "prefix AlphaBeta suffix"
        val snippet = buildTopLevelSearchSnippet(text, TopLevelSearchQuery("alphabeta"), maxSnippetLength = 12)

        assertNotNull(snippet)
        assertTrue(snippet.preview.contains("AlphaBeta"))
        assertEquals("AlphaBeta", snippet.preview.substring(snippet.matchStartInPreview, snippet.matchEndExclusiveInPreview))
        assertTrue(snippet.preview.length <= 14)
    }

    @Test
    fun `snippet adds ellipsis when truncated from both sides`() {
        val text = "0123456789abcdef0123456789"
        val snippet = buildTopLevelSearchSnippet(text, TopLevelSearchQuery("abcdef"), maxSnippetLength = 8)

        assertNotNull(snippet)
        assertTrue(snippet.hasLeadingEllipsis)
        assertTrue(snippet.hasTrailingEllipsis)
        assertTrue(snippet.preview.startsWith("…"))
        assertTrue(snippet.preview.endsWith("…"))
    }

    @Test
    fun `snippet returns null on no match or blank query`() {
        val noMatch = buildTopLevelSearchSnippet("hello world", TopLevelSearchQuery("zzz"))
        val blankQuery = buildTopLevelSearchSnippet("hello world", TopLevelSearchQuery("   "))

        assertNull(noMatch)
        assertNull(blankQuery)
    }

    @Test
    fun `source normalization is trimmed collapsed and bounded`() {
        val normalized = normalizeTopLevelSnippetSource("  a\n\n b\t\t c  ", maxLength = 5)

        assertEquals("a b c", normalized)
    }
}