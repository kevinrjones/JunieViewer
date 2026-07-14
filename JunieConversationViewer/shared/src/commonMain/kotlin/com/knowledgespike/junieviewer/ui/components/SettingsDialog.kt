package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.knowledgespike.junieviewer.ui.theme.ThemeMode

/**
 * Settings dialog with Junie home path configuration and theme mode selector.
 */
@Composable
fun SettingsDialog(
    currentHomePath: String,
    currentThemeMode: ThemeMode,
    onHomePathChange: (String) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
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

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(modifier = Modifier.selectableGroup()) {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentThemeMode == mode,
                                    onClick = { onThemeModeChange(mode) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp)
                                .testTag("theme_mode_${mode.name.lowercase()}")
                                .semantics { contentDescription = "${mode.name} theme" },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentThemeMode == mode,
                                onClick = null
                            )
                            Text(
                                text = mode.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

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
