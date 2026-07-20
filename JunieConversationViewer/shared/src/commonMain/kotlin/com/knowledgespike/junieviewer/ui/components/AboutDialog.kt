package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Simple About dialog showing application name and version information.
 */
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier.testTag("about_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Junie Conversation Viewer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "A desktop viewer for Junie Session conversations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Built with Compose Multiplatform",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("about_dialog_close")
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * Simple How to Use dialog with basic usage instructions.
 */
@Composable
fun HowToUseDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(max = 480.dp)
                .testTag("how_to_use_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "How to Use",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                val instructions = listOf(
                    "Open Session" to "Use File > Open Session or the toolbar button to select a Session.",
                    "Search Messages" to "Type in the Search field to filter Messages. Use Find Next/Previous to navigate matches.",
                    "Filter by Kind" to "Use the filter chips below the toolbar to show/hide Messages by Sender or Kind.",
                    "Refresh" to "Use File > Refresh or the toolbar button to reload the current Session from disk.",
                    "Auto-Refresh" to "Toggle auto-refresh to control live tracking of Session updates.",
                    "Collapse/Show All" to "Use View > Collapse All or Show All to control collapsible content blocks."
                )

                instructions.forEach { (title, description) ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("how_to_use_dialog_close")
                ) {
                    Text("Close")
                }
            }
        }
    }
}
