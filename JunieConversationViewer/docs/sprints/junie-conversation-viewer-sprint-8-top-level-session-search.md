---
sprint: 8
name: Top-Level Session Search
status: planned
---

# 1. Title

Sprint 8 — Top-Level Session Search

# 2. Related Documents

- [`docs/tasks/junie-conversation-viewer-tasks-sprint-8-top-level-session-search.md`](../tasks/junie-conversation-viewer-tasks-sprint-8-top-level-session-search.md) — companion implementation checklist for this sprint.
- [`docs/sprints/junie-conversation-viewer-sprint-7-ci-github-automation-and-readme.md`](junie-conversation-viewer-sprint-7-ci-github-automation-and-readme.md) — preceding sprint baseline.
- [`docs/HOW_TO_USE.md`](../HOW_TO_USE.md) — current operator guidance that must be updated for the new search workflow.
- [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md) — canonical domain language for Conversation, Session, Event, Message, Human, Junie, Search Query, and HITL.
- [`docs/RECAP.md`](../RECAP.md) — project timeline.
- [`docs/TESTING.md`](../TESTING.md) — testing standards and verification commands.
- [`docs/project_memory.md`](../project_memory.md) — decisions, gotchas, and shipped summaries.

# 3. Sprint Goal

Add a new **Top-Level Session Search** capability that lets a Human run a Search Query across all discovered Session files and open a selected matching Session in the existing Conversation viewer, while preserving current in-Conversation Search behavior.

# 4. Background and Current Baseline

## 4.1 Current Session Discovery and Loading

- `SessionRepository.listSessions(homePath)` discovers Session directories under `~/.junie/sessions/`.
- `SessionRepository.loadSession(sessionId, homePath)` loads `events.jsonl` line-by-line and tolerates parse failures.
- Session loading currently favors resiliency: malformed lines are skipped; unknown Event kinds are tolerated and logged.

## 4.2 Current Search Behavior

- Current search is **Conversation Search** only (active Session only).
- Filtering/matching is in `MessageVisibilityEngine`, using case-insensitive substring matching against `MessageContentRegistry.searchableText(message)`.
- Search UI and next/previous navigation are in `ConversationToolbar` and command wiring (`ConversationCommand`, `JunieMenuBar`).

## 4.3 Product Gap

Humans cannot currently search across all Sessions. They must open Sessions one-by-one and run Conversation Search manually, which is slow for large histories.

# 5. Problem Statement

The application needs a second search capability that operates across all discovered Session files. The new capability must produce useful Session-level results (identity + confidence context), let the Human open a selected Session quickly, and avoid regression in the existing Conversation Search flow.

# 6. Scope

## 6.1 In Scope

- Add top-level Search Query entry and execution flow across discovered Sessions.
- Search readable Session content derived from Events/Messages using case-insensitive substring matching.
- Return Session-grouped results with Session identity, match count, and preview/snippet.
- Open selected Session from results in the existing Conversation viewer.
- Define expected post-navigation behavior for existing Conversation Search state.
- Add testing and documentation updates needed for the new workflow.

## 6.2 Out of Scope / Deferred

- Persistent indexing service and background index refresh.
- Real-time global result auto-refresh while live tracking appends new Session content.
- Advanced ranking/relevance scoring beyond deterministic MVP ordering.
- Multi-query history and saved searches.

# 7. Explicit Capability Separation

## 7.1 Existing Capability: Conversation Search

- Scope: current opened Session only.
- Search target: rendered Message-level searchable text.
- Controls: existing toolbar Search Messages field and Find Next/Find Previous commands.

## 7.2 New Capability: Top-Level Session Search

- Scope: all discovered Sessions under the configured Junie home path.
- Search target: Session content from `events.jsonl` (MVP derived from parsed/mapped Message text where practical).
- Output: Session-level results list that supports click-to-open navigation.

## 7.3 Non-Regression Requirement

Sprint 8 must add Top-Level Session Search **without changing expected Conversation Search behavior** inside the currently opened Conversation.

# 8. Functional Requirements

- **FR1:** Human can open a top-level Search Query input from the application chrome (toolbar/menu/dialog workflow decided in sprint tasks).
- **FR2:** Search scans all discovered Sessions under `~/.junie/sessions/` using current configured home path.
- **FR3:** Search uses case-insensitive substring matching.
- **FR4:** Search returns Session-grouped results with: Session identifier/display name, match count, and preview snippet.
- **FR5:** Human can select a result and open that Session in the Conversation viewer.
- **FR6:** Missing/unreadable/malformed Session files are handled gracefully; search still returns partial results when possible.
- **FR7:** Search supports cancellation when a newer Search Query replaces an in-flight search.
- **FR8:** If search runs while typing, query input is debounced.
- **FR9:** Existing Conversation Search remains available and predictable after opening a Session from top-level results.
- **FR10:** Search result view includes explicit loading, empty, and partial-results/error states.

# 9. Non-Functional Requirements

- **NFR1 Performance:** UI remains responsive while scanning many Sessions and large `events.jsonl` files.
- **NFR2 Reliability:** Per-Session failures do not crash global search; failures are isolated and recoverable.
- **NFR3 Determinism:** Result ordering and counts are deterministic for identical inputs.
- **NFR4 Observability:** Logs record skipped Sessions and parse/read failures at appropriate severity.
- **NFR5 Maintainability:** New search logic is testable as isolated domain/repository collaborators.
- **NFR6 Accessibility:** Keyboard-first usage is supported for entering query, traversing results, and opening Session.

# 10. UX and Interaction Model

## 10.1 MVP Recommendation

Use a dedicated top-level search surface (toolbar control or modal/search panel opened by command) separate from Conversation Search to avoid control conflation.

## 10.2 Expected States

- Idle (no query)
- Searching (progress/loading indicator)
- Results (Session-grouped list)
- No matches
- Partial results with warnings (some Sessions failed to scan)
- Fatal error (unexpected failure)

## 10.3 Results Presentation

Each result row should include:

- Session identity (id/display label)
- Match count
- Preview snippet for one or more matching Message segments
- Optional timestamp metadata if available

## 10.4 Post-Selection Behavior

Selecting a Session result loads that Session in the existing viewer and closes/dismisses the top-level results surface according to final UX decision.

# 11. Data and Search Design

## 11.1 MVP Strategy: On-Demand Scan

- Default Sprint 8 implementation is on-demand scan of discovered Sessions.
- No persisted index in MVP unless discovery proves on-demand scan is not viable.

## 11.2 Search Input and Result Model (Conceptual)

- Query model capturing normalized Search Query and runtime options (debounce/cancel context).
- Result model containing Session summary and nested match previews.
- Error collection model for partial-results reporting.

## 11.3 Search Targets

- Prefer parsed Event → mapped Message text for consistent human-readable matching.
- Where structured mapping is unavailable, include safe fallback handling for unsupported/unknown payload text based on HITL decision.

## 11.4 Concurrency Model

- Execute scans off the UI thread.
- Cancel in-flight search jobs when newer queries arrive.
- Bound per-query work so stale requests do not compete with active ones.

# 12. Error Handling and Logging Expectations

- Missing `events.jsonl`: mark Session as skipped and continue.
- Unreadable file / IO error: capture warning and continue scanning other Sessions.
- Malformed JSONL line: skip line, continue Session scan, and track parse warning count.
- Unexpected exceptions: fail current query with Human-visible error state and diagnostic log entry.
- Logs should include Session id/path context for troubleshooting.

# 13. Testing Strategy

## 13.1 Automated Coverage

- Domain/repository tests for case-insensitive matching, grouping, counting, and snippet generation.
- Tests for malformed lines, missing files, unreadable files, empty Sessions, and mixed success/failure scans.
- ViewModel tests for cancellation/debouncing and state transitions (loading/results/empty/partial-error).
- UI tests for query input, results rendering, click-to-open flow, and keyboard interactions.
- Regression tests ensuring Conversation Search behavior remains intact.

## 13.2 Verification Commands

- `./gradlew :shared:jvmTest`
- `./gradlew test`

# 14. Accessibility and Keyboard Considerations

- Top-level Search Query input must be focusable via keyboard shortcut/menu action.
- Results list must support keyboard traversal and Enter-to-open behavior.
- Status messaging for loading/no-results/errors must be screen-reader discoverable where applicable.
- Preserve existing Find Next/Find Previous keyboard behavior for Conversation Search.

# 15. Documentation Updates Required

- Update `docs/HOW_TO_USE.md` with explicit distinction between Conversation Search and Top-Level Session Search.
- Update `README.md` summary if feature is shipped in sprint implementation.
- Update `docs/RECAP.md` with Sprint 8 outcomes.
- Update `docs/project_memory.md` with shipped behavior, decisions, gotchas, and test coverage notes.

# 16. Risks and Trade-offs

- **Performance risk:** on-demand scans may feel slow with very large Session sets.
- **UX risk:** top-level and in-Conversation search may be confused if controls are too similar.
- **Reliability risk:** partial/corrupt JSONL data can reduce result quality.
- **Scope risk:** adding indexing or live global updates in Sprint 8 may over-expand delivery.

# 17. Open Questions for HITL Review (with Recommendations)

1. **Entry point location:** toolbar field, dialog, sidebar, or overlay?
   - **Recommendation:** dedicated dialog/panel opened from toolbar/menu, separate from Conversation Search field.
2. **Execution timing:** live while typing, Enter-only, or both?
   - **Recommendation:** both; debounced live updates plus explicit Enter submit.
3. **Result granularity:** Session-level rows only or match-level rows grouped by Session?
   - **Recommendation:** Session-level rows with compact previews; keep match-level expansion deferred.
4. **Open-position behavior:** open Session at first matching Message when feasible?
   - **Recommendation:** defer jump-to-first-match unless low effort; open Session normally in MVP.
5. **Search source:** raw JSONL, parsed Events, mapped Messages, or hybrid?
   - **Recommendation:** parsed Events + mapped Message text first; fallback handling for unsupported payloads.
6. **Unsupported/unknown Event payloads:** include in searchable corpus?
   - **Recommendation:** include safe textual fallback where practical, clearly labeled as unsupported-derived text.
7. **Metadata in results:** include date/time when available?
   - **Recommendation:** include lightweight timestamp metadata when available with low overhead.
8. **Caching/indexing now or later?**
   - **Recommendation:** defer persistent indexing; ship on-demand scan MVP first.
9. **Conversation Search Query after opening result:** preserve, clear, or prompt?
   - **Recommendation:** clear Conversation Search Query on Session change to avoid confusing stale highlights.
10. **Live tracking impact on top-level results:** auto-update or defer?
    - **Recommendation:** defer auto-updating global results; rerun query manually in MVP.

All items above require HITL confirmation before implementation is finalized.

# 18. Acceptance Criteria

- Human can enter a top-level Search Query.
- Search runs across all discovered Session `events.jsonl` files.
- Matching Sessions display match counts and useful previews.
- Selecting a result opens that Session in the Conversation viewer.
- Existing Conversation Search still works as expected.
- Malformed, missing, empty, or unreadable Session files are handled gracefully.
- UI remains responsive during search.
- Automated tests cover domain/repository logic, ViewModel behavior, and UI interactions.
- Documentation clearly explains Conversation Search vs Top-Level Session Search.
- `./gradlew :shared:jvmTest` and `./gradlew test` pass for implementation delivery.
- HITL review is completed and approved.

# 19. Definition of Done

- Functional and non-functional requirements are implemented and validated.
- Non-regression for Conversation Search is verified by automated coverage.
- Top-level search UX states (loading, empty, partial-error, results) are complete.
- Required docs (`HOW_TO_USE`, `README` if needed, `RECAP`, `project_memory`) are updated.
- HITL has reviewed open decisions and accepted Sprint 8 outcomes.

# 20. Deferred Items

- Persistent index/caching layer for faster repeated global searches.
- Rich relevance/ranking and advanced snippet highlighting.
- Auto-refresh of top-level results from live Session updates.
- Jump-to-first-match navigation after opening a Session from top-level result.