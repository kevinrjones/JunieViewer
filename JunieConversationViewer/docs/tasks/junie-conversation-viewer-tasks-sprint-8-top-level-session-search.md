# Sprint 8 — Top-Level Session Search: Task Breakdown

## 1. Related Sprint

**Sprint document:** [`docs/sprints/junie-conversation-viewer-sprint-8-top-level-session-search.md`](../sprints/junie-conversation-viewer-sprint-8-top-level-session-search.md)

**Sprint goal:** Add a Top-Level Session Search workflow that searches all discovered Session files and opens the selected Session in the Conversation viewer without regressing existing Conversation Search.

## 2. Related Documents

| Document | Role |
|----------|------|
| [`docs/sprints/junie-conversation-viewer-sprint-8-top-level-session-search.md`](../sprints/junie-conversation-viewer-sprint-8-top-level-session-search.md) | **Primary source of truth** for requirements, constraints, and decisions. |
| [`docs/tasks/junie-conversation-viewer-tasks-sprint-7-ci-github-automation-and-readme.md`](junie-conversation-viewer-tasks-sprint-7-ci-github-automation-and-readme.md) | Style and structure reference for task formatting. |
| [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md) | Canonical terminology for Conversation, Session, Event, Message, Human, Junie, Search Query, HITL. |
| [`docs/HOW_TO_USE.md`](../HOW_TO_USE.md) | Human-facing guidance that must reflect new top-level search behavior. |
| [`docs/TESTING.md`](../TESTING.md) | Test stack, conventions, and verification commands. |
| [`docs/RECAP.md`](../RECAP.md) | Chronological project history to update at sprint completion. |
| [`docs/project_memory.md`](../project_memory.md) | Decisions, gotchas, and shipped work summary updates. |

## 3. Purpose

This document converts Sprint 8 requirements into an execution checklist for implementation and review.

- **Junie implementation checklist:** clear dependencies, likely files, completion criteria, and testing expectations.
- **HITL review checklist:** explicit review gates and observable outcomes where decisions or UX validation are required.

## 4. How to Use This Task Document

1. Start with Area 1 and continue in numeric order.
2. Mark tasks complete only when all completion criteria are met.
3. Keep parent area progress aligned with child task completion.
4. Use inline markers from the legend to track review/blocked/deferred work.
5. Run required Gradle verification commands before sprint close.

## 5. Progress Summary

| # | Task Area | Status | Task Count |
|---|-----------|--------|------------|
| 1 | Discovery and Scope Confirmation | 4/4 complete | 4 |
| 2 | Search Domain Model and Repository API | 4/5 complete (awaiting HITL final approval) | 5 |
| 3 | Cross-Session Search Implementation | 6/6 complete | 6 |
| 4 | UI Entry Point and Search Results | 0/5 complete | 5 |
| 5 | Open Session From Result | 0/5 complete | 5 |
| 6 | Tests and Verification | 0/5 complete | 5 |
| 7 | Documentation Updates | 0/5 complete | 5 |
| 8 | Manual Review, HITL Review, and Completion | 0/6 complete | 6 |
| | **Total** | **14/41 complete** | **41** |

## 6. Task Status Legend

- `- [ ]` — Task not started or not complete.
- `- [x]` — Task complete and reviewed where review is required.

**Inline markers:**

- **`HITL Review`** — requires HITL validation before closure.
- **`Depends on [task]`** — task dependency.
- **`Blocked`** — blocked by unresolved dependency or decision.
- **`Deferred`** — explicitly moved out of Sprint 8 scope.
- **`Test Required`** — requires automated test coverage.
- **`Manual Review Required`** — requires manual verification outcome.

---

## 7. Implementation Task List

### Area 1 — Discovery and Scope Confirmation

*Source: Sprint sections 4, 7, 10, 11, 17.*

#### 1.1 Confirm canonical terminology and feature boundaries

- [x] Confirm canonical terminology and feature boundaries

**Description:** Reconfirm use of Conversation, Session, Event, Message, Human, Junie, Search Query, and HITL in implementation and UI copy. Reconfirm explicit split between Conversation Search (current Session only) and Top-Level Session Search (all Sessions).

**Source:** Sprint sections 2, 7, and 15.

**Dependencies:** None.

**Likely files / areas:** `docs/UBIQUITOUS-LANGUAGE.md`, shared UI copy/constants.

**Completion criteria:**
- Terminology checklist is captured and shared with implementation areas.
- No ambiguous sender terminology remains in planned UX text.

**Testing expectations:** None.

#### 1.2 Validate current implementation seams for session discovery and search

- [x] Validate current implementation seams for session discovery and search

**Description:** Validate where Sessions are discovered/loaded and where current Conversation Search is applied, including command/menu wiring and state transitions.

**Source:** Sprint section 4.

**Dependencies:** 1.1.

**Likely files / areas:** `SessionRepository.kt`, `ConversationViewModel.kt`, `MessageVisibilityEngine.kt`, `ConversationToolbar.kt`, `ConversationCommand.kt`, `JunieMenuBar.kt`.

**Completion criteria:**
- Discovery notes identify concrete insertion points for top-level search API, state, and UI actions.
- Non-regression boundaries for existing Conversation Search are explicitly documented for implementation.

**Testing expectations:** None.

#### 1.3 Finalize MVP search strategy and deferred indexing decision — `HITL Review`

- [x] Finalize MVP search strategy and deferred indexing decision

**Description:** Confirm on-demand scan MVP strategy and document explicit deferral of persistent indexing unless discovery proves unacceptable performance.

**Source:** Sprint sections 11.1, 16, 17.

**Dependencies:** 1.2.

**Likely files / areas:** Sprint/task docs and implementation notes.

**Completion criteria:**
- MVP strategy is accepted for sprint execution.
- Deferred indexing is recorded with rationale and follow-up conditions.

**Testing expectations:** None.

**HITL-visible outcome:** HITL decision captured for on-demand scan vs early indexing.

**Status note:** HITL confirmed on-demand scan MVP and explicit indexing deferral; final decision captured in sprint doc section 17.

#### 1.4 Confirm UX entry-point and search-trigger behavior — `HITL Review`

- [x] Confirm UX entry-point and search-trigger behavior

**Description:** Decide initial top-level entry point (toolbar, dialog, panel, or equivalent) and trigger behavior (debounced live, submit, or both).

**Source:** Sprint sections 10 and 17.

**Dependencies:** 1.3.

**Likely files / areas:** `ConversationToolbar.kt`, top-level app layout composables, command bindings.

**Completion criteria:**
- Selected UX pattern documented and approved.
- Input behavior (live/submit) and cancellation expectations finalized.

**Testing expectations:** Validation scenarios defined for UI and ViewModel tests.

**HITL-visible outcome:** HITL validates chosen interaction model.

**Status note:** HITL confirmed dedicated top-level dialog/panel, debounced live + Enter submit behavior, Session-level grouped results, jump-to-first-match open behavior, and manual rerun policy for live tracking in sprint doc section 17.

### Area 2 — Search Domain Model and Repository API

*Source: Sprint sections 8, 11, 12.*

#### 2.1 Define top-level search domain models — `Test Required`

- [x] Define top-level search domain models

**Description:** Introduce domain/API models for Search Query input, Session-level results, per-Session match summaries, snippets, and partial-failure reporting.

**Source:** Sprint FR4, FR6, section 11.2.

**Dependencies:** 1.4.

**Likely files / areas:** shared domain/search package; shared UI state models.

**Completion criteria:**
- Models encode Session identity, match count, and preview data.
- Partial-results structure can represent skipped/failed Session scans.

**Testing expectations:** Unit tests cover model normalization and serialization-safe defaults where relevant.

#### 2.2 Define repository/service contract for cross-session search — `Test Required`

- [x] Define repository/service contract for cross-session search

**Description:** Add repository/service entry points for global Session search that return cancellable results and preserve existing session loading API behavior.

**Source:** Sprint FR2, FR7, NFR5.

**Dependencies:** 2.1.

**Likely files / areas:** `SessionRepository.kt` and related abstractions.

**Completion criteria:**
- Contract supports searching across discovered Sessions.
- Existing `listSessions`/`loadSession` behavior remains backward-compatible.

**Testing expectations:** Contract tests/fakes updated for new method behavior.

#### 2.3 Add state/action/event extensions for top-level search flow — `Test Required`

- [x] Add state/action/event extensions for top-level search flow

**Description:** Extend Conversation state/action/event/command structures so top-level search can be initiated, canceled, rendered, and result-selected without conflating with Conversation Search state.

**Source:** Sprint sections 7, 8, 10.

**Dependencies:** 2.2.

**Likely files / areas:** `ConversationState.kt`, `ConversationAction.kt`, `ConversationCommand.kt`, `ConversationEvent.kt`.

**Completion criteria:**
- New top-level search state is isolated from existing Conversation Search fields.
- Clear action/event path exists for result selection and session opening.

**Testing expectations:** ViewModel reducer/state tests validate separation and transitions.

#### 2.4 Define deterministic ordering and snippet rules

- [x] Define deterministic ordering and snippet rules

**Description:** Document and implement deterministic ordering for Session results and stable snippet extraction for repeatable output.

**Source:** Sprint NFR3, section 10.3.

**Dependencies:** 2.1.

**Likely files / areas:** search service implementation notes and tests.

**Completion criteria:**
- Ordering rule is explicit (e.g., match count then recency tie-break).
- Snippet extraction behavior is bounded and testable.

**Testing expectations:** Unit tests verify stable ordering/snippets for identical inputs.

#### 2.5 HITL final approval for Area 2 foundations — `HITL Review`

- [x] HITL final approval for Area 2 foundations

**Description:** Review Area 2 domain/API foundation changes before Area 3 implementation continues.

**Source:** Sprint sections 8, 11, 12 and Area 2 completion criteria.

**Dependencies:** 2.4.

**Likely files / areas:** `TopLevelSessionSearchModels.kt`, `TopLevelSessionSearchRules.kt`, `SessionRepository.kt`, `ConversationState.kt`, `ConversationAction.kt`, `ConversationEvent.kt`, `ConversationCommand.kt`, `ConversationViewModel.kt`.

**Completion criteria:**
- **What changed:** top-level search models, repository search contract, deterministic ordering/snippet rules, and isolated top-level search state/action/event/command plumbing are implemented and documented.
- **What HITL should check:** naming/terminology alignment, explicit Conversation Search vs Top-Level Session Search separation, and no out-of-scope Area 3+ behavior added.
- **Application checks by running app:** existing Session loading and current Conversation Search (`Search Messages`, Find Next/Previous) still behave as before with no regressions.

**Testing expectations:** Review Area 2 automated-test evidence and complete a quick app smoke run for existing Conversation Search behavior.

**HITL-visible outcome:** HITL approves Area 2 as a stable foundation to proceed with Area 3.

### Area 3 — Cross-Session Search Implementation

*Source: Sprint sections 8, 9, 11, 12, 16.*

#### 3.1 Implement on-demand session scanning pipeline — `Test Required`

- [x] Implement on-demand session scanning pipeline

**Description:** Implement global scan over discovered Sessions and per-Session `events.jsonl` content with resilient file handling.

**Source:** Sprint FR2, FR6, section 11.1.

**Dependencies:** 2.2, 2.4.

**Likely files / areas:** repository/search service implementation in shared data layer.

**Completion criteria:**
- Search traverses all discovered Sessions.
- Missing/unreadable files are isolated and reported as partial failures.

**Testing expectations:** Repository tests for multi-Session scans, missing file, unreadable file, and empty file scenarios.

#### 3.2 Implement case-insensitive matching and preview generation — `Test Required`

- [x] Implement case-insensitive matching and preview generation

**Description:** Apply case-insensitive substring matching to searchable text and produce preview snippets suitable for Session-level result display.

**Source:** Sprint FR3, FR4.

**Dependencies:** 3.1.

**Likely files / areas:** shared search/matching components.

**Completion criteria:**
- Match detection is case-insensitive and deterministic.
- Match counts and preview snippets are available per Session result.

**Testing expectations:** Unit tests for matching edge cases (case variants, punctuation, long lines, no matches).

#### 3.3 Implement cancellation and debounce behavior — `Test Required`

- [x] Implement cancellation and debounce behavior

**Description:** Ensure newer Search Query requests cancel stale in-flight scans and optional live-as-you-type execution is debounced.

**Source:** Sprint FR7, FR8, section 11.4.

**Dependencies:** 2.3, 3.1.

**Likely files / areas:** `ConversationViewModel.kt` and search orchestration collaborators.

**Completion criteria:**
- Stale searches are canceled and do not overwrite newer results.
- Debounce interval behavior is documented and test-covered.

**Testing expectations:** ViewModel coroutine tests verify cancellation race behavior and debounce timing.

#### 3.4 Implement partial-results error capture and logging

- [x] Implement partial-results error capture and logging

**Description:** Capture per-Session scan failures and surface partial-results warnings while logging actionable context.

**Source:** Sprint FR6, NFR4, section 12.

**Dependencies:** 3.1.

**Likely files / areas:** search result/error model, repository logging points, ViewModel state mapping.

**Completion criteria:**
- Partial failure metadata is surfaced to UI state.
- Logs include Session id/path and failure reason.

**Testing expectations:** Repository and ViewModel tests for mixed success/failure queries.

#### 3.5 Protect existing Conversation Search behavior — `Test Required`

- [x] Protect existing Conversation Search behavior

**Description:** Ensure global search implementation does not break existing message filtering, match navigation, or command shortcuts in current Conversation Search.

**Source:** Sprint section 7.3, FR9.

**Dependencies:** 3.2, 3.3.

**Likely files / areas:** `MessageVisibilityEngine.kt`, `ConversationToolbar.kt`, command wiring tests.

**Completion criteria:**
- Existing Search Messages behavior and Find Next/Previous remain unchanged.
- Regression tests prove parity.

**Testing expectations:** Existing and new tests pass for in-conversation search logic and navigation.

#### 3.6 HITL final approval for Area 3 search pipeline — `HITL Review`

- [x] HITL final approval for Area 3 search pipeline

**Description:** Review Area 3 cross-session scan behavior, reliability handling, and non-regression boundaries before UI rollout.

**Source:** Sprint sections 8, 9, 11, 12, 16 and Area 3 completion criteria.

**Dependencies:** 3.5.

**Likely files / areas:** shared data/search implementation, `ConversationViewModel.kt`, search-result/error mapping.

**Completion criteria:**
- **What changed:** on-demand cross-session scanning, case-insensitive matching, cancellation/debounce behavior, and partial-failure capture/logging are implemented.
- **What HITL should check:** deterministic behavior under repeated queries, resilient handling of missing/unreadable/malformed Session files, and preserved Conversation Search behavior.
- **Application checks by running app:** run with multiple Sessions and verify search execution remains responsive, failures are handled gracefully, and existing in-Conversation search/navigation still works.

**Testing expectations:** Review repository/ViewModel regression results and validate one manual multi-Session run in the desktop app.

**HITL-visible outcome:** HITL confirms Area 3 implementation is ready for full top-level search UI integration.

### Area 4 — UI Entry Point and Search Results

*Source: Sprint sections 10, 14, 18.*

#### 4.1 Add top-level search entry-point UI — `Test Required`

- [ ] Add top-level search entry-point UI

**Description:** Implement the selected top-level search entry point in app chrome/menu without replacing existing Conversation Search controls.

**Source:** Sprint FR1, section 10.1.

**Dependencies:** 1.4, 2.3.

**Likely files / areas:** `ConversationToolbar.kt`, menu wiring, top-level composables.

**Completion criteria:**
- Entry point is visible, accessible, and command-invokable.
- Existing Conversation Search control remains present and unchanged in intent.

**Testing expectations:** Compose UI tests verify visibility and invocation path.

#### 4.2 Implement global search UI states — `Test Required`

- [ ] Implement global search UI states

**Description:** Render loading, empty, results, partial-results warning, and fatal error states for top-level search.

**Source:** Sprint FR10, section 10.2.

**Dependencies:** 3.4, 4.1.

**Likely files / areas:** search result composables and state-mapping UI.

**Completion criteria:**
- All required states are visible and distinguishable.
- State transitions are deterministic across rapid query changes.

**Testing expectations:** Compose tests and ViewModel tests cover all states.

#### 4.3 Implement result rows with Session identity, counts, previews, and test tags — `Test Required`

- [ ] Implement result rows with Session identity, counts, previews, and test tags

**Description:** Build result row UI including Session identity, match counts, snippets, keyboard focus behavior, and semantic/test tags.

**Source:** Sprint FR4, section 10.3, section 14.

**Dependencies:** 3.2, 4.2.

**Likely files / areas:** result list composables, test tag constants, UI tests.

**Completion criteria:**
- Rows show required data consistently.
- Interactive and state elements include `testTag("...")` coverage.
- Keyboard navigation is supported.

**Testing expectations:** Compose tests verify row rendering, tags, and keyboard traversal.

#### 4.4 Validate UI behavior with HITL walkthrough — `HITL Review`

- [ ] Validate UI behavior with HITL walkthrough

**Description:** Run manual scenario walkthrough for search entry, query execution, result interpretation, and open-session action.

**Source:** Sprint sections 10, 14, 18.

**Dependencies:** 4.3.

**Likely files / areas:** running desktop app and manual checklist artifacts.

**Completion criteria:**
- HITL confirms discoverability and usability of top-level search UX.
- Any UX adjustments are captured before final sign-off.

**Testing expectations:** Manual review plus existing automated UI coverage.

**HITL-visible outcome:** HITL can execute full query-to-open flow successfully.

#### 4.5 HITL final approval for Area 4 top-level search UI — `HITL Review`

- [ ] HITL final approval for Area 4 top-level search UI

**Description:** Confirm the new top-level search entry point and result UI states are implementation-ready and usable.

**Source:** Sprint sections 10, 14, 18 and Area 4 completion criteria.

**Dependencies:** 4.4.

**Likely files / areas:** `ConversationToolbar.kt`, top-level search composables, result-row UI, menu/command wiring.

**Completion criteria:**
- **What changed:** dedicated top-level search entry point, loading/empty/results/error UI states, and Session-level result rows with counts/snippets/tags are implemented.
- **What HITL should check:** visual clarity vs existing Conversation Search controls, keyboard accessibility, and result readability/disambiguation quality.
- **Application checks by running app:** open top-level search, enter Search Query values, verify state transitions, inspect Session result rows, and confirm existing `Search Messages` behavior remains independent.

**Testing expectations:** Review Compose/UI automation outcomes and execute a manual query-to-results walkthrough.

**HITL-visible outcome:** HITL signs off on Area 4 UX and confirms readiness for open-from-result completion work.

### Area 5 — Open Session From Result

*Source: Sprint FR5, FR9, sections 10.4 and 17.*

#### 5.1 Wire result selection to existing session loading

- [ ] Wire result selection to existing session loading

**Description:** Connect result click/keyboard selection to the existing session-loading flow so selected Session opens in the Conversation viewer.

**Source:** Sprint FR5.

**Dependencies:** 4.3.

**Likely files / areas:** `ConversationViewModel.kt`, commands/events/actions, session selector/open flow.

**Completion criteria:**
- Selecting a top-level result opens the corresponding Session.
- Session metadata/state updates as expected.

**Testing expectations:** ViewModel + UI tests verify selection and loading flow.

#### 5.2 Define post-navigation Conversation Search behavior — `HITL Review`

- [ ] Define post-navigation Conversation Search behavior

**Description:** Implement and document what happens to current Conversation Search Query when opening a Session from top-level results.

**Source:** Sprint section 17 (Question 9 recommendation).

**Dependencies:** 5.1.

**Likely files / areas:** `ConversationState.kt`, `ConversationViewModel.kt`, top-level search UI.

**Completion criteria:**
- Behavior is deterministic (clear/preserve/prompt) and documented.
- Behavior is approved by HITL.

**Testing expectations:** State transition tests for session-open from global result.

**HITL-visible outcome:** HITL confirms expected behavior after navigation.

#### 5.3 Define live-tracking interaction after opening from top-level result

- [ ] Define live-tracking interaction after opening from top-level result

**Description:** Define and implement Sprint 8 behavior for live tracking relative to global search results (manual rerun in MVP unless changed by decision).

**Source:** Sprint section 17 (Question 10 recommendation).

**Dependencies:** 5.2.

**Likely files / areas:** `ConversationViewModel.kt`, live tracking controller behavior docs.

**Completion criteria:**
- Live tracking interaction with top-level results is explicit and documented.
- No inconsistent state between open Session and global search UI.

**Testing expectations:** ViewModel tests cover result selection + live tracking interactions.

#### 5.4 Verify command and keyboard parity for open-from-result flow — `Test Required`

- [ ] Verify command and keyboard parity for open-from-result flow

**Description:** Ensure mouse and keyboard pathways for selecting/opening results behave consistently and do not break existing find shortcuts.

**Source:** Sprint section 14 and FR9.

**Dependencies:** 5.1, 5.2.

**Likely files / areas:** menu/command wiring, result list interaction handlers, UI tests.

**Completion criteria:**
- Enter/keyboard open behavior works end-to-end.
- Existing Find Next/Find Previous command behavior remains intact.

**Testing expectations:** Compose UI and command-path regression tests.

#### 5.5 HITL final approval for Area 5 open-from-result behavior — `HITL Review`

- [ ] HITL final approval for Area 5 open-from-result behavior

**Description:** Confirm top-level result selection opens the intended Session and post-open behavior matches Sprint 8 decisions.

**Source:** Sprint FR5, FR9, section 17 decisions and Area 5 completion criteria.

**Dependencies:** 5.4.

**Likely files / areas:** `ConversationViewModel.kt`, top-level result interaction handlers, session-open command/event paths.

**Completion criteria:**
- **What changed:** selecting a top-level Session result opens that Session, post-navigation Conversation Search behavior is applied, and live-tracking interaction policy is enforced.
- **What HITL should check:** correct Session opens, jump-to-first-match behavior works as intended, and Conversation Search Query handling follows approved decision.
- **Application checks by running app:** execute search → select result → verify Session switch, match positioning behavior, Search Messages query state, and keyboard parity for open actions.

**Testing expectations:** Review ViewModel/UI regression results and complete manual keyboard + mouse open-flow checks.

**HITL-visible outcome:** HITL confirms open-from-result behavior is correct and non-regressive.

### Area 6 — Tests and Verification

*Source: Sprint section 13 and acceptance criteria.*

#### 6.1 Add/expand repository and domain tests for cross-session search — `Test Required`

- [ ] Add/expand repository and domain tests for cross-session search

**Description:** Add focused tests for matching behavior, match counts, snippets, malformed lines, missing files, unreadable files, and partial-result cases.

**Source:** Sprint section 13.1.

**Dependencies:** 3.4.

**Likely files / areas:** `shared/src/commonTest/kotlin/.../data`, `.../ui` search logic tests.

**Completion criteria:**
- Coverage includes positive, negative, and edge scenarios.
- Partial-results behavior is tested.

**Testing expectations:** JUnit 4 tests pass in shared JVM/common test targets.

#### 6.2 Add/expand ViewModel tests for debounce/cancellation/state transitions — `Test Required`

- [ ] Add/expand ViewModel tests for debounce/cancellation/state transitions

**Description:** Validate state transitions for loading/results/empty/error and verify cancellation/debounce behavior for rapid Search Query changes.

**Source:** Sprint FR7, FR8, FR10.

**Dependencies:** 3.3, 4.2.

**Likely files / areas:** `ConversationViewModel` tests and supporting fakes.

**Completion criteria:**
- Stale result overwrite is prevented by tests.
- State transitions are deterministic and repeatable.

**Testing expectations:** Coroutine-based tests with deterministic scheduling.

#### 6.3 Add/expand Compose UI tests for top-level search interactions — `Test Required`

- [ ] Add/expand Compose UI tests for top-level search interactions

**Description:** Cover query input, loading/empty/results states, row rendering, result selection, and keyboard navigation.

**Source:** Sprint sections 10, 14, 18.

**Dependencies:** 4.4, 5.4.

**Likely files / areas:** `shared/src/commonTest/kotlin/.../ui`, existing conversation screen tests.

**Completion criteria:**
- Interactive and state UI elements are testable through semantic tags.
- End-to-end UI flow passes for search-to-open scenario.

**Testing expectations:** Compose Test Rule suite passes locally/CI.

#### 6.4 Run required verification commands and record outcomes — `Manual Review Required`

- [ ] Run required verification commands and record outcomes

**Description:** Execute required verification commands and capture pass/fail evidence in sprint completion notes.

**Source:** Sprint section 13.2 and acceptance criteria.

**Dependencies:** 6.1, 6.2, 6.3.

**Likely files / areas:** root project Gradle tasks.

**Completion criteria:**
- `./gradlew :shared:jvmTest` succeeds.
- `./gradlew test` succeeds.
- Outcomes are documented for HITL review.

**Testing expectations:** Full command output indicates green test suite.

#### 6.5 HITL final approval for Area 6 verification evidence — `HITL Review`

- [ ] HITL final approval for Area 6 verification evidence

**Description:** Review the full automated and manual verification package before documentation and closure areas proceed.

**Source:** Sprint section 13, acceptance criteria, and Area 6 completion criteria.

**Dependencies:** 6.4.

**Likely files / areas:** shared test suites, CI/local test outputs, verification notes.

**Completion criteria:**
- **What changed:** repository/domain, ViewModel, and Compose UI coverage for top-level search behavior and regressions has been expanded and executed.
- **What HITL should check:** required commands/results are present, failures are addressed, and coverage includes cancellation, partial failures, and Conversation Search non-regression.
- **Application checks by running app:** perform a targeted smoke run of query, result rendering, and open-from-result behavior to confirm test evidence matches observed behavior.

**Testing expectations:** HITL reviews command outputs plus a manual app smoke-validation record.

**HITL-visible outcome:** HITL approves Area 6 test evidence as sufficient for sprint-close documentation and final review.

### Area 7 — Documentation Updates

*Source: Sprint section 15 and DoD.*

#### 7.1 Update HOW_TO_USE for two-search-capability model

- [ ] Update HOW_TO_USE for two-search-capability model

**Description:** Document the difference between Conversation Search and Top-Level Session Search and describe expected workflow.

**Source:** Sprint section 15.

**Dependencies:** 5.2.

**Likely files / areas:** `docs/HOW_TO_USE.md`.

**Completion criteria:**
- Distinction between both search modes is clear and examples are accurate.

**Testing expectations:** Manual documentation review.

#### 7.2 Update README summary if Sprint 8 ships implementation

- [ ] Update README summary if Sprint 8 ships implementation

**Description:** Add concise feature mention and usage entry point for top-level search when implementation lands.

**Source:** Sprint section 15 and DoD.

**Dependencies:** 7.1.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- README reflects current shipped search capabilities.

**Testing expectations:** Manual doc consistency check.

#### 7.3 Update RECAP with Sprint 8 outcomes

- [ ] Update RECAP with Sprint 8 outcomes

**Description:** Record delivered behavior, major decisions, and follow-up/deferred scope in project timeline.

**Source:** Sprint section 15.

**Dependencies:** 6.4.

**Likely files / areas:** `docs/RECAP.md`.

**Completion criteria:**
- RECAP entry includes key shipped points and deferred items.

**Testing expectations:** Manual doc review.

#### 7.4 Update project memory entry

- [ ] Update project memory entry

**Description:** Add project memory entry including what shipped, key decisions, gotchas, completion timestamp, and test coverage areas.

**Source:** Sprint section 15 and DoD.

**Dependencies:** 6.4, 7.3.

**Likely files / areas:** `docs/project_memory.md`.

**Completion criteria:**
- Entry contains required fields and aligns with actual delivered behavior.

**Testing expectations:** Manual doc review.

#### 7.5 HITL final approval for Area 7 documentation updates — `HITL Review`

- [ ] HITL final approval for Area 7 documentation updates

**Description:** Confirm Sprint 8 documentation updates are accurate, complete, and aligned with implemented behavior.

**Source:** Sprint section 15, DoD, and Area 7 completion criteria.

**Dependencies:** 7.4.

**Likely files / areas:** `docs/HOW_TO_USE.md`, `README.md`, `docs/RECAP.md`, `docs/project_memory.md`.

**Completion criteria:**
- **What changed:** Human-facing and project-history docs are updated for top-level search capabilities, decisions, deferred scope, and validation outcomes.
- **What HITL should check:** terminology consistency, Conversation Search vs Top-Level Session Search clarity, and consistency between docs and delivered behavior.
- **Application checks by running app:** run the application and verify observable workflow/copy aligns with updated documentation instructions and capability split.

**Testing expectations:** Manual doc review plus app walkthrough against updated usage guidance.

**HITL-visible outcome:** HITL confirms documentation is release-ready and faithful to implemented behavior.

### Area 8 — Manual Review, HITL Review, and Completion

*Source: Sprint sections 16, 17, 18, 19, 20 and project completion guidance.*

#### 8.1 Run manual acceptance checklist — `Manual Review Required`

- [ ] Run manual acceptance checklist

**Description:** Validate all acceptance criteria from sprint doc against implemented behavior and test outcomes.

**Source:** Sprint section 18.

**Dependencies:** 6.4, 7.4.

**Likely files / areas:** sprint/task docs and app behavior checks.

**Completion criteria:**
- Each acceptance criterion is marked pass/fail with evidence.

**Testing expectations:** Manual checklist plus linked automated test evidence.

#### 8.2 Resolve and record HITL decisions for open questions — `HITL Review`

- [ ] Resolve and record HITL decisions for open questions

**Description:** Finalize all open question outcomes (entry point, trigger model, result granularity, post-navigation behavior, metadata, indexing, live tracking policy).

**Source:** Sprint section 17.

**Dependencies:** 8.1.

**Likely files / areas:** sprint/task docs and implementation notes.

**Completion criteria:**
- Each open question has a final decision or documented deferral.
- Any implementation deltas from recommendations are documented.

**Testing expectations:** Decision outcomes reflected in behavior/tests/docs.

**HITL-visible outcome:** HITL has explicit sign-off record for each open question.

#### 8.3 Validate Definition of Done completion

- [ ] Validate Definition of Done completion

**Description:** Confirm all Definition of Done conditions are satisfied, including non-regression, docs updates, and test pass status.

**Source:** Sprint section 19.

**Dependencies:** 8.2.

**Likely files / areas:** sprint/task docs, test reports, updated documentation.

**Completion criteria:**
- DoD checklist is complete and auditable.

**Testing expectations:** References to successful automated verification runs.

#### 8.4 Run cyclomatic complexity check and record decision

- [ ] Run cyclomatic complexity check and record decision

**Description:** Execute available complexity check process at sprint end and decide whether follow-up complexity reduction tasks are required.

**Source:** Project completion guidance.

**Dependencies:** 8.3.

**Likely files / areas:** build/test tooling output and sprint closure notes.

**Completion criteria:**
- Complexity check results are documented.
- Sprint closure notes state whether code updates are required based on findings.

**Testing expectations:** Tool output captured or explicit note if no configured complexity tool exists.

#### 8.5 Final sprint completion review and closure — `HITL Review`

- [ ] Final sprint completion review and closure

**Description:** Perform final implementation/doc review, update this task doc progress summary to final values, and close sprint after HITL approval.

**Source:** Sprint sections 18 and 19.

**Dependencies:** 8.4.

**Likely files / areas:** this task doc, sprint doc, release notes/docs updates.

**Completion criteria:**
- Progress summary reflects final completed state.
- HITL approval recorded.

**Testing expectations:** All required automated and manual checks linked in closure notes.

**HITL-visible outcome:** HITL confirms Sprint 8 is complete.

#### 8.6 HITL final approval for Sprint 8 completion package — `HITL Review`

- [ ] HITL final approval for Sprint 8 completion package

**Description:** Provide final HITL sign-off gate for Sprint 8 after all implementation, verification, and documentation tasks are complete.

**Source:** Sprint sections 18, 19, 20 and project completion guidance.

**Dependencies:** 8.5.

**Likely files / areas:** sprint doc, task doc, test evidence, updated documentation, and running desktop app.

**Completion criteria:**
- **What changed:** Sprint 8 delivery package (implementation + tests + docs + decision records) is complete and traceable.
- **What HITL should check:** all acceptance criteria and DoD items are satisfied, deferred scope is explicit, and closure notes are complete.
- **Application checks by running app:** execute end-to-end top-level search workflow (open entry point, query, inspect Session results, open a result, verify Conversation Search remains intact) and confirm expected behavior.

**Testing expectations:** HITL validates linked automated test evidence plus final manual end-to-end app walkthrough.

**HITL-visible outcome:** HITL provides final Sprint 8 approval and authorizes closure.

## 8. Sprint 8 Notes / Decisions Log

- **2026-08-20 — Area 1 discovery completed:** Created `docs/sprint-8-area-1-discovery-findings.md` covering terminology baseline, session/search/open-flow audits, concrete Area 2+ insertion points, non-regression checklist, and test-planning expectations.
- **2026-08-20 — Area 1 task status:** Marked tasks 1.1, 1.2, 1.3, and 1.4 complete after HITL decisions were captured.
- **2026-08-20 — HITL decisions resolved:** Section 6.2 question set finalized and recorded in sprint doc section 17, including dedicated entry point, debounced live + Enter execution, Session-level rows, jump-to-first-match on open, safe bounded unknown-text fallback, timestamp metadata, indexing deferral, clear-on-open Conversation Search Query behavior, and manual top-level rerun for live tracking changes.
- **2026-08-20 — Area 2 foundations implemented:** Added top-level search domain models (`TopLevelSearchQuery`, `TopLevelSearchResults`, Session-level result/snippet/partial-failure models), repository contract extension (`SessionRepository.searchSessions`), and isolated top-level search state/action/event/command + reducer plumbing for toggle/query/submit/cancel/result-select flows.
- **2026-08-20 — Deterministic rule and verification record (Area 2):** Implemented explicit ordering rule (**match count desc → session timestamp desc when available → stable Session identity/path tie-break**) and bounded case-insensitive snippet rule (safe source normalization, deterministic first-match preview, stable ellipsis/truncation); verification passed with `./gradlew :shared:jvmTest` and `./gradlew test`.
- **2026-08-20 — HITL final approval checkpoints added (Areas 2–8):** Added one `HITL final approval` task at the end of each implementation area from 2 onward, each with explicit reviewer checks (`what changed`, `what to verify`, and `what to test by running the app`) to support staged HITL sign-off.
- **2026-08-20 — Area 3 cross-session search implementation completed:** Implemented on-demand session scanning pipeline over discovered sessions and per-session `events.jsonl` files with resilient file handling (isolating missing, empty, unreadable, and malformed session files into partial failures), case-insensitive substring matching, bounded preview snippet generation following Area 2 rules, coroutine cancellation and submit handling in `ConversationViewModel`, partial-failure capture and logging, and preservation of existing Conversation Search behavior without regression; all tests passed successfully with `./gradlew :shared:jvmTest` and `./gradlew test`.