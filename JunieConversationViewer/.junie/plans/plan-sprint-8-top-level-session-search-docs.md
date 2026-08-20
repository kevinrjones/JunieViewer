---
sessionId: session-260819-173719-fw12
---

# Requirements

### Overview & Goals
Create a new Sprint 8 planning package for **Top-Level Session Search** with two new docs:
- `docs/sprints/junie-conversation-viewer-sprint-8-top-level-session-search.md`
- `docs/tasks/junie-conversation-viewer-tasks-sprint-8-top-level-session-search.md`

The docs must match existing project style, be implementation-ready, and preserve canonical domain language.

### In Scope
- Author a full Sprint 8 sprint document with the required sections and decision framing.
- Author a full Sprint 8 task breakdown with checkboxed tasks, dependencies, testing expectations, and HITL-visible outcomes.
- Explicitly separate and preserve:
  - existing **Conversation Search** (current session only)
  - new **Top-Level Session Search** (all discovered sessions)
- Add correct cross-links between sprint/task docs and supporting documentation.

### Out of Scope
- No production code changes for search implementation.
- No edits to existing source files.
- No broad rewrites of previous sprint/task docs (except optional cross-reference mentions if needed).

### Functional Requirements for the New Docs
- Sprint doc includes all requested sections (goal, background, scope, functional and non-functional requirements, UX model, data/search design, errors/logging, testing, accessibility, risks, open questions with recommendations, DoD, deferred items).
- Task doc follows established structure from `docs/tasks/`:
  - progress summary table
  - task status legend
  - numbered areas with checkbox tasks
  - per-task fields: description, source, dependencies, likely files/areas, completion criteria, testing expectations, HITL-visible outcome where applicable.
- Task areas cover discovery, API/model design, cross-session search, UI/results, open-session wiring, tests, docs updates, and completion review.
- Acceptance criteria include responsiveness, graceful handling of bad/missing files, regression protection for existing Conversation Search, and required Gradle test commands.

### Terminology & Style Requirements
- Use `Conversation`, `Session`, `Event`, `Message`, `Human`, `Junie`, `Search Query`, and `HITL` consistently per `docs/UBIQUITOUS-LANGUAGE.md`.
- Avoid using `user` when `Human` is the intended sender label.
- Keep tone and sectioning aligned with current sprint/task artifacts.

# Technical Design

### Current Implementation Findings (Grounding)
- Current in-session search is message-level filtering in `shared/src/commonMain/kotlin/com/knowledgespike/junieviewer/ui/MessageVisibilityEngine.kt` using case-insensitive substring matching on `MessageContentRegistry.searchableText(message)`.
- Search UI currently exists in `shared/src/commonMain/kotlin/com/knowledgespike/junieviewer/ui/components/ConversationToolbar.kt` (`Search Messages...` field with match navigation).
- Search commands and keyboard mappings are in:
  - `shared/src/commonMain/kotlin/com/knowledgespike/junieviewer/ui/ConversationCommand.kt`
  - `desktopApp/src/main/kotlin/com/knowledgespike/junieviewer/desktop/JunieMenuBar.kt`
- Session discovery and loading are filesystem-based in `shared/src/commonMain/kotlin/com/knowledgespike/junieviewer/data/SessionRepository.kt`:
  - `listSessions(homePath)` discovers session directories under `.../sessions`
  - `loadSession(sessionId, homePath)` reads `events.jsonl` line-by-line and tolerates parse failures.
- Existing documentation style reference:
  - Sprint format: `docs/sprints/junie-conversation-viewer-sprint-7-ci-github-automation-and-readme.md`
  - Task format: `docs/tasks/junie-conversation-viewer-tasks-sprint-7-ci-github-automation-and-readme.md`

### Key Documentation Decisions to Encode
- Document an MVP recommendation of **on-demand scan** across session `events.jsonl` files; treat indexing/caching as deferred unless discovery disproves viability.
- Preserve existing Conversation Search behavior and define explicit post-navigation behavior when opening a session from top-level results.
- Recommend session-grouped results with session identity, match count, and content preview as the default result model.
- Specify cancellation/debouncing and robust error handling expectations for malformed, missing, or unreadable files.

### Proposed Document Construction
- Sprint doc will be written as a delivery blueprint for feature implementation and HITL decisions, including:
  - dedicated section contrasting current Conversation Search vs new Top-Level Session Search
  - UX interaction model options and recommended default path
  - data/search architecture narrative tied to real repository/viewmodel seams.
- Task doc will be written as execution checklist with 8 implementation areas and granular subtasks that map directly back to sprint requirements.
- Open-questions section will include all required decision prompts with a recommended answer for each and explicit HITL review status.

### File Plan
- New: `docs/sprints/junie-conversation-viewer-sprint-8-top-level-session-search.md`
- New: `docs/tasks/junie-conversation-viewer-tasks-sprint-8-top-level-session-search.md`

### Risks & Trade-offs to Capture in Docs
- Performance risk for large session sets if scanning is purely on-demand.
- UX complexity risk if top-level search and in-conversation search controls are visually conflated.
- Reliability risk from partially written JSONL or unreadable session files.
- Scope risk from first-sprint overreach into indexing/live global updates.

# Testing

### Validation Approach
- Validate both new docs against existing sprint/task structure and section depth.
- Verify every required section from the issue is present and explicitly scoped.
- Verify required acceptance criteria and Gradle commands are included verbatim where needed.

### Content Integrity Checks
- Check all cross-links between sprint/task/supporting docs.
- Check terminology alignment with `docs/UBIQUITOUS-LANGUAGE.md` (`Human`/`Junie` usage and search vocabulary).
- Check that Conversation Search regression-protection requirements are explicit.

### Review Readiness
- Ensure task checklist items are actionable, dependency-ordered, and HITL-reviewable.
- Ensure sprint open questions include recommendations while remaining marked for HITL decision.
- Ensure deferred scope and DoD are explicit and measurable.

# Delivery Steps

### ✓ Step 1: Build sprint-8 baseline from current code and doc conventions
Sprint 8 documentation scope is anchored to real code paths and existing documentation patterns.
- Extract current search/session architecture from `SessionRepository`, `ConversationViewModel`, `MessageVisibilityEngine`, and toolbar/menu command wiring.
- Capture documentation conventions from Sprint 7 and its companion tasks doc (metadata, heading style, task metadata blocks, status legend).
- Map discovered implementation seams to the requested Sprint 8 feature scope (top-level search without regression of current Conversation Search).

### ✓ Step 2: Author Sprint 8 sprint document
A complete Sprint 8 sprint specification exists in `docs/sprints/junie-conversation-viewer-sprint-8-top-level-session-search.md`.
- Write required sections: goal, context, problem, functional and non-functional requirements, UX model, search/data design, error/logging, testing, accessibility, documentation updates, risks/trade-offs, DoD, deferred items.
- Add an explicit separation section for existing Conversation Search vs new Top-Level Session Search.
- Include the required open decision set with recommended answers and HITL review framing.
- Keep recommendations incremental, with on-demand scan MVP as default and indexing marked as potential follow-up.

### ✓ Step 3: Author Sprint 8 task breakdown document
An execution-ready task checklist exists in `docs/tasks/junie-conversation-viewer-tasks-sprint-8-top-level-session-search.md`.
- Create progress summary table, task status legend, and numbered implementation areas 1–8.
- For each task, include checkbox, description, source, dependencies, likely files/areas, completion criteria, testing expectations, and HITL-visible outcome where applicable.
- Cover domain/API modeling, cross-session scanning, UI/result states, click-to-open navigation, regression safety for in-conversation search, and required automated test expectations.
- Include required verification commands (`./gradlew :shared:jvmTest` and `./gradlew test`).

### ✓ Step 4: Cross-link, consistency, and acceptance pass
Both Sprint 8 docs are internally consistent, linked, and ready for HITL review.
- Verify sprint/task cross-links and references to supporting docs (`HOW_TO_USE`, `RECAP`, `project_memory`, `README` as applicable).
- Confirm canonical terminology and sender labeling are consistent (`Human`/`Junie`, not ambiguous alternatives).
- Run a final acceptance checklist against all issue constraints, including explicit non-regression of existing Conversation Search and documented deferred items.

### ✓ Step 5: Audit Sprint 8 Area 1 sources and implementation seams
Area 1 discovery is grounded in the current sprint/task docs, project terminology docs, and implementation seams.
- Read required docs and prior Area 1 findings docs to align output structure and terminology.
- Inspect current session discovery/loading, conversation search, and session-open flow in code.
- Capture concrete insertion points and non-regression boundaries for future Areas 2+.

### ✓ Step 6: Author Sprint 8 Area 1 discovery findings document
A new discovery findings doc exists at `docs/sprint-8-area-1-discovery-findings.md` with complete Area 1 coverage.
- Write all required sections from documentation baseline through HITL review summary.
- Include concrete file/method seams, MVP strategy recommendation, UX recommendations, and open HITL questions with recommendations.
- Document explicit Conversation Search vs Top-Level Session Search boundaries.

### ✓ Step 7: Update Sprint 8 task document Area 1 status and notes
The Sprint 8 task document reflects completed discovery work and pending HITL decisions.
- Mark 1.1 and 1.2 complete if criteria are satisfied.
- Keep 1.3 and 1.4 pending unless explicit HITL decisions are already captured.
- Update Area 1 progress summary and add concise notes/decisions log entries.

### ✓ Step 8: Final consistency pass and submit Area 1 documentation changes
Area 1 documentation changes are cross-checked and ready for handoff.
- Verify terminology consistency and required non-regression boundaries.
- Verify links and references in new/updated docs.
- Submit with concise summary and verification notes.

### ✓ Step 9: Capture HITL decisions for Sprint 8 Section 6.2
Section 6.2 open questions are reviewed with HITL and explicit decisions are captured.
- Walk through all ten decision prompts from `docs/sprint-8-area-1-discovery-findings.md` section 6.2.
- Record final decisions for entry point, trigger timing, result granularity, open-position behavior, search source, unknown payload handling, metadata visibility, indexing strategy, post-open Conversation Search behavior, and live tracking policy.

### ✓ Step 10: Update Sprint 8 sprint document with approved decisions
The sprint document reflects final HITL-approved choices and removes ambiguity for implementation.
- Update open-questions outcomes with explicit accepted decisions.
- Align UX model, MVP strategy, and deferred items with the approved Section 6.2 answers.

### ✓ Step 11: Update Sprint 8 task document status and decision log
The task document reflects resolved HITL decisions and accurate Area 1 progress.
- Mark tasks 1.3 and 1.4 complete only after decisions are captured.
- Add or update Sprint 8 notes/decision log entries linking to finalized decisions.

### ✓ Step 12: Final consistency pass and submit decision-capture updates
Sprint and task docs are synchronized and ready for Area 2 execution.
- Verify terminology and capability split remain explicit.
- Verify cross-links and status totals match the new Area 1 completion state.
- Submit concise verification notes and next-step readiness.

### ✓ Step 13: Inspect Area 2 implementation seams and test surfaces
Area 2 changes are grounded in current domain/data/UI architecture and existing tests.
- Review current `SessionRepository` contracts, related fakes/tests, and top-level search seams.
- Review `ConversationState`/`Action`/`Event`/`Command`/`ViewModel` structures for non-regression-safe extension points.
- Confirm target package/file locations and deterministic test strategy for Area 2 scope.

### ✓ Step 14: Implement top-level search domain models plus deterministic ordering/snippet rules
Shared domain foundations for Top-Level Session Search are implemented with pure, testable logic.
- Add query/result/snippet/partial-failure models that preserve Session identity and safe defaults.
- Implement deterministic result ordering and bounded snippet extraction helpers.
- Add model and ordering/snippet tests covering normalization, tie-breaks, truncation, and case-insensitive matching.

### ✓ Step 15: Extend repository/service contract and supporting test doubles
Cross-session search contract is added while preserving existing session list/load compatibility.
- Extend repository/service API with coroutine-friendly `searchSessions` entry point.
- Update fakes or test fixtures to satisfy the new contract.
- Add/adjust contract-level tests ensuring structured result behavior.

### ✓ Step 16: Add top-level search state/action/event/command and ViewModel reducer plumbing
Top-level search flow state is introduced without conflating existing Conversation Search behavior.
- Add isolated top-level search state and status fields to `ConversationState`.
- Add Area 2 action/event/command structures for toggle/query/submit/cancel/result-select paths.
- Implement minimal ViewModel transition handling and tests proving separation/non-regression.

### ✓ Step 17: Run Area 2 verification tests and update Sprint 8 task tracking
Area 2 completion is validated and documentation reflects implementation status.
- Run relevant test targets including `./gradlew :shared:jvmTest` and `./gradlew test` (or document any justified scope limits during iteration).
- Update `docs/tasks/junie-conversation-viewer-tasks-sprint-8-top-level-session-search.md` Area 2 items/status/notes.
- Confirm progress totals and decision log entries are accurate.

### ✓ Step 18: Final consistency pass and submit Area 2 implementation
Area 2 changes are coherent, non-regressive, and ready for handoff.
- Verify terminology and capability boundaries remain explicit in code and docs.
- Verify no out-of-scope Area 3+ behavior was implemented.
- Submit concise summary with verification outcomes.

### ✓ Step 19: Add HITL final approval checkpoints for Sprint 8 Area 2+
Sprint 8 task documentation includes explicit HITL final approval tasks at the end of each area from 2 onward.
- Add one `HITL final approval` task to the end of Areas 2–8 in `docs/tasks/junie-conversation-viewer-tasks-sprint-8-top-level-session-search.md`.
- For each new approval task, document: what changed in that area, what HITL should check, and what app behavior HITL can validate by running the application.
- Keep existing task ordering, dependencies, and implementation statuses intact unless directly affected by these new review tasks.

### ✓ Step 20: Reconcile progress summary and notes after HITL checkpoint additions
Sprint 8 progress tracking remains accurate after adding Area 2+ approval tasks.
- Update Area 2–8 task counts and completion/progress values to reflect newly added approval tasks.
- Add or update concise Sprint 8 notes describing the new HITL review checkpoints and intended reviewer workflow.
- Verify consistency with Sprint 7 style while preserving Sprint 8 terminology and scope boundaries.

### ✓ Step 21: Final consistency pass and submit HITL-review-task update
The documentation update is internally consistent and ready for continued Sprint 8 execution.
- Verify no production source code behavior changes were introduced for this request.
- Verify all new HITL approval tasks include concrete review expectations and runnable app checks.
- Submit concise verification notes and readiness outcome.