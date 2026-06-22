package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.knowledgespike.junieviewer.domain.SessionInfo
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Icon(
//                imageVector = Icons.Default.DateRange,
//                contentDescription = null,
//                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = session.id,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                
                val dateTime = try {
                    Instant.fromEpochMilliseconds(session.lastModified)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .toString()
                } catch (e: Exception) {
                    "Unknown"
                }
                
                Text(
                    text = "Last modified: $dateTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
