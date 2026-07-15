package com.knowledgespike.junieviewer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/**
 * Shared collapsible block structure used by ThoughtBlock and ToolCallBlock.
 * Renders a clickable header with expand/collapse indicator and label,
 * plus an animated body that appears when expanded.
 */
@Composable
fun CollapsibleBlock(
    label: String,
    backgroundColor: Color,
    borderColor: Color,
    headerShape: Shape = RICH_CONTENT_SHAPE,
    headerTestTag: String,
    headerTrailing: @Composable RowScope.() -> Unit = {},
    body: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val spacing = JunieViewerTheme.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(headerShape)
                .border(width = RICH_CONTENT_BORDER_WIDTH, color = borderColor, shape = headerShape)
                .background(backgroundColor)
                .clickable { expanded = !expanded }
                .padding(horizontal = spacing.lg, vertical = spacing.md)
                .testTag(headerTestTag),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (expanded) "▼" else "▶",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(spacing.md))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            headerTrailing()
        }
        AnimatedVisibility(visible = expanded) {
            body()
        }
    }
}
