---
sprint: 3
name: UI Polish and Theme Refresh
status: planned
---

# 1. Title

Sprint 3 — UI Polish and Theme Refresh

# 2. Related Documents

- [`docs/sprints/junie-conversation-viewer-sprint-2-conversation-ui-implementation.md`](junie-conversation-viewer-sprint-2-conversation-ui-implementation.md)
  — the preceding implementation sprint; Sprint 3 builds on its baseline.
- [`docs/tasks/junie-conversation-viewer-tasks-sprint-3-ui-polish-and-theme-refresh.md`](../tasks/junie-conversation-viewer-tasks-sprint-3-ui-polish-and-theme-refresh.md)
  — the companion task breakdown for this sprint.
- [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md)
  — canonical domain terms used consistently in code, tests, and UI copy.
- [`docs/RECAP.md`](../RECAP.md)
  — chronological project history.
- [`docs/TESTING.md`](../TESTING.md)
  — testing stack, Robot pattern, semantic `testTag` conventions, and Gradle commands.
- [`docs/project_memory.md`](../project_memory.md)
  — decisions, gotchas, and shipped work.

# 3. Sprint Goal

Move the Junie Conversation Viewer UI from functional-but-basic (a bare default `MaterialTheme {}` with no custom colours, typography, shapes, or dark mode) to a **polished, modern, readable, and visually coherent** desktop application, using the LogViewer app as design inspiration and modern conversation/chat-transcript UI patterns as research input.

The result is a themed, accessible, cross-platform desktop app where a HITL can comfortably read, scan, search, filter, and review asymmetric Conversations — short Human Messages and long, rich Junie Messages — with clear visual hierarchy, consistent styling, and light/dark/system theme support.

# 4. Current UI Baseline

## 4.1 Theme State

- `App.kt` wraps everything in a **default `MaterialTheme {}`** — no custom `ColorScheme`, `Typography`, `Shapes`, or dark-mode support.
- All components use ad-hoc styling: 64+ instances of hardcoded `Color(0x...)`, `.dp`, `.sp`, `fontSize`, and `fontWeight` literals scattered across 15+ component files.
- Text glyphs ("▲", "▼", "⚠", "⚙") are used in place of proper icons.
- No hover, focus, or selection states beyond default Material behaviour.
- No semantic colour tokens for message roles, code blocks, diffs, or terminal output.

## 4.2 Component Inventory

| Component | Key Styling Issues |
|---|---|
| `ConversationScreen.kt` | Hardcoded `16.dp` padding throughout, text glyphs for nav arrows, no themed states |
| `MessageItems.kt` | Hardcoded dp/sp values, no hover/selection states |
| `SessionSelector.kt` | Hardcoded padding, no density/hierarchy polish |
| `SessionContextHeader.kt` | Minimal styling, hardcoded spacing |
| `FilterBar.kt` | Basic chip styling, hardcoded padding |
| `CodeBlockWithCopy.kt` | Hardcoded `Color(0x...)` for background, no theme tokens |
| `DiffBlock.kt` | Hardcoded colours for added/removed lines |
| `TerminalOutputBlock.kt` | Hardcoded dark background colour |
| `ThoughtBlock.kt` | Hardcoded colours and spacing |
| `ToolCallBlock.kt` | Hardcoded colours and spacing |
| `StructuredOutputBlock.kt` | Hardcoded monospace styling |
| `ErrorWarningBlock.kt` | Hardcoded error colours |
| `SettingsDialog.kt` | Basic layout, hardcoded spacing |
| `FatalErrorDialog.kt` | Hardcoded spacing |
| `MarkdownContent.kt` | Hardcoded font sizes and spacing |

## 4.3 Architecture

- MVI pattern: `ConversationViewModel` / `ConversationState` / `ConversationAction` / `ConversationEvent`.
- Preferences persisted via `PreferencesRepository` (already supports key-value persistence).
- Strong Compose test suite using Robot pattern in `shared/src/commonTest/kotlin/.../ui/`.

# 5. Design Inspiration and Findings

## 5.1 LogViewer Findings

The LogViewer app (`~/Dropbox/projects/utilities/LogViewer`) provides a mature theme architecture:

**Colour Palette:**
- *Industrial Dark:* background `#2B2B2B`, surface `#3C3F41`, accent `#00A3E0`, text `#E0E0E0`, tab bg `#323232`.
- *Clean Light:* background `#FFFFFF`, surface `#F5F5F5`, accent `#007ACC`, text `#121212`, tab bg `#E0E0E0`.
- Semantic per-log-level colours (info, warn, error, debug, trace, fatal) with distinct light/dark variants.

**Theme Architecture:**
- `KLogViewerColors` object: centralised colour constants for both palettes.
- `KLogViewerTheme` composable: selects palette based on `isSystemInDarkTheme()`, provides Material `colors` + custom `LogLevelColors` and `CustomColors` via `staticCompositionLocalOf`.
- Theme accessor object (`KLogViewerTheme.logColors`, `KLogViewerTheme.customColors`) for convenient access.
- Compact 13sp sans-serif UI typography applied uniformly.

**Adaptable Patterns:**
- Dual-palette colour object → adapt to M3 `lightColorScheme`/`darkColorScheme`.
- Semantic CompositionLocal tokens → adapt log-level colours to **message-role/kind** colours (Human accent, Junie accent, Thought, Tool Call, Terminal, Diff added/removed, code background, error/warning).
- Theme accessor object → `JunieViewerTheme` object for convenient semantic token access.

## 5.2 Conversation/Chat-Transcript UI Research

Patterns observed across modern conversation UIs (Slack, Discord, AI chat apps, developer assistant UIs, support-ticket tools):

- **Role distinction:** Coloured accent rails or avatars distinguish Human from AI/assistant messages; colour is never the sole differentiator (icon + label + position also used).
- **Constrained line length:** Long-form AI responses use a readable max-width (600–720px) rather than spanning the full viewport.
- **Turn grouping:** Consecutive same-sender messages share a metadata header; timestamps and sender labels appear once per turn, not per message.
- **Collapsed secondary detail:** Intermediate reasoning (thoughts, tool calls) is collapsed by default with an expand affordance; keeps the conversation scannable.
- **Sticky context headers:** Session/conversation identity stays visible during scroll.
- **Subtle interaction states:** Hover highlights, selection states, and focus rings are present but restrained — they aid navigation without adding visual noise.
- **Restrained chrome:** Top bars and toolbars are compact; the conversation content dominates the viewport.
- **Visual rhythm:** Consistent spacing between turns, tighter spacing within turns, creates a readable cadence in long transcripts.
- **Accessibility:** High-contrast text, keyboard-navigable message lists, screen-reader landmarks, scalable text.

# 6. Scope

- Custom `JunieViewerTheme` composable with M3 `lightColorScheme`/`darkColorScheme` and app-specific semantic tokens via `CompositionLocal`.
- `ThemeMode` (Light/Dark/System) with manual override persisted in `Preferences`.
- Theme toggle in `SettingsDialog` and/or top bar.
- Replace all hardcoded colours, spacing, and typography with theme tokens.
- Polish application chrome (top bar, session context header, filter bar, settings/session picker).
- Polish conversation surface (message cards, Human/Junie distinction, turn grouping, readability).
- Polish rich content blocks (code, diff, terminal, tool call, thought, structured output, markdown, error/warning).
- Improve loading, empty, error, and no-results states.
- Accessibility: contrast ratios, keyboard focus, screen-reader semantics, scalable text.
- Cross-platform desktop polish: macOS, Windows, Linux behaviour.
- Extend existing test suite; no regressions.

# 7. Out of Scope

- Sidebar navigation (confirmed: restyle existing `SessionSelector` only).
- Real-time session tailing / streaming.
- Full Markdown parser replacement.
- Advanced syntax highlighting beyond existing dependencies.
- Mobile UI.
- New large dependencies without justification.
- Modifying `docs/UBIQUITOUS-LANGUAGE.md` (additions are follow-up tasks only).

# 8. User Stories

- As a **HITL**, I can switch between light, dark, and system-following themes, because the app respects my preference and persists it.
- As a **HITL**, I can immediately distinguish Human Messages from Junie Messages by colour accent, layout, and labelling, because the theme provides clear role-based visual tokens.
- As a **HITL**, I can comfortably read long Junie Responses, because typography, spacing, and line length are optimised for readability.
- As a **HITL**, I can scan a long Conversation quickly, because turn grouping, visual rhythm, and consistent spacing create a clear hierarchy.
- As a **HITL**, I can identify code blocks, diffs, terminal output, tool calls, and thoughts at a glance, because each has a distinct, themed visual treatment.
- As a **HITL**, I can copy code, diffs, and terminal output cleanly, because copy affordances are consistent and produce plain text.
- As a **HITL**, I can use the app on macOS, Windows, or Linux with native-feeling behaviour, because the theme and layout adapt to each platform.
- As a **HITL**, I can operate the entire UI by keyboard, because focus order is logical and all interactive elements are reachable.
- As a **HITL**, I can read the UI comfortably at different text scales, because the theme uses scalable typography.

# 9. Functional Requirements

- FR1: App provides light, dark, and system-following colour schemes.
- FR2: Theme preference is persisted via `PreferencesRepository` and restored on launch.
- FR3: A theme toggle is accessible from `SettingsDialog` or the top bar.
- FR4: All UI components use theme tokens exclusively — no hardcoded colours, spacing, or typography.
- FR5: Human and Junie Messages have distinct, themed visual treatments (accent, layout, labelling).
- FR6: Rich content blocks (code, diff, terminal, tool call, thought, structured output, error/warning) have distinct themed styles.
- FR7: Loading, empty, error, and no-results states are visually clear and themed.
- FR8: Session selector is restyled with improved density, hierarchy, and interaction states.

# 10. Non-Functional Requirements

- NFR1: WCAG AA contrast ratios (4.5:1 for normal text, 3:1 for large text) in both light and dark themes.
- NFR2: All interactive elements reachable by keyboard with visible focus indicators.
- NFR3: Screen-reader semantics (content descriptions, headings, roles) on all important elements.
- NFR4: Text scales correctly when system font size changes.
- NFR5: Consistent behaviour on macOS, Windows, and Linux (scrolling, copy, shortcuts, font rendering).
- NFR6: No performance regression — theme switch does not cause visible lag or excessive recomposition.
- NFR7: All existing tests continue to pass; new theme/component tests added.

# 11. Design Principles

1. **Theme tokens only.** Every colour, spacing value, and typography style comes from the theme system. No hardcoded literals in components.
2. **Semantic over structural.** Tokens are named for their purpose (`humanAccent`, `codeBackground`, `diffAdded`) not their appearance (`blue`, `grey200`).
3. **Restrained chrome.** The conversation content dominates; toolbars and controls are compact and unobtrusive.
4. **Asymmetry-aware.** Human Messages stay compact; Junie content owns the readable width.
5. **Progressive disclosure.** Secondary detail (thoughts, tool calls) is visually de-emphasised.
6. **Accessibility from the start.** Contrast, keyboard, and screen-reader support are built into every component, not bolted on later.
7. **Fallback over failure.** Any unstyled or unknown content degrades gracefully to readable themed text.

# 12. Proposed Visual System

## 12.1 Colour Tokens

### Light Palette (inspired by LogViewer Clean Light)

| Token | Hex | Usage |
|---|---|---|
| `background` | `#FFFFFF` | App background |
| `surface` | `#F5F5F5` | Cards, elevated surfaces |
| `surfaceVariant` | `#E8E8E8` | Secondary surfaces, filter bar |
| `primary` | `#007ACC` | Primary accent, links, active states |
| `primaryContainer` | `#E3F2FD` | Human message background |
| `secondaryContainer` | `#F5F5F5` | Junie message background |
| `onBackground` | `#121212` | Primary text |
| `onSurface` | `#121212` | Surface text |
| `onSurfaceVariant` | `#616161` | Secondary text, metadata |
| `error` | `#D32F2F` | Error states |
| `outline` | `#E0E0E0` | Borders, dividers |

### Dark Palette (inspired by LogViewer Industrial Dark)

| Token | Hex | Usage |
|---|---|---|
| `background` | `#1E1E1E` | App background |
| `surface` | `#2B2B2B` | Cards, elevated surfaces |
| `surfaceVariant` | `#3C3F41` | Secondary surfaces, filter bar |
| `primary` | `#00A3E0` | Primary accent, links, active states |
| `primaryContainer` | `#1A3A4A` | Human message background |
| `secondaryContainer` | `#2B2B2B` | Junie message background |
| `onBackground` | `#E0E0E0` | Primary text |
| `onSurface` | `#E0E0E0` | Surface text |
| `onSurfaceVariant` | `#9E9E9E` | Secondary text, metadata |
| `error` | `#FF5252` | Error states |
| `outline` | `#3C3F41` | Borders, dividers |

### Semantic Conversation Tokens (via `ConversationColors` + `CompositionLocal`)

| Token | Light | Dark | Usage |
|---|---|---|---|
| `humanAccent` | `#007ACC` | `#00A3E0` | Human message accent rail/border |
| `junieAccent` | `#4CAF50` | `#66BB6A` | Junie message accent rail/border |
| `thoughtBackground` | `#FFF8E1` | `#3E2723` | Thought block background |
| `thoughtBorder` | `#FFD54F` | `#795548` | Thought block border |
| `toolCallBackground` | `#F3E5F5` | `#1A237E` | Tool call block background |
| `toolCallBorder` | `#CE93D8` | `#5C6BC0` | Tool call block border |
| `terminalBackground` | `#263238` | `#1B1B1B` | Terminal output background |
| `terminalText` | `#4CAF50` | `#66BB6A` | Terminal output text |
| `codeBackground` | `#F5F5F5` | `#2B2B2B` | Code block background |
| `codeBorder` | `#E0E0E0` | `#3C3F41` | Code block border |
| `diffAdded` | `#E8F5E9` | `#1B3A1B` | Diff added-line background |
| `diffRemoved` | `#FFEBEE` | `#3A1B1B` | Diff removed-line background |
| `diffAddedText` | `#2E7D32` | `#66BB6A` | Diff added-line text |
| `diffRemovedText` | `#C62828` | `#EF5350` | Diff removed-line text |
| `errorBackground` | `#FFEBEE` | `#3A1B1B` | Error block background |
| `warningBackground` | `#FFF8E1` | `#3E2723` | Warning block background |

## 12.2 Typography Scale

Adapted from LogViewer's compact 13sp approach, using M3 `Typography`:

| Role | Size | Weight | Family | Usage |
|---|---|---|---|---|
| `headlineSmall` | 18sp | Bold | SansSerif | App title |
| `titleMedium` | 14sp | SemiBold | SansSerif | Section headers, turn headers |
| `titleSmall` | 13sp | SemiBold | SansSerif | Sender labels, kind markers |
| `bodyLarge` | 14sp | Normal | SansSerif | Message body text |
| `bodyMedium` | 13sp | Normal | SansSerif | Secondary text, metadata |
| `bodySmall` | 12sp | Normal | SansSerif | Timestamps, captions |
| `labelMedium` | 12sp | Medium | SansSerif | Filter chips, buttons |
| `labelSmall` | 11sp | Medium | SansSerif | Badges, counters |
| Code/Terminal | 13sp | Normal | Monospace | Code blocks, diffs, terminal |

## 12.3 Shape, Elevation, and Border Treatment

- **Message cards:** 8dp rounded corners, 1dp elevation (light) / 0dp elevation with subtle border (dark).
- **Code/diff/terminal blocks:** 6dp rounded corners, 1dp border, no elevation.
- **Filter chips:** 16dp rounded corners (pill shape).
- **Dialogs:** 12dp rounded corners, 4dp elevation.
- **Buttons:** 8dp rounded corners.
- **Dividers:** 1dp, `outline` colour, horizontal full-width.

## 12.4 Spacing and Density Scale

| Token | Value | Usage |
|---|---|---|
| `xs` | 2dp | Tight internal padding |
| `sm` | 4dp | Chip padding, icon gaps |
| `md` | 8dp | Standard internal padding |
| `lg` | 12dp | Card padding, section gaps |
| `xl` | 16dp | Screen-edge padding, major section gaps |
| `xxl` | 24dp | Between-turn spacing |

## 12.5 Component Hierarchy

```
JunieViewerTheme (M3 + semantic tokens)
├── Scaffold
│   ├── TopBar (title, session picker, settings, search)
│   ├── SessionContextHeader
│   ├── FilterBar
│   └── ConversationBody (LazyColumn)
│       ├── TurnHeader
│       ├── HumanMessageItem (compact, right-inset, humanAccent)
│       └── JunieMessageItem (full-width, junieAccent)
│           ├── MessageBody
│           │   ├── MarkdownContent
│           │   ├── CodeBlockWithCopy
│           │   ├── DiffBlock
│           │   ├── TerminalOutputBlock
│           │   ├── ThoughtBlock
│           │   ├── ToolCallBlock
│           │   ├── StructuredOutputBlock
│           │   └── ErrorWarningBlock
│           └── MessageKindMarker
├── SessionSelector (dialog, restyled)
├── SettingsDialog (with theme toggle)
└── FatalErrorDialog
```

# 13. Theme Architecture

## 13.1 `JunieViewerTheme` Composable

```kotlin
// ui/theme/JunieViewerTheme.kt
@Composable
fun JunieViewerTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
)
```

- Resolves `ThemeMode` to `isDark` boolean (System uses `isSystemInDarkTheme()`).
- Provides M3 `MaterialTheme` with custom `lightColorScheme()` / `darkColorScheme()`.
- Provides `ConversationColors` via `LocalConversationColors`.
- Provides `JunieViewerSpacing` via `LocalJunieViewerSpacing`.

## 13.2 `ThemeMode` Enum

```kotlin
// ui/theme/ThemeMode.kt
enum class ThemeMode { Light, Dark, System }
```

Persisted in `Preferences` via `PreferencesRepository` as a string key `"themeMode"`.

## 13.3 Semantic Token Access

```kotlin
// ui/theme/JunieViewerTheme.kt
object JunieViewerTheme {
    val conversationColors: ConversationColors
        @Composable get() = LocalConversationColors.current

    val spacing: JunieViewerSpacing
        @Composable get() = LocalJunieViewerSpacing.current
}
```

## 13.4 New Files

| File | Purpose |
|---|---|
| `ui/theme/JunieViewerTheme.kt` | Theme composable, CompositionLocal providers, accessor object |
| `ui/theme/ThemeMode.kt` | `ThemeMode` enum |
| `ui/theme/ConversationColors.kt` | Semantic colour data class + light/dark instances |
| `ui/theme/JunieViewerSpacing.kt` | Spacing scale data class |
| `ui/theme/JunieViewerTypography.kt` | Custom `Typography` definition |

# 14. Proposed Conversation Surface Improvements

- **Human Messages:** Compact, right-inset, `humanAccent` left border or accent rail, `primaryContainer` background, constrained max-width.
- **Junie Messages:** Full readable width, `junieAccent` left border, `secondaryContainer` background.
- **Turn grouping:** Consistent spacing within turns (`md`), larger spacing between turns (`xxl`), turn header with sender label and optional timestamp.
- **Message Kind markers:** Themed icon + label (not text glyphs), using semantic colours.
- **Long-form readability:** Constrained content width (max ~720dp), comfortable line height, themed body typography.

# 15. Proposed Session Navigation Improvements

- Restyle `SessionSelector` dialog with improved density and hierarchy.
- Hover and selection states on session list items.
- Active session visually highlighted.
- Session context header: compact, themed, always visible.
- No sidebar (confirmed design decision).

# 16. Proposed Rich Content Improvements

- **Code blocks:** Themed `codeBackground`/`codeBorder`, monospace typography token, consistent copy button styling.
- **Diffs:** Themed `diffAdded`/`diffRemoved` backgrounds and text colours, clear +/- indicators.
- **Terminal output:** Themed `terminalBackground`/`terminalText`, monospace, `$`-prefixed command styling.
- **Thought blocks:** Themed `thoughtBackground`/`thoughtBorder`, de-emphasised styling, collapsible.
- **Tool call blocks:** Themed `toolCallBackground`/`toolCallBorder`, structured layout, collapsible.
- **Structured output:** Themed monospace rendering, consistent with code block styling.
- **Error/warning blocks:** Themed `errorBackground`/`warningBackground`, icon + label (not colour alone).
- **Markdown:** Themed heading sizes, code spans, list markers, link styling.

# 17. Accessibility Requirements

- A1: WCAG AA contrast ratios in both themes.
- A2: Visible keyboard focus indicators on all interactive elements.
- A3: Logical focus order (top bar → filter bar → message list → dialogs).
- A4: `contentDescription` on all icons and interactive elements.
- A5: `semantics { heading() }` on section headers.
- A6: Colour is never the sole differentiator (always paired with icon/label/position).
- A7: Text scales with system font size preference.
- A8: Screen-reader landmarks for major UI regions.

# 18. Cross-Platform Desktop Considerations

- C1: Test on macOS, Windows, and Linux.
- C2: Native scrolling behaviour (smooth scroll, scroll speed).
- C3: Platform-appropriate keyboard shortcuts (Cmd on macOS, Ctrl on Windows/Linux).
- C4: Font rendering differences (ensure readability across platforms).
- C5: Window sizing and resizing behaviour.
- C6: Copy-to-clipboard produces clean plain text on all platforms.
- C7: High-DPI / Retina display support.

# 19. Testing Strategy

## 19.1 Automated Tests

- Extend existing Robot-pattern UI tests to verify themed components.
- Theme-switching test: verify light/dark/system modes apply correctly.
- Regression tests: all existing `ConversationScreenTest`, `AccessibilityAndArea8Test`, `RichContentRenderingTest` tests must pass.
- New component tests for themed rich-content blocks where practical.
- Test commands: `./gradlew test`, `./gradlew :shared:jvmTest`.

## 19.2 Manual Review Checklist

- Visual review of light and dark themes.
- Visual review of all message kinds in both themes.
- Keyboard navigation walkthrough.
- Cross-platform visual check (macOS, Windows, Linux).
- Copy-to-clipboard verification for code, diff, and terminal blocks.
- Font scaling verification.

# 20. Incremental Delivery Plan

## Part 1 — Design Discovery and Baseline Audit

- **Objective:** Confirm current UI state, capture design findings, establish design tokens.
- **After:** *After this part, the HITL should see a documented audit of the current UI and confirmed design token definitions.*

## Part 2 — Theme and Token Foundation

- **Objective:** Create `JunieViewerTheme`, `ThemeMode`, colour tokens, typography, spacing; wire into `App.kt`; add theme toggle to `SettingsDialog`.
- **Files:** `ui/theme/*`, `App.kt`, `SettingsDialog.kt`, `Preferences.kt`, `PreferencesRepository.kt`.
- **After:** *After this part, the HITL should be able to toggle between light, dark, and system themes via Settings, and see the app's background, surface, and text colours change accordingly.*

## Part 3 — Application Chrome Polish

- **Objective:** Restyle top bar, session context header, filter bar, session picker, and settings dialog using theme tokens.
- **Files:** `ConversationScreen.kt`, `SessionContextHeader.kt`, `FilterBar.kt`, `SessionSelector.kt`, `SettingsDialog.kt`.
- **After:** *After this part, the HITL should see a polished, compact top bar and filter area with consistent themed styling, and the session picker should have improved density and interaction states.*

## Part 4 — Conversation Surface Redesign

- **Objective:** Restyle message cards, Human/Junie distinction, turn grouping, kind markers, and long-form readability using theme tokens.
- **Files:** `MessageItems.kt`, `ConversationScreen.kt`.
- **After:** *After this part, the HITL should see clearly distinct Human and Junie messages with themed accent colours, improved turn grouping, and comfortable long-form readability.*

## Part 5 — Rich Content Styling

- **Objective:** Restyle all rich content blocks using semantic theme tokens.
- **Files:** `CodeBlockWithCopy.kt`, `DiffBlock.kt`, `TerminalOutputBlock.kt`, `ThoughtBlock.kt`, `ToolCallBlock.kt`, `StructuredOutputBlock.kt`, `ErrorWarningBlock.kt`, `MarkdownContent.kt`.
- **After:** *After this part, the HITL should see all rich content blocks (code, diff, terminal, thought, tool call, structured output, error/warning) with distinct, themed visual treatments in both light and dark modes.*

## Part 6 — States and Feedback

- **Objective:** Restyle loading, empty, error, and no-results states using theme tokens.
- **Files:** `ConversationScreen.kt`, `FatalErrorDialog.kt`.
- **After:** *After this part, the HITL should see polished, themed loading, empty, error, and no-results states.*

## Part 7 — Accessibility and Cross-Platform Polish

- **Objective:** Ensure contrast ratios, keyboard focus, screen-reader semantics, scalable text, and cross-platform behaviour.
- **Files:** All UI files, theme tokens.
- **After:** *After this part, the UI should be keyboard-navigable, have visible focus indicators, meet WCAG AA contrast, and behave consistently on macOS, Windows, and Linux.*

## Part 8 — Testing and Review

- **Objective:** Extend automated tests, run manual review checklist, fix issues.
- **Files:** Test files in `shared/src/commonTest/kotlin/.../ui/`.
- **After:** *After this part, all automated tests should pass (including new theme/component tests), and the manual review checklist should be completed.*

## Part 9 — Documentation and Completion

- **Objective:** Update `README.md`, `project_memory.md`, `RECAP.md`; record decisions and deferred items; run cyclomatic complexity check.
- **Files:** `README.md`, `docs/project_memory.md`, `docs/RECAP.md`.
- **After:** *After this part, all documentation should reflect the shipped UI changes, and the HITL should be able to run the application and confirm the sprint outcomes.*

# 21. Risks and Mitigations

- **R1 — Sprint 2 incomplete:** Areas 3–6 of Sprint 2 are still in progress. *Mitigation:* Sprint 3 can begin theme foundation work independently; conversation surface work depends on Sprint 2 baseline being stable.
- **R2 — Theme token proliferation:** Too many tokens become hard to maintain. *Mitigation:* Start with the minimal set defined in section 12; add tokens only when a concrete component needs them.
- **R3 — Cross-platform colour rendering:** Colours may appear differently on macOS/Windows/Linux. *Mitigation:* Test on all three platforms; prefer high-contrast token values.
- **R4 — Performance regression:** Theme switching or excessive recomposition. *Mitigation:* Use `staticCompositionLocalOf` for stable tokens; profile theme switch.
- **R5 — Accessibility gaps:** Contrast or focus issues discovered late. *Mitigation:* Accessibility is built into every part, not deferred to Part 7.
- **R6 — Scope creep:** Temptation to redesign layout during styling. *Mitigation:* This sprint reskins; layout changes are out of scope unless explicitly listed.

# 22. Open Questions

- **Q1:** Should Sprint 3 wait for Sprint 2 completion or begin against the current baseline?
- **Q2:** Adopt LogViewer's exact accent colours (`#00A3E0`/`#007ACC`) or define a Junie-branded accent?
- **Q3:** Is a monospace-font token for code/diff/terminal sufficient, or should a bundled font be considered?
- **Q4:** Should the theme toggle be in `SettingsDialog` only, or also in the top bar for quick access?
- **Q5:** Should Thought and Tool Call blocks be collapsed by default in the restyled UI?

# 23. Definition of Done

This sprint is done when all below hold:

- `JunieViewerTheme` composable exists with light/dark/system support and semantic tokens.
- `ThemeMode` preference is persisted and restored on launch.
- All UI components use theme tokens exclusively — no hardcoded colours, spacing, or typography.
- Human and Junie Messages have distinct, themed visual treatments.
- All rich content blocks have distinct, themed styles.
- Loading, empty, error, and no-results states are themed.
- Session selector is restyled with improved density and interaction states.
- WCAG AA contrast ratios met in both themes.
- Keyboard focus and screen-reader semantics present on all interactive elements.
- All existing tests pass; new theme/component tests added.
- Manual cross-platform review completed (macOS, Windows, Linux).
- `README.md`, `project_memory.md`, and `RECAP.md` updated.
- Cyclomatic complexity check run and reviewed.
- HITL final approval granted.
