package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.ui.ConversationAction
import com.knowledgespike.junieviewer.ui.TopLevelSearchState
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

@Composable
fun TopLevelSearchDialog(
    state: TopLevelSearchState,
    onAction: (ConversationAction) -> Unit,
    onDismiss: () -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val spacing = JunieViewerTheme.spacing

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.85f)
                .testTag("top_level_search_dialog")
                .semantics { contentDescription = "Top-Level Session Search Dialog" }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.xxl)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top-Level Session Search",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("top_level_search_close")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Top-Level Search")
                    }
                }

                Spacer(modifier = Modifier.height(spacing.md))

                // Search Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    OutlinedTextField(
                        value = state.query.raw,
                        onValueChange = { onAction(ConversationAction.OnTopLevelSearchQueryChange(it)) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .testTag("top_level_search_input")
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                                    onAction(ConversationAction.OnSubmitTopLevelSearch)
                                    true
                                } else {
                                    false
                                }
                            },
                        placeholder = { Text("Search across all sessions...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onAction(ConversationAction.OnSubmitTopLevelSearch) })
                    )

                    Button(
                        onClick = { onAction(ConversationAction.OnSubmitTopLevelSearch) },
                        modifier = Modifier.testTag("top_level_search_submit")
                    ) {
                        Text("Search")
                    }
                }

                Spacer(modifier = Modifier.height(spacing.md))

                // Partial results warning if present
                if (state.results.isPartial) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing.md)
                            .testTag("top_level_search_partial_warning")
                    ) {
                        Row(
                            modifier = Modifier.padding(spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Warning: ${state.results.partialFailures.size} session(s) could not be scanned or were malformed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Content Area States
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when {
                        state.status == TopLevelSearchStatus.Running -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("top_level_search_loading"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(spacing.sm))
                                    Text("Searching sessions...")
                                }
                            }
                        }
                        state.status == TopLevelSearchStatus.EmptyQuery || state.query.isBlank -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("top_level_search_empty_query"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Enter a search query to search across all sessions.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        state.status == TopLevelSearchStatus.Failed || state.results.fatalError != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("top_level_search_error"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.results.fatalError ?: "Search failed.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        state.results.sessionResults.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("top_level_search_no_results"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matching sessions found.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("top_level_search_results"),
                                verticalArrangement = Arrangement.spacedBy(spacing.sm)
                            ) {
                                itemsIndexed(state.results.sessionResults) { index, result ->
                                    TopLevelSearchResultRow(
                                        result = result,
                                        index = index,
                                        onClick = { onAction(ConversationAction.OnTopLevelSearchResultSelected(result)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }
}

@Composable
fun TopLevelSearchResultRow(
    result: TopLevelSessionSearchResult,
    index: Int,
    onClick: () -> Unit
) {
    val spacing = JunieViewerTheme.spacing

    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("top_level_search_result_row_$index")
            .semantics { contentDescription = "Session result ${result.session.sessionId}, ${result.matchCount} matches" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.session.sessionId,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("top_level_search_result_session")
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "${result.matchCount} match${if (result.matchCount == 1) "" else "es"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(horizontal = spacing.sm, vertical = spacing.xs)
                            .testTag("top_level_search_result_match_count")
                    )
                }
            }

            if (!result.session.sessionPath.isBlank()) {
                Text(
                    text = result.session.sessionPath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = spacing.xs)
                )
            }

            val firstSnippet = result.summary.firstSnippet
            if (firstSnippet != null) {
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = buildString {
                        if (firstSnippet.hasLeadingEllipsis) append("…")
                        append(firstSnippet.preview)
                        if (firstSnippet.hasTrailingEllipsis) append("…")
                        if (result.summary.additionalSnippetCount > 0) {
                            append(" (+${result.summary.additionalSnippetCount} more)")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(top = spacing.xs)
                        .testTag("top_level_search_result_snippet")
                )
            }
        }
    }
}
