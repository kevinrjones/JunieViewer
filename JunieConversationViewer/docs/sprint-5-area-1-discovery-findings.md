# Sprint 5 Area 1 — Discovery and Scope Confirmation Findings

> **Date:** 2026-07-18
> **Author:** Junie (automated discovery)
> **Status:** HITL review complete (2026-07-18) — all 12 open questions decided

---

## 1. Documentation Baseline

### Canonical Terms for Sprint 5

The following terms from `UBIQUITOUS-LANGUAGE.md` must be used consistently:

| Term | Definition (summary) |
|---|---|
| **Conversation** | The complete ordered exchange for one Session. |
| **Session** | A single recorded Junie run; folder under `~/.junie/sessions/` containing `events.jsonl`. |
| **Event** | A single raw JSON line in `events.jsonl`. |
| **Message** | A display unit derived from one Event, with a Sender, Message Kind, and content. |
| **Human** | The person interacting with Junie (Sender). Avoid "user" as Sender label. |
| **Junie** | The AI assistant (Sender). Avoid "assistant", "AI", "bot", "agent". |
| **Search Query** | Free text for case-insensitive substring matching. |
| **Filter** | User-controlled predicate showing/hiding Messages by Sender/Kind. |
| **Message Kind** | Classification determining rendering and filterability. |
| **HITL** | Human In The Loop — reviewer of sprint deliverables. |

### Testing Conventions

- **Stack:** JUnit 4, MockK, Turbine (Flow testing), Compose Test Rule, Okio temp dirs.
- **Patterns:** Fakes over mocks for repositories; Robot Pattern for UI tests.
- **Tags:** `testTag("...")` on all interactive/state UI elements.
- **Commands:** `./gradlew :shared:jvmTest` (shared module), `./gradlew test` (full suite).
- **Current count:** 142+ tests, 0 failures as of Sprint 4 completion.

### Prior Decisions Affecting Sprint 5

- **Sprint 4 shipped:** Text selection via `SelectionContainer`, search highlighting with match navigation, live tracking via polling-based `LiveSessionTracker`, filter coverage for all 18 `MessageKind` values, `AgentTaskFailedEvent` support.
- **D4 (deferred from Sprint 3):** Syntax highlighting theme wiring — `SyntaxThemes.default(darkMode = false)` in `CodeBlock.kt` remains hardcoded.
- **Sprint 3 HITL decisions:** ThoughtBlock/ToolCallBlock collapsed by default; theme toggle in Settings only; Junie messages 90% width right-aligned, Human 33% left-aligned.
- **Sprint 4 HITL decisions:** Sub-agent visual → Badge/Label; SubAgent stays under Tools filter; search highlighting for text + code/terminal (defer Markdown); live tracking always-on.

### Gotchas from project_memory.md / RECAP.md

- `StateFlow` updates can emit intermediate states — Turbine tests must consume multiple items.
- LazyColumn virtualisation means off-screen items are not rendered in UI tests — assertions must target visible items only.
- `compose-material-icons-extended` is in the classpath — Material Icons can be used for toolbar buttons.
- Windows/Linux cross-platform verification still pending from Sprint 3.
- No configured cyclomatic complexity tool in project.
- Collapsible block state is local `remember` — will need hoisting for global Collapse All/Show All.

---

## 2. Current Top Chrome, Search, Filter, and Session Picker Audit

### Component Layout

The current top chrome is implemented in `ConversationScreen.kt` as the `SearchAndFilterChrome` private composable (lines 114–206).

| Component | File | Location | testTag |
|---|---|---|---|
| Search Messages field | `ConversationScreen.kt` | `SearchAndFilterChrome` (line 134) | `search_field` |
| Clear search button | `ConversationScreen.kt` | Trailing icon in search field | `search_clear_button` |
| Result count | `ConversationScreen.kt` | `MatchNavigationBar` | `result_count` |
| Match position | `ConversationScreen.kt` | `MatchNavigationBar` | `match_position` |
| Previous match button | `ConversationScreen.kt` | `MatchNavigationBar` (lines 209–258) | `prev_match_button` |
| Next match button | `ConversationScreen.kt` | `MatchNavigationBar` | `next_match_button` |
| Session picker button | `ConversationScreen.kt` | `SearchAndFilterChrome` (line 167) | `session_picker_button` |
| Settings button | `ConversationScreen.kt` | `SearchAndFilterChrome` (line 179) | `settings_button` |
| Filter chips (6) | `FilterBar.kt` | Called from `SearchAndFilterChrome` (line 192) | `filter_human`, `filter_junie`, `filter_thought`, `filter_tool`, `filter_patch`, `filter_terminal` |

### Session Picker

- Opened via `TextButton` with tag `session_picker_button` triggering `ConversationAction.OnToggleSessionPicker`.
- Renders `SessionSelector` dialog (lines 51–57 in `ConversationScreen.kt`).

### Settings

- Opened via `TextButton` with tag `settings_button` triggering `ConversationAction.OnToggleSettings`.
- Renders `SettingsDialog` (lines 60–66 in `ConversationScreen.kt`).

### Components Likely to Move or Interact with Toolbar

| Component | Recommendation |
|---|---|
| Search Messages field | **Move into toolbar.** Include prev/next match controls adjacent. |
| Session picker button | **Add Open Session button to toolbar** that triggers the same `OnToggleSessionPicker` action. |
| Settings button | **Keep in current location** or add to File/Edit menu. Not a toolbar priority. |
| Filter chips | **Keep below toolbar** in current position (see Q12). |
| Match navigation bar | **Move into toolbar** next to search field. |

### Risks in Moving Search to Toolbar

1. **State coupling:** Search field state (`searchQuery`, `currentMatchIndex`, `filteredMessages`) is managed by `ConversationViewModel`. Moving the field to a toolbar composable requires passing the same state and actions — straightforward but must not duplicate state.
2. **Match navigation bar:** Currently appears conditionally below the search field. Must be co-located with search in the toolbar.
3. **Layout pressure:** Adding search field + prev/next buttons + other toolbar buttons may crowd narrow windows.

---

## 3. Live Tracking, Refresh, and Session Loading Audit

### Current Session Loading Flow

```
HITL selects Session (OnSessionSelected action)
  → ViewModel.loadMessages()
    → _state.update(isLoading = true)
    → withContext(ioDispatcher):
        → repository.setSession(sessionId, homePath)
        → repository.getMessages()
            → Reads entire events.jsonl line-by-line
            → JsonlParser.parseLine() → Either<Error, JunieEvent>
            → EventToMessageMapper.mapEventsToMessages(events) → List<Message>
        → repository.getSessionInfo(sessionId, homePath)
    → _state.update(messages, filteredMessages, isLoading = false)
    → filterMessages(searchQuery)
    → startLiveTracking()
```

### Live Tracking

- **Start:** `ConversationViewModel.startLiveTracking()` is called after successful `loadMessages()` (line 209).
- **Mechanism:** `LiveSessionTracker` uses polling-based file watching. Maintains byte offset to read only new content.
- **New messages:** Appended to `state.messages`; `filterMessages()` is re-applied automatically (line 238).
- **File reset:** `LiveTrackingEvent.FileReset` triggers a full session reload (lines 240–248).
- **File deleted:** `LiveTrackingEvent.FileDeleted` stops tracking.
- **Stop:** Tracking is cancelled when session changes or is deselected.

### Manual Reload

- **No explicit Refresh button exists.** The only "retry" is in the error state (line 344).
- A manual Refresh command would call `loadMessages()` again, which re-reads the entire file and restarts live tracking.

### Scroll Position

- **Auto-scroll:** `LaunchedEffect(messageCount)` scrolls to bottom when new messages arrive, provided the HITL is already near bottom (lines 394–405 in `ConversationScreen.kt`).
- **Match navigation:** `LaunchedEffect(state.currentMatchIndex)` scrolls to the current search match (lines 384–390).

### What Needs to Change for Sprint 5

| Feature | Required Changes |
|---|---|
| **Manual Refresh** | Add `ConversationAction.Refresh` that calls `loadMessages()`. Must preserve or intentionally reset filters/search. |
| **Auto-refresh on/off** | Add `isAutoRefreshEnabled: Boolean` to `ConversationState`. When disabled, cancel `LiveSessionTracker` collection. When re-enabled, restart tracking. |
| **Refresh while auto-refresh off** | `Refresh` action re-reads the full file (one-shot) without restarting live tracking. |
| **Refresh while auto-refresh on** | `Refresh` action re-reads the full file and restarts live tracking from the new end-of-file offset. |
| **Sort order + live tracking** | When newest-first, new messages must be prepended to the display list. Auto-scroll logic must adapt: scroll to top in newest-first, scroll to bottom in oldest-first. |

---

## 4. Collapsible Block State Audit

### Collapsible Blocks

| Block | File | Uses `CollapsibleBlock` | Default State |
|---|---|---|---|
| ThoughtBlock | `ThoughtBlock.kt` | Yes | Collapsed |
| ToolCallBlock | `ToolCallBlock.kt` | Yes | Collapsed |
| CodeBlockWithCopy | `CodeBlockWithCopy.kt` | Yes (wraps `CodeBlock`) | Expanded |
| DiffBlock | `DiffBlock.kt` | Yes | Collapsed |
| TerminalOutputBlock | `TerminalOutputBlock.kt` | Yes | Collapsed |
| StructuredOutputBlock | `StructuredOutputBlock.kt` | Yes | Collapsed |
| ErrorWarningBlock | `ErrorWarningBlock.kt` | No | Always expanded |

### State Storage

- **Local composable state:** `CollapsibleBlock.kt` uses `remember { mutableStateOf(initiallyExpanded) }` (lines 53–55).
- **Not in ViewModel:** Expansion state is not part of `ConversationState`.
- **`forceExpanded` parameter:** `CollapsibleBlock` accepts a `forceExpanded: Boolean` parameter (line 48). When `true`, the block auto-expands for search matches. The HITL can manually collapse it, which sets a local `userDismissed` flag (lines 72–75).

### Global Collapse All / Show All — Implementation Options

| Approach | Pros | Cons |
|---|---|---|
| **A: Hoist state to ViewModel** | Full control; survives LazyColumn recycling; testable | Requires tracking expansion state per message/block; more complex state model |
| **B: Shared expansion event flow** | Lightweight; ViewModel emits a one-shot event; each `CollapsibleBlock` reacts | Relies on `remember` surviving recycling; may lose state for off-screen items |
| **C: Key-based reset** | Change a "generation" key on Collapse All/Show All; forces `remember` to reinitialise | Simple; but loses per-block manual overrides |

**Recommendation:** Approach A (hoist to ViewModel) for blocks that need global control (ThoughtBlock, ToolCallBlock). Other blocks (Code, Diff, Terminal, StructuredOutput) can remain local unless HITL requests otherwise.

### Per-Block Interaction After Global Command

- After "Collapse All", the HITL should still be able to expand individual blocks manually.
- After "Show All", the HITL should still be able to collapse individual blocks manually.
- This requires per-block state that can be overridden after a global command — supports Approach A.

### Search Force-Expand After Collapse All

- Current `forceExpanded` logic auto-expands blocks containing search matches.
- After "Collapse All", if a search match exists in a collapsed block, `forceExpanded` should still trigger expansion for that block.
- **Recommendation:** Search `forceExpanded` takes priority over global collapse state (see Q7).

### LazyColumn Recycling Risks

- With local `remember` state, off-screen items lose their expansion state when recycled.
- Hoisting to ViewModel (Approach A) eliminates this risk.
- Stable keys are already used for LazyColumn items (message ID-based).

---

## 5. Copy and Text Selection Audit

### Current SelectionContainer Usage

- `SelectionContainer` wraps `CodeBlock` inside `CodeBlockWithCopy.kt` (line 37).
- Other rich blocks (Diff, Terminal, StructuredOutput) also use `SelectionContainer` around their content.
- Sprint 4 added `SelectionContainer` wrapping for message body content.

### Current Copy Buttons

| Block | Copy Button | What It Copies |
|---|---|---|
| CodeBlockWithCopy | `CopyButton` | Full code block content |
| DiffBlock | `CopyButton` | Full diff content |
| TerminalOutputBlock | `CopyButton` | Full terminal output |
| StructuredOutputBlock | `CopyButton` | Full structured data |
| ToolCallBlock | `CopyButton` in header | Full tool call content |

### Clipboard Access

- `CopyButton.kt` (lines 15–29) uses `LocalClipboardManager` to set plain text on the clipboard (line 21).
- Standard Compose Desktop clipboard API.

### Global Copy Command Feasibility

- **Challenge:** Compose Desktop does not expose the "currently selected text" from a `SelectionContainer` to external code (toolbar/menu). There is no public API to query what text the HITL has selected.
- **Workaround options:**
  1. Use AWT's `Toolkit.getDefaultToolkit().systemSelection` — may work on some platforms but is unreliable.
  2. Dispatch a synthetic `Ctrl+C`/`Cmd+C` key event to the focused component — fragile.
  3. Accept that the menu/toolbar Copy command works only when a `CopyButton` context is available, and rely on the OS-level `Cmd+C` for selected text.
- **Recommendation:** The toolbar/menu Copy command should attempt to copy selected text via the system clipboard shortcut passthrough. If no text is selected, the command does nothing (standard desktop behaviour). Existing per-block `CopyButton` components remain unchanged.

### Compose Desktop Clipboard Limitations

- `LocalClipboardManager` is scoped to the Compose composition — it cannot be accessed from the `MenuBar` DSL which runs outside the composition.
- The `MenuBar` Copy item should use AWT clipboard (`java.awt.Toolkit.getDefaultToolkit().systemClipboard`) or dispatch the copy action through the ViewModel.
- **Key limitation:** No way to programmatically query "what text is currently selected" in a `SelectionContainer`.

---

## 6. Desktop App Entry Point and Menu Baseline

### Entry Point

- **File:** `desktopApp/src/main/kotlin/com/knowledgespike/junieviewer/main.kt`
- **Structure:** `application { Window(...) { ConversationScreen(...) } }`
- **No `MenuBar`** is defined in the `Window` block.

### App Lifecycle

- `onCloseRequest` triggers `saveAndExit()` which persists window dimensions and position to `PreferencesRepository` before calling `exitApplication()` (lines 70–85).
- Window state (size, position) is saved and restored on launch.

### Current Keyboard Shortcuts

- **Only one shortcut exists:** `Cmd+F` / `Ctrl+F` for focusing the search field, handled via `onPreviewKeyEvent` in `ConversationScreen.kt` (lines 75–85).
- No other keyboard shortcuts are implemented.

### Where Menu Should Be Added

- Inside the `Window` block in `main.kt`, before `ConversationScreen(...)`.
- Compose Desktop `MenuBar` DSL: `MenuBar { Menu("File") { Item("Open Session...", onClick = { ... }) } }`.
- Menu items should dispatch `ConversationCommand` instances to the ViewModel.

### macOS Considerations

- macOS renders the `MenuBar` in the system menu bar (top of screen), not in the window frame.
- `Cmd+Q` is handled automatically by macOS for Quit.
- `Cmd+,` is the convention for Preferences/Settings on macOS.
- The app name in the macOS menu bar comes from the application bundle name.

---

## 7. LogViewer Reference App Findings

### Source Files Inspected

| File | Purpose |
|---|---|
| `app/src/main/kotlin/Main.kt` | Application entry point with `MenuBar` |
| `ui/src/main/kotlin/.../FilterBar.kt` | Toolbar with icon buttons and search |
| `ui/src/main/kotlin/.../FilterBarInteractiveControls.kt` | Toolbar interactive controls |
| `app/src/main/kotlin/.../AppMenuActionKey.kt` | Menu action enum |
| `ui/src/main/kotlin/.../MenuShortcutSet.kt` | Platform-specific shortcuts |

### Toolbar Structure

- **Container:** `Surface(elevation = 2.dp)` with `MaterialTheme.colors.surface` background.
- **Layout:** Horizontal `Row` with icon buttons grouped by function, separated by vertical `Divider` components.
- **Icon buttons:** `filterBarIcon()` composable — 28dp `IconButton` containing 18dp `Icon` with 2dp vertical padding.
- **Spacing:** 8dp horizontal padding on the toolbar container.
- **Search field:** Placed within the toolbar row, uses `Modifier.weight(1f)` to fill available space.

### Menu Structure

| Menu | Items |
|---|---|
| **File** | Open File, Open Directory, Connect to SFTP/S3, Add Local/Remote items, Recently Opened (submenu), New Tab, Close Tab, Exit |
| **Edit** | Copy (with shortcut, enabled when selection exists), Font, Clear |
| **View** | Toggle Dark Mode, Toggle Sidebar |

### Keyboard Shortcuts

- Managed via `menuShortcutSetForOs()` which returns platform-appropriate `KeyShortcut` instances.
- Copy: `Cmd+C` (macOS) / `Ctrl+C` (Windows/Linux).
- New Tab: `Cmd+T` / `Ctrl+T`.
- Close Tab: `Cmd+W` / `Ctrl+W`.

### Command/Action Model

- `AppMenuActionKey` enum maps menu items to specific intents.
- `appMenuIntentFor(key: AppMenuActionKey)` converts an enum value to a `KLogViewerIntent`.
- `viewModel.handleIntent(intent)` processes the intent.
- This pattern allows menu items and toolbar buttons to trigger the same logic.

### Design Guidance for Junie Conversation Viewer

**Adopt:**
- `Surface` with 2dp elevation for toolbar background.
- 28dp `IconButton` / 18dp `Icon` sizing pattern.
- `Divider` separators between logical button groups.
- `menuShortcutSetForOs()` pattern for platform-specific shortcuts.
- Shared command model (sealed interface instead of enum, for extensibility).
- `MenuBar` DSL inside `Window` block.

**Do not copy:**
- Tab management (New Tab, Close Tab) — not applicable to Junie Conversation Viewer.
- SFTP/S3 connection features — not applicable.
- Font dialog — not applicable.
- `selectedIndices`-based copy enablement — Junie Conversation Viewer uses `SelectionContainer`, not row selection.

---

## 8. Open Questions

| ID | Question | Context | Recommendation | Needs HITL? | Status | HITL Decision (2026-07-18) |
|---|---|---|---|---|---|---|
| Q1 | Should the Open button open the existing Session picker, a native file chooser, or both? | Current Session picker is a dialog listing available Sessions. A native file chooser would allow browsing to arbitrary `events.jsonl` files. | Use existing Session picker for consistency. A native file chooser could be added later. | Yes | **Decided** | **Session picker.** Use existing Session picker dialog. |
| Q2 | Should Auto-refresh enabled/disabled be persisted across app launches? | Currently live tracking is always-on (Sprint 4 decision). Adding a toggle raises the question of persistence. | Yes, persist in `PreferencesRepository` alongside `ThemeMode`. Default to enabled. | Yes | **Decided** | **Yes, persist.** Save in `PreferencesRepository` alongside `ThemeMode`. Default to enabled. |
| Q3 | Should sort order be persisted across app launches? | New feature — no prior decision. | Yes, persist in `PreferencesRepository`. Default to oldest-first. | Yes | **Decided** | **Yes, persist.** Save in `PreferencesRepository`. Default to oldest-first. |
| Q4 | What keyboard shortcuts should be used on macOS and Windows/Linux? | Only `Cmd+F`/`Ctrl+F` exists today. Need a full shortcut set. | Follow platform conventions. Proposed table below. | Yes | **Decided** | **Approved as proposed** (with Collapse All/Show All updated to IntelliJ-style `Cmd+Shift+−`/`Cmd+Shift++`). See updated table below. |
| Q5 | Should Copy copy only selected text, the current Message, or fall back to something else when no text is selected? | Compose Desktop does not expose selected text to external code. Per-block `CopyButton` exists. | Copy selected text if available (via OS shortcut passthrough); if no selection, do nothing. Keep per-block copy buttons. | Yes | **Decided** | **Selected text only.** Copy selected text via OS shortcut passthrough; if no selection, do nothing. Keep per-block copy buttons. |
| Q6 | Should Collapse All affect only rich content blocks or also any future grouped Messages? | Current collapsible blocks: ThoughtBlock, ToolCallBlock, CodeBlockWithCopy, DiffBlock, TerminalOutputBlock, StructuredOutputBlock. | Affect all current collapsible blocks (Thought, ToolCall, Code, Diff, Terminal, StructuredOutput). | Yes | **Decided** | **All collapsible blocks.** Affect all current collapsible blocks (Thought, ToolCall, Code, Diff, Terminal, StructuredOutput). |
| Q7 | When Search finds a match inside a collapsed block, should Search force-expand that block even after Collapse All? | Current `forceExpanded` logic auto-expands blocks with search matches. | Yes, search `forceExpanded` should take priority over global collapse state. | Yes | **Decided** | **Yes, force-expand.** Search `forceExpanded` takes priority over global collapse state. |
| Q8 | Should the menu include Open Recent Sessions in Sprint 5 or defer it? | LogViewer has a "Recently Opened" submenu. Would require tracking recent Sessions. | Defer to Sprint 6 to maintain focus. | Yes | **Decided** | **Defer to Sprint 6.** |
| Q9 | Should toolbar buttons use icons only, icons plus labels, or adaptive labels? | LogViewer uses icons only with tooltips. | Icons only with tooltips, matching LogViewer style for a compact toolbar. | Yes | **Decided** | **Icons only** with tooltips on hover. |
| Q10 | Should Refresh preserve current scroll position, jump to current match, or jump according to sort order? | Current reload resets to top. Live tracking preserves position when near bottom. | Preserve current scroll position to avoid disorienting the HITL. | Yes | **Decided** | **Preserve position.** Keep current scroll position after refresh. |
| Q11 | Should newest-first mode place newly appended live Messages at the top immediately? | New feature — no prior decision. Affects auto-scroll logic. | Yes, new Messages appear at top in newest-first; at bottom in oldest-first. Auto-scroll adapts accordingly. | Yes | **Decided** | **Yes, top immediately.** New Messages appear at top in newest-first; at bottom in oldest-first. |
| Q12 | Should existing Filter chips remain below the toolbar, move into the toolbar, or become a View menu/filter submenu? | Current filter chips are in `FilterBar.kt`, rendered below search chrome. | Keep filter chips below toolbar in current position to avoid toolbar overcrowding. | Yes | **Decided** | **Keep below toolbar.** Filter chips remain in current position. |

### Proposed Keyboard Shortcuts (Q4)

| Action | macOS | Windows/Linux |
|---|---|---|
| Open Session | `Cmd+O` | `Ctrl+O` |
| Refresh | `Cmd+R` | `Ctrl+R` |
| Toggle Auto-Refresh | `Cmd+Shift+R` | `Ctrl+Shift+R` |
| Copy | `Cmd+C` | `Ctrl+C` |
| Find / Focus Search | `Cmd+F` | `Ctrl+F` |
| Find Next | `Cmd+G` | `Ctrl+G` or `F3` |
| Find Previous | `Cmd+Shift+G` | `Ctrl+Shift+G` or `Shift+F3` |
| Toggle Sort Order | `Cmd+Shift+O` | `Ctrl+Shift+O` |
| Collapse All | `Cmd+Shift+−` | `Ctrl+Shift+−` |
| Show All | `Cmd+Shift++` | `Ctrl+Shift++` |
| Quit | `Cmd+Q` | `Alt+F4` |

---

## 9. Design Recommendations

### Command/Action Model

- Introduce a `ConversationCommand` sealed interface mapping all toolbar, menu, and keyboard actions to a single dispatch point.
- Each command maps to an existing or new `ConversationAction` in the ViewModel.
- Inspired by LogViewer's `AppMenuActionKey` pattern but using a sealed interface for type safety and extensibility.
- Commands: `OpenSession`, `Refresh`, `ToggleAutoRefresh`, `ToggleSortOrder`, `CollapseAll`, `ShowAll`, `Copy`, `FocusSearch`, `FindNext`, `FindPrevious`, `Quit`, `About`.

### Toolbar UI

- Use `Surface(elevation = 2.dp)` with `MaterialTheme.colors.surface` background, matching LogViewer.
- 28dp `IconButton` with 18dp `Icon`, 2dp vertical padding.
- Group buttons with `Divider` separators: [Open Session] | [Refresh, Auto-Refresh] | [Sort Order] | [Collapse All, Show All] | [Copy] | [Search field + prev/next].
- Search field uses `Modifier.weight(1f)` to fill remaining space.
- Use Material Icons from `compose-material-icons-extended` (already in classpath).
- Support both Light and Dark themes via `ConversationColors` tokens.

### Menu Bar

- Add `MenuBar` inside `Window` block in `main.kt`.
- File: Open Session, Refresh, Quit.
- Edit: Copy, Find (focus search).
- View: Toggle Auto-Refresh, Toggle Sort Order, Collapse All, Show All, Find Next, Find Previous.
- Session: Reload from Disk (same as Refresh).
- Help: How to Use, About Junie Conversation Viewer.
- Use `menuShortcutSetForOs()` pattern for platform-specific shortcuts.

### Collapsible State

- Hoist expansion state for ThoughtBlock and ToolCallBlock to ViewModel (Approach A from Section 4).
- Add `expandedBlockIds: Set<String>` or per-message expansion state to `ConversationState`.
- "Collapse All" sets all to collapsed; "Show All" sets all to expanded.
- Per-block manual toggle overrides the global state.
- Search `forceExpanded` takes priority over global collapse.

### Sort Order

- Add `SortOrder` enum (`OldestFirst`, `NewestFirst`) to `ConversationState`.
- ViewModel re-sorts `filteredMessages` when sort order changes.
- Live tracking: in newest-first, prepend new messages; in oldest-first, append.
- Auto-scroll: scroll to top in newest-first, scroll to bottom in oldest-first.

### Auto-Refresh

- Add `isAutoRefreshEnabled: Boolean` to `ConversationState`, defaulting to `true`.
- When disabled, cancel `LiveSessionTracker` collection but keep the Session loaded.
- When re-enabled, restart tracking from current file position.
- Persist preference in `PreferencesRepository`.

### Filter Chips

- Keep filter chips in their current position below the toolbar.
- No changes to filter logic in Sprint 5.

---

## 10. HITL Review Summary

### What Was Audited

- All project documentation (UBIQUITOUS-LANGUAGE, TESTING, project_memory, RECAP, Sprint 4 docs).
- Current top chrome implementation: search field, filter chips, session picker, settings button.
- Live tracking and session loading flow: `LiveSessionTracker`, `ConversationViewModel`, `SessionRepositoryImpl`.
- Collapsible block state model: `CollapsibleBlock`, all 7 block types, `forceExpanded` logic.
- Copy and text selection: `SelectionContainer`, `CopyButton`, clipboard API limitations.
- Desktop app entry point: `main.kt`, window lifecycle, current keyboard shortcuts.
- LogViewer reference app: toolbar styling, menu structure, command model, keyboard shortcuts.

### Key Findings

1. **No toolbar or menu exists** — the app has search/filter chrome but no dedicated toolbar or application menu.
2. **Only one keyboard shortcut** (`Cmd+F`/`Ctrl+F`) is implemented.
3. **Collapsible state is local `remember`** — must be hoisted to ViewModel for global Collapse All/Show All.
4. **Compose Desktop does not expose selected text** to external code — global Copy command has limitations.
5. **Live tracking is always-on** — needs a toggle for Auto-refresh control.
6. **No sort order control exists** — messages are always displayed in file order (oldest-first).
7. **LogViewer provides a clear design reference** — 28dp icons, 2dp elevation, `Divider` separators, `MenuBar` DSL, `AppMenuActionKey` pattern.
8. **Filter chips should remain below the toolbar** to avoid overcrowding.
9. **Search field can move to toolbar** using `Modifier.weight(1f)` for responsive sizing.

### HITL Decisions Required

All 12 open questions (Q1–Q12) require HITL input before proceeding to Area 2.

### Recommended Next Step

Await HITL review and decisions on Q1–Q12. Once resolved, proceed to Area 2 (Command/Action Model Design).
