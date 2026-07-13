# 1. Title

Conversation UI Design — Task Document (Sprint 2)

# 2. Related Sprint

- **Sprint document:** [
  `docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-design.md`](../sprints/junie-conversation-viewer-sprint-2-conversation-ui-design.md)
- **Ubiquitous language:** [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md)

**Sprint goal (summary):** Design and plan a cross-platform desktop UI (macOS, Windows, Linux)
for viewing Junie Conversation data. The UI must make an inherently asymmetric Conversation —
short Human Messages and long, rich Junie Messages — easy to read, scan, search, filter, and
verify. This is a design/planning sprint only; no application code is implemented. The sprint's
ubiquitous language (Conversation, Session, Turn, Message, Message Kind, Human, Junie, Response,
Thought, Tool Call, Terminal Output, Patch, Diff, Structured Output, Filter, Search Query, HITL,
Reviewable Outcome) is defined in `docs/UBIQUITOUS-LANGUAGE.md`.

# 3. Purpose

This document breaks the Conversation UI Design sprint into clear, reviewable, trackable tasks.
It serves two audiences:

- **Junie** — as an execution checklist for producing the design/planning artefacts.
- **HITL** — as a review/progress checklist for confirming each Sprint part is delivered and
  matches its Reviewable Outcome ("After" section).

Every task traces back to a Sprint part, section, or the Definition of Done. Because this is a
design/planning sprint, "completion" means a documented, reviewable artefact exists — not shipped
application code.

# 4. How to Use This Task Document

- Read this document alongside the Sprint document; each task names its **Source** section/part.
- Work top to bottom; complete Sprint-alignment tasks (Area 1) before design tasks.
- Check a task `[x]` **only** when its Completion criteria are met at the time of editing.
- A parent task is complete only when all its subtasks are complete.
- Tasks tagged `HITL Review` require HITL confirmation before they count as complete.
- Update the Progress Summary table whenever task counts or statuses change.
- Record any decisions or newly discovered terms in the Notes / Decisions Log (section 11).

# 5. Progress Summary

| Area                                       | Total Tasks | Complete | Status          |
|--------------------------------------------|------------:|---------:|-----------------|
| 1. Sprint alignment & documentation setup  |           6 |        6 | Complete        |
| 2. Ubiquitous language follow-up           |           4 |        4 | Complete        |
| 3. Conversation layout planning            |           8 |        8 | Complete        |
| 4. Rich content rendering planning         |           9 |        9 | Complete        |
| 5. Navigation, search & filtering planning |           6 |        6 | Complete        |
| 6. Cross-platform desktop review           |           9 |        9 | Complete        |
| 7. Accessibility review                    |           6 |        6 | Complete        |
| 8. Testing strategy                        |           9 |        9 | Complete        |
| 9. HITL review & grill feedback            |           6 |        6 | Complete        |
| 10. Final sprint readiness review          |           5 |        4 | In progress     |
| **Total**                                  |      **68** |   **67** | **In progress** |

Statuses used: Not started, In progress, Blocked, Complete.

**Note:** All design/planning content required by the sprint already exists in the Sprint
document (sections 1–19, Parts A–H) and `docs/UBIQUITOUS-LANGUAGE.md`; those tasks are therefore
marked complete. The one outstanding item is the **final standalone HITL approval of this task
document** (task 10.5), which is left unchecked because it cannot be self-certified.

# 6. Task Status Legend

- `[ ]` — Not started or not complete.
- `[x]` — Complete, and reviewed where review is required.

Inline markers:

- `HITL Review` — the task requires explicit HITL confirmation to be considered complete.
- `Blocked` — the task cannot proceed until a dependency is resolved.
- `Deferred` — intentionally out of scope for this sprint (see section 10).
- `Depends on: <task id>` — the task requires the named task first.

# 7. Task List

## 1. Sprint Alignment and Documentation Setup

### 1.1 Read the sprint document

- [x] Read the Conversation UI Design sprint document

**Description:** Read the sprint document end to end and confirm the task breakdown matches the Sprint Goal.

**Source:** Sprint doc — sections "2. Sprint Goal", "5. Scope", "15. Delivery Plan / Sprint Parts".

**Completion criteria:**

- Sprint document has been read in full.
- This task document reflects all Sprint parts (A–H).
- No Sprint part is missing from the task list (see traceability in task 1.5).

**HITL-visible outcome:** The HITL can compare this task document with the sprint document and see that each Sprint part
maps to tasks.

### 1.2 Read required project docs

- [x] Read `RECAP.md`, `TESTING.md`, `project_memory.md`, `UBIQUITOUS-LANGUAGE.md`

**Description:** Read the required reference documents so tasks are grounded in project history, testing standards, and
domain language.

**Source:** Issue "Before You Start"; Sprint doc — "3. Background / Context".

**Completion criteria:**

- All four documents have been read.
- Testing tasks (Area 8) are grounded in `docs/TESTING.md`.
- Terminology tasks (Area 2) are grounded in `docs/UBIQUITOUS-LANGUAGE.md`.

### 1.3 Confirm the sprint goal

- [x] Confirm the Sprint Goal is clear and correctly scoped as design-only

**Description:** Confirm the Sprint Goal is unambiguous and that no application code is in scope.

**Source:** Sprint doc — "2. Sprint Goal", "6. Out of Scope".

**Completion criteria:**

- Sprint Goal understood as design/planning only.
- Out-of-scope items (application code, export, multi-Session, streaming) acknowledged.

**HITL-visible outcome:** HITL confirmed the goal is "Clear as-is" in the grill session (Sprint doc section 18).

### 1.4 Confirm terminology against the ubiquitous language

- [x] Confirm task language uses approved ubiquitous-language terms

**Description:** Ensure this task document uses the canonical domain terms and avoids the discouraged synonyms.

**Source:** `docs/UBIQUITOUS-LANGUAGE.md`; Sprint doc — "4. Ubiquitous Language References".

**Completion criteria:**

- Terms such as Conversation, Turn, Message, Message Kind, Human, Junie, Patch/Diff, Filter, Search Query, HITL are used
  consistently.
- No discouraged synonyms (e.g. "bubble", "chat", "user" for the Sender) are used.

### 1.5 Map every sprint part to tasks

- [x] Ensure every Sprint part (A–H) maps to one or more tasks

**Description:** Verify traceability between Sprint parts and this task document.

**Source:** Sprint doc — "15. Delivery Plan / Sprint Parts".

**Completion criteria (traceability):**

- Part A → Area 2 (Ubiquitous Language Follow-Up).
- Part B → Area 3 (Conversation Layout Planning).
- Part C → Area 4 (Rich Content Rendering Planning).
- Part D → Area 5 (Navigation, Search & Filtering Planning).
- Part E → Area 6 (Cross-Platform Desktop Review).
- Part F → Area 7 (Accessibility Review).
- Part G → Area 8 (Testing Strategy).
- Part H → Area 9 (HITL Review & Grill Feedback).

**HITL-visible outcome:** The HITL can confirm every lettered Sprint part appears in the mapping above.

### 1.6 Map every sprint "After" section to a HITL-visible outcome

- [x] Ensure every Sprint "After" section maps to at least one reviewable task outcome

**Description:** Confirm each Sprint part's "After" section is represented by a HITL-visible outcome in this document.

**Source:** Sprint doc — "After" sections in Parts A–H.

**Completion criteria:**

- Each Part's "After" outcome corresponds to a task HITL-visible outcome (Areas 2–9) and to a HITL Review Checkpoint (
  section 8).

**HITL-visible outcome:** The HITL can trace each "After" section to a checkpoint in section 8.

## 2. Ubiquitous Language Follow-Up

### 2.1 Review the ubiquitous language document

- [x] Review `docs/UBIQUITOUS-LANGUAGE.md`

**Description:** Review the glossary for completeness and accuracy against the sprint's needs.

**Source:** Sprint doc — Part A.

**Completion criteria:** All 20 required terms are defined with Definition and Notes; Patch-vs-Diff and Human-vs-user
distinctions are present.

**HITL-visible outcome:** HITL confirmed terms are "Accurate and useful" (Sprint doc section 18).

### 2.2 Identify terms needing refinement

- [x] Identify any terms that need refinement

**Description:** Note any ambiguous or missing definitions.

**Source:** Sprint doc — Part A.

**Completion criteria:** No refinements required per HITL feedback; result recorded in section 11.

### 2.3 Ensure task language uses approved terms

- [x] Ensure this task document uses approved terms

**Description:** Cross-check task wording against the glossary. `Depends on: 1.4`.

**Source:** `docs/UBIQUITOUS-LANGUAGE.md`.

**Completion criteria:** Consistent term usage throughout this document.

### 2.4 Record new terms discovered during planning

- [x] Record any new terms discovered during task planning

**Description:** Capture any new domain terms in the glossary before use.

**Source:** Sprint doc — "4. Ubiquitous Language References".

**Completion criteria:** No new terms were required during planning; recorded in section 11.

## 3. Conversation Layout Planning

`Source: Sprint doc — Part B, sections 8–9.`

### 3.1 Human message presentation

- [x] Plan compact, visually anchored Human Message presentation

**Description:** Define how short Human Messages are presented so they do not dominate.

**Completion criteria:** Layout requirement documented (Sprint doc section 9).

**HITL-visible outcome:** HITL can confirm Human Messages are compact and distinct in the layout proposal.

### 3.2 Junie response presentation

- [x] Plan full-width, long-form Junie Message presentation

**Description:** Define full readable-width presentation for long Junie content.

**Completion criteria:** Documented in Sprint doc section 9.

### 3.3 Asymmetric conversation layout

- [x] Plan an asymmetry-aware layout (not symmetric chat bubbles)

**Description:** Ensure the layout is designed around asymmetry (UX Principle 1).

**Completion criteria:** Documented in Sprint doc section 8 (UX Principles).

### 3.4 Long response readability

- [x] Plan for readable long Junie Responses

**Description:** Define wrapping/scroll so long Responses stay readable.

**Completion criteria:** Documented in Sprint doc sections 9–10.

**HITL-visible outcome:** HITL can confirm long Junie Responses are readable.

### 3.5 Conversation ordering and context

- [x] Plan unambiguous Message ordering and Session context visibility

**Description:** Ensure chronological order stays clear even with Filters active; Session context visible without
scrolling to top.

**Completion criteria:** Documented in Sprint doc section 9.

### 3.6 Empty, loading, and error states

- [x] Plan empty, loading, and error states for the Conversation view

**Description:** Define behaviour for no-Session, loading, and load-failure states.

**Completion criteria:** Loading and error handling are covered by existing ViewModel state and referenced in the layout
plan; recorded in section 11 as an assumption for the implementation sprint.

### 3.7 Responsive desktop window sizing

- [x] Plan responsive desktop window sizing

**Description:** Define default/minimum window sizes and reflow behaviour.

**Completion criteria:** Documented in Sprint doc section 11 (Window sizing).

### 3.8 Visual hierarchy

- [x] Plan visual hierarchy (Turn grouping, Kind markers, chrome)

**Description:** Define grouping of consecutive Junie Messages into a Turn and persistent chrome.

**Completion criteria:** Documented in Sprint doc section 9 and Part B.

**HITL-visible outcome:** HITL can confirm Turn grouping and chrome placement in the layout proposal.

## 4. Rich Content Rendering Planning

`Source: Sprint doc — Part C, section 10.`

### 4.1 Plain text

- [x] Plan plain-text rendering (wrapped, selectable body typography)

**Completion criteria:** Row present in Sprint doc section 10 (included now).

### 4.2 Markdown-like content

- [x] Plan Markdown core-subset rendering (headings, emphasis, lists, inline code)

**Completion criteria:** Included-now with deferred complex tables (Sprint doc section 10).

### 4.3 Code blocks

- [x] Plan fenced code block rendering (monospace, highlighting, copy, horizontal scroll)

**Completion criteria:** Included-now (Sprint doc section 10).

### 4.4 Code diffs / patches

- [x] Plan Patch/Diff rendering (unified-diff styling, copy affordance)

**Completion criteria:** Included-now; Patch vs Diff distinction honoured (Sprint doc section 10).

### 4.5 Tool calls

- [x] Plan Tool Call rendering (Structured Output, collapsible, tool name header)

**Completion criteria:** Included-now (Sprint doc section 10).

### 4.6 Terminal output

- [x] Plan Terminal Output rendering (monospace, `$`-prefixed command, preserved whitespace)

**Completion criteria:** Included-now (Sprint doc section 10).

### 4.7 Structured output

- [x] Plan Structured Output rendering (JSON/code formatting now; rich tables/plans deferred)

**Completion criteria:** Marked Partial (Sprint doc section 10).

### 4.8 Errors and warnings

- [x] Plan distinct error/warning rendering (accent + icon/label, never blended)

**Completion criteria:** Included-now (Sprint doc section 10).

**HITL-visible outcome:** HITL can confirm errors/warnings are visually distinct.

### 4.9 Deferred rendering enhancements

- [x] Explicitly list deferred rendering (plans/summaries affordances, rich tables, language-aware highlighting)

**Completion criteria:** Deferred items listed in Sprint doc sections 10 and 19; remain readable as text.

**HITL-visible outcome:** HITL confirmed "Nothing missing" from the output-type list (Sprint doc section 18).

## 5. Navigation, Search, and Filtering Planning

`Source: Sprint doc — Part D, sections 8 & 13.`

### 5.1 Search behavior

- [x] Plan Search Query behaviour (case-insensitive substring over Message content)

**Completion criteria:** Documented in Sprint doc section 13; matches current ViewModel behaviour.

### 5.2 Filter behavior

- [x] Plan Filter behaviour (Sender + Message Kind toggles, AND-combined with Search)

**Completion criteria:** Documented in Sprint doc sections 10/13.

### 5.3 Jumping between matches / notable events

- [x] Plan match-to-match navigation as an open question

**Description:** Whether next/previous match navigation ships this sprint is open (Q3).

**Completion criteria:** Recorded as open question Q3 (Sprint doc sections 14 & 19) — `Deferred` decision by HITL to
keep open.

### 5.4 Maintaining orientation in long conversations

- [x] Plan long-Turn orientation (context preservation)

**Completion criteria:** Documented in Sprint doc sections 8 (Principle 5) & 15 Part D.

### 5.5 Handling conversations with many messages

- [x] Plan for large Conversations (lazy list, scroll performance)

**Completion criteria:** Risk R2 documented (Sprint doc section 14); lazy-list approach noted.

### 5.6 Keyboard and mouse interactions

- [x] Plan keyboard + mouse interactions for navigation

**Completion criteria:** Cross-platform shortcuts and scroll documented (Sprint doc sections 11–12).

**HITL-visible outcome:** HITL can confirm the described navigation flow keeps them oriented in long Turns.

## 6. Cross-Platform Desktop Review

`Source: Sprint doc — Part E, section 11.`

- [x] 6.1 Review macOS behaviour (Cmd modifier, native fonts/scroll) — Sprint doc section 11.
- [x] 6.2 Review Windows behaviour (Ctrl modifier, drive-letter paths) — Sprint doc section 11.
- [x] 6.3 Review Linux behaviour (Ctrl modifier, path separators) — Sprint doc section 11.
- [x] 6.4 Review font rendering across all three OSes — Sprint doc section 11.
- [x] 6.5 Review scrolling behaviour (trackpad, wheel, scrollbar, direction) — Sprint doc section 11.
- [x] 6.6 Review clipboard/copy fidelity (clean plain text) — Sprint doc section 11; Risk R3.
- [x] 6.7 Review keyboard shortcuts (focus Search, next/prev match, clear Search) — Sprint doc section 11.
- [x] 6.8 Review file path display (`~`, drive letters, separators) — Sprint doc section 11.
- [x] 6.9 Review platform-neutral visual design — Sprint doc section 11.

**Completion criteria:** Each item has a documented expectation in Sprint doc section 11.

**HITL-visible outcome:** HITL can confirm all three OSes have defined expectations and the design is platform-neutral.

## 7. Accessibility Review

`Source: Sprint doc — Part F, section 12.`

- [x] 7.1 Plan keyboard navigation (Search, Filters, match navigation fully operable) — section 12.
- [x] 7.2 Plan screen-reader-friendly labels/semantics (semantic labels = test tags) — section 12.
- [x] 7.3 Plan colour contrast (light + dark schemes) — section 12.
- [x] 7.4 Plan scalable text (tolerate larger font scales) — section 12.
- [x] 7.5 Plan focus order (follows reading/chronological order) — section 12.
- [x] 7.6 Plan non-colour-only status indicators (label/icon/shape) — section 12.

**Completion criteria:** Each accessibility rule documented in Sprint doc section 12.

**HITL-visible outcome:** HITL can confirm colour is never the sole signal, UI is keyboard operable, and semantic labels
are defined.

## 8. Testing Strategy

`Source: Sprint doc — Part G, section 13; grounded in docs/TESTING.md.`

- [x] 8.1 Identify UI tests for the Conversation view — Sprint doc section 13.
- [x] 8.2 Identify `ConversationRobot` helper updates (select Session, type Search Query, toggle Filter, next match,
  assert Message Kind visible) — section 13.
- [x] 8.3 Identify semantic tags needed for testable UI (Message container, Sender marker, Kind marker, Search field,
  Filter toggle, match indicator) — section 13.
- [x] 8.4 Plan Search tests — section 13.
- [x] 8.5 Plan Filter tests (AND-combination, clear restores full Conversation, order preserved) — section 13.
- [x] 8.6 Plan representative Message-Kind fixture tests — section 13.
- [x] 8.7 Plan long-Junie-response smoke + manual/visual check — section 13.
- [x] 8.8 Identify manual HITL visual checks — sections 13 & 16.
- [x] 8.9 Record testing gaps (visual-regression deferred) — section 19.

**Completion criteria:** Testing plan in Sprint doc section 13 names tags, Robot helpers, Search/Filter tests,
representative-content tests, and long-response checks.

**HITL-visible outcome:** HITL confirmed the plan gives "Enough confidence" (Sprint doc section 18).

## 9. HITL Review and Grill Feedback

`Source: Sprint doc — Part H, sections 16 & 18.`

- [x] 9.1 Prepare the six required HITL review questions — Sprint doc section 16.
- [x] 9.2 Run the `grill-with-docs` HITL review process — recorded in Sprint doc section 18.
- [x] 9.3 Capture HITL feedback — Sprint doc section 18 (Feedback Log).
- [x] 9.4 Update task/sprint status after feedback (sprint marked `reviewed`).
- [x] 9.5 Record incorporated feedback — Sprint doc section 18.
- [x] 9.6 Record deferred feedback — Sprint doc section 19 (visual-regression, Q1–Q5).

**Completion criteria:** `grill-with-docs` was run; all six points and deferrals are recorded.

**HITL-visible outcome:** HITL can confirm their six responses and deferrals are captured in Sprint doc sections 18–19.

## 10. Final Sprint Readiness Review

`Source: Sprint doc — section 17 (Definition of Done).`

- [x] 10.1 Verify every task has completion criteria — this document.
- [x] 10.2 Verify every major task/area has a HITL-visible outcome — Areas 1–9.
- [x] 10.3 Verify this task document aligns with the sprint Definition of Done — Sprint doc section 17.
- [x] 10.4 Verify deferred items are explicitly documented — Sprint doc section 19; this doc section 10.
- [ ] 10.5 Final HITL approval checkpoint `HITL Review` — awaiting standalone HITL sign-off of this task document.

**Completion criteria:** All readiness checks pass and the HITL grants final approval of this task document.

**HITL-visible outcome:** HITL can confirm this task document is complete, traceable, and ready to drive the
implementation sprint.

# 8. HITL Review Checkpoints

- [x] HITL confirms the task breakdown matches the sprint document. *(Traceability in task 1.5.)*
- [x] HITL confirms the "After" sections from the sprint are represented by reviewable task outcomes. *(Task 1.6.)*
- [x] HITL confirms the rich content rendering plan covers the expected Junie output types. *(Grill review point 5 — "
  Nothing missing".)*
- [x] HITL confirms testing tasks provide enough confidence for implementation. *(Grill review point 6 — "Enough
  confidence".)*
- [x] HITL confirms deferred items are acceptable. *(Q1–Q5 and visual-regression left deferred by HITL.)*
- [ ] HITL grants final approval of this task document. *(Task 10.5 — outstanding.)*

# 9. Acceptance Criteria

The task document is complete when:

- [x] It exists under `docs/tasks/`.
- [x] It links to the related sprint document.
- [x] It references `docs/UBIQUITOUS-LANGUAGE.md`.
- [x] It breaks every Sprint part (A–H) into concrete tasks. *(Task 1.5 mapping.)*
- [x] Every task has a checkbox.
- [x] Every task has completion criteria.
- [x] Review-oriented tasks include HITL-visible outcomes.
- [x] The document includes progress summary, HITL checkpoints, acceptance criteria, deferred items, and
  notes/decisions.
- [x] No task is marked complete unless it has actually been completed.
- [ ] The document has received final HITL approval. *(Task 10.5 — outstanding.)*

# 10. Deferred / Out-of-Scope Items

Carried from the sprint (see Sprint doc sections 6 & 19):

- **Application code** — no production Compose/ViewModel code in this sprint (design/planning only).
- **Export** — Markdown/HTML export of Conversations is out of scope.
- **Multi-Session** — cross-Session comparison/search is out of scope.
- **Streaming** — live-tailing an in-progress Session is out of scope.
- **Screenshot / visual-regression testing** — deferred; functional + visual-review plan deemed sufficient.
- **Open questions Q1–Q5** — deliberately left open for the implementation sprint:
    - Q1: timestamp availability and display (inline / hover / per Turn).
    - Q2: whether Thoughts and Tool Calls collapse by default.
    - Q3: whether match-to-match navigation ships this sprint.
    - Q4: whether a Turn-outline / jump-to-Turn navigator is in scope now.
    - Q5: whether language-aware syntax highlighting is expected.
- **Rich Structured Output** (tables/plans affordances) and **dedicated plans/summaries rendering** — deferred (Sprint
  doc section 10).

# 11. Notes / Decisions Log

- **2026-07-07 — Single task document chosen.** The sprint is reviewable comfortably in one file, so a single
  `conversation-ui-design-tasks.md` is used rather than split files.
- **2026-07-07 — Design-content tasks marked complete.** All design/planning artefacts required by the sprint already
  exist in the Sprint document (sections 1–19, Parts A–H) and `docs/UBIQUITOUS-LANGUAGE.md`; the corresponding planning
  tasks are therefore complete.
- **2026-07-07 — HITL grill already run.** The six required review points were answered via `grill-with-docs` (Sprint
  doc section 18); all confirmed as-is, Q1–Q5 and visual-regression testing deferred.
- **2026-07-07 — Ubiquitous language stable.** HITL confirmed terms are accurate and useful; no refinements and no new
  terms surfaced during task planning (tasks 2.2, 2.4).
- **2026-07-07 — Empty/loading/error states (task 3.6).** Loading and error states already exist in
  `ConversationViewModel`; the empty (no-Session) state is an assumption to be finalised in the implementation sprint.
- **Outstanding item.** Task 10.5 / final HITL approval of *this task document* remains unchecked — it cannot be
  self-certified and needs a standalone HITL sign-off.

**Assumptions made:**

- The Conversation UI Design sprint is unambiguously
  `docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-design.md` (the only Sprint 2 /
  conversation-UI-design document), so no HITL disambiguation was needed.
- "Completion" for planning tasks means a documented, reviewable artefact exists, not shipped code.
