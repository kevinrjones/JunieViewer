package com.knowledgespike.junieviewer.ui.components.renderers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

@Composable
fun UnsupportedMessageRenderer(
    message: Message,
    searchQuery: String,
    isCurrentMatch: Boolean,
    blockExpansionStates: Map<String, Boolean>,
    onToggleBlock: (String) -> Unit
) {
    val spacing = JunieViewerTheme.spacing
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.sm)
            .testTag("unsupported_event_card"),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = (message.content as? MessageContent.Text)?.text ?: "Unsupported event",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(spacing.md)
        )
    }
}
