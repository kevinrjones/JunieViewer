package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.knowledgespike.junieviewer.ui.ConversationState
import com.knowledgespike.junieviewer.ui.formatTimestamp
import com.knowledgespike.junieviewer.ui.selectedSession
import com.knowledgespike.junieviewer.ui.selectedSessionId
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/**
 * Single-line footer showing Session metadata spread evenly across the width:
 * log name / Session id, date, and project / working directory.
 */
@Composable
fun SessionContextFooter(
    state: ConversationState,
    modifier: Modifier = Modifier
) {
    val session = state.selectedSession
    val spacing = JunieViewerTheme.spacing

    val sessionLabel = if (state.selectedSessionId != null) {
        "Session: ${state.selectedSessionId}"
    } else {
        "Session: —"
    }

    val dateLabel = if (session != null) {
        val millis = session.createdAt ?: session.lastModified
        val prefix = if (session.createdAt != null) "Created" else "Last modified"
        "$prefix: ${formatTimestamp(millis)}"
    } else {
        "Date: —"
    }

    val projectLabel = if (session?.workingDirectory != null) {
        "Project: ${session.workingDirectory}"
    } else {
        "Project: —"
    }

    Surface(
        modifier = modifier.fillMaxWidth().testTag("session_context_footer"),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.md)
        ) {
            Text(
                text = sessionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).testTag("session_footer_log_name")
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).testTag("session_footer_date")
            )
            Text(
                text = projectLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).testTag("session_footer_project")
            )
        }
    }
}
