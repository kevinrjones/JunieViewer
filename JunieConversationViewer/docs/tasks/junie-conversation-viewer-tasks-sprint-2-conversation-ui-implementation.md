# Sprint 2 — Conversation UI Implementation: Task Breakdown

## 1. Related Sprint

**Sprint document:** [`docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md`](../sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md)

**Sprint goal:** Implement the dedicated cross-platform desktop Conversation UI designed in Sprint 2 (Conversation UI Design), so a HITL can read, scan, search, filter, and verify an asymmetric Conversation — short Human Messages and long, rich Junie Messages — on macOS, Windows, and Linux. The implementation builds on the existing Sprint 0–1 baseline (MVI architecture, `ConversationViewModel`, `ConversationScreen`, Robot-pattern test harness) and extends it part by part into the explicitly-designed Conversation UI.

**Traceability:** This implementation sprint traces back to:

- [`docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-design.md`](../sprints/junie-conversation-viewer-sprint-2-conversation-ui-design.md) — the design sprint (Parts A–H) that defines layout, rendering, navigation, accessibility, cross-platform, and testing design.
- [`docs/tasks/conversation-ui-design-tasks.md`](conversation-ui-design-tasks.md) — the reviewable task breakdown of the design sprint, ensuring every design part maps to an implementation part and Reviewable Outcome.

## 2. Related Documents

| Document | Role |
|---|---|
| [`docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md`](../sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md) | **Primary source of truth.** Defines the 8 delivery parts with concrete "After" sections that this task document breaks into trackable tasks. |
| [`docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-design.md`](../sprints/junie-conversation-viewer-sprint-2-conversation-ui-design.md) | Design sprint (Parts A–H) providing the layout, rendering, navigation, accessibility, and testing design that the implementation sprint turns into code. |
| [`docs/tasks/conversation-ui-design-tasks.md`](conversation-ui-design-tasks.md) | Design task breakdown ensuring traceability from design parts to implementation parts. |
| [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md) | Canonical domain terms used consistently in code, tests, UI copy, and documentation throughout this sprint. |
| [`docs/RECAP.md`](../RECAP.md) | Chronological project history confirming the current baseline (Sprints 0–1) this sprint builds on. |
| [`docs/TESTING.md`](../TESTING.md) | Testing stack (JUnit, Turbine, Compose `runComposeUiTest`, Robot pattern), semantic `testTag` conventions, and Gradle commands. |
| [`docs/project_memory.md`](../project_memory.md) | Architecture decisions, gotchas, and shipped work keeping this sprint consistent with prior decisions. |

## 3. Purpose

This document breaks the Sprint 2 Conversation UI Implementation sprint into concrete, trackable tasks. It serves as:

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

| # | Task Area | Status | Task Count |
|---|---|---|---|
| 1 | Sprint Alignment and Traceability | Complete | 10 |
| 2 | UI Implementation Baseline | Complete | 8 |
| 3 | Asymmetric Human/Junie Conversation Layout | In progress | 18 |
| 3.5 | Deserialization Hardening (Option B + Option A) | In progress | 10 |
| 4 | Rich Content Rendering | Not started | 13 |
| 5 | Search, Filters, and Navigation | Not started | 12 |
| 6 | Session Context, Empty, Loading, and Error States | Not started | 8 |
| 7 | Accessibility and Cross-Platform Desktop Polish | Not started | 13 |
| 8 | Automated Testing | Not started | 13 |
| 9 | HITL Review and Documentation | Not started | 12 |
| 10 | Final Sprint Completion | Not started | 7 |
| | **Total** | | **124** |

## 6. Task Status Legend

- `- [ ]` — Task not started or not complete.
- `- [x]` — Task complete and reviewed where review is required.

**Inline markers:**

- **`HITL Review`** — Task requires HITL visual or functional review before it can be marked complete.
- **`Blocked`** — Task is blocked by an external dependency or unresolved question.
- **`Deferred`** — Task has been explicitly moved out of this sprint's scope.
- **`Depends on [task]`** — Task depends on another task being completed first.
- **`Test Required`** — Task must have automated test coverage before completion.
- **`Manual Review Required`** — Task requires manual verification (e.g., cross-platform visual check).

---

## 7. Implementation Task List

### Area 1 — Sprint Alignment and Traceability

*Source: Delivery Part 1 (design Parts A/B). Ensures the implementation is grounded in the sprint and design documents before any code changes.*

#### 1.1 Read the implementation sprint document

- [x] Read the implementation sprint document

**Description:** Read `docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md` end-to-end to understand the sprint goal, delivery parts 1–8, and Definition of Done.

**Source:** Implementation sprint, sections 1–21.

**Dependencies:** None.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- Implementation sprint document has been read and understood.
- All 8 delivery parts and their "After" sections are identified.

**Testing expectations:** No automated tests required.

#### 1.2 Read the design sprint document

- [x] Read the design sprint document

**Description:** Read `docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-design.md` to understand the design decisions (Parts A–H) that the implementation sprint turns into code.

**Source:** Design sprint document.

**Dependencies:** None.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- Design sprint document has been read.
- Design Parts A–H are understood and can be traced to implementation delivery parts.

**Testing expectations:** No automated tests required.

#### 1.3 Read the design task document

- [x] Read the design task document

**Description:** Read `docs/tasks/conversation-ui-design-tasks.md` to confirm design task completion status and traceability.

**Source:** Design task document.

**Dependencies:** None.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- Design task document has been reviewed.
- Any incomplete design tasks that affect implementation are noted.

**Testing expectations:** No automated tests required.

#### 1.4 Read UBIQUITOUS-LANGUAGE.md

- [x] Read UBIQUITOUS-LANGUAGE.md

**Description:** Read `docs/UBIQUITOUS-LANGUAGE.md` to confirm canonical domain terms. All code, tests, and UI copy in this sprint must use these terms consistently.

**Source:** Implementation sprint, section 5.

**Dependencies:** None.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- Ubiquitous language terms are understood and will be applied consistently.
- Candidate additions (Match, Match Cursor, Turn Header, Empty/Loading/Error State) are noted for follow-up.

**Testing expectations:** No automated tests required.

#### 1.5 Read RECAP.md, TESTING.md, and project_memory.md

- [x] Read supporting project documents

**Description:** Read `docs/RECAP.md` (project history), `docs/TESTING.md` (testing stack and patterns), and `docs/project_memory.md` (architecture decisions and gotchas) to understand the current baseline.

**Source:** Implementation sprint, section 2.

**Dependencies:** None.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- Current baseline (Sprints 0–1) is understood.
- Testing stack (JUnit, Turbine, Robot pattern, `runComposeUiTest`) is understood.
- Known gotchas (e.g., `StateFlow` intermediate emissions, `compose.desktop.currentOs` test dependency) are noted.

**Testing expectations:** No automated tests required.

#### 1.6 Confirm implementation scope and out-of-scope items

- [x] Confirm implementation scope and out-of-scope items

**Description:** Cross-check the implementation sprint's Scope (section 6) and Out of Scope (section 7) against this task document to ensure nothing is missed or accidentally included.

**Source:** Implementation sprint, sections 6–7.

**Dependencies:** Tasks 1.1–1.5.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- Every in-scope item has corresponding tasks in this document.
- Every out-of-scope item is listed in the Deferred / Out-of-Scope section.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** The HITL can compare this task document with the sprint scope and confirm coverage.

#### 1.7 Map every delivery part to tasks

- [x] Map every delivery part to tasks

**Description:** Verify that each of the 8 delivery parts (Parts 1–8) in the implementation sprint has corresponding tasks in this document's task areas.

**Source:** Implementation sprint, section 17 (Delivery Plan).

**Dependencies:** Tasks 1.1–1.6.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- A mapping exists from each delivery part to one or more task areas.
- No delivery part is unrepresented.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** The HITL can trace each delivery part to specific tasks.

#### 1.8 Map every "After" section to a HITL-visible task outcome

- [x] Map every "After" section to a HITL-visible task outcome

**Description:** Verify that each delivery part's "After" section maps to at least one task with a HITL-visible outcome, so the HITL can verify each part's completion.

**Source:** Implementation sprint, section 17 (each Part's "After" subsection).

**Dependencies:** Task 1.7.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- Every "After" section has a corresponding HITL-visible outcome in the task list.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** The HITL can verify that every "After" outcome is reviewable.

#### 1.9 Record assumptions and open questions

- [x] Record assumptions and open questions

**Description:** Document any assumptions made during task creation and carry forward open questions Q1–Q5 from the sprint document.

**Source:** Implementation sprint, sections 8 (Assumptions) and 20 (Open Questions).

**Dependencies:** Tasks 1.1–1.8.

**Likely files / areas:** This task document (Notes / Decisions Log section).

**Completion criteria:**
- Assumptions are recorded in the Notes / Decisions Log.
- Open questions Q1–Q5 are documented in the Deferred / Out-of-Scope section.

**Testing expectations:** No automated tests required.

#### 1.10 Confirm task document structure matches issue requirements

- [x] Confirm task document structure matches issue requirements

**Description:** Verify that this task document includes all 12 required sections and meets the acceptance criteria specified in the original issue.

**Source:** Original issue requirements.

**Dependencies:** Tasks 1.1–1.9.

**Likely files / areas:** This task document.

**Completion criteria:**
- All 12 sections are present and populated.
- Acceptance criteria checklist can be satisfied.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** The HITL can review the document structure against the issue requirements.

---

### Area 2 — UI Implementation Baseline

*Source: Delivery Part 1 (design Parts A/B). Confirms and stabilises the current Conversation screen as the baseline before making changes.*

#### 2.1 Review ConversationRoot and ConversationScreen responsibilities

- [x] Review ConversationRoot and ConversationScreen responsibilities

**Description:** Review `ConversationRoot` (state collection from ViewModel) and `ConversationScreen` (Scaffold with top bar, search, filters, message list) to understand the current screen structure and identify extension points.

**Source:** Implementation sprint, Part 1; section 11.

**Dependencies:** Area 1 complete.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- Current `ConversationRoot` → `ConversationScreen` → `MessageItem` hierarchy is documented or understood.
- Extension points for layout changes (Part 2) and new renderers (Part 3) are identified.

**Testing expectations:** No automated tests required for this review task.

#### 2.2 Review ConversationViewModel and ConversationState

- [x] Review ConversationViewModel and ConversationState

**Description:** Review `ConversationViewModel` (MVI actions, state management, search/filter logic) and `ConversationState` (current state shape) to understand what state changes are needed for new features.

**Source:** Implementation sprint, Part 1; section 11.

**Dependencies:** Area 1 complete.

**Likely files / areas:** `ui/ConversationViewModel.kt`, `ui/ConversationState.kt`.

**Completion criteria:**
- Current state shape and action handling are understood.
- Needed state extensions (e.g., loading/error states, match cursor) are identified.

**Testing expectations:** No automated tests required for this review task.

#### 2.3 Verify existing launch behaviour is preserved

- [x] Verify existing launch behaviour is preserved — `Test Required`

**Description:** Confirm the application still launches on desktop and shows the existing Conversation data after any baseline changes.

**Source:** Implementation sprint, Part 1 "After" section.

**Dependencies:** Tasks 2.1–2.2.

**Likely files / areas:** `ui/ConversationScreen.kt`, `ui/ConversationViewModel.kt`.

**Completion criteria:**
- Application launches without errors.
- Existing Conversation data is displayed.

**Testing expectations:** Existing tests pass unchanged (`./gradlew :shared:jvmTest`).

**HITL-visible outcome:** The application launches and displays Conversation data as before.

#### 2.4 Verify existing search behaviour is preserved

- [x] Verify existing search behaviour is preserved — `Test Required`

**Description:** Confirm that the existing `search_field` and text search functionality still works correctly.

**Source:** Implementation sprint, Part 1; section 13.

**Dependencies:** Task 2.3.

**Likely files / areas:** `ui/ConversationScreen.kt`, `ui/ConversationViewModel.kt`.

**Completion criteria:**
- Search field accepts input and filters messages by text content.
- Clearing search restores the full message list.

**Testing expectations:** Existing Robot-pattern search tests pass (`ConversationScreenTest`).

#### 2.5 Verify existing filter behaviour is preserved

- [x] Verify existing filter behaviour is preserved — `Test Required`

**Description:** Confirm that the existing `FilterBar` (Sender and Message Kind toggles) still works correctly.

**Source:** Implementation sprint, Part 1; section 13.

**Dependencies:** Task 2.3.

**Likely files / areas:** `ui/ConversationScreen.kt`, `components/FilterBar.kt`, `ui/ConversationViewModel.kt`.

**Completion criteria:**
- Filter toggles filter messages by sender and kind.
- Filters combine with search (AND logic).

**Testing expectations:** Existing Robot-pattern filter tests pass.

#### 2.6 Identify semantic tags needed for new or changed UI

- [x] Identify semantic tags needed for new or changed UI

**Description:** Inventory existing `testTag` values and plan new tags needed for the implementation (e.g., sender marker, message kind marker, turn header, no_results, loading_indicator, error_state, match indicator).

**Source:** Implementation sprint, section 14; `docs/TESTING.md`.

**Dependencies:** Tasks 2.1–2.2.

**Likely files / areas:** `ui/ConversationScreen.kt`, test files.

**Completion criteria:**
- List of existing tags is documented.
- List of new tags needed is documented.

**Testing expectations:** No automated tests required for this planning task.

#### 2.7 Establish representative fixture data for implementation and testing

- [x] Establish representative fixture data — `Test Required`

**Description:** Create or extend test fixture data that exercises every Message Kind (Human Text, Junie Text/Markdown, fenced code, Patch/Diff, Terminal Output, Tool Call, error) for use in implementation and testing.

**Source:** Implementation sprint, section 16 (Representative fixtures).

**Dependencies:** Tasks 2.1–2.2.

**Likely files / areas:** `shared/src/commonTest/kotlin/...` (test fixtures), `domain/Message.kt`.

**Completion criteria:**
- Fixture data covers every Message Kind listed in the sprint.
- Fixtures can be used in both unit and UI tests.

**Testing expectations:** Fixture data renders without crashing in existing test infrastructure.

#### 2.8 Run baseline test suite before UI changes

- [x] Run baseline test suite — `Test Required`

**Description:** Run `./gradlew :shared:jvmTest` to confirm all existing tests pass before making any UI changes. Record the result.

**Source:** Implementation sprint, Part 1; `docs/TESTING.md`.

**Dependencies:** Tasks 2.3–2.5.

**Likely files / areas:** Test output.

**Completion criteria:**
- All existing tests pass.
- Test result is recorded in the Notes / Decisions Log.

**Testing expectations:** `./gradlew :shared:jvmTest` passes with zero failures.

**HITL-visible outcome:** Baseline test results confirm nothing has regressed.

---

### Area 3 — Asymmetric Human/Junie Conversation Layout

*Source: Delivery Part 2 (design Part B). Implements the designed asymmetric layout and Turn grouping.*

#### 3.1 Implement compact right-inset Human messages

- [x] Implement compact right-inset Human messages — `Test Required`

**Description:** Modify `MessageItem` so `Sender.Human` messages are compact, right-inset, with `primaryContainer` accent and constrained max width so short prompts never span the full pane.

**Source:** Implementation sprint, Part 2; section 11 (Asymmetric Message layout).

**Dependencies:** Area 2 complete.

**Likely files / areas:** `ui/ConversationScreen.kt` (`MessageItem`).

**Completion criteria:**
- Human messages are visually compact and right-aligned.
- Max width is constrained (short prompts do not span full width).
- `primaryContainer` accent is applied via theme tokens.

**Testing expectations:** UI test asserting Human message positioning and styling.

#### 3.2 Implement full-width left-inset Junie messages

- [x] Implement full-width left-inset Junie messages — `Test Required`

**Description:** Modify `MessageItem` so `Sender.Junie` messages are left-inset with `secondaryContainer` accent and full readable content width for long-form reading.

**Source:** Implementation sprint, Part 2; section 11.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `ui/ConversationScreen.kt` (`MessageItem`).

**Completion criteria:**
- Junie messages use full readable content width.
- `secondaryContainer` accent is applied via theme tokens.
- Long Junie responses are readable without horizontal scrolling.

**Testing expectations:** UI test asserting Junie message positioning and styling.

#### 3.3 Implement sender labels

- [x] Implement sender labels — `Test Required`

**Description:** Add sender labels ("Human" / "Junie") to each message, using ubiquitous language terms.

**Source:** Implementation sprint, Part 2; section 11.

**Dependencies:** Tasks 3.1–3.2.

**Likely files / areas:** `ui/ConversationScreen.kt` (`MessageItem`).

**Completion criteria:**
- Each message displays a sender label.
- Labels use canonical terms from `UBIQUITOUS-LANGUAGE.md`.

**Testing expectations:** UI test asserting sender labels are visible with correct text.

#### 3.4 Implement Message Kind markers

- [x] Implement Message Kind markers — `Test Required`

**Description:** Add a non-colour-only Message Kind marker (icon + text label) to each message so the HITL can identify the content type.

**Source:** Implementation sprint, Part 2; section 11; section 14 (non-colour-only signals).

**Dependencies:** Tasks 3.1–3.2.

**Likely files / areas:** `ui/ConversationScreen.kt` (`MessageItem`).

**Completion criteria:**
- Each message displays a Message Kind marker.
- Markers use icon + text (not colour alone).

**Testing expectations:** UI test asserting Message Kind markers are visible.

#### 3.5 Define and implement message spacing

- [x] Define and implement message spacing

**Description:** Define consistent vertical spacing between messages using theme tokens (no hardcoded `dp` values).

**Source:** Implementation sprint, Part 2; section 10 (principle 6 — theme tokens only).

**Dependencies:** Tasks 3.1–3.2.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- Consistent spacing between messages.
- Spacing uses theme dimensions, not hardcoded values.

**Testing expectations:** Visual verification; no specific automated test required.

#### 3.6 Implement Turn container and Turn Header for consecutive Junie messages

- [x] Implement Turn container and Turn Header — `Test Required`

**Description:** Group consecutive `Sender.Junie` messages between two Human messages into a Turn container with a Turn Header, so the HITL can see where a Turn begins and ends.

**Source:** Implementation sprint, Part 2; section 11 (Turn grouping).

**Dependencies:** Tasks 3.1–3.2.

**Likely files / areas:** `ui/ConversationScreen.kt`, new Turn grouping composable.

**Completion criteria:**
- Consecutive Junie messages are visually grouped.
- A Turn Header marks the beginning of each Turn.
- Turn grouping does not break message order.

**Testing expectations:** UI test asserting Turn grouping with multiple Junie messages.

#### 3.7 Preserve chronological message order

- [x] Preserve chronological message order — `Test Required`

**Description:** Ensure messages maintain chronological order at all times, including while Filters are active.

**Source:** Implementation sprint, Part 2; section 11.

**Dependencies:** Tasks 3.1–3.6.

**Likely files / areas:** `ui/ConversationViewModel.kt`, `ui/ConversationScreen.kt`.

**Completion criteria:**
- Messages are always displayed in chronological order.
- Filtering does not reorder messages.

**Testing expectations:** UI test asserting message order with and without filters.

#### 3.8 Implement scroll behaviour for long conversations

- [x] Implement scroll behaviour for long conversations

**Description:** Ensure the `LazyColumn` provides smooth vertical scrolling; long content wraps or scrolls within its message without breaking message order or Turn grouping.

**Source:** Implementation sprint, Part 2; section 11.

**Dependencies:** Tasks 3.1–3.6.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- Vertical scrolling works smoothly with trackpad, wheel, and scrollbar.
- Long message content does not break layout.

**Testing expectations:** Manual verification of scroll behaviour; automated smoke test for long Turn.

#### 3.9 Ensure Human messages do not visually dominate

- [x] Ensure Human messages do not visually dominate — `Manual Review Required`

**Description:** Verify that the compact Human message styling ensures short Human prompts do not dominate the screen when interleaved with long Junie responses.

**Source:** Implementation sprint, Part 2 "After" section.

**Dependencies:** Tasks 3.1–3.8.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- Human messages are visually subordinate to Junie responses.
- The layout reads naturally with mixed short/long content.

**Testing expectations:** Manual visual review.

#### 3.10 HITL visual review of asymmetric layout

- [ ] HITL visual review of asymmetric layout — `HITL Review`

**Description:** HITL reviews the implemented layout to confirm Human vs Junie messages are distinct, Turns are grouped, long Responses are readable, and Human prompts do not dominate.

**Source:** Implementation sprint, Part 2 "After" section; HITL Review Plan (After Part 2).

**Dependencies:** Tasks 3.1–3.9.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms the layout meets the design intent.
- Any feedback is captured in the Notes / Decisions Log.

**Testing expectations:** No automated tests; HITL visual review.

**HITL-visible outcome:** The HITL can open a Session and immediately distinguish Human Messages from Junie Responses, with long Junie Responses readable and Human prompts not dominating.

#### 3.11 Stabilize Conversation loading for real Junie session data

*Source: `desktopApp/logs/viewer.log` crash report (2026-07-12). Two crash causes found after Area 3.5 hardening.*

##### 3.11.1 Inspect latest viewer.log crash exceptions

- [x] Inspect latest `desktopApp/logs/viewer.log` crash exceptions

**Description:** Read the latest crash log to identify all exceptions. Two root causes found: (1) `NextPromptSuggestionEvent.suggestion` typed as `String?` but real data is `JsonArray` — 11 parse failures; (2) `CodeTextView` (kodeview library) uses internal `verticalScroll` which crashes with infinite height constraints when hosted inside `LazyColumn`.

**Source:** `desktopApp/logs/viewer.log`, 2026-07-12.

**Completion criteria:** All crash exceptions identified and root causes documented.

**Testing expectations:** No automated tests required for inspection.

##### 3.11.2 Identify crash path

- [x] Identify whether the crash is caused by parsing, mapping, repository loading, ViewModel state handling, or UI rendering

**Description:** Crash 1 (parse error) occurs in `JsonlParser.parseLine()` → `NextPromptSuggestionEvent$$serializer.deserialize()` — a known event class with wrong field type. Crash 2 (fatal) occurs in Compose layout measurement — `CodeTextView` inside `LazyColumn` item triggers `IllegalStateException: Vertically scrollable component was measured with an infinity maximum height constraints`.

**Completion criteria:** Crash paths documented with file/class/function.

**Testing expectations:** No automated tests required for identification.

##### 3.11.3 Add tests reproducing the latest crash

- [x] Add tests reproducing the latest crash

**Description:** Add a parser test for `NextPromptSuggestionEvent` with real-world `JsonArray` suggestion format to confirm it no longer fails.

**Source:** `desktopApp/logs/viewer.log` — `NextPromptSuggestionEvent` parse failure.

**Likely files / areas:** `shared/src/commonTest/kotlin/.../data/JsonlParserTest.kt`.

**Completion criteria:** Test parses the real-world JSON format successfully.

**Testing expectations:** `./gradlew :shared:jvmTest` passes. `Test Required`

##### 3.11.4 Fix NextPromptSuggestionEvent field type mismatch

- [x] Fix NextPromptSuggestionEvent field type mismatch

**Description:** Change `NextPromptSuggestionEvent.suggestion` from `String?` to `JsonElement?` to match the real data format (a `JsonArray` of objects, not a primitive string).

**Source:** `desktopApp/logs/viewer.log` — `Expected JsonPrimitive, but had JsonArray`.

**Likely files / areas:** `domain/JunieEvent.kt`.

**Completion criteria:** `NextPromptSuggestionEvent` with array suggestion parses without error.

**Testing expectations:** Parser test confirms successful parsing. `Test Required`

##### 3.11.5 Fix CodeBlock infinite height crash inside LazyColumn

- [x] Fix CodeBlock infinite height crash inside LazyColumn

**Description:** Add `heightIn(max = 600.dp)` to `CodeTextView` modifier in `CodeBlock.kt` to prevent infinite-height measurement when the component (which uses internal `verticalScroll`) is hosted inside a `LazyColumn` item.

**Source:** `desktopApp/logs/viewer.log` — `IllegalStateException: Vertically scrollable component was measured with an infinity maximum height constraints`.

**Likely files / areas:** `ui/components/CodeBlock.kt`.

**Completion criteria:** Application does not crash when rendering code blocks in the conversation list.

**Testing expectations:** Manual verification by running the app. Compose layout crash cannot be reproduced in headless unit tests.

##### 3.11.6 Verify supported conversation content still renders

- [x] Verify supported conversation content still renders

**Description:** Run the test suite to confirm existing parsing, mapping, and UI tests still pass after the fixes.

**Completion criteria:** `./gradlew :shared:jvmTest` — BUILD SUCCESSFUL, 0 failures.

**Testing expectations:** `./gradlew :shared:jvmTest` passes. `Test Required`

##### 3.11.7 Run the relevant test suite

- [x] Run the relevant test suite

**Description:** Run `./gradlew :shared:jvmTest` after all fixes.

**Completion criteria:** All tests pass.

**Testing expectations:** BUILD SUCCESSFUL.

##### 3.11.8 Record remaining deferred deserialization or rendering work

- [x] Record remaining deferred deserialization or rendering work

**Description:** Document that other event classes may have similar field type mismatches if Junie's real data format differs from the assumed schema. The `JsonElement?` approach used for `NextPromptSuggestionEvent.suggestion` should be applied to any field whose real format is uncertain.

**Completion criteria:** Note added to Notes / Decisions Log.

**Testing expectations:** No automated tests required.

---

### Area 3.5 — Deserialization Hardening (Option B + Option A)

*Source: Delivery Part 2.5. Ensures the JSONL parser handles unknown event kinds gracefully and surfaces them in the UI, then adds proper classes for all discovered event kinds. See `docs/junie-jsonl-deserialization-investigation.md`.*

#### 3.5.1 Add UnknownJunieEvent and UnknownAgentEvent fallback classes

- [x] Add UnknownJunieEvent and UnknownAgentEvent fallback classes

**Description:** Add `UnknownJunieEvent(kind: String, timestampMs: Long, raw: JsonObject)` to the `JunieEvent` sealed hierarchy and `UnknownAgentEvent(kind: String, raw: JsonObject)` to the `AgentEvent` sealed hierarchy. These classes preserve the raw JSON so no data is lost.

**Source:** Implementation sprint, Part 2.5 (Phase B); `docs/junie-jsonl-deserialization-investigation.md`, Option B.

**Dependencies:** None.

**Likely files / areas:** `domain/JunieEvent.kt`.

**Completion criteria:**
- `UnknownJunieEvent` and `UnknownAgentEvent` exist in the sealed hierarchies.
- Both preserve the original `kind` string and raw `JsonObject`.

**Testing expectations:** Unit tests confirming construction and field access. `Test Required`

#### 3.5.2 Implement custom JsonContentPolymorphicSerializer for JunieEvent and AgentEvent

- [x] Implement custom JsonContentPolymorphicSerializer for JunieEvent and AgentEvent

**Description:** Replace the default sealed-class serializer with a custom `JsonContentPolymorphicSerializer` for both `JunieEvent` and `AgentEvent`. The serializer checks the `kind` discriminator field and delegates to the matching registered subtype serializer, or falls back to `UnknownJunieEvent` / `UnknownAgentEvent` when no match is found.

**Source:** Implementation sprint, Part 2.5 (Phase B).

**Dependencies:** Task 3.5.1.

**Likely files / areas:** `domain/JunieEvent.kt` or new serializer files, `data/JsonlParser.kt`.

**Completion criteria:**
- Unknown `kind` values do not throw `SerializationException`.
- Known `kind` values still deserialize to their proper classes.
- The `Json` instance in `JsonlParser` uses the custom serializers.

**Testing expectations:** Unit tests: parse a line with a known kind, parse a line with an unknown kind, parse a `SessionA2uxEvent` with an unknown nested agent event kind. `Test Required`

#### 3.5.3 Map unknown events to a visible UI element

- [x] Map unknown events to a visible UI element

**Description:** In `SessionRepository.mapEventsToMessages()`, map `UnknownJunieEvent` and `UnknownAgentEvent` to a `Message` that renders as a visible "Unsupported event: {kind}" card in the Conversation UI. The card should be visually distinct (e.g., muted/warning style) and collapsed by default, but clearly present so the HITL can see what events are not yet supported and report them. Unknown events must **not** be silently discarded.

**Source:** Implementation sprint, Part 2.5 (Phase B); HITL requirement: "I do not want to skip unknown events and blindly throw them away."

**Dependencies:** Tasks 3.5.1, 3.5.2.

**Likely files / areas:** `data/SessionRepository.kt`, `domain/Message.kt` (may need a new `MessageKind` or content type), `ui/ConversationScreen.kt` (unknown-event card composable).

**Completion criteria:**
- Unknown events appear as visible items in the Conversation list.
- The event kind name is displayed so the user can report it.
- The card is visually distinct from normal messages.

**Testing expectations:** UI test confirming unknown-event card renders with the kind name visible. `Test Required`

**HITL-visible outcome:** Unknown events are visible in the Conversation, not silently dropped.

#### 3.5.4 Add logging for known vs unknown event counts per session load

- [x] Add logging for known vs unknown event counts per session load

**Description:** When a session is loaded, log the count of known events, unknown events, and total events. This helps diagnose data coverage without requiring the user to inspect the UI.

**Source:** Implementation sprint, Part 2.5 (Phase B).

**Dependencies:** Tasks 3.5.1, 3.5.2.

**Likely files / areas:** `data/SessionRepository.kt`.

**Completion criteria:**
- Session load logs include counts of known, unknown, and total events.
- Log level is INFO or WARN for unknown events.

**Testing expectations:** No automated test required; verify via manual log inspection.

#### 3.5.5 Add tests for Phase B unknown-event fallback

- [x] Add tests for Phase B unknown-event fallback

**Description:** Comprehensive tests for the unknown-event fallback: parsing unknown top-level events, parsing unknown nested agent events, verifying raw JSON is preserved, verifying the UI renders unknown-event cards, verifying known events still parse correctly.

**Source:** Implementation sprint, Part 2.5 (Phase B).

**Dependencies:** Tasks 3.5.1–3.5.3.

**Likely files / areas:** `shared/src/commonTest/kotlin/...` (parser tests, UI tests).

**Completion criteria:**
- Tests cover unknown top-level event parsing.
- Tests cover unknown nested agent event parsing.
- Tests cover UI rendering of unknown-event cards.
- All tests pass: `./gradlew :shared:jvmTest`.

**Testing expectations:** `./gradlew :shared:jvmTest` green. `Test Required`

#### 3.5.6 Add @Serializable classes for missing top-level event kinds

- [x] Add @Serializable classes for missing top-level event kinds

**Description:** Add proper `@Serializable` classes for the 4 missing top-level event kinds: `TaskStartedEvent`, `TaskState`, `UserMessagesCommittedToHistory`, `UserAsyncResponseEvent`. Inspect real JSONL payloads to determine the correct fields for each. Decide whether each maps to a UI `Message` or is metadata-only.

**Source:** Implementation sprint, Part 2.5 (Phase A); `docs/junie-jsonl-deserialization-investigation.md`, event kind inventory.

**Dependencies:** Tasks 3.5.1–3.5.5 (Phase B must be stable first).

**Likely files / areas:** `domain/JunieEvent.kt`, `data/SessionRepository.kt` (mapping).

**Completion criteria:**
- All 4 top-level event kinds have proper `@Serializable` classes.
- Each is registered in the custom serializer.
- Mapping decision (UI-visible vs metadata-only) is documented per event kind.

**Testing expectations:** Unit tests for each new event class. `Test Required`

#### 3.5.7 Add @Serializable classes for missing nested agent event kinds

- [x] Add @Serializable classes for missing nested agent event kinds

**Description:** Add proper `@Serializable` classes for the 13 missing nested agent event kinds: `AvailablePullRequestsEvent`, `LlmResponseMetadataEvent`, `CurrentDirectoryUpdatedEvent`, `EnvironmentVariablesUpdatedEvent`, `ViewFilesBlockUpdatedEvent`, `ContextWindowReportEvent`, `FileChangesBlockUpdatedEvent`, `TipSuggestionCreatedEvent`, `ShowPlanProgressEvent`, `NextPromptSuggestionEvent`, `AskAsyncRequestUpdatedEvent`, `AuthorizationAvailabilityEvent`, `AgentStartedEvent`, `SuggestPlanEvent`. Inspect real JSONL payloads to determine the correct fields. Decide per event kind whether it maps to a UI `Message` or is metadata-only.

**Source:** Implementation sprint, Part 2.5 (Phase A); `docs/junie-jsonl-deserialization-investigation.md`, event kind inventory.

**Dependencies:** Task 3.5.6.

**Likely files / areas:** `domain/JunieEvent.kt`, `data/SessionRepository.kt` (mapping).

**Completion criteria:**
- All 13 nested agent event kinds have proper `@Serializable` classes.
- Each is registered in the custom serializer.
- Mapping decision (UI-visible vs metadata-only) is documented per event kind.

**Testing expectations:** Unit tests for each new event class. `Test Required`

#### 3.5.8 Add tests for Phase A known event classes

- [x] Add tests for Phase A known event classes

**Description:** Tests for all 17 new event classes: verify deserialization from representative JSON payloads, verify correct field mapping, verify the custom serializer routes to the proper class instead of the unknown fallback.

**Source:** Implementation sprint, Part 2.5 (Phase A).

**Dependencies:** Tasks 3.5.6, 3.5.7.

**Likely files / areas:** `shared/src/commonTest/kotlin/...` (parser tests).

**Completion criteria:**
- Each of the 17 new event classes has at least one deserialization test.
- All tests pass: `./gradlew :shared:jvmTest`.

**Testing expectations:** `./gradlew :shared:jvmTest` green. `Test Required`

#### 3.5.9 Verify real session loading with complete event coverage

- [ ] Verify real session loading with complete event coverage

**Description:** Load a real Junie session (e.g., `session-260709-111457-1utg`) and verify that the event count matches the JSONL line count (minus blank lines). Confirm that no events are silently dropped. Any remaining unknown events should be visible in the UI as unsupported-event cards.

**Source:** Implementation sprint, Part 2.5.

**Dependencies:** Tasks 3.5.1–3.5.8.

**Likely files / areas:** Manual verification or integration test.

**Completion criteria:**
- Real session loads with 0 silently dropped events.
- Unknown events (if any remain) are visible in the UI.
- Log output confirms known/unknown/total counts.

**Testing expectations:** Manual verification or integration test. `Manual Review Required`

#### 3.5.10 HITL review of deserialization hardening

- [ ] HITL review of deserialization hardening — `HITL Review`

**Description:** HITL reviews the deserialization hardening work: confirms unknown events are visible in the UI, confirms known events parse correctly, confirms no data is silently lost, confirms real sessions render complete conversations.

**Source:** Implementation sprint, Part 2.5.

**Dependencies:** Tasks 3.5.1–3.5.9.

**Likely files / areas:** Running application, real session data.

**Completion criteria:**
- HITL confirms unknown events are visible, not silently dropped.
- HITL confirms known events render correctly.
- HITL confirms real sessions show complete conversations.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** The HITL can load a real Junie session and see all events — known events render normally, unknown events appear as visible indicators.

---

### Area 4 — Rich Content Rendering

*Source: Delivery Part 3 (design Part C). Implements rendering for each representative Junie output type.*

#### 4.1 Render plain text messages

- [ ] Render plain text messages — `Test Required`

**Description:** Ensure plain text messages render with wrapped, selectable body typography via theme tokens.

**Source:** Implementation sprint, Part 3; section 12 (Plain text row).

**Dependencies:** Area 2 complete.

**Likely files / areas:** `ui/ConversationScreen.kt` (`MessageItem`).

**Completion criteria:**
- Plain text messages wrap correctly.
- Text is selectable.
- Typography uses theme tokens.

**Testing expectations:** UI test with plain text fixture.

#### 4.2 Render Markdown core subset

- [ ] Render Markdown core subset — `Test Required`

**Description:** Implement rendering for the agreed Markdown core subset: headings, bold/italic, lists, inline code, and links-as-text. Complex tables are deferred.

**Source:** Implementation sprint, Part 3; section 12 (Markdown-like row).

**Dependencies:** Task 4.1.

**Likely files / areas:** New Markdown renderer composable in `components/`, `ui/ConversationScreen.kt`.

**Completion criteria:**
- Headings, bold, italic, lists, inline code, and links render correctly.
- Complex tables degrade to readable text.

**Testing expectations:** UI test with Markdown fixture covering each supported element.

#### 4.3 Render fenced code blocks with copy affordance

- [ ] Render fenced code blocks with copy affordance — `Test Required`

**Description:** Reuse `components/CodeBlock.kt` (`dev.snipme.highlights`) for fenced code blocks with horizontal scroll and add a copy affordance that copies clean plain text.

**Source:** Implementation sprint, Part 3; section 12 (Fenced code blocks row).

**Dependencies:** Task 4.1.

**Likely files / areas:** `components/CodeBlock.kt`.

**Completion criteria:**
- Fenced code blocks render in monospace with syntax highlighting.
- Horizontal scroll works for wide code.
- Copy affordance copies clean plain text to clipboard.

**Testing expectations:** UI test for code block rendering; copy-action test.

#### 4.4 Render Patch/Diff content with styling

- [ ] Render Patch/Diff content with styling — `Test Required`

**Description:** Extend `MessageContent.Diff` rendering with unified-diff styling (added/removed emphasis) and a copy affordance.

**Source:** Implementation sprint, Part 3; section 12 (Patch / Diff row).

**Dependencies:** Task 4.1.

**Likely files / areas:** `ui/ConversationScreen.kt`, `components/` (Diff renderer).

**Completion criteria:**
- Added lines are visually emphasised (e.g., green accent).
- Removed lines are visually emphasised (e.g., red accent).
- Copy affordance copies clean diff text.

**Testing expectations:** UI test with Diff fixture.

#### 4.5 Render Terminal Output

- [ ] Render Terminal Output — `Test Required`

**Description:** Add a Terminal Output renderer: monospace block, `$`-prefixed command line, preserved whitespace, and copy affordance.

**Source:** Implementation sprint, Part 3; section 12 (Terminal Output row).

**Dependencies:** Task 4.1.

**Likely files / areas:** New composable in `components/`, `domain/Message.kt` (extend `MessageContent` if needed).

**Completion criteria:**
- Terminal output renders in monospace.
- Command lines are `$`-prefixed.
- Whitespace is preserved.
- Copy affordance copies clean text.

**Testing expectations:** UI test with Terminal Output fixture.

#### 4.6 Render Tool Call summaries (collapsible)

- [ ] Render Tool Call summaries — `Test Required`

**Description:** Add a Tool Call renderer with structured-output (JSON-style) formatting and a collapsible header showing the tool name. Collapse/expand behaviour per open question Q2.

**Source:** Implementation sprint, Part 3; section 12 (Tool Call row).

**Dependencies:** Task 4.1.

**Likely files / areas:** New composable in `components/`, `domain/Message.kt`.

**Completion criteria:**
- Tool Call displays tool name in a collapsible header.
- Expanded view shows structured content.
- Collapse/expand works correctly.

**Testing expectations:** UI test for Tool Call rendering and collapse/expand.

#### 4.7 Render Structured Output fallback

- [ ] Render Structured Output fallback — `Test Required`

**Description:** Add a Structured Output renderer for JSON/code formatting. Rich tables/plans are deferred; content remains readable as text.

**Source:** Implementation sprint, Part 3; section 12 (Structured Output row).

**Dependencies:** Task 4.1.

**Likely files / areas:** New composable in `components/`, `domain/Message.kt`.

**Completion criteria:**
- Structured output renders in a formatted, readable style.
- Unsupported structures degrade to readable text.

**Testing expectations:** UI test with Structured Output fixture.

#### 4.8 Render errors and warnings distinctly

- [ ] Render errors and warnings distinctly — `Test Required`

**Description:** Add visually distinct rendering for error/warning messages with accent colour **plus** icon/label (never blended silently into plain text).

**Source:** Implementation sprint, Part 3; section 12 (Errors / warnings row).

**Dependencies:** Task 4.1.

**Likely files / areas:** `ui/ConversationScreen.kt`, `components/`.

**Completion criteria:**
- Errors and warnings are visually distinct from normal messages.
- Non-colour-only indicator (icon + label) is present.

**Testing expectations:** UI test with error/warning fixture.

#### 4.9 Render Thought messages (de-emphasised)

- [ ] Render Thought messages — `Test Required`

**Description:** Render Thought messages in a de-emphasised style. Thoughts are collapsible per implementation principle 4 (progressive disclosure).

**Source:** Implementation sprint, Part 3; section 10 (principle 4); section 12.

**Dependencies:** Task 4.1.

**Likely files / areas:** `ui/ConversationScreen.kt`, `components/`.

**Completion criteria:**
- Thoughts are visually de-emphasised compared to primary content.
- Thoughts are collapsible.

**Testing expectations:** UI test for Thought rendering.

#### 4.10 Implement malformed content fallback

- [ ] Implement malformed content fallback — `Test Required`

**Description:** Ensure any Message Kind or malformed content that has no dedicated renderer degrades to readable text (principle 5 — fallback over failure). Nothing becomes invisible or crashes the list.

**Source:** Implementation sprint, section 10 (principle 5); section 12.

**Dependencies:** Tasks 4.1–4.9.

**Likely files / areas:** `ui/ConversationScreen.kt` (`MessageItem`).

**Completion criteria:**
- Unknown or malformed content renders as readable text.
- No content causes a crash or becomes invisible.

**Testing expectations:** UI test with malformed/unknown content fixture.

#### 4.11 Create representative test fixtures per Message Kind

- [ ] Create representative test fixtures per Message Kind — `Test Required`

**Description:** Create a fixture Session exercising every Message Kind (Human Text, Junie Text/Markdown, fenced code, Patch/Diff, Terminal Output, Tool Call, Thought, Structured Output, error) that renders without crashing with correct Kind markers.

**Source:** Implementation sprint, section 16 (Representative fixtures).

**Dependencies:** Tasks 4.1–4.10. *Depends on* task 2.7.

**Likely files / areas:** `shared/src/commonTest/kotlin/...` (test fixtures).

**Completion criteria:**
- Fixture covers every Message Kind.
- All fixtures render without crashing.
- Correct Message Kind markers are displayed.

**Testing expectations:** Automated test running all fixtures.

#### 4.12 Document deferred rich rendering enhancements

- [ ] Document deferred rich rendering enhancements

**Description:** Document which rich rendering features are deferred (complex Markdown tables, advanced syntax highlighting, rich plans/summaries) in the Deferred / Out-of-Scope section.

**Source:** Implementation sprint, section 7; section 12.

**Dependencies:** Tasks 4.1–4.11.

**Likely files / areas:** This task document (Deferred / Out-of-Scope section).

**Completion criteria:**
- All deferred rendering enhancements are documented with reasons.

**Testing expectations:** No automated tests required.

#### 4.13 HITL visual review of rich content rendering

- [ ] HITL visual review of rich content rendering — `HITL Review`

**Description:** HITL reviews the implemented rich content rendering to confirm each content type is visually identifiable, errors are distinct, and copy actions produce clean text.

**Source:** Implementation sprint, Part 3 "After" section; HITL Review Plan (After Part 3).

**Dependencies:** Tasks 4.1–4.12.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms each content type is visually identifiable.
- HITL confirms errors/warnings are distinct.
- HITL confirms copy actions produce clean text.
- Any feedback is captured in the Notes / Decisions Log.

**Testing expectations:** No automated tests; HITL visual review.

**HITL-visible outcome:** A representative Conversation containing plain text, code, a Diff, a Tool Call, Terminal Output, and Structured Output renders with each content type visually identifiable.

### Area 5 — Search, Filters, and Navigation

*Source: Delivery Part 4 (design Part D). Refines Search Query UI and Message Kind Filters and adds orientation aids.*

#### 5.1 Review current search query behaviour

- [ ] Review current search query behaviour

**Description:** Review the existing `search_field` implementation: case-insensitive substring match over Message content, driven through `ConversationAction` into `ConversationState.searchQuery`, producing `filteredMessages`.

**Source:** Implementation sprint, Part 4; section 13.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `ui/ConversationScreen.kt`, `ui/ConversationViewModel.kt`.

**Completion criteria:**
- Current search behaviour is understood and documented.
- Any needed refinements are identified.

**Testing expectations:** No automated tests required for this review task.

#### 5.2 Refine search field placement

- [ ] Refine search field placement

**Description:** Confirm or refine the placement of the `search_field` in the top bar / chrome area so it remains visible and accessible at all times.

**Source:** Implementation sprint, Part 4; section 11.

**Dependencies:** Task 5.1.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- Search field is persistently visible in the chrome area.
- Placement is consistent with the design.

**Testing expectations:** Visual verification.

#### 5.3 Preserve or improve text search behaviour

- [ ] Preserve or improve text search behaviour — `Test Required`

**Description:** Ensure text search continues to work as designed: case-insensitive substring match, real-time filtering as the user types.

**Source:** Implementation sprint, Part 4; section 13.

**Dependencies:** Task 5.1.

**Likely files / areas:** `ui/ConversationViewModel.kt`.

**Completion criteria:**
- Search filters messages in real-time.
- Search is case-insensitive.

**Testing expectations:** Existing search tests pass; extend if behaviour changes.

#### 5.4 Review and refine filter chip layout

- [ ] Review and refine filter chip layout

**Description:** Review the current `FilterBar` layout and refine filter chip placement for clarity and usability.

**Source:** Implementation sprint, Part 4; section 13.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `components/FilterBar.kt`.

**Completion criteria:**
- Filter chips are clearly laid out and labelled.
- Layout works at different window widths.

**Testing expectations:** Visual verification.

#### 5.5 Ensure Message Kind filters are understandable

- [ ] Ensure Message Kind filters are understandable — `Test Required`

**Description:** Verify that Message Kind filter labels use ubiquitous language terms and are understandable to the HITL.

**Source:** Implementation sprint, Part 4; section 13.

**Dependencies:** Task 5.4.

**Likely files / areas:** `components/FilterBar.kt`.

**Completion criteria:**
- Filter labels match canonical terms from `UBIQUITOUS-LANGUAGE.md`.
- Filters are self-explanatory.

**Testing expectations:** UI test asserting filter labels.

#### 5.6 Implement no-results state

- [ ] Implement no-results state — `Test Required`

**Description:** When Search + Filters yield no Messages, show a distinct `no_results` state explaining that no Messages match (not a blank list).

**Source:** Implementation sprint, Part 4; section 13.

**Dependencies:** Tasks 5.3, 5.5.

**Likely files / areas:** `ui/ConversationScreen.kt`, `ui/ConversationViewModel.kt`.

**Completion criteria:**
- A `no_results` state is displayed when no messages match.
- The state includes explanatory text.
- The state has a `testTag("no_results")`.

**Testing expectations:** UI test asserting `no_results` state appears when search/filter yields nothing.

#### 5.7 Add result count

- [ ] Add result count

**Description:** Add a result count indicator showing how many Messages match the current Search + Filter combination, where it aids orientation.

**Source:** Implementation sprint, Part 4; section 13.

**Dependencies:** Tasks 5.3, 5.5.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- Result count is displayed when Search or Filters are active.
- Count updates in real-time as search/filter changes.

**Testing expectations:** Visual verification; optional UI test.

#### 5.8 Support keyboard/mouse interaction for search and filters

- [ ] Support keyboard/mouse interaction for search and filters

**Description:** Ensure Search field can be focused via keyboard shortcut (Cmd/Ctrl+F or similar), cleared, and that Filters are keyboard-accessible.

**Source:** Implementation sprint, Part 4; section 13; section 15.

**Dependencies:** Tasks 5.2, 5.4.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- Search field is focusable via keyboard shortcut.
- Search can be cleared via keyboard.
- Filters are keyboard-accessible.

**Testing expectations:** Manual keyboard interaction verification.

#### 5.9 Maintain user orientation in long conversations

- [ ] Maintain user orientation in long conversations

**Description:** Ensure the persistent Turn Header and Session context help the user maintain orientation when scrolling through long conversations.

**Source:** Implementation sprint, Part 4; section 13.

**Dependencies:** Tasks 3.6, 6.1.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- User can identify their position in a long conversation.
- Turn Headers and Session context remain visible or accessible.

**Testing expectations:** Manual verification with long conversation data.

#### 5.10 Implement match-to-match navigation (if Q3 confirmed)

- [ ] Implement match-to-match navigation — `Blocked` (pending Q3 confirmation)

**Description:** If HITL confirms Q3 is in scope: implement next/previous Match navigation driven by a Match Cursor. If not confirmed, this task is deferred.

**Source:** Implementation sprint, Part 4; section 13; open question Q3.

**Dependencies:** Tasks 5.3, 5.5. *Blocked* pending Q3 HITL decision.

**Likely files / areas:** `ui/ConversationViewModel.kt`, `ui/ConversationScreen.kt`.

**Completion criteria:**
- If in scope: next/previous Match buttons work; Match Cursor tracks position.
- If deferred: task is marked as deferred with reason.

**Testing expectations:** If implemented: unit tests for Match Cursor; UI test for navigation.

#### 5.11 Test search and filter combinations

- [ ] Test search and filter combinations — `Test Required`

**Description:** Test that Search + Filter combine correctly (AND logic), clearing restores the full Conversation, and message order is preserved.

**Source:** Implementation sprint, Part 4; section 16.

**Dependencies:** Tasks 5.3, 5.5, 5.6.

**Likely files / areas:** `shared/src/commonTest/kotlin/...`.

**Completion criteria:**
- Search + Filter AND-combination works correctly.
- Clearing search/filters restores full conversation.
- Message order is preserved during filtering.

**Testing expectations:** Automated tests for search+filter combinations.

#### 5.12 HITL review of search/filter usability

- [ ] HITL review of search/filter usability — `HITL Review`

**Description:** HITL reviews the search and filter implementation to confirm they behave as designed and the no-match state is clear.

**Source:** Implementation sprint, Part 4 "After" section; HITL Review Plan (After Part 4).

**Dependencies:** Tasks 5.1–5.11.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms search and filters update the Conversation correctly.
- HITL confirms no-match state is clear.
- Any feedback is captured in the Notes / Decisions Log.

**Testing expectations:** No automated tests; HITL review.

**HITL-visible outcome:** The HITL can enter a Search Query, toggle Filters, see the Conversation update, and understand when no Messages match.

---

### Area 6 — Session Context, Empty, Loading, and Error States

*Source: Delivery Part 5 (design Parts B/D). Delivers the Session header and all non-happy-path states.*

#### 6.1 Display current Session context

- [ ] Display current Session context — `Test Required`

**Description:** Implement a slim, persistent header (in or just below the chrome) showing the selected Session id and, when available, a timestamp/context line — visible without scrolling to the top.

**Source:** Implementation sprint, Part 5; section 11 (Session context/header).

**Dependencies:** Area 2 complete.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- Session id is displayed in a persistent header.
- Timestamp/context line is shown when available.
- Header is visible without scrolling.

**Testing expectations:** UI test asserting Session context is displayed.

#### 6.2 Handle no-Session-selected state

- [ ] Handle no-Session-selected state — `Test Required`

**Description:** When no Session is selected, show a distinct empty-state prompt guiding the user to select a Session.

**Source:** Implementation sprint, Part 5; section 11.

**Dependencies:** Task 6.1.

**Likely files / areas:** `ui/ConversationScreen.kt`, `ui/ConversationState.kt`.

**Completion criteria:**
- A clear "no Session selected" state is displayed.
- The state includes guidance to select a Session.

**Testing expectations:** UI test asserting no-Session state.

#### 6.3 Handle empty Conversation state

- [ ] Handle empty Conversation state — `Test Required`

**Description:** When a Session is selected but has no Messages, show a distinct "Session has no Messages" state.

**Source:** Implementation sprint, Part 5; section 11.

**Dependencies:** Task 6.1.

**Likely files / areas:** `ui/ConversationScreen.kt`, `ui/ConversationState.kt`.

**Completion criteria:**
- A clear "empty Conversation" state is displayed.
- The state is distinct from the no-Session state.

**Testing expectations:** UI test asserting empty Conversation state.

#### 6.4 Implement loading state

- [ ] Implement loading state — `Test Required`

**Description:** Show a progress indicator (`loading_indicator`) while `ConversationState.isLoading` is true.

**Source:** Implementation sprint, Part 5; section 11.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `ui/ConversationScreen.kt`, `ui/ConversationState.kt`.

**Completion criteria:**
- A loading indicator is displayed during data loading.
- The indicator has `testTag("loading_indicator")`.

**Testing expectations:** UI test asserting loading indicator appears.

#### 6.5 Implement recoverable error state

- [ ] Implement recoverable error state — `Test Required`

**Description:** Implement a recoverable error surface (`error_state`) with a retry affordance where applicable. Use the existing `FatalErrorDialog` for fatal cases.

**Source:** Implementation sprint, Part 5; section 11.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `ui/ConversationScreen.kt`, `ui/ConversationState.kt`, `components/FatalErrorDialog.kt`.

**Completion criteria:**
- Recoverable errors display an error surface with retry option.
- The error surface has `testTag("error_state")`.
- Fatal errors continue to use `FatalErrorDialog`.

**Testing expectations:** UI test asserting error state and retry affordance.

#### 6.6 Handle malformed or unsupported content with fallback UI

- [ ] Handle malformed content with fallback UI — `Test Required`

**Description:** Ensure malformed or unsupported content degrades to readable text rather than crashing or showing blank space. (Complements task 4.10 at the state level.)

**Source:** Implementation sprint, Part 5; section 10 (principle 5).

**Dependencies:** Task 4.10.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- Malformed content renders as readable text.
- No crash or blank space occurs.

**Testing expectations:** UI test with malformed content.

#### 6.7 Ensure error copy is understandable to the HITL

- [ ] Ensure error copy is understandable — `Manual Review Required`

**Description:** Review all error messages and state descriptions to ensure they are clear, professional, and understandable to a non-technical HITL.

**Source:** Implementation sprint, Part 5.

**Dependencies:** Tasks 6.2–6.5.

**Likely files / areas:** `ui/ConversationScreen.kt`, `components/FatalErrorDialog.kt`.

**Completion criteria:**
- Error messages are clear and professional.
- No vague messages like "Something went wrong".

**Testing expectations:** Manual review of error copy.

#### 6.8 Test or manually review each state

- [ ] Test or manually review each state — `Test Required`, `HITL Review`

**Description:** Verify each state (no-Session, empty, loading, error) through automated tests where practical and HITL manual review.

**Source:** Implementation sprint, Part 5 "After" section; HITL Review Plan.

**Dependencies:** Tasks 6.1–6.7.

**Likely files / areas:** Running application, test files.

**Completion criteria:**
- Each state has been tested or manually reviewed.
- HITL confirms each state is clear and understandable.

**Testing expectations:** Automated tests for loading/empty/error states; HITL visual review.

**HITL-visible outcome:** The HITL can tell which Session is open and see clear states for loading, empty, and error conditions.

---

### Area 7 — Accessibility and Cross-Platform Desktop Polish

*Source: Delivery Part 6 (design Parts E/F). Delivers accessibility semantics and desktop behaviour on macOS, Windows, and Linux.*

#### 7.1 Implement keyboard focus order

- [ ] Implement keyboard focus order

**Description:** Ensure focus order follows reading/chronological order (top to bottom): chrome controls first, then message list.

**Source:** Implementation sprint, Part 6; section 14.

**Dependencies:** Areas 3–6 complete.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- Tab/focus order follows logical reading order.
- All interactive controls are reachable via keyboard.

**Testing expectations:** Manual keyboard navigation verification.

#### 7.2 Add screen-reader-friendly labels and semantics

- [ ] Add screen-reader-friendly labels and semantics — `Test Required`

**Description:** Ensure every interactive control and important Message exposes a semantic label that doubles as its `testTag`. Labels include: Search field, Filter toggles, session/settings buttons, Message container, Sender marker, Message Kind marker, match indicator.

**Source:** Implementation sprint, Part 6; section 14.

**Dependencies:** Tasks 2.6, 3.3, 3.4.

**Likely files / areas:** `ui/ConversationScreen.kt`, all component files.

**Completion criteria:**
- All interactive controls have semantic labels.
- Labels double as `testTag` values.

**Testing expectations:** UI tests can find elements by their semantic tags.

#### 7.3 Verify colour contrast

- [ ] Verify colour contrast — `Manual Review Required`

**Description:** Verify sufficient colour contrast in both light and dark schemes for all text, accents, and interactive elements.

**Source:** Implementation sprint, Part 6; section 14.

**Dependencies:** Areas 3–4 complete.

**Likely files / areas:** Theme tokens, `ui/ConversationScreen.kt`.

**Completion criteria:**
- Text is readable in both light and dark themes.
- Contrast meets accessibility guidelines.

**Testing expectations:** Manual visual review in both themes.

#### 7.4 Verify scalable text

- [ ] Verify scalable text — `Manual Review Required`

**Description:** Verify that layout tolerates larger font scales; text is selectable with a readable minimum size and line length.

**Source:** Implementation sprint, Part 6; section 14.

**Dependencies:** Areas 3–4 complete.

**Likely files / areas:** `ui/ConversationScreen.kt`, theme tokens.

**Completion criteria:**
- Layout does not break at larger font scales.
- Text remains readable and selectable.

**Testing expectations:** Manual verification at increased font scale.

#### 7.5 Verify non-colour-only status indicators

- [ ] Verify non-colour-only status indicators — `Manual Review Required`

**Description:** Confirm that Sender, Message Kind, and error state always pair colour with a label, icon, or shape.

**Source:** Implementation sprint, Part 6; section 14.

**Dependencies:** Tasks 3.3, 3.4, 4.8.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- No status is communicated by colour alone.
- Every coloured indicator has a text or icon companion.

**Testing expectations:** Manual visual review.

#### 7.6 macOS visual and behaviour review

- [ ] macOS visual and behaviour review — `Manual Review Required`

**Description:** Run the application on macOS and verify: window sizing, Cmd shortcuts, font rendering, scrolling, clipboard/copy, and overall visual coherence.

**Source:** Implementation sprint, Part 6; section 15.

**Dependencies:** Areas 3–6 complete.

**Likely files / areas:** Running application on macOS.

**Completion criteria:**
- Application looks and behaves correctly on macOS.
- Cmd shortcuts work (focus search, clear search, copy).
- Scrolling and clipboard work natively.

**Testing expectations:** Manual review; document results.

#### 7.7 Windows visual and behaviour review

- [ ] Windows visual and behaviour review — `Manual Review Required`

**Description:** Run the application on Windows and verify: window sizing, Ctrl shortcuts, font rendering, scrolling, clipboard/copy, and overall visual coherence.

**Source:** Implementation sprint, Part 6; section 15.

**Dependencies:** Areas 3–6 complete.

**Likely files / areas:** Running application on Windows.

**Completion criteria:**
- Application looks and behaves correctly on Windows.
- Ctrl shortcuts work.
- Scrolling and clipboard work natively.

**Testing expectations:** Manual review; document results.

#### 7.8 Linux visual and behaviour review

- [ ] Linux visual and behaviour review — `Manual Review Required`

**Description:** Run the application on Linux and verify: window sizing, Ctrl shortcuts, font rendering, scrolling, clipboard/copy, and overall visual coherence.

**Source:** Implementation sprint, Part 6; section 15.

**Dependencies:** Areas 3–6 complete.

**Likely files / areas:** Running application on Linux.

**Completion criteria:**
- Application looks and behaves correctly on Linux.
- Ctrl shortcuts work.
- Scrolling and clipboard work natively.

**Testing expectations:** Manual review; document results.

#### 7.9 Font rendering review

- [ ] Font rendering review — `Manual Review Required`

**Description:** Verify platform default UI and monospace fonts render legibly on all three OSes via theme tokens.

**Source:** Implementation sprint, Part 6; section 15.

**Dependencies:** Tasks 7.6–7.8.

**Likely files / areas:** Theme tokens.

**Completion criteria:**
- UI font is legible on all platforms.
- Monospace font renders correctly in code blocks and terminal output.

**Testing expectations:** Manual visual review on each platform.

#### 7.10 Scrolling review

- [ ] Scrolling review — `Manual Review Required`

**Description:** Verify smooth vertical scrolling with trackpad, wheel, and scrollbar; respect platform scroll direction on all three OSes.

**Source:** Implementation sprint, Part 6; section 15.

**Dependencies:** Tasks 7.6–7.8.

**Likely files / areas:** `ui/ConversationScreen.kt`.

**Completion criteria:**
- Scrolling is smooth on all input methods.
- Platform scroll direction is respected.

**Testing expectations:** Manual verification on each platform.

#### 7.11 Clipboard/copy behaviour review

- [ ] Clipboard/copy behaviour review — `Manual Review Required`

**Description:** Verify that copying text, code, a Diff, or Terminal Output yields clean, unstyled plain text on every OS.

**Source:** Implementation sprint, Part 6; section 15 (Risk R3).

**Dependencies:** Tasks 4.3–4.5, 7.6–7.8.

**Likely files / areas:** Copy action implementations.

**Completion criteria:**
- Copied content is clean plain text on all platforms.
- No rich text or styling is included in clipboard.

**Testing expectations:** Manual copy/paste verification on each platform.

#### 7.12 Keyboard shortcut review

- [ ] Keyboard shortcut review — `Manual Review Required`

**Description:** Verify platform-appropriate shortcuts: Cmd on macOS, Ctrl on Windows/Linux for at least focus Search, clear Search, and next/previous Match (if in scope).

**Source:** Implementation sprint, Part 6; section 15.

**Dependencies:** Tasks 5.8, 7.6–7.8.

**Likely files / areas:** Shortcut wiring in `ui/ConversationScreen.kt`.

**Completion criteria:**
- Shortcuts use platform-appropriate modifier keys.
- At minimum: focus search and clear search shortcuts work.

**Testing expectations:** Manual keyboard shortcut verification.

#### 7.13 Platform-neutral file path display review

- [ ] Platform-neutral file path display review — `Manual Review Required`

**Description:** Verify that Session and file paths display in platform-native style; do not mangle `~`, drive letters, or separators.

**Source:** Implementation sprint, Part 6; section 15.

**Dependencies:** Tasks 7.6–7.8.

**Likely files / areas:** `ui/ConversationScreen.kt`, path display logic.

**Completion criteria:**
- Paths display correctly on each platform.
- `~`, drive letters, and separators are not mangled.

**Testing expectations:** Manual verification on each platform.

### Area 8 — Automated Testing

*Source: Delivery Part 7 (design Part G). Covers the core Conversation UI behaviour with automated tests plus a manual checklist.*

#### 8.1 Run baseline test suite before changes

- [ ] Run baseline test suite before changes — `Test Required`

**Description:** Run `./gradlew :shared:jvmTest` before making any UI changes to confirm all existing tests pass. Record the result.

**Source:** Implementation sprint, Part 7; `docs/TESTING.md`.

**Dependencies:** Area 1 complete.

**Likely files / areas:** Test output.

**Completion criteria:**
- All existing tests pass.
- Result is recorded.

**Testing expectations:** `./gradlew :shared:jvmTest` passes with zero failures.

#### 8.2 Update or create Compose UI tests

- [ ] Update or create Compose UI tests — `Test Required`

**Description:** Add or update Compose UI tests using `runComposeUiTest` to cover the new UI behaviour introduced in Areas 3–6.

**Source:** Implementation sprint, Part 7; section 16; `docs/TESTING.md`.

**Dependencies:** Areas 3–6 complete.

**Likely files / areas:** `shared/src/commonTest/kotlin/...`.

**Completion criteria:**
- UI tests cover asymmetric layout, rich content rendering, search/filter, and states.

**Testing expectations:** All new UI tests pass.

#### 8.3 Update ConversationRobot helper

- [ ] Update ConversationRobot helper — `Test Required`

**Description:** Extend `ConversationRobot` with intent-level helpers: e.g. `selectSession(...)`, `goToNextMatch()`, `assertMessageOfKindVisible(...)`, `assertNoResults()`, `assertLoadingVisible()`, `assertErrorVisible()`.

**Source:** Implementation sprint, Part 7; section 16; `docs/TESTING.md`.

**Dependencies:** Areas 3–6 complete.

**Likely files / areas:** `shared/src/commonTest/kotlin/...` (Robot file).

**Completion criteria:**
- Robot has helpers for all new UI interactions.
- Existing helpers (`typeSearchQuery`, `toggleFilter`, `assertMessageCount`, `assertMessageVisible`) still work.

**Testing expectations:** Robot helpers are used in new tests.

#### 8.4 Add semantic tags for important controls and content

- [ ] Add semantic tags — `Test Required`

**Description:** Add stable `Modifier.testTag(...)` to every important element: Message container, Sender marker, Message Kind marker, `search_field`, Filter toggles, `no_results`, `loading_indicator`, `error_state`, match indicator, Turn Header.

**Source:** Implementation sprint, Part 7; section 16.

**Dependencies:** Task 2.6.

**Likely files / areas:** `ui/ConversationScreen.kt`, all component files.

**Completion criteria:**
- All listed elements have stable `testTag` values.
- Tags double as accessibility labels.

**Testing expectations:** UI tests can find elements by their tags.

#### 8.5 Test Human/Junie rendering

- [ ] Test Human/Junie rendering — `Test Required`

**Description:** Automated tests asserting Sender markers, asymmetric positioning, and Turn grouping render correctly.

**Source:** Implementation sprint, Part 7; section 16.

**Dependencies:** Area 3 complete.

**Likely files / areas:** `shared/src/commonTest/kotlin/...`.

**Completion criteria:**
- Tests verify Human and Junie messages render with correct markers.
- Tests verify Turn grouping.

**Testing expectations:** Tests pass.

#### 8.6 Test rich content rendering

- [ ] Test rich content rendering — `Test Required`

**Description:** Representative-fixture rendering tests per Message Kind: plain text, Markdown, code, Diff, Terminal Output, Tool Call, Structured Output, error, Thought.

**Source:** Implementation sprint, Part 7; section 16.

**Dependencies:** Area 4 complete.

**Likely files / areas:** `shared/src/commonTest/kotlin/...`.

**Completion criteria:**
- Each Message Kind fixture renders without crashing.
- Correct Kind markers are displayed.

**Testing expectations:** Tests pass for all fixtures.

#### 8.7 Test search query behaviour

- [ ] Test search query behaviour — `Test Required`

**Description:** Tests for search filtering: case-insensitive match, real-time update, clearing restores full list.

**Source:** Implementation sprint, Part 7; section 16.

**Dependencies:** Area 5 complete.

**Likely files / areas:** `shared/src/commonTest/kotlin/...`.

**Completion criteria:**
- Search tests cover basic filtering, case insensitivity, and clear behaviour.

**Testing expectations:** Tests pass.

#### 8.8 Test filters

- [ ] Test filters — `Test Required`

**Description:** Tests for filter toggles: individual filters, AND-combination with search, clearing restores full list, order preserved.

**Source:** Implementation sprint, Part 7; section 16.

**Dependencies:** Area 5 complete.

**Likely files / areas:** `shared/src/commonTest/kotlin/...`.

**Completion criteria:**
- Filter tests cover individual and combined filtering.
- Message order is preserved.

**Testing expectations:** Tests pass.

#### 8.9 Test no-results state

- [ ] Test no-results state — `Test Required`

**Description:** Test that `no_results` state appears when Search + Filters yield no Messages.

**Source:** Implementation sprint, Part 7; section 16.

**Dependencies:** Task 5.6.

**Likely files / areas:** `shared/src/commonTest/kotlin/...`.

**Completion criteria:**
- `no_results` element is visible when no messages match.

**Testing expectations:** Test passes.

#### 8.10 Test long Junie response

- [ ] Test long Junie response — `Test Required`

**Description:** Automated smoke test for a very long Junie Turn: renders without crashing, scrolling works, Turn grouping holds.

**Source:** Implementation sprint, Part 7; section 16.

**Dependencies:** Areas 3–4 complete.

**Likely files / areas:** `shared/src/commonTest/kotlin/...`.

**Completion criteria:**
- Long Turn fixture renders without crashing.
- Turn grouping is maintained.

**Testing expectations:** Test passes; manual visual check that scrolling holds.

#### 8.11 Test empty/loading/error states

- [ ] Test empty/loading/error states — `Test Required`

**Description:** Tests for loading indicator, empty states (no-Session, no-Messages), and recoverable error state where practical.

**Source:** Implementation sprint, Part 7; section 16.

**Dependencies:** Area 6 complete.

**Likely files / areas:** `shared/src/commonTest/kotlin/...`.

**Completion criteria:**
- State tests verify correct elements are displayed for each state.

**Testing expectations:** Tests pass.

#### 8.12 Record testing gaps

- [ ] Record testing gaps

**Description:** Document any testing gaps (e.g., screenshot/visual-regression testing, cross-platform behaviour) in the Notes / Decisions Log.

**Source:** Implementation sprint, Part 7; section 16.

**Dependencies:** Tasks 8.1–8.11.

**Likely files / areas:** This task document.

**Completion criteria:**
- Testing gaps are documented with reasons.

**Testing expectations:** No automated tests required.

#### 8.13 Run final test suite

- [ ] Run final test suite — `Test Required`

**Description:** Run `./gradlew test` (all tests) and `./gradlew :shared:jvmTest` (shared module) to confirm everything passes after all changes.

**Source:** Implementation sprint, Part 7; `docs/TESTING.md`.

**Dependencies:** Tasks 8.1–8.12.

**Likely files / areas:** Test output.

**Completion criteria:**
- All tests pass with zero failures.
- Result is recorded.

**Testing expectations:** `./gradlew test` and `./gradlew :shared:jvmTest` both pass.

**HITL-visible outcome:** The automated test suite covers the core Conversation UI behaviour and the HITL has a checklist for visual review.

---

### Area 9 — HITL Review and Documentation

*Source: Delivery Part 8 (design Part H). Closes out the sprint with review and documentation updates.*

#### 9.1 Prepare HITL review checklist

- [ ] Prepare HITL review checklist

**Description:** Prepare the HITL Review Checkpoints section (section 8 of this document) as a ready-to-use checklist for the HITL.

**Source:** Implementation sprint, Part 8.

**Dependencies:** Areas 1–8 complete.

**Likely files / areas:** This task document.

**Completion criteria:**
- HITL Review Checkpoints section is complete and ready for use.

**Testing expectations:** No automated tests required.

#### 9.2 HITL review after baseline/layout implementation

- [ ] HITL review after baseline/layout — `HITL Review`

**Description:** HITL reviews the application after Parts 1–2 (baseline and asymmetric layout) are implemented.

**Source:** Implementation sprint, HITL Review Plan (After Part 2).

**Dependencies:** Areas 2–3 complete.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms baseline is preserved and layout is asymmetric.

**Testing expectations:** No automated tests; HITL visual review.

**HITL-visible outcome:** Human vs Junie messages are distinct; long Responses are readable.

#### 9.3 HITL review after rich content rendering

- [ ] HITL review after rich content — `HITL Review`

**Description:** HITL reviews the application after Part 3 (rich content rendering) is implemented.

**Source:** Implementation sprint, HITL Review Plan (After Part 3).

**Dependencies:** Area 4 complete.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms each content type is visually identifiable.

**Testing expectations:** No automated tests; HITL visual review.

**HITL-visible outcome:** Each Message Kind is visually identifiable; errors are distinct.

#### 9.4 HITL review after search/filter/navigation

- [ ] HITL review after search/filter — `HITL Review`

**Description:** HITL reviews the application after Part 4 (search, filters, navigation) is implemented.

**Source:** Implementation sprint, HITL Review Plan (After Part 4).

**Dependencies:** Area 5 complete.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms search and filters work as designed.

**Testing expectations:** No automated tests; HITL visual review.

**HITL-visible outcome:** Search + Filters update the Conversation; no-match state is clear.

#### 9.5 HITL review after accessibility/cross-platform polish

- [ ] HITL review after accessibility — `HITL Review`

**Description:** HITL reviews the application after Part 6 (accessibility and cross-platform polish) is implemented.

**Source:** Implementation sprint, HITL Review Plan (After Part 6).

**Dependencies:** Area 7 complete.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms keyboard operability, semantic labels, and non-colour-only signals.

**Testing expectations:** No automated tests; HITL visual review.

**HITL-visible outcome:** UI is keyboard navigable with semantic labels; manual checklist documented.

#### 9.6 HITL final review before completion

- [ ] HITL final review — `HITL Review`

**Description:** HITL performs final review before sprint completion (Part 8): Definition of Done satisfied, deferred items recorded, docs updated.

**Source:** Implementation sprint, HITL Review Plan (Before completion).

**Dependencies:** Areas 1–8 complete.

**Likely files / areas:** Running application, documentation.

**Completion criteria:**
- HITL confirms all sprint outcomes are met or explicitly deferred.
- Final approval is granted.

**Testing expectations:** No automated tests; HITL review.

**HITL-visible outcome:** The HITL can run the application, inspect representative Conversations, and confirm sprint outcomes.

#### 9.7 Capture HITL feedback

- [ ] Capture HITL feedback

**Description:** Record all HITL feedback from review checkpoints in the Notes / Decisions Log.

**Source:** Implementation sprint, Part 8.

**Dependencies:** Tasks 9.2–9.6.

**Likely files / areas:** This task document.

**Completion criteria:**
- All feedback is recorded with dates.

**Testing expectations:** No automated tests required.

#### 9.8 Incorporate accepted feedback

- [ ] Incorporate accepted feedback

**Description:** Implement changes based on accepted HITL feedback.

**Source:** Implementation sprint, Part 8.

**Dependencies:** Task 9.7.

**Likely files / areas:** Varies based on feedback.

**Completion criteria:**
- Accepted feedback items are implemented.
- Related tasks are updated.

**Testing expectations:** Tests updated as needed for feedback changes.

#### 9.9 Record deferred feedback

- [ ] Record deferred feedback

**Description:** Document any HITL feedback that is deferred to future sprints in the Deferred / Out-of-Scope section.

**Source:** Implementation sprint, Part 8.

**Dependencies:** Task 9.7.

**Likely files / areas:** This task document.

**Completion criteria:**
- Deferred feedback is documented with reasons.

**Testing expectations:** No automated tests required.

#### 9.10 Update documentation if behaviour changes

- [ ] Update documentation if behaviour changes

**Description:** Update `README.md` (via `readme-updater` skill) and `docs/project_memory.md` (via `project-memory` skill) if behaviour changed during the sprint.

**Source:** Implementation sprint, Part 8.

**Dependencies:** Areas 1–8 complete.

**Likely files / areas:** `README.md`, `docs/project_memory.md`.

**Completion criteria:**
- README reflects current application state.
- Project memory records what was shipped, key decisions, and gotchas.

**Testing expectations:** No automated tests required.

#### 9.11 Update README or developer docs if needed

- [ ] Update README or developer docs if needed

**Description:** Ensure developer documentation (README, TESTING.md) reflects any new testing patterns, commands, or setup steps introduced during the sprint.

**Source:** Implementation sprint, Part 8.

**Dependencies:** Areas 1–8 complete.

**Likely files / areas:** `README.md`, `docs/TESTING.md`.

**Completion criteria:**
- Developer docs are current.

**Testing expectations:** No automated tests required.

#### 9.12 Update task status as work completes

- [ ] Update task status as work completes

**Description:** Keep this task document up to date: check off completed tasks, update the Progress Summary table, and record notes as implementation progresses.

**Source:** Implementation sprint, Part 8.

**Dependencies:** Ongoing throughout the sprint.

**Likely files / areas:** This task document.

**Completion criteria:**
- All completed tasks are checked off.
- Progress Summary reflects current status.

**Testing expectations:** No automated tests required.

---

### Area 10 — Final Sprint Completion

*Source: Delivery Part 8 (design Part H). Final verification and sign-off.*

#### 10.1 Verify every delivery part is complete or explicitly deferred

- [ ] Verify every delivery part is complete or deferred

**Description:** Cross-check each of the 8 delivery parts against this task document to confirm all are complete or explicitly deferred with documented reasons.

**Source:** Implementation sprint, section 17.

**Dependencies:** Areas 1–9 complete.

**Likely files / areas:** This task document, implementation sprint document.

**Completion criteria:**
- Every delivery part is accounted for.
- Deferred parts have documented reasons.

**Testing expectations:** No automated tests required.

#### 10.2 Verify every "After" outcome is satisfied or deferred

- [ ] Verify every "After" outcome is satisfied or deferred

**Description:** Confirm that each delivery part's "After" section outcome is observable in the application or explicitly deferred.

**Source:** Implementation sprint, section 17.

**Dependencies:** Task 10.1.

**Likely files / areas:** Running application.

**Completion criteria:**
- Every "After" outcome is verified or deferred.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** The HITL can verify each "After" outcome.

#### 10.3 Verify tests pass or failures are documented

- [ ] Verify tests pass — `Test Required`

**Description:** Run `./gradlew test` and confirm all tests pass. Document any known failures with reasons.

**Source:** Implementation sprint, Part 8.

**Dependencies:** Area 8 complete.

**Likely files / areas:** Test output.

**Completion criteria:**
- All tests pass, or failures are documented with reasons.

**Testing expectations:** `./gradlew test` passes.

#### 10.4 Verify HITL review checkpoints are complete or deferred

- [ ] Verify HITL checkpoints are complete or deferred

**Description:** Confirm all 11 HITL Review Checkpoints (section 8) are checked or explicitly deferred.

**Source:** This task document, section 8.

**Dependencies:** Area 9 complete.

**Likely files / areas:** This task document.

**Completion criteria:**
- All checkpoints are addressed.

**Testing expectations:** No automated tests required.

#### 10.5 Verify no scope creep

- [ ] Verify no scope creep

**Description:** Confirm that out-of-scope items (section 10) did not accidentally expand the sprint. No deferred items were implemented without explicit HITL approval.

**Source:** Implementation sprint, section 7.

**Dependencies:** Tasks 10.1–10.4.

**Likely files / areas:** This task document, Deferred / Out-of-Scope section.

**Completion criteria:**
- No out-of-scope items were accidentally included.

**Testing expectations:** No automated tests required.

#### 10.6 Record final notes and decisions

- [ ] Record final notes and decisions

**Description:** Record any final notes, decisions, or lessons learned in the Notes / Decisions Log.

**Source:** Implementation sprint, Part 8.

**Dependencies:** Tasks 10.1–10.5.

**Likely files / areas:** This task document.

**Completion criteria:**
- Final notes are recorded.

**Testing expectations:** No automated tests required.

#### 10.7 Get final HITL approval

- [ ] Get final HITL approval — `HITL Review`

**Description:** Obtain final HITL approval for sprint completion. The HITL confirms the sprint outcomes, reviews deferred items, and grants sign-off.

**Source:** Implementation sprint, Part 8; section 21 (Definition of Done).

**Dependencies:** Tasks 10.1–10.6.

**Likely files / areas:** This task document, running application.

**Completion criteria:**
- HITL grants final approval.
- Sprint is marked as complete.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** The HITL can run the application, inspect representative Conversations, confirm the sprint outcomes, and see any deferred items documented.

---

## 8. HITL Review Checkpoints

- [ ] HITL confirms the task breakdown matches the implementation sprint document.
- [ ] HITL confirms every sprint "After" section maps to a reviewable task outcome.
- [ ] HITL confirms the Human/Junie conversation layout is readable and asymmetric.
- [ ] HITL confirms long Junie responses remain readable.
- [ ] HITL confirms unknown events are visible in the UI and known events parse correctly (Part 2.5).
- [ ] HITL confirms rich content types are visually identifiable.
- [ ] HITL confirms search and filters are understandable.
- [ ] HITL confirms empty/loading/error states are understandable.
- [ ] HITL confirms accessibility and cross-platform review items are sufficient.
- [ ] HITL confirms automated and manual testing coverage is acceptable.
- [ ] HITL confirms deferred items are explicitly documented.
- [ ] HITL gives final approval for sprint completion.

## 9. Acceptance Criteria

The task document is complete when:

- [ ] It exists under `docs/tasks/`.
- [ ] It links to the related implementation sprint document.
- [ ] It references the related design sprint and design task document.
- [ ] It references `docs/UBIQUITOUS-LANGUAGE.md`.
- [ ] It breaks every implementation delivery part into concrete tasks.
- [ ] Every task has a checkbox.
- [ ] Every task has completion criteria.
- [ ] Implementation tasks include testing expectations.
- [ ] Review-oriented tasks include HITL-visible outcomes.
- [ ] The document includes progress summary, HITL checkpoints, acceptance criteria, deferred items, and notes/decisions.
- [ ] No task is marked complete unless it has actually been completed.
- [ ] The document can be used by Junie and the HITL to track progress.

## 10. Deferred / Out-of-Scope Items

Items from the implementation sprint's Out of Scope section (section 7), plus open questions carried forward:

| # | Item | Reason Deferred | Follow-up |
|---|---|---|---|
| D1 | Real-time session tailing / streaming of an in-progress Session | Not in sprint scope; requires streaming architecture | Future sprint — streaming/live-tail feature |
| D2 | Full Markdown parser replacement | Only the agreed core subset is supported this sprint | Future sprint if richer Markdown is needed |
| D3 | Advanced/language-aware syntax highlighting | Beyond existing project dependencies (`dev.snipme.highlights`) | Future sprint — evaluate language-aware highlighting libraries |
| D4 | Complex virtualised navigation beyond basic lazy list | Basic `LazyColumn` scrolling is sufficient for this sprint | Future sprint if performance requires virtualisation |
| D5 | Editing, annotating, or replaying Conversation logs | Read-only viewer scope | Future product decision |
| D6 | Cloud sync or remote Sessions; multi-Session comparison or cross-Session search | Local-only scope | Future product decision |
| D7 | Export (Markdown/HTML) of Conversations | Not in sprint scope | Future sprint — export feature |
| D8 | Mobile UI | Desktop-only scope | Future product decision |
| D9 | Modifying `docs/UBIQUITOUS-LANGUAGE.md` | Candidate terms are listed but not added until follow-up tasks | Follow-up task before terms appear in shipped code |
| D10 | Screenshot/visual-regression testing | Deferred from design sprint; covered by manual HITL visual review | Future sprint — evaluate screenshot testing tools |

### Open Questions (carried forward from design sprint)

| # | Question | Status |
|---|---|---|
| Q1 | Are Timestamps reliably present per Message, and should they show inline, on hover, or only per Turn? | Open — resolve during implementation |
| Q2 | Should Thoughts and Tool Calls collapse by default, or expand with a collapse option? | Open — resolve during implementation |
| Q3 | Is match-to-match navigation (next/previous Match) in scope this sprint, or is Search + Filter sufficient? | Open — HITL to confirm |
| Q4 | Is a Turn-level outline / jump-to-Turn navigator in scope now, or deferred? | Open — HITL to confirm |
| Q5 | Is current syntax highlighting sufficient, or is language-aware highlighting expected? | Open — HITL to confirm |

## 11. Notes / Decisions Log

<!-- Record decisions, assumptions, and notes as implementation progresses -->

| Date | Decision / Note |
|---|---|
| 2026-07-09 | **Area 1 complete.** All source documents read: implementation sprint (8 delivery parts, 21 sections), design sprint (Parts A–H), design tasks (67/68 complete — only final HITL approval pending, does not block implementation), UBIQUITOUS-LANGUAGE.md (17 canonical terms + candidate additions noted), RECAP.md, TESTING.md, project_memory.md. |
| 2026-07-09 | **Scope verified.** All 9 in-scope items from sprint section 6 have corresponding task areas. All 9 out-of-scope items from sprint section 7 are listed in Deferred/Out-of-Scope (D1–D9). D10 (screenshot testing) added from design sprint deferrals. |
| 2026-07-09 | **Delivery part mapping verified.** Part 1 → Areas 1–2, Part 2 → Area 3, Part 2.5 → Area 3.5, Part 3 → Area 4, Part 4 → Area 5, Part 5 → Area 6, Part 6 → Area 7, Part 7 → Area 8, Part 8 → Areas 9–10. Every "After" section has at least one HITL-visible outcome in the task list. |
| 2026-07-09 | **Open questions Q1–Q5 confirmed** in Deferred/Out-of-Scope section. No blockers found — all open questions can be resolved during implementation without blocking Area 2. |
| 2026-07-09 | **Design task 10.5 (final HITL approval of design tasks)** is the only incomplete design task. This does not block implementation since all design content exists in the sprint document. |
| 2026-07-09 | **Candidate ubiquitous language additions noted:** Match, Match Cursor, Turn Header, Empty State / Loading State / Error State. These must be added to UBIQUITOUS-LANGUAGE.md before appearing in shipped code (per sprint section 5). |
| 2026-07-09 | **Area 2 complete.** Baseline code reviewed: ConversationRoot → ConversationScreen (Scaffold: top bar, search_field, FilterBar, LazyColumn message_list) → MessageItem. ConversationViewModel (MVI: 7 actions, search/filter logic). ConversationState (messages, filteredMessages, searchQuery, isLoading, sessions, filter). |
| 2026-07-09 | **Existing testTags inventoried:** session_picker_button, settings_button, search_field, message_list, filter_human, filter_junie, filter_thought, filter_tool, filter_patch, filter_terminal. **New tags needed:** sender_marker, message_kind_marker, turn_header, no_results, loading_indicator, error_state, match_indicator. |
| 2026-07-09 | **Representative fixture data created:** `fixtures/RepresentativeFixtures.kt` with 8 messages covering Human Text, Junie Text, Code, Diff/Patch, Terminal, Tool Call, Thought, and error content. |
| 2026-07-09 | **Baseline tests green:** `./gradlew :shared:jvmTest` — BUILD SUCCESSFUL, 0 failures. |
| 2026-07-09 | **Ubiquitous language mismatch found:** Sender label in MessageItem says "You" for Human messages. Should be "Human" per UBIQUITOUS-LANGUAGE.md. To be fixed in Area 3 (task 3.3 sender labels). |
| 2026-07-09 | **Area 3 implemented.** Asymmetric layout: Human messages compact right-aligned (max 480dp, primaryContainer), Junie messages full-width (secondaryContainer). Sender labels fixed to "Human"/"Junie" per ubiquitous language. Message Kind markers added (icon + text). Turn grouping with TurnHeader divider for consecutive Junie messages. Spacing uses theme tokens (12dp between turns, 8dp Junie internal padding). 5 new UI tests added. All tests green. Task 3.10 (HITL visual review) left unchecked — awaiting HITL review. |
| 2026-07-11 | **⚠️ BLOCKER — Deserialization data loss.** Investigation found 17 missing event kinds (4 top-level, 13 nested agent events). ~77% of real JSONL lines are silently dropped. The app does not crash fatally (Either.catch prevents that) but renders severely incomplete conversations. See `docs/junie-jsonl-deserialization-investigation.md`. **Recommendation:** Implement Option B (unknown-event fallback with custom serializer) as a hardening task before Area 4. Effort: 0.5–1 day, low risk. Awaiting HITL decision. |
| 2026-07-12 | **HITL decision: implement both Option B and Option A as Area 3.5.** Unknown events must be visible in the UI (not silently dropped) so users can report them. Phase B (tolerant fallback) first, then Phase A (add all 17 known event classes). Added as delivery Part 2.5 in sprint doc and Area 3.5 (10 tasks) in this task doc. Blocker resolved — work can proceed. |
| 2026-07-12 | **Area 3.5 implemented (tasks 3.5.1–3.5.8).** Phase B: replaced `@JsonClassDiscriminator` with custom `JsonContentPolymorphicSerializer` for both `JunieEvent` and `AgentEvent`. `UnknownJunieEvent`/`UnknownAgentEvent` preserve raw `JsonObject`. Unknown events map to visible "Unsupported event: {kind}" cards using `MessageKind.Unsupported` + `errorContainer` Surface. Session load logs known/unknown/total counts. Phase A: added all 4 top-level (`TaskStartedEvent`, `TaskState`, `UserMessagesCommittedToHistory`, `UserAsyncResponseEvent`) and all 13 nested agent event classes. All 4 top-level are metadata-only (no UI message). All 13 nested agent events are metadata-only (no UI message) — the unknown-event fallback remains as a permanent safety net. 20 new/updated tests added (parser + repository). `./gradlew :shared:jvmTest` BUILD SUCCESSFUL, 50 tests, 0 failures. Tasks 3.5.9 (manual real-session verification) and 3.5.10 (HITL review) left unchecked. **Deserialization blocker is resolved — UI sprint can continue.** |
| 2026-07-12 | **Crash fix (Area 3, tasks 3.11.1–3.11.8).** Two crash causes found in `desktopApp/logs/viewer.log`: (1) `NextPromptSuggestionEvent.suggestion` typed as `String?` but real data is `JsonArray` — fixed by changing to `JsonElement?`; (2) `CodeTextView` (kodeview) uses internal `verticalScroll` causing `IllegalStateException` inside `LazyColumn` — fixed by adding `heightIn(max = 600.dp)`. 1 new regression test added. `./gradlew :shared:jvmTest` BUILD SUCCESSFUL. **Other event classes may have similar field type mismatches** — the `JsonElement?` approach should be used for any field whose real format is uncertain. **UI sprint is unblocked.** |
