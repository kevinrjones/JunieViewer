package com.knowledgespike.junieviewer.ui.components.renderers

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.ui.components.ToolCallBlock
import com.knowledgespike.junieviewer.ui.components.blockContainsSearchHit

@Composable
fun ToolMessageRenderer(
    message: Message,
    searchQuery: String,
    isCurrentMatch: Boolean,
    blockExpansionStates: Map<String, Boolean>,
    onToggleBlock: (String) -> Unit
) {
    val toolText = when (val c = message.content) {
        is MessageContent.Code -> c.code
        is MessageContent.Text -> c.text
        else -> ""
    }
    val blockId = "${message.id}:tool"
    ToolCallBlock(
        content = toolText,
        modifier = Modifier.testTag("tool_call_block"),
        searchQuery = searchQuery,
        isCurrentMatch = isCurrentMatch,
        forceExpanded = isCurrentMatch && blockContainsSearchHit(toolText, searchQuery),
        externalExpanded = blockExpansionStates[blockId],
        onToggle = { onToggleBlock(blockId) }
    )
}
