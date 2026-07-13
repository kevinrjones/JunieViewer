package com.knowledgespike.junieviewer.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.ui.components.*
import dev.snipme.highlights.model.SyntaxLanguage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    val searchFocusRequester = remember { FocusRequester() }
    val isSearchOrFilterActive = state.searchQuery.isNotBlank() || !state.filter.isDefault()

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
                        modifier = Modifier.padding(16.dp).semantics { heading() }
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { onAction(ConversationAction.OnToggleSessionPicker) },
                            modifier = Modifier.padding(end = 8.dp).testTag("session_picker_button")
                                .semantics { contentDescription = "Select Session" }
                        ) {
                            Text(state.selectedSessionId ?: "Select Session")
                        }

                        TextButton(
                            onClick = { onAction(ConversationAction.OnToggleSettings) },
                            modifier = Modifier.padding(end = 16.dp).testTag("settings_button")
                                .semantics { contentDescription = "Settings" }
                        ) {
                            Text("Settings")
                        }
                    }
                }
                // Session context header — visible when a Session is selected
                if (state.selectedSessionId != null) {
                    SessionContextHeader(
                        state = state,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onAction(ConversationAction.OnSearchQueryChange(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .focusRequester(searchFocusRequester)
                        .testTag("search_field"),
                    placeholder = { Text("Search Messages...") },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onAction(ConversationAction.OnSearchQueryChange("")) },
                                modifier = Modifier.testTag("search_clear_button")
                                    .semantics { contentDescription = "Clear search" }
                            ) {
                                Text("✕")
                            }
                        }
                    },
                    singleLine = true
                )
                FilterBar(
                    filter = state.filter,
                    onToggleFilter = { onAction(ConversationAction.OnToggleFilter(it)) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                // Result count and match navigation when Search or Filters are active
                if (isSearchOrFilterActive) {
                    val matchCount = state.filteredMessages.size
                    val totalCount = state.messages.size
                    val countText = if (matchCount == 0) {
                        "No matching Messages"
                    } else {
                        "$matchCount of $totalCount Messages"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = countText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("result_count")
                        )
                        if (matchCount > 1) {
                            val matchLabel = if (state.currentMatchIndex >= 0) {
                                "${state.currentMatchIndex + 1} / $matchCount"
                            } else ""
                            Text(
                                text = matchLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp).testTag("match_position")
                            )
                            IconButton(
                                onClick = { onAction(ConversationAction.OnPreviousMatch) },
                                modifier = Modifier.size(32.dp).testTag("prev_match_button")
                                    .semantics { contentDescription = "Previous match" }
                            ) {
                                Text("▲", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(
                                onClick = { onAction(ConversationAction.OnNextMatch) },
                                modifier = Modifier.size(32.dp).testTag("next_match_button")
                                    .semantics { contentDescription = "Next match" }
                            ) {
                                Text("▼", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        },
        modifier = Modifier.onPreviewKeyEvent { keyEvent ->
            // Cmd+F (macOS) / Ctrl+F (Windows/Linux) focuses the search field
            if (keyEvent.type == KeyEventType.KeyDown &&
                keyEvent.key == Key.F &&
                (keyEvent.isMetaPressed || keyEvent.isCtrlPressed)
            ) {
                searchFocusRequester.requestFocus()
                true
            } else {
                false
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // State priority: Loading > Error > No Session > Empty Conversation > No Results > Normal
            when {
                state.isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("loading_indicator")
                            .semantics { contentDescription = "Loading Conversation" },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading Conversation\u2026",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                state.errorMessage != null -> {
                    // Recoverable error state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("error_state")
                            .semantics { contentDescription = "Error loading Conversation" },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⚠",
                                style = MaterialTheme.typography.headlineLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.errorMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onAction(ConversationAction.OnRetryClick) },
                                modifier = Modifier.testTag("retry_button")
                                    .semantics { contentDescription = "Retry loading" }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                state.selectedSessionId == null -> {
                    // No Session selected state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("no_session_state")
                            .semantics { contentDescription = "No Session selected" },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No Session selected",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Choose a Session to view its Conversation.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                state.messages.isEmpty() -> {
                    // Empty Conversation state — session selected but no messages
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("empty_conversation")
                            .semantics { contentDescription = "Empty Conversation" },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "This Session has no Messages",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "The selected Session loaded successfully, but no Conversation Messages were found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                state.filteredMessages.isEmpty() -> {
                    // No-results state from search/filter
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("no_results")
                            .semantics { contentDescription = "No matching Messages" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Messages match the current Search Query and Filters.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    // Normal conversation list
                    val turns = groupMessagesIntoTurns(state.filteredMessages)
                    val listState = rememberLazyListState()

                    // Auto-scroll to the current match when it changes
                    LaunchedEffect(state.currentMatchIndex) {
                        val matchIdx = state.currentMatchIndex
                        if (matchIdx >= 0 && matchIdx < state.filteredMessages.size) {
                            val lazyItemIndex = lazyColumnIndexForMessage(turns, matchIdx)
                            listState.animateScrollToItem(lazyItemIndex)
                        }
                    }

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
                                items(
                                    items = turn.messages,
                                    key = { it.id }
                                ) { message ->
                                    HumanMessageItem(message = message)
                                }
                            } else {
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
    }
}

/**
 * Computes the LazyColumn item index for the message at [messageIndex] in the flat
 * filteredMessages list, accounting for Junie Turn headers inserted before each Junie turn.
 */
fun lazyColumnIndexForMessage(turns: List<Turn>, messageIndex: Int): Int {
    var flatIdx = 0
    var lazyIdx = 0
    for (turn in turns) {
        if (turn.sender == Sender.Junie) {
            // Junie turns have a header item before the messages
            if (flatIdx + turn.messages.size > messageIndex) {
                return lazyIdx + 1 + (messageIndex - flatIdx) // +1 for header
            }
            lazyIdx += 1 + turn.messages.size // header + messages
        } else {
            if (flatIdx + turn.messages.size > messageIndex) {
                return lazyIdx + (messageIndex - flatIdx)
            }
            lazyIdx += turn.messages.size
        }
        flatIdx += turn.messages.size
    }
    return lazyIdx // fallback
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
    MessageKind.TestRun -> "🧪 Test"
    MessageKind.Mcp -> "🔌 MCP"
    MessageKind.SubAgent -> "🤖 SubAgent"
    MessageKind.Question -> "❓ Question"
    MessageKind.Choice -> "🔘 Choice"
    MessageKind.SystemMessage -> "ℹ️ System"
    MessageKind.Cancelled -> "⛔ Cancelled"
    MessageKind.Status -> "📋 Status"
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
        MessageKind.Tool, MessageKind.Mcp -> ToolCallBlock(
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

/**
 * Persistent Session context header showing Session id and timestamp.
 * Displayed in the top bar when a Session is selected.
 */
@Composable
fun SessionContextHeader(
    state: ConversationState,
    modifier: Modifier = Modifier
) {
    val session = state.selectedSession
    val timestampLabel = if (session != null) {
        val millis = session.createdAt ?: session.lastModified
        val label = if (session.createdAt != null) "Created" else "Last modified"
        val formatted = formatTimestamp(millis)
        "$label: $formatted"
    } else null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("session_context_header"),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(
                text = "Session: ${state.selectedSessionId.orEmpty()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (timestampLabel != null) {
                Text(
                    text = timestampLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (session?.workingDirectory != null) {
                Text(
                    text = "Project: ${session.workingDirectory}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Formats an epoch-millis timestamp into a human-readable local date/time string. */
fun formatTimestamp(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

/** Heuristic: returns true if the text contains common Markdown formatting markers. */
fun looksLikeMarkdown(text: String): Boolean {
    val markers = listOf("# ", "## ", "**", "__", "```", "- ", "* ", "1. ", "[", "](")
    return markers.any { text.contains(it) }
}
