package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.knowledgespike.junieviewer.domain.SessionInfo
import com.knowledgespike.junieviewer.ui.formatTimestamp
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/**
 * Dialog listing available Sessions for selection.
 * Uses themed spacing and improved density for compact browsing.
 */
@Composable
fun SessionSelector(
    sessions: List<SessionInfo>,
    selectedSessionId: String?,
    onSessionSelected: (SessionInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val spacing = JunieViewerTheme.spacing

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .padding(spacing.xxl)
            ) {
                Text(
                    text = "Select Session",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = spacing.xl)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    items(sessions) { session ->
                        SessionItem(
                            session = session,
                            isSelected = session.id == selectedSessionId,
                            onClick = { onSessionSelected(session) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.xl),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

/**
 * A single row in the Session selector list, showing the Session id,
 * directory context, and the best available timestamp.
 * Selected Session is visually highlighted with primary container colour.
 */
@Composable
fun SessionItem(
    session: SessionInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val spacing = JunieViewerTheme.spacing

    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = spacing.lg, vertical = spacing.md)
                .fillMaxWidth()
        ) {
            // Session id
            Text(
                text = session.id,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            // Working directory — the directory Junie was operating in
            if (!session.workingDirectory.isNullOrBlank()) {
                Text(
                    text = "Project: ${session.workingDirectory}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = spacing.xs)
                )
            }

            // Timestamp — prefer created time when available, fall back to last modified
            val timestampLabel = if (session.createdAt != null && session.createdAt > 0L) {
                "Created: ${formatTimestamp(session.createdAt)}"
            } else if (session.lastModified > 0L) {
                "Last modified: ${formatTimestamp(session.lastModified)}"
            } else {
                null
            }

            if (timestampLabel != null) {
                Text(
                    text = timestampLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = spacing.xs)
                )
            }
        }
    }
}
