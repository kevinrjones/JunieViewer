package com.knowledgespike.junieviewer.ui.components.renderers

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.ui.components.*
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

@Composable
fun MarkdownMessageRenderer(
    message: Message,
    searchQuery: String,
    isCurrentMatch: Boolean,
    blockExpansionStates: Map<String, Boolean>,
    onToggleBlock: (String) -> Unit
) {
    val mdText = (message.content as? MessageContent.Text)?.text ?: ""
    val colors = JunieViewerTheme.conversationColors
    val blockId = "${message.id}:markdown"
    CollapsibleBlock(
        label = "Markdown",
        backgroundColor = colors.codeBackground,
        borderColor = colors.codeBorder,
        headerTestTag = "markdown_block_header",
        bodyTestTag = "markdown_block_body",
        forceExpanded = isCurrentMatch && blockContainsSearchHit(mdText, searchQuery),
        externalExpanded = blockExpansionStates[blockId],
        onToggle = { onToggleBlock(blockId) },
        body = {
            TrackedSelectionContainer(modifier = Modifier.testTag("selectable_message_text")) {
                MarkdownContent(
                    markdown = mdText,
                    modifier = Modifier.testTag("markdown_content"),
                    searchQuery = searchQuery,
                    isCurrentMatch = isCurrentMatch
                )
            }
        },
        modifier = Modifier.testTag("markdown_collapsible_block")
    )
}
