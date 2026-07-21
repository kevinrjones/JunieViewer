package com.knowledgespike.junieviewer.ui.components.renderers

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.ui.components.ThoughtBlock
import com.knowledgespike.junieviewer.ui.components.blockContainsSearchHit

@Composable
fun ThoughtMessageRenderer(
    message: Message,
    searchQuery: String,
    isCurrentMatch: Boolean,
    blockExpansionStates: Map<String, Boolean>,
    onToggleBlock: (String) -> Unit
) {
    val thoughtText = (message.content as? MessageContent.Text)?.text ?: ""
    val blockId = "${message.id}:thought"
    ThoughtBlock(
        text = thoughtText,
        modifier = Modifier.testTag("thought_block"),
        searchQuery = searchQuery,
        isCurrentMatch = isCurrentMatch,
        forceExpanded = isCurrentMatch && blockContainsSearchHit(thoughtText, searchQuery),
        externalExpanded = blockExpansionStates[blockId],
        onToggle = { onToggleBlock(blockId) }
    )
}
