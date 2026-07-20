# Sprint 6 Area 1 — Discovery, Characterization, and Quick Wins Findings

> **Date:** 2026-07-20
> **Author:** Junie (automated discovery)
> **Status:** HITL review complete (2026-07-20) — all open questions Q1–Q8 decided

---

## 1. Documentation Baseline

### Canonical Terms for Sprint 6

The following terms from `UBIQUITOUS-LANGUAGE.md` must be used consistently:

| Term | Definition (summary) |
|---|---|
| **Conversation** | The complete ordered exchange for one Session. |
| **Session** | A single recorded Junie run; folder under `~/.junie/sessions/` containing `events.jsonl`. |
| **Event** | A single raw JSON line in `events.jsonl`. |
| **Message** | A display unit derived from one Event, with a Sender, Message Kind, and content. |
| **Human** | The person interacting with Junie (Sender). Avoid "user" as Sender label. |
| **Junie** | The AI assistant (Sender). |
| **Search Query** | Free text for case-insensitive substring matching. |
| **Filter** | User-controlled predicate showing/hiding Messages by Sender/Kind. |
| **Message Kind** | Classification determining rendering and filterability. |
| **HITL** | Human In The Loop — reviewer of sprint deliverables. |

### Testing Conventions

- **Stack:** JUnit 4, Turbine (Flow testing), Compose Test Rule, Strikt, Okio temp dirs; fakes over mocks; Robot Pattern for UI tests; semantic `testTag`s.
- **Commands:** `./gradlew :shared:jvmTest` (shared module), `./gradlew test` (full suite), `./gradlew check` at sprint end.

### Prior Decisions/Gotchas Affecting Sprint 6 Remediation

- **Sprint 5 Q7 decision:** Search force-expansion takes priority over global Collapse All — semantics `manualExpanded || (forceExpanded && !userDismissedForce)` must be preserved by Area 6 centralization.
- **Sprint 5:** Block expansion hoisted to ViewModel `blockExpansionStates` for global commands, but search force-expansion remained UI-side (`rememberMessageExpansionState`) — the direct cause of finding F4.
- **Sprint 4/5 gotcha:** `StateFlow` updates can emit intermediate states; Turbine tests had to consume multiple items — the direct symptom of finding F8 (now fixed; tests assert a single emission).
- **Gotcha:** LazyColumn virtualisation means off-screen items are not rendered in UI tests — consolidated suites (Area 2) must keep assertions on visible items.
- **Gotcha:** No configured cyclomatic complexity tool in the project — task 10.5 will need a tool choice or manual review.
- **Sprint 4:** Live tracking is polling-based (`LiveSessionTracker` with byte offset); `-live-` ID prefix patching was introduced to avoid key collisions — the direct cause of finding F9.

---

## 2. Findings Confirmation (Task 1.2)

All twelve findings were verified against the current code. Line references below are current as of 2026-07-20 (pre-quick-win).

| Finding | Verdict | Current references | Notes / amendments |
|---|---|---|---|
| **F1** Triple `MessageKind` dispatch | Confirmed | `MessageItems.kt` (652 lines) L359–472 and L513–604; `ConversationViewModel.kt` L416–436 (`collectCollapsibleBlockIds`, with a nested content `when`) | File is 652 lines, not 653. |
| **F2** `JsonElement` in domain | **Amended: 19 event types, not 20+** | `domain/AgentEvents.kt` (290 lines) — 19 event types carry `JsonElement`/`JsonObject` fields (see §3) | Slightly smaller scope than reviewed. |
| **F3** Parallel event registries | Confirmed | Mapper `when` `EventToMessageMapper.kt` L64–205 (~140 lines of branches in a 220-line file); ask/choice manual JSON navigation L133–172; registries `EventSerializers.kt` L16–30 (13 top-level) and L33–65 (28+ agent) | The "~220-line `when`" is the whole file; the `when` itself is ~140 lines. |
| **F4** Split expansion ownership | Confirmed | ViewModel: `collapseAllBlocks` L442, `showAllBlocks` L450, `toggleBlockExpansion` L473–479; UI: `MessageExpansionState` `MessageItems.kt` L601–624, `rememberMessageExpansionState` L630–652 | |
| **F5** Markdown parsing in UI | Confirmed | `MarkdownContent.kt` (306 lines): `parseMarkdownBlocks` L105–190, `renderInlineMarkdown` L232–306, bespoke `applySearchHighlight` L195–230 despite `SearchHighlight.kt` | |
| **F6** `main.kt` junk drawer | Confirmed | `desktopApp/src/main/kotlin/com/knowledgespike/junieviewer/main.kt` — 333 lines; window-state persistence L92–104; `MenuBar` L116–230 (~115 lines); AWT synthetic-copy hack L260–301; `Slf4jLogger` wiring L318–333 | Path is `src/main`, not `src/jvmMain`. |
| **F7** Repository bypasses parser | Confirmed — **fixed in Area 1** | `SessionRepository.kt` L172–204: line loop with `contains("currentDirectory")` pre-filter, ad-hoc `Json.parseToJsonElement`, nested `blob` re-parse, silent catch | See §5. |
| **F8** Non-atomic search updates | Confirmed — **fixed in Area 1** | `ConversationViewModel.kt` L126–129 (two consecutive `_state.update`s) + side-effecting `filterMessages(query)` L484–527 | See §5. |
| **F9** Unstable Message IDs | Confirmed | `EventToMessageMapper.kt` L212: `id = "$index-${ts ?: "$tag-${content.hashCode()}"}"` (plus `event.hashCode()` fallbacks L27/L35/L45); `LiveSessionTracker.kt` L78–82: `"${messageIndex}-live-${msg.id}"` re-indexing | Fix scheduled for Area 4. |
| **F10** Turn grouping in composition | Confirmed — **fixed in Area 1** | `ConversationScreen.kt` L310: unremembered `groupMessagesIntoTurns(state.filteredMessages)`; defined in `domain/Turn.kt` L16 | See §5. |
| **F11** Test copy-paste architecture | **Amended** | `JsonlParserTest.kt` is exactly 603 lines of hardcoded-JSON tests; **14** UI test files hand-wire `ConversationViewModel` + `PreferencesRepository`; only 2 of them (`AccessibilityAndArea8Test`, `ConversationScreenTest`) also wire `ConversationRobot` (2 more use the Robot without constructing the ViewModel) | The DSL (Area 2) still pays off for all 14 files. |
| **F12** slf4j catalog bypass | Confirmed — **fixed in Area 1** | `gradle/libs.versions.toml` L22 had inline `version = "2.0.13"` | See §5. |

---

## 3. Typed-Payload Inventory Preview (Input to Area 3)

19 event types in `domain/AgentEvents.kt` carry raw `JsonElement`/`JsonObject` fields:

| Event | Raw fields |
|---|---|
| `ResultBlockUpdatedEvent` | `changes` |
| `AgentPlanUpdatedEvent` | `items` |
| `AvailablePullRequestsEvent` | `pullRequests`, `agent` |
| `LlmResponseMetadataEvent` | `modelUsage` |
| `EnvironmentVariablesUpdatedEvent` | `variables` |
| `ViewFilesBlockUpdatedEvent` | `files` |
| `ContextWindowReportEvent` | `percentage` |
| `FileChangesBlockUpdatedEvent` | `changes` |
| `ShowPlanProgressEvent` | `progress`, `items` |
| `NextPromptSuggestionEvent` | `suggestion` |
| `AskAsyncRequestUpdatedEvent` | `request` |
| `AuthorizationAvailabilityEvent` | `agent` |
| `AgentStartedEvent` | `agent` |
| `SuggestPlanEvent` | `plan`, `sections`, `deliveryPlan` |
| `AskRequestUpdatedEvent` | `askRequest` |
| `ChoiceRequestUpdatedEvent` | `choiceRequest` |
| `SubagentSpawnedEvent` | `agent` |
| `AgentTaskFailedEvent` | `details` |
| `UnknownAgentEvent` | `raw: JsonObject` |

The full payload-shape inventory against real `events.jsonl` fixtures is task 3.1.

---

## 4. Test/Build Baseline (Tasks 1.1, 1.8)

| Check | Before Area 1 | After Area 1 quick wins |
|---|---|---|
| `./gradlew :shared:jvmTest` | Green — part of 378 total tests | Green |
| `./gradlew test` | Green — 378 tests, 0 failures | Green — **425 tests, 0 failures** |

Test additions during Area 1 (+47):

- `EventToMessageMapperCharacterizationTest.kt` — **29 new tests** locking in current event→Message mapping (see §6).
- `SearchStateDerivationTest.kt` — **4 new tests**: exactly one state emission per Search Query change (set and clear), and Turn derivation in the ViewModel after load and after query change.
- `SessionRepositoryTest.kt` — **5 new tests** characterizing working-directory extraction (direct field, nested `blob`, absent → null, malformed-line tolerance, first-hit-wins), written and verified green against the old implementation before the refactor.
- Remaining delta comes from tests counted across both modules in the post-change full run.

Two existing `ConversationViewModelTest` tests were mechanically updated: they previously consumed the intermediate emission that F8 removed (`search query updates filtered messages`, `toggling filters updates filtered messages`) and now assert the single atomic emission. No assertion was weakened; the single-emission expectation is strictly stronger.

---

## 5. Quick Wins Shipped (Tasks 1.3–1.6)

### F12 — Version catalog hygiene (1.3)

- Added `slf4j = "2.0.13"` to `[versions]`; `slf4j-api` now uses `version.ref = "slf4j"`. No inline dependency versions remain in the catalog.

### F8 — Atomic search-state updates (1.4)

- `filterMessages` is now a **pure function** `filterMessages(currentState, query): ConversationState` with no `_state.update` inside; it derives filtered Messages, sort order, match index, and (new) Turns, returning the new state.
- All five call sites (Search Query change, Filter toggle, sort toggle, session load, live-tracking append) fold the derivation into a **single** `_state.update`, so each action produces exactly one emission.
- Covered by `SearchStateDerivationTest` (`expectNoEvents()` after the single emission).

### F10 — Turn grouping out of composition (1.5)

- `ConversationState` gained `turns: List<Turn>`, derived in the ViewModel inside `filterMessages` (ViewModel derivation — the sprint doc's preferred option under Q4; see Open Questions).
- The constructor default `turns = groupMessagesIntoTurns(filteredMessages)` keeps hand-built states in UI tests consistent without touching 14 test files.
- `ConversationScreen.ConversationList` now reads `state.turns`; grouping no longer runs on every recomposition.

### F7 — extractWorkingDirectory on JsonlParser (1.6)

- The bespoke string scanner was replaced with `JsonlParser.parseLine`-based extraction, decomposed into `workingDirectoryOf` (typed dispatch), `workingDirectoryFromBlob`, and `workingDirectoryFromRaw`; malformed lines are logged at debug and skipped; first hit still wins.
- The cheap `contains("currentDirectory")` pre-filter was deliberately kept so `listSessions` doesn't fully parse every session file (performance guard).
- **Deviation (flagged for HITL):** one supporting production change outside `SessionRepository.kt` was required — `@JsonNames("currentDirectory")` on `CurrentDirectoryUpdatedEvent.directory` in `AgentEvents.kt`. Real logs emit the key `currentDirectory`, which the typed parser previously dropped; without the alias the parser-based rewrite would have lost the direct-field case. Both key names now parse; the existing `JsonlParserTest` stays green.

---

## 6. Characterization Coverage (Task 1.7)

`EventToMessageMapperCharacterizationTest.kt` (29 tests) plus the pre-existing `EventToMessageMapperTest.kt` (15 tests) now cover **every** event type handled by the mapper:

- **Message-producing top-level events:** `UserPromptEvent`, `UnknownJunieEvent`, `SystemMessageEvent`, `CancelAgentEvent`, `TaskContinueStopped`, `UserResponseEvent`.
- **Metadata-only top-level events (assert no Message):** all 7.
- **Message-producing agent events:** `ResultBlockUpdated`, `AgentThoughtBlockUpdated`, `AgentPatchCreated`, `ToolBlockUpdated`, `TerminalBlockUpdated`, `TestRunBlockUpdated`, `McpBlockUpdated`, `AgentFailure`, `MarkdownBlockUpdated`, `AskRequestUpdated`, `ChoiceRequestUpdated` (plus previously covered `AgentTaskFailed`, `UnknownAgentEvent`, `CustomAgentBlockUpdated`, `SubagentSpawned`).
- **Metadata-only agent events (assert no Message):** all 18.

Notable current behaviours now locked in before Areas 3–5:

- Ask/choice extraction: option `description` → `id` → `"option"` fallback, and a trailing newline in the assembled text.
- Raw `toString()` fallback content for non-object ask/choice payloads.
- MCP labels rendered wrapped as `Code("json")`.
- `AgentFailureEvent.errorCode` is ignored by the mapper.
- `TestRunBlockUpdatedEvent` always produces a Message.
- hashCode-based and timestamp-based ID generation; event index preserved across skipped events (relevant to the Area 4 stable-ID work).

---

## 7. Refactoring Safety Concerns

1. **ID stability (Area 4, R3):** Expansion state (`blockExpansionStates`) and search match position are keyed by Message ID. The stable-ID change will alter every ID; tests must confirm live-append + full reload yields identical IDs and that keyed state survives (task 4.6). IDs are session-scoped and never persisted, limiting blast radius.
2. **`turns` staleness risk (new, from F10 fix):** `turns` is derived only where `filterMessages` runs. Any future code path that sets `filteredMessages` without going through `filterMessages` would leave `turns` stale. All current writers go through the pure derivation; Area 5/6 work must preserve this invariant (a good candidate for enforcement when the ViewModel is reworked).
3. **`blob` payloads (Area 3):** Working-directory data can be nested inside a JSON-encoded *string* field (`blob`) — typed payload design must model this two-level decoding explicitly, not assume flat structures.
4. **Lenient parsing:** The parser and mapper silently tolerate malformed payloads in several places (e.g., ask/choice try/catch → `toString()`). Area 3 task 3.6 must make this behaviour explicit and logged, without changing outcomes.
5. **Key-name drift between fixtures and real logs:** The `directory` vs `currentDirectory` alias discovered in Area 1 suggests other event fields may have similar drift between serializer definitions and real `events.jsonl` keys. Task 3.1 should verify field names against real logs, not just existing fixtures.
6. **Platform-sensitive `main.kt` (Area 8, R5):** Menu shortcuts and the AWT synthetic-copy hack are macOS-sensitive; extraction must be mechanical with a manual checklist.

---

## 8. Testing Gaps Relevant to Remediation

- **Closed in Area 1:** event→Message mapping per event type; working-directory extraction; search-update atomicity; Turn derivation.
- **Still open (later areas):** per-`MessageKind` collapsibility/searchable-text unit tests (arrive with the registry, 5.2); Markdown parser coverage (7.2 — an existing `MarkdownParserTest.kt` in `ui/` covers rendering-level behaviour, not a standalone parser); ID-stability tests (4.4/4.6); `WindowStateTracker` unit test (8.3).
- **Structural:** 14 UI test files hand-wire ViewModel+Prefs (DSL, 2.1–2.2); `JsonlParserTest.kt` (603 lines) parameterization (2.4).

---

## 9. Open Questions for HITL (Q1–Q8)

| ID | Question | Recommendation | Status | HITL Decision (2026-07-20) |
|---|---|---|---|---|
| Q1 | Strategy or Visitor for self-mapping events (F3)? | **Strategy** — `toMessage()` on each event with a shared context parameter; simpler and respects the one-level inheritance cap. | **Decided** | **Strategy.** As recommended. |
| Q2 | Should `MessageContentRegistry` hold composable references, or renderers in a UI-layer map? | **Split** — collapsibility + searchable text in a shared registry; renderer lookup in a UI-layer map so the shared layer stays Compose-free. | **Decided** | **Split registry.** As recommended. |
| Q3 | Stable ID source (F9): file line offset or first-parse UUID cache? | **File line offset** (session path + line number) — deterministic across reloads, no cache to manage. | **Decided** | **File line offset.** As recommended. |
| Q4 | Turn grouping fully in ViewModel or memoized in UI (F10)? | **ViewModel derivation** — *already implemented this way in Area 1* (testable without Compose). Needs HITL ratification or rework to `remember`. | **Decided** | **ViewModel derivation ratified** as shipped in Area 1. |
| Q5 | Consolidate UI tests into how many files? | **~6 behaviour-area suites** (search, filters, sort, expansion, commands, live tracking). | **Decided** | **Six behaviour-area suites.** As recommended. |
| Q6 | ADRs for registry and self-mapping decisions? | **Yes** — one ADR per structural decision, per project guidelines. | **Decided** | **Yes.** One ADR per structural decision. |
| Q7 | If the sprint runs long, which parts defer first? | **Defer Area 8 (entry point) then Area 7 (Markdown parser)**; never defer Areas 3–5 (blockers). | **Decided** | **No deferral.** All ten areas are committed this sprint (recommendation overridden). |
| Q8 | (New, from Area 1) Is the `@JsonNames("currentDirectory")` alias on `CurrentDirectoryUpdatedEvent.directory` acceptable, and should task 3.1 include a systematic serializer field-name audit against real logs? | **Yes to both** — keep the alias; audit field names in 3.1. | **Decided** | **Accept alias + audit.** As recommended. |

---

## 10. Design/Remediation Recommendations

- **Proceed in the planned order** (Areas 2 → 3 → 4 → 5): the characterization safety net from Area 1 is in place; the test DSL (Area 2) should land before the structural blockers so refactors are verified concisely.
- **Adopt the F10 ViewModel derivation as the Q4 answer** — it shipped naturally as part of making `filterMessages` pure and is covered by unit tests without Compose.
- **Carry the `turns`-staleness invariant into Area 5/6**: keep `filterMessages` the only writer of `filteredMessages`/`turns`.
- **Extend task 3.1 with a field-name audit** of serializer definitions vs real `events.jsonl` keys (per §7.5).
- **Keep the `contains("currentDirectory")` pre-filter** unless profiling shows it unnecessary; `listSessions` calls `extractWorkingDirectory` per session.

---

## 11. HITL Review Summary

### What Was Audited

- All project documentation (UBIQUITOUS-LANGUAGE, TESTING, project_memory, RECAP, Sprint 5/6 docs).
- All 12 review findings against current code, with corrected line references and two amendments (F2: 19 event types; F11: robot wiring in 2 files, ViewModel+Prefs wiring in 14).
- Event mapping coverage, fixtures, and the full `commonTest` suite layout (36 files).

### What Was Shipped in Area 1

1. **F12** — slf4j version moved to `[versions]` with `version.ref`.
2. **F8** — search/filter/sort/load/live updates are atomic; `filterMessages` is pure.
3. **F10** — Turn grouping derived in the ViewModel (`state.turns`); no grouping in composition.
4. **F7** — `extractWorkingDirectory` rebuilt on `JsonlParser` (plus the `@JsonNames` alias deviation flagged above).
5. **38 new tests** (29 characterization + 4 derivation/atomicity + 5 repository) — full suite green: **425 tests, 0 failures**.

### HITL Decisions

All eight questions (Q1–Q8) were decided by the HITL on 2026-07-20 — see §9. Notably, Q7 overrides the deferral recommendation: **no area may be deferred**; all ten areas are committed this sprint.

### Recommended Next Step

Proceed to Area 2 (Test Infrastructure), carrying the Q5 six-suite consolidation decision and the Q8 field-name audit into tasks 2.3 and 3.1 respectively.
