package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
    expanded: Boolean = true,
    onToggle: () -> Unit = {}
) {
    val colors = JunieViewerTheme.conversationColors

    CollapsibleBlock(
        label = "Patch",
        backgroundColor = colors.codeBackground,
        borderColor = colors.codeBorder,
        headerTestTag = "patch_block_header",
        bodyTestTag = "patch_block_body",
        expanded = expanded,
        onToggle = onToggle,
        headerTrailing = {
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = diff)
        },
        body = {
            TrackedSelectionContainer(modifier = Modifier.testTag("selectable_diff_content")) {
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
        modifier = modifier.richContentBox(
            backgroundColor = colors.codeBackground,
            borderColor = colors.codeBorder,
            padding = spacing.md,
            scrollable = true
        )
    ) {
        diff.lines().forEach { line ->
            val (bgColor, textColor) = diffLineColors(line, colors)
            Text(
                text = themedHighlightSearchMatches(
                        text = line,
                        query = searchQuery,
                        isCurrentMatch = isCurrentMatch
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
