package com.knowledgespike.junieviewer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    var expanded by remember { mutableStateOf(false) }
    val toolName = extractToolName(content)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("tool_call_header"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (expanded) "▼" else "▶",
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🔧 $toolName",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = content)
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
                    .testTag("tool_call_body")
            )
        }
    }
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
