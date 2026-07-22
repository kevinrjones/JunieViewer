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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.knowledgespike.junieviewer.markdown.MarkdownBlock
import com.knowledgespike.junieviewer.markdown.parseMarkdownBlocks
import com.knowledgespike.junieviewer.search.findCaseInsensitiveMatches
import com.knowledgespike.junieviewer.ui.theme.ConversationColors
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import com.knowledgespike.junieviewer.ui.theme.MonospaceFont

/**
 * Lightweight Markdown renderer supporting the core subset:
 * headings, bold, italic, lists, inline code, and links-as-text.
 * Complex tables degrade to readable plain text.
 *
 * Supports Search highlighting: when [searchQuery] is non-blank, matching text
 * is highlighted using theme-aware colours. [isCurrentMatch] controls whether
 * current-match or regular highlight colours are used.
 */
@Composable
fun MarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }
    val spacing = JunieViewerTheme.spacing
    val colors = JunieViewerTheme.conversationColors
    val primaryColor = MaterialTheme.colorScheme.primary
    val codeBackground = colors.codeBackground

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) Spacer(modifier = Modifier.height(spacing.sm))
            when (block) {
                is MarkdownBlock.Heading -> Text(
                    text = applySearchHighlight(
                        AnnotatedString(block.text), searchQuery, isCurrentMatch, colors
                    ),
                    style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineSmall
                        2 -> MaterialTheme.typography.titleLarge
                        3 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleSmall
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("markdown_heading")
                )
                is MarkdownBlock.Paragraph -> Text(
                    text = applySearchHighlight(
                        renderInlineMarkdown(block.text, primaryColor, codeBackground),
                        searchQuery, isCurrentMatch, colors
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("markdown_paragraph")
                )
                is MarkdownBlock.ListItem -> Text(
                    text = applySearchHighlight(
                        buildAnnotatedString {
                            append("  ${block.bullet} ")
                            append(renderInlineMarkdown(block.text, primaryColor, codeBackground))
                        },
                        searchQuery, isCurrentMatch, colors
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(vertical = spacing.xs)
                        .testTag("markdown_list_item")
                )
                is MarkdownBlock.CodeFence -> CodeBlockWithCopy(
                    code = block.code,
                    modifier = Modifier
                        .padding(vertical = spacing.sm)
                        .testTag("markdown_code_fence")
                )
            }
        }
    }
}

/**
 * Applies search highlighting over an existing [AnnotatedString], preserving
 * any inline Markdown formatting spans already present.
 *
 * Returns the original string unchanged when [query] is blank or has no matches.
 */
fun applySearchHighlight(
    annotated: AnnotatedString,
    query: String,
    isCurrentMatch: Boolean,
    colors: ConversationColors
): AnnotatedString {
    if (query.isBlank()) return annotated

    val matches = findCaseInsensitiveMatches(annotated.text, query)
    if (matches.isEmpty()) return annotated

    val bgColor = if (isCurrentMatch) colors.currentMatchBackground else colors.searchHighlightBackground
    val fgColor = if (isCurrentMatch) colors.currentMatchText else colors.searchHighlightText
    val style = SpanStyle(background = bgColor, color = fgColor)

    return buildAnnotatedString {
        append(annotated)
        matches.forEach { range ->
            addStyle(style, range.first, range.last + 1)
        }
    }
}

/** Renders inline Markdown formatting (bold, italic, inline code, links-as-text). */
fun renderInlineMarkdown(
    text: String,
    linkColor: Color = Color.Unspecified,
    codeBackground: Color = Color.Unspecified
): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            // Inline code
            text[i] == '`' && i + 1 < text.length -> {
                val end = text.indexOf('`', i + 1)
                if (end > i) {
                    withStyle(SpanStyle(fontFamily = MonospaceFont, background = codeBackground)) {
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
            // Link [text](url) — render as "text" with themed primary colour
            text[i] == '[' -> {
                val closeBracket = text.indexOf(']', i + 1)
                if (closeBracket > i && closeBracket + 1 < text.length && text[closeBracket + 1] == '(') {
                    val closeParen = text.indexOf(')', closeBracket + 2)
                    if (closeParen > closeBracket) {
                        withStyle(SpanStyle(color = linkColor)) {
                            append(text.substring(i + 1, closeBracket))
                        }
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
