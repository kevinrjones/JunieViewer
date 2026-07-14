---
sessionId: session-260713-155706-1u7m
---

# Requirements

### Overview & Goals
Produce two high-quality planning documents (a **sprint document** and a **tasks document**) that define Sprint 3 — *UI Polish and Theme Refresh* — for the Junie Conversation Viewer. The documents lay out how to move the UI from functional-but-basic (a bare default `MaterialTheme {}`) to a polished, modern, readable, visually coherent desktop app, using the **LogViewer** app (`~/Dropbox/projects/utilities/LogViewer`) as design inspiration plus research into modern conversation/chat-transcript UIs.

**No UI implementation happens in this task** — only the planning documents are created.

### Scope
**In scope**
- New sprint doc: `JunieConversationViewer/docs/sprints/junie-conversation-viewer-sprint-3-ui-polish-and-theme-refresh.md`
- New tasks doc: `JunieConversationViewer/docs/tasks/junie-conversation-viewer-tasks-sprint-3-ui-polish-and-theme-refresh.md`
- Both follow the established style of the existing Sprint 2 docs (YAML frontmatter, numbered sections, Related Documents, Areas, per-task Description/Source/Dependencies/Likely files/Completion criteria/Testing, HITL checkpoints, status legend, progress summary).

**Out of scope**
- Any changes to Kotlin/Compose source code, tests, or build files.
- Completing the in-progress Sprint 2 areas (noted as a dependency/open question in the sprint doc).

### Confirmed Design Decisions (from HITL)
- **Theme modes:** light + dark + follow-system, with a manual override persisted via the existing `Preferences`/`PreferencesRepository`.
- **Theme architecture:** Material 3 `lightColorScheme`/`darkColorScheme` plus app-specific semantic tokens (message roles, code/diff/terminal, spacing, shape, elevation) exposed via `CompositionLocal` — adapting LogViewer's `KLogViewerColors`/`KLogViewerTheme` pattern idiomatically to M3.
- **Session navigation:** restyle the existing `SessionSelector` flow (density, hierarchy, hover/selection/focus states); no sidebar.

### Functional Requirements for the Documents
- Sprint doc includes every section listed in the issue: requirements, goals, current baseline, design findings (LogViewer + conversation-app research), scope/out-of-scope, user stories, functional & non-functional requirements, design principles, proposed visual system (colour tokens, typography, shape/elevation, spacing/density, component hierarchy), conversation/session-navigation/rich-content improvements, accessibility, cross-platform (macOS/Windows/Linux), testing strategy, risks, open questions, incremental delivery plan with per-part "After this part" HITL-visible outcomes, and Definition of Done.
- Tasks doc mirrors the 9 suggested areas (discovery/audit, theme foundation, chrome polish, conversation surface, rich content, states & feedback, accessibility & cross-platform, testing & review, documentation & completion), each task with checkbox, description, source, dependencies, likely files, completion criteria, testing expectations, and HITL-visible outcome.
- Domain language from `docs/UBIQUITOUS-LANGUAGE.md` (Session, Conversation, Turn, Message, Human/Junie, etc.) is preserved throughout.

# Technical Design

### Current Implementation (audit findings)
- App entry: `shared/src/commonMain/.../App.kt` wraps everything in a **default `MaterialTheme {}`** — no custom colours, typography, shapes, or dark mode.
- Screens/components: `ui/ConversationScreen.kt` (top bar, filter bar, loading/error/empty states), `ui/components/` — `MessageItems.kt`, `SessionSelector.kt`, `SessionContextHeader.kt`, `FilterBar.kt`, `MarkdownContent.kt`, `CodeBlock(WithCopy).kt`, `DiffBlock.kt`, `TerminalOutputBlock.kt`, `ThoughtBlock.kt`, `ToolCallBlock.kt`, `StructuredOutputBlock.kt`, `ErrorWarningBlock.kt`, `SettingsDialog.kt`, `FatalErrorDialog.kt`. Several use ad-hoc styling and text glyphs (e.g. "▲"/"▼") rather than themed primitives.
- MVI pattern: `ConversationViewModel`/`ConversationState`/`ConversationEvent`/`ConversationAction`; preferences persisted via `PreferencesRepository`.
- Strong existing Compose test suite in `shared/src/commonTest/kotlin/.../ui/` (robot pattern, accessibility, rich-content, states tests) — the sprint doc will require these keep passing and be extended.

### Design Inspiration Findings
**LogViewer** (`ui/src/main/kotlin/com/klogviewer/ui/theme/`):
- `KLogViewerColors` object: dual palettes — *Industrial Dark* (`#2B2B2B` bg, `#3C3F41` surface, `#00A3E0` accent) and *Clean Light* (`#FFFFFF`/`#F5F5F5`, `#007ACC` accent), plus semantic per-log-level colours.
- `KLogViewerTheme`: compact 13sp sans-serif UI typography, semantic colours exposed via `staticCompositionLocalOf` + a theme accessor object — the pattern we adapt to M3 (semantic **message-role/kind** colours instead of log levels).

**Conversation-app research** (to be captured in the sprint doc): role-coloured accent rails/avatars for Human vs Junie distinction (Slack/Discord/AI chat), constrained line length for long-form readability, turn grouping with shared metadata headers, collapsed-by-default secondary detail (thoughts/tool calls), sticky context headers, subtle hover/selection states, restrained chrome.

### Proposed Changes (document content, not code)
The sprint doc will specify a future `JunieViewerTheme` composable in a new `ui/theme/` package:
```kotlin
@Composable
fun JunieViewerTheme(themeMode: ThemeMode = ThemeMode.System, content: @Composable () -> Unit)

data class ConversationColors(/* humanAccent, junieAccent, thought, toolCall, terminal, diffAdded, diffRemoved, codeBackground, ... */)
data class JunieViewerSpacing(/* xs..xl density scale */)
val LocalConversationColors = staticCompositionLocalOf<ConversationColors> { ... }
```
with `ThemeMode` (`Light`/`Dark`/`System`) persisted in `Preferences`, and a toggle surfaced in `SettingsDialog`/top bar.

### File Structure
- **Added:** `docs/sprints/junie-conversation-viewer-sprint-3-ui-polish-and-theme-refresh.md`, `docs/tasks/junie-conversation-viewer-tasks-sprint-3-ui-polish-and-theme-refresh.md` (both under `JunieConversationViewer/docs/`).
- **Modified:** none (source code untouched).

### Risks
- Sprint 2 Areas 3–6 are still in progress — the sprint doc will state the dependency and sequencing assumption explicitly as an open question.
- Doc drift: tasks doc must be cross-checked against sprint delivery parts for 1:1 traceability.

### Open Questions (recorded in the sprint doc for HITL)
- Should Sprint 3 wait for Sprint 2 completion or run against the current baseline?
- Adopt LogViewer's exact accent colours (`#00A3E0`/`#007ACC`) or a Junie-branded accent?
- Is a monospace-font token for code/diff/terminal sufficient, or should a bundled font be considered (would need dependency justification)?

# Testing

### Validation Approach
Since this task only creates planning documents, validation is documentary:
- Verify both files exist at the specified paths and render as valid Markdown.
- Check the sprint doc contains **every** section required by the issue (checklist review).
- Check every task in the tasks doc has checkbox, description, source, dependencies, likely files, completion criteria, testing expectations, and (where relevant) HITL-visible outcome.
- Cross-check: every sprint delivery part maps to a tasks-doc area, and no task introduces work absent from the sprint doc.
- Confirm domain terms match `docs/UBIQUITOUS-LANGUAGE.md` and style matches the Sprint 2 documents (frontmatter, numbered sections, status legend, progress summary table).

### Key Scenarios
- A future implementation agent can execute the tasks doc without rediscovering design intent (file paths, token names, palette values, and acceptance criteria are concrete).
- Each delivery part has a visually inspectable "After this part" outcome for HITL review.

### Edge Cases
- Accessibility and cross-platform items appear inside early areas (theme foundation), not only in a late area.
- Testing expectations reference the existing test files (e.g. `ConversationScreenTest.kt`, `AccessibilityAndArea8Test.kt`, `RichContentRenderingTest.kt`) so regressions are guarded.

# Delivery Steps

### ✓ Step 1: Capture design research and baseline audit material
All research needed to write the documents is consolidated: current-UI audit, LogViewer findings, and conversation-app patterns.

- Complete the audit of `ConversationScreen.kt` and all `ui/components/*` for ad-hoc styling, hardcoded values, and missing states.
- Extract the concrete LogViewer palette, typography, and CompositionLocal token pattern from `KLogViewerColors.kt`/`KLogViewerTheme.kt` as adaptable M3 findings.
- Summarize conversation/chat-transcript UI patterns (role distinction, turn grouping, collapsible detail, readability, metadata placement, navigation affordances).
- Re-read `UBIQUITOUS-LANGUAGE.md`, `TESTING.md`, `project_memory.md`, `RECAP.md`, and the Sprint 2 docs to lock terminology and document style.

### ✓ Step 2: Write the Sprint 3 sprint document
`docs/sprints/junie-conversation-viewer-sprint-3-ui-polish-and-theme-refresh.md` exists with all required sections in the established sprint-doc style.

- YAML frontmatter, title, Related Documents, requirements, goals, current UI baseline, and design-inspiration findings (LogViewer + conversation apps).
- Scope/out-of-scope, user stories, functional and non-functional requirements, design principles.
- Proposed visual system: colour tokens (light/dark palettes with concrete hex values, semantic conversation tokens), typography scale, shape/elevation/border, spacing/density, component hierarchy, `JunieViewerTheme`/`ThemeMode` design with persisted preference.
- Proposed conversation-surface, session-navigation (restyled selector), and rich-content improvements; accessibility requirements; macOS/Windows/Linux considerations; testing strategy; risks; open questions.
- Incremental delivery plan (theme foundation → chrome → conversation surface → rich content → states → accessibility/cross-platform → testing → docs) with an "After this part" HITL-visible outcome per part, and a Definition of Done.

### ✓ Step 3: Write the Sprint 3 tasks document
`docs/tasks/junie-conversation-viewer-tasks-sprint-3-ui-polish-and-theme-refresh.md` exists, matching the Sprint 2 tasks-doc format.

- Header sections: related sprint link, related documents, purpose, how-to-use, progress summary table, task status legend.
- Areas 1–9 (discovery/audit, theme/token foundation, application chrome, conversation surface, rich content, states & feedback, accessibility & cross-platform, testing & review, documentation & completion), each task with checkbox, description, source, dependencies, likely files (concrete paths such as `ui/theme/JunieViewerTheme.kt`, `MessageItems.kt`, `SettingsDialog.kt`), completion criteria, testing expectations, and HITL-visible outcome.
- HITL review checkpoints, acceptance criteria, deferred/out-of-scope items, and a notes/decisions log seeded with the confirmed decisions (light+dark+system, M3 + extended tokens, restyled selector).

### ✓ Step 4: Cross-check consistency and report
Both documents are verified consistent, complete, and ready for HITL sign-off.

- Verify 1:1 traceability between sprint delivery parts and tasks-doc areas; fix any drift.
- Run the section checklist from the issue against both documents; confirm domain terms match `UBIQUITOUS-LANGUAGE.md`.
- Report the files created, a summary of the proposed sprint, major design assumptions, and open questions needing HITL input before implementation.