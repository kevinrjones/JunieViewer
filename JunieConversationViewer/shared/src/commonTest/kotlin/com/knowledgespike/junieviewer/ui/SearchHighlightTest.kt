package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.knowledgespike.junieviewer.ui.components.highlightSearchMatches
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Unit tests for the [highlightSearchMatches] utility function.
 * Covers empty/blank queries, no matches, single/multiple/adjacent matches,
 * case-insensitive matching, regex-special characters, and current vs non-current styling.
 */
class SearchHighlightTest {

    private val highlightBg = Color(0xFFFFF176)
    private val highlightText = Color(0xFF1B1B1B)
    private val currentBg = Color(0xFFFF8F00)
    private val currentText = Color(0xFFFFFFFF)

    private fun highlight(
        text: String,
        query: String,
        isCurrentMatch: Boolean = false
    ): AnnotatedString = highlightSearchMatches(
        text = text,
        query = query,
        isCurrentMatch = isCurrentMatch,
        highlightBackground = highlightBg,
        highlightText = highlightText,
        currentMatchBackground = currentBg,
        currentMatchText = currentText
    )

    @Test
    fun `empty query returns unhighlighted text`() {
        val result = highlight("Hello world", "")
        expectThat(result.text).isEqualTo("Hello world")
        expectThat(result.spanStyles).isEmpty()
    }

    @Test
    fun `blank query returns unhighlighted text`() {
        val result = highlight("Hello world", "   ")
        expectThat(result.text).isEqualTo("Hello world")
        expectThat(result.spanStyles).isEmpty()
    }

    @Test
    fun `no matches returns unhighlighted text`() {
        val result = highlight("Hello world", "xyz")
        expectThat(result.text).isEqualTo("Hello world")
        expectThat(result.spanStyles).isEmpty()
    }

    @Test
    fun `single match is highlighted`() {
        val result = highlight("Hello world", "world")
        expectThat(result.text).isEqualTo("Hello world")
        expectThat(result.spanStyles).hasSize(1)
        expectThat(result.spanStyles[0].start).isEqualTo(6)
        expectThat(result.spanStyles[0].end).isEqualTo(11)
    }

    @Test
    fun `multiple matches are all highlighted`() {
        val result = highlight("foo bar foo baz foo", "foo")
        expectThat(result.spanStyles).hasSize(3)
    }

    @Test
    fun `matching is case-insensitive`() {
        val result = highlight("Hello HELLO hello", "hello")
        expectThat(result.spanStyles).hasSize(3)
        // Original casing preserved
        expectThat(result.text).isEqualTo("Hello HELLO hello")
    }

    @Test
    fun `query with regex special characters is treated as literal`() {
        val result = highlight("price is $10.00 today", "$10.00")
        expectThat(result.spanStyles).hasSize(1)
        expectThat(result.text).isEqualTo("price is \$10.00 today")
    }

    @Test
    fun `adjacent matches are highlighted separately`() {
        val result = highlight("aaaa", "aa")
        expectThat(result.spanStyles).hasSize(2)
        expectThat(result.spanStyles[0].start).isEqualTo(0)
        expectThat(result.spanStyles[0].end).isEqualTo(2)
        expectThat(result.spanStyles[1].start).isEqualTo(2)
        expectThat(result.spanStyles[1].end).isEqualTo(4)
    }

    @Test
    fun `non-current match uses regular highlight colours`() {
        val result = highlight("test", "test", isCurrentMatch = false)
        val style = result.spanStyles[0].item
        expectThat(style.background).isEqualTo(highlightBg)
        expectThat(style.color).isEqualTo(highlightText)
    }

    @Test
    fun `current match uses current-match colours`() {
        val result = highlight("test", "test", isCurrentMatch = true)
        val style = result.spanStyles[0].item
        expectThat(style.background).isEqualTo(currentBg)
        expectThat(style.color).isEqualTo(currentText)
    }

    @Test
    fun `empty text returns empty annotated string`() {
        val result = highlight("", "test")
        expectThat(result.text).isEqualTo("")
        expectThat(result.spanStyles).isEmpty()
    }
}
