# Sprint 8 Area 1 — Discovery and Scope Confirmation Findings

> **Date:** 2026-08-20
> **Author:** Junie (automated discovery)
> **Status:** Draft complete — pending HITL decisions for tasks 1.3 and 1.4
> **Sprint:** [Sprint 8 — Top-Level Session Search](sprints/junie-conversation-viewer-sprint-8-top-level-session-search.md)
> **Task doc:** [Sprint 8 Task Breakdown](tasks/junie-conversation-viewer-tasks-sprint-8-top-level-session-search.md)

---

## 1. Documentation Baseline

### 1.1 Sprint 8 baseline

Sprint 8 adds a new **Top-Level Session Search** capability while preserving current **Conversation Search** behavior.

- Primary delivery remains incremental: on-demand scan MVP first, indexing/caching deferred unless discovery proves it necessary.
- Existing search (`Search Messages...`, Find Next/Find Previous) remains scoped to the currently opened Conversation.
- New feature must stay explicitly separate in state, UI entry point, and command intent.

### 1.2 Canonical terminology checklist

Source of truth: `docs/UBIQUITOUS-LANGUAGE.md`.

| Term | Discovery confirmation for Sprint 8 Area 1 |
|---|---|
| **Conversation** | Treated as one ordered exchange for one selected Session. |
| **Session** | Treated as one run under `~/.junie/sessions/<sessionId>/events.jsonl`. |
| **Event** | Treated as one JSONL line parsed by `JsonlParser`. |
| **Message** | Treated as UI display unit derived from Event(s) via `EventToMessageMapper`. |
| **Human** | Use as sender term in docs/UI labels; avoid sender label `user`. |
| **Junie** | Use as sender term for assistant output. |
| **Search Query** | Free-text case-insensitive substring input. |
| **HITL** | Human reviewer for open decisions and sign-off. |
| **Conversation Search** | Search inside currently opened Conversation only. |
| **Top-Level Session Search** | Search across all discovered Sessions. |

### 1.3 Capability split (must stay explicit)

- **Conversation Search** searches Messages of the currently selected Session only.
- **Top-Level Session Search** searches all discovered Session files and returns Session-level results.

These are related but distinct capabilities and should not share one undifferentiated state model.

### 1.4 UI copy guidance

- Keep sender copy aligned to **Human** / **Junie**.
- Avoid ambiguous sender-oriented terms (for example, `user`/`assistant`) in sender context.
- Keep existing `Search Messages...` wording for Conversation Search; do not relabel it as global/top-level search.

---

## 2. Current Session Discovery and Loading Audit

### 2.1 Entry points and method seams

Primary seams:

- `SessionRepository.loadSession(sessionId, homePath)`
- `SessionRepository.listSessions(homePath)`
- `SessionRepository.getSessionInfo(sessionId, homePath)`
- `ConversationViewModel.loadSessions()` -> repository `listSessions`
- `ConversationViewModel.loadMessages()` -> repository `loadSession` + `getSessionInfo`

### 2.2 Home path and configuration flow

- Default home path is `~/.junie` via `SessionLoadState.junieHomePath`.
- `ConversationViewModel.loadPreferences()` loads persisted path from `PreferencesRepository` and applies it to state.
- `ConversationAction.OnHomePathChange` updates state and persists via `saveHomePath`.
- Repository expands `~` using `getPlatform().userHome` in `SessionRepositoryImpl.expandPath`.

### 2.3 How `events.jsonl` is located and read

- Session listing resolves `<homePath>/sessions` and enumerates directory children.
- Session load resolves `<homePath>/sessions/<sessionId>/events.jsonl`.
- File reading is line-by-line via `scanLines(path, onLine)`.

### 2.4 Malformed/unknown Event handling

- `loadSession` parses each non-blank line with `JsonlParser.parseLine`.
- Parse failures increment `parseErrors` and are skipped (load continues).
- Unknown event kinds are retained as unknown-event domain variants and logged as warnings.
- Missing session file returns `SessionLoadResult(emptyList(), path, 0L)`.

### 2.5 Event -> Message mapping

- Parsed `JunieEvent` values are transformed via `EventToMessageMapper.mapEventsToMessages(events)`.
- Output Messages are consumed by `ConversationState.sessionLoad.messages` and then filtered/derived for display by `MessageVisibilityEngine`.

### 2.6 Current failure behavior

- Invalid path resolution, missing directories/files, listing failures, and parse errors are handled defensively with logging and empty/partial outcomes.
- `ConversationViewModel.loadMessages()` surfaces user-facing error text when repository load throws.

### 2.7 Concrete insertion points for cross-Session search API

- **Repository contract:** `SessionRepository` interface (add new top-level search method, preserve existing methods unchanged).
- **Repository implementation:** `SessionRepositoryImpl` near `scanLines`/`loadSession` helpers for reusable line scanning and parse tolerance.
- **Domain model seam:** shared domain package near `SessionInfo`/`SessionLoadResult` for Session-level search result types.

---

## 3. Current Conversation Search Audit

### 3.1 Where Search Query state lives

- Query state is `ConversationState.search.searchQuery` (with convenience accessor `ConversationState.searchQuery`).
- Updated by `ConversationAction.OnSearchQueryChange` in `ConversationViewModel.onAction`.

### 3.2 How matching is performed

- Matching happens in `MessageVisibilityEngine.matches`.
- If query is non-blank, matching is case-insensitive substring against `MessageContentRegistry.searchableText(message)`.

### 3.3 Searchable text extraction

- `MessageContentRegistry.searchableText` delegates to per-kind descriptor extraction.
- Default extraction uses content payload text/code/diff/terminal/structured strings.

### 3.4 Filters + search interaction

- `MessageVisibilityEngine.derive` applies active `FilterState` first, then query matching, then sort order.
- `ConversationViewModel.updateState` always re-derives filtered messages/turns/current-match index in one consistent state update.

### 3.5 Match count and match navigation

- Match count is effectively `filteredMessages.size` when query is non-blank.
- Current index is `search.currentMatchIndex`.
- Next/previous navigation routes through `ConversationAction.OnNextMatch` / `OnPreviousMatch`.
- Toolbar shows count and current index; `Find Next`/`Find Previous` route through commands.

### 3.6 Menu/toolbar/command wiring

- Toolbar search UI: `ConversationToolbar` (`Search Messages...`, clear, next/previous controls).
- Command model: `ConversationCommand` includes `FocusSearch`, `FindNext`, `FindPrevious`.
- Menu binding: `desktopApp/.../JunieMenuBar.kt` Edit menu items and shortcuts (`Cmd/Ctrl+F`, `Cmd/Ctrl+G`, `Shift+Cmd/Ctrl+G`).
- One-time focus event: `ConversationEvent.FocusSearch`.

### 3.7 Non-regression boundaries for Sprint 8

- Keep current search query/filter/sort derivation behavior unchanged for in-conversation search.
- Keep existing keyboard/menu semantics for find focus/next/previous unchanged.
- Do not repurpose existing Conversation Search state as global search state.

---

## 4. Current Session Open / Selection Flow Audit

### 4.1 How a Human opens a Session today

- Entry points:
  - Toolbar `Open Session` action.
  - File menu `Open Session…` command (`ConversationCommand.OpenSession`).
- `OpenSession` maps to `ConversationAction.OnToggleSessionPicker`.
- `ConversationViewModel` toggles `DialogState.isSessionPickerOpen`; opening triggers `loadSessions()`.

### 4.2 Selection and load transitions

- Picker UI: `SessionSelector` renders available `SessionInfo` rows.
- Row click dispatches `ConversationAction.OnSessionSelected(session)`.
- ViewModel updates:
  - `selectedSessionId`
  - `selectedSession`
  - closes picker dialog
  - persists last session
  - calls `loadMessages()`

### 4.3 Where top-level result selection should connect later

Recommended integration seam for Area 5:

- Reuse existing `OnSessionSelected` path (or equivalent dedicated action that internally reuses same state transition) so top-level result selection opens Session via the proven load flow.

### 4.4 Risk: preserving vs clearing Conversation Search Query

- Current reload path preserves current Conversation Search state unless explicitly changed.
- Opening a different Session through current selector may carry prior query/filter into the new Conversation context.
- Sprint 8 must decide and document deterministic post-navigation behavior for Conversation Search Query when opening from top-level results.

---

## 5. Recommended MVP Search Strategy

### 5.1 Recommendation

Use an **on-demand scan** MVP across all discovered Sessions in Sprint 8.

- Scan discovered Session directories at query time.
- Read each `events.jsonl` line-by-line, with parse tolerance mirroring current repository behavior.
- Produce Session-level result summaries (identity, match count, preview snippet, partial-failure metadata).
- No persistent index/caching in Sprint 8 baseline.

### 5.2 Required behavioral characteristics

- Cancellable in-flight scans when a newer Search Query starts.
- Debounce support if live-as-you-type is enabled.
- Deterministic ordering (documented and testable).
- Partial-result handling for missing/unreadable/malformed Session data without hard-failing all results.

### 5.3 Why indexing is deferred now

- Existing repository already has resilient line-scan infrastructure suitable for MVP.
- Indexing adds invalidation and lifecycle complexity (freshness, live updates, storage format).
- MVP risk can be measured first with real Session volumes.

### 5.4 Conditions to revisit indexing

Revisit indexing only if measured evidence shows MVP is inadequate, for example:

- Query latency breaches agreed thresholds on realistic Session volumes.
- Frequent repeated queries over stable data produce unacceptable re-scan cost.
- Live-tracking + global search introduces user-visible stale-result issues that on-demand scan cannot address cleanly.

---

## 6. UX Entry Point and Trigger Recommendation

### 6.1 Recommended initial UX pattern (for HITL approval)

- Add a dedicated **Top-Level Session Search** dialog/panel opened from toolbar/menu command.
- Keep this entry point visually and behaviorally separate from existing `Search Messages...` field.
- Support both:
  - debounced live search while typing
  - explicit Enter submit
- Render Session-level result rows with compact snippets.
- Selecting a result opens Session via existing session-open flow in MVP.

### 6.2 Open questions requiring HITL confirmation

| # | Decision topic | Context | Recommendation | Risk / trade-off | HITL decision required |
|---|---|---|---|---|---|
| 1 | Entry point location | Existing toolbar already has in-conversation search and Open Session controls. | Use dedicated top-level dialog/panel launched from explicit command/button. | Reduces conflation risk; adds one more control in chrome/menu. | Yes |
| 2 | Execution timing | Live typing is responsive but can increase scan churn. | Support debounced live + Enter submit; cancel stale scans. | Needs careful coroutine cancellation/debounce tests. | Yes |
| 3 | Result granularity | Message-level rows can be noisy across many Sessions. | Session-level grouped rows in MVP with count + preview. | Less immediate deep-jump precision; clearer first pass. | Yes |
| 4 | Open-position behavior | Jump-to-first-match may require tighter mapping from match source to rendered Message. | Open Session normally first; defer jump-to-match unless low-risk seam emerges. | Safer MVP, but one extra step for Human to locate exact line. | Yes |
| 5 | Search source | Choices: raw JSON, parsed Event model, mapped Message text, or hybrid. | Prefer mapped searchable text pipeline (Event -> Message -> searchable text), with pragmatic fallback as needed. | Strict mapped-only may miss opaque unsupported payload fields. | Yes |
| 6 | Unsupported/unknown payloads | Unknown Events currently surface as unsupported indicators. | Include supported mapped text by default; include unknown payload text only when safely extractable and bounded. | Better recall vs potential noisy/unfriendly snippets. | Yes |
| 7 | Metadata in results | Session selector already surfaces working directory + time metadata. | Include timestamp/session identity in result rows when available. | Slightly denser rows; improves disambiguation. | Yes |
| 8 | Caching/indexing deferral | Sprint scope and complexity constraints favor MVP first. | Explicitly defer persistent indexing in Sprint 8. | Potential performance limits on very large data sets. | Yes |
| 9 | Conversation Search Query after open | Current flow can preserve query across session navigation. | Default recommendation: clear Conversation Search Query when opening from top-level result (or explicitly prompt/indicate behavior). | Clearing avoids confusion; preserving aids repeat workflow but risks conflation. | Yes |
| 10 | Live tracking impact on top-level results | Current live tracking is session-focused. | MVP: no automatic top-level result live updates; manual re-run of top-level query. | Simpler and predictable; less “always fresh” behavior. | Yes |

---

## 7. Proposed Implementation Insertion Points (Area 2+)

| Future concern | Likely insertion points |
|---|---|
| Top-level search domain models | `shared/src/commonMain/kotlin/com/knowledgespike/junieviewer/domain/` (near `SessionInfo`, `Message`, `JunieEvent`). |
| Repository/service contract | `shared/src/commonMain/kotlin/com/knowledgespike/junieviewer/data/SessionRepository.kt` interface. |
| Cross-session search implementation | `SessionRepositoryImpl` in same file, reusing `scanLines` + parse-tolerant patterns from `loadSession`/`extractWorkingDirectory`. |
| ViewModel state for top-level search | `ConversationState.kt` new concern object (parallel to existing `SearchState`, not merged). |
| ViewModel actions/events/commands | `ConversationAction.kt`, `ConversationEvent.kt`, `ConversationCommand.kt`, `ConversationViewModel.kt`. |
| Menu/toolbar entry point | `shared/.../ui/components/ConversationToolbar.kt`, `desktopApp/.../JunieMenuBar.kt`, plus command routing in `ConversationViewModel`. |
| Result UI surface | `shared/.../ui/components/` (new top-level search dialog/panel composable + result row composables). |
| Result-selection open flow | Reuse `ConversationAction.OnSessionSelected` transition path in `ConversationViewModel` and `SessionSelector`-adjacent interaction pattern. |
| Repository/domain tests | `shared/src/commonTest/kotlin/com/knowledgespike/junieviewer/data/SessionRepositoryTest.kt` and related domain test packages. |
| ViewModel state/cancellation tests | `shared/src/commonTest/kotlin/com/knowledgespike/junieviewer/ui/ConversationViewModelTest.kt`. |
| Compose UI tests | `shared/src/commonTest/kotlin/com/knowledgespike/junieviewer/ui/ConversationScreenTest.kt` plus new top-level search UI tests. |
| Desktop command/menu regression tests | `shared/src/jvmTest/kotlin/com/knowledgespike/junieviewer/SharedLogicDesktopTest.kt` and command-state tests in shared UI packages. |

---

## 8. Non-Regression Checklist (Conversation Search)

The following behavior must remain unchanged unless a later explicit HITL decision changes it:

- `Search Messages...` remains scoped to the currently opened Conversation.
- `Find Next` / `Find Previous` remain scoped to current Conversation matches.
- Current filter + search interaction remains unchanged.
- Current match highlighting/current-match index behavior remains unchanged.
- Current refresh/live-tracking interaction with Conversation Search remains unchanged.

---

## 9. Testing and Verification Planning (for future Areas)

Area 1 does not implement tests, but Area 2+ should include:

- Repository/domain tests for cross-session scan behavior, matching, deterministic ordering, snippets, and partial-failure capture.
- ViewModel tests for top-level search state transitions, cancellation, debounce timing, and stale-result suppression.
- Compose UI tests for top-level search entry point and states (loading/empty/results/partial/failure), plus keyboard interactions.
- Command/menu regression tests for new top-level command wiring without breaking existing find shortcuts.
- Existing Conversation Search regression coverage updates to prove no behavior drift.

Required verification commands at implementation completion:

- `./gradlew :shared:jvmTest`
- `./gradlew test`

---

## 10. HITL Review Summary

### What was audited

- Sprint/task/doc baselines and canonical terminology sources.
- Session discovery/loading seams and failure handling.
- Current Conversation Search state/matching/command wiring.
- Session selection/open transitions and existing test patterns.

### Key findings

- Existing architecture already separates session-loading and in-conversation search concerns; this supports adding a separate top-level search concern cleanly.
- Repository line-scan + parse-tolerant behavior provides a viable foundation for on-demand cross-session MVP scanning.
- Main risk is UX/state conflation; separation of entry points and state is mandatory for non-regression.

### Decisions required before Area 2

- Resolve the 10 open questions in Section 6.2 (especially entry point, trigger timing, post-open Conversation Search Query behavior, and indexing deferral confirmation).

### Recommended next step

- Accept Area 1 findings, capture HITL decisions for tasks 1.3 and 1.4, then start Area 2 domain/repository contract implementation with explicit non-regression guardrails from Section 8.
