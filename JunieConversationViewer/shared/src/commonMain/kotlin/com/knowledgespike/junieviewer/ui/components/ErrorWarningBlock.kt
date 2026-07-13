package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Renders error or warning messages with distinct visual treatment.
 * Uses accent colour plus a non-colour-only indicator (icon + label)
 * so the message does not blend into normal plain text.
 */
@Composable
fun ErrorWarningBlock(
    text: String,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isWarning)
        MaterialTheme.colorScheme.tertiaryContainer
    else
        MaterialTheme.colorScheme.errorContainer

    val contentColor = if (isWarning)
        MaterialTheme.colorScheme.onTertiaryContainer
    else
        MaterialTheme.colorScheme.onErrorContainer

    val icon = if (isWarning) "⚠️" else "❌"
    val label = if (isWarning) "Warning" else "Error"

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$icon $label",
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}
