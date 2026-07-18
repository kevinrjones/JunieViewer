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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
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
fun HumanMessageItem(message: Message, searchQuery: String = "", isCurrentMatch: Boolean = false) {
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
            testTagSuffix = "human",
            searchQuery = searchQuery,
            isCurrentMatch = isCurrentMatch
        )
    }
}

/**
 * Full-width, left-aligned Junie message card optimised for long-form reading.
 */
@Composable
fun JunieMessageItem(message: Message, searchQuery: String = "", isCurrentMatch: Boolean = false) {
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
            testTagSuffix = "junie",
            searchQuery = searchQuery,
            isCurrentMatch = isCurrentMatch
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
    testTagSuffix: String,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false
) {
    val spacing = JunieViewerTheme.spacing
    val isHuman = message.sender == Sender.Human
    val expansionState = rememberMessageExpansionState(
        isCurrentMatch = isCurrentMatch,
        isHuman = isHuman,
        searchQuery = searchQuery
    )

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isHuman) Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        expansionState.toggle()
                                    }
                                }
                                .testTag("human_block_header")
                            else Modifier
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isHuman) {
                        Text(
                            text = if (expansionState.isVisible) "▼" else "▶",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(spacing.sm))
                    }
                    Text(
                        text = senderLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("sender_marker")
                    )
                    if (message.kind == MessageKind.SubAgent) {
                        SubAgentBadge()
                    }
                    MessageKindMarker(
                        kind = message.kind,
                        modifier = Modifier.testTag("message_kind_marker")
                    )
                }
                if (isHuman) {
                    AnimatedVisibility(
                        visible = expansionState.isVisible,
                        modifier = Modifier.testTag("human_block_body")
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(spacing.md))
                            MessageBody(message = message, searchQuery = searchQuery, isCurrentMatch = isCurrentMatch)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(spacing.md))
                    MessageBody(message = message, searchQuery = searchQuery, isCurrentMatch = isCurrentMatch)
                }
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
// Sub-Agent badge — small textual label for sub-agent messages
// ---------------------------------------------------------------------------

/**
 * Renders a small "Sub-Agent" badge/label for sub-agent messages.
 * Uses text and a border to be visible without relying on colour alone.
 */
@Composable
private fun SubAgentBadge(modifier: Modifier = Modifier) {
    val spacing = JunieViewerTheme.spacing
    Surface(
        modifier = modifier.testTag("sub_agent_badge"),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
    ) {
        Text(
            text = "Sub-Agent",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs)
        )
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
fun MessageBody(message: Message, searchQuery: String = "", isCurrentMatch: Boolean = false) {
    when (message.kind) {
        MessageKind.Thought -> {
            val thoughtText = (message.content as? MessageContent.Text)?.text ?: ""
            ThoughtBlock(
                text = thoughtText,
                modifier = Modifier.testTag("thought_block"),
                searchQuery = searchQuery,
                isCurrentMatch = isCurrentMatch,
                forceExpanded = isCurrentMatch && blockContainsSearchHit(thoughtText, searchQuery)
            )
        }
        MessageKind.Error, MessageKind.Warning -> {
            val errText = when (val c = message.content) {
                is MessageContent.Text -> c.text
                else -> "Unknown error"
            }
            ErrorWarningBlock(
                text = errText,
                isWarning = message.kind == MessageKind.Warning,
                modifier = Modifier.testTag("error_warning_block"),
                searchQuery = searchQuery,
                isCurrentMatch = isCurrentMatch,
                forceExpanded = isCurrentMatch && blockContainsSearchHit(errText, searchQuery)
            )
        }
        MessageKind.Tool, MessageKind.Mcp -> {
            val toolText = when (val c = message.content) {
                is MessageContent.Code -> c.code
                is MessageContent.Text -> c.text
                else -> ""
            }
            ToolCallBlock(
                content = toolText,
                modifier = Modifier.testTag("tool_call_block"),
                searchQuery = searchQuery,
                isCurrentMatch = isCurrentMatch,
                forceExpanded = isCurrentMatch && blockContainsSearchHit(toolText, searchQuery)
            )
        }
        MessageKind.Markdown -> {
            val mdText = (message.content as? MessageContent.Text)?.text ?: ""
            val colors = JunieViewerTheme.conversationColors
            CollapsibleBlock(
                label = "Markdown",
                backgroundColor = colors.codeBackground,
                borderColor = colors.codeBorder,
                headerTestTag = "markdown_block_header",
                bodyTestTag = "markdown_block_body",
                forceExpanded = isCurrentMatch && blockContainsSearchHit(mdText, searchQuery),
                body = {
                    SelectionContainer(modifier = Modifier.testTag("selectable_message_text")) {
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
        MessageKind.SubAgent -> {
            val subAgentText = (message.content as? MessageContent.Text)?.text ?: ""
            val colors = JunieViewerTheme.conversationColors
            CollapsibleBlock(
                label = "Sub-Agent",
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                borderColor = MaterialTheme.colorScheme.tertiary,
                headerTestTag = "sub_agent_block_header",
                bodyTestTag = "sub_agent_block_body",
                forceExpanded = isCurrentMatch && blockContainsSearchHit(subAgentText, searchQuery),
                body = {
                    SelectionContainer(modifier = Modifier.testTag("selectable_sub_agent_content")) {
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
        MessageKind.Unsupported -> UnsupportedEventCard(message)
        else -> ContentRenderer(message.content, isMarkdownKind = false, searchQuery = searchQuery, isCurrentMatch = isCurrentMatch)
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
private fun ContentRenderer(
    content: MessageContent,
    isMarkdownKind: Boolean,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false
) {
    val colors = JunieViewerTheme.conversationColors
    when (content) {
        is MessageContent.Text -> {
            if (isMarkdownKind || looksLikeMarkdown(content.text)) {
                SelectionContainer(modifier = Modifier.testTag("selectable_message_text")) {
                    MarkdownContent(
                        markdown = content.text,
                        modifier = Modifier.testTag("markdown_content"),
                        searchQuery = searchQuery,
                        isCurrentMatch = isCurrentMatch
                    )
                }
            } else {
                SelectionContainer(modifier = Modifier.testTag("selectable_message_text")) {
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
        }
        is MessageContent.Code -> CodeBlockWithCopy(
            code = content.code,
            language = when (content.language.lowercase()) {
                "json" -> SyntaxLanguage.JAVASCRIPT
                "bash", "sh" -> SyntaxLanguage.SHELL
                else -> SyntaxLanguage.KOTLIN
            },
            modifier = Modifier.testTag("code_block"),
            forceExpanded = isCurrentMatch && blockContainsSearchHit(content.code, searchQuery)
        )
        is MessageContent.Diff -> DiffBlock(
            diff = content.diff,
            modifier = Modifier.testTag("diff_block"),
            searchQuery = searchQuery,
            isCurrentMatch = isCurrentMatch,
            forceExpanded = isCurrentMatch && blockContainsSearchHit(content.diff, searchQuery)
        )
        is MessageContent.Terminal -> TerminalOutputBlock(
            output = content.output,
            modifier = Modifier.testTag("terminal_block"),
            searchQuery = searchQuery,
            isCurrentMatch = isCurrentMatch,
            forceExpanded = isCurrentMatch && blockContainsSearchHit(content.output, searchQuery)
        )
        is MessageContent.Structured -> StructuredOutputBlock(
            data = content.data,
            modifier = Modifier.testTag("structured_output_block"),
            searchQuery = searchQuery,
            isCurrentMatch = isCurrentMatch,
            forceExpanded = isCurrentMatch && blockContainsSearchHit(content.data, searchQuery)
        )
    }
}

// ---------------------------------------------------------------------------
// Expansion state helper — encapsulates auto-expand-on-search + manual collapse
// ---------------------------------------------------------------------------

/** Holds the expansion state for a collapsible message card. */
class MessageExpansionState(
    expanded: Boolean,
    private val forceExpanded: Boolean
) {
    var expanded by mutableStateOf(expanded)
        private set
    var userDismissedForce by mutableStateOf(false)
        internal set

    /** Whether the content should be visually expanded. */
    val isVisible: Boolean
        get() = expanded || (forceExpanded && !userDismissedForce)

    /** Toggles the expanded state, tracking user dismissal of force-expand. */
    fun toggle() {
        expanded = !expanded
        if (!expanded && forceExpanded) {
            userDismissedForce = true
        }
    }
}

/**
 * Remembers a [MessageExpansionState] that auto-expands when the message is the
 * current search match and resets the user-dismissed flag when force-expand ends.
 */
@Composable
fun rememberMessageExpansionState(
    isCurrentMatch: Boolean,
    isHuman: Boolean,
    searchQuery: String
): MessageExpansionState {
    val state = remember { MessageExpansionState(expanded = true, forceExpanded = false) }
    val forceExpanded = isCurrentMatch && isHuman && searchQuery.isNotBlank()
    val result = remember(forceExpanded) {
        MessageExpansionState(expanded = state.expanded, forceExpanded = forceExpanded)
    }
    if (!forceExpanded) {
        result.userDismissedForce = false
    }
    return result
}
