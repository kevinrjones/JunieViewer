package com.knowledgespike.junieviewer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.ui.components.CodeBlock
import com.knowledgespike.junieviewer.ui.components.FilterBar
import com.knowledgespike.junieviewer.ui.components.SessionSelector
import com.knowledgespike.junieviewer.ui.components.SettingsDialog
import dev.snipme.highlights.model.SyntaxLanguage

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
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(state.selectedSessionId ?: "Select Session")
                        }

                        TextButton(
                            onClick = { onAction(ConversationAction.OnToggleSettings) },
                            modifier = Modifier.padding(end = 16.dp)
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
                        .padding(horizontal = 16.dp),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = state.filteredMessages,
                key = { it.id }
            ) { message ->
                MessageItem(message = message)
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val backgroundColor = when (message.sender) {
        Sender.Human -> MaterialTheme.colorScheme.primaryContainer
        Sender.Junie -> MaterialTheme.colorScheme.secondaryContainer
    }

    val alignment = Modifier.padding(
        start = if (message.sender == Sender.Human) 0.dp else 32.dp,
        end = if (message.sender == Sender.Human) 32.dp else 0.dp
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(alignment),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (message.sender == Sender.Human) "You" else "Junie",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            when (val content = message.content) {
                is MessageContent.Text -> Text(
                    text = content.text,
                    style = MaterialTheme.typography.bodyMedium
                )
                is MessageContent.Code -> CodeBlock(
                    code = content.code,
                    language = when (content.language.lowercase()) {
                        "json" -> SyntaxLanguage.JAVASCRIPT // JSON not available, using JS
                        "bash", "sh" -> SyntaxLanguage.SHELL
                        else -> SyntaxLanguage.KOTLIN
                    }
                )
                is MessageContent.Diff -> CodeBlock(
                    code = content.diff,
                    language = SyntaxLanguage.DEFAULT
                )
            }
        }
    }
}
