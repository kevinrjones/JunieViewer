package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Lightweight Markdown renderer supporting the core subset:
 * headings, bold, italic, lists, inline code, and links-as-text.
 * Complex tables degrade to readable plain text.
 */
@Composable
fun MarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) Spacer(modifier = Modifier.height(4.dp))
            when (block) {
                is MarkdownBlock.Heading -> Text(
                    text = block.text,
                    style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineSmall
                        2 -> MaterialTheme.typography.titleLarge
                        3 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleSmall
                    },
                    fontWeight = FontWeight.Bold
                )
                is MarkdownBlock.Paragraph -> Text(
                    text = renderInlineMarkdown(block.text),
                    style = MaterialTheme.typography.bodyMedium
                )
                is MarkdownBlock.ListItem -> Text(
                    text = buildAnnotatedString {
                        append("  ${block.bullet} ")
                        append(renderInlineMarkdown(block.text))
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                is MarkdownBlock.CodeFence -> CodeBlockWithCopy(
                    code = block.code,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/** Parsed Markdown block types. */
sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class ListItem(val bullet: String, val text: String) : MarkdownBlock
    data class CodeFence(val code: String, val language: String) : MarkdownBlock
}

/** Parses Markdown text into a list of blocks. */
fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        when {
            // Fenced code block
            line.trimStart().startsWith("```") -> {
                val lang = line.trimStart().removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.CodeFence(codeLines.joinToString("\n"), lang))
                i++ // skip closing ```
            }
            // Heading
            line.startsWith("#") -> {
                val level = line.takeWhile { it == '#' }.length
                val text = line.drop(level).trimStart()
                blocks.add(MarkdownBlock.Heading(level.coerceIn(1, 6), text))
                i++
            }
            // Unordered list
            line.trimStart().let { it.startsWith("- ") || it.startsWith("* ") } -> {
                val trimmed = line.trimStart()
                val text = trimmed.drop(2)
                blocks.add(MarkdownBlock.ListItem("•", text))
                i++
            }
            // Ordered list
            line.trimStart().matches(Regex("^\\d+\\.\\s.*")) -> {
                val trimmed = line.trimStart()
                val num = trimmed.takeWhile { it.isDigit() || it == '.' }
                val text = trimmed.drop(num.length).trimStart()
                blocks.add(MarkdownBlock.ListItem(num, text))
                i++
            }
            // Blank line — skip
            line.isBlank() -> {
                i++
            }
            // Paragraph
            else -> {
                val paraLines = mutableListOf(line)
                i++
                while (i < lines.size && lines[i].isNotBlank() &&
                    !lines[i].startsWith("#") &&
                    !lines[i].trimStart().startsWith("```") &&
                    !lines[i].trimStart().let { it.startsWith("- ") || it.startsWith("* ") } &&
                    !lines[i].trimStart().matches(Regex("^\\d+\\.\\s.*"))
                ) {
                    paraLines.add(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.Paragraph(paraLines.joinToString(" ")))
            }
        }
    }
    return blocks
}

/** Renders inline Markdown formatting (bold, italic, inline code, links-as-text). */
fun renderInlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            // Inline code
            text[i] == '`' && i + 1 < text.length -> {
                val end = text.indexOf('`', i + 1)
                if (end > i) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            // Bold **text** or __text__
            (text.startsWith("**", i) || text.startsWith("__", i)) -> {
                val marker = text.substring(i, i + 2)
                val end = text.indexOf(marker, i + 2)
                if (end > i) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            // Italic *text* or _text_
            (text[i] == '*' || text[i] == '_') && i + 1 < text.length && text[i + 1] != ' ' -> {
                val marker = text[i]
                val end = text.indexOf(marker, i + 1)
                if (end > i && end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            // Link [text](url) — render as "text"
            text[i] == '[' -> {
                val closeBracket = text.indexOf(']', i + 1)
                if (closeBracket > i && closeBracket + 1 < text.length && text[closeBracket + 1] == '(') {
                    val closeParen = text.indexOf(')', closeBracket + 2)
                    if (closeParen > closeBracket) {
                        append(text.substring(i + 1, closeBracket))
                        i = closeParen + 1
                    } else {
                        append(text[i])
                        i++
                    }
                } else {
                    append(text[i])
                    i++
                }
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}
