# Sprint 3 — UI Polish and Theme Refresh: Task Breakdown

## 1. Related Sprint

**Sprint document:** [`docs/sprints/junie-conversation-viewer-sprint-3-ui-polish-and-theme-refresh.md`](../sprints/junie-conversation-viewer-sprint-3-ui-polish-and-theme-refresh.md)

**Sprint goal:** Move the Junie Conversation Viewer UI from functional-but-basic (a bare default `MaterialTheme {}`) to a polished, modern, readable, and visually coherent desktop application with light/dark/system theme support, semantic colour tokens, and consistent styling across all components.

## 2. Related Documents

| Document | Role                                                                                      |
|---|-------------------------------------------------------------------------------------------|
| [`docs/sprints/junie-conversation-viewer-sprint-3-ui-polish-and-theme-refresh.md`](../sprints/junie-conversation-viewer-sprint-3-ui-polish-and-theme-refresh.md) | **Primary source of truth.** Defines the 9 delivery parts with concrete "After" sections. |
| [`docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md`](../sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md) | Preceding sprint; Sprint 3 builds on its baseline.                                        |
| [`docs/sprint-3-area-1-findings`](../sprint-3-area-1-findings) | Sprint 3 findings.                                                                        |
| [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md) | Canonical domain terms.                                                                   |
| [`docs/RECAP.md`](../RECAP.md) | Chronological project history.                                                            |
| [`docs/TESTING.md`](../TESTING.md) | Testing stack, Robot pattern, `testTag` conventions, Gradle commands.                     |
| [`docs/project_memory.md`](../project_memory.md) | Decisions, gotchas, shipped work.                                                         |

## 3. Purpose

This document breaks the Sprint 3 UI Polish and Theme Refresh sprint into concrete, trackable tasks. It serves as:

- **Junie's implementation checklist** — each task has clear completion criteria, dependencies, and testing expectations.
- **HITL's review and progress checklist** — each task has a checkbox, and review-oriented tasks include HITL-visible outcomes.

## 4. How to Use This Task Document

1. **Before starting implementation**, read the Related Documents listed above.
2. **Work through tasks in area order** (1–9). Within each area, tasks are ordered by dependency.
3. **Check off tasks** (`- [x]`) only when all completion criteria are met.
4. **Mark parent tasks complete** only when all subtasks are complete.
5. **Use inline markers** (see Task Status Legend) to flag blocked, deferred, or review-dependent tasks.
6. **Update the Progress Summary** table as areas are completed.

## 5. Progress Summary

| # | Task Area | Status                             | Task Count |
|---|---|------------------------------------|---|
| 1 | Design Discovery and Baseline Audit | 6/6 complete                       | 6 |
| 2 | Theme and Token Foundation | 10/10 complete                     | 10 |
| 3 | Application Chrome Polish | 8/8 complete                       | 8 |
| 4 | Conversation Surface Redesign | 8/8 complete  | 8 |
| 5 | Rich Content Styling | Not started                        | 10 |
| 6 | States and Feedback | Not started                        | 6 |
| 7 | Accessibility and Cross-Platform Polish | Not started                        | 10 |
| 8 | Testing and Review | Not started                        | 8 |
| 9 | Documentation and Completion | Not started                        | 7 |
| | **Total** |                                    | **73** |

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

### Area 1 — Design Discovery and Baseline Audit

*Source: Delivery Part 1. Confirms current UI state and captures design findings before any code changes.*

#### 1.1 Audit current UI for hardcoded styling

- [x] Audit current UI for hardcoded styling

**Description:** Review all UI files for hardcoded `Color(0x...)`, `.dp`, `.sp`, `fontSize`, `fontWeight` literals and text glyphs used as icons. Produce a checklist of items to replace with theme tokens.

**Source:** Sprint doc section 4 (Current UI Baseline).

**Dependencies:** None.

**Likely files / areas:** `ui/ConversationScreen.kt`, all files in `ui/components/`.

**Completion criteria:**
- Audit checklist exists documenting every hardcoded styling instance.
- Each instance is tagged with the theme token that will replace it.

**Testing expectations:** No automated tests required.

#### 1.2 Review LogViewer theme architecture

- [x] Review LogViewer theme architecture

**Description:** Review `KLogViewerColors.kt` and `KLogViewerTheme.kt` to confirm the dual-palette, CompositionLocal, and accessor-object patterns that will be adapted to M3.

**Source:** Sprint doc section 5.1 (LogViewer Findings).

**Dependencies:** None.

**Likely files / areas:** `~/Dropbox/projects/utilities/LogViewer/ui/src/main/kotlin/com/klogviewer/ui/theme/`.

**Completion criteria:**
- LogViewer patterns documented and mapped to M3 equivalents.

**Testing expectations:** No automated tests required.

#### 1.3 Research conversation/chat UI patterns

- [x] Research conversation/chat UI patterns

**Description:** Review modern conversation UIs (Slack, Discord, AI chat apps) and document patterns for role distinction, turn grouping, collapsible detail, readability, and accessibility.

**Source:** Sprint doc section 5.2 (Conversation/Chat-Transcript UI Research).

**Dependencies:** None.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- Research findings documented in sprint doc section 5.2.

**Testing expectations:** No automated tests required.

#### 1.4 Confirm design token definitions

- [x] Confirm design token definitions

**Description:** Finalise the colour tokens (light/dark palettes + semantic conversation tokens), typography scale, spacing scale, and shape/elevation rules from sprint doc section 12.

**Source:** Sprint doc section 12 (Proposed Visual System).

**Dependencies:** 1.1, 1.2, 1.3.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- All token tables in sprint doc section 12 are confirmed and ready for implementation.

**Testing expectations:** No automated tests required.

#### 1.5 Read project documentation

- [x] Read project documentation

**Description:** Read `UBIQUITOUS-LANGUAGE.md`, `TESTING.md`, `project_memory.md`, `RECAP.md`, and Sprint 2 docs to lock terminology and understand the current baseline.

**Source:** Sprint doc section 2 (Related Documents).

**Dependencies:** None.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- All related documents read and understood.
- Domain terms confirmed for use in implementation.

**Testing expectations:** No automated tests required.

#### 1.6 HITL review of design findings — `HITL Review`

- [x] HITL review of design findings

**Description:** Present the audit results, design token definitions, and research findings to the HITL for approval before implementation begins.

**Source:** Sprint doc Part 1 "After" section.

**Dependencies:** 1.1, 1.2, 1.3, 1.4, 1.5.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- HITL has reviewed and approved the design token definitions and audit findings.

**Testing expectations:** No automated tests required.

**HITL-visible outcome:** Documented audit of current UI and confirmed design token definitions.

### Area 2 — Theme and Token Foundation

*Source: Delivery Part 2. Creates the theme infrastructure that all subsequent areas depend on.*

#### 2.1 Create `ThemeMode` enum

- [x] Create `ThemeMode` enum — `Test Required`

**Description:** Create `ui/theme/ThemeMode.kt` with `enum class ThemeMode { Light, Dark, System }`.

**Source:** Sprint doc section 13.2.

**Dependencies:** None.

**Likely files / areas:** `shared/src/commonMain/.../ui/theme/ThemeMode.kt` (new).

**Completion criteria:**
- `ThemeMode` enum exists with three values.
- Unit test verifies enum values.

**Testing expectations:** Unit test for enum values.

#### 2.2 Create `ConversationColors` data class

- [x] Create `ConversationColors` data class

**Description:** Create `ui/theme/ConversationColors.kt` with the semantic colour data class and light/dark instances per sprint doc section 12.1 semantic tokens table.

**Source:** Sprint doc sections 12.1, 13.4.

**Dependencies:** None.

**Likely files / areas:** `shared/src/commonMain/.../ui/theme/ConversationColors.kt` (new).

**Completion criteria:**
- `ConversationColors` data class with all semantic tokens from section 12.1.
- `lightConversationColors()` and `darkConversationColors()` factory functions with concrete hex values.
- `LocalConversationColors` `staticCompositionLocalOf` provider.

**Testing expectations:** Unit test verifying light/dark instances have distinct values.

#### 2.3 Create `JunieViewerSpacing` data class

- [x] Create `JunieViewerSpacing` data class

**Description:** Create `ui/theme/JunieViewerSpacing.kt` with the spacing scale (xs through xxl) per sprint doc section 12.4.

**Source:** Sprint doc sections 12.4, 13.4.

**Dependencies:** None.

**Likely files / areas:** `shared/src/commonMain/.../ui/theme/JunieViewerSpacing.kt` (new).

**Completion criteria:**
- `JunieViewerSpacing` data class with xs/sm/md/lg/xl/xxl values.
- `LocalJunieViewerSpacing` `staticCompositionLocalOf` provider.

**Testing expectations:** Unit test verifying spacing values.

#### 2.4 Create `JunieViewerTypography`

- [x] Create `JunieViewerTypography`

**Description:** Create `ui/theme/JunieViewerTypography.kt` defining the custom M3 `Typography` per sprint doc section 12.2.

**Source:** Sprint doc sections 12.2, 13.4.

**Dependencies:** None.

**Likely files / areas:** `shared/src/commonMain/.../ui/theme/JunieViewerTypography.kt` (new).

**Completion criteria:**
- Custom `Typography` with all roles from section 12.2.
- Monospace font family token for code/terminal content.

**Testing expectations:** No automated tests required (visual verification).

#### 2.5 Create M3 colour schemes

- [x] Create M3 colour schemes

**Description:** Define `lightColorScheme()` and `darkColorScheme()` with the palette values from sprint doc section 12.1 light/dark tables.

**Source:** Sprint doc section 12.1.

**Dependencies:** None.

**Likely files / areas:** `shared/src/commonMain/.../ui/theme/JunieViewerTheme.kt` (new).

**Completion criteria:**
- Light and dark `ColorScheme` instances with all values from section 12.1.

**Testing expectations:** No automated tests required.

#### 2.6 Create `JunieViewerTheme` composable

- [x] Create `JunieViewerTheme` composable — `Test Required`

**Description:** Create the main theme composable that resolves `ThemeMode`, provides `MaterialTheme` with custom schemes, and provides semantic tokens via `CompositionLocal`. Include the `JunieViewerTheme` accessor object.

**Source:** Sprint doc sections 13.1, 13.3.

**Dependencies:** 2.1, 2.2, 2.3, 2.4, 2.5.

**Likely files / areas:** `shared/src/commonMain/.../ui/theme/JunieViewerTheme.kt`.

**Completion criteria:**
- `JunieViewerTheme` composable resolves `ThemeMode` to light/dark.
- Provides `MaterialTheme`, `LocalConversationColors`, `LocalJunieViewerSpacing`.
- `JunieViewerTheme` accessor object provides `conversationColors` and `spacing`.

**Testing expectations:** Compose UI test verifying theme provides correct colours for each mode.

#### 2.7 Add `themeMode` to `Preferences` and `PreferencesRepository`

- [x] Add `themeMode` to `Preferences` and `PreferencesRepository` — `Test Required`

**Description:** Extend `Preferences` data class with a `themeMode: ThemeMode` field (default `System`). Extend `PreferencesRepository` to persist and restore it.

**Source:** Sprint doc section 13.2.

**Dependencies:** 2.1.

**Likely files / areas:** `shared/src/commonMain/.../domain/Preferences.kt`, `shared/src/commonMain/.../data/PreferencesRepository.kt`.

**Completion criteria:**
- `Preferences` includes `themeMode` field.
- `PreferencesRepository` persists and restores `themeMode`.

**Testing expectations:** Unit test in `PreferencesRepositoryTest` verifying persistence of `themeMode`.

#### 2.8 Wire `JunieViewerTheme` into `App.kt`

- [x] Wire `JunieViewerTheme` into `App.kt`

**Description:** Replace the bare `MaterialTheme {}` in `App.kt` with `JunieViewerTheme(themeMode = ...)` reading from the persisted preference.

**Source:** Sprint doc section 13.1.

**Dependencies:** 2.6, 2.7.

**Likely files / areas:** `shared/src/commonMain/.../App.kt`.

**Completion criteria:**
- `App.kt` uses `JunieViewerTheme` instead of `MaterialTheme`.
- Theme mode is read from `ConversationState` (which reads from `Preferences`).

**Testing expectations:** Existing tests must still pass.

#### 2.9 Add theme toggle to `SettingsDialog`

- [x] Add theme toggle to `SettingsDialog`

**Description:** Add a theme mode selector (Light/Dark/System) to `SettingsDialog` that dispatches a `ConversationAction` to update the preference.

**Source:** Sprint doc section 13.2, FR3.

**Dependencies:** 2.7, 2.8.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/SettingsDialog.kt`, `shared/src/commonMain/.../ui/ConversationAction.kt`.

**Completion criteria:**
- `SettingsDialog` shows a theme mode selector with three options.
- Selecting a mode persists the preference and the theme updates immediately.

**Testing expectations:** UI test verifying theme toggle changes theme mode.

#### 2.10 HITL review of theme foundation — `HITL Review`

- [x] HITL review of theme foundation

**Description:** HITL verifies that light, dark, and system themes can be toggled and the app's colours change accordingly.

**Source:** Sprint doc Part 2 "After" section.

**Dependencies:** 2.1–2.9.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms theme switching works.
- HITL confirms background, surface, and text colours change between modes.

**Testing expectations:** Manual visual review.

**HITL-visible outcome:** Toggle between light, dark, and system themes via Settings; app colours change accordingly.

### Area 3 — Application Chrome Polish

*Source: Delivery Part 3. Restyles the persistent chrome (top bar, headers, filter bar, dialogs) using theme tokens.*

#### 3.1 Restyle top bar with theme tokens

- [x] Restyle top bar with theme tokens

**Description:** Replace hardcoded padding, colours, and text glyphs in the top bar area of `ConversationScreen.kt` with theme tokens and spacing values. Replace "▲"/"▼" text glyphs with proper themed indicators.

**Source:** Sprint doc sections 12, 14; FR4.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/ConversationScreen.kt`.

**Completion criteria:**
- Top bar uses `MaterialTheme.colorScheme` and `JunieViewerTheme.spacing` exclusively.
- No hardcoded `dp`, `sp`, or `Color(0x...)` in top bar code.
- Text glyphs replaced with themed text or icon composables.

**Testing expectations:** Existing UI tests pass; visual review.

#### 3.2 Restyle `SessionContextHeader` with theme tokens

- [x] Restyle `SessionContextHeader` with theme tokens

**Description:** Replace hardcoded styling in `SessionContextHeader.kt` with theme tokens.

**Source:** Sprint doc section 15; FR4.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/SessionContextHeader.kt`.

**Completion criteria:**
- All padding, colours, and typography use theme tokens.

**Testing expectations:** Existing tests pass.

#### 3.3 Restyle `FilterBar` with theme tokens

- [x] Restyle `FilterBar` with theme tokens

**Description:** Replace hardcoded styling in `FilterBar.kt` with theme tokens. Apply pill-shaped chips per section 12.3.

**Source:** Sprint doc sections 12.3, 12.4; FR4.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/FilterBar.kt`.

**Completion criteria:**
- Filter chips use themed colours, pill shape (16dp corners), and spacing tokens.

**Testing expectations:** Existing filter tests pass.

#### 3.4 Restyle `SessionSelector` with improved density

- [x] Restyle `SessionSelector` with improved density

**Description:** Restyle `SessionSelector.kt` with theme tokens, improved density, visual hierarchy, and hover/selection states on session list items. Active session visually highlighted.

**Source:** Sprint doc section 15; FR8.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/SessionSelector.kt`.

**Completion criteria:**
- Session list items use themed colours and spacing.
- Hover state visible on session items.
- Active/selected session visually distinct.
- Improved density (tighter padding, clearer hierarchy).

**Testing expectations:** Existing session selector tests pass; visual review.

#### 3.5 Restyle `SettingsDialog` with theme tokens

- [x] Restyle `SettingsDialog` with theme tokens

**Description:** Replace hardcoded styling in `SettingsDialog.kt` with theme tokens. Apply dialog shape per section 12.3.

**Source:** Sprint doc sections 12.3, 12.4; FR4.

**Dependencies:** 2.9 (theme toggle already added).

**Likely files / areas:** `shared/src/commonMain/.../ui/components/SettingsDialog.kt`.

**Completion criteria:**
- Dialog uses 12dp rounded corners, themed colours, spacing tokens.

**Testing expectations:** Existing tests pass.

#### 3.6 Restyle search field and match navigation

- [x] Restyle search field and match navigation

**Description:** Replace hardcoded styling in the search field and match navigation area of `ConversationScreen.kt` with theme tokens.

**Source:** Sprint doc section 12; FR4.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/ConversationScreen.kt`.

**Completion criteria:**
- Search field and match navigation use theme tokens exclusively.

**Testing expectations:** Existing search tests pass.

#### 3.7 Verify no hardcoded styling remains in chrome

- [x] Verify no hardcoded styling remains in chrome

**Description:** Grep all chrome-related files for remaining hardcoded `Color(0x...)`, `.dp`, `.sp` literals and fix any remaining instances.

**Source:** Sprint doc section 11, principle 1.

**Dependencies:** 3.1–3.6.

**Likely files / areas:** All chrome files.

**Completion criteria:**
- Zero hardcoded colour/spacing/typography literals in chrome files.

**Testing expectations:** All existing tests pass.

#### 3.8 HITL review of chrome polish — `HITL Review`

- [x] HITL review of chrome polish

**Description:** HITL verifies polished top bar, filter area, session picker, and settings dialog in both light and dark themes.

**Source:** Sprint doc Part 3 "After" section.

**Dependencies:** 3.1–3.7.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms chrome is polished and consistently themed.

**Testing expectations:** Manual visual review.

**HITL-visible outcome:** Polished, compact top bar and filter area with consistent themed styling; session picker with improved density and interaction states.

### Area 4 — Conversation Surface Redesign

*Source: Delivery Part 4. Restyles message cards, Human/Junie distinction, turn grouping, and readability.*

#### 4.1 Apply themed accent colours to Human messages

- [x] Apply themed accent colours to Human messages

**Description:** Replace hardcoded Human message styling with `humanAccent` semantic token, `primaryContainer` background, and themed spacing.

**Source:** Sprint doc sections 12.1, 14; FR5.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/MessageItems.kt`.

**Completion criteria:**
- Human messages use `humanAccent` border/rail and `primaryContainer` background from theme.
- Constrained max-width preserved.

**Testing expectations:** Existing message tests pass.

#### 4.2 Apply themed accent colours to Junie messages

- [x] Apply themed accent colours to Junie messages

**Description:** Replace hardcoded Junie message styling with `junieAccent` semantic token, `secondaryContainer` background, and themed spacing.

**Source:** Sprint doc sections 12.1, 14; FR5.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/MessageItems.kt`.

**Completion criteria:**
- Junie messages use `junieAccent` border/rail and `secondaryContainer` background from theme.

**Testing expectations:** Existing message tests pass.

#### 4.3 Apply themed styling to turn headers

- [x] Apply themed styling to turn headers

**Description:** Restyle turn headers with theme tokens (typography, colours, spacing).

**Source:** Sprint doc section 14; FR4.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/ConversationScreen.kt` or `MessageItems.kt`.

**Completion criteria:**
- Turn headers use `titleMedium` typography and themed colours.
- Spacing between turns uses `xxl` token; within turns uses `md` token.

**Testing expectations:** Existing turn grouping tests pass.

#### 4.4 Replace text glyph kind markers with themed markers

- [x] Replace text glyph kind markers with themed markers

**Description:** Replace text-glyph Message Kind markers with themed icon + label composables using semantic colours.

**Source:** Sprint doc section 14; design principle 6 (colour never sole differentiator).

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/MessageItems.kt`, `shared/src/commonMain/.../ui/ConversationScreen.kt`.

**Completion criteria:**
- Kind markers use themed text/icon composables, not raw text glyphs.
- Each kind has a distinct colour + label (not colour alone).

**Testing expectations:** Existing kind marker tests pass.

#### 4.5 Apply message card shape and elevation

- [x] Apply message card shape and elevation

**Description:** Apply 8dp rounded corners and appropriate elevation/border per section 12.3 to message cards.

**Source:** Sprint doc section 12.3.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/MessageItems.kt`.

**Completion criteria:**
- Message cards have 8dp rounded corners.
- Light theme: 1dp elevation. Dark theme: subtle border, 0dp elevation.

**Testing expectations:** Visual review.

#### 4.6 Improve long-form readability

- [x] Improve long-form readability

**Description:** Ensure Junie message content uses constrained max-width (~720dp), comfortable line height, and `bodyLarge` typography from theme.

**Source:** Sprint doc section 14; conversation UI research (constrained line length).

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/MessageItems.kt`.

**Completion criteria:**
- Junie message content width constrained for readability.
- Body text uses themed `bodyLarge` typography.

**Testing expectations:** Visual review.

#### 4.7 Verify no hardcoded styling in conversation surface

- [x] Verify no hardcoded styling in conversation surface

**Description:** Grep message-related files for remaining hardcoded literals and fix.

**Source:** Sprint doc principle 1.

**Dependencies:** 4.1–4.6.

**Likely files / areas:** `MessageItems.kt`, `ConversationScreen.kt`.

**Completion criteria:**
- Zero hardcoded colour/spacing/typography literals in conversation surface files.

**Testing expectations:** All existing tests pass.

#### 4.8 HITL review of conversation surface — `HITL Review`

- [x] HITL review of conversation surface

**Description:** HITL verifies distinct Human/Junie messages, turn grouping, kind markers, and readability in both themes.

**Source:** Sprint doc Part 4 "After" section.

**Dependencies:** 4.1–4.7.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms clear Human/Junie distinction, turn grouping, and comfortable readability.

**Testing expectations:** Manual visual review.

**HITL-visible outcome:** Clearly distinct Human and Junie messages with themed accent colours, improved turn grouping, and comfortable long-form readability.

### Area 5 — Rich Content Styling

*Source: Delivery Part 5. Restyles all rich content blocks using semantic theme tokens.*

#### 5.1 Restyle `CodeBlockWithCopy` with theme tokens

- [ ] Restyle `CodeBlockWithCopy` with theme tokens

**Description:** Replace hardcoded colours in `CodeBlockWithCopy.kt` with `codeBackground`/`codeBorder` semantic tokens and monospace typography token.

**Source:** Sprint doc section 16; FR6.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/CodeBlockWithCopy.kt`.

**Completion criteria:**
- Code blocks use `codeBackground`, `codeBorder` from `ConversationColors`.
- Monospace font from typography token.
- 6dp rounded corners, 1dp border per section 12.3.
- Copy button consistently styled.

**Testing expectations:** Existing code block tests pass.

#### 5.2 Restyle `DiffBlock` with theme tokens

- [ ] Restyle `DiffBlock` with theme tokens

**Description:** Replace hardcoded diff colours with `diffAdded`/`diffRemoved`/`diffAddedText`/`diffRemovedText` semantic tokens.

**Source:** Sprint doc section 16; FR6.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/DiffBlock.kt`.

**Completion criteria:**
- Added lines use `diffAdded` background and `diffAddedText` colour.
- Removed lines use `diffRemoved` background and `diffRemovedText` colour.
- 6dp rounded corners, 1dp border.

**Testing expectations:** Existing diff tests pass.

#### 5.3 Restyle `TerminalOutputBlock` with theme tokens

- [ ] Restyle `TerminalOutputBlock` with theme tokens

**Description:** Replace hardcoded terminal colours with `terminalBackground`/`terminalText` semantic tokens.

**Source:** Sprint doc section 16; FR6.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/TerminalOutputBlock.kt`.

**Completion criteria:**
- Terminal blocks use `terminalBackground` and `terminalText` from `ConversationColors`.
- Monospace font, `$`-prefixed command styling.

**Testing expectations:** Existing terminal tests pass.

#### 5.4 Restyle `ThoughtBlock` with theme tokens

- [ ] Restyle `ThoughtBlock` with theme tokens

**Description:** Replace hardcoded thought colours with `thoughtBackground`/`thoughtBorder` semantic tokens. Ensure de-emphasised styling.

**Source:** Sprint doc section 16; FR6.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/ThoughtBlock.kt`.

**Completion criteria:**
- Thought blocks use `thoughtBackground` and `thoughtBorder` from `ConversationColors`.
- Visually de-emphasised relative to Response messages.

**Testing expectations:** Existing thought tests pass.

#### 5.5 Restyle `ToolCallBlock` with theme tokens

- [ ] Restyle `ToolCallBlock` with theme tokens

**Description:** Replace hardcoded tool call colours with `toolCallBackground`/`toolCallBorder` semantic tokens.

**Source:** Sprint doc section 16; FR6.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/ToolCallBlock.kt`.

**Completion criteria:**
- Tool call blocks use `toolCallBackground` and `toolCallBorder` from `ConversationColors`.

**Testing expectations:** Existing tool call tests pass.

#### 5.6 Restyle `StructuredOutputBlock` with theme tokens

- [ ] Restyle `StructuredOutputBlock` with theme tokens

**Description:** Replace hardcoded styling in `StructuredOutputBlock.kt` with theme tokens, consistent with code block styling.

**Source:** Sprint doc section 16; FR6.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/StructuredOutputBlock.kt`.

**Completion criteria:**
- Structured output uses `codeBackground`/`codeBorder` tokens and monospace font.

**Testing expectations:** Existing structured output tests pass.

#### 5.7 Restyle `ErrorWarningBlock` with theme tokens

- [ ] Restyle `ErrorWarningBlock` with theme tokens

**Description:** Replace hardcoded error/warning colours with `errorBackground`/`warningBackground` semantic tokens. Ensure icon + label (not colour alone).

**Source:** Sprint doc section 16; FR6; design principle 6.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/ErrorWarningBlock.kt`.

**Completion criteria:**
- Error blocks use `errorBackground`, warning blocks use `warningBackground`.
- Icon + label present (colour not sole differentiator).

**Testing expectations:** Existing error/warning tests pass.

#### 5.8 Restyle `MarkdownContent` with theme tokens

- [ ] Restyle `MarkdownContent` with theme tokens

**Description:** Replace hardcoded font sizes, spacing, and colours in `MarkdownContent.kt` with theme tokens.

**Source:** Sprint doc section 16; FR4.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/MarkdownContent.kt`.

**Completion criteria:**
- Heading sizes, code spans, list markers, and link styling use theme tokens.
- No hardcoded `sp` or `Color(0x...)` literals.

**Testing expectations:** Existing markdown tests pass.

#### 5.9 Verify no hardcoded styling in rich content blocks

- [ ] Verify no hardcoded styling in rich content blocks

**Description:** Grep all rich content files for remaining hardcoded literals and fix.

**Source:** Sprint doc principle 1.

**Dependencies:** 5.1–5.8.

**Likely files / areas:** All `ui/components/` rich content files.

**Completion criteria:**
- Zero hardcoded colour/spacing/typography literals in rich content files.

**Testing expectations:** All existing tests pass.

#### 5.10 HITL review of rich content styling — `HITL Review`

- [ ] HITL review of rich content styling

**Description:** HITL verifies all rich content blocks have distinct, themed visual treatments in both light and dark modes.

**Source:** Sprint doc Part 5 "After" section.

**Dependencies:** 5.1–5.9.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms each rich content type is visually distinct and themed.

**Testing expectations:** Manual visual review.

**HITL-visible outcome:** All rich content blocks (code, diff, terminal, thought, tool call, structured output, error/warning) with distinct, themed visual treatments in both light and dark modes.

### Area 6 — States and Feedback

*Source: Delivery Part 6. Restyles loading, empty, error, and no-results states.*

#### 6.1 Restyle loading state with theme tokens

- [ ] Restyle loading state with theme tokens

**Description:** Replace hardcoded styling in the loading state of `ConversationScreen.kt` with theme tokens.

**Source:** Sprint doc section 20 Part 6; FR7.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/ConversationScreen.kt`.

**Completion criteria:**
- Loading indicator and text use themed colours and spacing.

**Testing expectations:** Existing loading state tests pass.

#### 6.2 Restyle empty states with theme tokens

- [ ] Restyle empty states with theme tokens

**Description:** Restyle "no Session selected" and "Session has no Messages" empty states with theme tokens.

**Source:** Sprint doc section 20 Part 6; FR7.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/ConversationScreen.kt`.

**Completion criteria:**
- Empty states use themed colours, typography, and spacing.
- Text glyph "⚠" replaced with themed indicator.

**Testing expectations:** Existing empty state tests pass.

#### 6.3 Restyle error state with theme tokens

- [ ] Restyle error state with theme tokens

**Description:** Restyle the error state in `ConversationScreen.kt` with theme tokens.

**Source:** Sprint doc section 20 Part 6; FR7.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/ConversationScreen.kt`.

**Completion criteria:**
- Error state uses `error` colour from theme, themed spacing and typography.

**Testing expectations:** Existing error state tests pass.

#### 6.4 Restyle `FatalErrorDialog` with theme tokens

- [ ] Restyle `FatalErrorDialog` with theme tokens

**Description:** Replace hardcoded styling in `FatalErrorDialog.kt` with theme tokens.

**Source:** Sprint doc section 20 Part 6; FR7.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/components/FatalErrorDialog.kt`.

**Completion criteria:**
- Dialog uses themed colours, 12dp rounded corners, spacing tokens.

**Testing expectations:** Existing fatal error tests pass.

#### 6.5 Restyle no-results state with theme tokens

- [ ] Restyle no-results state with theme tokens

**Description:** Restyle the "no Messages match" state with theme tokens.

**Source:** Sprint doc section 20 Part 6; FR7.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonMain/.../ui/ConversationScreen.kt`.

**Completion criteria:**
- No-results state uses themed colours and typography.

**Testing expectations:** Existing no-results tests pass.

#### 6.6 HITL review of states — `HITL Review`

- [ ] HITL review of states

**Description:** HITL verifies polished loading, empty, error, and no-results states in both themes.

**Source:** Sprint doc Part 6 "After" section.

**Dependencies:** 6.1–6.5.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms all states are polished and themed.

**Testing expectations:** Manual visual review.

**HITL-visible outcome:** Polished, themed loading, empty, error, and no-results states.

### Area 7 — Accessibility and Cross-Platform Polish

*Source: Delivery Part 7. Ensures contrast, keyboard focus, screen-reader semantics, scalable text, and cross-platform behaviour.*

#### 7.1 Verify WCAG AA contrast ratios

- [ ] Verify WCAG AA contrast ratios — `Manual Review Required`

**Description:** Check all text/background colour combinations in both themes against WCAG AA requirements (4.5:1 normal text, 3:1 large text). Fix any failures.

**Source:** Sprint doc section 17 A1; NFR1.

**Dependencies:** Areas 2–6 complete.

**Likely files / areas:** `ui/theme/ConversationColors.kt`, `ui/theme/JunieViewerTheme.kt`.

**Completion criteria:**
- All text/background combinations meet WCAG AA in both themes.

**Testing expectations:** Manual contrast check with documented results.

#### 7.2 Add visible keyboard focus indicators

- [ ] Add visible keyboard focus indicators

**Description:** Ensure all interactive elements (buttons, chips, list items, text fields) have visible focus indicators when navigated by keyboard.

**Source:** Sprint doc section 17 A2; NFR2.

**Dependencies:** Areas 2–6 complete.

**Likely files / areas:** All UI component files.

**Completion criteria:**
- Keyboard focus ring or highlight visible on all interactive elements.

**Testing expectations:** Manual keyboard navigation walkthrough.

#### 7.3 Verify logical focus order

- [ ] Verify logical focus order

**Description:** Verify focus order follows: top bar → filter bar → message list → dialogs.

**Source:** Sprint doc section 17 A3.

**Dependencies:** 7.2.

**Likely files / areas:** `ConversationScreen.kt`, dialog composables.

**Completion criteria:**
- Tab/focus order is logical and predictable.

**Testing expectations:** Manual keyboard navigation test.

#### 7.4 Verify `contentDescription` on all interactive elements

- [ ] Verify `contentDescription` on all interactive elements

**Description:** Ensure all icons, buttons, and interactive elements have `contentDescription` or equivalent accessibility semantics.

**Source:** Sprint doc section 17 A4; NFR3.

**Dependencies:** Areas 2–6 complete.

**Likely files / areas:** All UI component files.

**Completion criteria:**
- Every icon and interactive element has a `contentDescription`.

**Testing expectations:** Automated accessibility test or manual review.

#### 7.5 Verify heading semantics

- [ ] Verify heading semantics

**Description:** Ensure section headers use `semantics { heading() }` for screen-reader navigation.

**Source:** Sprint doc section 17 A5.

**Dependencies:** Areas 2–6 complete.

**Likely files / areas:** `ConversationScreen.kt`, `MessageItems.kt`.

**Completion criteria:**
- All section headers have heading semantics.

**Testing expectations:** Manual review or automated check.

#### 7.6 Verify colour is never sole differentiator

- [ ] Verify colour is never sole differentiator

**Description:** Confirm every colour-coded element also has an icon, label, or positional cue.

**Source:** Sprint doc section 17 A6; design principle 6.

**Dependencies:** Areas 2–6 complete.

**Likely files / areas:** All UI component files.

**Completion criteria:**
- No element relies on colour alone for meaning.

**Testing expectations:** Manual visual review.

#### 7.7 Verify scalable text

- [ ] Verify scalable text

**Description:** Verify text scales correctly when system font size preference changes.

**Source:** Sprint doc section 17 A7; NFR4.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `ui/theme/JunieViewerTypography.kt`.

**Completion criteria:**
- Text scales with system font size on all platforms.

**Testing expectations:** Manual test with different system font sizes.

#### 7.8 Cross-platform behaviour verification — `Manual Review Required`

- [ ] Cross-platform behaviour verification

**Description:** Test on macOS, Windows, and Linux for scrolling, copy, shortcuts, font rendering, window sizing, and high-DPI support.

**Source:** Sprint doc section 18; NFR5.

**Dependencies:** Areas 2–6 complete.

**Likely files / areas:** Running application on each platform.

**Completion criteria:**
- Documented checklist results for all three platforms.

**Testing expectations:** Manual cross-platform checklist.

#### 7.9 Verify copy-to-clipboard produces plain text

- [ ] Verify copy-to-clipboard produces plain text

**Description:** Verify code, diff, and terminal copy actions produce clean plain text on all platforms.

**Source:** Sprint doc section 18 C6.

**Dependencies:** Area 5 complete.

**Likely files / areas:** `CodeBlockWithCopy.kt`, `DiffBlock.kt`, `TerminalOutputBlock.kt`.

**Completion criteria:**
- Copy produces plain text, not rich text, on all platforms.

**Testing expectations:** Manual copy-paste test.

#### 7.10 HITL review of accessibility and cross-platform — `HITL Review`

- [ ] HITL review of accessibility and cross-platform

**Description:** HITL verifies keyboard navigation, focus indicators, contrast, and cross-platform behaviour.

**Source:** Sprint doc Part 7 "After" section.

**Dependencies:** 7.1–7.9.

**Likely files / areas:** Running application.

**Completion criteria:**
- HITL confirms accessibility and cross-platform requirements met.

**Testing expectations:** Manual review.

**HITL-visible outcome:** UI is keyboard-navigable, has visible focus indicators, meets WCAG AA contrast, and behaves consistently on macOS, Windows, and Linux.

### Area 8 — Testing and Review

*Source: Delivery Part 8. Extends automated tests and runs manual review checklist.*

#### 8.1 Add theme-switching UI test

- [ ] Add theme-switching UI test — `Test Required`

**Description:** Write a Compose UI test that verifies `JunieViewerTheme` applies correct colours for Light, Dark, and System modes.

**Source:** Sprint doc section 19.1.

**Dependencies:** Area 2 complete.

**Likely files / areas:** `shared/src/commonTest/kotlin/.../ui/theme/JunieViewerThemeTest.kt` (new).

**Completion criteria:**
- Test verifies theme provides distinct colour schemes for each mode.

**Testing expectations:** Test passes in `./gradlew :shared:jvmTest`.

#### 8.2 Add semantic token unit tests

- [ ] Add semantic token unit tests — `Test Required`

**Description:** Write unit tests verifying `ConversationColors` light/dark instances have distinct, non-default values for all tokens.

**Source:** Sprint doc section 19.1.

**Dependencies:** 2.2.

**Likely files / areas:** `shared/src/commonTest/kotlin/.../ui/theme/ConversationColorsTest.kt` (new).

**Completion criteria:**
- Tests verify all semantic tokens have distinct light/dark values.

**Testing expectations:** Tests pass.

#### 8.3 Run existing test suite regression check

- [ ] Run existing test suite regression check — `Test Required`

**Description:** Run `./gradlew :shared:jvmTest` and verify all existing tests pass with the new theme.

**Source:** Sprint doc section 19.1; NFR7.

**Dependencies:** Areas 2–7 complete.

**Likely files / areas:** All test files.

**Completion criteria:**
- `./gradlew :shared:jvmTest` — BUILD SUCCESSFUL, 0 failures.

**Testing expectations:** Full test suite green.

#### 8.4 Add themed component tests where practical

- [ ] Add themed component tests where practical

**Description:** Add Compose UI tests for key themed components (e.g., message items in light/dark, rich content blocks) where practical.

**Source:** Sprint doc section 19.1.

**Dependencies:** Areas 2–6 complete.

**Likely files / areas:** `shared/src/commonTest/kotlin/.../ui/`.

**Completion criteria:**
- At least 3 new component tests covering themed rendering.

**Testing expectations:** Tests pass.

#### 8.5 Run manual visual review checklist — light theme

- [ ] Run manual visual review checklist — light theme — `Manual Review Required`

**Description:** Walk through the manual review checklist (section 19.2) in light theme.

**Source:** Sprint doc section 19.2.

**Dependencies:** Areas 2–7 complete.

**Likely files / areas:** Running application.

**Completion criteria:**
- All checklist items verified in light theme.

**Testing expectations:** Manual review documented.

#### 8.6 Run manual visual review checklist — dark theme

- [ ] Run manual visual review checklist — dark theme — `Manual Review Required`

**Description:** Walk through the manual review checklist (section 19.2) in dark theme.

**Source:** Sprint doc section 19.2.

**Dependencies:** Areas 2–7 complete.

**Likely files / areas:** Running application.

**Completion criteria:**
- All checklist items verified in dark theme.

**Testing expectations:** Manual review documented.

#### 8.7 Fix issues found during review

- [ ] Fix issues found during review

**Description:** Address any issues found during automated tests, manual review, or HITL feedback.

**Source:** Sprint doc section 19.

**Dependencies:** 8.1–8.6.

**Likely files / areas:** Any affected files.

**Completion criteria:**
- All identified issues resolved.
- All tests pass after fixes.

**Testing expectations:** Full test suite green after fixes.

#### 8.8 HITL review of testing — `HITL Review`

- [ ] HITL review of testing

**Description:** HITL confirms test coverage is adequate and manual review checklist is complete.

**Source:** Sprint doc Part 8 "After" section.

**Dependencies:** 8.1–8.7.

**Likely files / areas:** Test results, review checklist.

**Completion criteria:**
- HITL confirms testing is adequate.

**Testing expectations:** Manual review.

**HITL-visible outcome:** All automated tests pass (including new theme/component tests); manual review checklist completed.

### Area 9 — Documentation and Completion

*Source: Delivery Part 9. Updates documentation, records decisions, and runs final checks.*

#### 9.1 Update `README.md`

- [ ] Update `README.md`

**Description:** Update `README.md` using the `readme-updater` skill to reflect the new theme system and UI improvements.

**Source:** Sprint doc Part 9; project guidelines.

**Dependencies:** Areas 2–8 complete.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- `README.md` reflects current application state including theme support.

**Testing expectations:** No automated tests required.

#### 9.2 Update `docs/project_memory.md`

- [ ] Update `docs/project_memory.md`

**Description:** Update `project_memory.md` using the `project-memory` skill with what was shipped, key decisions, gotchas, and test coverage areas.

**Source:** Sprint doc Part 9; project guidelines.

**Dependencies:** Areas 2–8 complete.

**Likely files / areas:** `docs/project_memory.md`.

**Completion criteria:**
- Sprint 3 entry added with all required fields.

**Testing expectations:** No automated tests required.

#### 9.3 Update `docs/RECAP.md`

- [ ] Update `docs/RECAP.md`

**Description:** Add Sprint 3 entry to `RECAP.md` documenting what was shipped.

**Source:** Sprint doc Part 9.

**Dependencies:** Areas 2–8 complete.

**Likely files / areas:** `docs/RECAP.md`.

**Completion criteria:**
- Sprint 3 entry added to RECAP.

**Testing expectations:** No automated tests required.

#### 9.4 Record deferred items and decisions

- [ ] Record deferred items and decisions

**Description:** Document any deferred items, open questions resolved during implementation, and key decisions in the notes/decisions log below.

**Source:** Sprint doc Part 9.

**Dependencies:** Areas 2–8 complete.

**Likely files / areas:** This task document (section 10).

**Completion criteria:**
- All deferred items and decisions documented.

**Testing expectations:** No automated tests required.

#### 9.5 Run cyclomatic complexity check

- [ ] Run cyclomatic complexity check

**Description:** Run a cyclomatic complexity check on the codebase per project guidelines. Identify any functions that exceed acceptable thresholds and create follow-up tasks if needed.

**Source:** Project guidelines (sprint completion).

**Dependencies:** Areas 2–8 complete.

**Likely files / areas:** All source files.

**Completion criteria:**
- Complexity check run and results documented.
- Follow-up tasks created for any high-complexity functions if needed.

**Testing expectations:** No automated tests required.

#### 9.6 Full test suite final run

- [ ] Full test suite final run — `Test Required`

**Description:** Run `./gradlew test` to confirm all tests pass as the final check.

**Source:** Sprint doc section 23 (Definition of Done).

**Dependencies:** All previous areas complete.

**Likely files / areas:** All test files.

**Completion criteria:**
- `./gradlew test` — BUILD SUCCESSFUL, 0 failures.

**Testing expectations:** Full suite green.

#### 9.7 HITL final approval — `HITL Review`

- [ ] HITL final approval

**Description:** HITL runs the application, inspects representative Conversations in both themes, and grants final sprint approval.

**Source:** Sprint doc Part 9 "After" section; Definition of Done.

**Dependencies:** 9.1–9.6.

**Likely files / areas:** Running application, documentation.

**Completion criteria:**
- HITL grants final approval.
- All Definition of Done items satisfied.

**Testing expectations:** Manual review.

**HITL-visible outcome:** All documentation reflects shipped UI changes; HITL can run the application and confirm sprint outcomes.

---

## 8. HITL Review Checkpoints

| Checkpoint | After Area | What to Verify |
|---|---|---|
| Design findings review | Area 1 | Audit results and design token definitions approved. |
| Theme foundation review | Area 2 | Light/dark/system theme toggle works; colours change. |
| Chrome polish review | Area 3 | Top bar, filter bar, session picker polished and themed. |
| Conversation surface review | Area 4 | Human/Junie distinction, turn grouping, readability. |
| Rich content review | Area 5 | All content blocks distinct and themed in both modes. |
| States review | Area 6 | Loading, empty, error, no-results states polished. |
| Accessibility review | Area 7 | Keyboard nav, focus indicators, contrast, cross-platform. |
| Testing review | Area 8 | Test coverage adequate; manual checklist complete. |
| Final approval | Area 9 | Definition of Done satisfied; sprint approved. |

## 9. Acceptance Criteria

- All tasks in Areas 1–9 checked off.
- All HITL review checkpoints passed.
- All automated tests pass (`./gradlew test`).
- Manual review checklists completed for both themes.
- Cross-platform verification documented.
- Documentation updated (`README.md`, `project_memory.md`, `RECAP.md`).
- Cyclomatic complexity check run and reviewed.

## 10. Deferred / Out-of-Scope Items

- **D1:** Sidebar navigation — confirmed out of scope; restyle existing `SessionSelector` only.
- **D2:** Real-time session tailing / streaming.
- **D3:** Full Markdown parser replacement.
- **D4:** Advanced syntax highlighting beyond existing dependencies.
- **D5:** Mobile UI.
- **D6:** Bundled fonts — confirmed deferred; `FontFamily.Monospace` is sufficient (Q3 resolved 2026-07-14).
- **D7:** Modifying `docs/UBIQUITOUS-LANGUAGE.md` — additions are follow-up tasks only.

## 11. Notes / Decisions Log

| # | Date | Decision / Note |
|---|---|---|
| N1 | 2026-07-13 | **Theme modes confirmed:** Light + Dark + System with manual override persisted in Preferences. |
| N2 | 2026-07-13 | **Theme architecture confirmed:** M3 `lightColorScheme`/`darkColorScheme` + app-specific semantic tokens via `CompositionLocal`, adapting LogViewer's pattern to M3. |
| N3 | 2026-07-13 | **Session navigation confirmed:** Restyle existing `SessionSelector` flow; no sidebar. |
| N4 | 2026-07-13 | **Colour palette:** Inspired by LogViewer's Industrial Dark / Clean Light palettes with Junie-specific semantic tokens for message roles and content kinds. |
| N5 | 2026-07-13 | **Area 1 complete (tasks 1.1–1.5).** Findings documented in [`docs/sprint-3-area-1-findings.md`](../sprint-3-area-1-findings.md). Audit found ~120 hardcoded styling instances across 15 files. Two new semantic tokens proposed: `terminalCommand` and `diffHunkHeader`. CodeBlock `SyntaxThemes.default(darkMode = false)` must be wired to ThemeMode. Task 1.6 (HITL review) remains unchecked pending approval. |
| N6 | 2026-07-14 | **Area 1 open questions Q1–Q7 resolved (HITL).** Q1: Run against current baseline (no Sprint 2 blocking dependency). Q2: Use LogViewer accent colours (`#007ACC` light, `#00A3E0` dark). Q3: `FontFamily.Monospace` sufficient, no bundled font. Q4: Theme toggle in Settings dialog only. Q5: ThoughtBlock/ToolCallBlock collapsed by default. Q6: Yes, add `terminalCommand` semantic token. Q7: Yes, add `diffHunkHeader` semantic token. Findings doc updated. |
| N7 | 2026-07-14 | **Area 2 complete (tasks 2.1–2.9).** Files added: `ui/theme/ThemeMode.kt`, `ConversationColors.kt`, `JunieViewerSpacing.kt`, `JunieViewerTypography.kt`, `JunieViewerTheme.kt`. Modified: `AppPreferences` (added `themeMode: String`), `ConversationState` (added `themeMode`), `ConversationAction` (added `OnThemeModeChange`), `ConversationViewModel` (handles theme load/save), `App.kt` (replaced `MaterialTheme` with `JunieViewerTheme`), `SettingsDialog` (added radio button theme selector with test tags `theme_mode_light/dark/system`), `ConversationScreen` (wires new SettingsDialog params). Tests added: `ThemeModeTest`, `ConversationColorsTest`, `JunieViewerSpacingTest`, `JunieViewerThemeTest` (Compose UI), plus new tests in `PreferencesRepositoryTest` and `ConversationViewModelTest`. All tests pass (`./gradlew :shared:jvmTest`). `themeMode` stored as String in JSON for backwards compatibility; invalid values default to `System`. Task 2.10 (HITL review) remains unchecked. |
| N8 | 2026-07-14 | **Area 3 complete (tasks 3.1–3.7).** Chrome files restyled: `ConversationScreen.kt` (top bar wrapped in `Surface` with tonal elevation + `HorizontalDivider`, search field with `OutlinedTextFieldDefaults.colors`, match nav with themed glyph colours, all spacing via `JunieViewerTheme.spacing`), `SessionContextHeader.kt` (spacing tokens), `FilterBar.kt` (pill-shaped chips via `RoundedCornerShape(50)` + `FilterChipDefaults.filterChipColors` with themed selected/unselected colours), `SessionSelector.kt` (tighter density with `spacing.md`/`spacing.lg`/`spacing.xs`, selected state colours for all text, `shapes.small` for items), `SettingsDialog.kt` (all spacing via theme tokens, explicit surface colour). Intentional remaining literals: `1.dp` tonalElevation (M3 Surface param), `MATCH_NAV_BUTTON_SIZE = 32.dp` (named constant for icon buttons), `RoundedCornerShape(50)` (percent-based pill shape). Glyphs ✕/▲/▼ retained as themed `Text` — no Material Icons dependency available. Non-chrome hardcoded values in `ConversationScreen.kt` content states/list belong to Areas 4/6. All tests pass (`./gradlew :shared:jvmTest`). Task 3.8 (HITL review) remains unchecked. |
| N9 | 2026-07-14 | **Area 3 HITL feedback — layout restructure.** HITL found top chrome too crowded (title bar + session info + search + filters stacked). Decisions: (1) removed app title/top bar, (2) moved Session metadata from top to a one-line footer (`SessionContextFooter`), (3) top area now focuses on search field + compact Session/Settings `TextButton`s + filter chips, visually separated by dividers. `SessionContextHeader` superseded by `SessionContextFooter.kt`. `ConversationScreen` changed from `Scaffold` to `Column` layout with `SearchAndFilterChrome` + content + footer. Footer shows three evenly-spread fields: Session id, date, project — with `TextOverflow.Ellipsis`. New test tags: `session_context_footer`, `session_footer_log_name`, `session_footer_date`, `session_footer_project`. Robot updated: `assertSessionContextVisible` now uses `session_context_footer`. All tests pass (`./gradlew :shared:jvmTest`). Task 3.8 HITL review remains pending. |
| N10 | 2026-07-14 | **Area 4 complete (tasks 4.1–4.7).** Files changed: `MessageItems.kt` (accent rails via `humanAccent`/`junieAccent`, `Card` with `MESSAGE_CARD_SHAPE` 8dp rounded corners + 1dp elevation + `outlineVariant` border, `MessageKindMarker` composable with coloured dot + clean label replacing emoji-prefixed glyphs, `JUNIE_READABLE_MAX_WIDTH = 720.dp` for long-form readability, `UnsupportedEventCard` hardcoded dp replaced with spacing tokens, plain text upgraded to `bodyLarge`), `MessageFormatting.kt` (emoji prefixes removed from all `messageKindLabel` values), `ConversationScreen.kt` (conversation list padding/spacing uses `JunieViewerTheme.spacing`, extra `Spacer` before Turn headers for between-turn separation). Turn headers now use `titleMedium` typography, `junieAccent` colour, and `semantics { heading() }`. Kind indicator dot colours mapped to semantic tokens per `MessageKind`. Named layout constants: `HUMAN_MAX_CARD_WIDTH`, `JUNIE_READABLE_MAX_WIDTH`, `MESSAGE_CARD_SHAPE`, `ACCENT_RAIL_WIDTH`. Intentional remaining literals: `8.dp` dot size (visual constant), `1.dp` elevation/border (M3 Card params). All tests pass (`./gradlew :shared:jvmTest`). Task 4.8 (HITL review) remains unchecked. |
