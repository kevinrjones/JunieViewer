package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/**
 * Renders error or warning messages in a collapsible block with distinct visual treatment.
 * Expanded by default. Uses accent colour plus a non-colour-only label.
 */
@Composable
fun ErrorWarningBlock(
    text: String,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false,
    forceExpanded: Boolean = false
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    val containerColor = if (isWarning) colors.warningBackground else colors.errorBackground
    val borderColor = if (isWarning) colors.warningBackground else colors.errorBackground
    val label = if (isWarning) "Warning" else "Error"

    CollapsibleBlock(
        label = label,
        backgroundColor = containerColor,
        borderColor = borderColor,
        headerTestTag = "error_warning_block_header",
        bodyTestTag = "error_warning_block_body",
        forceExpanded = forceExpanded,
        body = {
            TrackedSelectionContainer(modifier = Modifier.testTag("selectable_error_warning_content")) {
                Text(
                    text = themedHighlightSearchMatches(
                        text = text,
                        query = searchQuery,
                        isCurrentMatch = isCurrentMatch
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg)
                )
            }
        },
        modifier = modifier
    )
}
