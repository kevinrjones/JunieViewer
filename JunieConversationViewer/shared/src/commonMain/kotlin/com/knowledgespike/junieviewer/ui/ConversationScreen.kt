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
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import com.knowledgespike.junieviewer.ui.components.*

/**
 * Root composable that collects ViewModel state, handles one-time events,
 * and delegates to ConversationScreen.
 */
@Composable
fun ConversationRoot(
    viewModel: ConversationViewModel,
    onCopyText: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val commandState = ConversationCommandState.fromConversationState(state)

    // Dialog state driven by ViewModel events
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHowToUseDialog by remember { mutableStateOf(false) }

    // Search focus requester shared between event handling and toolbar
    val searchFocusRequester = remember { FocusRequester() }

    // Collect one-time events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ConversationEvent.ShowError -> { /* handled elsewhere */ }
                ConversationEvent.FocusSearch -> searchFocusRequester.requestFocus()
                ConversationEvent.ShowAbout -> showAboutDialog = true
                ConversationEvent.ShowHowToUse -> showHowToUseDialog = true
                ConversationEvent.CopyText -> onCopyText()
            }
        }
    }

    // About dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    // How to Use dialog
    if (showHowToUseDialog) {
        HowToUseDialog(onDismiss = { showHowToUseDialog = false })
    }

    // Provide the text selection reporter so TrackedSelectionContainer instances can
    // publish selection changes to the ViewModel, driving Copy command enablement.
    CompositionLocalProvider(
        LocalTextSelectionReporter provides { containerId, hasSelection ->
            viewModel.onAction(ConversationAction.OnTextSelectionChanged(containerId, hasSelection))
        }
    ) {
        ConversationScreen(
            state = state,
            commandState = commandState,
            onAction = viewModel::onAction,
            onCommand = viewModel::onCommand,
            onCopySelectedText = onCopyText,
            searchFocusRequester = searchFocusRequester
        )
    }
}

/**
 * Main Conversation screen with toolbar at the top, filter chips below,
 * conversation content in the middle, and a Session metadata footer.
 */
@Composable
fun ConversationScreen(
    state: ConversationState,
    commandState: ConversationCommandState = ConversationCommandState.fromConversationState(state),
    onAction: (ConversationAction) -> Unit,
    onCommand: (ConversationCommand) -> Unit = {},
    onCopySelectedText: () -> Unit = {},
    searchFocusRequester: FocusRequester = remember { FocusRequester() }
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
        // Toolbar with search field, command buttons, and navigation controls
        ConversationToolbar(
            state = state,
            commandState = commandState,
            onCommand = onCommand,
            onCopySelectedText = onCopySelectedText,
            onSearchQueryChange = { onAction(ConversationAction.OnSearchQueryChange(it)) },
            searchFocusRequester = searchFocusRequester
        )

        // Filter chips remain below the toolbar per HITL decision (Q12)
        FilterChrome(state, onAction)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // State priority: Loading > Error > No Session > Empty Conversation > No Results > Normal
            val errorMessage = state.errorMessage
            when {
                state.isLoading -> LoadingState()
                errorMessage != null -> ErrorState(errorMessage, onAction)
                state.selectedSessionId == null -> NoSessionState()
                state.messages.isEmpty() -> EmptyConversationState()
                state.filteredMessages.isEmpty() -> NoResultsState()
                else -> ConversationList(state, onAction)
            }
        }

        SessionContextFooter(state = state)
    }
}

// ---------------------------------------------------------------------------
// Filter chrome — filter chips below the toolbar (per HITL decision Q12)
// ---------------------------------------------------------------------------

/** Filter chips and active-filter summary, displayed below the toolbar. */
@Composable
private fun FilterChrome(
    state: ConversationState,
    onAction: (ConversationAction) -> Unit
) {
    val spacing = JunieViewerTheme.spacing

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column {
            FilterBar(
                filter = state.filter,
                onToggleFilter = { onAction(ConversationAction.OnToggleFilter(it)) },
                modifier = Modifier.padding(vertical = spacing.sm)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

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
private fun BoxScope.ConversationList(state: ConversationState, onAction: (ConversationAction) -> Unit) {
    // Turns are derived in the ViewModel alongside filteredMessages (F10) — no per-recomposition grouping
    val turns = state.turns
    val listState = rememberLazyListState()

    // Scroll to current search match
    LaunchedEffect(state.currentMatchIndex) {
        val matchIdx = state.currentMatchIndex
        if (matchIdx >= 0 && matchIdx < state.filteredMessages.size) {
            val lazyItemIndex = lazyColumnIndexForMessage(turns, matchIdx)
            listState.animateScrollToItem(lazyItemIndex)
        }
    }

    // Auto-scroll when new messages arrive: scroll to the edge where new messages appear
    // In OldestFirst mode, new messages appear at the bottom — scroll to bottom if near bottom.
    // In NewestFirst mode, new messages appear at the top — scroll to top if near top.
    val messageCount = state.filteredMessages.size
    LaunchedEffect(messageCount) {
        if (messageCount > 0 && state.searchQuery.isBlank()) {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (state.sortOrder == SortOrder.NewestFirst) {
                val firstVisibleIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                val nearTop = totalItems == 0 || firstVisibleIndex <= 2
                if (nearTop) {
                    listState.animateScrollToItem(0)
                }
            } else {
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val nearBottom = totalItems == 0 || lastVisibleIndex >= totalItems - 3
                if (nearBottom) {
                    listState.animateScrollToItem(maxOf(0, totalItems - 1))
                }
            }
        }
    }

    val spacing = JunieViewerTheme.spacing

    // Precomputed once per filteredMessages/currentMatchIndex change, so each row can look
    // up "is this the current match?" by id instead of an O(n) indexOf() per recomposition.
    val currentMatchMessageId = remember(state.filteredMessages, state.currentMatchIndex) {
        state.filteredMessages.getOrNull(state.currentMatchIndex)?.id
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().testTag("message_list"),
        contentPadding = PaddingValues(spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        turns.forEach { turn ->
            if (turn.sender != Sender.Human) {
                item(key = "turn-header-${turn.messages.first().id}") {
                    Spacer(modifier = Modifier.height(spacing.xl))
                    TurnHeader()
                }
            }

            // Both message renderers share the same signature, so the Human/Junie turn
            // dispatch collapses into a single items(...) block parameterized by renderer.
            val renderer: @Composable (Message, String, Boolean, Map<String, Boolean>, (String) -> Unit) -> Unit =
                if (turn.sender == Sender.Human) {
                    { message, query, isMatch, blockStates, onToggle -> HumanMessageItem(message, query, isMatch, blockStates, onToggle) }
                } else {
                    { message, query, isMatch, blockStates, onToggle -> JunieMessageItem(message, query, isMatch, blockStates, onToggle) }
                }

            items(items = turn.messages, key = { it.id }) { message ->
                val isCurrentMatch = state.searchQuery.isNotBlank() && message.id == currentMatchMessageId
                renderer(
                    message,
                    state.searchQuery,
                    isCurrentMatch,
                    state.derivedBlockExpansionStates
                ) { blockId -> onAction(ConversationAction.OnToggleBlockExpansion(blockId)) }
            }
        }
    }

    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(listState),
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
    )
}
