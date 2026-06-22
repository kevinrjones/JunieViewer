package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun FatalErrorDialog(
    throwable: Throwable,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        // We can't set the title in common code easily if we want to keep it multiplatform,
        // but on Desktop it might be available.
        // For now, let's use a standard Dialog and a Surface to ensure we control the content.
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.width(400.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Unexpected Application Error",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                Text("The application encountered a technical difficulty that it couldn't recover from. The details have been recorded in the log files to assist with troubleshooting.")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = throwable.message ?: throwable.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = onDismiss) {
                        Text("Close Application")
                    }
                }
            }
        }
    }
}
