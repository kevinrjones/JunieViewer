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
 * Collapsed by default for progressive disclosure — thoughts are internal
 * reasoning and less important than primary Junie response content.
 */
@Composable
fun ThoughtBlock(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    CollapsibleBlock(
        label = "Thought",
        backgroundColor = colors.thoughtBackground,
        borderColor = colors.thoughtBorder,
        headerTestTag = "thought_header",
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
                    text = text,
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
