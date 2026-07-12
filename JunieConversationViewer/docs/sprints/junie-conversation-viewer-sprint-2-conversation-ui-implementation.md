---
sprint: 2
name: Conversation UI Implementation
status: planned
---

# 1. Title

Sprint 2 — Conversation UI Implementation: Building the Dedicated Conversation UI

# 2. Related Documents

This implementation sprint is grounded in the following documents. Each is read-only input;
none is modified by this sprint.

- [`docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-design.md`](junie-conversation-viewer-sprint-2-conversation-ui-design.md)
  — **primary source of truth.** Defines the layout, rendering, navigation, accessibility,
  cross-platform, and testing design (Parts A–H) that this sprint turns into code.
- [`docs/tasks/conversation-ui-design-tasks.md`](../tasks/conversation-ui-design-tasks.md)
  — the reviewable task breakdown of the design sprint; used to ensure every design part maps to
  an implementation part and Reviewable Outcome.
- [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md)
  — canonical domain terms; all code, tests, and UI copy in this sprint use these terms.
- [`docs/RECAP.md`](../RECAP.md)
  — chronological project history; confirms the current baseline (Sprints 0–1) this sprint builds on.
- [`docs/TESTING.md`](../TESTING.md)
  — testing stack, Robot pattern, semantic `testTag` conventions, and Gradle commands used by
  the Testing Strategy (section 16).
- [`docs/project_memory.md`](../project_memory.md)
  — decisions, gotchas, and shipped work; keeps this sprint consistent with prior architecture.

# 3. Sprint Goal

Implement the dedicated cross-platform desktop Conversation UI designed in Sprint 2 (Conversation
UI Design), so a HITL can **read, scan, search, filter, and verify** an asymmetric Conversation —
short Human Messages and long, rich Junie Messages — on **macOS**, **Windows**, and **Linux**.

The result must be detailed enough for Junie to implement part by part and clear enough for the
HITL to review each part against a concrete Reviewable Outcome.

# 4. Background and Design Inputs

The Conversation Viewer already has a working baseline from Sprints 0–1:

- A Compose Multiplatform desktop app with an MVI `ConversationViewModel`
  (`ConversationState`, `ConversationAction`, `ConversationEvent`).
- `ConversationScreen` renders a `Scaffold` (top bar with title, `session_picker_button`,
  `settings_button`, `search_field`, `FilterBar`) and a `LazyColumn` (`message_list`) of `MessageItem`s.
- `MessageItem` already distinguishes `Sender.Human` from `Sender.Junie` and renders
  `MessageContent.Text`, `.Code` (via `components/CodeBlock.kt`), and `.Diff`.
- `SessionRepository` reads `events.jsonl`, parses Events, and maps them to `Message`s;
  `JsonlParser` handles the Junie log format.
- Real-time Search and Filtering by Sender and Message Kind, plus a Robot-pattern UI test harness.

This sprint refines that baseline into the explicitly-designed Conversation UI rather than
rewriting it. The design sprint (its Parts A–H and per-part "After" sections) is the source of
truth; the design task document confirms full traceability.

# 5. Ubiquitous Language

This sprint uses the canonical terms in
[`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md) consistently in code, tests, and UI copy:
Conversation, Session, Event, Message, Human, Junie, Turn, Response, Thought, Tool Call,
Terminal Output, Patch, Diff, Structured Output, Message Kind, Filter, Search Query, HITL,
Reviewable Outcome.

## Candidate Ubiquitous Language Additions

These terms may be introduced during implementation. They are **not** added to
`docs/UBIQUITOUS-LANGUAGE.md` in this sprint; each is listed as a follow-up task to be defined
there **before** it appears in shipped code or UI copy.

- **Match** — a single Message that satisfies the active Search Query (used for match navigation).
- **Match Cursor** — the index of the currently focused Match within the set of Matches.
- **Turn Header** — the grouping affordance that visually binds consecutive Junie Messages of one Turn.
- **Empty State / Loading State / Error State** — named non-happy-path Conversation view states.

# 6. Scope

Implementation planning (no code is written in *this* document; the parts below are implemented
in the later coding work) covers:

- Primary Conversation screen layout and asymmetric Human/Junie Message rendering.
- Readable long Junie Responses and Turn grouping.
- Rich content rendering: plain text, Markdown-like content, fenced code blocks, Patch/Diff,
  Terminal Output, Tool Call summaries, and a Structured Output fallback.
- Code block rendering with a copy affordance.
- Search Query UI and Message Kind Filters (preserving current behaviour).
- Session context/header area.
- Empty, loading, and error states, and a malformed-content fallback.
- Accessibility semantics (labels doubling as test tags, focus order, contrast, scalable text).
- Compose UI testability (semantic tags, Robot helpers, representative fixtures).
- Cross-platform desktop behaviour on macOS, Windows, and Linux.
- HITL visual review at defined checkpoints.

# 7. Out of Scope

- Real-time session tailing / streaming of an in-progress Session.
- Full Markdown parser replacement (only the agreed core subset is supported).
- Advanced/language-aware syntax highlighting beyond existing project dependencies.
- Complex virtualized navigation beyond what this sprint needs (basic lazy list is sufficient).
- Editing, annotating, or replaying Conversation logs.
- Cloud sync or remote Sessions; multi-Session comparison or cross-Session search.
- Export (Markdown/HTML) of Conversations.
- Mobile UI.
- Modifying `docs/UBIQUITOUS-LANGUAGE.md` (any additions are follow-up tasks only).

# 8. Assumptions

- The existing MVI architecture and components are the baseline; parts extend, not replace, them.
- Message Kinds not yet represented in `MessageContent` (Thought, Tool Call, Terminal, Structured
  Output) are rendered via planned extensions with a documented fallback: render as readable text.
- Timestamps may not be reliably present per Message (Q1 open); Session context is still shown.
- The design sprint's deferrals hold: screenshot/visual-regression testing and open questions
  Q1–Q5 remain deferred to be resolved during implementation.
- "Completion" of a part means observable app behaviour or a reviewable test/doc artefact exists.

# 9. User Stories

These carry the design sprint's stories into implementation and drive the delivery parts:

- As a **HITL**, I can open a Session and immediately distinguish Human Messages from Junie
  Responses, because their layout, alignment, and accent differ.
- As a **HITL**, I can read a long Junie Response comfortably, because it uses the full readable
  content width and never has short Human prompts dominating the screen.
- As a **HITL**, I can tell consecutive Junie Messages belong to one Turn, because they are
  visually grouped.
- As a **HITL**, I can see each Message's Message Kind (Text, Thought, Tool Call, Terminal Output,
  Patch) via a non-colour-only marker.
- As a **HITL**, I can verify a Patch/Diff and copy code, a Diff, or Terminal Output as clean text.
- As a **HITL**, I can type a Search Query, toggle Message Kind Filters, and understand when no
  Messages match.
- As a **HITL**, I can tell which Session is open and see clear loading, empty, and error states.
- As a **HITL** on macOS/Windows/Linux, I get native-feeling window sizing, scrolling, copy, and
  shortcuts, and I can operate the UI by keyboard.

# 10. Implementation Principles

1. **Extend, don't rewrite.** Build on the existing MVI `ConversationViewModel` and
   `ConversationScreen`/`MessageItem`; preserve current Search/Filter behaviour.
2. **Asymmetry-aware layout.** Human Messages stay compact and anchored; Junie content owns the
   readable width. No symmetric chat bubbles.
3. **Testability-first.** Every important element gets a stable `Modifier.testTag(...)` that also
   serves as its accessibility label, added *as* the UI is built, not after.
4. **Progressive disclosure.** Intermediate Junie output (Thoughts, Tool Calls) can be
   de-emphasised or collapsed without hiding content.
5. **Fallback over failure.** Any Message Kind or malformed content that has no dedicated renderer
   degrades to readable text; nothing becomes invisible or crashes the list.
6. **Theme tokens only.** Colours, typography, and spacing come from `MaterialTheme`; no hardcoded
   colours or `dp`/`sp` literals, so light/dark and scalable text work.
7. **Small, reviewable increments.** Each delivery part ends in an observable app behaviour or a
   reviewable test/doc artefact.

# 11. Proposed UI Implementation

**Entry points (existing).** `ConversationRoot` collects `ConversationState` from
`ConversationViewModel` and renders `ConversationScreen`. The screen is a `Scaffold`:

- **Top bar / chrome:** title, `session_picker_button` (opens `SessionSelector`),
  `settings_button` (opens `SettingsDialog`), `search_field`, and `FilterBar`. This persistent
  chrome keeps the Session selector, Search Query field, and Filter toggles visible at all times.
- **Body:** a `LazyColumn` tagged `message_list` rendering `MessageItem` per Message.

**Session context/header.** A slim, persistent header (in or just below the chrome) shows the
selected Session id and, when available, a timestamp/context line — visible without scrolling to
the top. When no Session is selected it shows the empty-state prompt.

**Asymmetric Message layout (`MessageItem`).**

- `Sender.Human`: compact, right-inset, `primaryContainer` accent, constrained max width so short
  prompts never span the full pane.
- `Sender.Junie`: left-inset, `secondaryContainer` accent, full readable content width for
  long-form reading.
- Each Message shows a **sender label** and a **Message Kind marker** (icon + text, not colour
  alone).

**Turn grouping.** Consecutive `Sender.Junie` Messages between two Human Messages form one Turn
and share a Turn container / Turn Header so the HITL can see where a Turn begins and ends.
Chronological Message order is preserved at all times, including while Filters are active.

**Scroll & long-form.** The `LazyColumn` provides vertical scrolling; long content wraps or scrolls
within its Message without breaking Message order or Turn grouping.

**Empty / loading / error states (see also Part 5).**

- **Loading:** a progress indicator (`loading_indicator`) while `ConversationState.isLoading`.
- **Empty:** distinct "no Session selected" and "Session has no Messages" states with guidance.
- **Error:** a recoverable error surface (`error_state` / existing `FatalErrorDialog` for fatal
  cases) with a retry affordance where applicable.

# 12. Rich Content Rendering Implementation Plan

Current `MessageContent` is `Text | Code | Diff`. This sprint keeps those and adds renderers for
Message Kinds not yet represented, each with a documented fallback to readable text. New rendering
lives in `components/` (e.g. new composables alongside `CodeBlock`), selected per `Message.kind`.

| Content | Status | Rendering approach |
|---|---|---|
| Plain text | Extend | Wrapped, selectable body typography via theme tokens. |
| Markdown-like | Add (core subset) | Headings, bold/italic, lists, inline code, links-as-text; complex tables deferred. |
| Fenced code blocks | Reuse | `components/CodeBlock.kt` (`dev.snipme.highlights`), horizontal scroll, **copy affordance**. |
| Patch / Diff | Extend | Unified-diff styling (added/removed emphasis) on `MessageContent.Diff`, copy affordance. |
| Terminal Output | Add | Monospace block, `$`-prefixed command line, preserved whitespace, copy affordance. |
| Tool Call | Add | Structured-Output (JSON-style) formatting; collapsible header with tool name. |
| Structured Output (general) | Add (partial) | JSON/code formatting now; rich tables/plans deferred, readable as text. |
| Errors / warnings | Add | Visually distinct accent **plus** icon/label; never blended silently into plain text. |
| Plans / summaries | Fallback | Rendered as Markdown for now; dedicated affordances deferred. |

Rules: every code/Diff/Terminal block exposes a reliable copy action (clean plain text, section
15); Thoughts are de-emphasised and collapsible; deferred items remain readable — nothing hidden.

# 13. Search, Filter, and Navigation Implementation Plan

**Search Query.** Preserve the existing `search_field` behaviour: case-insensitive substring match
over Message content, driven through `ConversationAction` into `ConversationState.searchQuery`,
producing `filteredMessages`.

**Filters.** Preserve `FilterBar`: Sender and Message Kind toggles, AND-combined with the Search
Query. Clearing Search/Filters restores the full Conversation with order preserved. Add a result
count where it aids orientation.

**No-results state.** When Search + Filters yield no Messages, show a distinct `no_results` state
explaining that no Messages match (not a blank list).

**Navigation & orientation.** Basic lazy-list scrolling. Match-to-match navigation (next/previous
Match, driven by a Match Cursor) is planned behind open question **Q3** — implemented only if the
HITL confirms it is in scope this sprint; otherwise Search + Filter is sufficient and match
navigation is deferred. Long-Turn orientation is preserved via the persistent Turn Header and
Session context.

**Keyboard/mouse.** Focus Search, clear Search, and (if in scope) next/previous Match are bound to
platform-appropriate shortcuts (section 15). Mouse/trackpad scrolling behaves natively.

# 14. Accessibility Implementation Plan

- **Semantic labels = test tags.** Every interactive control and important Message exposes a
  semantic label that doubles as its `testTag` (Search field, Filter toggles, session/settings
  buttons, Message container, Sender marker, Message Kind marker, match indicator).
- **Non-colour-only signals.** Sender, Message Kind, and error state always pair colour with a
  label, icon, or shape.
- **Focus order** follows reading/chronological order (top to bottom).
- **Full keyboard operability:** Search, Filters, and match navigation reachable and operable
  without a mouse.
- **Contrast** sufficient in both light and dark schemes; **scalable text** — layout tolerates
  larger font scales; text is selectable with a readable minimum size and line length.

# 15. Cross-Platform Desktop Considerations

- **Window sizing:** sensible default and minimum size; remember last size/position where the
  platform allows.
- **Modifiers/shortcuts:** Cmd on macOS, Ctrl on Windows/Linux for at least focus Search, clear
  Search, and next/previous Match (if in scope).
- **Fonts:** platform default UI and monospace fonts via theme tokens; verify legibility on all
  three OSes.
- **Scroll:** smooth vertical scrolling with trackpad, wheel, and scrollbar; respect platform
  scroll direction.
- **Clipboard/copy:** copying text, code, a Diff, or Terminal Output yields clean, unstyled plain
  text on every OS (Risk R3).
- **File-path display:** show Session and file paths in platform-native style; do not mangle `~`,
  drive letters, or separators.
- **Platform-neutral visual design:** one coherent visual language that does not look broken or
  alien on any OS.

# 16. Testing Strategy

Grounded in [`docs/TESTING.md`](../TESTING.md) (JUnit, Turbine, Compose `runComposeUiTest`, the
Robot pattern, and semantic `testTag`s). Unit tests live in `shared/src/commonTest/kotlin/...`;
UI tests use `runComposeUiTest` in the same tree.

- **Unit tests (ViewModel/logic):** Search Query filtering, Filter AND-combination, and any new
  navigation state (e.g. Match Cursor) covered with Turbine, preferring Fakes over Mocks.
- **Compose UI tests:** driven through `ConversationRobot`, extended with intent-level helpers —
  e.g. `selectSession(...)`, existing `typeSearchQuery(...)`, `toggleFilter(...)`,
  `goToNextMatch()`, `assertMessageOfKindVisible(...)` alongside existing `assertMessageCount` /
  `assertMessageVisible`.
- **Semantic tags:** add stable `Modifier.testTag(...)` to every important element (Message
  container, Sender marker, Message Kind marker, `search_field`, Filter toggles, `no_results`,
  `loading_indicator`, `error_state`, match indicator); tags double as accessibility labels.
- **Representative fixtures:** a fixture Session exercising every Message Kind (Human Text, Junie
  Text/Markdown, fenced code, Patch/Diff, Terminal Output, Tool Call, error) renders without
  crashing with correct Kind markers.
- **Search/Filter tests:** Search + Filter combine (AND); clearing restores the full Conversation;
  Message order preserved; `no_results` shown when nothing matches.
- **Long-response test:** automated smoke test for a very long Junie Turn plus a manual/visual
  check that scrolling and Turn grouping hold.
- **State tests:** loading, empty (no-Session and no-Messages), and recoverable error states where
  practical.
- **Commands:** `./gradlew test` (all tests) and `./gradlew :shared:jvmTest` (shared module).
- **Deferred:** screenshot/visual-regression testing remains deferred; covered by manual HITL
  visual review.

# 17. Delivery Plan

Eight numbered parts. Each is implemented in the later coding work; every part has a concrete,
HITL-verifiable "After". Parts map back to the design sprint Parts A–H.

## Part 1 — Establish UI implementation baseline (design Part A/B)

- **Objective:** confirm and stabilise the current Conversation screen as the baseline to build on.
- **Implementation tasks:** review `ConversationRoot`/`ConversationScreen`/`ConversationViewModel`
  responsibilities; confirm existing Search/Filter still works; inventory and plan semantic tags;
  align visible copy with the ubiquitous language.
- **Files/areas:** `ui/ConversationScreen.kt`, `ui/ConversationViewModel.kt`, `ui/ConversationState.kt`.
- **Testing:** existing Robot tests pass unchanged; add missing baseline tags.
- **HITL review:** confirm nothing regressed.
- **After:** *After running this part, the application should still launch on desktop and show the
  existing Conversation data with Search and Filter still working.*

## Part 2 — Implement asymmetric Human/Junie message layout (design Part B)

- **Objective:** deliver the designed asymmetric layout and Turn grouping.
- **Implementation tasks:** compact right-inset Human Messages with constrained width; full-width
  left-inset Junie Messages; sender labels; Turn container/Turn Header for consecutive Junie
  Messages; preserve chronological order.
- **Files/areas:** `ui/ConversationScreen.kt` (`MessageItem`), new Turn grouping composable.
- **Testing:** UI tests asserting Sender markers and Turn grouping; order-preservation test.
- **HITL review:** distinguish Human vs Junie at a glance; long Responses readable.
- **After:** *After running this part, the HITL should be able to open a Session and immediately
  distinguish Human Messages from Junie Responses, with long Junie Responses readable and Human
  prompts not dominating.*

## Part 3 — Implement rich content rendering foundations (design Part C)

- **Objective:** render each representative Junie output type per section 12.
- **Implementation tasks:** plain text; Markdown core subset; fenced code (reuse `CodeBlock`) with
  copy affordance; Patch/Diff styling; Terminal Output; Tool Call summary (collapsible); Structured
  Output fallback; distinct errors/warnings.
- **Files/areas:** `components/CodeBlock.kt`, new `components/` renderers, `domain/Message.kt`
  (extend `MessageContent`/kind mapping if needed), `data/SessionRepository.kt` mapping.
- **Testing:** representative-fixture rendering tests per Message Kind; copy-action test.
- **HITL review:** each content type visually identifiable; errors distinct.
- **After:** *After running this part, a representative Conversation containing plain text, code, a
  Diff, a Tool Call, Terminal Output, and Structured Output should render with each content type
  visually identifiable.*

## Part 4 — Implement search, filters, and conversation navigation (design Part D)

- **Objective:** refine Search Query UI and Message Kind Filters and add orientation aids.
- **Implementation tasks:** Search field placement; Filter chip layout; result count; `no_results`
  state; long-Turn orientation; match-to-match navigation only if Q3 is confirmed in scope.
- **Files/areas:** `ui/ConversationScreen.kt`, `components/FilterBar.kt`, `ui/ConversationViewModel.kt`.
- **Testing:** Search/Filter AND-combination, clear-restores, order-preserved, `no_results` tests;
  Match Cursor unit tests if implemented.
- **HITL review:** Search + Filters update the Conversation and no-match is clear.
- **After:** *After running this part, the HITL should be able to enter a Search Query, toggle
  Filters, see the Conversation update, and understand when no Messages match.*

## Part 5 — Implement session context and empty/loading/error states (design Part B/D)

- **Objective:** deliver the Session header and all non-happy-path states.
- **Implementation tasks:** current-Session indicator; no-Session-selected state; empty-Conversation
  state; loading state; recoverable error state; malformed-content fallback.
- **Files/areas:** `ui/ConversationScreen.kt`, `ui/ConversationState.kt`, `components/FatalErrorDialog.kt`.
- **Testing:** state tests for loading/empty/error where practical.
- **HITL review:** the open Session is identifiable and each state is clear.
- **After:** *After running this part, the HITL should be able to tell which Session is open and
  see clear states for loading, empty, and error conditions.*

## Part 6 — Implement accessibility and cross-platform polish (design Parts E/F)

- **Objective:** deliver accessibility semantics and desktop behaviour on all three OSes.
- **Implementation tasks:** keyboard focus order; semantic labels; contrast in light/dark; scalable
  text; platform-appropriate shortcuts; clean-plain-text copy; font/scroll checks.
- **Files/areas:** `ui/ConversationScreen.kt`, theme tokens, shortcut wiring.
- **Testing:** keyboard-operability UI tests; a documented manual cross-platform checklist.
- **HITL review:** keyboard navigable, semantic labels present, colour never the sole signal.
- **After:** *After running this part, the UI should be navigable with keyboard focus, use semantic
  labels for important controls, and have a documented manual review checklist for macOS, Windows,
  and Linux.*

## Part 7 — Implement and update tests (design Part G)

- **Objective:** cover the core Conversation UI behaviour automatically plus a manual checklist.
- **Implementation tasks:** Compose UI tests; `ConversationRobot` helper updates; semantic tags;
  representative Message-Kind fixtures; Search/Filter tests; long-Response test; empty/error tests;
  manual HITL visual-review checklist.
- **Files/areas:** `shared/src/commonTest/kotlin/...`, test fixtures, `docs/TESTING.md` (helpers).
- **Testing:** `./gradlew test` and `./gradlew :shared:jvmTest` green.
- **HITL review:** confirm coverage and checklist adequacy.
- **After:** *After running this part, the automated test suite should cover the core Conversation
  UI behaviour and the HITL should have a checklist for visual review of rich content and
  cross-platform behaviour.*

## Part 8 — Final HITL review and documentation update (design Part H)

- **Objective:** close out the sprint with review and doc updates.
- **Implementation tasks:** update `README.md` (via `readme-updater`) and `docs/project_memory.md`
  (via `project-memory`) if behaviour changed; record deferred items; confirm Definition of Done;
  obtain HITL approval; run the end-of-sprint cyclomatic-complexity check per project guidelines.
- **Files/areas:** `README.md`, `docs/project_memory.md`, `docs/RECAP.md`.
- **Testing:** full suite green; complexity check reviewed.
- **HITL review:** final sign-off.
- **After:** *After completing this part, the HITL should be able to run the application, inspect
  representative Conversations, confirm the sprint outcomes, and see any deferred items documented.*

# 18. HITL Review Plan

The HITL reviews at these checkpoints; each names what to verify:

- **After Part 2 (layout):** Human vs Junie Messages are distinct, Turns are grouped, long
  Responses are readable, Human prompts do not dominate.
- **After Part 3 (rich rendering):** every Message Kind is visually identifiable; errors/warnings
  are distinct; copy actions produce clean text; deferred content is still readable.
- **After Part 4 (search/filter/navigation):** Search + Filters behave as designed and `no_results`
  is clear; orientation holds in long Turns.
- **After Part 6 (accessibility & cross-platform):** keyboard operability, semantic labels, and
  colour-plus-icon signals confirmed; manual checklist run on macOS/Windows/Linux.
- **Before completion (Part 8):** Definition of Done satisfied, deferred items recorded, docs
  updated; final approval granted.

# 19. Risks and Mitigations

- **R1 — Turn grouping ambiguity:** Events may not cleanly delimit Turns. *Mitigation:* explicit
  grouping heuristic with a documented ADR; fall back to per-Message rendering if grouping is
  uncertain.
- **R2 — Long-Turn performance:** very long Turns may stress the lazy list. *Mitigation:* keep
  `LazyColumn` keys stable; smoke-test a large fixture; avoid unnecessary recomposition.
- **R3 — Cross-platform copy fidelity:** styled content may copy as rich text. *Mitigation:* copy
  raw plain text explicitly and test copy output.
- **R4 — Markdown scope creep:** *Mitigation:* hold the agreed core subset; defer tables/advanced
  syntax and keep them readable as text.
- **R5 — Scope size:** rendering fidelity may exceed one sprint. *Mitigation:* fallback-to-text for
  every unfinished renderer; parts are independently shippable.
- **R6 — Terminology drift:** *Mitigation:* cross-check all copy/code against
  `docs/UBIQUITOUS-LANGUAGE.md`; add candidate terms there before use.

# 20. Open Questions

Carried forward from the design sprint (Q1–Q5) and left open for this implementation sprint by
explicit HITL decision:

- **Q1:** Are Timestamps reliably present per Message, and should they show inline, on hover, or
  only per Turn?
- **Q2:** Should Thoughts and Tool Calls collapse by default, or expand with a collapse option?
- **Q3:** Is match-to-match navigation (next/previous Match) in scope this sprint (Part 4), or is
  Search + Filter sufficient?
- **Q4:** Is a Turn-level outline / jump-to-Turn navigator in scope now, or deferred?
- **Q5:** Is current syntax highlighting sufficient, or is language-aware highlighting expected?

# 21. Definition of Done

This sprint's planning document is done when all below hold:

- Sprint document exists at `docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md`.
- Implementation scope is mapped back to the design sprint and task document.
- Every delivery part (1–8) has a concrete, reviewable "After" section.
- UI implementation plan covers Human and Junie Message rendering.
- UI implementation plan covers rich Junie output types (text, Markdown, code, Patch/Diff, Terminal
  Output, Tool Call, Structured Output fallback, errors/warnings).
- Search/Filter behaviour is planned and testable.
- Accessibility and cross-platform review are planned.
- Testing strategy references `docs/TESTING.md` and names the Gradle commands.
- HITL review checkpoints are included.
- Deferred/out-of-scope items are explicit.
- Open questions (Q1–Q5) are documented.
- No application code was implemented in this planning task.
