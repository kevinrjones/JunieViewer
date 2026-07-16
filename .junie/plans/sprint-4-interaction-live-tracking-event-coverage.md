---
sessionId: session-260716-062414-1c7g
---

# Requirements

### Overview & Goals

**Sprint 4 — Interaction, Live Tracking, and Event Coverage**

Improve the Junie Conversation Viewer from a polished static transcript viewer into a more interactive and analysis-friendly tool by adding partial text selection/copy support, Search result highlighting, live Session tracking, clearer sub-agent/event representation, filter coverage review, `AgentTaskFailedEvent` support, and updated user/developer documentation.

### Current Baseline

- Sprint 3 (UI Polish and Theme Refresh) is complete: themed Light/Dark/System modes, semantic tokens, polished chrome, asymmetric Human/Junie layout, rich content styling, accessibility.
- `MessageKind` enum has 18 values including `SubAgent`, `Error`, `Warning`, `Unsupported`, `TestRun`, `Mcp`, `Question`, `Choice`, `SystemMessage`, `Cancelled`, `Status`.
- `FilterCategory` enum has 7 values: `Human`, `Junie`, `Thought`, `Tool`, `Patch`, `Terminal`, `AlwaysShow`.
- Filter bar shows 6 toggles: Human, Junie, Thoughts, Tools, Patches, Terminal. `AlwaysShow` kinds are always visible.
- Search is case-insensitive substring matching; match navigation exists (prev/next with wrap-around) but no text highlighting.
- Session loading is one-shot: parse entire `events.jsonl` on session selection.
- `AgentTaskFailedEvent` is not in the EVENT_CATALOG or codebase — payload shape unknown.
- Text is not selectable; copy is via dedicated copy buttons on code/diff/terminal blocks.
- Unknown events handled via `UnknownJunieEvent`/`UnknownAgentEvent` fallback.

### Scope

**In Scope:**
1. Text selection and partial copy across Human Messages, Junie text/Markdown, code blocks, diff blocks, terminal output, structured output, error/warning blocks.
2. Sub-agent representation discovery and implementation.
3. Filter/top button coverage audit against all `MessageKind`/`FilterCategory` values.
4. Documentation and "how to" guidance updates.
5. Search highlighting (matching text highlighted in UI, current match vs all matches, theme-aware).
6. Live tracking of Session data (watch `events.jsonl`, incremental updates, scroll preservation).
7. `AgentTaskFailedEvent` support (model, serializer, mapping, rendering, tests).

**Out of Scope:**
- Export to Markdown/HTML.
- Database ingestion.
- Mobile UI.
- Full Markdown parser replacement.
- Advanced syntax highlighting.
- Multi-session comparison.
- Cloud/remote sessions.
- Full visual regression screenshot testing.
- Live editing/replaying of logs.

### User Stories

- As a **HITL**, I can select and copy part of a Message's text, because I need to quote specific fragments in reviews or reports.
- As a **HITL**, I can see sub-agent activity clearly distinguished within the Conversation, because I need to understand which work was delegated.
- As a **HITL**, I can see all relevant Message Kinds represented in the filter bar, because I need to control what is visible without missing categories.
- As a **HITL**, I can see matching Search text highlighted in the Conversation, because visual highlighting helps me locate matches faster than scrolling through match navigation alone.
- As a **HITL**, I can see the Conversation update in near real time as Junie works, because I want to monitor progress without manually reloading.
- As a **HITL**, I can see `AgentTaskFailedEvent` rendered as a visible error/failure block, because task failures must not be silently dropped.
- As a **HITL**, I can find clear "how to" documentation, because I need to understand how to use the viewer's features.

### Functional Requirements

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

### Non-Functional Requirements

- NFR1: Text selection does not conflict with clickable/collapsible components (thoughts, tool calls).
- NFR2: Live tracking handles partial writes and file write races gracefully.
- NFR3: Live tracking performs acceptably on large Sessions (thousands of events).
- NFR4: Search highlighting meets WCAG AA contrast in both themes.
- NFR5: Cross-platform file watching works on macOS, Windows, and Linux (with polling fallback if needed).
- NFR6: All existing tests continue to pass; new tests added for each feature area.

# Technical Design

### Current Implementation

**Key files and patterns:**
- `shared/src/commonMain/.../domain/Message.kt` — `MessageKind` enum (18 values), `FilterCategory` enum (7 values), `Message` data class.
- `shared/src/commonMain/.../ui/ConversationViewModel.kt` — MVI pattern, `ConversationState`/`ConversationAction`/`ConversationEvent`, filter logic in `applyFiltersAndSearch()`.
- `shared/src/commonMain/.../data/SessionRepositoryImpl.kt` — one-shot session loading, `EventToMessageMapper`.
- `shared/src/commonMain/.../data/JsonlParser.kt` — line-by-line JSONL parsing with `Either.catch`.
- `shared/src/commonMain/.../data/events/TopLevelEvents.kt`, `AgentEvents.kt`, `EventSerializers.kt` — sealed event hierarchies with unknown fallback.
- `shared/src/commonMain/.../ui/components/` — `MessageItems.kt`, `CodeBlock.kt`, `DiffBlock.kt`, `TerminalOutputBlock.kt`, `ThoughtBlock.kt`, `ToolCallBlock.kt`, `StructuredOutputBlock.kt`, `ErrorWarningBlock.kt`, `MarkdownContent.kt`.
- `shared/src/commonMain/.../ui/ConversationScreen.kt` — main screen with `SearchAndFilterChrome`.
- `shared/src/commonMain/.../ui/theme/` — `JunieViewerTheme`, `ConversationColors`, semantic tokens.

### Key Decisions

 # | Decision | Rationale |
---|---|---|
 KD1 | Use Compose `SelectionContainer` for text selection | Standard Compose Desktop API; wraps readable content areas without breaking existing copy buttons. |
 KD2 | Discovery-first approach for sub-agent representation | Sub-agent event sources and visual design need investigation before implementation; avoid premature UI decisions. |
 KD3 | Audit-first approach for filter coverage | Map all 18 `MessageKind` values to filter controls before changing UI; document the mapping. |
 KD4 | Add `searchHighlight*` semantic tokens to `ConversationColors` | Theme-aware highlight colours for both light and dark modes; current match vs all matches. |
 KD5 | File watching with polling fallback for live tracking | `java.nio.file.WatchService` on JVM; polling fallback for unreliable platforms. |
 KD6 | Incremental parsing for live tracking | Parse only newly appended lines; maintain byte/line offset to avoid re-parsing entire file. |
 KD7 | Tolerant `JsonElement?` fields for `AgentTaskFailedEvent` | Payload shape unknown; use tolerant fields and document as open question. |

### Proposed Changes

#### 1. Text Selection and Partial Copy
- Wrap message content areas in `SelectionContainer` in `MessageItems.kt`.
- Ensure `SelectionContainer` does not wrap clickable/collapsible headers (ThoughtBlock/ToolCallBlock toggle).
- Code/diff/terminal blocks: `SelectionContainer` around text content; existing `CopyButton` remains outside.
- Copy produces plain text.

#### 2. Sub-Agent Representation
- Discovery task: inspect `MessageKind.SubAgent`, trace how sub-agent events flow through `EventToMessageMapper`, identify all sub-agent-related event kinds.
- Design decision: choose between nested messages, badges/markers, grouped blocks, or visual lanes.
- Implementation: update `MessageItems.kt` and potentially `MessageKindMarker` with sub-agent visual treatment.
- Ensure colour is not the sole differentiator (icon/label/badge).

#### 3. Filter Coverage
- Audit: map all 18 `MessageKind` values to current filter controls.
- Current filters: Human, Junie, Thought, Tool, Patch, Terminal (6 toggles).
- `AlwaysShow` kinds (Error, Warning, Unsupported, Question, Choice, SystemMessage, Cancelled, Status) are always visible — confirm this is correct.
- Decide: should SubAgent, Mcp, TestRun, StructuredOutput get dedicated filters or remain grouped under existing categories?
- Update `FilterBar.kt` and `ConversationViewModel` filter logic if new filters are added.

#### 4. Search Highlighting
- Add `searchHighlightBackground`, `searchHighlightText`, `currentMatchBackground`, `currentMatchText` tokens to `ConversationColors`.
- Create a `highlightSearchMatches()` utility that splits text into annotated spans.
- Apply highlighting in `MessageItems.kt`, `MarkdownContent.kt`, `CodeBlock.kt`, `DiffBlock.kt`, `TerminalOutputBlock.kt`, `StructuredOutputBlock.kt`, `ErrorWarningBlock.kt` where practical.
- Integrate with match navigation: pass current match index to highlight current match distinctly.

#### 5. Live Session Tracking
- Add `FileWatcher` interface in `data/` with JVM implementation using `WatchService` and polling fallback.
- Add `LiveSessionTracker` that watches the selected Session's `events.jsonl`.
- Maintain file byte offset; on change, read only new bytes, split into lines, parse via `JsonlParser`.
- Emit new `Message` objects to `ConversationViewModel` via `Flow`.
- ViewModel appends to `ConversationState.messages` and re-applies filters.
- Scroll behaviour: preserve position; auto-scroll only if user is near bottom.
- Start/stop lifecycle tied to session selection.
- Handle partial lines (incomplete JSON at EOF during write).

#### 6. `AgentTaskFailedEvent` Support
- Add `AgentTaskFailedEvent` data class to `AgentEvents.kt` with `@SerialName("AgentTaskFailedEvent")`.
- Use tolerant fields: `val message: String? = null`, `val details: JsonElement? = null`, `val taskId: String? = null`.
- Register in `EventSerializers` polymorphic dispatch map.
- Map to `Message` with `MessageKind.Error` (or new `TaskFailed` kind) and `Sender.Junie` in `EventToMessageMapper`.
- Render using existing `ErrorWarningBlock` with "Task Failed" label.
- Add parser, repository mapping, and UI rendering tests.

#### 7. Documentation
- Create or update `docs/HOW_TO_USE.md` (or add to `README.md`) covering all features.
- Update `docs/TESTING.md` if testing guidance changes.
- Update `docs/project_memory.md` and `docs/RECAP.md` at sprint completion.

### Risks

 # | Risk | Mitigation |
---|---|---|
 R1 | Compose `SelectionContainer` conflicts with collapsible blocks | Scope `SelectionContainer` to content areas only, not toggle headers. |
 R2 | Sub-agent visual design unclear | Discovery task before implementation; HITL review of design proposal. |
 R3 | Live tracking file watching unreliable cross-platform | Polling fallback; manual cross-platform verification. |
 R4 | `AgentTaskFailedEvent` payload unknown | Tolerant `JsonElement?` fields; document as open question. |
 R5 | Search highlighting performance on large Messages | Lazy highlighting; only highlight visible content. |
 R6 | Scope creep across 7 feature areas | Each area has clear completion criteria; HITL review gates. |

# Open Questions

### Open Questions

 # | Question | Context |
---|---|---|
 Q1 | What exact UI should represent sub-agents? | Badges, nested messages, grouped blocks, or visual lanes? Discovery task will propose options. |
 Q2 | Should sub-agents be filterable separately? | Currently `SubAgent` maps to `FilterCategory.Tool`. Should it have its own filter toggle? |
 Q3 | Which top filter buttons are missing or redundant? | Audit task will produce a documented mapping. |
 Q4 | Should Search highlighting apply inside code/diff/terminal blocks, or only text/Markdown initially? | Performance and complexity trade-off. |
 Q5 | Should current match be highlighted differently from all matches? | Two-colour scheme (current match vs other matches) adds complexity. |
 Q6 | Should live tracking auto-scroll when new events arrive? | Only when user is already near the bottom, or always? |
 Q7 | Should live tracking use file watching, polling, or a hybrid? | `WatchService` reliability varies by platform. |
 Q8 | What is the exact payload shape of `AgentTaskFailedEvent`? | Not found in EVENT_CATALOG or codebase; use tolerant fields. |
 Q9 | Should partial text selection be supported everywhere, or only text-like content initially? | Code/diff/terminal selection may have Compose limitations. |

### Deferred / Out-of-Scope Items

 # | Item |
---|---|
 D1 | Export to Markdown/HTML |
 D2 | Database ingestion |
 D3 | Mobile UI |
 D4 | Full Markdown parser replacement |
 D5 | Advanced syntax highlighting (deferred from Sprint 3 as D4) |
 D6 | Multi-session comparison |
 D7 | Cloud/remote sessions |
 D8 | Full visual regression screenshot testing |
 D9 | Live editing/replaying of logs |

# Delivery Steps

### ✓ Step 1: Create sprint document
Sprint document exists at `docs/sprints/junie-conversation-viewer-sprint-4-interaction-live-tracking-and-event-coverage.md`.

- Create the sprint document following the exact structure of the Sprint 3 document (sections 1–23: Title, Related Documents, Sprint Goal, Current Baseline, Design Inspiration/Findings, Scope, Out of Scope, User Stories, Functional Requirements, Non-Functional Requirements, Design Principles, Proposed Visual System additions, Theme Architecture additions, Proposed changes per feature area, Accessibility, Cross-Platform, Testing Strategy, Incremental Delivery Plan with 9 parts and "After" outcomes, Risks, Open Questions, Definition of Done).
- Use the sprint goal, user stories, functional/non-functional requirements, scope, risks, and open questions from the Requirements and Technical Design tabs.
- Reference all related documents: Sprint 3 sprint/task docs, UBIQUITOUS-LANGUAGE.md, TESTING.md, RECAP.md, project_memory.md, EVENT_CATALOG.md, junie-jsonl-deserialization-investigation.md.
- Include 9 delivery parts: (1) Discovery and Scope Confirmation, (2) Text Selection and Partial Copy, (3) Sub-Agent and Event Representation, (4) Filter Coverage and Top Controls, (5) Search Highlighting, (6) Live Session Tracking, (7) AgentTaskFailedEvent Support, (8) Documentation and How-To Updates, (9) Testing, Review, and Completion.
- Each delivery part has Objective, Files, and "After" section with HITL-visible Reviewable Outcome.
- Use canonical terms from UBIQUITOUS-LANGUAGE.md throughout.

### ✓ Step 2: Create task breakdown document
Task breakdown document exists at `docs/tasks/junie-conversation-viewer-tasks-sprint-4-interaction-live-tracking-and-event-coverage.md`.

- Create the task document following the exact structure of the Sprint 3 task document (sections 1–11: Related Sprint, Related Documents, Purpose, How to Use, Progress Summary table, Task Status Legend, Implementation Task List with 9 areas, HITL Review Checkpoints table, Acceptance Criteria, Deferred/Out-of-Scope Items, Notes/Decisions Log).
- **Area 1 — Discovery and Scope Confirmation:** tasks to read docs, audit filter buttons vs MessageKind values, inspect copy/select behaviour, inspect Search implementation, inspect Session loading flow, inspect sub-agent and failure event models, gather AgentTaskFailedEvent examples, record open questions, HITL review.
- **Area 2 — Text Selection and Partial Copy:** tasks to add SelectionContainer to message content, preserve copy buttons, handle code/diff/terminal selection, test partial selection, manual clipboard verification.
- **Area 3 — Sub-Agent and Event Representation:** tasks to identify sub-agent event sources, propose visual representation, HITL design review, update MessageKind/mapping if needed, add UI markers/badges, add tests, HITL review.
- **Area 4 — Filter Coverage and Top Controls:** tasks to map all MessageKind values to filters, decide missing/redundant filters, update FilterBar and ViewModel, update tests, ensure labels match UBIQUITOUS-LANGUAGE.md, HITL review.
- **Area 5 — Search Highlighting:** tasks to add highlight tokens to ConversationColors, create highlight utility, highlight in plain text, highlight in Markdown/rich content, integrate with match navigation, distinguish current match, add tests, HITL review.
- **Area 6 — Live Session Tracking:** tasks to design file watching approach, implement FileWatcher interface, implement LiveSessionTracker, incremental parsing, update ViewModel state, scroll preservation/auto-scroll, handle partial writes/errors, add logging, add tests, cross-platform manual review, HITL review.
- **Area 7 — AgentTaskFailedEvent Support:** tasks to inspect payload, add event model, register serializer, map to Message, render as error block, add parser/repository/UI tests, verify unknown fallback intact, HITL review.
- **Area 8 — Documentation and How-To Updates:** tasks to create/update HOW_TO_USE.md or README, update TESTING.md, update RECAP.md, update project_memory.md, document deferred items.
- **Area 9 — Testing, Review, and Completion:** tasks to run `./gradlew :shared:jvmTest`, run `./gradlew test`, add/extend Robot tests, run manual checklist, run cyclomatic complexity check, fix review issues, HITL final approval.
- Every task includes: checkbox (unchecked), description, source, dependencies, likely files/areas, completion criteria, testing expectations.
- HITL review tasks include HITL-visible outcome.
- No tasks marked complete.
- Progress summary table shows 0/N for all areas.