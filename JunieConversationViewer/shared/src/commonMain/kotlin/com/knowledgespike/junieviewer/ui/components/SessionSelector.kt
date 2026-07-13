package com.knowledgespike.junieviewer.ui.components

// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.knowledgespike.junieviewer.domain.SessionInfo
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
fun SessionSelector(
    sessions: List<SessionInfo>,
    selectedSessionId: String?,
    onSessionSelected: (SessionInfo) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Text(
                    text = "Select Session",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
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
                        .padding(top = 16.dp),
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
 * Formats an epoch-millis timestamp as a human-readable local date/time string.
 */
private fun formatTimestamp(epochMillis: Long): String {
    return try {
        val tz = try { TimeZone.currentSystemDefault() } catch (_: Throwable) { TimeZone.UTC }
        Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(tz)
            .toString()
            .replace("T", " ")
            .substringBefore(".")
    } catch (_: Throwable) {
        "Unknown"
    }
}

/**
 * A single row in the Session selector list, showing the session id,
 * directory context, and the best available timestamp.
 */
@Composable
fun SessionItem(
    session: SessionInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Session id
            Text(
                text = session.id,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            // Working directory — the directory Junie was operating in
            if (!session.workingDirectory.isNullOrBlank()) {
                Text(
                    text = "Project: ${session.workingDirectory}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
