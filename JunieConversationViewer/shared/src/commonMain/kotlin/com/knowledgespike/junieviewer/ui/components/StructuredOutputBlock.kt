package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import com.knowledgespike.junieviewer.ui.theme.MonospaceFont

/** Maximum height for structured output blocks to prevent infinite-height measurement in LazyColumn. */
private val STRUCTURED_OUTPUT_MAX_HEIGHT = 400.dp

/**
 * Renders structured output (JSON-like or key-value data) in a readable monospace block.
 * Unsupported structures degrade to readable text.
 * Includes a copy affordance for clean text.
 */
@Composable
fun StructuredOutputBlock(
    data: String,
    modifier: Modifier = Modifier
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Structured Output",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = data)
        }
        Text(
            text = data,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = MonospaceFont
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = STRUCTURED_OUTPUT_MAX_HEIGHT)
                .clip(RICH_CONTENT_SHAPE)
                .border(width = RICH_CONTENT_BORDER_WIDTH, color = colors.codeBorder, shape = RICH_CONTENT_SHAPE)
                .background(colors.codeBackground)
                .padding(spacing.md)
                .horizontalScroll(rememberScrollState())
        )
    }
}
