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
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import com.knowledgespike.junieviewer.ui.theme.MonospaceFont

/** Maximum height for terminal blocks to prevent infinite-height measurement in LazyColumn. */
private val TERMINAL_BLOCK_MAX_HEIGHT = 600.dp

/**
 * Renders terminal/shell output in a monospace block with preserved whitespace.
 * Command lines (starting with "$") are visually distinguished.
 * Includes a copy affordance for clean terminal text.
 */
@Composable
fun TerminalOutputBlock(
    output: String,
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
                text = "Terminal",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = output)
        }
        SelectionContainer(modifier = Modifier.testTag("selectable_terminal_content")) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = TERMINAL_BLOCK_MAX_HEIGHT)
                    .clip(RICH_CONTENT_SHAPE)
                    .border(width = RICH_CONTENT_BORDER_WIDTH, color = colors.codeBorder, shape = RICH_CONTENT_SHAPE)
                    .background(colors.terminalBackground)
                    .padding(spacing.md)
                    .horizontalScroll(rememberScrollState())
            ) {
                output.lines().forEach { line ->
                val isCommand = line.trimStart().startsWith("$")
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MonospaceFont
                    ),
                    color = if (isCommand) colors.terminalCommand else colors.terminalText,
                    modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs)
                )
                }
            }
        }
    }
}
