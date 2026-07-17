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
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
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
 * Main Conversation screen with search/filter controls at the top,
 * conversation content in the middle, and a Session metadata footer.
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
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
    ) {
        SearchAndFilterChrome(state, onAction, searchFocusRequester, isSearchOrFilterActive)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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

        SessionContextFooter(state = state)
    }
}

// ---------------------------------------------------------------------------
// Search and filter chrome (replaces the former top bar with app title)
// ---------------------------------------------------------------------------

@Composable
private fun SearchAndFilterChrome(
    state: ConversationState,
    onAction: (ConversationAction) -> Unit,
    searchFocusRequester: FocusRequester,
    isSearchOrFilterActive: Boolean
) {
    val spacing = JunieViewerTheme.spacing

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column {
            // Search row with compact session/settings controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onAction(ConversationAction.OnSearchQueryChange(it)) },
                    modifier = Modifier
                        .weight(1f)
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
                                Text(
                                    "✕",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                TextButton(
                    onClick = { onAction(ConversationAction.OnToggleSessionPicker) },
                    modifier = Modifier
                        .padding(start = spacing.sm)
                        .testTag("session_picker_button")
                        .semantics { contentDescription = "Select Session" }
                ) {
                    Text(
                        text = state.selectedSessionId ?: "Session",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                TextButton(
                    onClick = { onAction(ConversationAction.OnToggleSettings) },
                    modifier = Modifier
                        .padding(start = spacing.xs)
                        .testTag("settings_button")
                        .semantics { contentDescription = "Settings" }
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Filter chips row
            FilterBar(
                filter = state.filter,
                onToggleFilter = { onAction(ConversationAction.OnToggleFilter(it)) },
                modifier = Modifier.padding(vertical = spacing.sm)
            )

            // Match navigation when search/filter is active
            if (isSearchOrFilterActive) {
                MatchNavigationBar(state, onAction)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/** Match count and previous/next navigation controls. */
@Composable
private fun MatchNavigationBar(state: ConversationState, onAction: (ConversationAction) -> Unit) {
    val spacing = JunieViewerTheme.spacing
    val matchCount = state.filteredMessages.size
    val totalCount = state.messages.size
    val countText = if (matchCount == 0) "No matching Messages" else "$matchCount of $totalCount Messages"

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.xl, vertical = spacing.sm),
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
                modifier = Modifier.padding(end = spacing.sm).testTag("match_position")
            )
            IconButton(
                onClick = { onAction(ConversationAction.OnPreviousMatch) },
                modifier = Modifier.size(MATCH_NAV_BUTTON_SIZE).testTag("prev_match_button")
                    .semantics { contentDescription = "Previous match" }
            ) {
                Text(
                    "▲",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { onAction(ConversationAction.OnNextMatch) },
                modifier = Modifier.size(MATCH_NAV_BUTTON_SIZE).testTag("next_match_button")
                    .semantics { contentDescription = "Next match" }
            ) {
                Text(
                    "▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Fixed size for match navigation icon buttons — not part of the spacing scale. */
private val MATCH_NAV_BUTTON_SIZE = 32.dp

// ---------------------------------------------------------------------------
// Content states
// ---------------------------------------------------------------------------

/** Centred state message used by loading, empty, no-session, and no-results states. */
@Composable
private fun CenteredStateMessage(
    title: String,
    description: String,
    testTag: String,
    accessibilityDescription: String,
    aboveTitle: @Composable (ColumnScope.() -> Unit)? = null
) {
    val spacing = JunieViewerTheme.spacing
    Box(
        modifier = Modifier.fillMaxSize().testTag(testTag)
            .semantics { contentDescription = accessibilityDescription },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            aboveTitle?.invoke(this)
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingState() {
    val spacing = JunieViewerTheme.spacing
    CenteredStateMessage(
        title = "Loading Conversation\u2026",
        description = "",
        testTag = "loading_indicator",
        accessibilityDescription = "Loading Conversation",
        aboveTitle = {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(spacing.lg))
        }
    )
}

@Composable
private fun ErrorState(errorMessage: String, onAction: (ConversationAction) -> Unit) {
    val spacing = JunieViewerTheme.spacing
    Box(
        modifier = Modifier.fillMaxSize().testTag("error_state")
            .semantics { contentDescription = "Error loading Conversation" },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.padding(spacing.xl)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(spacing.xl)
            ) {
                Text(
                    "Error",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(spacing.md))
                Text(
                    errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(spacing.lg))
                Button(
                    onClick = { onAction(ConversationAction.OnRetryClick) },
                    modifier = Modifier.testTag("retry_button").semantics { contentDescription = "Retry loading" }
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun NoSessionState() = CenteredStateMessage(
    title = "No Session selected",
    description = "Choose a Session to view its Conversation.",
    testTag = "no_session_state",
    accessibilityDescription = "No Session selected"
)

@Composable
private fun EmptyConversationState() = CenteredStateMessage(
    title = "This Session has no Messages",
    description = "The selected Session loaded successfully, but no Conversation Messages were found.",
    testTag = "empty_conversation",
    accessibilityDescription = "Empty Conversation"
)

@Composable
private fun NoResultsState() = CenteredStateMessage(
    title = "No Results",
    description = "No Messages match the current Search Query and Filters.",
    testTag = "no_results",
    accessibilityDescription = "No matching Messages"
)

@Composable
private fun BoxScope.ConversationList(state: ConversationState) {
    val turns = groupMessagesIntoTurns(state.filteredMessages)
    val listState = rememberLazyListState()

    // Scroll to current search match
    LaunchedEffect(state.currentMatchIndex) {
        val matchIdx = state.currentMatchIndex
        if (matchIdx >= 0 && matchIdx < state.filteredMessages.size) {
            val lazyItemIndex = lazyColumnIndexForMessage(turns, matchIdx)
            listState.animateScrollToItem(lazyItemIndex)
        }
    }

    // Auto-scroll to bottom when new messages arrive and user is near the bottom
    val messageCount = state.filteredMessages.size
    LaunchedEffect(messageCount) {
        if (messageCount > 0 && state.searchQuery.isBlank()) {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            // "Near bottom" = last visible item is within 3 items of the end
            val nearBottom = totalItems == 0 || lastVisibleIndex >= totalItems - 3
            if (nearBottom) {
                listState.animateScrollToItem(maxOf(0, layoutInfo.totalItemsCount - 1))
            }
        }
    }

    val spacing = JunieViewerTheme.spacing

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().testTag("message_list"),
        contentPadding = PaddingValues(spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        turns.forEach { turn ->
            if (turn.sender == Sender.Human) {
                items(items = turn.messages, key = { it.id }) { message ->
                    val msgIndex = state.filteredMessages.indexOf(message)
                    val isCurrentMatch = state.searchQuery.isNotBlank() && msgIndex == state.currentMatchIndex
                    HumanMessageItem(message = message, searchQuery = state.searchQuery, isCurrentMatch = isCurrentMatch)
                }
            } else {
                item(key = "turn-header-${turn.messages.first().id}") {
                    Spacer(modifier = Modifier.height(spacing.xl))
                    TurnHeader()
                }
                items(items = turn.messages, key = { it.id }) { message ->
                    val msgIndex = state.filteredMessages.indexOf(message)
                    val isCurrentMatch = state.searchQuery.isNotBlank() && msgIndex == state.currentMatchIndex
                    JunieMessageItem(message = message, searchQuery = state.searchQuery, isCurrentMatch = isCurrentMatch)
                }
            }
        }
    }

    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(listState),
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
    )
}
