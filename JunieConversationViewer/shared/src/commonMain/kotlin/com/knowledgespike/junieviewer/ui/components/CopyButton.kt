package com.knowledgespike.junieviewer.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString

/** Small text button that copies the given plain text to the system clipboard. */
@Composable
fun CopyButton(
    text: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    TextButton(
        onClick = { clipboardManager.setText(AnnotatedString(text)) },
        modifier = modifier.testTag("copy_button")
    ) {
        Text(
            text = "📋 Copy",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
