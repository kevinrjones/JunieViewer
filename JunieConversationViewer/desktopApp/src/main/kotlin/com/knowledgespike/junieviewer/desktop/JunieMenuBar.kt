package com.knowledgespike.junieviewer.desktop

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.knowledgespike.junieviewer.ui.ConversationCommand
import com.knowledgespike.junieviewer.ui.ConversationCommandState
import com.knowledgespike.junieviewer.ui.SortOrder

/**
 * Composable that renders the application menu bar.
 *
 * @param commandState The current state of conversation commands, used for enablement and labels.
 * @param onCommand Callback for when a conversation command is triggered.
 * @param onCopy Callback for the synthetic copy operation.
 * @param onQuit Callback for quitting the application.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.JunieMenuBar(
    commandState: ConversationCommandState,
    onCommand: (ConversationCommand) -> Unit,
    onCopy: () -> Unit,
    onQuit: () -> Unit
) {
    MenuBar {
        // File menu
        Menu("File") {
            Item(
                "Open Session…",
                shortcut = KeyShortcut(Key.O, meta = true),
                enabled = commandState.openSessionEnabled,
                onClick = { onCommand(ConversationCommand.OpenSession) }
            )
            Item(
                "Refresh",
                shortcut = KeyShortcut(Key.R, meta = true),
                enabled = commandState.refreshEnabled,
                onClick = { onCommand(ConversationCommand.Refresh) }
            )
            Separator()
            Item(
                "Quit",
                shortcut = KeyShortcut(Key.Q, meta = true),
                onClick = { onQuit() }
            )
        }

        // Edit menu
        Menu("Edit") {
            // Copy carries the standard Cmd+C / Ctrl+C accelerator so the menu item
            // looks and behaves like a conventional desktop Edit → Copy.
            Item(
                "Copy",
                shortcut = KeyShortcut(Key.C, meta = true),
                enabled = commandState.copyEnabled,
                onClick = { onCopy() }
            )
            Separator()
            Item(
                "Find…",
                shortcut = KeyShortcut(Key.F, meta = true),
                enabled = commandState.focusSearchEnabled,
                onClick = { onCommand(ConversationCommand.FocusSearch) }
            )
            Item(
                "Search All Sessions…",
                shortcut = KeyShortcut(Key.F, meta = true, shift = true),
                enabled = commandState.openTopLevelSearchEnabled,
                onClick = { onCommand(ConversationCommand.OpenTopLevelSearch) }
            )
            Item(
                "Find Next",
                shortcut = KeyShortcut(Key.G, meta = true),
                enabled = commandState.findNextEnabled,
                onClick = { onCommand(ConversationCommand.FindNext) }
            )
            Item(
                "Find Previous",
                shortcut = KeyShortcut(Key.G, meta = true, shift = true),
                enabled = commandState.findPreviousEnabled,
                onClick = { onCommand(ConversationCommand.FindPrevious) }
            )
        }

        // View menu
        Menu("View") {
            val sortLabel = when (commandState.sortOrder) {
                SortOrder.OldestFirst -> "Switch to Newest First"
                SortOrder.NewestFirst -> "Switch to Oldest First"
            }
            Item(
                sortLabel,
                enabled = commandState.toggleSortOrderEnabled,
                onClick = { onCommand(ConversationCommand.ToggleSortOrder) }
            )
            Separator()
            Item(
                "Collapse All",
                shortcut = KeyShortcut(Key.Minus, meta = true, shift = true),
                enabled = commandState.collapseAllEnabled,
                onClick = { onCommand(ConversationCommand.CollapseAll) }
            )
            Item(
                "Show All",
                shortcut = KeyShortcut(Key.Equals, meta = true, shift = true),
                enabled = commandState.showAllEnabled,
                onClick = { onCommand(ConversationCommand.ShowAll) }
            )
            Separator()
            val autoRefreshLabel = if (commandState.isAutoRefreshActive)
                "Disable Auto-Refresh" else "Enable Auto-Refresh"
            Item(
                autoRefreshLabel,
                shortcut = KeyShortcut(Key.R, meta = true, shift = true),
                enabled = commandState.toggleAutoRefreshEnabled,
                onClick = { onCommand(ConversationCommand.ToggleAutoRefresh) }
            )
        }

        // Session menu
        Menu("Session") {
            Item(
                "Reload from Disk",
                enabled = commandState.refreshEnabled,
                onClick = { onCommand(ConversationCommand.Refresh) }
            )
        }

        // Help menu
        Menu("Help") {
            Item(
                "How to Use",
                onClick = { onCommand(ConversationCommand.HowToUse) }
            )
            Item(
                "About Junie Conversation Viewer",
                onClick = { onCommand(ConversationCommand.About) }
            )
        }
    }
}
