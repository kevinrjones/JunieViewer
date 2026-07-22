package com.knowledgespike.junieviewer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.ui.components.renderers.MessageRendererRegistry
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

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
fun HumanMessageItem(
    message: Message,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false,
    blockExpansionStates: Map<String, Boolean> = emptyMap(),
    onToggleBlock: (String) -> Unit = {}
) {
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
            isCurrentMatch = isCurrentMatch,
            blockExpansionStates = blockExpansionStates,
            onToggleBlock = onToggleBlock
        )
    }
}

/**
 * Full-width, left-aligned Junie message card optimised for long-form reading.
 */
@Composable
fun JunieMessageItem(
    message: Message,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false,
    blockExpansionStates: Map<String, Boolean> = emptyMap(),
    onToggleBlock: (String) -> Unit = {}
) {
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
            isCurrentMatch = isCurrentMatch,
            blockExpansionStates = blockExpansionStates,
            onToggleBlock = onToggleBlock
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
    isCurrentMatch: Boolean = false,
    blockExpansionStates: Map<String, Boolean> = emptyMap(),
    onToggleBlock: (String) -> Unit = {}
) {
    val spacing = JunieViewerTheme.spacing
    val isHuman = message.sender == Sender.Human
    // Human card expansion is ViewModel-derived via a "{messageId}:card" block ID
    val humanCardBlockId = "${message.id}:card"
    val humanCardExpanded = blockExpansionStates[humanCardBlockId] ?: true

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
                                    detectTapGestures { onToggleBlock(humanCardBlockId) }
                                }
                                .testTag("human_block_header")
                            else Modifier
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isHuman) {
                        Text(
                            text = if (humanCardExpanded) "▼" else "▶",
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
                        visible = humanCardExpanded,
                        modifier = Modifier.testTag("human_block_body")
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(spacing.md))
                            MessageBody(
                                message = message,
                                searchQuery = searchQuery,
                                isCurrentMatch = isCurrentMatch,
                                blockExpansionStates = blockExpansionStates,
                                onToggleBlock = onToggleBlock
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(spacing.md))
                    MessageBody(
                        message = message,
                        searchQuery = searchQuery,
                        isCurrentMatch = isCurrentMatch,
                        blockExpansionStates = blockExpansionStates,
                        onToggleBlock = onToggleBlock
                    )
                }
            }
        }
    }
}

/**
 * Renders a small "Sub-Agent" badge/label for sub-agent messages.
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

/**
 * Renders the body content of a Message using the [MessageRendererRegistry].
 */
@Composable
fun MessageBody(
    message: Message,
    searchQuery: String = "",
    isCurrentMatch: Boolean = false,
    blockExpansionStates: Map<String, Boolean> = emptyMap(),
    onToggleBlock: (String) -> Unit = {}
) {
    MessageRendererRegistry.Render(
        message = message,
        searchQuery = searchQuery,
        isCurrentMatch = isCurrentMatch,
        blockExpansionStates = blockExpansionStates,
        onToggleBlock = onToggleBlock
    )
}
