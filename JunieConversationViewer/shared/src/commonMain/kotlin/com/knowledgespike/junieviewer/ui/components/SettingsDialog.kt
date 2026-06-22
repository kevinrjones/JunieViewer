package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun SettingsDialog(
    currentHomePath: String,
    onHomePathChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var path by remember { mutableStateOf(currentHomePath) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("Junie Home Path") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Default: ~/.junie") }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onHomePathChange(path)
                            onDismiss()
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
