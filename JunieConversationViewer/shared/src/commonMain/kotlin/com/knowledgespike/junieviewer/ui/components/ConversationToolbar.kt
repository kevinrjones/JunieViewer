package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import com.knowledgespike.junieviewer.ui.*
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/** Icon size inside toolbar buttons, matching LogViewer's 18dp pattern. */
private val TOOLBAR_ICON_SIZE = 18.dp

/** Button size for toolbar icon buttons, matching LogViewer's 28dp pattern. */
private val TOOLBAR_BUTTON_SIZE = 28.dp

/** Minimum width for the search field to remain usable. */
private val SEARCH_FIELD_MIN_WIDTH = 120.dp

/** Height of the vertical divider separating toolbar button groups. */
private val TOOLBAR_DIVIDER_HEIGHT = 20.dp

/** Icon size for the compact search-field clear button. */
private val SEARCH_CLEAR_ICON_SIZE = 14.dp

/** Vertical gap between a toolbar button and its tooltip, in pixels. */
private const val TOOLTIP_ANCHOR_SPACING_PX = 8

/**
 * Positions a tooltip above its anchor, horizontally centred but clamped so the tooltip
 * never overflows the window edges. The Material default centres the tooltip over the
 * anchor without clamping, so tooltips for buttons near the left edge (e.g. "Open Session")
 * get clipped off-screen; this provider keeps them fully visible.
 */
private class ClampingTooltipPositionProvider(
    private val spacingPx: Int = TOOLTIP_ANCHOR_SPACING_PX
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        // Centre horizontally over the anchor, then clamp within the window bounds.
        val preferredX = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = preferredX.coerceIn(0, maxX)

        // Prefer above the anchor; fall back to below when there is not enough room.
        val above = anchorBounds.top - popupContentSize.height - spacingPx
        val below = anchorBounds.bottom + spacingPx
        val y = if (above >= 0) above else below
        return IntOffset(x, y)
    }
}

/**
 * Application toolbar providing unified command access for the Conversation Viewer.
 *
 * Sits above the conversation content and filter chips. Styled to match LogViewer's
 * compact toolbar: Surface with 2dp elevation, 28dp icon buttons with 18dp icons,
 * Divider separators between logical groups.
 *
 * Button groups (left to right):
 * 1. Session/file: Open Session, Refresh
 * 2. Copy
 * 3. Search field with Find Previous / Find Next
 * 4. Live/view: Auto-Refresh toggle, Sort Order toggle
 * 5. Expansion: Collapse All, Show All
 */
@Composable
fun ConversationToolbar(
    state: ConversationState,
    commandState: ConversationCommandState,
    onCommand: (ConversationCommand) -> Unit,
    onCopySelectedText: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    searchFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val spacing = JunieViewerTheme.spacing

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth().testTag("conversation_toolbar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group 1: Session/file commands
            ToolbarIconButton(
                icon = Icons.Default.FolderOpen,
                contentDesc = "Open Session",
                tooltip = "Open Session",
                enabled = commandState.openSessionEnabled,
                onClick = { onCommand(ConversationCommand.OpenSession) },
                testTag = "toolbar_open_session"
            )
            ToolbarIconButton(
                icon = Icons.Default.Refresh,
                contentDesc = "Refresh",
                tooltip = "Refresh",
                enabled = commandState.refreshEnabled,
                onClick = { onCommand(ConversationCommand.Refresh) },
                testTag = "toolbar_refresh"
            )

            ToolbarDivider()

            // Group 2: Copy — dispatched via dedicated callback so the desktop layer
            // can route it directly to the platform clipboard without going through
            // the ViewModel event channel (which would cause a menu-accelerator loop).
            // Non-focusable so clicking it does not steal Compose keyboard focus away from
            // the Message text that has the actual selection — otherwise the synthetic
            // Ctrl+C/Cmd+C key event dispatched afterwards would target this button instead
            // of reaching the SelectionContainer holding the selected text.
            ToolbarIconButton(
                icon = Icons.Default.ContentCopy,
                contentDesc = "Copy",
                tooltip = "Copy",
                enabled = commandState.copyEnabled,
                onClick = { onCopySelectedText() },
                testTag = "toolbar_copy",
                focusable = false
            )

            ToolbarDivider()

            // Group 3: Live/view controls
            ToolbarIconButton(
                icon = Icons.Default.Autorenew,
                contentDesc = "Auto-Refresh",
                tooltip = if (commandState.isAutoRefreshActive) "Auto-Refresh: On" else "Auto-Refresh: Off",
                enabled = commandState.toggleAutoRefreshEnabled,
                selected = commandState.isAutoRefreshActive,
                onClick = { onCommand(ConversationCommand.ToggleAutoRefresh) },
                testTag = "toolbar_auto_refresh"
            )
            ToolbarIconButton(
                icon = Icons.AutoMirrored.Filled.Sort,
                contentDesc = "Sort Order",
                tooltip = when (commandState.sortOrder) {
                    SortOrder.OldestFirst -> "Sort: Oldest First"
                    SortOrder.NewestFirst -> "Sort: Newest First"
                },
                enabled = commandState.toggleSortOrderEnabled,
                onClick = { onCommand(ConversationCommand.ToggleSortOrder) },
                testTag = "toolbar_sort_order"
            )

            ToolbarDivider()

            // Group 4: Expansion controls
            ToolbarIconButton(
                icon = Icons.Default.UnfoldLess,
                contentDesc = "Collapse All",
                tooltip = "Collapse All",
                enabled = commandState.collapseAllEnabled,
                onClick = { onCommand(ConversationCommand.CollapseAll) },
                testTag = "toolbar_collapse_all"
            )
            ToolbarIconButton(
                icon = Icons.Default.UnfoldMore,
                contentDesc = "Show All",
                tooltip = "Show All",
                enabled = commandState.showAllEnabled,
                onClick = { onCommand(ConversationCommand.ShowAll) },
                testTag = "toolbar_show_all"
            )

            ToolbarDivider()

            // Group 5: Search field with navigation — last item, fills remaining width
            val isSearchOrFilterActive = state.searchQuery.isNotBlank() || !state.filter.isDefault()
            ToolbarSearchField(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                matchCount = if (isSearchOrFilterActive) state.filteredMessages.size else 0,
                currentMatchIndex = state.currentMatchIndex,
                totalCount = state.messages.size,
                isSearchOrFilterActive = isSearchOrFilterActive,
                findNextEnabled = commandState.findNextEnabled,
                findPreviousEnabled = commandState.findPreviousEnabled,
                onFindNext = { onCommand(ConversationCommand.FindNext) },
                onFindPrevious = { onCommand(ConversationCommand.FindPrevious) },
                focusRequester = searchFocusRequester,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Reusable toolbar icon button matching LogViewer's filterBarIcon pattern.
 *
 * 28dp button containing an 18dp icon with accessible tooltip and content description.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolbarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    tooltip: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
    selected: Boolean = false,
    focusable: Boolean = true
) {
    val tint = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    TooltipBox(
        positionProvider = ClampingTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(tooltip) } },
        state = rememberTooltipState()
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(TOOLBAR_BUTTON_SIZE)
                .testTag(testTag)
                .semantics { contentDescription = contentDesc }
                .focusProperties { canFocus = focusable }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(TOOLBAR_ICON_SIZE)
            )
        }
    }
}

/** Vertical divider separating toolbar button groups. */
@Composable
private fun ToolbarDivider() {
    val spacing = JunieViewerTheme.spacing
    Spacer(modifier = Modifier.width(spacing.sm))
    VerticalDivider(
        modifier = Modifier.height(TOOLBAR_DIVIDER_HEIGHT),
        color = MaterialTheme.colorScheme.outlineVariant
    )
    Spacer(modifier = Modifier.width(spacing.sm))
}

/**
 * Search field embedded in the toolbar with match count and Find Previous/Next controls.
 */
@Composable
private fun ToolbarSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    matchCount: Int,
    currentMatchIndex: Int,
    totalCount: Int,
    isSearchOrFilterActive: Boolean,
    findNextEnabled: Boolean,
    findPreviousEnabled: Boolean,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .widthIn(min = SEARCH_FIELD_MIN_WIDTH)
                .focusRequester(focusRequester)
                .testTag("search_field"),
            placeholder = { Text("Search Messages...", style = MaterialTheme.typography.bodySmall) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier
                            .size(TOOLBAR_BUTTON_SIZE)
                            .testTag("search_clear_button")
                            .semantics { contentDescription = "Clear search" }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SEARCH_CLEAR_ICON_SIZE),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        // Result count and navigation — shown when search or filters are active
        if (isSearchOrFilterActive) {
            val spacing = JunieViewerTheme.spacing
            Spacer(modifier = Modifier.width(spacing.sm))
            val countText = if (matchCount == 0) "No matching Messages"
                else "$matchCount of $totalCount Messages"
            Text(
                text = countText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("result_count")
            )

            if (matchCount > 1) {
                val matchLabel = if (currentMatchIndex >= 0) "${currentMatchIndex + 1} / $matchCount" else ""
                Text(
                    text = matchLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = spacing.sm).testTag("match_position")
                )
            }

            ToolbarIconButton(
                icon = Icons.Default.KeyboardArrowUp,
                contentDesc = "Previous match",
                tooltip = "Find Previous",
                enabled = findPreviousEnabled,
                onClick = onFindPrevious,
                testTag = "prev_match_button"
            )
            ToolbarIconButton(
                icon = Icons.Default.KeyboardArrowDown,
                contentDesc = "Next match",
                tooltip = "Find Next",
                enabled = findNextEnabled,
                onClick = onFindNext,
                testTag = "next_match_button"
            )
        }
    }
}
