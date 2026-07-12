---
sessionId: session-260709-111457-1utg
---

# Requirements

### Overview & Goals

Create a detailed task document at `docs/tasks/junie-conversation-viewer-tasks-sprint-2-conversation-ui-implementation.md` that breaks the Sprint 2 Conversation UI Implementation sprint into clear, reviewable, trackable implementation tasks.

The document serves both **Junie** (implementation checklist) and **HITL** (review/progress checklist).

**Filename assumption:** The user referenced `docs/dprints/...` which is treated as a typo for the existing `docs/sprints/...` path.

### Scope

**In Scope:**
- Single task document covering all 8 delivery parts from the implementation sprint
- 10 task areas as specified in the issue (Sprint Alignment, UI Baseline, Layout, Rich Content, Search/Filter/Nav, States, Accessibility/Cross-Platform, Testing, HITL Review, Final Completion)
- HITL Review Checkpoints section
- Acceptance Criteria, Deferred/Out-of-Scope, Notes/Decisions sections
- Progress Summary table

**Out of Scope:**
- Any application code implementation
- Modifying existing sprint or design documents

### Functional Requirements

- All tasks use Markdown checkbox syntax (`- [ ]` / `- [x]`)
- No task is marked complete unless already done
- Each task includes: description, source sprint section, dependencies, likely files/areas, completion criteria, testing expectations, HITL-visible outcome where appropriate
- Document follows the 12-section structure specified in the issue
- Tasks trace back to delivery Parts 1–8 and design Parts A–H

# Technical Design

### Current Implementation

The project has an existing task document pattern at `docs/tasks/conversation-ui-design-tasks.md` (68 tasks across 10 areas with progress summary table). The new document follows a similar but more detailed format per the issue requirements.

### Key Source Documents

 Document | Role |
---|---|
 `docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md` | Primary source — 8 delivery parts with "After" sections |
 `docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-design.md` | Design traceability (Parts A–H) |
 `docs/tasks/conversation-ui-design-tasks.md` | Design task traceability |
 `docs/UBIQUITOUS-LANGUAGE.md` | Canonical domain terms |
 `docs/TESTING.md` | Testing stack, Robot pattern, Gradle commands |
 `docs/project_memory.md` | Architecture decisions and gotchas |
 `docs/RECAP.md` | Chronological project history |

### Delivery Part → Task Area Mapping

 Delivery Part | Task Areas |
---|---|
 Part 1 — UI baseline | Areas 1 (Sprint Alignment), 2 (UI Baseline) |
 Part 2 — Asymmetric layout | Area 3 (Layout) |
 Part 3 — Rich content | Area 4 (Rich Content) |
 Part 4 — Search/filter/nav | Area 5 (Search/Filter/Nav) |
 Part 5 — Session context/states | Area 6 (States) |
 Part 6 — Accessibility/cross-platform | Area 7 (Accessibility) |
 Part 7 — Tests | Area 8 (Testing) |
 Part 8 — Final review/docs | Areas 9 (HITL Review), 10 (Final Completion) |

### File Structure

**Created:**
- `docs/tasks/junie-conversation-viewer-tasks-sprint-2-conversation-ui-implementation.md`

### Document Sections

1. Title
2. Related Sprint (with 2–4 sentence goal summary + traceability links)
3. Related Documents (7 documents with brief role descriptions)
4. Purpose
5. How to Use This Task Document
6. Progress Summary (table with area/status/count)
7. Task Status Legend (`[ ]`, `[x]`, inline markers: HITL Review, Blocked, Deferred, Depends on, Test Required, Manual Review Required)
8. Implementation Task List (Areas 1–10, ~80–100 tasks)
9. HITL Review Checkpoints (11 checkboxed items)
10. Acceptance Criteria
11. Deferred / Out-of-Scope Items (from sprint section 7 + open questions Q1–Q5)
12. Notes / Decisions Log

### Key Files Referenced in Tasks

- `ui/ConversationScreen.kt`, `ui/ConversationViewModel.kt`, `ui/ConversationState.kt`
- `components/CodeBlock.kt`, `components/FilterBar.kt`, `components/FatalErrorDialog.kt`
- `domain/Message.kt`, `data/SessionRepository.kt`
- `shared/src/commonTest/kotlin/...` (tests)
- Gradle commands: `./gradlew test`, `./gradlew :shared:jvmTest`

# Delivery Steps

### ✓ Step 1: Create document skeleton with metadata sections
The task document exists at `docs/tasks/junie-conversation-viewer-tasks-sprint-2-conversation-ui-implementation.md` with sections 1–7 and 9–12 populated.

- Create the file with Title, Related Sprint (summarising the sprint goal and traceability to design sprint + design tasks), Related Documents (all 7 docs with role summaries), Purpose, How to Use This Task Document.
- Add Progress Summary table with 10 task areas, all showing `Not started` status.
- Add Task Status Legend with checkbox syntax and inline markers.
- Add empty HITL Review Checkpoints section with the 11 specified checkbox items.
- Add Acceptance Criteria section matching the issue requirements.
- Add Deferred / Out-of-Scope Items section populated from sprint section 7 (real-time tailing, full Markdown parser, advanced syntax highlighting, complex virtualised navigation, editing/annotating, cloud sync, export, mobile UI, modifying UBIQUITOUS-LANGUAGE.md) plus open questions Q1–Q5.
- Add Notes / Decisions Log section (empty, ready for entries).

### ✓ Step 2: Write task areas 1–4 (Alignment, Baseline, Layout, Rich Content)
Task areas 1–4 are fully written with all subtasks, each following the specified format.

- **Area 1 — Sprint Alignment and Traceability (~10 tasks):** Reading implementation/design/task docs, reading UBIQUITOUS-LANGUAGE/RECAP/TESTING/project_memory, confirming scope and out-of-scope, mapping delivery parts to tasks, mapping "After" sections to HITL-visible outcomes, recording assumptions.
- **Area 2 — UI Implementation Baseline (~8 tasks):** Reviewing `ConversationRoot`/`ConversationScreen`/`ConversationViewModel`, preserving launch/search/filter behaviour, identifying semantic tags, establishing fixture data, running baseline test suite (`./gradlew :shared:jvmTest`).
- **Area 3 — Asymmetric Human/Junie Conversation Layout (~10 tasks):** Compact right-inset Human messages, full-width left-inset Junie messages, sender labels, message spacing, content width, Turn container/Turn Header, message order, scroll behaviour, HITL visual review. Source: delivery Part 2.
- **Area 4 — Rich Content Rendering (~13 tasks):** Plain text, Markdown core subset, fenced code blocks with copy affordance, Patch/Diff styling, Terminal Output, Tool Call summaries (collapsible), Structured Output fallback, errors/warnings, representative test fixtures per Message Kind, documenting deferred enhancements. Source: delivery Part 3.
- Each task includes description, source sprint section, dependencies, likely files/areas, completion criteria, testing expectations, and HITL-visible outcome where appropriate.

### ✓ Step 3: Write task areas 5–7 (Search/Filter, States, Accessibility)
Task areas 5–7 are fully written with all subtasks.

- **Area 5 — Search, Filters, and Navigation (~12 tasks):** Reviewing current search/filter behaviour, search field placement, text search preservation, filter chip layout, Message Kind filter clarity, no-results state, result count, keyboard/mouse interaction, long-conversation orientation, search+filter combination testing, HITL usability review. Source: delivery Part 4.
- **Area 6 — Session Context, Empty, Loading, and Error States (~8 tasks):** Session context display, no-Session-selected state, empty Conversation state, loading state (`loading_indicator`), recoverable error state (`error_state`/`FatalErrorDialog`), malformed content fallback, error copy clarity, testing/manual review of each state. Source: delivery Part 5.
- **Area 7 — Accessibility and Cross-Platform Desktop Polish (~13 tasks):** Keyboard focus order, screen-reader labels/semantics, colour contrast, scalable text, non-colour-only indicators, macOS/Windows/Linux visual+behaviour reviews, font rendering, scrolling, clipboard/copy, keyboard shortcuts, platform-neutral file path display. Source: delivery Part 6.

### ✓ Step 4: Write task areas 8–10 (Testing, HITL Review, Final Completion) and finalise
Task areas 8–10 are fully written and the Progress Summary table is updated with accurate task counts.

- **Area 8 — Automated Testing (~13 tasks):** Running `./gradlew :shared:jvmTest` before changes, Compose UI tests, Robot helper updates, semantic tags, Human/Junie rendering tests, rich content rendering tests, search/filter tests, no-results test, long Junie response test, empty/loading/error state tests, recording testing gaps, running final `./gradlew test`. Source: delivery Part 7, `docs/TESTING.md`.
- **Area 9 — HITL Review and Documentation (~12 tasks):** Preparing HITL checklist, reviews after baseline/layout (Part 2), after rich content (Part 3), after search/filter (Part 4), after accessibility (Part 6), final review (Part 8), capturing/incorporating/deferring feedback, updating docs/README if needed, updating task status. Source: delivery Part 8.
- **Area 10 — Final Sprint Completion (~7 tasks):** Verifying all delivery parts complete or deferred, verifying "After" outcomes, verifying tests pass, verifying HITL checkpoints, verifying no scope creep, recording final notes, getting HITL approval.
- Update the Progress Summary table with final task counts per area.
- Verify all acceptance criteria from the issue are met in the document.