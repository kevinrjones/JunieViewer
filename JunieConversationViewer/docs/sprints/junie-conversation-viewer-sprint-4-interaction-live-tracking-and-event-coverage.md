---
sprint: 4
name: Interaction, Live Tracking, and Event Coverage
status: planned
---

# 1. Title

Sprint 4 — Interaction, Live Tracking, and Event Coverage

# 2. Related Documents

- [`docs/sprints/junie-conversation-viewer-sprint-3-ui-polish-and-theme-refresh.md`](junie-conversation-viewer-sprint-3-ui-polish-and-theme-refresh.md) — the preceding sprint; Sprint 4 builds on its baseline.
- [`docs/tasks/junie-conversation-viewer-tasks-sprint-4-interaction-live-tracking-and-event-coverage.md`](../tasks/junie-conversation-viewer-tasks-sprint-4-interaction-live-tracking-and-event-coverage.md) — the companion task breakdown for this sprint.
- [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md) — canonical domain terms used consistently in code, tests, and UI copy.
- [`docs/RECAP.md`](../RECAP.md) — chronological project history.
- [`docs/TESTING.md`](../TESTING.md) — testing stack, Robot pattern, semantic `testTag` conventions, and Gradle commands.
- [`docs/project_memory.md`](../project_memory.md) — decisions, gotchas, and shipped work.
- [`docs/EVENT_CATALOG.md`](../../docs/EVENT_CATALOG.md) — catalogue of known Junie event types.
- [`docs/junie-jsonl-deserialization-investigation.md`](../junie-jsonl-deserialization-investigation.md) — JSONL deserialization investigation and findings.

# 3. Sprint Goal

Improve the Junie Conversation Viewer from a polished static transcript viewer into a more interactive and analysis-friendly tool by adding partial text selection/copy support, Search result highlighting, live Session tracking, clearer sub-agent/event representation, filter coverage review, `AgentTaskFailedEvent` support, and updated user/developer documentation.

# 4. Current Baseline

## 4.1 Theme and UI State

- Sprint 3 (UI Polish and Theme Refresh) is complete: themed Light/Dark/System modes, semantic tokens, polished chrome, asymmetric Human/Junie layout, rich content styling, accessibility.
- `JunieViewerTheme` composable with M3 `lightColorScheme`/`darkColorScheme` and `ConversationColors` semantic tokens via `CompositionLocal`.
- `ThemeMode` (Light/Dark/System) persisted in Preferences.

## 4.2 Domain Model State

- `MessageKind` enum has 18 values: `Human`, `Junie`, `Thought`, `ToolCall`, `ToolResult`, `Patch`, `Terminal`, `SubAgent`, `Error`, `Warning`, `Unsupported`, `TestRun`, `Mcp`, `Question`, `Choice`, `SystemMessage`, `Cancelled`, `Status`, `StructuredOutput`.
- `FilterCategory` enum has 7 values: `Human`, `Junie`, `Thought`, `Tool`, `Patch`, `Terminal`, `AlwaysShow`.
- Filter bar shows 6 toggles: Human, Junie, Thoughts, Tools, Patches, Terminal. `AlwaysShow` kinds are always visible.

## 4.3 Search State

- Search is case-insensitive substring matching.
- Match navigation exists (prev/next with wrap-around) but no text highlighting of matches.

## 4.4 Session Loading State

- Session loading is one-shot: parse entire `events.jsonl` on session selection.
- No live/incremental update capability.

## 4.5 Text Selection State

- Text is not selectable; copy is via dedicated copy buttons on code/diff/terminal blocks.

## 4.6 Event Handling State

- `AgentTaskFailedEvent` is not in the EVENT_CATALOG or codebase — payload shape unknown.
- Unknown events handled via `UnknownJunieEvent`/`UnknownAgentEvent` fallback.

# 5. Design Findings

## 5.1 Text Selection in Compose Desktop

- Compose provides `SelectionContainer` composable that enables text selection within its scope.
- `SelectionContainer` can wrap readable content areas without breaking existing interactive elements if scoped carefully.
- Clickable/collapsible headers (ThoughtBlock/ToolCallBlock toggles) should remain outside `SelectionContainer` to avoid conflicts.
- Existing `CopyButton` components remain functional alongside text selection.

## 5.2 File Watching Patterns

- `java.nio.file.WatchService` provides native file system event notification on JVM.
- macOS `WatchService` uses polling internally (not kqueue) — may have latency.
- Polling fallback provides reliable cross-platform behaviour.
- Incremental parsing (tracking byte offset) avoids re-parsing entire files on each change.

## 5.3 Search Highlighting Patterns

- Annotated string spans allow inline text highlighting in Compose.
- Two-colour scheme (current match vs other matches) provides clear navigation feedback.
- Theme-aware highlight colours ensure WCAG AA contrast in both light and dark modes.

# 6. Scope

- Text selection and partial copy across Human Messages, Junie text/Markdown, code blocks, diff blocks, terminal output, structured output, error/warning blocks.
- Sub-agent representation discovery and implementation.
- Filter/top button coverage audit against all `MessageKind`/`FilterCategory` values.
- Documentation and "how to" guidance updates.
- Search highlighting (matching text highlighted in UI, current match vs all matches, theme-aware).
- Live tracking of Session data (watch `events.jsonl`, incremental updates, scroll preservation).
- `AgentTaskFailedEvent` support (model, serializer, mapping, rendering, tests).

# 7. Out of Scope

- Export to Markdown/HTML.
- Database ingestion.
- Mobile UI.
- Full Markdown parser replacement.
- Advanced syntax highlighting.
- Multi-session comparison.
- Cloud/remote sessions.
- Full visual regression screenshot testing.
- Live editing/replaying of logs.

# 8. User Stories

- As a **HITL**, I can select and copy part of a Message's text, because I need to quote specific fragments in reviews or reports.
- As a **HITL**, I can see sub-agent activity clearly distinguished within the Conversation, because I need to understand which work was delegated.
- As a **HITL**, I can see all relevant Message Kinds represented in the filter bar, because I need to control what is visible without missing categories.
- As a **HITL**, I can see matching Search text highlighted in the Conversation, because visual highlighting helps me locate matches faster than scrolling through match navigation alone.
- As a **HITL**, I can see the Conversation update in near real time as Junie works, because I want to monitor progress without manually reloading.
- As a **HITL**, I can see `AgentTaskFailedEvent` rendered as a visible error/failure block, because task failures must not be silently dropped.
- As a **HITL**, I can find clear "how to" documentation, because I need to understand how to use the viewer's features.

# 9. Functional Requirements

- FR1: Text within Human Messages, Junie text/Markdown Messages, code blocks, diff blocks, terminal output, structured output, and error/warning blocks is selectable and copyable as plain text.
- FR2: Existing copy buttons on code/diff/terminal blocks continue to work alongside text selection.
- FR3: Sub-agent Messages are visually distinguishable (not by colour alone) and appear in chronological order.
- FR4: All `MessageKind` values that should be filterable have corresponding filter controls; a documented mapping exists.
- FR5: When a Search Query is active, matching text is highlighted in the Conversation with theme-aware colours.
- FR6: Current match is highlighted distinctly from other matches (if in scope after discovery).
- FR7: The UI updates incrementally as new Events are appended to the selected Session's `events.jsonl`.
- FR8: Scroll position is preserved during live updates; auto-scroll occurs only when already near the bottom.
- FR9: `AgentTaskFailedEvent` is parsed, mapped to a Message, and rendered as an error/failure block.
- FR10: User-facing and developer-facing documentation covers all viewer features.

# 10. Non-Functional Requirements

- NFR1: Text selection does not conflict with clickable/collapsible components (thoughts, tool calls).
- NFR2: Live tracking handles partial writes and file write races gracefully.
- NFR3: Live tracking performs acceptably on large Sessions (thousands of events).
- NFR4: Search highlighting meets WCAG AA contrast in both themes.
- NFR5: Cross-platform file watching works on macOS, Windows, and Linux (with polling fallback if needed).
- NFR6: All existing tests continue to pass; new tests added for each feature area.

# 11. Design Principles

1. **Selective wrapping.** `SelectionContainer` scopes to content areas only, not interactive headers or toggle controls.
2. **Theme-aware highlighting.** Search highlight colours use semantic tokens that adapt to light/dark themes.
3. **Incremental over full reload.** Live tracking parses only new data; scroll position is preserved.
4. **Tolerant parsing.** Unknown or partially-known event payloads use nullable/tolerant fields rather than failing.
5. **Discovery before design.** Sub-agent representation and filter coverage are audited before implementation decisions.
6. **Accessibility from the start.** Highlight colours meet WCAG AA contrast; text selection does not break screen-reader semantics.
7. **Fallback over failure.** File watching falls back to polling; unknown events fall back to existing handlers.

# 12. Proposed Visual System Additions

## 12.1 Search Highlight Tokens

| Token | Light | Dark | Usage |
|---|---|---|---|
| `searchHighlightBackground` | `#FFF176` | `#F9A825` | Background for all search matches |
| `searchHighlightText` | `#121212` | `#121212` | Text colour on search highlight |
| `currentMatchBackground` | `#FF8A65` | `#E65100` | Background for current/active match |
| `currentMatchText` | `#121212` | `#FFFFFF` | Text colour on current match highlight |

## 12.2 Sub-Agent Visual Treatment (Pending Discovery)

- Visual treatment to be determined after discovery task (Area 1).
- Options include: badge/label markers, nested message grouping, visual lane indicators.
- Colour must not be the sole differentiator — icon/label/badge required.

# 13. Theme Architecture Additions

## 13.1 Extended `ConversationColors`

Add search highlight tokens to the existing `ConversationColors` data class:

```kotlin
data class ConversationColors(
    // ... existing tokens ...
    val searchHighlightBackground: Color,
    val searchHighlightText: Color,
    val currentMatchBackground: Color,
    val currentMatchText: Color,
)
```

## 13.2 New Utility

```kotlin
// ui/components/SearchHighlight.kt
fun highlightSearchMatches(
    text: String,
    query: String,
    currentMatchIndex: Int,
    colors: ConversationColors
): AnnotatedString
```

# 14. Proposed Changes — Text Selection and Partial Copy

- Wrap message content areas in `SelectionContainer` in `MessageItems.kt`.
- Ensure `SelectionContainer` does not wrap clickable/collapsible headers (ThoughtBlock/ToolCallBlock toggle).
- Code/diff/terminal blocks: `SelectionContainer` around text content; existing `CopyButton` remains outside.
- Copy produces plain text.

**Files:** `MessageItems.kt`, `CodeBlock.kt`, `DiffBlock.kt`, `TerminalOutputBlock.kt`, `ThoughtBlock.kt`, `ToolCallBlock.kt`, `StructuredOutputBlock.kt`, `ErrorWarningBlock.kt`.

# 15. Proposed Changes — Sub-Agent and Event Representation

- Discovery task: inspect `MessageKind.SubAgent`, trace how sub-agent events flow through `EventToMessageMapper`, identify all sub-agent-related event kinds.
- Design decision: choose between nested messages, badges/markers, grouped blocks, or visual lanes.
- Implementation: update `MessageItems.kt` and potentially `MessageKindMarker` with sub-agent visual treatment.
- Ensure colour is not the sole differentiator (icon/label/badge).

**Files:** `MessageItems.kt`, `Message.kt`, `EventToMessageMapper.kt`, potentially `MessageKindMarker.kt`.

# 16. Proposed Changes — Filter Coverage and Top Controls

- Audit: map all 18 `MessageKind` values to current filter controls.
- Current filters: Human, Junie, Thought, Tool, Patch, Terminal (6 toggles).
- `AlwaysShow` kinds (Error, Warning, Unsupported, Question, Choice, SystemMessage, Cancelled, Status) are always visible — confirm this is correct.
- Decide: should SubAgent, Mcp, TestRun, StructuredOutput get dedicated filters or remain grouped under existing categories?
- Update `FilterBar.kt` and `ConversationViewModel` filter logic if new filters are added.

**Files:** `FilterBar.kt`, `ConversationViewModel.kt`, `Message.kt`.

# 17. Proposed Changes — Search Highlighting

- Add `searchHighlightBackground`, `searchHighlightText`, `currentMatchBackground`, `currentMatchText` tokens to `ConversationColors`.
- Create a `highlightSearchMatches()` utility that splits text into annotated spans.
- Apply highlighting in `MessageItems.kt`, `MarkdownContent.kt`, `CodeBlock.kt`, `DiffBlock.kt`, `TerminalOutputBlock.kt`, `StructuredOutputBlock.kt`, `ErrorWarningBlock.kt` where practical.
- Integrate with match navigation: pass current match index to highlight current match distinctly.

**Files:** `ConversationColors.kt`, `SearchHighlight.kt` (new), `MessageItems.kt`, `MarkdownContent.kt`, `CodeBlock.kt`, `DiffBlock.kt`, `TerminalOutputBlock.kt`, `StructuredOutputBlock.kt`, `ErrorWarningBlock.kt`, `ConversationViewModel.kt`.

# 18. Proposed Changes — Live Session Tracking

- Add `FileWatcher` interface in `data/` with JVM implementation using `WatchService` and polling fallback.
- Add `LiveSessionTracker` that watches the selected Session's `events.jsonl`.
- Maintain file byte offset; on change, read only new bytes, split into lines, parse via `JsonlParser`.
- Emit new `Message` objects to `ConversationViewModel` via `Flow`.
- ViewModel appends to `ConversationState.messages` and re-applies filters.
- Scroll behaviour: preserve position; auto-scroll only if user is near bottom.
- Start/stop lifecycle tied to session selection.
- Handle partial lines (incomplete JSON at EOF during write).

**Files:** `FileWatcher.kt` (new), `LiveSessionTracker.kt` (new), `SessionRepositoryImpl.kt`, `ConversationViewModel.kt`, `ConversationScreen.kt`, `JsonlParser.kt`.

# 19. Proposed Changes — `AgentTaskFailedEvent` Support

- Add `AgentTaskFailedEvent` data class to `AgentEvents.kt` with `@SerialName("AgentTaskFailedEvent")`.
- Use tolerant fields: `val message: String? = null`, `val details: JsonElement? = null`, `val taskId: String? = null`.
- Register in `EventSerializers` polymorphic dispatch map.
- Map to `Message` with `MessageKind.Error` (or new `TaskFailed` kind) and `Sender.Junie` in `EventToMessageMapper`.
- Render using existing `ErrorWarningBlock` with "Task Failed" label.
- Add parser, repository mapping, and UI rendering tests.

**Files:** `AgentEvents.kt`, `EventSerializers.kt`, `EventToMessageMapper.kt`, `ErrorWarningBlock.kt`.

# 20. Accessibility

- A1: Search highlight colours meet WCAG AA contrast ratios (4.5:1 for normal text) in both themes.
- A2: Text selection does not break screen-reader semantics or keyboard navigation.
- A3: Sub-agent visual treatment uses icon/label/badge, not colour alone.
- A4: New filter controls (if added) are keyboard-accessible with visible focus indicators.
- A5: Live tracking scroll behaviour does not disrupt keyboard focus position.
- A6: `AgentTaskFailedEvent` error blocks have appropriate `contentDescription`.

# 21. Cross-Platform Considerations

- C1: File watching behaviour varies by platform — `WatchService` on macOS uses polling internally.
- C2: Polling fallback ensures reliable cross-platform live tracking.
- C3: Text selection and clipboard behaviour verified on macOS, Windows, and Linux.
- C4: Search highlighting renders consistently across platforms.
- C5: Manual cross-platform verification for live tracking and clipboard.

# 22. Testing Strategy

## 22.1 Automated Tests

- Parser tests for `AgentTaskFailedEvent` deserialization.
- Repository mapping tests for `AgentTaskFailedEvent` → `Message`.
- Search highlight utility tests (match splitting, current match, case-insensitive).
- Filter coverage tests (all `MessageKind` values mapped correctly).
- Live tracking incremental parsing tests.
- Robot-pattern UI tests for new/changed components.
- Regression tests: all existing tests must pass.
- Test commands: `./gradlew test`, `./gradlew :shared:jvmTest`.

## 22.2 Manual Review Checklist

- Text selection and clipboard verification across content types.
- Search highlighting visual review in both themes.
- Live tracking with active Session (append events, verify UI updates).
- Sub-agent visual representation review.
- Filter bar completeness review.
- Cross-platform verification (macOS, Windows, Linux).
- `AgentTaskFailedEvent` rendering verification.

# 23. Incremental Delivery Plan

## Part 1 — Discovery and Scope Confirmation

- **Objective:** Read related docs, audit filter buttons vs `MessageKind` values, inspect copy/select behaviour, inspect Search implementation, inspect Session loading flow, inspect sub-agent and failure event models, gather `AgentTaskFailedEvent` examples, record open questions.
- **Files:** All domain model files, `ConversationViewModel.kt`, `EventToMessageMapper.kt`, `FilterBar.kt`, `ConversationScreen.kt`.
- **After:** *After this part, the HITL should see a documented audit of filter coverage, sub-agent event flow, and open questions with design recommendations.*

## Part 2 — Text Selection and Partial Copy

- **Objective:** Add `SelectionContainer` to message content areas, preserve copy buttons, handle code/diff/terminal selection, test partial selection.
- **Files:** `MessageItems.kt`, `CodeBlock.kt`, `DiffBlock.kt`, `TerminalOutputBlock.kt`, `ThoughtBlock.kt`, `ToolCallBlock.kt`, `StructuredOutputBlock.kt`, `ErrorWarningBlock.kt`.
- **After:** *After this part, the HITL should be able to select and copy partial text from any message content area, while existing copy buttons continue to work.*

## Part 3 — Sub-Agent and Event Representation

- **Objective:** Identify sub-agent event sources, propose visual representation, implement UI markers/badges after HITL design review.
- **Files:** `MessageItems.kt`, `Message.kt`, `EventToMessageMapper.kt`, potentially `MessageKindMarker.kt`.
- **After:** *After this part, the HITL should see sub-agent activity clearly distinguished within the Conversation with appropriate visual markers.*

## Part 4 — Filter Coverage and Top Controls

- **Objective:** Map all `MessageKind` values to filters, decide missing/redundant filters, update filter UI and ViewModel.
- **Files:** `FilterBar.kt`, `ConversationViewModel.kt`, `Message.kt`.
- **After:** *After this part, the HITL should see all relevant Message Kinds represented in the filter bar with clear, understandable labels.*

## Part 5 — Search Highlighting

- **Objective:** Add highlight tokens, create highlight utility, apply highlighting in text/Markdown/rich content, integrate with match navigation, distinguish current match.
- **Files:** `ConversationColors.kt`, `SearchHighlight.kt` (new), `MessageItems.kt`, `MarkdownContent.kt`, `CodeBlock.kt`, `DiffBlock.kt`, `TerminalOutputBlock.kt`, `StructuredOutputBlock.kt`, `ErrorWarningBlock.kt`, `ConversationViewModel.kt`.
- **After:** *After this part, the HITL should see matching Search text highlighted in the Conversation with theme-aware colours, and the current match highlighted distinctly.*

## Part 5A — Markdown Search Highlighting

- **Objective:** Add Search highlighting inside Markdown-rendered content, including headings, paragraphs, list items, and fenced code blocks. Supersedes the earlier decision to defer Markdown highlighting.
- **Files:** `MarkdownContent.kt`, `MessageItems.kt`.
- **After:** *After this part, the HITL should see Search matches highlighted inside Markdown-rendered Messages, with current-match styling distinct from regular-match styling.*

### Problem

Search highlights currently do not appear inside Markdown-rendered Messages, so matching text can be hard to locate even when Search filters/navigates to the correct Message.

### Functional Requirements

- **FR-MD1:** Search Query matches are highlighted inside Markdown headings.
- **FR-MD2:** Search Query matches are highlighted inside Markdown paragraphs.
- **FR-MD3:** Search Query matches are highlighted inside Markdown list items.
- **FR-MD4:** Search Query matches are highlighted inside Markdown fenced code blocks where practical.
- **FR-MD5:** Markdown inline formatting remains intact where practical when highlighting is applied.
- **FR-MD6:** Current-match styling is distinct from regular-match styling inside Markdown blocks.
- **FR-MD7:** Search remains case-insensitive and treats the Search Query as plain text, not regex.

### Non-Functional Requirements

- Highlighting must preserve readability in light and dark themes.
- Highlighting must not break Markdown parsing/rendering.
- Highlighting must not remove text selection/copy behavior.
- Highlighting must not replace the current lightweight Markdown renderer.
- Tests must cover parser/rendering behavior and UI integration.

### Acceptance Criteria

- Search matches are visible in Markdown headings, paragraphs, list items, and fenced code blocks.
- Current matching Message uses current-match highlight colours.
- Other matching Markdown Messages use regular Search highlight colours.
- Existing inline Markdown styling still works where practical.
- Automated tests cover Markdown Search highlighting.
- `./gradlew :shared:jvmTest` passes.

## Part 6 — Live Session Tracking

- **Objective:** Implement file watching with polling fallback, incremental parsing, ViewModel state updates, scroll preservation, partial write handling.
- **Files:** `FileWatcher.kt` (new), `LiveSessionTracker.kt` (new), `SessionRepositoryImpl.kt`, `ConversationViewModel.kt`, `ConversationScreen.kt`, `JsonlParser.kt`.
- **After:** *After this part, the HITL should see the Conversation update in near real time as new Events are appended to the selected Session's `events.jsonl`.*

## Part 7 — `AgentTaskFailedEvent` Support

- **Objective:** Add event model, register serializer, map to Message, render as error block, add tests.
- **Files:** `AgentEvents.kt`, `EventSerializers.kt`, `EventToMessageMapper.kt`, `ErrorWarningBlock.kt`.
- **After:** *After this part, the HITL should see `AgentTaskFailedEvent` rendered as a visible error/failure block, and existing unknown event handling should remain intact.*

## Part 8 — Documentation and How-To Updates

- **Objective:** Create/update user-facing and developer-facing documentation covering all viewer features.
- **Files:** `README.md`, `docs/HOW_TO_USE.md` (new or updated), `docs/TESTING.md`, `docs/RECAP.md`, `docs/project_memory.md`.
- **After:** *After this part, the HITL should find clear "how to" documentation covering all viewer features including new Sprint 4 capabilities.*

## Part 9 — Testing, Review, and Completion

- **Objective:** Run full test suite, add/extend Robot tests, run manual checklist, run cyclomatic complexity check, fix review issues, HITL final approval.
- **Files:** Test files in `shared/src/commonTest/kotlin/.../ui/`, `README.md`, `docs/project_memory.md`, `docs/RECAP.md`.
- **After:** *After this part, all automated tests should pass, the manual review checklist should be completed, cyclomatic complexity reviewed, and the HITL should grant final approval.*

# 24. Risks and Mitigations

- **R1 — Compose `SelectionContainer` conflicts with collapsible blocks:** `SelectionContainer` may interfere with clickable/collapsible headers. *Mitigation:* Scope `SelectionContainer` to content areas only, not toggle headers.
- **R2 — Sub-agent visual design unclear:** No established pattern for sub-agent representation. *Mitigation:* Discovery task before implementation; HITL review of design proposal.
- **R3 — Live tracking file watching unreliable cross-platform:** `WatchService` reliability varies by platform. *Mitigation:* Polling fallback; manual cross-platform verification.
- **R4 — `AgentTaskFailedEvent` payload unknown:** Not found in EVENT_CATALOG or codebase. *Mitigation:* Tolerant `JsonElement?` fields; document as open question.
- **R5 — Search highlighting performance on large Messages:** Highlighting many matches in long content may cause lag. *Mitigation:* Lazy highlighting; only highlight visible content.
- **R6 — Scope creep across 7 feature areas:** Many independent features in one sprint. *Mitigation:* Each area has clear completion criteria; HITL review gates between areas.

# 25. Open Questions

- **Q1:** What exact UI should represent sub-agents? Badges, nested messages, grouped blocks, or visual lanes? Discovery task will propose options.
- **Q2:** Should sub-agents be filterable separately? Currently `SubAgent` maps to `FilterCategory.Tool`. Should it have its own filter toggle?
- **Q3:** Which top filter buttons are missing or redundant? Audit task will produce a documented mapping.
- **Q4:** Should Search highlighting apply inside code/diff/terminal blocks, or only text/Markdown initially? Performance and complexity trade-off.
- **Q5:** Should current match be highlighted differently from all matches? Two-colour scheme adds complexity.
- **Q6:** Should live tracking auto-scroll when new events arrive? Only when user is already near the bottom, or always?
- **Q7:** Should live tracking use file watching, polling, or a hybrid? `WatchService` reliability varies by platform.
- **Q8:** What is the exact payload shape of `AgentTaskFailedEvent`? Not found in EVENT_CATALOG or codebase; use tolerant fields.
- **Q9:** Should partial text selection be supported everywhere, or only text-like content initially? Code/diff/terminal selection may have Compose limitations.

# 26. Definition of Done

This sprint is done when all below hold:

- Text within Human Messages, Junie text/Markdown, code blocks, diff blocks, terminal output, structured output, and error/warning blocks is selectable and copyable as plain text.
- Existing copy buttons continue to work alongside text selection.
- Sub-agent Messages are visually distinguishable (not by colour alone) and appear in chronological order.
- All `MessageKind` values that should be filterable have corresponding filter controls; a documented mapping exists.
- When a Search Query is active, matching text is highlighted with theme-aware colours.
- The UI updates incrementally as new Events are appended to the selected Session's `events.jsonl`.
- Scroll position is preserved during live updates.
- `AgentTaskFailedEvent` is parsed, mapped to a Message, and rendered as an error/failure block.
- User-facing and developer-facing documentation covers all viewer features.
- All existing tests pass; new tests added for each feature area.
- Manual cross-platform review completed (macOS, Windows, Linux).
- `README.md`, `project_memory.md`, and `RECAP.md` updated.
- Cyclomatic complexity check run and reviewed.
- HITL final approval granted.
