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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import com.knowledgespike.junieviewer.ui.theme.RICH_CONTENT_BORDER_WIDTH
import com.knowledgespike.junieviewer.ui.theme.RICH_CONTENT_SHAPE

/**
 * Shared collapsible block structure used by rich content blocks.
 * Renders a clickable header with expand/collapse indicator and label,
 * plus an animated body that appears when expanded.
 *
 * Expansion state is fully owned by the ViewModel (Area 6). This component
 * receives a ready-to-render [expanded] boolean and dispatches toggle actions
 * via [onToggle].
 *
 * @param expanded whether the block body is currently visible.
 * @param onToggle callback for manual expand/collapse clicks, dispatches to ViewModel.
 * @param bodyTestTag stable test tag applied to the body wrapper.
 */
@Composable
fun CollapsibleBlock(
    label: String,
    backgroundColor: Color,
    borderColor: Color,
    headerShape: Shape = RICH_CONTENT_SHAPE,
    headerTestTag: String,
    bodyTestTag: String = "",
    expanded: Boolean = true,
    onToggle: () -> Unit = {},
    headerTrailing: @Composable RowScope.() -> Unit = {},
    body: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = JunieViewerTheme.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(headerShape)
                .border(width = RICH_CONTENT_BORDER_WIDTH, color = borderColor, shape = headerShape)
                .background(backgroundColor)
                .clickable { onToggle() }
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
        AnimatedVisibility(
            visible = expanded,
            modifier = if (bodyTestTag.isNotEmpty()) Modifier.testTag(bodyTestTag) else Modifier
        ) {
            body()
        }
    }
}
