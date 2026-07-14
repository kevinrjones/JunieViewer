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
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.domain.groupMessagesIntoTurns
import com.knowledgespike.junieviewer.domain.lazyColumnIndexForMessage
import com.knowledgespike.junieviewer.ui.components.*

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
            currentThemeMode = state.themeMode,
            onHomePathChange = { onAction(ConversationAction.OnHomePathChange(it)) },
            onThemeModeChange = { onAction(ConversationAction.OnThemeModeChange(it)) },
            onDismiss = { onAction(ConversationAction.OnToggleSettings) }
        )
    }

    val searchFocusRequester = remember { FocusRequester() }
    val isSearchOrFilterActive = state.searchQuery.isNotBlank() || !state.filter.isDefault()

    Scaffold(
        topBar = { ConversationTopBar(state, onAction, searchFocusRequester, isSearchOrFilterActive) },
        modifier = Modifier.onPreviewKeyEvent { keyEvent ->
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
                state.isLoading -> LoadingState()
                state.errorMessage != null -> ErrorState(state.errorMessage, onAction)
                state.selectedSessionId == null -> NoSessionState()
                state.messages.isEmpty() -> EmptyConversationState()
                state.filteredMessages.isEmpty() -> NoResultsState()
                else -> ConversationList(state)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Top bar
// ---------------------------------------------------------------------------

@Composable
private fun ConversationTopBar(
    state: ConversationState,
    onAction: (ConversationAction) -> Unit,
    searchFocusRequester: FocusRequester,
    isSearchOrFilterActive: Boolean
) {
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
        if (isSearchOrFilterActive) {
            MatchNavigationBar(state, onAction)
        }
    }
}

@Composable
private fun MatchNavigationBar(state: ConversationState, onAction: (ConversationAction) -> Unit) {
    val matchCount = state.filteredMessages.size
    val totalCount = state.messages.size
    val countText = if (matchCount == 0) "No matching Messages" else "$matchCount of $totalCount Messages"

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = countText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).testTag("result_count")
        )
        if (matchCount > 1) {
            val matchLabel = if (state.currentMatchIndex >= 0) "${state.currentMatchIndex + 1} / $matchCount" else ""
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

// ---------------------------------------------------------------------------
// Content states
// ---------------------------------------------------------------------------

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize().testTag("loading_indicator")
            .semantics { contentDescription = "Loading Conversation" },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading Conversation\u2026", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorState(errorMessage: String, onAction: (ConversationAction) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().testTag("error_state")
            .semantics { contentDescription = "Error loading Conversation" },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onAction(ConversationAction.OnRetryClick) },
                modifier = Modifier.testTag("retry_button").semantics { contentDescription = "Retry loading" }
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun NoSessionState() {
    Box(
        modifier = Modifier.fillMaxSize().testTag("no_session_state")
            .semantics { contentDescription = "No Session selected" },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No Session selected", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Choose a Session to view its Conversation.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyConversationState() {
    Box(
        modifier = Modifier.fillMaxSize().testTag("empty_conversation")
            .semantics { contentDescription = "Empty Conversation" },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("This Session has no Messages", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("The selected Session loaded successfully, but no Conversation Messages were found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NoResultsState() {
    Box(
        modifier = Modifier.fillMaxSize().testTag("no_results")
            .semantics { contentDescription = "No matching Messages" },
        contentAlignment = Alignment.Center
    ) {
        Text("No Messages match the current Search Query and Filters.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BoxScope.ConversationList(state: ConversationState) {
    val turns = groupMessagesIntoTurns(state.filteredMessages)
    val listState = rememberLazyListState()

    LaunchedEffect(state.currentMatchIndex) {
        val matchIdx = state.currentMatchIndex
        if (matchIdx >= 0 && matchIdx < state.filteredMessages.size) {
            val lazyItemIndex = lazyColumnIndexForMessage(turns, matchIdx)
            listState.animateScrollToItem(lazyItemIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().testTag("message_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        turns.forEach { turn ->
            if (turn.sender == Sender.Human) {
                items(items = turn.messages, key = { it.id }) { message ->
                    HumanMessageItem(message = message)
                }
            } else {
                item(key = "turn-header-${turn.messages.first().id}") {
                    TurnHeader()
                }
                items(items = turn.messages, key = { it.id }) { message ->
                    JunieMessageItem(message = message)
                }
            }
        }
    }

    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(listState),
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
    )
}
