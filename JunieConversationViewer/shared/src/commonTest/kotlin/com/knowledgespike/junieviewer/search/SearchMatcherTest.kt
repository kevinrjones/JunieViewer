package com.knowledgespike.junieviewer.search

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

/**
 * Unit tests for [findCaseInsensitiveMatches] — the single pure implementation
 * of case-insensitive literal text matching shared by both search-highlight
 * call sites (Markdown content and plain-text blocks).
 */
class SearchMatcherTest {

    @Test
    fun `blank query returns no matches`() {
        expectThat(findCaseInsensitiveMatches("Hello world", "")).isEmpty()
    }

    @Test
    fun `empty text returns no matches`() {
        expectThat(findCaseInsensitiveMatches("", "test")).isEmpty()
    }

    @Test
    fun `no matches returns empty list`() {
        expectThat(findCaseInsensitiveMatches("Hello world", "xyz")).isEmpty()
    }

    @Test
    fun `single match returns correct range`() {
        val matches = findCaseInsensitiveMatches("Hello world", "world")
        expectThat(matches).hasSize(1)
        expectThat(matches[0]).isEqualTo(6 until 11)
    }

    @Test
    fun `multiple matches are all found`() {
        val matches = findCaseInsensitiveMatches("foo bar foo baz foo", "foo")
        expectThat(matches).hasSize(3)
    }

    @Test
    fun `matching is case-insensitive`() {
        val matches = findCaseInsensitiveMatches("Hello HELLO hello", "hello")
        expectThat(matches).hasSize(3)
    }

    @Test
    fun `adjacent matches are non-overlapping`() {
        val matches = findCaseInsensitiveMatches("aaaa", "aa")
        expectThat(matches).hasSize(2)
        expectThat(matches[0]).isEqualTo(0 until 2)
        expectThat(matches[1]).isEqualTo(2 until 4)
    }

    @Test
    fun `query with regex special characters is treated as literal`() {
        val matches = findCaseInsensitiveMatches("price is \$10.00 today", "$10.00")
        expectThat(matches).hasSize(1)
    }
}
