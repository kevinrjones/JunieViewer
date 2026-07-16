# Sprint 4 — Interaction, Live Tracking, and Event Coverage: Task Breakdown

## 1. Related Sprint

**Sprint document:** [
`docs/sprints/junie-conversation-viewer-sprint-4-interaction-live-tracking-and-event-coverage.md`](../sprints/junie-conversation-viewer-sprint-4-interaction-live-tracking-and-event-coverage.md)

**Sprint goal:** Improve the Junie Conversation Viewer from a polished static transcript viewer into a more interactive
and analysis-friendly tool by adding partial text selection/copy support, Search result highlighting, live Session
tracking, clearer sub-agent/event representation, filter coverage review, `AgentTaskFailedEvent` support, and updated
user/developer documentation.

## 2. Related Documents

| Document                                                                                                                                                                                           | Role                                                                                      |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| [`docs/sprints/junie-conversation-viewer-sprint-4-interaction-live-tracking-and-event-coverage.md`](../sprints/junie-conversation-viewer-sprint-4-interaction-live-tracking-and-event-coverage.md) | **Primary source of truth.** Defines the 9 delivery parts with concrete "After" sections. |
| [`docs/sprints/junie-conversation-viewer-sprint-3-ui-polish-and-theme-refresh.md`](../sprints/junie-conversation-viewer-sprint-3-ui-polish-and-theme-refresh.md)                                   | Preceding sprint; Sprint 4 builds on its baseline.                                        |
| [`docs/tasks/junie-conversation-viewer-tasks-sprint-3-ui-polish-and-theme-refresh.md`](junie-conversation-viewer-tasks-sprint-3-ui-polish-and-theme-refresh.md)                                    | Sprint 3 task breakdown for reference.                                                    |
| [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md)                                                                                                                                         | Canonical domain terms.                                                                   |
| [`docs/RECAP.md`](../RECAP.md)                                                                                                                                                                     | Chronological project history.                                                            |
| [`docs/TESTING.md`](../TESTING.md)                                                                                                                                                                 | Testing stack, Robot pattern, `testTag` conventions, Gradle commands.                     |
| [`docs/project_memory.md`](../project_memory.md)                                                                                                                                                   | Decisions, gotchas, shipped work.                                                         |
| [`docs/sprint-4-area-1-discovery-findings.md`](../sprint-4-area-1-discovery-findings.md)                                <br/>                                                                      | Discovery.                                                                                |
| [`docs/EVENT_CATALOG.md`](../../docs/EVENT_CATALOG.md)                                                                                                                                             | Catalogue of known Junie event types.                                                     |
| [`docs/junie-jsonl-deserialization-investigation.md`](../junie-jsonl-deserialization-investigation.md)                                                                                             | JSONL deserialization investigation and findings.                                         |

## 3. Purpose

This document breaks the Sprint 4 Interaction, Live Tracking, and Event Coverage sprint into concrete, trackable tasks.
It serves as:

- **Junie's implementation checklist** — each task has clear completion criteria, dependencies, and testing
  expectations.
- **HITL's review and progress checklist** — each task has a checkbox, and review-oriented tasks include HITL-visible
  outcomes.

## 4. How to Use This Task Document

1. **Before starting implementation**, read the Related Documents listed above.
2. **Work through tasks in area order** (1–9). Within each area, tasks are ordered by dependency.
3. **Check off tasks** (`- [x]`) only when all completion criteria are met.
4. **Mark parent tasks complete** only when all subtasks are complete.
5. **Use inline markers** (see Task Status Legend) to flag blocked, deferred, or review-dependent tasks.
6. **Update the Progress Summary** table as areas are completed.

## 5. Progress Summary

| # | Task Area                          | Status        | Task Count |
|---|------------------------------------|---------------|------------|
| 1 | Discovery and Scope Confirmation   | 9/9 complete  | 9          |
| 2 | Text Selection and Partial Copy    | 4/6 complete  | 6          |
| 3 | Sub-Agent and Event Representation | 6/7 complete  | 7          |
| 4 | Filter Coverage and Top Controls   | 0/6 complete  | 6          |
| 5 | Search Highlighting                | 0/8 complete  | 8          |
| 6 | Live Session Tracking              | 0/11 complete | 11         |
| 7 | AgentTaskFailedEvent Support       | 0/8 complete  | 8          |
| 8 | Documentation and How-To Updates   | 0/5 complete  | 5          |
| 9 | Testing, Review, and Completion    | 0/7 complete  | 7          |
|   | **Total**                          |               | **67**     |

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

*Source: Delivery Part 1. Confirms current state, audits filter coverage, inspects sub-agent and event models, and
records open questions before any code changes.*

#### 1.1 Read project documentation

- [x] Read project documentation

**Description:** Read `UBIQUITOUS-LANGUAGE.md`, `TESTING.md`, `project_memory.md`, `RECAP.md`, `EVENT_CATALOG.md`,
`junie-jsonl-deserialization-investigation.md`, and Sprint 3 docs to lock terminology and understand the current
baseline.

**Source:** Sprint doc section 2 (Related Documents).

**Dependencies:** None.

**Likely files / areas:** Documentation only.

**Completion criteria:**

- All related documents read and understood.
- Domain terms confirmed for use in implementation.

**Testing expectations:** No automated tests required.

#### 1.2 Audit filter buttons vs MessageKind values

- [x] Audit filter buttons vs MessageKind values

**Description:** Map all 18 `MessageKind` enum values to the current 6 filter toggles and `AlwaysShow` category.
Document which kinds are filterable, which are always visible, and which may be missing from the filter UI.

**Source:** Sprint doc section 16 (Filter Coverage), FR4.

**Dependencies:** 1.1.

**Likely files / areas:** `Message.kt` (`MessageKind`, `FilterCategory`), `FilterBar.kt`, `ConversationViewModel.kt`.

**Completion criteria:**

- Documented mapping table: `MessageKind` → `FilterCategory` → filter toggle (or AlwaysShow).
- List of any kinds not covered by current filters.
- Recommendation for missing/redundant filters.

**Testing expectations:** No automated tests required.

#### 1.3 Inspect current copy/select behaviour

- [x] Inspect current copy/select behaviour

**Description:** Review current text selection and copy behaviour across all content types. Document which areas support
copy (via buttons), which do not support text selection, and any Compose Desktop `SelectionContainer` limitations.

**Source:** Sprint doc section 14 (Text Selection), FR1, FR2.

**Dependencies:** 1.1.

**Likely files / areas:** `MessageItems.kt`, `CodeBlock.kt`, `DiffBlock.kt`, `TerminalOutputBlock.kt`,
`ThoughtBlock.kt`, `ToolCallBlock.kt`, `StructuredOutputBlock.kt`, `ErrorWarningBlock.kt`.

**Completion criteria:**

- Documented inventory of copy/select behaviour per content type.
- Identified conflicts between `SelectionContainer` and clickable/collapsible components.

**Testing expectations:** No automated tests required.

#### 1.4 Inspect current Search implementation

- [x] Inspect current Search implementation

**Description:** Review the current Search implementation: how queries are matched, how match navigation works, where
highlighting could be inserted, and how the current match index is tracked.

**Source:** Sprint doc section 17 (Search Highlighting), FR5, FR6.

**Dependencies:** 1.1.

**Likely files / areas:** `ConversationViewModel.kt`, `ConversationScreen.kt`, `SearchAndFilterChrome`.

**Completion criteria:**

- Documented Search flow: query input → matching → navigation → display.
- Identified insertion points for text highlighting.
- Noted how current match index is tracked and could be passed to highlight components.

**Testing expectations:** No automated tests required.

#### 1.5 Inspect current Session loading flow

- [x] Inspect current Session loading flow

**Description:** Review how Sessions are loaded: file discovery, `events.jsonl` parsing, `EventToMessageMapper`, and
state updates. Identify where incremental/live loading could be inserted.

**Source:** Sprint doc section 18 (Live Session Tracking), FR7, FR8.

**Dependencies:** 1.1.

**Likely files / areas:** `SessionRepositoryImpl.kt`, `JsonlParser.kt`, `ConversationViewModel.kt`.

**Completion criteria:**

- Documented Session loading flow: file selection → parsing → mapping → state.
- Identified insertion points for incremental parsing and live updates.
- Noted file watching approach options and trade-offs.

**Testing expectations:** No automated tests required.

#### 1.6 Inspect sub-agent and failure event models

- [x] Inspect sub-agent and failure event models

**Description:** Trace how `MessageKind.SubAgent` is created, which events produce it, and how sub-agent events flow
through `EventToMessageMapper`. Also inspect the event hierarchy for `AgentTaskFailedEvent` or similar failure events.

**Source:** Sprint doc sections 15, 19 (Sub-Agent, AgentTaskFailedEvent), FR3, FR9.

**Dependencies:** 1.1.

**Likely files / areas:** `AgentEvents.kt`, `TopLevelEvents.kt`, `EventSerializers.kt`, `EventToMessageMapper.kt`,
`Message.kt`.

**Completion criteria:**

- Documented sub-agent event flow: event types → mapper → MessageKind.SubAgent.
- Documented current failure event handling (or lack thereof).
- Identified where `AgentTaskFailedEvent` should be added.

**Testing expectations:** No automated tests required.

#### 1.7 Gather AgentTaskFailedEvent examples

- [x] Gather AgentTaskFailedEvent examples

**Description:** Search for real `AgentTaskFailedEvent` payloads in sample `events.jsonl` files or documentation. If
none found, document the unknown payload shape and confirm tolerant field approach.

**Source:** Sprint doc section 19, KD7, Q8.

**Dependencies:** 1.6.

**Likely files / areas:** Sample data files, `EVENT_CATALOG.md`.

**Completion criteria:**

- Either: real payload examples documented, OR: confirmed that payload shape is unknown and tolerant `JsonElement?`
  fields will be used.

**Testing expectations:** No automated tests required.

#### 1.8 Record open questions and design recommendations

- [x] Record open questions and design recommendations

**Description:** Compile all open questions from discovery tasks (1.2–1.7) with recommendations. Update sprint doc open
questions if new questions arise.

**Source:** Sprint doc section 25 (Open Questions).

**Dependencies:** 1.2, 1.3, 1.4, 1.5, 1.6, 1.7.

**Likely files / areas:** Documentation only.

**Completion criteria:**

- All open questions documented with context and recommendations.
- Design recommendations for sub-agent representation, filter coverage, and live tracking approach.

**Testing expectations:** No automated tests required.

#### 1.9 HITL review of discovery findings — `HITL Review`

- [x] HITL review of discovery findings

**Description:** Present the audit results, design recommendations, and open questions to the HITL for approval before
implementation begins.

**Source:** Sprint doc Part 1 "After" section.

**Dependencies:** 1.8.

**Likely files / areas:** Documentation only.

**Completion criteria:**

- HITL has reviewed and approved the discovery findings and design recommendations.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** Documented audit of filter coverage, sub-agent event flow, and open questions with design
recommendations.

---

### Area 2 — Text Selection and Partial Copy

*Source: Delivery Part 2. Adds text selection to readable content areas while preserving existing copy buttons.*

#### 2.1 Add SelectionContainer to message content areas

- [x] Add SelectionContainer to message content areas — `Test Required`

**Description:** Wrap Human Message text and Junie Message text/Markdown content in `SelectionContainer` in
`MessageItems.kt`. Ensure `SelectionContainer` does not wrap clickable/collapsible headers.

**Source:** Sprint doc section 14, FR1, NFR1, KD1.

**Dependencies:** 1.9.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/MessageItems.kt`.

**Completion criteria:**

- Human Message text is selectable and copyable.
- Junie Message text/Markdown content is selectable and copyable.
- Clickable/collapsible headers (ThoughtBlock, ToolCallBlock) are not wrapped in `SelectionContainer`.
- Copy produces plain text.

**Testing expectations:** UI test verifying `SelectionContainer` is present on message content.

#### 2.2 Add SelectionContainer to code blocks

- [x] Add SelectionContainer to code blocks — `Test Required`

**Description:** Wrap code block text content in `SelectionContainer`. Existing `CopyButton` remains outside the
selection container and continues to work.

**Source:** Sprint doc section 14, FR1, FR2.

**Dependencies:** 2.1.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/CodeBlock.kt`.

**Completion criteria:**

- Code block text is selectable and copyable via text selection.
- Existing `CopyButton` continues to work alongside text selection.

**Testing expectations:** UI test verifying both selection and copy button work.

#### 2.3 Add SelectionContainer to diff blocks

- [x] Add SelectionContainer to diff blocks — `Test Required`

**Description:** Wrap diff block text content in `SelectionContainer`. Existing `CopyButton` remains functional.

**Source:** Sprint doc section 14, FR1, FR2.

**Dependencies:** 2.1.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/DiffBlock.kt`.

**Completion criteria:**

- Diff block text is selectable and copyable.
- Existing `CopyButton` continues to work.

**Testing expectations:** UI test verifying selection works on diff content.

#### 2.4 Add SelectionContainer to terminal output and other blocks

- [x] Add SelectionContainer to terminal output and other blocks — `Test Required`

**Description:** Wrap terminal output, structured output, and error/warning block text content in `SelectionContainer`.
Existing copy buttons remain functional where present.

**Source:** Sprint doc section 14, FR1, FR2.

**Dependencies:** 2.1.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/TerminalOutputBlock.kt`, `StructuredOutputBlock.kt`,
`ErrorWarningBlock.kt`.

**Completion criteria:**

- Terminal output text is selectable and copyable.
- Structured output text is selectable and copyable.
- Error/warning block text is selectable and copyable.
- Existing copy buttons continue to work.

**Testing expectations:** UI tests verifying selection on each block type.

#### 2.5 Manual clipboard verification — `Manual Review Required`

- [x] Manual clipboard verification

**Description:** Manually verify that text selection and copy produces clean plain text on macOS. Test across Human
Messages, Junie text, code blocks, diff blocks, terminal output, structured output, and error/warning blocks.

**Source:** FR1, FR2, NFR1.

**Dependencies:** 2.1, 2.2, 2.3, 2.4.

**Likely files / areas:** Running application.

**Completion criteria:**

- Clipboard contains clean plain text after copy from each content type.
- No formatting artefacts or unexpected characters in copied text.

**Testing expectations:** Manual verification only.

#### 2.6 HITL review of text selection — `HITL Review`

- [x] HITL review of text selection

**Description:** Present text selection and copy behaviour to the HITL for approval.

**Source:** Sprint doc Part 2 "After" section.

**Dependencies:** 2.5.

**Likely files / areas:** Running application.

**Completion criteria:**

- HITL has reviewed and approved text selection behaviour.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** User can select and copy partial text from any message content area, while existing copy
buttons continue to work.

---

### Area 3 — Sub-Agent and Event Representation

*Source: Delivery Part 3. Identifies sub-agent event sources, proposes visual representation, and implements UI markers
after HITL design review.*

#### 3.1 Identify sub-agent event sources

- [x] Identify sub-agent event sources

**Description:** Trace all event types that produce `MessageKind.SubAgent` messages. Document the event flow from raw
JSONL through `EventToMessageMapper` to the UI.

**Source:** Sprint doc section 15, FR3.

**Dependencies:** 1.6.

**Likely files / areas:** `AgentEvents.kt`, `EventToMessageMapper.kt`, `Message.kt`.

**Completion criteria:**

- Complete list of event types that produce sub-agent messages.
- Documented event flow diagram or description.

**Testing expectations:** No automated tests required.

#### 3.2 Propose visual representation options

- [x] Propose visual representation options

**Description:** Based on discovery findings, propose 2–3 visual representation options for sub-agent messages. Options
may include: badge/label markers, nested message grouping, visual lane indicators, or icon-based differentiation.

**Source:** Sprint doc section 15, Q1, design principle 5.

**Dependencies:** 3.1.

**Likely files / areas:** Documentation only.

**Completion criteria:**

- 2–3 visual representation options documented with pros/cons.
- Each option ensures colour is not the sole differentiator.
- Recommendation for preferred option.

**Testing expectations:** No automated tests required.

#### 3.3 HITL design review for sub-agent representation — `HITL Review`

- [x] HITL design review for sub-agent representation

**Description:** Present visual representation options to the HITL for selection before implementation.

**Source:** Sprint doc Part 3, Q1.

**Dependencies:** 3.2.

**Likely files / areas:** Documentation only.

**Completion criteria:**

- HITL has selected a visual representation approach.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** Documented visual representation options with HITL-selected approach.

#### 3.4 Update MessageKind/mapping if needed

- [x] Update MessageKind/mapping if needed

**Description:** Based on HITL-selected design, update `MessageKind`, `FilterCategory`, or `EventToMessageMapper` if the
chosen representation requires model changes.

**Source:** Sprint doc section 15.

**Dependencies:** 3.3.

**Likely files / areas:** `Message.kt`, `EventToMessageMapper.kt`.

**Completion criteria:**

- Model changes (if any) implemented and consistent with chosen design.

**Testing expectations:** Unit tests if model changes are made.

#### 3.5 Add sub-agent UI markers/badges

- [x] Add sub-agent UI markers/badges — `Test Required`

**Description:** Implement the HITL-selected visual representation for sub-agent messages in the UI. Ensure colour is
not the sole differentiator (use icon/label/badge).

**Source:** Sprint doc section 15, FR3.

**Dependencies:** 3.3, 3.4.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/MessageItems.kt`, potentially `MessageKindMarker.kt`.

**Completion criteria:**

- Sub-agent messages are visually distinguishable from other message types.
- Colour is not the sole differentiator.
- Chronological order is preserved.

**Testing expectations:** UI test verifying sub-agent visual treatment is applied.

#### 3.6 Add sub-agent representation tests

- [x] Add sub-agent representation tests — `Test Required`

**Description:** Add automated tests for sub-agent event mapping and UI rendering.

**Source:** Sprint doc section 22, NFR6.

**Dependencies:** 3.5.

**Likely files / areas:** Test files in `shared/src/commonTest/kotlin/.../`.

**Completion criteria:**

- Tests verify sub-agent events are correctly mapped to messages.
- Tests verify sub-agent UI markers are rendered.

**Testing expectations:** Unit and UI tests.

#### 3.7 HITL review of sub-agent representation — `HITL Review`

- [ ] HITL review of sub-agent representation — awaiting HITL approval

**Description:** Present implemented sub-agent representation to the HITL for final approval.

**Source:** Sprint doc Part 3 "After" section.

**Dependencies:** 3.5, 3.6.

**Likely files / areas:** Running application.

**Completion criteria:**

- HITL has reviewed and approved sub-agent visual representation.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** Sub-agent activity is clearly distinguished within the Conversation with appropriate visual
markers.

---

### Area 4 — Filter Coverage and Top Controls

*Source: Delivery Part 4. Maps all MessageKind values to filters, decides missing/redundant filters, and updates the
filter UI.*

#### 4.1 Map all MessageKind values to filters

- [ ] Map all MessageKind values to filters

**Description:** Create a complete documented mapping from all 18 `MessageKind` values to `FilterCategory` values and
corresponding filter toggles. Identify any kinds not covered.

**Source:** Sprint doc section 16, FR4, Q3.

**Dependencies:** 1.2.

**Likely files / areas:** `Message.kt`, `FilterBar.kt`, `ConversationViewModel.kt`.

**Completion criteria:**

- Complete mapping table documented.
- All gaps identified.

**Testing expectations:** No automated tests required.

#### 4.2 Decide missing/redundant filters

- [ ] Decide missing/redundant filters

**Description:** Based on the mapping from 4.1, decide whether SubAgent, Mcp, TestRun, StructuredOutput, or other kinds
need dedicated filter toggles or should remain grouped under existing categories.

**Source:** Sprint doc section 16, Q2, Q3.

**Dependencies:** 4.1.

**Likely files / areas:** Documentation only.

**Completion criteria:**

- Decision documented for each `MessageKind` value.
- HITL-approved filter set defined.

**Testing expectations:** No automated tests required.

#### 4.3 Update FilterBar and ViewModel

- [ ] Update FilterBar and ViewModel — `Test Required`

**Description:** Add or modify filter toggles in `FilterBar.kt` and update filter logic in `ConversationViewModel.kt`
based on decisions from 4.2.

**Source:** Sprint doc section 16, FR4.

**Dependencies:** 4.2.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/FilterBar.kt`,
`shared/src/commonMain/.../ui/ConversationViewModel.kt`, `shared/src/commonMain/.../domain/Message.kt`.

**Completion criteria:**

- Filter bar shows all decided filter toggles.
- Filter logic correctly shows/hides messages based on active filters.
- Labels match `UBIQUITOUS-LANGUAGE.md` terms.

**Testing expectations:** Unit tests for filter logic; UI tests for filter bar rendering.

#### 4.4 Update filter tests

- [ ] Update filter tests — `Test Required`

**Description:** Add or update tests to verify all `MessageKind` values are correctly mapped to filter categories and
that filter toggles work as expected.

**Source:** Sprint doc section 22, NFR6.

**Dependencies:** 4.3.

**Likely files / areas:** Test files in `shared/src/commonTest/kotlin/.../`.

**Completion criteria:**

- Tests cover all `MessageKind` → `FilterCategory` mappings.
- Tests verify filter toggle behaviour for new/changed filters.

**Testing expectations:** Unit and UI tests.

#### 4.5 Ensure labels match UBIQUITOUS-LANGUAGE.md

- [ ] Ensure labels match UBIQUITOUS-LANGUAGE.md

**Description:** Verify all filter toggle labels use canonical terms from `UBIQUITOUS-LANGUAGE.md`. Update labels if
needed.

**Source:** Sprint doc section 16, design principle.

**Dependencies:** 4.3.

**Likely files / areas:** `FilterBar.kt`, `UBIQUITOUS-LANGUAGE.md`.

**Completion criteria:**

- All filter labels use canonical domain terms.

**Testing expectations:** No automated tests required.

#### 4.6 HITL review of filter coverage — `HITL Review`

- [ ] HITL review of filter coverage

**Description:** Present updated filter bar and documented mapping to the HITL for approval.

**Source:** Sprint doc Part 4 "After" section.

**Dependencies:** 4.3, 4.4, 4.5.

**Likely files / areas:** Running application, documentation.

**Completion criteria:**

- HITL has reviewed and approved filter coverage.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** All relevant Message Kinds are represented in the filter bar with clear, understandable
labels.

---

### Area 5 — Search Highlighting

*Source: Delivery Part 5. Adds theme-aware search match highlighting with current match distinction.*

#### 5.1 Add search highlight tokens to ConversationColors

- [ ] Add search highlight tokens to ConversationColors — `Test Required`

**Description:** Add `searchHighlightBackground`, `searchHighlightText`, `currentMatchBackground`, `currentMatchText`
tokens to `ConversationColors` with light and dark values per sprint doc section 12.1.

**Source:** Sprint doc sections 12.1, 13.1, KD4.

**Dependencies:** 1.4.

**Likely files / areas:** `shared/src/commonMain/.../ui/theme/ConversationColors.kt`.

**Completion criteria:**

- Four new tokens added to `ConversationColors`.
- Light and dark values defined per sprint doc.
- `lightConversationColors()` and `darkConversationColors()` updated.

**Testing expectations:** Unit test verifying new tokens have distinct light/dark values.

#### 5.2 Create search highlight utility

- [ ] Create search highlight utility — `Test Required`

**Description:** Create `highlightSearchMatches()` function that splits text into annotated spans with highlight
colours. Supports case-insensitive matching, current match distinction, and returns `AnnotatedString`.

**Source:** Sprint doc section 13.2, FR5, FR6.

**Dependencies:** 5.1.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/SearchHighlight.kt` (new).

**Completion criteria:**

- `highlightSearchMatches()` function exists and produces correct `AnnotatedString`.
- Case-insensitive matching works.
- Current match is highlighted differently from other matches.
- Empty query returns unhighlighted text.

**Testing expectations:** Unit tests for match splitting, current match, case-insensitive, empty query, no matches.

#### 5.3 Highlight matches in plain text messages

- [ ] Highlight matches in plain text messages — `Test Required`

**Description:** Apply search highlighting to plain text content in Human and Junie messages using the highlight
utility.

**Source:** Sprint doc section 17, FR5.

**Dependencies:** 5.2.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/MessageItems.kt`.

**Completion criteria:**

- Search matches are highlighted in plain text messages.
- Current match is visually distinct from other matches.

**Testing expectations:** UI test verifying highlights appear in message text.

#### 5.4 Highlight matches in Markdown/rich content

- [ ] Highlight matches in Markdown/rich content

**Description:** Apply search highlighting to Markdown-rendered content where practical. This may require integration
with the Markdown rendering pipeline.

**Source:** Sprint doc section 17, FR5, Q4.

**Dependencies:** 5.2.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/MarkdownContent.kt`.

**Completion criteria:**

- Search matches are highlighted in Markdown content where practical.
- Highlighting does not break Markdown rendering.

**Testing expectations:** Manual verification; automated test if practical.

#### 5.5 Highlight matches in code/diff/terminal blocks

- [ ] Highlight matches in code/diff/terminal blocks

**Description:** Apply search highlighting to code blocks, diff blocks, and terminal output blocks where practical.

**Source:** Sprint doc section 17, FR5, Q4.

**Dependencies:** 5.2.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/CodeBlock.kt`, `DiffBlock.kt`,
`TerminalOutputBlock.kt`, `StructuredOutputBlock.kt`, `ErrorWarningBlock.kt`.

**Completion criteria:**

- Search matches are highlighted in code/diff/terminal/structured/error blocks where practical.
- Highlighting does not break existing block rendering.

**Testing expectations:** Manual verification; automated test if practical.

#### 5.6 Integrate highlighting with match navigation

- [ ] Integrate highlighting with match navigation — `Test Required`

**Description:** Connect the search highlight system with the existing match navigation (prev/next). Pass the current
match index to the highlight utility so the current match is highlighted distinctly.

**Source:** Sprint doc section 17, FR6.

**Dependencies:** 5.3.

**Likely files / areas:** `shared/src/commonMain/.../ui/ConversationViewModel.kt`, `ConversationScreen.kt`.

**Completion criteria:**

- Current match is highlighted distinctly from other matches.
- Navigating to next/prev match updates the current match highlight.

**Testing expectations:** Unit test for current match index tracking; UI test for distinct highlight.

#### 5.7 Add search highlighting tests

- [ ] Add search highlighting tests — `Test Required`

**Description:** Add comprehensive tests for the search highlighting system: utility function, integration with
messages, and match navigation.

**Source:** Sprint doc section 22, NFR6.

**Dependencies:** 5.6.

**Likely files / areas:** Test files in `shared/src/commonTest/kotlin/.../`.

**Completion criteria:**

- Tests cover highlight utility edge cases.
- Tests cover integration with message rendering.
- Tests cover match navigation with highlighting.

**Testing expectations:** Unit and UI tests.

#### 5.8 HITL review of search highlighting — `HITL Review`

- [ ] HITL review of search highlighting

**Description:** Present search highlighting in both themes to the HITL for approval. Verify WCAG AA contrast.

**Source:** Sprint doc Part 5 "After" section, NFR4.

**Dependencies:** 5.6, 5.7.

**Likely files / areas:** Running application.

**Completion criteria:**

- HITL has reviewed and approved search highlighting in both themes.
- Highlight colours meet WCAG AA contrast.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** Matching Search text is highlighted in the Conversation with theme-aware colours, and the
current match is highlighted distinctly.

---

### Area 6 — Live Session Tracking

*Source: Delivery Part 6. Implements file watching with polling fallback, incremental parsing, and scroll-preserving UI
updates.*

#### 6.1 Design file watching approach

- [ ] Design file watching approach

**Description:** Based on discovery findings (1.5), design the file watching approach: `WatchService` with polling
fallback, or polling-only. Document the approach and trade-offs.

**Source:** Sprint doc section 18, KD5, KD6, Q7.

**Dependencies:** 1.5.

**Likely files / areas:** Documentation only.

**Completion criteria:**

- File watching approach documented with rationale.
- Polling interval and fallback strategy defined.

**Testing expectations:** No automated tests required.

#### 6.2 Implement FileWatcher interface

- [ ] Implement FileWatcher interface — `Test Required`

**Description:** Create `FileWatcher` interface in `data/` with a method to watch a file path and emit change events via
`Flow`. Implement JVM version using `WatchService` with polling fallback.

**Source:** Sprint doc section 18, KD5.

**Dependencies:** 6.1.

**Likely files / areas:** `shared/src/commonMain/.../data/FileWatcher.kt` (new), JVM implementation.

**Completion criteria:**

- `FileWatcher` interface defined with `Flow`-based API.
- JVM implementation using `WatchService` or polling.
- Handles file not found and permission errors gracefully.

**Testing expectations:** Unit test for file change detection.

#### 6.3 Implement LiveSessionTracker

- [ ] Implement LiveSessionTracker — `Test Required`

**Description:** Create `LiveSessionTracker` that uses `FileWatcher` to watch the selected Session's `events.jsonl`.
Maintains byte offset for incremental reading.

**Source:** Sprint doc section 18, KD6.

**Dependencies:** 6.2.

**Likely files / areas:** `shared/src/commonMain/.../data/LiveSessionTracker.kt` (new).

**Completion criteria:**

- `LiveSessionTracker` watches a specific `events.jsonl` file.
- Maintains byte offset to read only new content.
- Emits new lines via `Flow`.

**Testing expectations:** Unit test for incremental reading.

#### 6.4 Implement incremental parsing

- [ ] Implement incremental parsing — `Test Required`

**Description:** Parse only newly appended lines from `events.jsonl` using `JsonlParser`. Handle partial lines (
incomplete JSON at EOF during active write).

**Source:** Sprint doc section 18, KD6, NFR2.

**Dependencies:** 6.3.

**Likely files / areas:** `shared/src/commonMain/.../data/JsonlParser.kt`, `LiveSessionTracker.kt`.

**Completion criteria:**

- Only new lines are parsed (no re-parsing of entire file).
- Partial lines at EOF are buffered and retried on next change.
- Parse errors on individual lines do not stop processing.

**Testing expectations:** Unit tests for incremental parsing, partial line handling, and error recovery.

#### 6.5 Update ViewModel for live updates

- [ ] Update ViewModel for live updates — `Test Required`

**Description:** Update `ConversationViewModel` to accept new `Message` objects from `LiveSessionTracker` via `Flow`.
Append to `ConversationState.messages` and re-apply filters and search.

**Source:** Sprint doc section 18, FR7.

**Dependencies:** 6.4.

**Likely files / areas:** `shared/src/commonMain/.../ui/ConversationViewModel.kt`.

**Completion criteria:**

- ViewModel subscribes to live updates when a Session is selected.
- New messages are appended to state.
- Filters and search are re-applied after appending.
- Subscription is cancelled when Session changes or is deselected.

**Testing expectations:** Unit test for state updates with new messages.

#### 6.6 Implement scroll preservation and auto-scroll

- [ ] Implement scroll preservation and auto-scroll

**Description:** Preserve scroll position when new messages arrive during live tracking. Auto-scroll to bottom only when
the user is already near the bottom of the conversation.

**Source:** Sprint doc section 18, FR8.

**Dependencies:** 6.5.

**Likely files / areas:** `shared/src/commonMain/.../ui/ConversationScreen.kt`.

**Completion criteria:**

- Scroll position is preserved when new messages arrive and user is scrolled up.
- Auto-scroll to bottom occurs when user is near the bottom.
- "Near bottom" threshold is reasonable (e.g., within last 100px).

**Testing expectations:** Manual verification; automated test if practical.

#### 6.7 Handle partial writes and errors

- [ ] Handle partial writes and errors

**Description:** Handle file write races (partial JSON lines), file deletion, file truncation, and other error
conditions during live tracking.

**Source:** Sprint doc section 18, NFR2.

**Dependencies:** 6.4.

**Likely files / areas:** `LiveSessionTracker.kt`, `JsonlParser.kt`.

**Completion criteria:**

- Partial lines are buffered, not discarded.
- File deletion or truncation is handled gracefully (stop tracking, log warning).
- Errors are logged, not thrown to UI.

**Testing expectations:** Unit tests for error conditions.

#### 6.8 Add logging for live tracking

- [ ] Add logging for live tracking

**Description:** Add appropriate logging throughout the live tracking pipeline: file watch events, parse results,
errors, start/stop lifecycle.

**Source:** NFR2, NFR3.

**Dependencies:** 6.5.

**Likely files / areas:** `FileWatcher.kt`, `LiveSessionTracker.kt`, `ConversationViewModel.kt`.

**Completion criteria:**

- Debug-level logging for file watch events and parse results.
- Warning-level logging for errors and edge cases.
- Info-level logging for start/stop lifecycle.

**Testing expectations:** No automated tests required.

#### 6.9 Add live tracking tests

- [ ] Add live tracking tests — `Test Required`

**Description:** Add comprehensive tests for the live tracking pipeline: file watching, incremental parsing, ViewModel
state updates, error handling.

**Source:** Sprint doc section 22, NFR6.

**Dependencies:** 6.5, 6.7.

**Likely files / areas:** Test files in `shared/src/commonTest/kotlin/.../`.

**Completion criteria:**

- Tests cover file change detection.
- Tests cover incremental parsing.
- Tests cover ViewModel state updates with live data.
- Tests cover error conditions.

**Testing expectations:** Unit tests.

#### 6.10 Cross-platform manual review — `Manual Review Required`

- [ ] Cross-platform manual review

**Description:** Manually verify live tracking behaviour on macOS. Note any platform-specific issues for Windows/Linux
follow-up.

**Source:** Sprint doc section 21, NFR5.

**Dependencies:** 6.5, 6.6.

**Likely files / areas:** Running application.

**Completion criteria:**

- Live tracking works on macOS with acceptable latency.
- Any platform-specific issues documented.

**Testing expectations:** Manual verification only.

#### 6.11 HITL review of live tracking — `HITL Review`

- [ ] HITL review of live tracking

**Description:** Present live tracking behaviour to the HITL for approval. Demonstrate real-time updates with a live
Session.

**Source:** Sprint doc Part 6 "After" section.

**Dependencies:** 6.9, 6.10.

**Likely files / areas:** Running application.

**Completion criteria:**

- HITL has reviewed and approved live tracking behaviour.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** The Conversation updates in near real time as new Events are appended to the selected
Session's `events.jsonl`.

---

### Area 7 — `AgentTaskFailedEvent` Support

*Source: Delivery Part 7. Adds event model, serializer, mapper, and rendering for `AgentTaskFailedEvent`.*

#### 7.1 Inspect AgentTaskFailedEvent payload

- [ ] Inspect AgentTaskFailedEvent payload

**Description:** Review any available payload examples from discovery (1.7). Confirm field names and types, or confirm
tolerant `JsonElement?` approach.

**Source:** Sprint doc section 19, KD7, Q8.

**Dependencies:** 1.7.

**Likely files / areas:** Sample data, `EVENT_CATALOG.md`.

**Completion criteria:**

- Payload shape documented (known fields or confirmed unknown).
- Field approach decided (tolerant nullable fields).

**Testing expectations:** No automated tests required.

#### 7.2 Add AgentTaskFailedEvent data class

- [ ] Add AgentTaskFailedEvent data class — `Test Required`

**Description:** Add `AgentTaskFailedEvent` data class to `AgentEvents.kt` with `@SerialName("AgentTaskFailedEvent")`.
Use tolerant fields: `val message: String? = null`, `val details: JsonElement? = null`, `val taskId: String? = null`.

**Source:** Sprint doc section 19, KD7.

**Dependencies:** 7.1.

**Likely files / areas:** `shared/src/commonMain/.../data/events/AgentEvents.kt`.

**Completion criteria:**

- `AgentTaskFailedEvent` data class exists with tolerant nullable fields.
- `@SerialName` annotation is correct.

**Testing expectations:** Serialization test for the new event class.

#### 7.3 Register in EventSerializers

- [ ] Register in EventSerializers — `Test Required`

**Description:** Register `AgentTaskFailedEvent` in the polymorphic dispatch map in `EventSerializers.kt`.

**Source:** Sprint doc section 19.

**Dependencies:** 7.2.

**Likely files / areas:** `shared/src/commonMain/.../data/events/EventSerializers.kt`.

**Completion criteria:**

- `AgentTaskFailedEvent` is registered in the polymorphic serializer.
- Deserialization of `AgentTaskFailedEvent` JSON produces the correct data class.

**Testing expectations:** Parser test for deserialization.

#### 7.4 Map to Message in EventToMessageMapper

- [ ] Map to Message in EventToMessageMapper — `Test Required`

**Description:** Add mapping from `AgentTaskFailedEvent` to `Message` with `MessageKind.Error` and `Sender.Junie` in
`EventToMessageMapper`.

**Source:** Sprint doc section 19, FR9.

**Dependencies:** 7.3.

**Likely files / areas:** `shared/src/commonMain/.../data/SessionRepositoryImpl.kt` (or wherever `EventToMessageMapper`
lives).

**Completion criteria:**

- `AgentTaskFailedEvent` is mapped to a `Message` with appropriate kind and sender.
- Message content includes the failure message and any available details.

**Testing expectations:** Repository mapping test.

#### 7.5 Render as error block

- [ ] Render as error block — `Test Required`

**Description:** Ensure `AgentTaskFailedEvent` messages are rendered using the existing `ErrorWarningBlock` with a "Task
Failed" label.

**Source:** Sprint doc section 19, FR9.

**Dependencies:** 7.4.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/ErrorWarningBlock.kt`, `MessageItems.kt`.

**Completion criteria:**

- `AgentTaskFailedEvent` messages render as error blocks with "Task Failed" label.
- Block is visually consistent with existing error/warning blocks.

**Testing expectations:** UI test for error block rendering.

#### 7.6 Add parser tests

- [ ] Add parser tests — `Test Required`

**Description:** Add tests for `AgentTaskFailedEvent` deserialization: valid payload, minimal payload (all nulls),
unknown extra fields.

**Source:** Sprint doc section 22, NFR6.

**Dependencies:** 7.3.

**Likely files / areas:** Test files in `shared/src/commonTest/kotlin/.../data/`.

**Completion criteria:**

- Tests cover valid payload deserialization.
- Tests cover minimal payload (all nullable fields null).
- Tests cover unknown extra fields (tolerant parsing).

**Testing expectations:** Unit tests.

#### 7.7 Verify unknown fallback remains intact

- [ ] Verify unknown fallback remains intact — `Test Required`

**Description:** Verify that `UnknownJunieEvent`/`UnknownAgentEvent` fallback still works correctly for truly unknown
events after adding `AgentTaskFailedEvent`.

**Source:** Sprint doc section 19, NFR6.

**Dependencies:** 7.3.

**Likely files / areas:** Test files, `EventSerializers.kt`.

**Completion criteria:**

- Existing unknown event fallback tests still pass.
- New test with a fabricated unknown event type confirms fallback works.

**Testing expectations:** Unit test.

#### 7.8 HITL review of AgentTaskFailedEvent — `HITL Review`

- [ ] HITL review of AgentTaskFailedEvent

**Description:** Present `AgentTaskFailedEvent` rendering to the HITL for approval.

**Source:** Sprint doc Part 7 "After" section.

**Dependencies:** 7.5, 7.6, 7.7.

**Likely files / areas:** Running application or test output.

**Completion criteria:**

- HITL has reviewed and approved `AgentTaskFailedEvent` rendering.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** `AgentTaskFailedEvent` is rendered as a visible error/failure block, and existing unknown
event handling remains intact.

---

### Area 8 — Documentation and How-To Updates

*Source: Delivery Part 8. Creates/updates user-facing and developer-facing documentation.*

#### 8.1 Create or update HOW_TO_USE.md

- [ ] Create or update HOW_TO_USE.md

**Description:** Create `docs/HOW_TO_USE.md` (or add a comprehensive "How to Use" section to `README.md`) covering:
choosing a Session, searching, using filters, match navigation, theme selection, interpreting Human/Junie/sub-agent
messages, interpreting rich content blocks, live tracking, troubleshooting missing/unsupported events.

**Source:** Sprint doc section 8 (Documentation), FR10.

**Dependencies:** Areas 2–7 complete.

**Likely files / areas:** `docs/HOW_TO_USE.md` (new) or `README.md`.

**Completion criteria:**

- All viewer features documented with clear instructions.
- Covers new Sprint 4 features (text selection, search highlighting, live tracking, sub-agent representation).

**Testing expectations:** No automated tests required.

#### 8.2 Update README.md

- [ ] Update README.md

**Description:** Update `README.md` to reflect Sprint 4 features and capabilities. Use the `readme-updater` skill.

**Source:** Sprint doc Part 8, guidelines.

**Dependencies:** Areas 2–7 complete.

**Likely files / areas:** `README.md`.

**Completion criteria:**

- README reflects current application state including Sprint 4 features.

**Testing expectations:** No automated tests required.

#### 8.3 Update TESTING.md if needed

- [ ] Update TESTING.md if needed

**Description:** Update `docs/TESTING.md` if testing guidance has changed (new test patterns, new test commands, new
test areas).

**Source:** Sprint doc Part 8.

**Dependencies:** Areas 2–7 complete.

**Likely files / areas:** `docs/TESTING.md`.

**Completion criteria:**

- TESTING.md reflects any new testing patterns or areas.

**Testing expectations:** No automated tests required.

#### 8.4 Update RECAP.md

- [ ] Update RECAP.md

**Description:** Add Sprint 4 entry to `docs/RECAP.md` with shipped features, key decisions, and completion date.

**Source:** Sprint doc Part 8, guidelines.

**Dependencies:** Areas 2–7 complete.

**Likely files / areas:** `docs/RECAP.md`.

**Completion criteria:**

- Sprint 4 entry added to RECAP.md.

**Testing expectations:** No automated tests required.

#### 8.5 Update project_memory.md

- [ ] Update project_memory.md

**Description:** Update `docs/project_memory.md` with Sprint 4 shipped work, key decisions, gotchas, and test coverage
areas. Use the `project-memory` skill.

**Source:** Sprint doc Part 8, guidelines.

**Dependencies:** Areas 2–7 complete.

**Likely files / areas:** `docs/project_memory.md`.

**Completion criteria:**

- project_memory.md updated with Sprint 4 information.

**Testing expectations:** No automated tests required.

---

### Area 9 — Testing, Review, and Completion

*Source: Delivery Part 9. Final test suite run, manual checklist, cyclomatic complexity check, and HITL final approval.*

#### 9.1 Run ./gradlew :shared:jvmTest

- [ ] Run ./gradlew :shared:jvmTest

**Description:** Run the shared module JVM tests and verify all pass.

**Source:** Sprint doc section 22, NFR6.

**Dependencies:** Areas 2–7 complete.

**Likely files / areas:** All test files.

**Completion criteria:**

- All shared JVM tests pass.

**Testing expectations:** Full test suite execution.

#### 9.2 Run ./gradlew test

- [ ] Run ./gradlew test

**Description:** Run the full project test suite and verify all pass.

**Source:** Sprint doc section 22, NFR6.

**Dependencies:** 9.1.

**Likely files / areas:** All test files.

**Completion criteria:**

- All project tests pass.

**Testing expectations:** Full test suite execution.

#### 9.3 Add/extend Robot tests

- [ ] Add/extend Robot tests — `Test Required`

**Description:** Add or extend Robot-pattern UI tests for new Sprint 4 components and behaviours.

**Source:** Sprint doc section 22, NFR6.

**Dependencies:** Areas 2–7 complete.

**Likely files / areas:** Test files in `shared/src/commonTest/kotlin/.../ui/`.

**Completion criteria:**

- Robot tests cover text selection, search highlighting, filter changes, and AgentTaskFailedEvent rendering.

**Testing expectations:** Robot-pattern UI tests.

#### 9.4 Run manual checklist — `Manual Review Required`

- [ ] Run manual checklist

**Description:** Execute the manual review checklist from sprint doc section 22.2.

**Source:** Sprint doc section 22.2.

**Dependencies:** 9.2.

**Likely files / areas:** Running application.

**Completion criteria:**

- All manual checklist items verified.
- Any issues documented and fixed.

**Testing expectations:** Manual verification.

#### 9.5 Run cyclomatic complexity check

- [ ] Run cyclomatic complexity check

**Description:** Run a cyclomatic complexity check on the codebase. Identify any functions or classes with high
complexity introduced during Sprint 4.

**Source:** Guidelines (sprint completion requirement).

**Dependencies:** 9.2.

**Likely files / areas:** All source files.

**Completion criteria:**

- Cyclomatic complexity report generated.
- High-complexity areas identified and either refactored or documented as accepted.

**Testing expectations:** No automated tests required.

#### 9.6 Fix review issues

- [ ] Fix review issues

**Description:** Address any issues found during testing, manual review, or complexity check.

**Source:** Sprint doc Part 9.

**Dependencies:** 9.4, 9.5.

**Likely files / areas:** Any files with issues.

**Completion criteria:**

- All identified issues resolved or documented as deferred.

**Testing expectations:** Re-run affected tests after fixes.

#### 9.7 HITL final approval — `HITL Review`

- [ ] HITL final approval

**Description:** Present the complete Sprint 4 deliverables to the HITL for final approval.

**Source:** Sprint doc Part 9 "After" section, Definition of Done.

**Dependencies:** 9.6.

**Likely files / areas:** Running application, documentation.

**Completion criteria:**

- HITL has granted final approval for Sprint 4.
- All Definition of Done criteria met.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** All automated tests pass, manual review checklist completed, cyclomatic complexity reviewed,
and HITL grants final approval.

---

## 8. HITL Review Checkpoints

| # | Task | Area                 | HITL-Visible Outcome                                                                                       |
|---|------|----------------------|------------------------------------------------------------------------------------------------------------|
| 1 | 1.9  | Discovery            | Documented audit of filter coverage, sub-agent event flow, and open questions with design recommendations. |
| 2 | 2.6  | Text Selection       | User can select and copy partial text from any message content area.                                       |
| 3 | 3.3  | Sub-Agent            | Documented visual representation options with HITL-selected approach.                                      |
| 4 | 3.7  | Sub-Agent            | Sub-agent activity clearly distinguished within the Conversation.                                          |
| 5 | 4.6  | Filter Coverage      | All relevant Message Kinds represented in the filter bar.                                                  |
| 6 | 5.8  | Search Highlighting  | Matching Search text highlighted with theme-aware colours.                                                 |
| 7 | 6.11 | Live Tracking        | Conversation updates in near real time as new Events are appended.                                         |
| 8 | 7.8  | AgentTaskFailedEvent | AgentTaskFailedEvent rendered as a visible error/failure block.                                            |
| 9 | 9.7  | Completion           | All tests pass, manual review complete, HITL final approval.                                               |

## 9. Acceptance Criteria

- All 67 tasks marked complete.
- All `Test Required` tasks have passing automated tests.
- All `HITL Review` tasks have HITL approval.
- All `Manual Review Required` tasks have been manually verified.
- `./gradlew test` passes with no failures.
- `./gradlew :shared:jvmTest` passes with no failures.
- Cyclomatic complexity check run and reviewed.
- `README.md`, `project_memory.md`, and `RECAP.md` updated.
- Definition of Done from sprint doc section 26 is met.

## 10. Deferred / Out-of-Scope Items

| #  | Item                                                        |
|----|-------------------------------------------------------------|
| D1 | Export to Markdown/HTML                                     |
| D2 | Database ingestion                                          |
| D3 | Mobile UI                                                   |
| D4 | Full Markdown parser replacement                            |
| D5 | Advanced syntax highlighting (deferred from Sprint 3 as D4) |
| D6 | Multi-session comparison                                    |
| D7 | Cloud/remote sessions                                       |
| D8 | Full visual regression screenshot testing                   |
| D9 | Live editing/replaying of logs                              |

## 11. Notes / Decisions Log

| Date | Decision / Note                                                          |
|------|--------------------------------------------------------------------------|
|      | *(No entries yet — update as decisions are made during implementation.)* |
