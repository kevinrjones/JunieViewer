---
sprint: 5
name: Toolbar, Menu, and Navigation Controls
status: planned
---

# 1. Title

Sprint 5 — Toolbar, Menu, and Navigation Controls

# 2. Related Documents

- [`docs/sprints/junie-conversation-viewer-sprint-4-interaction-live-tracking-and-event-coverage.md`](junie-conversation-viewer-sprint-4-interaction-live-tracking-and-event-coverage.md) — the preceding sprint; Sprint 5 builds on its baseline of interactivity and live tracking.
- [`docs/tasks/junie-conversation-viewer-tasks-sprint-5-toolbar-menu-and-navigation-controls.md`](../tasks/junie-conversation-viewer-tasks-sprint-5-toolbar-menu-and-navigation-controls.md) — the companion task breakdown for this sprint.
- [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md) — canonical domain terms used consistently in code, tests, and UI copy.
- [`docs/RECAP.md`](../RECAP.md) — chronological project history.
- [`docs/TESTING.md`](../TESTING.md) — testing stack, Robot pattern, semantic `testTag` conventions, and Gradle commands.
- [`docs/project_memory.md`](../project_memory.md) — decisions, gotchas, and shipped work.
- [`docs/EVENT_CATALOG.md`](../../docs/EVENT_CATALOG.md) — catalogue of known Junie event types.

# 3. Sprint Goal

Add a full application toolbar and standard application menu to the Junie Conversation Viewer, providing unified command access through toolbar buttons, menu items, and keyboard shortcuts for all core viewer actions including Copy, Refresh, Open Session, Auto-Refresh toggle, Sort Order toggle, Collapse All/Show All, and Search Messages.

# 4. Current Baseline

## 4.1 Theme and UI State

- Sprint 4 (Interaction, Live Tracking, and Event Coverage) is complete: text selection, search highlighting, live tracking, filter coverage, and `AgentTaskFailedEvent` support are implemented.
- The UI follows an asymmetric layout with Human messages on the left and Junie messages on the right.
- `JunieViewerTheme` provides semantic tokens via `ConversationColors`.

## 4.2 Search State

- Search provides case-insensitive substring matching with visual highlighting of matches.
- Match navigation (prev/next) is functional with wrap-around support.
- Search currently occupies a dedicated chrome area within `ConversationScreen.kt`.

## 4.3 Session Loading State

- Session loading supports both initial one-shot parsing and live tracking via file watching/polling.
- New events are appended incrementally to the `ConversationState`.

## 4.4 Text Selection State

- Text selection is enabled across all major content blocks via `SelectionContainer`.
- Dedicated copy buttons exist for code, diff, and terminal blocks.

## 4.5 Filter State

- 6 toggle buttons (Human, Junie, Thoughts, Tools, Patches, Terminal) control visibility.
- `AlwaysShow` message kinds (Errors, Warnings, etc.) are always visible.

## 4.6 Desktop Entry Point

- Entry point: `desktopApp/src/main/kotlin/com/knowledgespike/junieviewer/main.kt`.
- The `Window` block currently lacks a `MenuBar`.
- No dedicated application toolbar exists; search and filter controls are currently part of the screen chrome.

# 5. Design Findings

## 5.1 LogViewer Toolbar Styling

- `FilterBar.kt` in the reference app uses a `filterBarIcon()` composable.
- Icons are 18dp inside a 28dp `IconButton`.
- 2dp vertical padding on icons.
- Toolbar uses a `Surface` with `elevation = 2.dp` and `MaterialTheme.colors.surface` background.
- `Divider` components serve as vertical separators between icon groups.
- 8dp horizontal padding for the toolbar container.

## 5.2 LogViewer Menu Implementation

- `MenuBar` is defined within `Main.kt`.
- Standard File, Edit, and View menus are provided.
- Keyboard shortcuts are managed via a `menuShortcutSetForOs()` utility to handle macOS vs. Windows/Linux differences.

## 5.3 LogViewer Command Model

- Uses an `AppMenuActionKey` enum to map menu items to specific intents or actions.
- This pattern allows multiple UI entry points (menu, toolbar, keyboard) to trigger the same logic.

## 5.4 Compose Desktop MenuBar API

- Compose Desktop provides a native `MenuBar` DSL usable inside the `Window` scope.
- `MenuBar { Menu("Name") { Item("Action", onClick = { ... }, shortcut = ...) } }`.

# 6. Scope

- **Toolbar UI:** A dedicated application toolbar with buttons for Copy, Refresh, Open Session, Auto-Refresh toggle, Sort Order toggle, Collapse All/Show All, and a Search Messages field.
- **Application Menu:** Native `MenuBar` with File, Edit, View, Session, and Help menus.
- **Shared Command/Action Model:** A unified model mapping toolbar, menu, and keyboard shortcuts to ViewModel actions.
- **Keyboard Shortcuts:** Full set of standard shortcuts (Cmd/Ctrl based) for all major actions.
- **Refresh and Auto-Refresh:** UI controls for manual refresh and toggling live tracking (auto-refresh).
- **Sort Order:** Control to toggle between Oldest-first (bottom-up) and Newest-first (top-down) message display.
- **Global Visibility Controls:** "Collapse All" and "Show All" commands for rich content blocks (Thoughts, Tool Calls).

# 7. Out of Scope

- Exporting Conversation to Markdown or HTML.
- Database ingestion or multi-session comparison.
- Mobile UI or Cloud/Remote sessions.
- "Open Recent Sessions" (deferred to Sprint 6).
- "Reveal Session in Finder" (deferred to Sprint 6).
- Application Settings dialog (deferred to Sprint 6).
- Submenus for Filters or Themes in the View menu (deferred).

# 8. User Stories

- As a **HITL**, I can access all major viewer actions from a consistent application menu, because native menus are a standard expectation for desktop productivity tools.
- As a **HITL**, I can trigger actions like Refresh and Search using keyboard shortcuts, because I want to work efficiently without switching between mouse and keyboard.
- As a **HITL**, I can use the toolbar to quickly toggle Auto-Refresh, because I need to control when the Conversation updates during live sessions.
- As a **HITL**, I can change the Sort Order of Messages to see the newest Messages at the top, because Newest-first is often better for monitoring active sessions.
- As a **HITL**, I can "Collapse All" rich content blocks with one click, because I want to quickly scan the high-level flow of a Conversation.
- As a **HITL**, I can "Show All" rich content blocks when I need to see full technical details across the entire Session.
- As a **HITL**, I can open a new Session using a dedicated toolbar button, because I frequently switch between different local logs.
- As a **HITL**, I can find common commands like Copy and Find in standard menu locations (Edit menu), because this matches my muscle memory from other apps.

# 9. Functional Requirements

- **FR1:** The application renders a native `MenuBar` at the top of the window (macOS) or within the window frame (Windows/Linux).
- **FR2:** A dedicated toolbar appears below the menu bar (or top of window) with styled icon buttons and separators.
- **FR3:** The toolbar includes buttons for: Open Session, Refresh, Auto-Refresh (toggle state), Sort Order (toggle state), Collapse All, Show All, and Copy.
- **FR4:** The toolbar includes a Search Messages text field with navigation (prev/next) integrated.
- **FR5:** A shared `ConversationCommand` model maps menu items, toolbar buttons, and shortcuts to `ConversationAction` intents.
- **FR6:** The File menu includes: Open Session, Quit.
- **FR7:** The Edit menu includes: Copy, Find (focus search).
- **FR8:** The View menu includes: Refresh, Toggle Auto-Refresh, Toggle Sort Order, Collapse All, Show All, Find Next, Find Previous.
- **FR9:** Keyboard shortcuts follow platform conventions (e.g., Cmd+R for Refresh on macOS, Ctrl+R on Windows).
- **FR10:** "Collapse All" minimizes all current Thought and Tool Call blocks in the view.
- **FR11:** "Show All" expands all current Thought and Tool Call blocks in the view.
- **FR12:** "Sort Order" toggles between Oldest-First (appended to bottom) and Newest-First (appended to top).
- **FR13:** "Auto-Refresh" toggle enables or disables live tracking of the `events.jsonl` file.
- **FR14:** Toolbar buttons provide tooltips with the command name and keyboard shortcut.
- **FR15:** Toolbar styling matches LogViewer (28dp buttons, 18dp icons, 2dp elevation).

# 10. Non-Functional Requirements

- **NFR1:** The toolbar does not conflict visually or functionally with the existing filter chips chrome.
- **NFR2:** Menu and toolbar actions respond with no perceptible latency.
- **NFR3:** Keyboard shortcuts do not conflict with system-level or standard Compose Desktop shortcuts.
- **NFR4:** Toolbar and Menu are fully theme-aware, adapting to Light and Dark modes.
- **NFR5:** Accessibility: All toolbar buttons have clear `contentDescription` for screen readers.
- **NFR6:** The application remains responsive when processing "Collapse All" or "Show All" on very large Conversations.

# 11. Design Principles

1. **Shared Command Model.** All UI triggers for a specific action (menu, toolbar, shortcut) must flow through a single command abstraction.
2. **LogViewer-Inspired Styling.** Maintain visual consistency with established "internal tool" aesthetics: compact, functional, and clear.
3. **Platform-Native Menus.** Use the native OS menu bar to provide a familiar and professional desktop experience.
4. **Progressive Disclosure.** Use tooltips and menus to reveal shortcuts and secondary actions without cluttering the main UI.
5. **Theme-Awareness.** Every addition must support both Light and Dark themes using semantic tokens.
6. **Accessible Interaction.** Ensure keyboard-only users can navigate and trigger all toolbar and menu actions.
7. **Non-Destructive Defaults.** State changes (like Sort Order or Collapse All) should be clear and reversible.

# 12. Proposed Visual System Additions

| Token | Light | Dark | Usage |
|---|---|---|---|
| `toolbarBackground` | `MaterialTheme.colors.surface` | `MaterialTheme.colors.surface` | Background of the toolbar surface |
| `toolbarElevation` | `2.dp` | `2.dp` | Shadow/elevation of the toolbar |
| `toolbarDivider` | `Color.LightGray` | `Color.DarkGray` | Vertical separator between icon groups |
| `toolbarIconTint` | `Color.Black` | `Color.White` | Tint for active toolbar icons |
| `toolbarIconDisabled` | `Color.Gray` | `Color.Gray` | Tint for disabled toolbar actions |

# 13. Theme Architecture Additions

## 13.1 Extended `ConversationColors`

Add toolbar tokens to the existing `ConversationColors` data class:

```kotlin
data class ConversationColors(
    // ... existing tokens ...
    val toolbarBackground: Color,
    val toolbarDivider: Color,
    val toolbarIconTint: Color,
    val toolbarIconDisabled: Color,
)
```

## 13.2 New Composables

- `ConversationToolbar`: The main toolbar container.
- `toolbarIcon`: A helper to render 18dp icons within 28dp buttons with standard padding.

# 14. Proposed Changes — Command/Action Model

Introduce a `ConversationCommand` sealed interface to unify UI triggers.

```kotlin
sealed interface ConversationCommand {
    object OpenSession : ConversationCommand
    object Refresh : ConversationCommand
    object ToggleAutoRefresh : ConversationCommand
    object ToggleSortOrder : ConversationCommand
    object CollapseAll : ConversationCommand
    object ShowAll : ConversationCommand
    object Copy : ConversationCommand
    object FocusSearch : ConversationCommand
    object FindNext : ConversationCommand
    object FindPrevious : ConversationCommand
    object Quit : ConversationCommand
    object About : ConversationCommand
}
```

**Files:** `ConversationAction.kt` (or new `ConversationCommand.kt`), `ConversationViewModel.kt`.

# 15. Proposed Changes — Toolbar UI

- Create `ConversationToolbar.kt` implementing the design findings (28dp buttons, 18dp icons).
- Group buttons logically: [Open] | [Refresh, Auto-Refresh] | [Sort Order] | [Collapse, Show] | [Copy].
- Integrate the Search field directly into the toolbar as a right-aligned (or central) element.
- Apply `Surface` with elevation.

**Files:** `ConversationToolbar.kt` (new), `ConversationScreen.kt`, `FilterBar.kt` (adjustment).

# 16. Proposed Changes — Menu Bar

- Update `main.kt` to include the `MenuBar` DSL inside the `Window` block.
- Map menu items to `ConversationCommand` which then dispatches to the ViewModel.
- Implement platform-specific shortcuts using `menuShortcutSetForOs()`.

**Files:** `main.kt`, `ConversationCommandMapper.kt` (new).

# 17. Proposed Changes — Refresh and Auto-Refresh

- Explicitly separate manual "Refresh" from "Auto-Refresh" (live tracking).
- Toolbar button for Auto-Refresh should show a "toggle on" or "active" state (e.g., highlight or specific icon).
- ViewModel manages the `LiveSessionTracker` lifecycle based on the Auto-Refresh state.

**Files:** `ConversationViewModel.kt`, `LiveSessionTracker.kt`.

# 18. Proposed Changes — Sort Order

- Add `SortOrder` enum: `OldestFirst` (default), `NewestFirst`.
- Update `ConversationState` to include `sortOrder`.
- ViewModel re-sorts the `messages` list whenever sort order changes.
- Ensure live tracking appends messages according to the current sort order.

**Files:** `ConversationState.kt`, `ConversationViewModel.kt`, `ConversationToolbar.kt`.

# 19. Proposed Changes — Collapse All / Show All

- Add `isExpanded` state to `ThoughtBlock` and `ToolCallBlock` that can be controlled externally.
- ViewModel emits a global "collapse/expand all" signal or updates all message states.
- Implementation: either a shared flow of "expansion events" or a property on the Message/UI state.

**Files:** `MessageItems.kt`, `ThoughtBlock.kt`, `ToolCallBlock.kt`, `ConversationViewModel.kt`.

# 20. Proposed Changes — Copy and Search Integration

- Move the Search UI from the content chrome into the toolbar.
- The "Copy" command in the menu/toolbar should trigger the standard copy action (Copy selected text if available).

**Files:** `ConversationScreen.kt`, `ConversationToolbar.kt`, `MessageItems.kt`.

# 21. Accessibility

- **A1:** Toolbar buttons must have `Modifier.semantics { contentDescription = "..." }`.
- **A2:** Native menu bar provides standard keyboard accessibility for screen readers.
- **A3:** All icons have corresponding text tooltips for hovered users.
- **A4:** Focus indicators must be clearly visible when navigating the toolbar via Tab.
- **A5:** Sort order changes should be announced or visually obvious (e.g., "Sorted by Newest").
- **A6:** Collapse/Show All state changes should be reflected in the semantics tree.

# 22. Cross-Platform Considerations

- **C1:** macOS uses the system menu bar (top of screen); Windows/Linux use the in-window menu bar.
- **C2:** Use `menuShortcutSetForOs()` to map `Meta` (Cmd) on macOS and `Ctrl` on others.
- **C3:** Native `MenuBar` in Compose Desktop handles platform differences in menu rendering.
- **C4:** Verify that toolbar elevation and dividers render clearly on all three target OSs.
- **C5:** Verify keyboard shortcuts don't conflict with OS-level shortcuts (e.g., Alt+F4, Cmd+Q).

# 23. Testing Strategy

## 23.1 Automated Tests

- **Command Model Tests:** Verify that `ConversationCommand` triggers the expected `ConversationAction` in the ViewModel.
- **Sort Order Tests:** Unit tests for message list sorting logic (Oldest vs Newest).
- **Collapse/Show All Tests:** Verify UI state changes when these commands are triggered.
- **Auto-Refresh Toggle Tests:** Verify `LiveSessionTracker` starts/stops correctly.
- **Robot-pattern UI Tests:** Test toolbar button clicks and search field interaction.

## 23.2 Manual Review Checklist

- **Visual Review:** Toolbar styling, spacing, and icons in both Light and Dark themes.
- **Menu Review:** All menus (File, Edit, View, Session, Help) present and functional on macOS.
- **Shortcut Verification:** Cmd+R, Cmd+F, Cmd+C, etc. working as expected.
- **Responsive Layout:** Toolbar behaviour when the window is very narrow (overflow or wrapping).
- **UX Flow:** Verify that "Collapse All" followed by Search still expands the matching block.

# 24. Incremental Delivery Plan

## Part 1 — Discovery and Scope Confirmation
- **Objective:** Finalize toolbar icon set, confirm exact keyboard shortcuts, and verify `MenuBar` API limitations.
- **Files:** `main.kt`, `ConversationToolbar.kt`.
- **After:** *The HITL sees a documented confirmation of the visual and command design.*

## Part 2 — Command/Action Model Design
- **Objective:** Implement the `ConversationCommand` interface and map it to ViewModel actions.
- **Files:** `ConversationAction.kt`, `ConversationViewModel.kt`.
- **After:** *The internal logic is ready to receive commands from multiple UI entry points.*

## Part 3 — Toolbar UI
- **Objective:** Create the basic `ConversationToolbar` with static buttons and separators.
- **Files:** `ConversationToolbar.kt`, `ConversationScreen.kt`.
- **After:** *A placeholder toolbar is visible at the top of the application.*

## Part 4 — Menu Bar
- **Objective:** Implement the native `MenuBar` in `main.kt` with platform-specific shortcuts.
- **Files:** `main.kt`.
- **After:** *Standard desktop menus are available and functional.*

## Part 5 — Refresh and Auto-Refresh Control
- **Objective:** Wire up the Refresh button and Auto-Refresh toggle to the `LiveSessionTracker`.
- **Files:** `ConversationViewModel.kt`, `ConversationToolbar.kt`.
- **After:** *The HITL can manually refresh or toggle live updates from the toolbar/menu.*

## Part 6 — Sort Order
- **Objective:** Implement Newest-first and Oldest-first sorting logic.
- **Files:** `ConversationState.kt`, `ConversationViewModel.kt`, `ConversationToolbar.kt`.
- **After:** *The Conversation can be flipped to show new messages at the top.*

## Part 7 — Collapse All / Show All
- **Objective:** Implement global expansion/collapse logic for Thoughts and Tool Calls.
- **Files:** `MessageItems.kt`, `ConversationViewModel.kt`.
- **After:** *The HITL can quickly expand or collapse all technical blocks.*

## Part 8 — Copy and Search Integration
- **Objective:** Move the Search field into the toolbar and wire up the Copy command.
- **Files:** `ConversationToolbar.kt`, `ConversationScreen.kt`.
- **After:** *The toolbar is fully functional, hosting the primary search and copy controls.*

## Part 9 — Documentation Updates
- **Objective:** Update README and project memory to reflect the new navigation system.
- **Files:** `README.md`, `project_memory.md`.
- **After:** *User and developer docs are up to date.*

## Part 10 — Testing, Review, and Completion
- **Objective:** Run automated suites and perform manual cross-platform checks.
- **Files:** All test files.
- **After:** *Sprint 5 is verified and ready for sign-off.*

# 25. Risks and Mitigations

- **R1 — Compose Desktop `MenuBar` limitations:** Certain native menu features may be restricted or behave differently across OSs. *Mitigation:* Stick to standard Menu/Item/Separator DSL; test early on all platforms.
- **R2 — Toolbar crowding:** Adding many buttons plus a search field may cause crowding on narrow windows. *Mitigation:* Use icons only (no labels), or allow the search field to shrink.
- **R3 — Sort order interaction with live tracking:** Appending messages to the top (Newest-first) might be disorienting during live updates. *Mitigation:* Ensure scroll position is handled gracefully in both modes.
- **R4 — Clipboard API limitations:** Accessing the system clipboard from a global command might have focus-related edge cases. *Mitigation:* Use Compose's `ClipboardManager` and test within the Command flow.
- **R5 — Scope creep:** 10 delivery parts is ambitious. *Mitigation:* Maintain strict focus on toolbar/menu and defer non-essential features (like Recent Sessions).
- **R6 — Platform-specific menu behavior:** macOS vs Windows/Linux menu placement. *Mitigation:* Rely on Compose Desktop's built-in `MenuBar` which abstracts most of this.

# 26. Open Questions

- **Q1: Should the Open button open the existing Session picker, a native file chooser, or both?**
  - Recommendation: Use existing Session picker for consistency. **HITL required.**
- **Q2: Should auto-refresh enabled/disabled be persisted across app launches?**
  - Recommendation: Yes, persist in Preferences alongside ThemeMode. **HITL required.**
- **Q3: Should sort order be persisted across app launches?**
  - Recommendation: Yes, persist in Preferences. **HITL required.**
- **Q4: What exact keyboard shortcuts should be used on macOS and Windows/Linux?**
  - Recommendation: Follow platform conventions (Cmd+R refresh, Cmd+F find, Cmd+C copy, Cmd+N newest-first toggle). **HITL required.**
- **Q5: Should Copy copy only selected text, the current Message, or fall back to something else when no text is selected?**
  - Recommendation: Copy selected text if available; if no selection, copy nothing (standard behaviour). **HITL required.**
- **Q6: Should Collapse All affect only rich content blocks or also any future grouped Messages?**
  - Recommendation: Only current collapsible blocks (ThoughtBlock, ToolCallBlock). **HITL required.**
- **Q7: When Search finds a match inside a collapsed block, should Search force-expand that block even after Collapse All?**
  - Recommendation: Yes, search should force-expand matching blocks to ensure the user can see what they found. **HITL required.**
- **Q8: Should the menu include Open Recent Sessions in Sprint 5 or defer it?**
  - Recommendation: Defer to Sprint 6 to maintain focus. **HITL required.**
- **Q9: Should toolbar buttons use icons only, icons plus labels, or adaptive labels?**
  - Recommendation: Icons only with tooltips, matching LogViewer style for a compact UI. **HITL required.**
- **Q10: Should refresh preserve current scroll position, jump to current match, or jump according to sort order?**
  - Recommendation: Preserve current scroll position to avoid disorienting the user. **HITL required.**
- **Q11: Should newest-first mode place newly appended live Messages at the top immediately?**
  - Recommendation: Yes, new Messages appear at top in newest-first; at bottom in oldest-first. **HITL required.**
- **Q12: Should the existing filter chips remain below the toolbar, move into the toolbar, or become a View menu/filter submenu?**
  - Recommendation: Keep filter chips below toolbar in current position to avoid toolbar overcrowding. **HITL required.**

# 27. Definition of Done

This sprint is complete when all the following conditions are met:

- A functional `MenuBar` is present with File, Edit, View, Session, and Help menus.
- A functional Toolbar is present with buttons for Open, Refresh, Auto-Refresh, Sort Order, Collapse All, Show All, and Copy.
- A Search Messages field is integrated into the Toolbar with match navigation.
- All actions are wired through a shared `ConversationCommand` model.
- Keyboard shortcuts for all major actions are implemented and verified on macOS.
- "Sort Order" correctly toggles between Oldest-First and Newest-First modes.
- "Collapse All" and "Show All" correctly expand/minimize all Thoughts and Tool Calls.
- "Auto-Refresh" correctly toggles the live tracking of session logs.
- Toolbar and Menu styling is theme-aware and visually consistent with LogViewer.
- All new components have unit tests and Robot-pattern UI tests where applicable.
- All existing tests pass.
- Documentation (README, project_memory, RECAP) is updated to reflect Sprint 5 changes.
- HITL final approval is granted.
