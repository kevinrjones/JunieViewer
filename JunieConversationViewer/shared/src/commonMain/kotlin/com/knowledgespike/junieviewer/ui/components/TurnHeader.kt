package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/**
 * Themed divider header marking the start of a Junie Turn in the conversation.
 */
@Composable
fun TurnHeader() {
    val spacing = JunieViewerTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.sm)
            .testTag("turn_header")
            .semantics { heading() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = JunieViewerTheme.conversationColors.junieAccent.copy(alpha = 0.4f)
        )
        Text(
            text = "Junie Turn",
            style = MaterialTheme.typography.titleMedium,
            color = JunieViewerTheme.conversationColors.junieAccent,
            modifier = Modifier.padding(horizontal = spacing.lg)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = JunieViewerTheme.conversationColors.junieAccent.copy(alpha = 0.4f)
        )
    }
}
