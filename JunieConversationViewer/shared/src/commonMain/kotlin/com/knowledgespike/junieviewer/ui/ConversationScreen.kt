package com.knowledgespike.junieviewer.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.ui.components.*
import dev.snipme.highlights.model.SyntaxLanguage

/**
 * Root composable that collects ViewModel state and delegates to ConversationScreen.
 */
@Composable
fun ConversationRoot(
    viewModel: ConversationViewModel
) {
    val state by viewModel.state.collectAsState()

    ConversationScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

/**
 * Main Conversation screen with top bar, search, filters, and the asymmetric message list.
 */
@Composable
fun ConversationScreen(
    state: ConversationState,
    onAction: (ConversationAction) -> Unit
) {
    if (state.isSessionPickerOpen) {
        SessionSelector(
            sessions = state.sessions,
            selectedSessionId = state.selectedSessionId,
            onSessionSelected = { onAction(ConversationAction.OnSessionSelected(it)) },
            onDismiss = { onAction(ConversationAction.OnToggleSessionPicker) }
        )
    }

    if (state.isSettingsOpen) {
        SettingsDialog(
            currentHomePath = state.junieHomePath,
            onHomePathChange = { onAction(ConversationAction.OnHomePathChange(it)) },
            onDismiss = { onAction(ConversationAction.OnToggleSettings) }
        )
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Junie Conversation Viewer",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { onAction(ConversationAction.OnToggleSessionPicker) },
                            modifier = Modifier.padding(end = 8.dp).testTag("session_picker_button")
                        ) {
                            Text(state.selectedSessionId ?: "Select Session")
                        }

                        TextButton(
                            onClick = { onAction(ConversationAction.OnToggleSettings) },
                            modifier = Modifier.padding(end = 16.dp).testTag("settings_button")
                        ) {
                            Text("Settings")
                        }
                    }
                }
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onAction(ConversationAction.OnSearchQueryChange(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("search_field"),
                    placeholder = { Text("Search messages...") }
                )
                FilterBar(
                    filter = state.filter,
                    onToggleFilter = { onAction(ConversationAction.OnToggleFilter(it)) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        val turns = groupMessagesIntoTurns(state.filteredMessages)
        val listState = rememberLazyListState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("message_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                turns.forEach { turn ->
                    if (turn.sender == Sender.Human) {
                        // Human messages render individually — compact, right-inset
                        items(
                            items = turn.messages,
                            key = { it.id }
                        ) { message ->
                            HumanMessageItem(message = message)
                        }
                    } else {
                        // Junie Turn: header + grouped messages
                        item(key = "turn-header-${turn.messages.first().id}") {
                            TurnHeader()
                        }
                        items(
                            items = turn.messages,
                            key = { it.id }
                        ) { message ->
                            JunieMessageItem(message = message)
                        }
                    }
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * Groups a flat list of Messages into Turns — contiguous runs of the same Sender.
 * Preserves chronological order.
 */
fun groupMessagesIntoTurns(messages: List<Message>): List<Turn> {
    if (messages.isEmpty()) return emptyList()

    val turns = mutableListOf<Turn>()
    var currentSender = messages.first().sender
    var currentMessages = mutableListOf(messages.first())

    for (i in 1 until messages.size) {
        val message = messages[i]
        if (message.sender == currentSender) {
            currentMessages.add(message)
        } else {
            turns.add(Turn(sender = currentSender, messages = currentMessages))
            currentSender = message.sender
            currentMessages = mutableListOf(message)
        }
    }
    turns.add(Turn(sender = currentSender, messages = currentMessages))
    return turns
}

/**
 * Represents a contiguous span of Messages from the same Sender.
 */
data class Turn(
    val sender: Sender,
    val messages: List<Message>
)

/**
 * Visual header marking the start of a Junie Turn.
 */
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

/**
 * Returns a short text label for a Message Kind, used as a non-colour-only marker.
 */
fun messageKindLabel(kind: MessageKind): String = when (kind) {
    MessageKind.Text -> "💬 Text"
    MessageKind.Markdown -> "📄 Markdown"
    MessageKind.Thought -> "💭 Thought"
    MessageKind.Tool -> "🔧 Tool"
    MessageKind.Patch -> "📝 Patch"
    MessageKind.Terminal -> "⌨ Terminal"
    MessageKind.StructuredOutput -> "📊 Structured"
    MessageKind.Error -> "❌ Error"
    MessageKind.Warning -> "⚠️ Warning"
    MessageKind.Unsupported -> "⚠ Unsupported"
}

/**
 * Compact, right-inset Human message card. Constrained max width so short prompts
 * do not span the full pane.
 */
@Composable
fun HumanMessageItem(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .testTag("message_item_human"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Human",
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
                Spacer(modifier = Modifier.height(4.dp))
                MessageBody(message = message)
            }
        }
    }
}

/**
 * Full-width, left-inset Junie message card optimised for long-form reading.
 */
@Composable
fun JunieMessageItem(message: Message) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("message_item_junie"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Junie",
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
            Spacer(modifier = Modifier.height(8.dp))
            MessageBody(message = message)
        }
    }
}

/**
 * Renders the body content of a Message based on its MessageContent type and MessageKind.
 * Dispatches to dedicated rich content composables for each content variant.
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
        MessageKind.Tool -> ToolCallBlock(
            content = when (val c = message.content) {
                is MessageContent.Code -> c.code
                is MessageContent.Text -> c.text
                else -> ""
            },
            modifier = Modifier.testTag("tool_call_block")
        )
        MessageKind.Unsupported -> {
            // Handled below as unsupported card
        }
        else -> {
            // Dispatch by content type for Text, Markdown, Code, Diff, Terminal, Structured
            when (val content = message.content) {
                is MessageContent.Text -> {
                    if (message.kind == MessageKind.Markdown || looksLikeMarkdown(content.text)) {
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
    }

    // Unsupported event kind indicator — visually distinct warning card
    if (message.kind == MessageKind.Unsupported) {
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
}

/** Heuristic: returns true if the text contains common Markdown formatting markers. */
fun looksLikeMarkdown(text: String): Boolean {
    val markers = listOf("# ", "## ", "**", "__", "```", "- ", "* ", "1. ", "[", "](")
    return markers.any { text.contains(it) }
}
