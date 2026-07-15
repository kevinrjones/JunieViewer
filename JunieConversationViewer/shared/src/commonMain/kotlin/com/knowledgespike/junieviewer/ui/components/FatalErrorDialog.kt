package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/** Dialog shown when the application encounters an unrecoverable error. */
@Composable
fun FatalErrorDialog(
    throwable: Throwable,
    onDismiss: () -> Unit
) {
    val spacing = JunieViewerTheme.spacing

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.testTag("fatal_error_dialog")
        ) {
            Column(modifier = Modifier.padding(spacing.xl)) {
                Text(
                    text = "Unexpected Application Error",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.testTag("fatal_error_message")
                )
                Spacer(Modifier.height(spacing.lg))
                Text(
                    "The application encountered a technical difficulty that it couldn't recover from. The details have been recorded in the log files to assist with troubleshooting.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.height(spacing.md))
                Text(
                    text = throwable.message ?: throwable.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(spacing.xl))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("fatal_error_close_button")
                    ) {
                        Text("Close Application")
                    }
                }
            }
        }
    }
}
