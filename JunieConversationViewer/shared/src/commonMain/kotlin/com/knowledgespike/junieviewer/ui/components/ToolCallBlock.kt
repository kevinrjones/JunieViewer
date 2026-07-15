package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import com.knowledgespike.junieviewer.ui.theme.MonospaceFont

/** Shape for tool call header (top corners only). */
private val TOOL_CALL_HEADER_SHAPE = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)

/** Shape for tool call body (bottom corners only). */
private val TOOL_CALL_BODY_SHAPE = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)

/** Maximum height for tool call body to prevent infinite-height measurement in LazyColumn. */
private val TOOL_CALL_BODY_MAX_HEIGHT = 400.dp

/**
 * Renders a Tool Call summary with a collapsible body.
 * Header shows the tool name; body shows the structured content (JSON-style).
 * Collapsed by default for progressive disclosure.
 */
@Composable
fun ToolCallBlock(
    content: String,
    modifier: Modifier = Modifier
) {
    val toolName = extractToolName(content)
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    CollapsibleBlock(
        label = "Tool: $toolName",
        backgroundColor = colors.toolCallBackground,
        borderColor = colors.toolCallBorder,
        headerShape = TOOL_CALL_HEADER_SHAPE,
        headerTestTag = "tool_call_header",
        headerTrailing = {
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = content)
        },
        body = {
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = MonospaceFont
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = TOOL_CALL_BODY_MAX_HEIGHT)
                    .clip(TOOL_CALL_BODY_SHAPE)
                    .border(width = RICH_CONTENT_BORDER_WIDTH, color = colors.toolCallBorder, shape = TOOL_CALL_BODY_SHAPE)
                    .background(colors.codeBackground)
                    .padding(spacing.md)
                    .testTag("tool_call_body")
            )
        },
        modifier = modifier
    )
}

/** Extracts a tool name from the content, looking for common patterns. */
private fun extractToolName(content: String): String {
    // Try "Tool: <name>" pattern
    val toolLine = content.lines().firstOrNull { it.trimStart().startsWith("Tool:") }
    if (toolLine != null) return toolLine.substringAfter("Tool:").trim()

    // Try JSON "name" or "tool" field
    val nameMatch = Regex(""""(?:name|tool)"\s*:\s*"([^"]+)"""").find(content)
    if (nameMatch != null) return nameMatch.groupValues[1]

    return "Tool Call"
}
