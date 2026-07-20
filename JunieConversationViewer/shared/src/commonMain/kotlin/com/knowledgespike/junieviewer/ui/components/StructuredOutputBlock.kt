package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.selection.SelectionContainer
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import com.knowledgespike.junieviewer.ui.theme.MonospaceFont

/**
 * Renders structured output (JSON-like or key-value data) in a collapsible monospace block.
 * Expanded by default; full content visible without truncation.
 */
@Composable
fun StructuredOutputBlock(
    data: String,
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
        label = "Structured Output",
        backgroundColor = colors.codeBackground,
        borderColor = colors.codeBorder,
        headerTestTag = "structured_output_block_header",
        bodyTestTag = "structured_output_block_body",
        forceExpanded = forceExpanded,
        externalExpanded = externalExpanded,
        onToggle = onToggle,
        headerTrailing = {
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = data)
        },
        body = {
            SelectionContainer(modifier = Modifier.testTag("selectable_structured_content")) {
                Text(
                    text = themedHighlightSearchMatches(
                        text = data,
                        query = searchQuery,
                        isCurrentMatch = isCurrentMatch
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MonospaceFont
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RICH_CONTENT_SHAPE)
                        .border(width = RICH_CONTENT_BORDER_WIDTH, color = colors.codeBorder, shape = RICH_CONTENT_SHAPE)
                        .background(colors.codeBackground)
                        .padding(spacing.md)
                        .horizontalScroll(rememberScrollState())
                )
            }
        },
        modifier = modifier
    )
}
