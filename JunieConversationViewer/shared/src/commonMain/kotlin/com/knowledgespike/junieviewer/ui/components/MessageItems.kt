package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.ui.looksLikeMarkdown
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import dev.snipme.highlights.model.SyntaxLanguage

// ---------------------------------------------------------------------------
// Layout constants for message card readability
// ---------------------------------------------------------------------------

/** Fraction of available width for Human message cards — keeps them compact and left-aligned. */
private const val HUMAN_WIDTH_FRACTION = 0.66f

/** Fraction of available width for Junie message cards — right-aligned for long-form readability. */
private const val JUNIE_WIDTH_FRACTION = 0.9f

/** Rounded corner shape for message cards. */
private val MESSAGE_CARD_SHAPE = RoundedCornerShape(8.dp)

/** Width of the accent rail on message cards. */
private val ACCENT_RAIL_WIDTH = 4.dp

// ---------------------------------------------------------------------------
// Unified message item — replaces duplicate HumanMessageItem / JunieMessageItem
// ---------------------------------------------------------------------------

/**
 * Compact, right-inset Human message card with accent rail.
 */
@Composable
fun HumanMessageItem(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        MessageCard(
            message = message,
            senderLabel = "Human",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            accentColor = JunieViewerTheme.conversationColors.humanAccent,
            widthFraction = HUMAN_WIDTH_FRACTION,
            testTagSuffix = "human"
        )
    }
}

/**
 * Full-width, left-aligned Junie message card optimised for long-form reading.
 */
@Composable
fun JunieMessageItem(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        MessageCard(
            message = message,
            senderLabel = "Junie",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            accentColor = JunieViewerTheme.conversationColors.junieAccent,
            widthFraction = JUNIE_WIDTH_FRACTION,
            testTagSuffix = "junie"
        )
    }
}

/**
 * Shared message card structure used by both Human and Junie message items.
 * Renders an accent rail, sender label, themed kind marker, and message body.
 */
@Composable
private fun MessageCard(
    message: Message,
    senderLabel: String,
    containerColor: Color,
    accentColor: Color,
    widthFraction: Float,
    testTagSuffix: String
) {
    val spacing = JunieViewerTheme.spacing

    val cardModifier = Modifier
        .fillMaxWidth(widthFraction)
        .testTag("message_item_$testTagSuffix")

    Card(
        modifier = cardModifier,
        shape = MESSAGE_CARD_SHAPE,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row {
            // Accent rail
            Box(
                modifier = Modifier
                    .width(ACCENT_RAIL_WIDTH)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Column(modifier = Modifier.padding(spacing.lg)) {
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
                    MessageKindMarker(
                        kind = message.kind,
                        modifier = Modifier.testTag("message_kind_marker")
                    )
                }
                Spacer(modifier = Modifier.height(spacing.md))
                MessageBody(message = message)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Themed Message Kind marker — coloured dot + clean text label
// ---------------------------------------------------------------------------

/**
 * Renders a small coloured dot indicator paired with a clean text label for the given [MessageKind].
 * Replaces raw emoji-prefixed labels with a themed visual indicator.
 */
@Composable
private fun MessageKindMarker(kind: MessageKind, modifier: Modifier = Modifier) {
    val dotColor = kindIndicatorColor(kind)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = kind.label
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(JunieViewerTheme.spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = kind.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Returns a semantic colour for the kind indicator dot. */
@Composable
private fun kindIndicatorColor(kind: MessageKind): Color {
    val cc = JunieViewerTheme.conversationColors
    return when (kind) {
        MessageKind.Text, MessageKind.Markdown -> MaterialTheme.colorScheme.primary
        MessageKind.Thought -> cc.thoughtBorder
        MessageKind.Tool, MessageKind.Mcp -> cc.toolCallBorder
        MessageKind.Patch -> cc.diffAddedText
        MessageKind.Terminal -> cc.terminalText
        MessageKind.StructuredOutput -> MaterialTheme.colorScheme.tertiary
        MessageKind.Error -> MaterialTheme.colorScheme.error
        MessageKind.Warning -> cc.warningBackground
        MessageKind.Unsupported -> MaterialTheme.colorScheme.error
        MessageKind.TestRun -> MaterialTheme.colorScheme.secondary
        MessageKind.SubAgent -> MaterialTheme.colorScheme.tertiary
        MessageKind.Question -> MaterialTheme.colorScheme.primary
        MessageKind.Choice -> MaterialTheme.colorScheme.secondary
        MessageKind.SystemMessage -> MaterialTheme.colorScheme.onSurfaceVariant
        MessageKind.Cancelled -> MaterialTheme.colorScheme.error
        MessageKind.Status -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

// ---------------------------------------------------------------------------
// Visual header marking the start of a Junie Turn
// ---------------------------------------------------------------------------

/** Themed divider header marking the start of a Junie Turn in the conversation. */
@Composable
fun TurnHeader() {
    val spacing = JunieViewerTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.sm)
            .testTag("turn_header")
            .semantics { heading() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = JunieViewerTheme.conversationColors.junieAccent.copy(alpha = 0.4f)
        )
        Text(
            text = "Junie Turn",
            style = MaterialTheme.typography.titleMedium,
            color = JunieViewerTheme.conversationColors.junieAccent,
            modifier = Modifier.padding(horizontal = spacing.lg)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = JunieViewerTheme.conversationColors.junieAccent.copy(alpha = 0.4f)
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
    val spacing = JunieViewerTheme.spacing
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.sm)
            .testTag("unsupported_event_card"),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = (message.content as? MessageContent.Text)?.text ?: "Unsupported event",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(spacing.md)
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
                    style = MaterialTheme.typography.bodyLarge,
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
