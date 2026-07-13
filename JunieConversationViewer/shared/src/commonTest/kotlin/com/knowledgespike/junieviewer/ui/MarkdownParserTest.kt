package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.ui.components.MarkdownBlock
import com.knowledgespike.junieviewer.ui.components.parseMarkdownBlocks
import com.knowledgespike.junieviewer.ui.components.renderInlineMarkdown
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

/** Unit tests for the lightweight Markdown parser used by MarkdownContent. */
class MarkdownParserTest {

    @Test
    fun `parses heading levels 1 through 4`() {
        val blocks = parseMarkdownBlocks("# H1\n## H2\n### H3\n#### H4")
        expectThat(blocks).hasSize(4)
        expectThat(blocks[0]).isA<MarkdownBlock.Heading>().and {
            get { level }.isEqualTo(1)
            get { text }.isEqualTo("H1")
        }
        expectThat(blocks[3]).isA<MarkdownBlock.Heading>().and {
            get { level }.isEqualTo(4)
            get { text }.isEqualTo("H4")
        }
    }

    @Test
    fun `parses unordered list items`() {
        val blocks = parseMarkdownBlocks("- item one\n* item two")
        expectThat(blocks).hasSize(2)
        expectThat(blocks[0]).isA<MarkdownBlock.ListItem>().and {
            get { bullet }.isEqualTo("•")
            get { text }.isEqualTo("item one")
        }
    }

    @Test
    fun `parses ordered list items`() {
        val blocks = parseMarkdownBlocks("1. first\n2. second")
        expectThat(blocks).hasSize(2)
        expectThat(blocks[0]).isA<MarkdownBlock.ListItem>().and {
            get { bullet }.isEqualTo("1.")
            get { text }.isEqualTo("first")
        }
    }

    @Test
    fun `parses fenced code blocks`() {
        val blocks = parseMarkdownBlocks("```kotlin\nval x = 1\n```")
        expectThat(blocks).hasSize(1)
        expectThat(blocks[0]).isA<MarkdownBlock.CodeFence>().and {
            get { language }.isEqualTo("kotlin")
            get { code }.isEqualTo("val x = 1")
        }
    }

    @Test
    fun `parses paragraphs`() {
        val blocks = parseMarkdownBlocks("Hello world.\nThis continues.")
        expectThat(blocks).hasSize(1)
        expectThat(blocks[0]).isA<MarkdownBlock.Paragraph>().and {
            get { text }.isEqualTo("Hello world. This continues.")
        }
    }

    @Test
    fun `renders bold inline markdown`() {
        val result = renderInlineMarkdown("This is **bold** text")
        expectThat(result.text).isEqualTo("This is bold text")
    }

    @Test
    fun `renders italic inline markdown`() {
        val result = renderInlineMarkdown("This is *italic* text")
        expectThat(result.text).isEqualTo("This is italic text")
    }

    @Test
    fun `renders inline code`() {
        val result = renderInlineMarkdown("Use `val x` here")
        expectThat(result.text).isEqualTo("Use val x here")
    }

    @Test
    fun `renders links as text only`() {
        val result = renderInlineMarkdown("See [docs](https://example.com) for info")
        expectThat(result.text).isEqualTo("See docs for info")
    }

    @Test
    fun `looksLikeMarkdown detects markdown markers`() {
        expectThat(looksLikeMarkdown("## Heading")).isTrue()
        expectThat(looksLikeMarkdown("**bold**")).isTrue()
        expectThat(looksLikeMarkdown("- list item")).isTrue()
        expectThat(looksLikeMarkdown("plain text only")).isFalse()
    }
}
