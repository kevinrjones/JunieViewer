---
sessionId: session-260622-214658-1rvg
---

# Requirements

### Overview & Goals
Create a single, implementation-focused sprint document at exactly `docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md` that turns the Sprint 2 *conversation UI design* work into a concrete, reviewable implementation plan for the cross-platform (macOS/Windows/Linux) desktop Conversation UI.

This is **documentation/sprint-planning only** — no application code is written. The document must be detailed enough for Junie to implement from later and clear enough for the HITL to review progress against.

### Scope
**In scope**
- Read the six source documents first (see Related Documents) and use the design sprint + `conversation-ui-design-tasks.md` as the primary source of truth.
- Produce one Markdown sprint document containing all **21 required sections** (Title → Definition of Done).
- A Delivery Plan of **8 numbered parts**, each with Objective, Implementation tasks, Files/areas likely touched, Testing expectations, HITL review expectations, and a concrete **"After"** section.
- Use the project's ubiquitous language consistently; add a *Candidate Ubiquitous Language Additions* subsection if new terms are introduced.
- Ground the Testing Strategy in `docs/TESTING.md` (Robot pattern, `testTag`, `./gradlew test`, `./gradlew :shared:jvmTest`).
- Note the filename-typo assumptions in the final response.

**Out of scope**
- Any application/source code changes (Compose, ViewModel, repositories, tests).
- Modifying `docs/UBIQUITOUS-LANGUAGE.md` (any needed changes are listed as follow-up tasks only).
- Real-time session tailing, full Markdown-parser replacement, advanced syntax highlighting beyond current deps, virtualized navigation beyond sprint needs, editing logs, cloud sync/remote sessions, mobile UI.

### Functional Requirements
- Document created at the exact path above.
- All 21 sections present, in order.
- Related Documents section links all six docs and summarizes how each influenced the sprint.
- Every delivery part has a concrete, observable/reviewable "After" section (no vague statements).
- Implementation plan covers Human vs Junie asymmetric layout, readable long Junie Responses, rich output rendering (plain text, Markdown-like, fenced code, Patch/Diff, Terminal Output, Tool Call, Structured Output fallback), Search Query UI, Message Kind Filters, Session context/header, empty/loading/error states, accessibility semantics, Compose testability, and cross-platform desktop behavior.
- Definition of Done enumerates the required completion checks from the issue.
- Open Questions and Deferred/Out-of-Scope items are explicit (carry forward Q1–Q5 and visual-regression deferral from the design sprint).

### Filename assumptions (typos to correct)
- `...conversation-ui-design/md` → `...conversation-ui-design.md`
- `docs/RECAP.doc` → `docs/RECAP.md`; `docs/TESTING.doc` → `docs/TESTING.md`; `docs/project_memory.doc` → `docs/project_memory.md`
- `docs/UBITQUITOUS-LANGUAGE.md` → `docs/UBIQUITOUS-LANGUAGE.md`

# Technical Design

### Current Implementation (grounding for the sprint content)
The Conversation UI already exists in a minimal form under `JunieConversationViewer/shared/src/commonMain/kotlin/com/knowledgespike/junieviewer/`:
- **`ui/ConversationScreen.kt`** — `ConversationRoot` + `ConversationScreen` (Scaffold topBar with title, `session_picker_button`, `settings_button`, `search_field`, `FilterBar`) and a `LazyColumn` (`message_list`) rendering `MessageItem`. `MessageItem` already distinguishes `Sender.Human` (primaryContainer, right-inset) from `Sender.Junie` (secondaryContainer, left-inset) and renders `MessageContent.Text/Code/Diff` (Code via `components/CodeBlock.kt` with `dev.snipme.highlights`).
- **`ui/ConversationViewModel.kt` / `ConversationState.kt` / `ConversationAction.kt` / `ConversationEvent.kt`** — MVI state with `searchQuery`, `filter`, `filteredMessages`, session picker/settings flags, loading/error handling.
- **`domain/Message.kt`** — `Message(id, sender, content, kind, timestamp)`; `MessageKind = Text, Thought, Tool, Patch, Terminal`; `MessageContent = Text | Code | Diff`; `Sender = Human | Junie`.
- **`data/SessionRepository.kt`, `PreferencesRepository.kt`, `JsonlParser.kt`** — session loading and Junie `events.jsonl` parsing.
- **`components/`** — `CodeBlock`, `FilterBar`, `SessionSelector`, `SettingsDialog`, `FatalErrorDialog`.

The implementation sprint must reference these existing entry points so Part 1 builds on the current baseline (preserving existing Search/Filter behavior) rather than rewriting it.

### Key Decisions (to bake into the document)
- **Single sprint document** at the exact required path; source of truth is the design sprint + task document.
- **8 delivery parts** mirroring the issue's suggested parts (baseline → asymmetric layout → rich rendering → search/filter/nav → session context & states → a11y + cross-platform → tests → final HITL/docs), each mapped back to the design sprint's Parts A–H and "After" outcomes.
- **Reuse existing MVI + component architecture**; new rendering for Message Kinds not yet represented in `MessageContent` (Thought, Tool, Terminal, Structured Output) is described as a planned extension with a documented fallback (render as readable text) — no code written now.
- **Testing grounded in `docs/TESTING.md`**: Robot pattern (`ConversationRobot`), `Modifier.testTag(...)` semantic tags, `./gradlew test` and `./gradlew :shared:jvmTest` commands, representative fixtures per Message Kind, and a manual HITL cross-platform checklist.
- **Do not edit** `docs/UBIQUITOUS-LANGUAGE.md`; propose any additions under *Candidate Ubiquitous Language Additions* and as follow-up tasks.

### File Structure
- **Added:** `docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md` (only file created).
- **Read (not modified):** design sprint, `docs/tasks/conversation-ui-design-tasks.md`, `docs/UBIQUITOUS-LANGUAGE.md`, `docs/RECAP.md`, `docs/TESTING.md`, `docs/project_memory.md`.

### Document section outline (21 sections)
1. Title · 2. Related Documents · 3. Sprint Goal · 4. Background and Design Inputs · 5. Ubiquitous Language (+ Candidate Additions) · 6. Scope · 7. Out of Scope · 8. Assumptions · 9. User Stories · 10. Implementation Principles · 11. Proposed UI Implementation · 12. Rich Content Rendering Implementation Plan · 13. Search, Filter, and Navigation Implementation Plan · 14. Accessibility Implementation Plan · 15. Cross-Platform Desktop Considerations · 16. Testing Strategy · 17. Delivery Plan (8 parts) · 18. HITL Review Plan · 19. Risks and Mitigations · 20. Open Questions · 21. Definition of Done.

### Risks
- **Vague "After" sections** — mitigate by tying each to observable UI behavior or a reviewable artifact.
- **Scope drift into coding** — mitigate by keeping every part as *plan* content and marking code as follow-up.
- **Terminology drift** — mitigate by cross-checking against `docs/UBIQUITOUS-LANGUAGE.md`.

# Delivery Steps

### ✓ Step 1: Assemble source inputs and document skeleton
A skeleton of the implementation sprint document exists at the exact required path with all 21 headings and the Related Documents summaries.

- Confirm the readings of the six source docs (design sprint, `conversation-ui-design-tasks.md`, `UBIQUITOUS-LANGUAGE.md`, `RECAP.md`, `TESTING.md`, `project_memory.md`).
- Create `docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md` with all 21 section headings in order.
- Write sections 1–8: Title, Related Documents (links + one-line influence summary each), Sprint Goal, Background and Design Inputs, Ubiquitous Language (reference + Candidate Additions subsection), Scope, Out of Scope, Assumptions.
- Carry forward deferrals (Q1–Q5, visual-regression) from the design sprint.

### ✓ Step 2: Write UI, rendering, search/filter, a11y and cross-platform plans
Sections 9–15 fully describe how to implement the Conversation UI, grounded in the existing codebase.

- Section 9 User Stories and Section 10 Implementation Principles (asymmetric layout, readable long Responses, testability-first).
- Section 11 Proposed UI Implementation referencing existing `ConversationScreen.kt`/`MessageItem`/MVI state and describing Human vs Junie layout, sender labels, spacing, content width, scroll, session header, and empty/loading/error states.
- Section 12 Rich Content Rendering plan for plain text, Markdown-like, fenced code (`CodeBlock`), Patch/Diff, Terminal Output, Tool Call summaries, and Structured Output fallback, noting extensions beyond current `MessageContent`.
- Section 13 Search/Filter/Navigation (preserve existing `search_field`/`FilterBar`, Message Kind filters, no-results state, orientation in long Turns).
- Sections 14–15 Accessibility (focus order, semantic labels = test tags, contrast, scalable text, non-color-only indicators) and Cross-Platform desktop considerations (modifiers, fonts, scroll, clipboard, file-path display).

### ✓ Step 3: Write Delivery Plan, Testing Strategy, HITL plan, and closing sections
Sections 16–21 are complete, with 8 delivery parts each carrying a concrete "After" section and a Definition of Done that satisfies the issue's checklist.

- Section 17 Delivery Plan: 8 numbered parts (baseline; asymmetric layout; rich rendering; search/filter/navigation; session context & empty/loading/error; accessibility + cross-platform; tests; final HITL + docs). Each part includes Objective, Implementation tasks, Files/areas likely touched, Testing expectations, HITL review expectations, and a concrete observable "After".
- Section 16 Testing Strategy grounded in `docs/TESTING.md`: unit + Compose UI tests, `ConversationRobot` helper updates, `testTag` semantic tags, representative Message-Kind fixtures, search/filter/long-response/state tests, manual HITL cross-platform checklist, and `./gradlew test` / `./gradlew :shared:jvmTest` commands.
- Section 18 HITL Review Plan with checkpoints (after layout, rich rendering, search/filter/nav, a11y/cross-platform, and before completion) stating what to verify at each.
- Sections 19–21 Risks and Mitigations, Open Questions (incl. Q1–Q5), and Definition of Done covering every required completion check.
- Verify traceability back to the design sprint Parts A–H and note the corrected filename assumptions in the final response.