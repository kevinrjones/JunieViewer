package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.ui.looksLikeMarkdown
import com.knowledgespike.junieviewer.ui.messageKindLabel
import dev.snipme.highlights.model.SyntaxLanguage

// ---------------------------------------------------------------------------
// Unified message item — replaces duplicate HumanMessageItem / JunieMessageItem
// ---------------------------------------------------------------------------

/**
 * Compact, right-inset Human message card.
 */
@Composable
fun HumanMessageItem(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        MessageCard(
            message = message,
            senderLabel = "Human",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            maxCardWidth = 480.dp,
            innerPadding = 12.dp,
            testTagSuffix = "human"
        )
    }
}

/**
 * Full-width, left-inset Junie message card optimised for long-form reading.
 */
@Composable
fun JunieMessageItem(message: Message) {
    MessageCard(
        message = message,
        senderLabel = "Junie",
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        maxCardWidth = Dp.Unspecified,
        innerPadding = 16.dp,
        testTagSuffix = "junie"
    )
}

/**
 * Shared message card structure used by both Human and Junie message items.
 * Eliminates the duplicate composable structure (Finding 8).
 */
@Composable
private fun MessageCard(
    message: Message,
    senderLabel: String,
    containerColor: Color,
    maxCardWidth: Dp,
    innerPadding: Dp,
    testTagSuffix: String
) {
    val cardModifier = if (maxCardWidth != Dp.Unspecified) {
        Modifier.widthIn(max = maxCardWidth).testTag("message_item_$testTagSuffix")
    } else {
        Modifier.fillMaxWidth().testTag("message_item_$testTagSuffix")
    }

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = senderLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("sender_marker")
                )
                Text(
                    text = messageKindLabel(message.kind),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("message_kind_marker")
                )
            }
            Spacer(modifier = Modifier.height(if (innerPadding >= 16.dp) 8.dp else 4.dp))
            MessageBody(message = message)
        }
    }
}

// ---------------------------------------------------------------------------
// Visual header marking the start of a Junie Turn
// ---------------------------------------------------------------------------

@Composable
fun TurnHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("turn_header"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = "Junie Turn",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

// ---------------------------------------------------------------------------
// Message body — content-type dispatch (Finding 5: no nested when-in-when)
// ---------------------------------------------------------------------------

/**
 * Renders the body content of a Message based on its MessageKind and MessageContent type.
 * Each kind is handled in a single flat dispatch — no nested when or post-when escape hatches.
 */
@Composable
fun MessageBody(message: Message) {
    when (message.kind) {
        MessageKind.Thought -> ThoughtBlock(
            text = (message.content as? MessageContent.Text)?.text ?: "",
            modifier = Modifier.testTag("thought_block")
        )
        MessageKind.Error, MessageKind.Warning -> ErrorWarningBlock(
            text = when (val c = message.content) {
                is MessageContent.Text -> c.text
                else -> "Unknown error"
            },
            isWarning = message.kind == MessageKind.Warning,
            modifier = Modifier.testTag("error_warning_block")
        )
        MessageKind.Tool, MessageKind.Mcp -> ToolCallBlock(
            content = when (val c = message.content) {
                is MessageContent.Code -> c.code
                is MessageContent.Text -> c.text
                else -> ""
            },
            modifier = Modifier.testTag("tool_call_block")
        )
        MessageKind.Unsupported -> UnsupportedEventCard(message)
        else -> ContentRenderer(message.content, isMarkdownKind = message.kind == MessageKind.Markdown)
    }
}

/**
 * Renders the unsupported event indicator as a visually distinct warning card.
 */
@Composable
private fun UnsupportedEventCard(message: Message) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .testTag("unsupported_event_card"),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = (message.content as? MessageContent.Text)?.text ?: "Unsupported event",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(8.dp)
        )
    }
}

/**
 * Renders message content by type — Text, Code, Diff, Terminal, Structured.
 * Separated from MessageBody to keep the kind dispatch flat.
 */
@Composable
private fun ContentRenderer(content: MessageContent, isMarkdownKind: Boolean) {
    when (content) {
        is MessageContent.Text -> {
            if (isMarkdownKind || looksLikeMarkdown(content.text)) {
                MarkdownContent(
                    markdown = content.text,
                    modifier = Modifier.testTag("markdown_content")
                )
            } else {
                Text(
                    text = content.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("plain_text_content")
                )
            }
        }
        is MessageContent.Code -> CodeBlockWithCopy(
            code = content.code,
            language = when (content.language.lowercase()) {
                "json" -> SyntaxLanguage.JAVASCRIPT
                "bash", "sh" -> SyntaxLanguage.SHELL
                else -> SyntaxLanguage.KOTLIN
            },
            modifier = Modifier.testTag("code_block")
        )
        is MessageContent.Diff -> DiffBlock(
            diff = content.diff,
            modifier = Modifier.testTag("diff_block")
        )
        is MessageContent.Terminal -> TerminalOutputBlock(
            output = content.output,
            modifier = Modifier.testTag("terminal_block")
        )
        is MessageContent.Structured -> StructuredOutputBlock(
            data = content.data,
            modifier = Modifier.testTag("structured_output_block")
        )
    }
}
