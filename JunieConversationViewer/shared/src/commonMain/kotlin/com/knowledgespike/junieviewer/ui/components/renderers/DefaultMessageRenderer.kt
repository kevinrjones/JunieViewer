package com.knowledgespike.junieviewer.ui.components.renderers

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.ui.components.CodeBlockWithCopy
import com.knowledgespike.junieviewer.ui.components.CollapsibleBlock
import com.knowledgespike.junieviewer.ui.components.DiffBlock
import com.knowledgespike.junieviewer.ui.components.MarkdownContent
import com.knowledgespike.junieviewer.ui.components.StructuredOutputBlock
import com.knowledgespike.junieviewer.ui.components.TerminalOutputBlock
import com.knowledgespike.junieviewer.ui.components.TrackedSelectionContainer
import com.knowledgespike.junieviewer.ui.components.themedHighlightSearchMatches
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import dev.snipme.highlights.model.SyntaxLanguage

@Composable
fun DefaultMessageRenderer(
    message: Message,
    searchQuery: String,
    isCurrentMatch: Boolean,
    blockExpansionStates: Map<String, Boolean>,
    onToggleBlock: (String) -> Unit
) {
    ContentRenderer(
        content = message.content,
        messageId = message.id,
        isMarkdownKind = message.kind == MessageKind.Markdown,
        searchQuery = searchQuery,
        isCurrentMatch = isCurrentMatch,
        blockExpansionStates = blockExpansionStates,
        onToggleBlock = onToggleBlock
    )
}

/**
 * Renders message content by type — Text, Code, Diff, Terminal, Structured.
 * Separated from MessageBody to keep the kind dispatch flat.
 */
@Composable
private fun ContentRenderer(
    content: MessageContent,
    messageId: String,
    isMarkdownKind: Boolean,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false,
    blockExpansionStates: Map<String, Boolean> = emptyMap(),
    onToggleBlock: (String) -> Unit = {}
) {
    val colors = JunieViewerTheme.conversationColors
    when (content) {
        is MessageContent.Text -> {
            val blockId = "$messageId:text"
            CollapsibleBlock(
                label = "Text",
                backgroundColor = colors.codeBackground,
                borderColor = colors.codeBorder,
                headerTestTag = "text_block_header",
                bodyTestTag = "text_block_body",
                expanded = blockExpansionStates[blockId] ?: true,
                onToggle = { onToggleBlock(blockId) },
                body = {
                    if (isMarkdownKind || looksLikeMarkdown(content.text)) {
                        TrackedSelectionContainer(modifier = Modifier.testTag("selectable_message_text")) {
                            MarkdownContent(
                                markdown = content.text,
                                modifier = Modifier.testTag("markdown_content"),
                                searchQuery = searchQuery,
                                isCurrentMatch = isCurrentMatch
                            )
                        }
                    } else {
                        TrackedSelectionContainer(modifier = Modifier.testTag("selectable_message_text")) {
                            Text(
                                text = themedHighlightSearchMatches(
                                    text = content.text,
                                    query = searchQuery,
                                    isCurrentMatch = isCurrentMatch
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.testTag("plain_text_content")
                            )
                        }
                    }
                },
                modifier = Modifier.testTag("text_collapsible_block")
            )
        }
        is MessageContent.Code -> {
            val blockId = "$messageId:code"
            CodeBlockWithCopy(
                code = content.code,
                language = when (content.language.lowercase()) {
                    "json" -> SyntaxLanguage.JAVASCRIPT
                    "bash", "sh" -> SyntaxLanguage.SHELL
                    else -> SyntaxLanguage.KOTLIN
                },
                modifier = Modifier.testTag("code_block"),
                expanded = blockExpansionStates[blockId] ?: true,
                onToggle = { onToggleBlock(blockId) }
            )
        }
        is MessageContent.Diff -> {
            val blockId = "$messageId:diff"
            DiffBlock(
                diff = content.diff,
                modifier = Modifier.testTag("diff_block"),
                searchQuery = searchQuery,
                isCurrentMatch = isCurrentMatch,
                expanded = blockExpansionStates[blockId] ?: true,
                onToggle = { onToggleBlock(blockId) }
            )
        }
        is MessageContent.Terminal -> {
            val blockId = "$messageId:terminal"
            TerminalOutputBlock(
                output = content.output,
                modifier = Modifier.testTag("terminal_block"),
                searchQuery = searchQuery,
                isCurrentMatch = isCurrentMatch,
                expanded = blockExpansionStates[blockId] ?: true,
                onToggle = { onToggleBlock(blockId) }
            )
        }
        is MessageContent.Structured -> {
            val blockId = "$messageId:structured"
            StructuredOutputBlock(
                data = content.data,
                modifier = Modifier.testTag("structured_output_block"),
                searchQuery = searchQuery,
                isCurrentMatch = isCurrentMatch,
                expanded = blockExpansionStates[blockId] ?: true,
                onToggle = { onToggleBlock(blockId) }
            )
        }
    }
}

private fun looksLikeMarkdown(text: String): Boolean {
    val markers = listOf("# ", "* ", "- ", "1. ", "```", "|", "> ")
    return markers.any { text.contains(it) }
}
