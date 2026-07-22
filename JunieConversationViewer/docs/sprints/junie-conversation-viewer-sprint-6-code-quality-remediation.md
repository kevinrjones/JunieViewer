---
sprint: 6
name: Code Quality Remediation
status: planned
---

# 1. Title

Sprint 6 — Code Quality Remediation

# 2. Related Documents

- [`docs/sprints/junie-conversation-viewer-sprint-5-toolbar-menu-and-navigation-controls.md`](junie-conversation-viewer-sprint-5-toolbar-menu-and-navigation-controls.md) — the preceding sprint; Sprint 6 remediates structural debt identified after its completion.
- [`docs/tasks/junie-conversation-viewer-tasks-sprint-6-code-quality-remediation.md`](../tasks/junie-conversation-viewer-tasks-sprint-6-code-quality-remediation.md) — the companion task breakdown for this sprint.
- [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md) — canonical domain terms used consistently in code, tests, and UI copy.
- [`docs/RECAP.md`](../RECAP.md) — chronological project history.
- [`docs/TESTING.md`](../TESTING.md) — testing stack, Robot pattern, semantic `testTag` conventions, and Gradle commands.
- [`docs/project_memory.md`](../project_memory.md) — decisions, gotchas, and shipped work.

# 3. Sprint Goal

Resolve all twelve findings from the whole-codebase "thermo-nuclear" code quality review: eliminate the triple-duplicated `MessageKind` dispatch, remove `JsonElement` leakage from the domain model, collapse the parallel event registries, centralize expansion state, extract Markdown parsing and desktop entry-point concerns into focused components, fix repository and state-update smells, stabilize message IDs, and restructure the test suite around shared infrastructure — all while preserving existing behaviour and keeping every test green.

# 4. Current Baseline

## 4.1 Review Findings Summary

A strict maintainability review (2026-07-20) produced 12 findings:

- **Blockers (structural):** F1 triple-duplicated `MessageKind` dispatch; F2 `JsonElement` leaked into domain model; F3 parallel event registries (Open/Closed violation).
- **High priority:** F4 split expansion state ownership; F5 Markdown parsing inside UI layer; F6 `main.kt` "junk drawer" (333 lines); F7 repository bypasses its own parser; F8 non-atomic state updates on search.
- **Worth fixing:** F9 unstable message IDs (`hashCode()` fallback + `-live-` prefix patching); F10 ungrouped turn derivation in composition; F11 test-suite copy-paste architecture; F12 version catalog bypass for `slf4j-api`.

## 4.2 Dispatch State

- `ui/components/MessageItems.kt` (~L359–472 and ~L513–603) and `ui/ConversationViewModel.kt` (~L413–436) each contain exhaustive `when` chains over `MessageKind` answering "what does this kind render / is it collapsible / what text is searchable".
- Adding one new Message Kind requires edits in three distant blocks.

## 4.3 Domain/Data State

- `domain/AgentEvents.kt`: 20+ event types carry raw `JsonElement`/`JsonObject` payloads.
- `data/EventToMessageMapper.kt` (~220 lines) navigates JSON manually (`jsonObject["field"]?.jsonPrimitive?.content`) and contains a large `when` over event types.
- `domain/EventSerializers.kt` holds a parallel map-based serializer registry.
- `data/SessionRepository.extractWorkingDirectory` (~L172–204) re-implements a mini JSONL scanner instead of using `JsonlParser`.
- `EventToMessageMapper` falls back to `content.hashCode()` for Message IDs; `LiveSessionTracker` patches collisions with a `-live-` prefix (~L79–82).

## 4.4 UI/ViewModel State

- Expansion state has two owners: ViewModel `blockExpansionStates` for manual toggles, and UI-side `rememberMessageExpansionState` (`MessageItems.kt` ~L606+) for search force-expansion.
- `MarkdownContent.kt` (~L114–307) performs manual index-walking Markdown parsing inside composables and maintains a bespoke `applySearchHighlight` despite `SearchHighlight.kt` existing.
- Search-query changes perform two consecutive `_state.update` calls (`ConversationViewModel.kt` ~L126–129).
- `groupMessagesIntoTurns(state.filteredMessages)` runs unremembered on every recomposition (`ConversationScreen.kt` ~L310).

## 4.5 Desktop Entry Point State

- `main.kt` (333 lines) mixes logging setup, a Swing `JOptionPane` exception dialog, window-state persistence (~L92–104), a 115-line `MenuBar`, and an AWT synthetic-copy-event hack (~L260–301).

## 4.6 Build and Test State

- `gradle/libs.versions.toml` hardcodes `slf4j-api = 2.0.13` inline instead of using a `version.ref`.
- `JsonlParserTest.kt` (603 lines) is a wall of near-identical hardcoded-JSON tests.
- 14+ UI test files each hand-wire `PreferencesRepository` + `ConversationViewModel` + `ConversationRobot`.
- All tests currently pass (`./gradlew test`, `./gradlew :shared:jvmTest`).

# 5. Design Findings

## 5.1 The Shared Code-Judo Move

Findings F1, F2, and F3 share one restructuring: **make each event/message kind self-describing**. Typed event payloads (F2) enable each event to map itself to a Message (F3, Strategy/Visitor — both pre-blessed patterns), and a `MessageContentRegistry` keyed by `MessageKind` (F1) removes the three UI/ViewModel `when` chains. Executing this once resolves all three blockers.

## 5.2 Ordering Constraint

F2 (typed payloads) must land before F3 (self-mapping events), which must land before or alongside F1 (registry), because the registry's searchable-text extractors depend on typed content. F9 (stable IDs) is best fixed inside the same mapper rework. F11 (test DSL) should land early so refactored areas are re-verified through the new shared test infrastructure.

## 5.3 Behaviour-Preservation Principle

This is a refactoring sprint: no user-visible behaviour change is intended. The existing test suite is the safety net; each area must end with a green build before the next begins.

# 6. Scope

- **Typed Domain Events:** Replace `JsonElement`/`JsonObject` payloads in `AgentEvents.kt` with concrete data classes / sealed hierarchies parsed at the deserialization boundary.
- **Self-Mapping Events:** Collapse `EventToMessageMapper`'s `when` block by pushing mapping into the events (Strategy/Visitor), so adding an event touches one place.
- **Message Content Registry:** A single registry mapping each `MessageKind` to its default collapsibility, searchable-text extractor, and renderer composable.
- **Centralized Expansion State:** ViewModel-derived "is expanded" per block, folding search force-expansion into state derivation.
- **Extracted Markdown Parser:** A non-UI `MarkdownDocument` parser; composables iterate blocks and reuse the canonical `SearchHighlight` helper.
- **Decomposed Entry Point:** `JunieMenuBar`, `DesktopClipboardManager`, `WindowStateTracker`, and logging setup extracted; `main.kt` becomes pure wiring.
- **Repository/State Hygiene:** `extractWorkingDirectory` via `JsonlParser`; atomic search-state updates; stable Message IDs; turn grouping moved into ViewModel derivation.
- **Test Infrastructure:** `runConversationUiTest { }` DSL, parameterized parser tests, consolidated UI integration files.
- **Build Hygiene:** `slf4j` version moved into `[versions]` with a `version.ref`.

# 7. Out of Scope

- Any user-visible feature additions or UI redesign.
- Introducing Arrow `Either` across layers that don't already use it (deferred to a future sprint).
- Replacing the bespoke Markdown parser with a third-party library.
- Modularizing `shared` into further Gradle modules.
- Performance work beyond the identified recomposition fix (F10).
- Consolidating `desktopApp`/`shared` dependency declarations into convention plugins.

# 8. User Stories

- As a **developer**, I can add a new agent event by touching a single place, because event definition, serialization, and mapping are co-located.
- As a **developer**, I can add a new Message Kind by registering one descriptor, because rendering, collapsibility, and searchable text are defined together.
- As a **developer**, I can reason about why a block is expanded by reading one ViewModel derivation, because expansion state has a single owner.
- As a **developer**, I can change the Markdown rendering safely, because parsing is a tested non-UI component.
- As a **developer**, I can write a new UI test in a few lines, because shared test infrastructure handles wiring and cleanup.
- As a **HITL**, I see identical application behaviour before and after the sprint, because this is a behaviour-preserving refactor verified by the existing suite.

# 9. Functional Requirements

- **FR1:** All `AgentEvent` subclasses expose typed payload properties; no `JsonElement`/`JsonObject` remains in `domain/`.
- **FR2:** Each `AgentEvent` maps itself to a Message (or explicitly to "no message") via a single polymorphic call; `EventToMessageMapper`'s event-type `when` block is removed.
- **FR3:** A `MessageContentRegistry` provides, per `MessageKind`: default collapsibility, searchable-text extraction, and the renderer; the three existing `when` chains are deleted.
- **FR4:** Block expansion (manual toggles, Collapse All/Show All, search force-expansion) is derived in the ViewModel; `rememberMessageExpansionState` is removed.
- **FR5:** Markdown parsing produces a `MarkdownDocument` model in a non-UI package; `MarkdownContent.kt` only renders it and uses the canonical `SearchHighlight` helper.
- **FR6:** `main.kt` contains only wiring; menu, clipboard hack, window-state persistence, and logging setup live in dedicated components.
- **FR7:** `SessionRepository.extractWorkingDirectory` uses `JsonlParser` and typed events.
- **FR8:** Search-query changes produce exactly one state emission; `filterMessages` becomes a pure function.
- **FR9:** Message IDs are stable across live tracking and full reloads; the `-live-` prefix patching is removed.
- **FR10:** Turn grouping is derived in the ViewModel (or memoized), not recomputed on every recomposition.
- **FR11:** A `runConversationUiTest { }` DSL exists; `JsonlParserTest` is parameterized/table-driven; overlapping UI integration files are consolidated.
- **FR12:** `libs.versions.toml` references `slf4j` via `version.ref`.

# 10. Non-Functional Requirements

- **NFR1:** No user-visible behaviour change: rendering, filtering, search, sorting, expansion, and live tracking behave identically.
- **NFR2:** Every existing test passes after each delivery part; no test is deleted, ignored, or weakened to force a pass.
- **NFR3:** Adding a new event or Message Kind after the sprint requires touching at most one production file per concern.
- **NFR4:** New non-UI components (Markdown parser, registries, trackers) are unit-testable without Compose.
- **NFR5:** Total UI test suite runtime does not regress materially after consolidation.
- **NFR6:** Inheritance stays capped at one level; composition is preferred throughout the rework.

# 11. Design Principles

1. **One Fact, One Owner.** Every piece of knowledge (collapsibility, searchable text, expansion, mapping) has exactly one authoritative definition.
2. **Typed Boundaries.** Raw JSON stops at the deserialization boundary; the domain speaks only typed models.
3. **Open/Closed via Registries.** New kinds/events are added by registration, not by editing dispatch chains.
4. **Behaviour Preservation.** Refactor under green tests; characterization tests are added before risky moves where coverage is thin.
5. **UI Renders, It Does Not Parse.** Parsing and derivation live below the UI layer.
6. **Pre-Blessed Patterns Only.** Strategy/Adapter/Command as per project guidelines; no speculative abstraction.
7. **Small Steps, Green Builds.** Each delivery part ends with a full green `check` before the next begins.

# 12. Proposed Changes — Typed Domain Events (F2)

- Replace every `JsonElement`/`JsonObject` field in `AgentEvents.kt` with concrete data classes or sealed hierarchies (e.g., typed `AskRequest`, `ChoiceRequest`, result-block payloads).
- Parse payloads fully at the deserialization boundary using the existing `EventSerializers.kt` registry.
- Where a payload's structure is genuinely open-ended, model it explicitly (e.g., a typed key/value structure), never as a serialization-library type.
- Move ask/choice question-and-option extraction out of the mapper into the typed models.

**Files:** `domain/AgentEvents.kt`, `domain/EventSerializers.kt`, `data/EventToMessageMapper.kt`, `data/SessionRepository.kt`, related tests.

# 13. Proposed Changes — Self-Mapping Events (F3)

- Introduce a polymorphic mapping operation on `AgentEvent` (Strategy or Visitor — HITL to choose, see Q1) so each event produces its own Message (or none).
- Collapse `EventToMessageMapper` to orchestration only (ordering, ID assignment); delete its 220-line `when` block.
- Ensure adding a new event requires touching only the event definition plus its serializer registration.

**Files:** `domain/AgentEvents.kt`, `data/EventToMessageMapper.kt`, `domain/EventSerializers.kt`, related tests.

# 14. Proposed Changes — Message Content Registry (F1)

- Create a `MessageContentRegistry` (or descriptor per `MessageKind`) holding: default collapsibility, searchable-text extractor, and renderer composable reference.
- Delete the two exhaustive `when` chains in `MessageItems.kt` and the one in `ConversationViewModel.kt`; both layers consult the registry.
- Decompose `MessageItems.kt` (653 lines) into focused files (`MessageKindMarker.kt`, `TurnHeader.kt`, `ExpansionState.kt`, per-kind renderers) as part of the move.

**Files:** `ui/components/MessageItems.kt` (split), new `ui/components/renderers/`, `ui/ConversationViewModel.kt`, related tests.

# 15. Proposed Changes — Centralized Expansion State (F4)

- Move search force-expansion into ViewModel state derivation: the ViewModel emits the final "is expanded" boolean per block.
- Remove `rememberMessageExpansionState`; the UI reads derived state and dispatches toggle actions only.
- Preserve Sprint 5 semantics: manual toggle, Collapse All/Show All, and search force-expansion priority (`manualExpanded || (forceExpanded && !userDismissedForce)`).

**Files:** `ui/ConversationViewModel.kt`, `ui/components/MessageItems.kt` (or its split successors), `CollapseShowAllTest.kt`.

# 16. Proposed Changes — Markdown Parser Extraction (F5)

- Extract the index-walking parsing from `MarkdownContent.kt` into a non-UI `MarkdownDocument` parser (e.g., `markdown/MarkdownParser.kt`) returning a typed block/inline model.
- The composable iterates the parsed model; no `substring`/`indexOf` walking remains in UI code.
- Delete the bespoke `applySearchHighlight` and reuse the canonical `SearchHighlight.kt` helper.
- Add direct unit tests for the parser covering existing rendering behaviour.

**Files:** new `markdown/` package, `ui/components/MarkdownContent.kt`, `SearchHighlight.kt`, new `MarkdownParserTest.kt`.

# 17. Proposed Changes — Entry Point Decomposition (F6)

- Extract the 115-line `MenuBar` into a `JunieMenuBar` composable.
- Extract the AWT synthetic-copy-event hack into a `DesktopClipboardManager` behind a small abstraction; `main.kt` knows only the command.
- Extract window-state persistence into a `WindowStateTracker` collaborating with `PreferencesRepository`.
- Move logging setup and the exception dialog into a dedicated `logging`/startup component.
- `main.kt` becomes pure wiring (target: well under 100 lines).

**Files:** `desktopApp/.../main.kt`, new `JunieMenuBar.kt`, `DesktopClipboardManager.kt`, `WindowStateTracker.kt`, logging setup file.

# 18. Proposed Changes — Repository, State, and ID Hygiene (F7, F8, F9, F10)

- **F7:** Reimplement `extractWorkingDirectory` on top of `JsonlParser` + typed events; delete the mini string scanner.
- **F8:** Make `filterMessages` pure (returns new state) and fold search-query changes into one atomic `_state.update`.
- **F9:** Derive stable Message IDs (e.g., from file line offset or first-parse assignment) in the mapper; remove `content.hashCode()` fallback and the `-live-` prefix patching in `LiveSessionTracker`.
- **F10:** Move `groupMessagesIntoTurns` into ViewModel state derivation (preferred) or wrap in `remember(state.filteredMessages)`.

**Files:** `data/SessionRepository.kt`, `ui/ConversationViewModel.kt`, `data/EventToMessageMapper.kt`, `data/LiveSessionTracker.kt`, `ui/ConversationScreen.kt`, related tests.

# 19. Proposed Changes — Test Infrastructure (F11)

- Build a `runConversationUiTest { }` DSL that wires temp directory, `PreferencesRepository`, `ConversationViewModel`, and `ConversationRobot`, with automatic cleanup.
- Migrate the 14+ UI test files to the DSL; consolidate overlapping integration files into fewer cohesive suites without losing any assertion.
- Refactor `JsonlParserTest.kt` (603 lines) into parameterized/table-driven tests, optionally generating JSON via the real serializers to verify symmetry.

**Files:** new `ui/ConversationUiTestDsl.kt` (or similar), all `shared/src/commonTest/**` UI test files, `data/JsonlParserTest.kt`.

# 20. Proposed Changes — Build Hygiene (F12)

- Add `slf4j = "2.0.13"` to `[versions]` in `libs.versions.toml` and switch `slf4j-api` to `version.ref = "slf4j"`.

**Files:** `gradle/libs.versions.toml`.

# 21. Testing Strategy

## 21.1 Automated Tests

- **Characterization first:** Before each blocker rework (F1–F3), confirm or add tests capturing current mapping/rendering/searchable-text behaviour per event type and Message Kind.
- **Parser tests:** New `MarkdownParserTest` covering all block/inline forms currently rendered.
- **ID stability tests:** Live-append followed by full reload yields identical Message IDs.
- **Atomicity tests:** A search-query change produces exactly one state emission.
- **Expansion tests:** `CollapseShowAllTest` passes unchanged (or with mechanical updates only) against the centralized model.
- **Regression gates:** `./gradlew :shared:jvmTest` and `./gradlew test` after every delivery part; `./gradlew check` at sprint end.

## 21.2 Manual Review Checklist

- **Visual parity:** All Message Kinds render identically in Light and Dark themes before/after.
- **Search flow:** Highlighting, match navigation, and force-expansion of collapsed matching blocks unchanged.
- **Live tracking:** Appending to an active `events.jsonl` shows no flicker or lost expansion state.
- **Menus and shortcuts:** All Sprint 5 menu items and shortcuts still work after `main.kt` decomposition.
- **Window state:** Size/position persistence across restarts still works via `WindowStateTracker`.

# 22. Incremental Delivery Plan

## Part 1 — Discovery, Characterization, and Quick Wins
- **Objective:** Confirm review findings against current code, land F12 (version catalog), F8 (atomic updates), F10 (turn grouping), and F7 (repository parser reuse); add characterization tests where coverage is thin.
- **Files:** `libs.versions.toml`, `ConversationViewModel.kt`, `ConversationScreen.kt`, `SessionRepository.kt`.
- **After:** *Low-risk fixes are shipped and the safety net for the structural work is in place.*

## Part 2 — Test Infrastructure (F11)
- **Objective:** Build the `runConversationUiTest { }` DSL, migrate UI tests, and parameterize `JsonlParserTest`.
- **Files:** `shared/src/commonTest/**`.
- **After:** *All subsequent refactors are verified through concise shared test infrastructure.*

## Part 3 — Typed Domain Events (F2)
- **Objective:** Replace all `JsonElement` payloads with typed models parsed at the boundary.
- **Files:** `AgentEvents.kt`, `EventSerializers.kt`, `EventToMessageMapper.kt`, `SessionRepository.kt`.
- **After:** *The domain model is fully typed; stringly JSON navigation is gone.*

## Part 4 — Self-Mapping Events and Stable IDs (F3, F9)
- **Objective:** Push event→Message mapping into the events; derive stable IDs; remove `-live-` patching.
- **Files:** `AgentEvents.kt`, `EventToMessageMapper.kt`, `LiveSessionTracker.kt`.
- **After:** *Adding an event touches one place, and Message identity is stable across reloads.*

## Part 5 — Message Content Registry (F1)
- **Objective:** Introduce the registry; delete the three `when` chains; split `MessageItems.kt`.
- **Files:** `MessageItems.kt` (split), `renderers/`, `ConversationViewModel.kt`.
- **After:** *Message Kind behaviour is defined once and consumed everywhere.*

## Part 6 — Centralized Expansion State (F4)
- **Objective:** Fold search force-expansion into ViewModel derivation; remove UI-side expansion state.
- **Files:** `ConversationViewModel.kt`, message renderers.
- **After:** *Expansion has a single owner with unchanged behaviour.*

## Part 7 — Markdown Parser Extraction (F5)
- **Objective:** Extract `MarkdownDocument` parsing from the UI; reuse canonical search highlighting.
- **Files:** new `markdown/` package, `MarkdownContent.kt`.
- **After:** *Markdown parsing is a tested, UI-free component.*

## Part 8 — Entry Point Decomposition (F6)
- **Objective:** Extract `JunieMenuBar`, `DesktopClipboardManager`, `WindowStateTracker`, and logging setup from `main.kt`.
- **Files:** `main.kt`, new desktop components.
- **After:** *`main.kt` is pure wiring; each concern is a focused, testable component.*

## Part 9 — Documentation Updates
- **Objective:** Update `TESTING.md` (new DSL), `RECAP.md`, `project_memory.md`, and `README.md` as needed; record ADRs for the registry and self-mapping decisions.
- **Files:** `docs/**`, `README.md`.
- **After:** *Documentation reflects the new architecture and its rationale.*

## Part 10 — Testing, Review, and Completion
- **Objective:** Full `./gradlew check`, manual parity checklist, cyclomatic complexity re-check, HITL sign-off.
- **Files:** All.
- **After:** *Sprint 6 is verified behaviour-preserving and ready for sign-off.*

# 23. Risks and Mitigations

- **R1 — Behaviour drift during mapper rework:** Typed parsing may silently change edge-case handling. *Mitigation:* Characterization tests per event type before Part 3; representative fixtures re-verified.
- **R2 — Test migration churn (F11):** Consolidating 14+ UI test files risks losing assertions. *Mitigation:* Migrate file-by-file; count assertions before/after; never delete a behaviour check.
- **R3 — ID change breaking persisted or in-flight state (F9):** Stable-ID derivation changes existing IDs. *Mitigation:* IDs are session-scoped and not persisted; verify expansion/search state keyed by ID survives reloads in tests.
- **R4 — Registry over-abstraction (F1):** A registry can become its own complexity. *Mitigation:* Keep it a plain map of descriptors; no reflection, no DI framework; ADR documents the shape.
- **R5 — `main.kt` extraction regressions (F6):** Menu shortcuts and the clipboard hack are platform-sensitive. *Mitigation:* Extract mechanically without logic changes; manual menu/shortcut checklist on macOS.
- **R6 — Sprint size:** Twelve findings across ten parts is ambitious. *Mitigation:* Parts are independently shippable; blockers (Parts 3–5) are prioritized; remaining parts can defer to Sprint 7 with HITL approval.

# 24. Open Questions

- **Q1: Strategy or Visitor for self-mapping events (F3)?**
  - Recommendation: Strategy — a `toMessage()` on each event (with a shared context parameter) is simpler and respects the one-level inheritance cap. **HITL required.**
- **Q2: Should the `MessageContentRegistry` hold composable references directly, or should renderers stay in a UI-layer map keyed by kind?**
  - Recommendation: Keep collapsibility and searchable-text in a shared registry; keep renderer lookup in a UI-layer map so the shared layer stays Compose-free where possible. **HITL required.**
- **Q3: Stable ID source (F9): file line offset or first-parse UUID cache?**
  - Recommendation: File line offset (session path + line number) — deterministic across reloads with no cache to manage. **HITL required.**
- **Q4: Should turn grouping move fully into the ViewModel (F10) or just be memoized in the UI?**
  - Recommendation: ViewModel derivation, so grouping is testable without Compose. **HITL required.**
- **Q5: Consolidate UI tests into how many files?**
  - Recommendation: Group by behaviour area (search, filters, sort, expansion, commands, live tracking) — roughly 6 cohesive suites. **HITL required.**
- **Q6: Should ADRs be written for the registry and self-mapping decisions?**
  - Recommendation: Yes — one ADR per structural decision, per project guidelines. **HITL required.**
- **Q7: If the sprint runs long, which parts defer first?**
  - Recommendation: Defer Part 8 (entry point) and Part 7 (Markdown parser) to Sprint 7; never defer the blockers (Parts 3–5). **HITL required.**

# 25. Definition of Done

This sprint is complete when all the following conditions are met:

- No `JsonElement`/`JsonObject` remains in `domain/AgentEvents.kt`.
- `EventToMessageMapper`'s event-type `when` block is removed; events self-map.
- A single `MessageContentRegistry` replaces the three `MessageKind` `when` chains.
- Expansion state has a single ViewModel owner; `rememberMessageExpansionState` is removed.
- Markdown parsing lives in a non-UI, unit-tested component; bespoke highlight code is deleted.
- `main.kt` is pure wiring; menu, clipboard, window-state, and logging concerns are extracted.
- `extractWorkingDirectory` uses `JsonlParser`; search updates are atomic; Message IDs are stable; turn grouping is derived once.
- `runConversationUiTest { }` DSL exists and UI tests use it; `JsonlParserTest` is parameterized.
- `libs.versions.toml` uses `version.ref` for slf4j.
- ADRs recorded for registry and self-mapping decisions (if Q6 approved).
- `./gradlew :shared:jvmTest`, `./gradlew test`, and `./gradlew check` all pass.
- Manual parity checklist passes; cyclomatic complexity re-checked.
- Documentation (`TESTING.md`, `RECAP.md`, `project_memory.md`, `README.md`) updated.
- HITL final approval is granted.
