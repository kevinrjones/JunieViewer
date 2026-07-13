package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.ui.ConversationState
import com.knowledgespike.junieviewer.ui.formatTimestamp

/**
 * Persistent Session context header showing Session id and timestamp.
 * Displayed in the top bar when a Session is selected.
 */
@Composable
fun SessionContextHeader(
    state: ConversationState,
    modifier: Modifier = Modifier
) {
    val session = state.selectedSession
    val timestampLabel = if (session != null) {
        val millis = session.createdAt ?: session.lastModified
        val label = if (session.createdAt != null) "Created" else "Last modified"
        val formatted = formatTimestamp(millis)
        "$label: $formatted"
    } else null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("session_context_header"),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(
                text = "Session: ${state.selectedSessionId.orEmpty()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (timestampLabel != null) {
                Text(
                    text = timestampLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (session?.workingDirectory != null) {
                Text(
                    text = "Project: ${session.workingDirectory}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
