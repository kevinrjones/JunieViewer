package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import com.knowledgespike.junieviewer.ui.theme.MonospaceFont

/**
 * Renders terminal/shell output in a collapsible monospace block.
 * Command lines (starting with "$") are visually distinguished.
 * Expanded by default; full content visible without truncation.
 */
@Composable
fun TerminalOutputBlock(
    output: String,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false,
    forceExpanded: Boolean = false,
    externalExpanded: Boolean? = null,
    onToggle: (() -> Unit)? = null
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    CollapsibleBlock(
        label = "Terminal",
        backgroundColor = colors.terminalBackground,
        borderColor = colors.codeBorder,
        headerTestTag = "terminal_block_header",
        bodyTestTag = "terminal_block_body",
        forceExpanded = forceExpanded,
        externalExpanded = externalExpanded,
        onToggle = onToggle,
        headerTrailing = {
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = output)
        },
        body = {
            TrackedSelectionContainer(modifier = Modifier.testTag("selectable_terminal_content")) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RICH_CONTENT_SHAPE)
                        .border(width = RICH_CONTENT_BORDER_WIDTH, color = colors.codeBorder, shape = RICH_CONTENT_SHAPE)
                        .background(colors.terminalBackground)
                        .padding(spacing.md)
                        .horizontalScroll(rememberScrollState())
                ) {
                    output.lines().forEach { line ->
                        val isCommand = line.trimStart().startsWith("$")
                        Text(
                            text = themedHighlightSearchMatches(
                        text = line,
                        query = searchQuery,
                        isCurrentMatch = isCurrentMatch
                    ),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = MonospaceFont
                            ),
                            color = if (isCommand) colors.terminalCommand else colors.terminalText,
                            modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs)
                        )
                    }
                }
            }
        },
        modifier = modifier
    )
}
