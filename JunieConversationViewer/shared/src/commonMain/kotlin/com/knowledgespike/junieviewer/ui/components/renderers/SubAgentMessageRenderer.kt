package com.knowledgespike.junieviewer.ui.components.renderers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.ui.components.*
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

@Composable
fun SubAgentMessageRenderer(
    message: Message,
    searchQuery: String,
    isCurrentMatch: Boolean,
    blockExpansionStates: Map<String, Boolean>,
    onToggleBlock: (String) -> Unit
) {
    val subAgentText = (message.content as? MessageContent.Text)?.text ?: ""
    val colors = JunieViewerTheme.conversationColors
    val blockId = "${message.id}:subagent"
    CollapsibleBlock(
        label = "Sub-Agent",
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        borderColor = MaterialTheme.colorScheme.tertiary,
        headerTestTag = "sub_agent_block_header",
        bodyTestTag = "sub_agent_block_body",
        forceExpanded = isCurrentMatch && blockContainsSearchHit(subAgentText, searchQuery),
        externalExpanded = blockExpansionStates[blockId],
        onToggle = { onToggleBlock(blockId) },
        body = {
            TrackedSelectionContainer(modifier = Modifier.testTag("selectable_sub_agent_content")) {
                Text(
                    text = themedHighlightSearchMatches(
                        text = subAgentText,
                        query = searchQuery,
                        isCurrentMatch = isCurrentMatch
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(JunieViewerTheme.spacing.lg)
                        .testTag("sub_agent_body")
                )
            }
        },
        modifier = Modifier.testTag("sub_agent_collapsible_block")
    )
}
