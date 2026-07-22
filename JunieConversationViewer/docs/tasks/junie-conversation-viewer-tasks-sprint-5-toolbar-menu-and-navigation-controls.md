# Sprint 5 — Toolbar, Menu, and Navigation Controls: Task Breakdown

## 1. Related Sprint

**Sprint document:** [`docs/sprints/junie-conversation-viewer-sprint-5-toolbar-menu-and-navigation-controls.md`](../sprints/junie-conversation-viewer-sprint-5-toolbar-menu-and-navigation-controls.md)

**Sprint goal:** Add a full application toolbar and standard application menu to the Junie Conversation Viewer, providing unified command access through toolbar buttons, menu items, and keyboard shortcuts.

## 2. Related Documents

| Document | Role |
|----------|------|
| [`docs/sprints/junie-conversation-viewer-sprint-5-toolbar-menu-and-navigation-controls.md`](../sprints/junie-conversation-viewer-sprint-5-toolbar-menu-and-navigation-controls.md) | **Primary source of truth.** Defines the delivery parts and requirements. |
| [`docs/sprints/junie-conversation-viewer-sprint-4-interaction-live-tracking-and-event-coverage.md`](../sprints/junie-conversation-viewer-sprint-4-interaction-live-tracking-and-event-coverage.md) | Preceding sprint; Sprint 5 builds on its baseline. |
| [`docs/tasks/junie-conversation-viewer-tasks-sprint-4-interaction-live-tracking-and-event-coverage.md`](junie-conversation-viewer-tasks-sprint-4-interaction-live-tracking-and-event-coverage.md) | Sprint 4 task breakdown for reference. |
| [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md) | Canonical domain terms. |
| [`docs/RECAP.md`](../RECAP.md) | Chronological project history. |
| [`docs/TESTING.md`](../TESTING.md) | Testing stack, Robot pattern, `testTag` conventions. |
| [`docs/project_memory.md`](../project_memory.md) | Decisions, gotchas, shipped work. |
| [`docs/EVENT_CATALOG.md`](../../docs/EVENT_CATALOG.md) | Catalogue of known Junie event types. |
| [`docs/sprint-5-area-1-discovery-findings.md`](../sprint-5-area-1-discovery-findings.md) | Area 1 discovery findings and design recommendations. |

## 3. Purpose

This document breaks Sprint 5 into concrete, trackable tasks. It serves as:

- **Junie's implementation checklist** — each task has clear completion criteria, dependencies, and testing expectations.
- **HITL's review and progress checklist** — each task has a checkbox, and review-oriented tasks include HITL-visible outcomes.

## 4. How to Use This Task Document

1. **Before starting implementation**, read the Related Documents listed above.
2. **Work through tasks in area order** (1–10). Within each area, tasks are ordered by dependency.
3. **Check off tasks** (`- [x]`) only when all completion criteria are met.
4. **Mark parent tasks complete** only when all subtasks are complete.
5. **Use inline markers** (see Task Status Legend) to flag blocked, deferred, or review-dependent tasks.
6. **Update the Progress Summary** table as areas are completed.

## 5. Progress Summary

| # | Task Area | Status                               | Task Count |
|---|-----------|--------------------------------------|------------|
| 1 | Discovery and Scope Confirmation | 9/9 complete                         | 9 |
| 2 | Command/Action Model Design | 5/5 complete                         | 5 |
| 3 | Toolbar UI | 8/9 complete (awaiting HITL review)  | 9 |
| 4 | Menu Bar | 7/7 complete (awaiting HITL review)  | 7 |
| 5 | Refresh and Auto-Refresh Control | 6/6 complete (manual review pending) | 6 |
| 6 | Sort Order | 7/7 complete (awaiting HITL review)  | 7 |
| 7 | Collapse All / Show All | 6/6 complete (awaiting HITL review)  | 6 |
| 8 | Copy and Search Integration | 6/6 complete                         | 6 |
| 9 | Documentation Updates | 5/5 complete                         | 5 |
| 10 | Testing, Review, and Completion | 7/7 complete (awaiting HITL review)  | 7 |
| | **Total** |                                      | **67** |

## 6. Task Status Legend

- `- [ ]` — Task not started or not complete.
- `- [x]` — Task complete and reviewed where review is required.

**Inline markers:**

- **`HITL Review`** — Task requires HITL visual or functional review before it can be marked complete.
- **`Blocked`** — Task is blocked by an external dependency or unresolved question.
- **`Deferred`** — Task has been explicitly moved out of this sprint's scope.
- **`Depends on [task]`** — Task depends on another task being completed first.
- **`Test Required`** — Task must have automated test coverage before completion.
- **`Manual Review Required`** — Task requires manual verification.

---

## 7. Implementation Task List

### Area 1 — Discovery and Scope Confirmation

*Source: Delivery Part 1. Confirms current top chrome, search field, filter bar, and menu support before code changes.*

#### 1.1 Read project documentation

- [x] Read project documentation

**Description:** Read `UBIQUITOUS-LANGUAGE.md`, `TESTING.md`, `project_memory.md`, `RECAP.md`, `EVENT_CATALOG.md`, and Sprint 4 docs to understand the current baseline.

**Source:** Sprint doc section 2 (Related Documents).

**Dependencies:** None.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- Key terminology and existing UI patterns are understood.

**Testing expectations:** None.

#### 1.2 Inspect current top chrome, search field, and filter bar implementation

- [x] Inspect current top chrome, search field, and filter bar implementation

**Description:** Analyze how the search field and filter bar are currently integrated into the screen layout.

**Source:** Delivery Part 1.

**Dependencies:** 1.1.

**Likely files / areas:** `ConversationScreen.kt`, `FilterBar.kt`.

**Completion criteria:**
- Implementation details of existing top UI elements are documented.

**Testing expectations:** None.

#### 1.3 Inspect existing live tracking and refresh/session-loading flow

- [x] Inspect existing live tracking and refresh/session-loading flow

**Description:** Understand how the session is loaded and how live tracking appends events.

**Source:** Delivery Part 1.

**Dependencies:** 1.1.

**Likely files / areas:** `ConversationViewModel.kt`, `LiveSessionTracker.kt`, `SessionRepository.kt`.

**Completion criteria:**
- Current refresh and tracking logic is understood.

**Testing expectations:** None.

#### 1.4 Inspect current collapsible block state model

- [x] Inspect current collapsible block state model

**Description:** Analyze how `ThoughtBlock` and `ToolCallBlock` manage their expansion state.

**Source:** Delivery Part 1.

**Dependencies:** 1.1.

**Likely files / areas:** `ThoughtBlock.kt`, `ToolCallBlock.kt`, `MessageItems.kt`.

**Completion criteria:**
- Expansion state management is understood.

**Testing expectations:** None.

#### 1.5 Inspect current text selection/copy capabilities and Compose Desktop clipboard limitations

- [x] Inspect current text selection/copy capabilities and Compose Desktop clipboard limitations

**Description:** Research how text selection works across the app and any clipboard API constraints.

**Source:** Delivery Part 1.

**Dependencies:** 1.1.

**Likely files / areas:** `ConversationScreen.kt`, `SelectionContainer` usage.

**Completion criteria:**
- Selection and clipboard limitations are documented.

**Testing expectations:** None.

#### 1.6 Inspect current desktop app entry point and menu support

- [x] Inspect current desktop app entry point and menu support

**Description:** Check the current `Window` setup in `main.kt` for menu bar support.

**Source:** Delivery Part 1.

**Dependencies:** 1.1.

**Likely files / areas:** `main.kt`.

**Completion criteria:**
- Desktop entry point structure is verified.

**Testing expectations:** None.

#### 1.7 Inspect LogViewer toolbar/menu styling and summarize relevant guidelines

- [x] Inspect LogViewer toolbar/menu styling and summarize relevant guidelines

**Description:** Audit the reference app's toolbar and menu implementation for visual consistency.

**Source:** Delivery Part 1.

**Dependencies:** 1.1.

**Likely files / areas:** LogViewer source files.

**Completion criteria:**
- Visual guidelines for the toolbar and menu are summarized.

**Testing expectations:** None.

#### 1.8 Identify open questions and design recommendations

- [x] Identify open questions and design recommendations

**Description:** List all design decisions needing HITL input.

**Source:** Delivery Part 1.

**Dependencies:** 1.2, 1.3, 1.4, 1.5, 1.6, 1.7.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- Open questions (Q1-Q12) are formalized.

**Testing expectations:** None.

#### 1.9 HITL review of discovery findings — `HITL Review`

- [x] HITL review of discovery findings — **HITL review complete (2026-07-18)**

**Description:** Present discovery audit and design recommendations to HITL. See [`docs/sprint-5-area-1-discovery-findings.md`](../sprint-5-area-1-discovery-findings.md) for the full audit.

**Source:** Delivery Part 1.

**Dependencies:** 1.8.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- HITL approval of discovery findings and design direction.

**Testing expectations:** None.

---

### Area 2 — Command/Action Model Design

*Source: Delivery Part 2. Defines a unified command model for all UI entry points.*

#### 2.1 Define shared command sealed interface (`ConversationCommand`)

- [x] Define shared command sealed interface (`ConversationCommand`)

**Description:** Create a sealed interface for commands: Copy, Refresh, OpenSession, ToggleAutoRefresh, ToggleSortOrder, CollapseAll, ShowAll, FocusSearch, FindNext, FindPrevious, Settings, Quit, About.

**Source:** Delivery Part 2.

**Dependencies:** 1.9.

**Likely files / areas:** `ConversationCommand.kt`.

**Completion criteria:**
- `ConversationCommand` interface is defined with all required cases.

**Testing expectations:** Compilation check.

#### 2.2 Map each command to existing or new ConversationAction/ViewModel functionality

- [x] Map each command to existing or new ConversationAction/ViewModel functionality

**Description:** Ensure the ViewModel can handle all defined commands.

**Source:** Delivery Part 2.

**Dependencies:** 2.1.

**Likely files / areas:** `ConversationViewModel.kt`, `ConversationAction.kt`.

**Completion criteria:**
- ViewModel dispatches or handles all `ConversationCommand` intents.

**Testing expectations:** None.

#### 2.3 Define enabled/disabled state rules for each command

- [x] Define enabled/disabled state rules for each command

**Description:** Define logic for when commands should be active (e.g., Copy needs selection).

**Source:** Delivery Part 2.

**Dependencies:** 2.2.

**Likely files / areas:** `ConversationViewModel.kt`.

**Completion criteria:**
- Command enablement rules are defined and implemented.

**Testing expectations:** None.

#### 2.4 Define keyboard shortcuts table (macOS primary, Windows/Linux noted)

- [x] Define keyboard shortcuts table

**Description:** Map each command to platform-appropriate keyboard shortcuts.

**Source:** Delivery Part 2.

**Dependencies:** 2.2.

**Likely files / areas:** `main.kt` or shortcut mapping file.

**Completion criteria:**
- Shortcut table is finalized and documented.

**Testing expectations:** None.

#### 2.5 Add tests for command-state logic — `Test Required`

- [x] Add tests for command-state logic

**Description:** Verify that commands trigger the correct state changes or actions.

**Source:** Delivery Part 2.

**Dependencies:** 2.3.

**Likely files / areas:** `ConversationViewModelTest.kt`.

**Completion criteria:**
- Automated tests verify command-to-action mapping and enablement logic.

**Testing expectations:** JUnit tests pass.

---

### Area 3 — Toolbar UI

*Source: Delivery Part 3. Implements the visual toolbar using LogViewer-inspired styling.*

#### 3.1 Create ConversationToolbar composable

- [x] Create ConversationToolbar composable

**Description:** Implement the main toolbar container with Surface, elevation, and background.

**Source:** Delivery Part 3.

**Dependencies:** 2.5.

**Likely files / areas:** `ConversationToolbar.kt`.

**Completion criteria:**
- `ConversationToolbar` container renders correctly.

**Testing expectations:** Visual check.

#### 3.2 Create toolbarIcon helper composable

- [x] Create toolbarIcon helper composable

**Description:** Implement the reusable icon button with 18dp icon and 28dp button size.

**Source:** Delivery Part 3.

**Dependencies:** 3.1.

**Likely files / areas:** `ConversationToolbar.kt`.

**Completion criteria:**
- `toolbarIcon` helper is functional and styled.

**Testing expectations:** Visual check.

#### 3.3 Add toolbar buttons: Copy, Refresh, Open Session, Auto-Refresh toggle, Sort Order toggle, Collapse All, Show All

- [x] Add toolbar buttons

**Description:** Populate the toolbar with buttons and dividers.

**Source:** Delivery Part 3.

**Dependencies:** 3.2.

**Likely files / areas:** `ConversationToolbar.kt`.

**Completion criteria:**
- All 7 specified buttons are present in the toolbar.

**Testing expectations:** Visual check.

#### 3.4 Move/include Search Messages field in toolbar

- [x] Move/include Search Messages field in toolbar

**Description:** Integrate the search field and navigation controls into the toolbar.

**Source:** Delivery Part 3.

**Dependencies:** 3.3.

**Likely files / areas:** `ConversationToolbar.kt`, `ConversationScreen.kt`.

**Completion criteria:**
- Search UI is moved to the toolbar without losing functionality.

**Testing expectations:** Search and match navigation still work.

#### 3.5 Add accessible labels/tooltips to all toolbar buttons

- [x] Add accessible labels/tooltips to all toolbar buttons

**Description:** Add `contentDescription` and hover tooltips to all buttons.

**Source:** Delivery Part 3.

**Dependencies:** 3.3.

**Likely files / areas:** `ConversationToolbar.kt`.

**Completion criteria:**
- All buttons have accessible labels and tooltips.

**Testing expectations:** Screen reader/hover check.

#### 3.6 Add iconography using Compose Material Icons

- [x] Add iconography using Compose Material Icons

**Description:** Select and apply appropriate icons for all actions.

**Source:** Delivery Part 3.

**Dependencies:** 3.3.

**Likely files / areas:** `ConversationToolbar.kt`.

**Completion criteria:**
- Icons are consistent and meaningful.

**Testing expectations:** Visual check.

#### 3.7 Support light/dark theme for toolbar

- [x] Support light/dark theme for toolbar

**Description:** Use `ConversationColors` to theme the toolbar background, dividers, and icons.

**Source:** Delivery Part 3.

**Dependencies:** 3.1.

**Likely files / areas:** `ConversationToolbar.kt`, `ConversationColors.kt`.

**Completion criteria:**
- Toolbar looks correct in both Light and Dark modes.

**Testing expectations:** Theme switch check.

#### 3.8 Handle narrow-window layout

- [x] Handle narrow-window layout

**Description:** Ensure the toolbar behaves gracefully when the window is narrow.

**Source:** Delivery Part 3.

**Dependencies:** 3.4.

**Likely files / areas:** `ConversationToolbar.kt`.

**Completion criteria:**
- Toolbar elements wrap or scroll appropriately in narrow windows.

**Testing expectations:** Window resize check.

#### 3.9 HITL review of toolbar visual design — `HITL Review`

- [ ] HITL review of toolbar visual design

**Description:** Present the final toolbar design to HITL.

**Source:** Delivery Part 3.

**Dependencies:** 3.8.

**Likely files / areas:** Visual presentation.

**Completion criteria:**
- HITL approval of toolbar aesthetics and layout.

**Testing expectations:** None.

---

### Area 4 — Menu Bar

*Source: Delivery Part 4. Implements native desktop menus.*

#### 4.1 Add MenuBar to Window in desktop entry point

- [x] Add MenuBar to Window in desktop entry point

**Description:** Use the Compose Desktop `MenuBar` API in `main.kt`.

**Source:** Delivery Part 4.

**Dependencies:** 2.5.

**Likely files / areas:** `main.kt`.

**Completion criteria:**
- A native menu bar appears in the application.

**Testing expectations:** Visual check.

#### 4.2 Implement File menu (Open Session, Refresh, Quit)

- [x] Implement File menu

**Description:** Add standard File actions.

**Source:** Delivery Part 4.

**Dependencies:** 4.1.

**Likely files / areas:** `main.kt`.

**Completion criteria:**
- File menu contains all 3 items.

**Testing expectations:** Click check.

#### 4.3 Implement Edit menu (Copy, Select All, Find/Search Messages, Find Next, Find Previous)

- [x] Implement Edit menu

**Description:** Add standard Edit and Search actions.

**Source:** Delivery Part 4.

**Dependencies:** 4.1.

**Likely files / areas:** `main.kt`.

**Completion criteria:**
- Edit menu contains all 5 items.

**Testing expectations:** Click check.

#### 4.4 Implement View menu (Oldest First/Newest First, Collapse All, Show All, Toggle Auto-Refresh)

- [x] Implement View menu

**Description:** Add view state toggles.

**Source:** Delivery Part 4.

**Dependencies:** 4.1.

**Likely files / areas:** `main.kt`.

**Completion criteria:**
- View menu contains all 4 items.

**Testing expectations:** Click check.

#### 4.5 Implement Session menu (Reload from Disk)

- [x] Implement Session menu

**Description:** Add session-specific actions.

**Source:** Delivery Part 4.

**Dependencies:** 4.1.

**Likely files / areas:** `main.kt`.

**Completion criteria:**
- Session menu is present.

**Testing expectations:** Click check.

#### 4.6 Implement Help menu (How to Use, About Junie Conversation Viewer)

- [x] Implement Help menu

**Description:** Add help and about items.

**Source:** Delivery Part 4.

**Dependencies:** 4.1.

**Likely files / areas:** `main.kt`.

**Completion criteria:**
- Help menu is present.

**Testing expectations:** Click check.

#### 4.7 HITL review of menu layout — `HITL Review`

- [x] HITL review of menu layout

**Description:** Present the menu structure and keyboard shortcuts to HITL.

**Source:** Delivery Part 4.

**Dependencies:** 4.6.

**Likely files / areas:** Visual presentation.

**Completion criteria:**
- HITL approval of menu organization and shortcut mapping.

**Testing expectations:** None.

---

### Area 5 — Refresh and Auto-Refresh Control

*Source: Delivery Part 5. Refines session update mechanisms.*

#### 5.1 Add explicit manual refresh command

- [x] Add explicit manual refresh command

**Description:** Implement logic to reload the current session file from disk.

**Source:** Delivery Part 5.

**Dependencies:** 2.5.

**Likely files / areas:** `ConversationViewModel.kt`, `SessionRepository.kt`.

**Completion criteria:**
- Manual refresh reloads the session correctly.

**Testing expectations:** Verify file changes appear after refresh.

#### 5.2 Add auto-refresh enabled/disabled state to ViewModel

- [x] Add auto-refresh enabled/disabled state to ViewModel

**Description:** Manage the toggle state for live tracking.

**Source:** Delivery Part 5.

**Dependencies:** 2.5.

**Likely files / areas:** `ConversationViewModel.kt`.

**Completion criteria:**
- Auto-refresh state is reactive and correctly managed.

**Testing expectations:** State verification.

#### 5.3 Wire auto-refresh toggle to live tracking start/stop

- [x] Wire auto-refresh toggle to live tracking start/stop

**Description:** Control the `LiveSessionTracker` based on the auto-refresh state.

**Source:** Delivery Part 5.

**Dependencies:** 5.2.

**Likely files / areas:** `ConversationViewModel.kt`, `LiveSessionTracker.kt`.

**Completion criteria:**
- Toggling auto-refresh starts/stops the file watcher.

**Testing expectations:** Watcher state check.

#### 5.4 Decide and implement auto-refresh preference persistence (if HITL approves)

- [x] Implement auto-refresh preference persistence

**Description:** Save the auto-refresh preference across app launches.

**Source:** Delivery Part 5.

**Dependencies:** 5.3.

**Likely files / areas:** Preferences handling code.

**Completion criteria:**
- Preference is saved and restored.

**Testing expectations:** App restart check.

#### 5.5 Add tests for refresh and auto-refresh behaviour — `Test Required`

- [x] Add tests for refresh and auto-refresh behaviour

**Description:** Automated verification of refresh and tracking toggling.

**Source:** Delivery Part 5.

**Dependencies:** 5.3.

**Likely files / areas:** `ConversationViewModelTest.kt`.

**Completion criteria:**
- Tests verify correct behavior of refresh and auto-refresh.

**Testing expectations:** JUnit tests pass.

#### 5.6 Manual review with actively changing events.jsonl — `Manual Review Required`

- [x] Manual review with actively changing events.jsonl

**Description:** Verify live updates in a real scenario.

**Source:** Delivery Part 5.

**Dependencies:** 5.3.

**Likely files / areas:** Running application.

**Completion criteria:**
- Live updates work reliably as the file changes.

**Testing expectations:** Manual verification.

---

### Area 6 — Sort Order: Oldest First / Newest First

*Source: Delivery Part 6. Implements message sorting.*

#### 6.1 Add sort order state (enum: OldestFirst, NewestFirst) to ViewModel

- [x] Add sort order state to ViewModel

**Description:** Implement the sorting state and toggle logic.

**Source:** Delivery Part 6.

**Dependencies:** 2.5.

**Likely files / areas:** `ConversationViewModel.kt`.

**Completion criteria:**
- Sort order state is reactive.

**Testing expectations:** State check.

#### 6.2 Apply sort order to visible Messages

- [x] Apply sort order to visible Messages

**Description:** Ensure the message list renders in the selected order.

**Source:** Delivery Part 6.

**Dependencies:** 6.1.

**Likely files / areas:** `ConversationViewModel.kt`.

**Completion criteria:**
- Message list order flips correctly.

**Testing expectations:** Visual check.

#### 6.3 Preserve filter/search behaviour with sort order

- [x] Preserve filter/search behaviour with sort order

**Description:** Verify that sorting doesn't break filtering or search match navigation.

**Source:** Delivery Part 6.

**Dependencies:** 6.2.

**Likely files / areas:** `ConversationViewModel.kt`.

**Completion criteria:**
- Filtering and search remain functional across sort modes.

**Testing expectations:** Match navigation check.

#### 6.4 Define and implement live-tracking behaviour when newest-first is active

- [x] Implement live-tracking behaviour for newest-first

**Description:** Ensure new messages appear at the top in newest-first mode.

**Source:** Delivery Part 6.

**Dependencies:** 6.2.

**Likely files / areas:** `ConversationViewModel.kt`.

**Completion criteria:**
- Live messages are prepended when in newest-first mode.

**Testing expectations:** Live update check.

#### 6.5 Define and implement scroll behaviour for each sort order

- [x] Implement scroll behaviour for each sort order

**Description:** Manage scroll position changes when flipping order to avoid disorientation.

**Source:** Delivery Part 6.

**Dependencies:** 6.2.

**Likely files / areas:** `ConversationScreen.kt`.

**Completion criteria:**
- Scroll position is handled gracefully during sort changes.

**Testing expectations:** Visual check.

#### 6.6 Add tests for ordering, filtering, search, and live updates — `Test Required`

- [x] Add tests for ordering, filtering, search, and live updates

**Description:** Automated verification of sorting logic.

**Source:** Delivery Part 6.

**Dependencies:** 6.4.

**Likely files / areas:** `ConversationViewModelTest.kt`.

**Completion criteria:**
- Tests verify sorting logic and its interaction with other features.

**Testing expectations:** JUnit tests pass.

#### 6.7 HITL review of sort-order UX — `HITL Review`

- [x] HITL review of sort-order UX

**Description:** Present the sorting functionality to HITL.

**Source:** Delivery Part 6.

**Dependencies:** 6.5.

**Likely files / areas:** Visual presentation.

**Completion criteria:**
- HITL approval of sorting behavior and live update integration.

**Testing expectations:** None.

---

### Area 7 — Collapse All / Show All

*Source: Delivery Part 7. Implements global block expansion control.*

#### 7.1 Audit current collapsible block state model

- [x] Audit current collapsible block state model

**Description:** Verify how expansion state is currently handled and identify necessary changes.

**Source:** Delivery Part 7.

**Dependencies:** 1.4.

**Likely files / areas:** `ThoughtBlock.kt`, `ToolCallBlock.kt`.

**Completion criteria:**
- State model is audited and update plan is ready.

**Testing expectations:** None.

#### 7.2 Add global collapse/show-all command event to ViewModel

- [x] Add global collapse/show-all command event to ViewModel

**Description:** Implement logic to update expansion state for all rich content blocks.

**Source:** Delivery Part 7.

**Dependencies:** 7.1, 2.5.

**Likely files / areas:** `ConversationViewModel.kt`.

**Completion criteria:**
- Global commands trigger expansion state updates.

**Testing expectations:** State check.

#### 7.3 Ensure per-block expand/collapse still works after global commands

- [x] Verify per-block interaction preserved

**Description:** Ensure user can still manually toggle individual blocks after a global command.

**Source:** Delivery Part 7.

**Dependencies:** 7.2.

**Likely files / areas:** `ThoughtBlock.kt`, `ToolCallBlock.kt`.

**Completion criteria:**
- Individual toggles remain functional.

**Testing expectations:** Manual toggle check.

#### 7.4 Define behaviour when search auto-expands a matching collapsed block

- [x] Implement search auto-expansion

**Description:** Ensure matches inside collapsed blocks cause expansion even if "Collapse All" was used.

**Source:** Delivery Part 7.

**Dependencies:** 7.2.

**Likely files / areas:** `ConversationViewModel.kt`.

**Completion criteria:**
- Matches are visible even if previously collapsed.

**Testing expectations:** Search match check.

#### 7.5 Add tests for collapse-all/show-all — `Test Required`

- [x] Add tests for collapse-all/show-all

**Description:** Automated verification of expansion state logic.

**Source:** Delivery Part 7.

**Dependencies:** 7.2.

**Likely files / areas:** `ConversationViewModelTest.kt`.

**Completion criteria:**
- Tests verify global expansion commands.

**Testing expectations:** JUnit tests pass.

#### 7.6 HITL review of collapse/show-all behaviour — `HITL Review`

- [x] HITL review of collapse/show-all behaviour

**Description:** Present the global expansion controls to HITL.

**Source:** Delivery Part 7.

**Dependencies:** 7.4.

**Likely files / areas:** Visual presentation.

**Completion criteria:**
- HITL approval of expansion behavior.

**Testing expectations:** None.

---

### Area 8 — Copy and Search Integration

*Source: Delivery Part 8. Integrates primary interaction controls into the toolbar.*

#### 8.1 Determine feasible selected-text copy implementation in Compose Desktop

- [x] Determine feasible selected-text copy implementation

**Description:** Research how to trigger "Copy" for the current selection via a button/menu.

**Source:** Delivery Part 8.

**Dependencies:** 1.5.

**Likely files / areas:** `ClipboardManager` usage.

**Completion criteria:**
- Copy implementation plan is finalized.

**Testing expectations:** None.

#### 8.2 Add toolbar/menu Copy command behaviour

- [x] Add toolbar/menu Copy command behaviour

**Description:** Implement the Copy command logic.

**Source:** Delivery Part 8.

**Dependencies:** 8.1.

**Likely files / areas:** `ConversationToolbar.kt`, `main.kt`.

**Completion criteria:**
- Toolbar/Menu Copy button works for selected text.

**Testing expectations:** Paste check.

#### 8.3 Preserve existing copy buttons on code/diff/terminal blocks

- [x] Preserve existing copy buttons

**Description:** Ensure existing per-block copy functionality remains functional.

**Source:** Delivery Part 8.

**Dependencies:** 8.2.

**Likely files / areas:** `MessageItems.kt`.

**Completion criteria:**
- Per-block copy buttons still work.

**Testing expectations:** Paste check for blocks.

#### 8.4 Add Focus Search command and keyboard shortcut (Cmd+F / Ctrl+F)

- [x] Add Focus Search command and keyboard shortcut

**Description:** Implement the command to focus the search field in the toolbar.

**Source:** Delivery Part 8.

**Dependencies:** 3.4.

**Likely files / areas:** `ConversationToolbar.kt`.

**Completion criteria:**
- Keyboard shortcut focuses the search field.

**Testing expectations:** Focus check.

#### 8.5 Ensure Find Next / Find Previous work from menu and keyboard

- [x] Ensure Find Next / Find Previous work from menu and keyboard

**Description:** Wire up match navigation to menu items and shortcuts (Cmd+G / Shift+Cmd+G).

**Source:** Delivery Part 8.

**Dependencies:** 3.4, 4.3.

**Likely files / areas:** `main.kt`, `ConversationViewModel.kt`.

**Completion criteria:**
- Match navigation is fully integrated.

**Testing expectations:** Navigation check.

#### 8.6 Add tests/manual verification — `Test Required`, `Manual Review Required`

- [x] Add tests/manual verification

**Description:** Verify copy and search integration.

**Source:** Delivery Part 8.

**Dependencies:** 8.5.

**Likely files / areas:** `ConversationViewModelTest.kt`, application.

**Completion criteria:**
- Search and copy integration is verified.

**Testing expectations:** Tests and manual verification.

---

### Area 9 — Documentation Updates

*Source: Delivery Part 9. Keeps documentation in sync.*

#### 9.1 Update docs/HOW_TO_USE.md with toolbar and menu documentation

- [x] Update docs/HOW_TO_USE.md

**Description:** Document the new toolbar, menu items, and keyboard shortcuts for users.

**Source:** Delivery Part 9.

**Dependencies:** 8.6.

**Likely files / areas:** `docs/HOW_TO_USE.md`.

**Completion criteria:**
- New features are documented for users.

**Testing expectations:** None.

#### 9.2 Update README.md

- [x] Update README.md

**Description:** Reflect the new navigation system in the project README.

**Source:** Delivery Part 9.

**Dependencies:** 9.1.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- README is up to date.

**Testing expectations:** None.

#### 9.3 Update docs/TESTING.md if test patterns change

- [x] Update docs/TESTING.md

**Description:** Document any new testing patterns (e.g., for Menus or Toolbars).

**Source:** Delivery Part 9.

**Dependencies:** 8.6.

**Likely files / areas:** `docs/TESTING.md`.

**Completion criteria:**
- Testing docs are current.

**Testing expectations:** None.

#### 9.4 Update docs/RECAP.md

- [x] Update docs/RECAP.md

**Description:** Add Sprint 5 milestones to the project recap.

**Source:** Delivery Part 9.

**Dependencies:** 8.6.

**Likely files / areas:** `docs/RECAP.md`.

**Completion criteria:**
- RECAP is up to date.

**Testing expectations:** None.

#### 9.5 Update docs/project_memory.md

- [x] Update docs/project_memory.md

**Description:** Record key decisions and outcomes from Sprint 5.

**Source:** Delivery Part 9.

**Dependencies:** 8.6.

**Likely files / areas:** `docs/project_memory.md`.

**Completion criteria:**
- Project memory is updated.

**Testing expectations:** None.

---

### Area 10 — Testing, Review, and Completion

*Source: Delivery Part 10. Final verification.*

#### 10.1 Run ./gradlew :shared:jvmTest

- [x] Run shared module tests

**Description:** Verify that all common logic tests pass.

**Source:** Delivery Part 10.

**Dependencies:** 8.6.

**Likely files / areas:** `shared` module.

**Completion criteria:**
- Shared tests pass.

**Testing expectations:** Green build.

#### 10.2 Run ./gradlew test

- [x] Run all tests

**Description:** Execute the full automated test suite.

**Source:** Delivery Part 10.

**Dependencies:** 10.1.

**Likely files / areas:** Entire project.

**Completion criteria:**
- All tests pass.

**Testing expectations:** Green build.

#### 10.3 Add/extend Robot-pattern UI tests — `Test Required`

- [x] Add/extend Robot-pattern UI tests

**Description:** Add UI tests for toolbar buttons and basic menu interaction.

**Source:** Delivery Part 10.

**Dependencies:** 10.2.

**Likely files / areas:** `desktopApp` tests.

**Completion criteria:**
- UI tests cover critical toolbar/menu paths.

**Testing expectations:** Green build.

#### 10.4 Run manual review checklist — `Manual Review Required`

- [x] Run manual review checklist

**Description:** Execute the manual verification checklist from the sprint doc.

**Source:** Delivery Part 10.

**Dependencies:** 10.3.

**Likely files / areas:** Application.

**Completion criteria:**
- All manual checks pass.

**Testing expectations:** Manual sign-off.

#### 10.5 Run cyclomatic complexity check

- [x] Run cyclomatic complexity check

**Description:** Audit code quality and complexity.

**Source:** Sprint Guidelines.

**Dependencies:** 10.4.

**Likely files / areas:** Entire codebase.

**Completion criteria:**
- Complexity check is run and results reviewed.

**Testing expectations:** None.

#### 10.6 Fix review issues

- [x] Fix review issues

**Description:** Address any findings from tests, manual review, or complexity check.

**Source:** Delivery Part 10.

**Dependencies:** 10.5.

**Likely files / areas:** Affected files.

**Completion criteria:**
- Issues are resolved.

**Testing expectations:** Re-run tests.

#### 10.7 HITL final approval — `HITL Review`

- [x] HITL final approval

**Description:** Obtain final sign-off for Sprint 5.

**Source:** Delivery Part 10.

**Dependencies:** 10.6.

**Likely files / areas:** Project delivery.

**Completion criteria:**
- HITL approval granted.

**Testing expectations:** None.

---

## 8. HITL Review Checkpoints

| # | Task | Area | HITL-Visible Outcome |
|---|------|------|---------------------|
| 1 | 1.9 | Discovery | Documented audit of current chrome, LogViewer findings, and open questions with design recommendations. |
| 2 | 3.9 | Toolbar UI | Toolbar visually matches LogViewer-inspired styling in both themes with all buttons functional. |
| 3 | 4.7 | Menu Bar | Application menu follows platform conventions with all commands wired and keyboard shortcuts working. |
| 4 | 6.7 | Sort Order | Sort order toggles correctly, interacts properly with live tracking, search, and filters. |
| 5 | 7.6 | Collapse All/Show All | Global collapse/show-all works correctly, per-block interaction preserved, search auto-expansion works. |
| 6 | 10.7 | Completion | All tests pass, manual review complete, HITL final approval. |

## 9. Acceptance Criteria

- All 67 tasks marked complete.
- All **Test Required** tasks have passing automated tests.
- All **HITL Review** tasks have HITL approval.
- All **Manual Review Required** tasks verified.
- `./gradlew test` passes.
- `./gradlew :shared:jvmTest` passes.
- Toolbar renders correctly in both themes.
- Menu bar works on macOS with correct shortcuts.
- All 12 open questions resolved with HITL input.
- `README.md`, `project_memory.md`, `RECAP.md` updated.
- Cyclomatic complexity check run and reviewed.

## 10. Deferred / Out-of-Scope Items

- Open Recent Sessions submenu.
- Reveal Session in Finder/File Manager.
- Settings dialog.
- Filter submenu in View menu.
- Theme submenu in View menu.
- Export functionality.
- Mobile UI.
- Database ingestion.

## 11. Notes / Decisions Log

| Date | Decision | Context |
|------|----------|---------|
| 2026-07-18 | Area 1 discovery complete | All 8 discovery tasks (1.1–1.8) completed. Findings documented in `docs/sprint-5-area-1-discovery-findings.md`. 12 open questions (Q1–Q12) formalized with recommendations. Task 1.9 (HITL review) awaiting approval. |
| 2026-07-18 | Area 1 HITL review complete | All 12 open questions (Q1–Q12) decided by HITL. Key decisions: Session picker (Q1), persist auto-refresh and sort order (Q2, Q3), IntelliJ-style collapse/expand shortcuts (Q4), selected-text-only copy (Q5), all collapsible blocks for Collapse All (Q6), search force-expands after Collapse All (Q7), defer Open Recent (Q8), icons-only toolbar (Q9), preserve scroll on refresh (Q10), newest-first live messages at top (Q11), filter chips stay below toolbar (Q12). |
| 2026-07-18 | Area 2 + Area 3 implementation complete | Created `ConversationCommand` sealed interface (13 commands), `SortOrder` enum, `ConversationCommandState` with `fromConversationState()` derivation. Added `onCommand()` to `ConversationViewModel`. Created `ConversationToolbar` composable with LogViewer-inspired styling (28dp buttons, 18dp icons, Surface with 2dp elevation, Divider separators, horizontal scroll for narrow windows). Moved search field into toolbar with match count and navigation. Filter chips remain below toolbar per Q12. Added `compose-material3-iconsExtended` dependency. 18 new command-state and dispatch tests in `ConversationCommandTest.kt`. Updated existing tests for new toolbar layout. Task 3.9 (HITL review) awaiting approval. |
| 2026-07-18 | Area 4 menu bar implementation complete | Added native Compose Desktop `MenuBar` in `main.kt` with 5 menus: File (Open Session, Refresh, Quit), Edit (Copy, Find, Find Next, Find Previous), View (Sort Order toggle, Collapse All, Show All, Auto-Refresh toggle), Session (Reload from Disk), Help (How to Use, About). All menu items dispatch `ConversationCommand` through the shared command model. Keyboard shortcuts added for all applicable items using `KeyShortcut` API. Added `HowToUse` command to sealed interface. Created `AboutDialog` and `HowToUseDialog` composables with test tags. Added event collection in `ConversationRoot` for `ShowAbout`, `ShowHowToUse`, and `FocusSearch` events. Hoisted ViewModel creation to `main.kt` for menu bar access; added lifecycle dependencies to desktopApp module. Task 4.7 (HITL review) awaiting approval. |
| 2026-07-19 | Area 5 refresh and auto-refresh implementation complete | Implemented manual Refresh via `refreshSession()` in ViewModel — reloads Session from disk preserving Search Query, Filters, and auto-refresh state. Wired `ToggleAutoRefresh` command to start/stop `LiveSessionTracker`: disabling stops live tracking but keeps Messages visible; enabling restarts tracking from cached file offset. Added `isAutoRefreshEnabled` field to `AppPreferences` with default `true` for backward compatibility. Preference is loaded on init and saved on toggle via `updatePreference()`. `loadMessages()` now conditionally starts live tracking only when auto-refresh is enabled. Created `RefreshAndAutoRefreshTest.kt` with 13 tests covering: manual refresh reload, no-op without Session, preserving Search/Filters, file content updates, toggle state, Messages visibility, preference persistence (save/load/default), Session selection with auto-refresh off, command state reflection, and refresh preserving disabled auto-refresh. All tests pass. Task 5.6 (manual review) left unchecked — requires HITL manual verification with actively changing `events.jsonl`. |
| 2026-07-19 | Area 6 sort order implementation complete | Implemented full sort order feature: `SortOrder` enum (`OldestFirst`/`NewestFirst`) with `ToggleSortOrder` command toggling state, re-deriving visible Messages, and persisting preference. Added `sortOrder` field to `AppPreferences` with backward-compatible default `"OldestFirst"`. `filterMessages()` now applies sort order after filtering — `NewestFirst` reverses the filtered list via `asReversed()`. `currentMatchIndex` is safely clamped after sort changes. Live-tracked Messages are appended to canonical `messages` list in chronological order; `filterMessages()` re-derives sorted `filteredMessages` so new Messages appear at top in `NewestFirst` and bottom in `OldestFirst`. Created `SortOrderTest.kt` with 15 tests covering: default state, toggle, visible ordering, persistence (save/load/invalid fallback), filter interaction, search interaction, Find Next/Previous in sorted order, match index validity, manual refresh respecting sort, and command state reflection. All tests pass (`./gradlew :shared:jvmTest` BUILD SUCCESSFUL). Task 6.7 (HITL review) left unchecked — requires HITL approval of sort-order UX. |
| 2026-07-19 | Area 7 collapse all / show all implementation complete | Hoisted block expansion state from local `remember` to ViewModel via `blockExpansionStates: Map<String, Boolean>` in `ConversationState`. Added `collectCollapsibleBlockIds()` to derive stable block IDs (pattern: `{messageId}:{blockType}`) for all collapsible blocks (Thought, Tool/MCP, Code, Diff, Terminal, Structured, Markdown, SubAgent). `CollapseAll` sets all IDs to `false`; `ShowAll` sets all to `true`. Added `OnToggleBlockExpansion(blockId)` action for per-block manual toggle. Updated `CollapsibleBlock` with `externalExpanded`/`onToggle` params — when provided, ViewModel state overrides local `remember`; when absent, blocks fall back to local state. Search `forceExpanded` takes priority over collapsed state via `visibleExpanded = manualExpanded \|\| (forceExpanded && !userDismissedForce)`. Updated all 6 block components + `MessageBody` + `ContentRenderer` + `MessageCard` + `ConversationScreen` to thread expansion state. Created `CollapseShowAllTest.kt` with 10 tests covering: collapse all, show all, per-block toggle after global commands, search force-expansion priority, clearing search restores explicit state, stability across sort/filter changes, default state, and toggle without prior global command. All tests pass (`./gradlew :shared:jvmTest` BUILD SUCCESSFUL). Task 7.6 (HITL review) left unchecked — requires HITL approval. |
| 2026-07-20 | Areas 8–10 complete (Sprint 5 final) | **Area 8 — Copy and Search Integration:** Copy command is selected-text-only no-op (Compose Desktop limitation — no API to detect selected text); OS-level Cmd+C handles actual copying. Per-block copy buttons unchanged. FocusSearch, FindNext, FindPrevious already wired via toolbar, menu, and keyboard shortcuts. Added 6 new tests to `ConversationCommandTest.kt`: Copy no-crash, FocusSearch event emission, FindNext/FindPrevious wrap-around, match navigation respecting search query. **Area 9 — Documentation:** Updated `HOW_TO_USE.md` (toolbar, menu, shortcuts, sort order, collapse/show all, copy behaviour), `README.md` (feature summary), `TESTING.md` (Sprint 5 testing additions), `RECAP.md` (completion milestone), `project_memory.md` (decisions and gotchas). **Area 10 — Testing and Review:** `./gradlew :shared:jvmTest` BUILD SUCCESSFUL, `./gradlew test` BUILD SUCCESSFUL. Lightweight cyclomatic complexity review — no excessive complexity found (largest files: MessageItems.kt 653 lines, ConversationViewModel.kt 528 lines). No configured complexity tool in project; manual review performed. Task 10.7 (HITL final approval) left unchecked — requires HITL sign-off. |
| 2026-07-20 | Copy menu enablement now reflects real text selection | Replaced the "messages loaded" proxy for Copy enablement with genuine selection tracking. Compose Foundation's selection-aware `SelectionContainer(selection, onSelectionChange)` overload is Kotlin-`internal` but JVM-public, so a small Java bridge (`SelectionContainerBridge.java` in `shared/src/jvmMain/java`) calls it legally. New `TrackedSelectionContainer` composable (with `expect`/`actual` `PlatformSelectionContainer`) replaces all 11 `SelectionContainer` usages and reports per-container selection presence via `LocalTextSelectionReporter` → `ConversationAction.OnTextSelectionChanged` → `ConversationState.hasTextSelection`. `copyEnabled` (toolbar and Edit → Copy menu) is now true only while text is actually selected. Containers report "no selection" on dispose so LazyColumn recycling never leaves stale enablement. Tests updated/added in `ConversationCommandTest.kt`; `./gradlew :shared:jvmTest` and `./gradlew test` BUILD SUCCESSFUL. |