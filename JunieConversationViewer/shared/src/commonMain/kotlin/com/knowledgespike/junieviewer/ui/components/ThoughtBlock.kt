package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.text.selection.SelectionContainer
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/**
 * Renders a Thought message de-emphasised and collapsible.
 * Expanded by default; supports forced expansion for Search auto-expand.
 */
@Composable
fun ThoughtBlock(
    text: String,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false,
    forceExpanded: Boolean = false
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    CollapsibleBlock(
        label = "Thought",
        backgroundColor = colors.thoughtBackground,
        borderColor = colors.thoughtBorder,
        headerTestTag = "thought_header",
        bodyTestTag = "thought_block_body",
        forceExpanded = forceExpanded,
        headerTrailing = {
            Spacer(modifier = Modifier.width(spacing.md))
            Text(
                text = text.take(80) + if (text.length > 80) "…" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontStyle = FontStyle.Italic,
                maxLines = 1
            )
        },
        body = {
            SelectionContainer(modifier = Modifier.testTag("selectable_thought_content")) {
                Text(
                    text = themedHighlightSearchMatches(
                        text = text,
                        query = searchQuery,
                        isCurrentMatch = isCurrentMatch
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.lg, vertical = spacing.md)
                        .testTag("thought_body")
                )
            }
        },
        modifier = modifier
    )
}
