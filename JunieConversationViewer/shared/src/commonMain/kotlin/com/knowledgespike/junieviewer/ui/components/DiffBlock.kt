package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.selection.SelectionContainer
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import com.knowledgespike.junieviewer.ui.theme.MonospaceFont

/**
 * Renders unified diff / Patch content in a collapsible block.
 *
 * Features:
 * - Expanded by default; full Patch content visible without truncation.
 * - Copy button copies the original unified diff text.
 * - Search highlighting applied in the inline view.
 * - Side-by-side diff view is deferred for future work.
 */
@Composable
fun DiffBlock(
    diff: String,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false,
    forceExpanded: Boolean = false
) {
    val colors = JunieViewerTheme.conversationColors

    CollapsibleBlock(
        label = "Patch",
        backgroundColor = colors.codeBackground,
        borderColor = colors.codeBorder,
        headerTestTag = "patch_block_header",
        bodyTestTag = "patch_block_body",
        forceExpanded = forceExpanded,
        headerTrailing = {
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = diff)
        },
        body = {
            SelectionContainer(modifier = Modifier.testTag("selectable_diff_content")) {
                InlineDiffView(
                    diff = diff,
                    searchQuery = searchQuery,
                    isCurrentMatch = isCurrentMatch,
                    modifier = Modifier.testTag("patch_inline_view")
                )
            }
        },
        modifier = modifier
    )
}

/** Inline / unified diff view — the original Patch rendering without height truncation. */
@Composable
private fun InlineDiffView(
    diff: String,
    searchQuery: String,
    isCurrentMatch: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RICH_CONTENT_SHAPE)
            .border(width = RICH_CONTENT_BORDER_WIDTH, color = colors.codeBorder, shape = RICH_CONTENT_SHAPE)
            .background(colors.codeBackground)
            .padding(spacing.md)
            .horizontalScroll(rememberScrollState())
    ) {
        diff.lines().forEach { line ->
            val (bgColor, textColor) = diffLineColors(line, colors)
            Text(
                text = highlightSearchMatches(
                    text = line,
                    query = searchQuery,
                    isCurrentMatch = isCurrentMatch,
                    highlightBackground = colors.searchHighlightBackground,
                    highlightText = colors.searchHighlightText,
                    currentMatchBackground = colors.currentMatchBackground,
                    currentMatchText = colors.currentMatchText
                ),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonospaceFont),
                color = textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = spacing.sm, vertical = spacing.xs)
            )
        }
    }
}

/** Returns background and text colours for a unified diff line. */
@Composable
private fun diffLineColors(
    line: String,
    colors: com.knowledgespike.junieviewer.ui.theme.ConversationColors
): Pair<Color, Color> = when {
    line.startsWith("+") && !line.startsWith("+++") -> colors.diffAdded to colors.diffAddedText
    line.startsWith("-") && !line.startsWith("---") -> colors.diffRemoved to colors.diffRemovedText
    line.startsWith("@@") -> colors.diffHunkHeader to MaterialTheme.colorScheme.onSurface
    else -> Color.Transparent to MaterialTheme.colorScheme.onSurface
}
