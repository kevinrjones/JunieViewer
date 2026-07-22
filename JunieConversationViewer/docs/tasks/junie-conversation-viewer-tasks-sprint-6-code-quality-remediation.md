# Sprint 6 — Code Quality Remediation: Task Breakdown

## 1. Related Sprint

**Sprint document:** [`docs/sprints/junie-conversation-viewer-sprint-6-code-quality-remediation.md`](../sprints/junie-conversation-viewer-sprint-6-code-quality-remediation.md)

**Sprint goal:** Resolve all twelve findings from the whole-codebase code quality review (2026-07-20) through behaviour-preserving refactoring: self-describing events and Message Kinds, typed domain boundaries, centralized expansion state, extracted Markdown parsing, a decomposed desktop entry point, repository/state hygiene, shared test infrastructure, and build hygiene.

## 2. Related Documents

| Document | Role |
|----------|------|
| [`docs/sprints/junie-conversation-viewer-sprint-6-code-quality-remediation.md`](../sprints/junie-conversation-viewer-sprint-6-code-quality-remediation.md) | **Primary source of truth.** Defines the delivery parts and requirements. |
| [`docs/sprints/junie-conversation-viewer-sprint-5-toolbar-menu-and-navigation-controls.md`](../sprints/junie-conversation-viewer-sprint-5-toolbar-menu-and-navigation-controls.md) | Preceding sprint; its behaviour must be preserved. |
| [`docs/tasks/junie-conversation-viewer-tasks-sprint-5-toolbar-menu-and-navigation-controls.md`](junie-conversation-viewer-tasks-sprint-5-toolbar-menu-and-navigation-controls.md) | Sprint 5 task breakdown for reference. |
| [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md) | Canonical domain terms. |
| [`docs/RECAP.md`](../RECAP.md) | Chronological project history. |
| [`docs/TESTING.md`](../TESTING.md) | Testing stack, Robot pattern, `testTag` conventions. |
| [`docs/project_memory.md`](../project_memory.md) | Decisions, gotchas, shipped work. |

## 3. Purpose

This document breaks Sprint 6 into concrete, trackable tasks. It serves as:

- **Junie's implementation checklist** — each task has clear completion criteria, dependencies, and testing expectations.
- **HITL's review and progress checklist** — each task has a checkbox, and review-oriented tasks include HITL-visible outcomes.

## 4. How to Use This Task Document

1. **Before starting implementation**, read the Related Documents listed above.
2. **Work through tasks in area order** (1–10). Within each area, tasks are ordered by dependency.
3. **Check off tasks** (`- [x]`) only when all completion criteria are met.
4. **Mark parent tasks complete** only when all subtasks are complete.
5. **Use inline markers** (see Task Status Legend) to flag blocked, deferred, or review-dependent tasks.
6. **Update the Progress Summary** table as areas are completed.
7. **Behaviour preservation is mandatory:** every area ends with a green `./gradlew :shared:jvmTest` and `./gradlew test` before starting the next.

## 5. Progress Summary

| # | Task Area | Status                             | Task Count |
|---|-----------|------------------------------------|------------|
| 1 | Discovery, Characterization, and Quick Wins (F7, F8, F10, F12) | Complete                           | 9 |
| 2 | Test Infrastructure (F11) | Complete                           | 6 |
| 3 | Typed Domain Events (F2) | Complete                           | 7 |
| 4 | Self-Mapping Events and Stable IDs (F3, F9) | Complete                           | 7 |
| 5 | Message Content Registry (F1) | Complete                           | 7 |
| 6 | Centralized Expansion State (F4) | Complete                           | 5 |
| 7 | Markdown Parser Extraction (F5) | Complete                           | 6 |
| 8 | Entry Point Decomposition (F6) | 8.1–8.6 Complete, 8.7 pending HITL | 7 |
| 9 | Documentation Updates | Not started                        | 5 |
| 10 | Testing, Review, and Completion | Not started                        | 7 |
| | **Total** |                                    | **66** |

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


### Area 1 — Discovery, Characterization, and Quick Wins (F7, F8, F10, F12)

*Source: Delivery Part 1. Confirms the review findings against current code and ships the low-risk fixes first.*

#### 1.1 Read project documentation and the review findings

- [x] Read project documentation and the review findings

**Description:** Read `UBIQUITOUS-LANGUAGE.md`, `TESTING.md`, `project_memory.md`, `RECAP.md`, the Sprint 6 sprint document, and the full review findings (F1–F12) to understand the baseline and remediation plan.

**Source:** Sprint doc sections 2 and 4.

**Dependencies:** None.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- All 12 findings and their target remedies are understood.

**Testing expectations:** None.

#### 1.2 Confirm each finding against current code

- [x] Confirm each finding against current code

**Description:** Verify the cited locations for F1–F12 (line ranges may have drifted); note any finding already partially addressed and any additional coverage gaps.

**Source:** Delivery Part 1.

**Dependencies:** 1.1.

**Likely files / areas:** `MessageItems.kt`, `ConversationViewModel.kt`, `AgentEvents.kt`, `EventToMessageMapper.kt`, `SessionRepository.kt`, `LiveSessionTracker.kt`, `MarkdownContent.kt`, `main.kt`, `libs.versions.toml`, test suite.

**Completion criteria:**
- Each finding is confirmed (or amended) with current file/line references.

**Testing expectations:** None.

#### 1.3 Fix version catalog bypass for slf4j (F12)

- [x] Fix version catalog bypass for slf4j

**Description:** Add `slf4j = "2.0.13"` to `[versions]` in `libs.versions.toml` and switch `slf4j-api` to `version.ref = "slf4j"`.

**Source:** Delivery Part 1; F12.

**Dependencies:** 1.2.

**Likely files / areas:** `gradle/libs.versions.toml`.

**Completion criteria:**
- No inline dependency versions remain in the catalog; build succeeds.

**Testing expectations:** `./gradlew build` compiles.

#### 1.4 Make search-state updates atomic (F8) — `Test Required`

- [x] Make search-state updates atomic

**Description:** Make `filterMessages` a pure function returning the derived state, and fold search-query changes into a single `_state.update` call.

**Source:** Delivery Part 1; F8.

**Dependencies:** 1.2.

**Likely files / areas:** `ConversationViewModel.kt`.

**Completion criteria:**
- One state emission per search-query change; `filterMessages` has no side effects.

**Testing expectations:** New test asserting exactly one emission per query change; existing search tests pass.

#### 1.5 Move turn grouping out of composition (F10) — `Test Required`

- [x] Move turn grouping out of composition

**Description:** Move `groupMessagesIntoTurns(state.filteredMessages)` into ViewModel state derivation (preferred, per Q4) or wrap in `remember(state.filteredMessages)`.

**Source:** Delivery Part 1; F10.

**Dependencies:** 1.2, HITL decision Q4.

**Likely files / areas:** `ConversationScreen.kt`, `ConversationViewModel.kt`.

**Completion criteria:**
- Grouping runs only when `filteredMessages` changes, not on every recomposition.

**Testing expectations:** Unit test for grouping derivation; existing UI tests pass.

#### 1.6 Rebuild extractWorkingDirectory on JsonlParser (F7) — `Test Required`

- [x] Rebuild extractWorkingDirectory on JsonlParser

**Description:** Delete the mini string scanner in `SessionRepository.extractWorkingDirectory` and reimplement it using `JsonlParser` and parsed events.

**Source:** Delivery Part 1; F7.

**Dependencies:** 1.2.

**Likely files / areas:** `SessionRepository.kt`, `SessionRepositoryTest.kt`.

**Completion criteria:**
- No bespoke JSONL scanning remains; behaviour unchanged for existing session files.

**Testing expectations:** Tests cover sessions with and without a working directory.

#### 1.7 Add characterization tests for event mapping — `Test Required`

- [x] Add characterization tests for event mapping

**Description:** Where coverage is thin, add tests capturing the current event→Message mapping output (kind, content, searchable text, collapsibility) per event type, using representative fixtures. These lock in behaviour before the structural rework in Areas 3–5.

**Source:** Delivery Part 1; Testing Strategy 21.1.

**Dependencies:** 1.2.

**Likely files / areas:** `EventToMessageMapperTest.kt` (or new), `RepresentativeFixtures.kt`.

**Completion criteria:**
- Every event type currently mapped has at least one test asserting its resulting Message shape.

**Testing expectations:** All new tests green.

#### 1.8 Verify green build after quick wins

- [x] Verify green build after quick wins

**Description:** Run `./gradlew :shared:jvmTest` and `./gradlew test` after Area 1 changes.

**Source:** Delivery Part 1; NFR2.

**Dependencies:** 1.3, 1.4, 1.5, 1.6, 1.7.

**Likely files / areas:** Entire project.

**Completion criteria:**
- Full green build.

**Testing expectations:** Green build.

#### 1.9 HITL review of confirmed findings and open questions — `HITL Review`

- [x] HITL review of confirmed findings and open questions

**Description:** Present confirmed findings, any deviations from the review, and open questions Q1–Q7 with recommendations for HITL decisions.

**Source:** Delivery Part 1; sprint doc section 24.

**Dependencies:** 1.8.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- HITL decisions recorded for Q1–Q7 in the Notes / Decisions Log.

**Testing expectations:** None.

---

### Area 2 — Test Infrastructure (F11)

*Source: Delivery Part 2. Builds shared test infrastructure so subsequent refactors are verified concisely.*

#### 2.1 Design and build runConversationUiTest DSL

- [x] Design and build runConversationUiTest DSL

**Description:** Create a `runConversationUiTest { }` DSL that wires temp session directory, `PreferencesRepository`, `ConversationViewModel`, and `ConversationRobot`, with automatic cleanup of temporary files.

**Source:** Delivery Part 2; F11.

**Dependencies:** 1.9.

**Likely files / areas:** New `shared/src/commonTest/.../ConversationUiTestDsl.kt`.

**Completion criteria:**
- A UI test can be written with wiring reduced to a single DSL call.

**Testing expectations:** DSL exercised by at least one migrated test.

#### 2.2 Migrate UI test files to the DSL

- [x] Migrate UI test files to the DSL

**Description:** Migrate the 14+ UI test files (e.g., `ConversationScreenTest`, `SortOrderTest`, `FilterBehaviourTest`, `CollapseShowAllTest`, `ConversationCommandTest`, `RefreshAndAutoRefreshTest`) to `runConversationUiTest { }`, file by file, keeping every assertion.

**Source:** Delivery Part 2; F11.

**Dependencies:** 2.1.

**Likely files / areas:** All `shared/src/commonTest/**` UI test files.

**Completion criteria:**
- No test hand-wires repository/ViewModel/Robot; assertion count is not reduced.

**Testing expectations:** Green build after each file migration.

#### 2.3 Consolidate overlapping UI integration test files

- [x] Consolidate overlapping UI integration test files

**Description:** Merge overlapping integration-level UI test files into cohesive behaviour-area suites (per Q5 decision, ~6 suites: search, filters, sort, expansion, commands, live tracking) without losing any behaviour check.

**Source:** Delivery Part 2; F11.

**Dependencies:** 2.2, HITL decision Q5.

**Likely files / areas:** `shared/src/commonTest/**` UI test files.

**Completion criteria:**
- Consolidated suites cover all previous assertions; before/after assertion counts documented.

**Testing expectations:** Green build; suite runtime does not regress materially (NFR5).

#### 2.4 Parameterize JsonlParserTest — `Test Required`

- [x] Parameterize JsonlParserTest

**Description:** Refactor the 603-line `JsonlParserTest.kt` into parameterized/table-driven tests; where sensible, generate input JSON via the real serializers to verify round-trip symmetry.

**Source:** Delivery Part 2; F11.

**Dependencies:** 2.1.

**Likely files / areas:** `data/JsonlParserTest.kt`.

**Completion criteria:**
- Coverage preserved with substantially less duplication; adding a new event case requires one table row.

**Testing expectations:** Green build.

#### 2.5 Verify green build after test restructuring

- [x] Verify green build after test restructuring

**Description:** Run `./gradlew :shared:jvmTest` and `./gradlew test`.

**Source:** Delivery Part 2; NFR2.

**Dependencies:** 2.3, 2.4.

**Likely files / areas:** Entire project.

**Completion criteria:**
- Full green build.

**Testing expectations:** Green build.

#### 2.6 Update docs/TESTING.md with the new DSL — `Manual Review Required`

- [x] Update docs/TESTING.md with the new DSL

**Description:** Document `runConversationUiTest { }`, the consolidated suite layout, and the parameterized parser-test pattern.

**Source:** Delivery Part 2.

**Dependencies:** 2.5.

**Likely files / areas:** `docs/TESTING.md`.

**Completion criteria:**
- New test infrastructure is documented for future contributors.

**Testing expectations:** None.

---

### Area 3 — Typed Domain Events (F2)

*Source: Delivery Part 3. Removes `JsonElement` from the domain model; raw JSON stops at the deserialization boundary.*

#### 3.1 Inventory JsonElement usage in the domain

- [x] Inventory JsonElement usage in the domain

**Description:** List every `JsonElement`/`JsonObject` field across the 20+ event types in `AgentEvents.kt` and the payload shapes actually observed in real `events.jsonl` files/fixtures.

**Source:** Delivery Part 3; F2.

**Dependencies:** 1.9.

**Likely files / areas:** `domain/AgentEvents.kt`, fixtures.

**Completion criteria:**
- A payload-shape inventory exists for every affected event type.

**Testing expectations:** None.

#### 3.2 Design typed payload models

- [x] Design typed payload models

**Description:** Define concrete data classes / sealed hierarchies for each payload (e.g., `AskRequest` with question and options, `ChoiceRequest`, result-block payloads). Model genuinely open-ended payloads explicitly (typed key/value), never as serialization-library types. Prefer sealed interfaces and tiny types per project guidelines.

**Source:** Delivery Part 3; F2.

**Dependencies:** 3.1.

**Likely files / areas:** `domain/AgentEvents.kt` (or new `domain/payloads/`).

**Completion criteria:**
- Typed model design covers every inventoried payload shape.

**Testing expectations:** None (design).

#### 3.3 Implement typed payloads and boundary parsing — `Test Required`

- [x] Implement typed payloads and boundary parsing

**Description:** Replace `JsonElement` fields with the typed models and parse them fully at the deserialization boundary via the `EventSerializers.kt` registry.

**Source:** Delivery Part 3; F2, FR1.

**Dependencies:** 3.2.

**Likely files / areas:** `domain/AgentEvents.kt`, `domain/EventSerializers.kt`.

**Completion criteria:**
- No `JsonElement`/`JsonObject` remains in `domain/`.

**Testing expectations:** Parameterized parser tests (2.4) pass; new payload-parsing tests added.

#### 3.4 Move ask/choice extraction out of the mapper — `Test Required`

- [x] Move ask/choice extraction out of the mapper

**Description:** Delete the manual question/option extraction logic in `EventToMessageMapper.kt` (~L133–172); the typed `AskRequest`/`ChoiceRequest` models expose this data directly.

**Source:** Delivery Part 3; F2.

**Dependencies:** 3.3.

**Likely files / areas:** `data/EventToMessageMapper.kt`.

**Completion criteria:**
- No `jsonObject[...]` navigation remains in the mapper.

**Testing expectations:** Characterization tests (1.7) pass unchanged.

#### 3.5 Update repository consumers of typed events

- [x] Update repository consumers of typed events

**Description:** Update `SessionRepository` (and any other consumer) to read typed payload properties instead of navigating JSON.

**Source:** Delivery Part 3; F2, FR7.

**Dependencies:** 3.3.

**Likely files / areas:** `data/SessionRepository.kt`.

**Completion criteria:**
- No stringly-typed JSON access outside the deserialization boundary.

**Testing expectations:** Repository tests pass.

#### 3.6 Handle unknown/malformed payloads deliberately — `Test Required`

- [x] Handle unknown/malformed payloads deliberately

**Description:** Define and test explicit behaviour for unparseable or unknown payload shapes (preserve current lenient behaviour; log at appropriate levels; no silent swallowing).

**Source:** Delivery Part 3; F2; guidelines on logging.

**Dependencies:** 3.3.

**Likely files / areas:** `domain/EventSerializers.kt`, `data/JsonlParser.kt`.

**Completion criteria:**
- Malformed-payload behaviour is explicit, logged, and tested.

**Testing expectations:** Negative-case tests added and green.

#### 3.7 Verify green build after typed events

- [x] Verify green build after typed events

**Description:** Run `./gradlew :shared:jvmTest` and `./gradlew test`.

**Source:** Delivery Part 3; NFR2.

**Dependencies:** 3.4, 3.5, 3.6.

**Likely files / areas:** Entire project.

**Completion criteria:**
- Full green build.

**Testing expectations:** Green build.


---

### Area 4 — Self-Mapping Events and Stable IDs (F3, F9)

*Source: Delivery Part 4. Collapses the parallel event registries and stabilizes Message identity.*

#### 4.1 Choose Strategy vs Visitor with HITL (Q1) — `HITL Review`

- [x] Choose Strategy vs Visitor with HITL

**Description:** Confirm the self-mapping mechanism per Q1 (recommendation: Strategy — `toMessage()` on each event with a shared context parameter).

**Source:** Delivery Part 4; F3; Open Question Q1.

**Dependencies:** 1.9, 3.7.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- HITL decision recorded in the Notes / Decisions Log.

**Testing expectations:** None.

#### 4.2 Implement self-mapping on AgentEvent — `Test Required`, `Depends on 4.1`

- [x] Implement self-mapping on AgentEvent

**Description:** Add the polymorphic mapping operation to `AgentEvent` so each event produces its own Message (or explicitly no Message), using its typed payload from Area 3.

**Source:** Delivery Part 4; F3, FR2.

**Dependencies:** 4.1.

**Likely files / areas:** `domain/AgentEvents.kt`.

**Completion criteria:**
- Every event type maps itself; no external `when` over event types is needed.

**Testing expectations:** Characterization tests (1.7) pass unchanged.

#### 4.3 Collapse EventToMessageMapper to orchestration

- [x] Collapse EventToMessageMapper to orchestration

**Description:** Delete the ~220-line `when` block; the mapper retains only orchestration (ordering, ID assignment, filtering of no-message events).

**Source:** Delivery Part 4; F3, FR2.

**Dependencies:** 4.2.

**Likely files / areas:** `data/EventToMessageMapper.kt`.

**Completion criteria:**
- Mapper contains no event-type dispatch; adding an event touches only its definition and serializer registration.

**Testing expectations:** Mapper tests pass unchanged.

#### 4.4 Derive stable Message IDs (F9) — `Test Required`

- [x] Derive stable Message IDs

**Description:** Replace the `content.hashCode()` fallback with a stable ID source per Q3 decision (recommendation: session path + file line offset), assigned during mapping.

**Source:** Delivery Part 4; F9; Open Question Q3.

**Dependencies:** 4.3, HITL decision Q3.

**Likely files / areas:** `data/EventToMessageMapper.kt`.

**Completion criteria:**
- IDs are deterministic across live tracking and full reloads.

**Testing expectations:** New ID-stability test: live-append then full reload yields identical IDs.

#### 4.5 Remove -live- prefix patching from LiveSessionTracker

- [x] Remove -live- prefix patching from LiveSessionTracker

**Description:** Delete the `-live-` prefix collision workaround (~L79–82); stable IDs make it unnecessary.

**Source:** Delivery Part 4; F9.

**Dependencies:** 4.4.

**Likely files / areas:** `data/LiveSessionTracker.kt`.

**Completion criteria:**
- No ID patching remains; live and reloaded Messages share identity.

**Testing expectations:** Live-tracking tests pass; no UI flicker in manual check.

#### 4.6 Verify expansion/search state survives reloads — `Test Required`

- [x] Verify expansion/search state survives reloads

**Description:** Add tests confirming that block expansion state and search match positions keyed by Message ID remain valid after a manual refresh during live tracking.

**Source:** Delivery Part 4; F9; Risk R3.

**Dependencies:** 4.5.

**Likely files / areas:** `RefreshAndAutoRefreshTest.kt`, `CollapseShowAllTest.kt` (or consolidated suites).

**Completion criteria:**
- State keyed by ID is preserved across reloads in tests.

**Testing expectations:** New tests green.

#### 4.7 Verify green build after mapping rework

- [x] Verify green build after mapping rework

**Description:** Run `./gradlew :shared:jvmTest` and `./gradlew test`.

**Source:** Delivery Part 4; NFR2.

**Dependencies:** 4.6.

**Likely files / areas:** Entire project.

**Completion criteria:**
- Full green build.

**Testing expectations:** Green build.

---

### Area 5 — Message Content Registry (F1)

*Source: Delivery Part 5. Replaces the three exhaustive `MessageKind` `when` chains with a single registry.*

#### 5.1 Confirm registry shape with HITL (Q2) — `HITL Review`

- [x] Confirm registry shape with HITL

**Description:** Decide per Q2 whether renderer composables live in the shared registry or in a UI-layer map keyed by kind (recommendation: collapsibility + searchable text shared; renderers UI-side).

**Source:** Delivery Part 5; F1; Open Question Q2.

**Dependencies:** 1.9, 4.7.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- HITL decision recorded in the Notes / Decisions Log.

**Testing expectations:** None.

#### 5.2 Implement MessageContentRegistry — `Test Required`, `Depends on 5.1`

- [x] Implement MessageContentRegistry

**Description:** Create the registry (plain map of descriptors — no reflection, no DI framework) providing per `MessageKind`: default collapsibility, searchable-text extractor, and renderer lookup per the Q2 decision.

**Source:** Delivery Part 5; F1, FR3.

**Dependencies:** 5.1.

**Likely files / areas:** New `ui/MessageContentRegistry.kt` (or shared equivalent).

**Completion criteria:**
- Registry covers every existing `MessageKind` with behaviour identical to the current `when` chains.

**Testing expectations:** Unit tests per kind for collapsibility and searchable text.

#### 5.3 Delete the ViewModel MessageKind when chain

- [x] Delete the ViewModel MessageKind when chain

**Description:** Replace the `when` in `ConversationViewModel.kt` (~L413–436) with registry lookups for searchable text and collapsibility.

**Source:** Delivery Part 5; F1, FR3.

**Dependencies:** 5.2.

**Likely files / areas:** `ui/ConversationViewModel.kt`.

**Completion criteria:**
- No `MessageKind` dispatch remains in the ViewModel.

**Testing expectations:** Search/filter/expansion tests pass unchanged.

#### 5.4 Delete the two MessageItems when chains

- [x] Delete the two MessageItems when chains

**Description:** Replace the two exhaustive `when` chains in `MessageItems.kt` (~L359–472, ~L513–603) with registry-driven rendering.

**Source:** Delivery Part 5; F1, FR3.

**Dependencies:** 5.2.

**Likely files / areas:** `ui/components/MessageItems.kt`.

**Completion criteria:**
- Rendering is registry-driven; adding a Message Kind requires one descriptor registration.

**Testing expectations:** UI tests pass; visual parity in manual check.

#### 5.5 Decompose MessageItems.kt into focused files

- [x] Decompose MessageItems.kt into focused files

**Description:** Split the 653-line file into `MessageKindMarker.kt`, `TurnHeader.kt`, `ExpansionState.kt`, and a `renderers/` directory of per-kind composables.

**Source:** Delivery Part 5; F1.

**Dependencies:** 5.4.

**Likely files / areas:** `ui/components/` (split), new `ui/components/renderers/`.

**Completion criteria:**
- No file in the split exceeds ~300 lines; responsibilities are cohesive.

**Testing expectations:** Green build; UI tests pass.

#### 5.6 Write ADR for registry and self-mapping decisions

- [x] Write ADR for registry and self-mapping decisions

**Description:** Record ADRs for the `MessageContentRegistry` and event self-mapping (per Q6 decision), including the named reason for each pattern per project guidelines.

**Source:** Delivery Part 5; Open Question Q6; guidelines (ADR per architectural decision).

**Dependencies:** 5.2, 4.2.

**Likely files / areas:** `docs/adr/` (new).

**Completion criteria:**
- ADRs exist documenting context, decision, and consequences.

**Testing expectations:** None.

#### 5.7 Verify green build after registry — `Manual Review Required`

- [x] Verify green build after registry

**Description:** Run `./gradlew :shared:jvmTest` and `./gradlew test`; manually verify all Message Kinds render identically in both themes.

**Source:** Delivery Part 5; NFR1, NFR2.

**Dependencies:** 5.5.

**Likely files / areas:** Entire project.

**Completion criteria:**
- Full green build and visual parity confirmed.

**Testing expectations:** Green build; manual visual check.

---

### Area 6 — Centralized Expansion State (F4)

*Source: Delivery Part 6. Gives expansion state a single ViewModel owner.*

#### 6.1 Audit the two expansion-state owners

- [x] Audit the two expansion-state owners

**Description:** Document the current interplay between ViewModel `blockExpansionStates` and UI-side `rememberMessageExpansionState`, including the Sprint 5 force-expansion priority rule.

**Source:** Delivery Part 6; F4.

**Dependencies:** 5.7.

**Likely files / areas:** `ConversationViewModel.kt`, `MessageItems.kt` split successors.

**Completion criteria:**
- All expansion-affecting inputs (manual toggle, Collapse All/Show All, search force-expansion, force dismissal) are enumerated.

**Testing expectations:** None.

#### 6.2 Derive final per-block expansion in the ViewModel — `Test Required`

- [x] Derive final per-block expansion in the ViewModel

**Description:** Fold search force-expansion into ViewModel state derivation so the ViewModel emits the final "is expanded" boolean per block, preserving `manualExpanded || (forceExpanded && !userDismissedForce)` semantics.

**Source:** Delivery Part 6; F4, FR4.

**Dependencies:** 6.1.

**Likely files / areas:** `ui/ConversationViewModel.kt`.

**Completion criteria:**
- A single derivation produces expansion state; UI receives it ready-made.

**Testing expectations:** `CollapseShowAllTest` passes with at most mechanical updates.

#### 6.3 Remove rememberMessageExpansionState from the UI

- [x] Remove rememberMessageExpansionState from the UI

**Description:** Delete the UI-side expansion state holder; renderers read derived state and dispatch toggle actions only.

**Source:** Delivery Part 6; F4, FR4.

**Dependencies:** 6.2.

**Likely files / areas:** Message renderer files.

**Completion criteria:**
- No competing expansion-state owner remains in the UI layer.

**Testing expectations:** UI expansion tests pass.

#### 6.4 Verify search force-expansion behaviour — `Test Required`

- [x] Verify search force-expansion behaviour

**Description:** Confirm the full Sprint 5 matrix still holds: Collapse All then search expands matching blocks; clearing search restores explicit state; manual dismissal of a forced expansion sticks.

**Source:** Delivery Part 6; F4; Sprint 5 Q7 decision.

**Dependencies:** 6.3.

**Likely files / areas:** Consolidated expansion test suite.

**Completion criteria:**
- All expansion interaction cases are covered and green.

**Testing expectations:** Green tests for the full matrix.

#### 6.5 Verify green build after expansion centralization

- [x] Verify green build after expansion centralization

**Description:** Run `./gradlew :shared:jvmTest` and `./gradlew test`.

**Source:** Delivery Part 6; NFR2.

**Dependencies:** 6.4.

**Likely files / areas:** Entire project.

**Completion criteria:**
- Full green build.

**Testing expectations:** Green build.


---

### Area 7 — Markdown Parser Extraction (F5)

*Source: Delivery Part 7. Moves Markdown parsing out of composables into a tested, UI-free component.*

#### 7.1 Catalogue currently supported Markdown forms

- [x] Catalogue currently supported Markdown forms

**Description:** List every block and inline form currently handled by `MarkdownContent.kt` (~L114–307) so the extracted parser preserves exact behaviour, including quirks.

**Source:** Delivery Part 7; F5.

**Dependencies:** 1.9.

**Likely files / areas:** `ui/components/MarkdownContent.kt`.

**Completion criteria:**
- Complete inventory of supported syntax and known edge cases.

**Testing expectations:** None.

#### 7.2 Implement MarkdownDocument parser — `Test Required`

- [x] Implement MarkdownDocument parser

**Description:** Create a non-UI parser (e.g., `markdown/MarkdownParser.kt`) producing a typed `MarkdownDocument` block/inline model, replacing the index-walking logic.

**Source:** Delivery Part 7; F5, FR5.

**Dependencies:** 7.1.

**Likely files / areas:** New `markdown/` package.

**Completion criteria:**
- Parser reproduces the catalogued behaviour with no Compose dependency.

**Testing expectations:** New `MarkdownParserTest` covering all catalogued forms and edge cases.

#### 7.3 Rewire MarkdownContent to render the parsed model

- [x] Rewire MarkdownContent to render the parsed model

**Description:** The composable iterates `MarkdownDocument` blocks; no `substring`/`indexOf` walking remains in UI code.

**Source:** Delivery Part 7; F5, FR5.

**Dependencies:** 7.2.

**Likely files / areas:** `ui/components/MarkdownContent.kt`.

**Completion criteria:**
- `MarkdownContent.kt` contains rendering only.

**Testing expectations:** Existing Markdown-related UI tests pass.

#### 7.4 Replace bespoke applySearchHighlight with canonical helper

- [x] Replace bespoke applySearchHighlight with canonical helper

**Description:** Delete the bespoke highlight implementation (~L195–229) and reuse `SearchHighlight.kt`.

**Source:** Delivery Part 7; F5.

**Dependencies:** 7.3.

**Likely files / areas:** `ui/components/MarkdownContent.kt`, `SearchHighlight.kt`.

**Completion criteria:**
- One canonical highlight implementation remains in the codebase.

**Testing expectations:** Search highlighting tests pass.

#### 7.5 Verify green build after parser extraction — `Manual Review Required`

- [x] Verify green build after parser extraction

**Description:** Run `./gradlew :shared:jvmTest` and `./gradlew test`; manually spot-check Markdown-heavy Messages in both themes.

**Source:** Delivery Part 7; NFR1, NFR2.

**Dependencies:** 7.4.

**Likely files / areas:** Entire project.

**Completion criteria:**
- Full green build and Markdown rendering parity.

**Testing expectations:** Green build; manual visual check.

#### 7.6 HITL review of Markdown rendering parity — `HITL Review`

- [x] HITL review of Markdown rendering parity

**Description:** Present before/after rendering of representative Markdown-heavy Conversations to HITL.

**Source:** Delivery Part 7.

**Dependencies:** 7.5.

**Likely files / areas:** Visual presentation.

**Completion criteria:**
- HITL approval of rendering parity.

**Testing expectations:** None.

---

### Area 8 — Entry Point Decomposition (F6)

*Source: Delivery Part 8. Reduces `main.kt` to pure wiring.*

#### 8.1 Extract JunieMenuBar composable

- [x] Extract JunieMenuBar composable

**Description:** Move the 115-line `MenuBar` definition from `main.kt` into a dedicated `JunieMenuBar` composable, keeping all commands and keyboard shortcuts unchanged.

**Source:** Delivery Part 8; F6, FR6.

**Dependencies:** 1.9.

**Likely files / areas:** `desktopApp/.../main.kt`, new `JunieMenuBar.kt`.

**Completion criteria:**
- Menu structure and shortcuts are byte-for-byte behaviour-identical.

**Testing expectations:** Manual menu/shortcut checklist on macOS.

#### 8.2 Extract DesktopClipboardManager

- [x] Extract DesktopClipboardManager

**Description:** Move the AWT synthetic-copy-event hack (~L260–301) behind a `DesktopClipboardManager` abstraction; `main.kt` knows only the command.

**Source:** Delivery Part 8; F6, FR6.

**Dependencies:** 8.1.

**Likely files / areas:** New `DesktopClipboardManager.kt`, `main.kt`.

**Completion criteria:**
- No `java.awt.EventQueue` references remain in `main.kt`.

**Testing expectations:** Manual copy check (toolbar/menu Copy with selection).

#### 8.3 Extract WindowStateTracker — `Test Required`

- [x] Extract WindowStateTracker

**Description:** Move window-state persistence (~L92–104) into a `WindowStateTracker` collaborating with `PreferencesRepository` via constructor injection.

**Source:** Delivery Part 8; F6, FR6.

**Dependencies:** 8.1.

**Likely files / areas:** New `WindowStateTracker.kt`, `main.kt`.

**Completion criteria:**
- Window size/position persistence logic lives in a testable class.

**Testing expectations:** Unit test for the state→preferences mapping.

#### 8.4 Extract logging setup and exception dialog

- [x] Extract logging setup and exception dialog

**Description:** Move `setupLogging`, the `Slf4jLogger` wiring, and the global exception dialog into a dedicated startup/logging component.

**Source:** Delivery Part 8; F6, FR6.

**Dependencies:** 8.1.

**Likely files / areas:** New logging setup file, `main.kt`.

**Completion criteria:**
- Logging/exception concerns are outside `main.kt`.

**Testing expectations:** App starts; logs still written to `~/.junieviewer/logs/`.

#### 8.5 Reduce main.kt to pure wiring

- [x] Reduce main.kt to pure wiring

**Description:** After extractions, `main.kt` should contain only composition of the extracted components (target: well under 100 lines).

**Source:** Delivery Part 8; F6, FR6.

**Dependencies:** 8.2, 8.3, 8.4.

**Likely files / areas:** `desktopApp/.../main.kt`.

**Completion criteria:**
- `main.kt` contains no business, menu, clipboard, persistence, or logging logic.

**Testing expectations:** Green build; app launches.

#### 8.6 Verify green build after decomposition

- [x] Verify green build after decomposition

**Description:** Run `./gradlew :shared:jvmTest` and `./gradlew test`.

**Source:** Delivery Part 8; NFR2.

**Dependencies:** 8.5.

**Likely files / areas:** Entire project.

**Completion criteria:**
- Full green build.

**Testing expectations:** Green build.

#### 8.7 HITL review of desktop behaviour parity — `HITL Review`, `Manual Review Required`

- [ ] HITL review of desktop behaviour parity

**Description:** HITL verifies menus, shortcuts, copy behaviour, window-state persistence across restart, and the exception dialog after decomposition.

**Source:** Delivery Part 8; Manual Review Checklist 21.2.

**Dependencies:** 8.6.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL approval of full desktop behaviour parity.

**Testing expectations:** Manual verification on macOS.

---

### Area 9 — Documentation Updates

*Source: Delivery Part 9. Keeps documentation in sync with the new architecture.*

#### 9.1 Update docs/TESTING.md

- [ ] Update docs/TESTING.md

**Description:** Finalize testing documentation: the DSL (from 2.6), consolidated suite layout, parameterized parser pattern, and characterization-test approach.

**Source:** Delivery Part 9.

**Dependencies:** 8.7.

**Likely files / areas:** `docs/TESTING.md`.

**Completion criteria:**
- Testing docs reflect the restructured suite.

**Testing expectations:** None.

#### 9.2 Update README.md

- [ ] Update README.md

**Description:** Reflect the refactored architecture (registry, typed events, decomposed entry point) where the README describes internals; user-facing content is unchanged.

**Source:** Delivery Part 9.

**Dependencies:** 8.7.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- README is accurate for the post-sprint codebase.

**Testing expectations:** None.

#### 9.3 Update docs/RECAP.md

- [ ] Update docs/RECAP.md

**Description:** Add Sprint 6 milestones to the project recap.

**Source:** Delivery Part 9.

**Dependencies:** 8.7.

**Likely files / areas:** `docs/RECAP.md`.

**Completion criteria:**
- RECAP is up to date.

**Testing expectations:** None.

#### 9.4 Update docs/project_memory.md

- [ ] Update docs/project_memory.md

**Description:** Record what was shipped, key decisions (Q1–Q7 outcomes, ADR references), gotchas, and test coverage areas for Sprint 6.

**Source:** Delivery Part 9; project guidelines (project-memory skill).

**Dependencies:** 8.7.

**Likely files / areas:** `docs/project_memory.md`.

**Completion criteria:**
- Project memory is updated per the sprint-completion guidelines.

**Testing expectations:** None.

#### 9.5 Finalize ADRs

- [ ] Finalize ADRs

**Description:** Ensure ADRs from 5.6 (registry, self-mapping) plus any additional structural decisions (stable ID scheme, expansion derivation) are complete and cross-linked.

**Source:** Delivery Part 9; guidelines (ADR per architectural decision).

**Dependencies:** 8.7.

**Likely files / areas:** `docs/adr/`.

**Completion criteria:**
- All Sprint 6 architectural decisions are documented.

**Testing expectations:** None.

---

### Area 10 — Testing, Review, and Completion

*Source: Delivery Part 10. Final verification.*

#### 10.1 Run ./gradlew :shared:jvmTest

- [ ] Run shared module tests

**Description:** Verify that all common logic tests pass.

**Source:** Delivery Part 10.

**Dependencies:** 9.5.

**Likely files / areas:** `shared` module.

**Completion criteria:**
- Shared tests pass.

**Testing expectations:** Green build.

#### 10.2 Run ./gradlew test

- [ ] Run all tests

**Description:** Execute the full automated test suite.

**Source:** Delivery Part 10.

**Dependencies:** 10.1.

**Likely files / areas:** Entire project.

**Completion criteria:**
- All tests pass.

**Testing expectations:** Green build.

#### 10.3 Run ./gradlew check

- [ ] Run gradle check for all modules

**Description:** Run the full `check` task per project guidelines.

**Source:** Delivery Part 10; guidelines (After Adding Code).

**Dependencies:** 10.2.

**Likely files / areas:** Entire project.

**Completion criteria:**
- `check` succeeds for all modules.

**Testing expectations:** Green build.

#### 10.4 Run manual parity checklist — `Manual Review Required`

- [ ] Run manual parity checklist

**Description:** Execute the manual review checklist from sprint doc section 21.2 (visual parity, search flow, live tracking, menus/shortcuts, window state).

**Source:** Delivery Part 10.

**Dependencies:** 10.3.

**Likely files / areas:** Running application.

**Completion criteria:**
- All manual checks pass.

**Testing expectations:** Manual sign-off.

#### 10.5 Run cyclomatic complexity check

- [ ] Run cyclomatic complexity check

**Description:** Re-run the complexity review to confirm the remediation reduced complexity (mapper `when` gone, `MessageItems.kt` split, `main.kt` slim) and decide whether further reduction steps are needed.

**Source:** Sprint Guidelines (sprint completion requirement).

**Dependencies:** 10.4.

**Likely files / areas:** Entire codebase.

**Completion criteria:**
- Complexity check run, results reviewed, and update/no-update decision recorded.

**Testing expectations:** None.

#### 10.6 Fix review issues

- [ ] Fix review issues

**Description:** Address any findings from tests, manual review, or the complexity check.

**Source:** Delivery Part 10.

**Dependencies:** 10.5.

**Likely files / areas:** Affected files.

**Completion criteria:**
- Issues are resolved.

**Testing expectations:** Re-run tests.

#### 10.7 HITL final approval — `HITL Review`

- [ ] HITL final approval

**Description:** Obtain final sign-off for Sprint 6.

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
| 1 | 1.9 | Discovery | Confirmed findings, quick wins shipped, and Q1–Q7 decisions recorded. |
| 2 | 4.1 | Self-Mapping Events | Strategy vs Visitor decision for event self-mapping. |
| 3 | 5.1 | Message Content Registry | Registry shape decision (shared vs UI-layer renderer lookup). |
| 4 | 7.6 | Markdown Parser | Markdown rendering parity confirmed on representative Conversations. |
| 5 | 8.7 | Entry Point | Menus, shortcuts, copy, window-state persistence, and exception dialog verified after decomposition. |
| 6 | 10.7 | Completion | All tests and checks pass, manual parity confirmed, HITL final approval. |

## 9. Acceptance Criteria

- All 66 tasks marked complete.
- All **Test Required** tasks have passing automated tests.
- All **HITL Review** tasks have HITL approval.
- All **Manual Review Required** tasks verified.
- All 12 review findings (F1–F12) resolved per the sprint doc's Definition of Done.
- No `JsonElement`/`JsonObject` in `domain/`; mapper `when` block removed; three `MessageKind` `when` chains replaced by the registry.
- Expansion state has a single ViewModel owner; Markdown parsing is UI-free; `main.kt` is pure wiring.
- Message IDs stable across live tracking and reloads; search updates atomic; turn grouping derived once.
- `runConversationUiTest { }` DSL in use; `JsonlParserTest` parameterized; no assertion lost in consolidation.
- `./gradlew :shared:jvmTest`, `./gradlew test`, and `./gradlew check` pass.
- ADRs recorded; `README.md`, `TESTING.md`, `RECAP.md`, `project_memory.md` updated.
- Cyclomatic complexity check run and reviewed.

## 10. Deferred / Out-of-Scope Items

- Any user-visible feature additions or UI redesign.
- Introducing Arrow `Either` across layers that don't already use it.
- Replacing the bespoke Markdown parser with a third-party library.
- Modularizing `shared` into further Gradle modules.
- Convention plugins for shared dependency declarations.
- Performance work beyond the F10 recomposition fix.
- If the sprint runs long (per Q7): Area 7 (Markdown parser) and Area 8 (entry point) are the designated deferral candidates; Areas 3–5 (blockers) must not be deferred.

## 11. Notes / Decisions Log

| Date | Decision | Context |
|------|----------|---------|
| 2026-07-20 | Sprint 6 planned from code quality review | Whole-codebase "thermo-nuclear" review produced 12 findings (3 structural blockers, 5 high priority, 4 worth fixing). Sprint 6 scoped to resolve all of them via behaviour-preserving refactoring, ordered so the shared code-judo move (typed payloads → self-mapping events → content registry) resolves the blockers, with test infrastructure landing early as the safety net. |
| 2026-07-20 | Area 1 completed (tasks 1.1–1.8); HITL review 1.9 pending | Discovery findings recorded in [`docs/sprint-6-area-1-discovery-findings.md`](../sprint-6-area-1-discovery-findings.md). All 12 findings confirmed with current line references; two amendments: F2 covers 19 event types (not 20+), and F11's "ViewModel+Prefs+Robot" triple wiring applies to 2 files while 14 files hand-wire ViewModel+Prefs. Quick wins shipped: F12 (slf4j `version.ref`), F8 (pure `filterMessages(state, query)`, one atomic emission per search/filter/sort/load/live update), F10 (Turns derived in ViewModel as `ConversationState.turns`), F7 (`extractWorkingDirectory` rebuilt on `JsonlParser`). Deviation flagged for HITL: `@JsonNames("currentDirectory")` alias added to `CurrentDirectoryUpdatedEvent.directory` — real logs use the `currentDirectory` key, required to preserve behaviour through the parser (proposed Q8). 38 new tests added (29 event-mapping characterization, 4 search-atomicity/turn-derivation, 5 working-directory characterization); two `ConversationViewModelTest` tests mechanically updated from two-emission to single-emission expectations. Baseline: pre-change `./gradlew :shared:jvmTest` + `./gradlew test` green (378 tests, 0 failures); post-change green (425 tests, 0 failures). Key risks/open questions: Q1–Q7 pending HITL plus proposed Q8; `turns` must only be written via `filterMessages` (staleness invariant); serializer field names may drift from real logs — recommend field-name audit in task 3.1. |
| 2026-07-20 | HITL decisions Q1–Q8 recorded (task 1.9 complete) | **Q1:** Strategy — `toMessage()` on each event with a shared context parameter. **Q2:** Split registry — collapsibility + searchable text shared; renderer lookup in a UI-layer map. **Q3:** File line offset (session path + line number) as the stable Message ID source. **Q4:** ViewModel derivation of Turn grouping ratified (as shipped in Area 1). **Q5:** Consolidate UI tests into ~6 behaviour-area suites (search, filters, sort, expansion, commands, live tracking). **Q6:** Yes — one ADR per structural decision. **Q7:** **No deferral** — all ten areas are committed this sprint (HITL overrode the defer-8-then-7 recommendation). **Q8 (new):** Accept the `@JsonNames("currentDirectory")` alias and add a systematic serializer field-name audit against real logs to task 3.1. |
| 2026-07-20 | Area 2 completed (tasks 2.1–2.6) | Shared test infrastructure shipped. **2.1:** New `ConversationUiTestDsl.kt` with `runConversationUiTest { }` (Compose; temp-prefs + `FakeSessionRepository` + lazy ViewModel/Robot, `setConversationContent()`, scope implements `SemanticsNodeInteractionsProvider`, automatic temp-file cleanup) and `runConversationStateTest { }` (ViewModel tests; `Dispatchers.setMain`/`resetMain`, `createViewModel()`, `advanceUntilIdle()`, exposed `testScope`). **2.2:** 15 hand-wiring files migrated (the 14 from discovery plus Area 1's `SearchStateDerivationTest`); 4 pure-component files (`MarkdownSearchHighlightTest`, `SessionSelectorTest`, `SessionStatesTest`, plus component-only tests) intentionally left un-wired since they render composables without the ViewModel; no assertion changed. **2.3 (Q5):** Six behaviour-area suites — new `ExpansionBehaviourTest` (CollapseShowAll 10t/30a + CollapsibleBlock 14t/26a = 24t/56a), `LiveTrackingBehaviourTest` (LiveTrackingViewModel 3t/8a + RefreshAndAutoRefresh 13t/20a = 16t/28a), `SearchBehaviourTest` (SearchFilterNavigation 9t/19a + SearchStateDerivation 4t/10a = 13t/29a); `FilterBehaviourTest`, `SortOrderTest`, `ConversationCommandTest` already 1:1; all merged assertions carried verbatim, source files deleted. **2.4:** `JsonlParserTest` now table-driven — 49 `ParserCase` rows under `@RunWith(Parameterized::class)` (case name in failure output; new event case = one row) plus new `JsonlParserTestRoundTrip` (5 tests via real serializers from `EventSerializers.kt`). **2.5:** `./gradlew :shared:jvmTest` (forced rerun) and `./gradlew test` green — 430 tests, 0 failures across 35 classes (425 baseline + 5 round-trip). **2.6:** `TESTING.md` documents the DSL, suite layout, and parser pattern (manual review of the doc still open for HITL). No Area 3+ work started; the serializer field-name audit remains scheduled for task 3.1 per Q8. |
| 2026-07-22 | Toolbar Copy parity fix (Area 8) | Toolbar Copy button was not copying selected text — it dispatched `ConversationCommand.Copy` through the ViewModel event channel, which emitted a `CopyText` event that posted a synthetic Cmd+C key event via `DesktopClipboardManager.dispatchCopy()`. However, the synthetic key event was intercepted by the menu bar's keyboard accelerator before reaching Compose's `SelectionContainer`, creating a no-op loop. Initial fix (intercepting `Copy` in `ConversationRoot`'s `onCommand` wrapper) did not resolve the issue. Second fix: added a dedicated `onCopySelectedText` callback threaded through `ConversationToolbar` → `ConversationScreen` → `ConversationRoot`, so the toolbar Copy button invokes `onCopyText()` (i.e. `clipboardManager.dispatchCopy(window)`) directly without entering the ViewModel event channel — still not sufficient. **Root cause found:** clicking the toolbar `IconButton` grabs Compose-internal keyboard focus (Compose Desktop buttons take focus on click, unlike browsers), so the synthetic Ctrl+C/Cmd+C key event dispatched immediately afterward is routed to the button's own focus chain instead of the `SelectionContainer` holding the actual text selection — the clipboard was therefore never updated. **Final fix:** the toolbar Copy `IconButton` was made non-focusable via `Modifier.focusProperties { canFocus = false }` (new `focusable` parameter on `ToolbarIconButton`), so clicking it no longer steals focus away from the selected text; the subsequent `dispatchCopy` synthetic key event now reaches the `SelectionContainer` correctly. All three copy paths (toolbar button, Edit → Copy menu, Cmd+C/Ctrl+C accelerator) converge on the same `clipboardManager.dispatchCopy(window)` implementation. `./gradlew :shared:jvmTest` and `./gradlew test` green. Task 8.7 remains pending HITL manual verification. |