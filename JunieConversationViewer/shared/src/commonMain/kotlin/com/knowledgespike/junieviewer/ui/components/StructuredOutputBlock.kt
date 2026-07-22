package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
    expanded: Boolean = true,
    onToggle: () -> Unit = {}
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    CollapsibleBlock(
        label = "Structured Output",
        backgroundColor = colors.codeBackground,
        borderColor = colors.codeBorder,
        headerTestTag = "structured_output_block_header",
        bodyTestTag = "structured_output_block_body",
        expanded = expanded,
        onToggle = onToggle,
        headerTrailing = {
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = data)
        },
        body = {
            TrackedSelectionContainer(modifier = Modifier.testTag("selectable_structured_content")) {
                Text(
                    text = themedHighlightSearchMatches(
                        text = data,
                        query = searchQuery,
                        isCurrentMatch = isCurrentMatch
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MonospaceFont
                    ),
                    modifier = Modifier.richContentBox(
                        backgroundColor = colors.codeBackground,
                        borderColor = colors.codeBorder,
                        padding = spacing.md,
                        scrollable = true
                    )
                )
            }
        },
        modifier = modifier
    )
}
