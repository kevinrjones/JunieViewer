package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.knowledgespike.junieviewer.domain.FilterCategory
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/**
 * Renders a small coloured indicator and label for a [MessageKind].
 */
@Composable
fun MessageKindMarker(
    kind: MessageKind,
    modifier: Modifier = Modifier
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    val dotColor = when (kind.filterCategory) {
        FilterCategory.Human -> colors.humanAccent
        FilterCategory.Junie -> colors.junieAccent
        FilterCategory.Thought -> colors.thoughtBorder
        FilterCategory.Tool -> colors.toolCallBorder
        FilterCategory.Patch -> colors.diffAddedText
        FilterCategory.Terminal -> colors.terminalText
        FilterCategory.AlwaysShow -> colors.junieAccent
    }

    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = kind.label
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(spacing.md)
                .clip(CircleShape)
                .background(dotColor)
                .testTag("message_kind_dot")
        )
        Text(
            text = kind.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("message_kind_label")
        )
    }
}
