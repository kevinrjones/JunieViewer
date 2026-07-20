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
 * Shared collapsible block structure used by rich content blocks.
 * Renders a clickable header with expand/collapse indicator and label,
 * plus an animated body that appears when expanded.
 *
 * @param initiallyExpanded whether the block starts expanded (default true).
 *   Used only when [externalExpanded] is null (no ViewModel-managed state).
 * @param forceExpanded when true the body is shown regardless of manual state,
 *   used for Search auto-expansion. Takes priority over global collapse state.
 * @param externalExpanded ViewModel-managed expansion state for this block.
 *   When non-null, overrides [initiallyExpanded] and local remember state.
 * @param onToggle callback for manual expand/collapse clicks. When non-null,
 *   delegates toggle to the ViewModel instead of local state.
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
    initiallyExpanded: Boolean = true,
    forceExpanded: Boolean = false,
    externalExpanded: Boolean? = null,
    onToggle: (() -> Unit)? = null,
    headerTrailing: @Composable RowScope.() -> Unit = {},
    body: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    // When external state is provided, use it; otherwise fall back to local remember.
    var localExpanded by remember { mutableStateOf(initiallyExpanded) }
    val manualExpanded = externalExpanded ?: localExpanded

    // Track whether the user explicitly collapsed while forceExpanded was active.
    var userDismissedForce by remember { mutableStateOf(false) }
    // Reset the dismissal flag when forceExpanded becomes false (e.g. search cleared).
    if (!forceExpanded) {
        userDismissedForce = false
    }
    val visibleExpanded = manualExpanded || (forceExpanded && !userDismissedForce)
    val spacing = JunieViewerTheme.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(headerShape)
                .border(width = RICH_CONTENT_BORDER_WIDTH, color = borderColor, shape = headerShape)
                .background(backgroundColor)
                .clickable {
                    if (onToggle != null) {
                        onToggle()
                    } else {
                        localExpanded = !localExpanded
                    }
                    // If collapsing while force-expanded, dismiss the force so block actually collapses.
                    if (manualExpanded && forceExpanded) {
                        userDismissedForce = true
                    }
                }
                .padding(horizontal = spacing.lg, vertical = spacing.md)
                .testTag(headerTestTag),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (visibleExpanded) "▼" else "▶",
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
            visible = visibleExpanded,
            modifier = if (bodyTestTag.isNotEmpty()) Modifier.testTag(bodyTestTag) else Modifier
        ) {
            body()
        }
    }
}
