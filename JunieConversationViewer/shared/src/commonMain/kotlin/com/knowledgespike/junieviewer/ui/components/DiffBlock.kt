package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import com.knowledgespike.junieviewer.ui.theme.MonospaceFont

/** Maximum height for diff blocks to prevent infinite-height measurement in LazyColumn. */
private val DIFF_BLOCK_MAX_HEIGHT = 600.dp

/**
 * Renders unified diff content with added/removed line styling.
 * Added lines use a green-tinted background with "+" prefix.
 * Removed lines use a red-tinted background with "-" prefix.
 * Includes a copy affordance for clean diff text.
 */
@Composable
fun DiffBlock(
    diff: String,
    modifier: Modifier = Modifier
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Diff",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = diff)
        }
        SelectionContainer(modifier = Modifier.testTag("selectable_diff_content")) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = DIFF_BLOCK_MAX_HEIGHT)
                    .clip(RICH_CONTENT_SHAPE)
                    .border(width = RICH_CONTENT_BORDER_WIDTH, color = colors.codeBorder, shape = RICH_CONTENT_SHAPE)
                    .background(colors.codeBackground)
                    .padding(spacing.md)
                    .horizontalScroll(rememberScrollState())
            ) {
                diff.lines().forEach { line ->
                val (bgColor, textColor) = when {
                    line.startsWith("+") && !line.startsWith("+++") ->
                        colors.diffAdded to colors.diffAddedText
                    line.startsWith("-") && !line.startsWith("---") ->
                        colors.diffRemoved to colors.diffRemovedText
                    line.startsWith("@@") ->
                        colors.diffHunkHeader to MaterialTheme.colorScheme.onSurface
                    else -> Color.Transparent to MaterialTheme.colorScheme.onSurface
                }
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MonospaceFont
                    ),
                    color = textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                        .padding(horizontal = spacing.sm, vertical = spacing.xs)
                )
                }
            }
        }
    }
}
