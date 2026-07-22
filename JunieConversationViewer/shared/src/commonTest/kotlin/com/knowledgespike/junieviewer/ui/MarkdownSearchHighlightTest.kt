package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import com.knowledgespike.junieviewer.ui.components.MarkdownContent
import com.knowledgespike.junieviewer.ui.components.applySearchHighlight
import com.knowledgespike.junieviewer.ui.components.renderInlineMarkdown
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import com.knowledgespike.junieviewer.ui.theme.darkConversationColors
import com.knowledgespike.junieviewer.ui.theme.lightConversationColors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class MarkdownSearchHighlightTest {

    private val lightColors = lightConversationColors()
    private val darkColors = darkConversationColors()

    // region Unit Tests for applySearchHighlight

    @Test
    fun `empty query returns original annotated string`() {
        val input = AnnotatedString("Hello World")
        val result = applySearchHighlight(input, "", false, lightColors)
        expectThat(result).isEqualTo(input)
    }

    @Test
    fun `no matches returns original annotated string`() {
        val input = AnnotatedString("hello world")
        val result = applySearchHighlight(input, "xyz", false, lightColors)
        expectThat(result).isEqualTo(input)
    }

    @Test
    fun `single match highlights correctly`() {
        val input = AnnotatedString("hello world")
        val result = applySearchHighlight(input, "hello", false, lightColors)
        
        expectThat(result.text).isEqualTo("hello world")
        expectThat(result.spanStyles).hasSize(1)
        expectThat(result.spanStyles[0].start).isEqualTo(0)
        expectThat(result.spanStyles[0].end).isEqualTo(5)
        expectThat(result.spanStyles[0].item.background).isEqualTo(lightColors.searchHighlightBackground)
    }

    @Test
    fun `multiple matches highlight correctly`() {
        val input = AnnotatedString("a banana")
        val result = applySearchHighlight(input, "a", false, lightColors)
        
        expectThat(result.text).isEqualTo("a banana")
        // "a", "bAnAnA" -> index 0, 3, 5, 7? No, "a banana": 0:a, 1: , 2:b, 3:a, 4:n, 5:a, 6:n, 7:a
        // indices: 0, 3, 5, 7.
        expectThat(result.spanStyles).hasSize(4)
    }

    @Test
    fun `case insensitive matching`() {
        val input = AnnotatedString("Hello World")
        val result = applySearchHighlight(input, "HELLO", false, lightColors)
        
        expectThat(result.text).isEqualTo("Hello World")
        expectThat(result.spanStyles).hasSize(1)
        expectThat(result.spanStyles[0].start).isEqualTo(0)
        expectThat(result.spanStyles[0].end).isEqualTo(5)
    }

    @Test
    fun `preserves inline markdown spans`() {
        // renderInlineMarkdown("some **bold** text", ...)
        val annotated = renderInlineMarkdown("some **bold** text", Color.Blue, Color.Gray)

        // The word "bold" should have a Bold span already.
        // We highlight "bold"
        val result = applySearchHighlight(annotated, "bold", false, lightColors)

        expectThat(result.text).contains("bold")
        // Should have at least the bold span and the highlight span
        assertTrue(result.spanStyles.size > 1, "Should have more than one span style")

        // Verify we have a highlight span at the correct position
        val boldIndex = result.text.indexOf("bold")
        val highlightSpan = result.spanStyles.find {
            it.start == boldIndex && it.end == boldIndex + 4 && it.item.background == lightColors.searchHighlightBackground
        }
        assertNotNull(highlightSpan, "Highlight span should exist for 'bold'")
    }

    @Test
    fun `current match uses current match colors`() {
        val input = AnnotatedString("test")
        val result = applySearchHighlight(input, "test", true, lightColors)

        expectThat(result.spanStyles[0].item.background).isEqualTo(lightColors.currentMatchBackground)
        expectThat(result.spanStyles[0].item.color).isEqualTo(lightColors.currentMatchText)
    }

    @Test
    fun `non-current match uses regular highlight colors`() {
        val input = AnnotatedString("test")
        val result = applySearchHighlight(input, "test", false, lightColors)

        expectThat(result.spanStyles[0].item.background).isEqualTo(lightColors.searchHighlightBackground)
        expectThat(result.spanStyles[0].item.color).isEqualTo(lightColors.searchHighlightText)
    }

    // endregion

    // region Compose UI Tests

    @Test
    fun `markdown content shows heading with search highlight test tag`() = runComposeUiTest {
        setContent {
            JunieViewerTheme {
                MarkdownContent(
                    markdown = "# Test Heading\n\nA paragraph.",
                    searchQuery = "test",
                    isCurrentMatch = false
                )
            }
        }
        onNodeWithTag("markdown_heading").assertExists()
    }

    @Test
    fun `markdown content shows paragraph with search highlight test tag`() = runComposeUiTest {
        setContent {
            JunieViewerTheme {
                MarkdownContent(
                    markdown = "A paragraph with **bold** text.",
                    searchQuery = "bold",
                    isCurrentMatch = false
                )
            }
        }
        onNodeWithTag("markdown_paragraph").assertExists()
    }

    @Test
    fun `markdown content shows list item with search highlight test tag`() = runComposeUiTest {
        setContent {
            JunieViewerTheme {
                MarkdownContent(
                    markdown = "- Item one\n- Item two",
                    searchQuery = "item",
                    isCurrentMatch = false
                )
            }
        }
        onAllNodesWithTag("markdown_list_item").onFirst().assertExists()
    }

    // endregion
}
