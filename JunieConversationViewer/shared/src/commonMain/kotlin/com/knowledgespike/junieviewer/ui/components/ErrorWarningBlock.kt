package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.selection.SelectionContainer
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/**
 * Renders error or warning messages with distinct visual treatment.
 * Uses accent colour plus a non-colour-only indicator (icon + label)
 * so the message does not blend into normal plain text.
 */
@Composable
fun ErrorWarningBlock(
    text: String,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    val containerColor = if (isWarning)
        colors.warningBackground
    else
        colors.errorBackground

    val contentColor = if (isWarning)
        MaterialTheme.colorScheme.onSurface
    else
        MaterialTheme.colorScheme.onError

    val indicator = if (isWarning) "⚠" else "✖"
    val label = if (isWarning) "Warning" else "Error"

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = RICH_CONTENT_SHAPE
    ) {
        Column(modifier = Modifier.padding(spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = indicator,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor
                )
                Spacer(modifier = Modifier.width(spacing.md))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor
                )
            }
            Spacer(modifier = Modifier.padding(top = spacing.sm))
            SelectionContainer(modifier = Modifier.testTag("selectable_error_warning_content")) {
                val convColors = JunieViewerTheme.conversationColors
                Text(
                    text = highlightSearchMatches(
                        text = text,
                        query = searchQuery,
                        isCurrentMatch = isCurrentMatch,
                        highlightBackground = convColors.searchHighlightBackground,
                        highlightText = convColors.searchHighlightText,
                        currentMatchBackground = convColors.currentMatchBackground,
                        currentMatchText = convColors.currentMatchText
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
        }
    }
}
