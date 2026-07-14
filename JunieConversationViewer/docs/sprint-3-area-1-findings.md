# Area 1 Findings — Design Discovery and Baseline Audit

**Sprint:** 3 — UI Polish and Theme Refresh
**Date:** 2026-07-13
**Status:** Complete (awaiting HITL review)

---

## 1. Hardcoded Styling Audit

### 1.1 ConversationScreen.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 121 | ConversationTopBar | `16.dp` | Title padding | `JunieViewerTheme.spacing.xl` |
| 126 | ConversationTopBar | `8.dp` | Session picker button end padding | `JunieViewerTheme.spacing.md` |
| 133 | ConversationTopBar | `16.dp` | Settings button end padding | `JunieViewerTheme.spacing.xl` |
| 143 | ConversationTopBar | `16.dp, 2.dp` | Session context header padding | `JunieViewerTheme.spacing.xl`, `.xs` |
| 151 | ConversationTopBar | `16.dp` | Search field horizontal padding | `JunieViewerTheme.spacing.xl` |
| 162 | ConversationTopBar | `"✕"` text | Clear search icon | Themed icon component |
| 171 | ConversationTopBar | `8.dp` | Filter bar vertical padding | `JunieViewerTheme.spacing.md` |
| 186 | MatchNavigationBar | `16.dp, 4.dp` | Row padding | `JunieViewerTheme.spacing.xl`, `.sm` |
| 201 | MatchNavigationBar | `4.dp` | Match position end padding | `JunieViewerTheme.spacing.sm` |
| 205 | MatchNavigationBar | `32.dp` | Prev match button size | `JunieViewerTheme.spacing` or constant |
| 208 | MatchNavigationBar | `"▲"` text | Previous match indicator | Themed icon component |
| 212 | MatchNavigationBar | `32.dp` | Next match button size | `JunieViewerTheme.spacing` or constant |
| 215 | MatchNavigationBar | `"▼"` text | Next match indicator | Themed icon component |
| 234 | LoadingState | `16.dp` | Spacer height | `JunieViewerTheme.spacing.xl` |
| 248 | ErrorState | `"⚠"` text | Error icon | Themed icon component |
| 249 | ErrorState | `8.dp` | Spacer height | `JunieViewerTheme.spacing.md` |
| 251 | ErrorState | `16.dp` | Spacer height | `JunieViewerTheme.spacing.xl` |
| 271 | NoSessionState | `8.dp` | Spacer height | `JunieViewerTheme.spacing.md` |
| 286 | EmptyConversationState | `8.dp` | Spacer height | `JunieViewerTheme.spacing.md` |
| 319 | ConversationList | `16.dp` | Content padding | `JunieViewerTheme.spacing.xl` |
| 320 | ConversationList | `12.dp` | Vertical arrangement spacing | `JunieViewerTheme.spacing.lg` |

### 1.2 MessageItems.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 37 | HumanMessageItem | `480.dp` | Max card width | Themed constant or `JunieViewerTheme` |
| 38 | HumanMessageItem | `12.dp` | Inner padding | `JunieViewerTheme.spacing.lg` |
| 54 | JunieMessageItem | `16.dp` | Inner padding | `JunieViewerTheme.spacing.xl` |
| 101 | MessageCard | `8.dp` / `4.dp` | Spacer height (conditional) | `JunieViewerTheme.spacing.md` / `.sm` |
| 127 | TurnHeader | `8.dp` | Turn header label horizontal padding | `JunieViewerTheme.spacing.md` |
| 180 | UnsupportedEventCard | `4.dp` | Top padding | `JunieViewerTheme.spacing.sm` |
| 189 | UnsupportedEventCard | `8.dp` | Inner padding | `JunieViewerTheme.spacing.md` |

### 1.3 CodeBlock.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 42 | CodeBlock | `600.dp` | Max height | Themed constant |
| 43 | CodeBlock | `RoundedCornerShape(8.dp)` | Corner radius | Shape token |
| 45 | CodeBlock | `8.dp` | Inner padding | `JunieViewerTheme.spacing.md` |

### 1.4 DiffBlock.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 51 | DiffBlock | `600.dp` | Max height | Themed constant |
| 52 | DiffBlock | `RoundedCornerShape(8.dp)` | Corner radius | Shape token |
| 54 | DiffBlock | `8.dp` | Inner padding | `JunieViewerTheme.spacing.md` |
| 60 | DiffBlock | `Color(0x3300AA00)` | Added line background | `JunieViewerTheme.conversationColors.diffAdded` |
| 62 | DiffBlock | `Color(0x33CC0000)` | Removed line background | `JunieViewerTheme.conversationColors.diffRemoved` |
| 64 | DiffBlock | `Color(0x220000CC)` | Hunk header background | `JunieViewerTheme.conversationColors` (new token) |
| 65 | DiffBlock | `Color.Transparent` | Context line background | `Color.Transparent` (acceptable) |
| 69-72 | DiffBlock | `FontFamily.Monospace, 12.sp, 16.sp` | Monospace text style | `MaterialTheme.typography` code token |
| 77 | DiffBlock | `4.dp, 1.dp` | Line padding | `JunieViewerTheme.spacing.sm`, `.xs` |
| 41 | DiffBlock | `"📝 Diff"` emoji | Diff label icon | Themed icon component |

### 1.5 TerminalOutputBlock.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 40 | TerminalOutputBlock | `"⌨ Terminal"` emoji | Terminal label | Themed icon component |
| 50 | TerminalOutputBlock | `600.dp` | Max height | Themed constant |
| 51 | TerminalOutputBlock | `RoundedCornerShape(8.dp)` | Corner radius | Shape token |
| 52 | TerminalOutputBlock | `Color(0xFF1E1E1E)` | Terminal background | `JunieViewerTheme.conversationColors.terminalBackground` |
| 53 | TerminalOutputBlock | `8.dp` | Inner padding | `JunieViewerTheme.spacing.md` |
| 60-63 | TerminalOutputBlock | `FontFamily.Monospace, 12.sp, 16.sp` | Monospace text style | `MaterialTheme.typography` code token |
| 64 | TerminalOutputBlock | `Color(0xFF4EC9B0)` | Command line colour | `JunieViewerTheme.conversationColors.terminalCommand` (new) |
| 64 | TerminalOutputBlock | `Color(0xFFD4D4D4)` | Normal text colour | `JunieViewerTheme.conversationColors.terminalText` |
| 66 | TerminalOutputBlock | `4.dp, 1.dp` | Line padding | `JunieViewerTheme.spacing.sm`, `.xs` |

### 1.6 ThoughtBlock.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 43 | ThoughtBlock | `RoundedCornerShape(8.dp)` | Corner radius | Shape token |
| 46 | ThoughtBlock | `12.dp, 8.dp` | Header padding | `JunieViewerTheme.spacing.lg`, `.md` |
| 51 | ThoughtBlock | `"▼"` / `"▶"` text | Expand/collapse indicator | Themed icon component |
| 55 | ThoughtBlock | `8.dp` | Spacer width | `JunieViewerTheme.spacing.md` |
| 57 | ThoughtBlock | `"💭 Thought"` emoji | Thought label | Themed icon component |
| 63 | ThoughtBlock | `8.dp` | Spacer width | `JunieViewerTheme.spacing.md` |
| 81 | ThoughtBlock | `12.dp, 8.dp` | Body padding | `JunieViewerTheme.spacing.lg`, `.md` |

### 1.7 ToolCallBlock.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 46 | ToolCallBlock | `RoundedCornerShape(topStart=8.dp, topEnd=8.dp)` | Top corner radius | Shape token |
| 49 | ToolCallBlock | `12.dp, 8.dp` | Header padding | `JunieViewerTheme.spacing.lg`, `.md` |
| 54 | ToolCallBlock | `"▼"` / `"▶"` text | Expand/collapse indicator | Themed icon component |
| 57 | ToolCallBlock | `8.dp` | Spacer width | `JunieViewerTheme.spacing.md` |
| 59 | ToolCallBlock | `"🔧 $toolName"` emoji | Tool label | Themed icon component |
| 69-72 | ToolCallBlock | `FontFamily.Monospace, 12.sp, 16.sp` | Monospace text style | `MaterialTheme.typography` code token |
| 76 | ToolCallBlock | `400.dp` | Max height | Themed constant |
| 77 | ToolCallBlock | `RoundedCornerShape(bottomStart=8.dp, bottomEnd=8.dp)` | Bottom corner radius | Shape token |
| 79 | ToolCallBlock | `8.dp` | Body padding | `JunieViewerTheme.spacing.md` |

### 1.8 StructuredOutputBlock.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 39 | StructuredOutputBlock | `"📊 Structured Output"` emoji | Label | Themed icon component |
| 48-51 | StructuredOutputBlock | `FontFamily.Monospace, 12.sp, 16.sp` | Monospace text style | `MaterialTheme.typography` code token |
| 55 | StructuredOutputBlock | `400.dp` | Max height | Themed constant |
| 56 | StructuredOutputBlock | `RoundedCornerShape(8.dp)` | Corner radius | Shape token |
| 58 | StructuredOutputBlock | `8.dp` | Inner padding | `JunieViewerTheme.spacing.md` |

### 1.9 ErrorWarningBlock.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 38 | ErrorWarningBlock | `"⚠️"` / `"❌"` emoji | Warning/error icon | Themed icon component |
| 46 | ErrorWarningBlock | `12.dp` | Column padding | `JunieViewerTheme.spacing.lg` |
| 53 | ErrorWarningBlock | `8.dp` | Spacer width | `JunieViewerTheme.spacing.md` |
| 55 | ErrorWarningBlock | `4.dp` | Top padding | `JunieViewerTheme.spacing.sm` |

### 1.10 MarkdownContent.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 37 | MarkdownContent | `4.dp` | Block spacing | `JunieViewerTheme.spacing.sm` |
| 47 | MarkdownContent | `FontWeight.Bold` | Heading font weight | `MaterialTheme.typography` heading style |
| 62 | MarkdownContent | `4.dp` | Code fence vertical padding | `JunieViewerTheme.spacing.sm` |
| 153 | renderInlineMarkdown | `FontFamily.Monospace, 13.sp` | Inline code style | `MaterialTheme.typography` code token |

### 1.11 SessionSelector.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 40 | SessionSelector | `24.dp` | Dialog padding | `JunieViewerTheme.spacing.xxl` |
| 45 | SessionSelector | `16.dp` | Title bottom padding | `JunieViewerTheme.spacing.xl` |
| 65 | SessionSelector | `16.dp` | Button row top padding | `JunieViewerTheme.spacing.xl` |
| 109 | SessionItem | `4.dp` | Item vertical padding | `JunieViewerTheme.spacing.sm` |
| 113 | SessionItem | `16.dp` | Item inner padding | `JunieViewerTheme.spacing.xl` |
| 120 | SessionItem | `FontWeight.Bold` / `FontWeight.Normal` | Selected state weight | `MaterialTheme.typography` |
| 129 | SessionItem | `2.dp` | Working directory top padding | `JunieViewerTheme.spacing.xs` |
| 147 | SessionItem | `4.dp` | Timestamp top padding | `JunieViewerTheme.spacing.sm` |

### 1.12 SessionContextHeader.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 40 | SessionContextHeader | `12.dp, 6.dp` | Inner padding | `JunieViewerTheme.spacing.lg`, custom |

### 1.13 SettingsDialog.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 24 | SettingsDialog | `24.dp` | Dialog padding | `JunieViewerTheme.spacing.xxl` |
| 29 | SettingsDialog | `16.dp` | Title bottom padding | `JunieViewerTheme.spacing.xl` |
| 43 | SettingsDialog | `24.dp` | Button row top padding | `JunieViewerTheme.spacing.xxl` |

### 1.14 FatalErrorDialog.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 27 | FatalErrorDialog | `6.dp` | Tonal elevation | Elevation token |
| 28 | FatalErrorDialog | `400.dp` | Dialog width | Themed constant |
| 30 | FatalErrorDialog | `24.dp` | Column padding | `JunieViewerTheme.spacing.xxl` |
| 35 | FatalErrorDialog | `16.dp` | Spacer height | `JunieViewerTheme.spacing.xl` |
| 37 | FatalErrorDialog | `8.dp` | Spacer height | `JunieViewerTheme.spacing.md` |
| 43 | FatalErrorDialog | `24.dp` | Spacer height | `JunieViewerTheme.spacing.xxl` |

### 1.15 FilterBar.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 27 | FilterBar | `16.dp` | Horizontal padding | `JunieViewerTheme.spacing.xl` |
| 28 | FilterBar | `8.dp` | Chip spacing | `JunieViewerTheme.spacing.md` |

### 1.16 CopyButton.kt

| Line | Component | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 25 | CopyButton | `"📋 Copy"` emoji | Copy label | Themed icon component |

### 1.17 MessageFormatting.kt

| Line | Function | Hardcoded Value | Purpose | Recommended Token |
|---|---|---|---|---|
| 12-29 | messageKindLabel | 17 emoji glyphs (💬📄💭🔧📝⌨📊❌⚠️⚠🧪🔌🤖❓🔘ℹ️⛔📋) | Message kind indicators | Themed icon components |

### Audit Summary

| Category | Count |
|---|---|
| Hardcoded `.dp` values | ~65 |
| Hardcoded `.sp` values | ~8 |
| Hardcoded `Color(0x...)` | 4 (DiffBlock: 3, TerminalOutputBlock: 1) |
| Hardcoded `Color.Xxx` | 3 (Color.Transparent, Color(0xFF4EC9B0), Color(0xFFD4D4D4)) |
| Hardcoded `FontFamily.Monospace` | 4 (DiffBlock, TerminalOutputBlock, ToolCallBlock, StructuredOutputBlock) |
| Hardcoded `FontWeight` | 3 (MarkdownContent, SessionSelector) |
| Hardcoded `RoundedCornerShape` | ~8 |
| Emoji/text glyphs as icons | ~25 (17 in MessageFormatting + 8 scattered) |
| **Total hardcoded instances** | **~120** |

---

## 2. LogViewer Theme Architecture Findings

### 2.1 Architecture Overview

The LogViewer app (`~/Dropbox/projects/utilities/LogViewer/ui/src/main/kotlin/com/klogviewer/ui/theme/`) uses a two-file theme system:

**`KLogViewerColors.kt`** — Centralised colour constants object:
- All colours defined as `val` properties in a single `object KLogViewerColors`
- Dual palettes: `darkBackground`, `darkSurface`, `darkAccent` vs `lightBackground`, `lightSurface`, `lightAccent`
- Semantic log-level colours: `infoColor`, `warnColor`, `errorColor`, `debugColor`, `traceColor`, `fatalColor` — each with light and dark variants
- Custom UI colours: `tabBackground`, `selectedTabBackground`, `dividerColor` — each with light and dark variants

**`KLogViewerTheme.kt`** — Theme composable + accessor object:
- `KLogViewerTheme` composable wraps `MaterialTheme` with custom `colors` (Material 2)
- Uses `isSystemInDarkTheme()` to select palette
- Provides semantic colours via `staticCompositionLocalOf`:
  - `LocalLogLevelColors` → `LogLevelColors` data class
  - `LocalCustomColors` → `CustomColors` data class
- Accessor object: `KLogViewerTheme.logColors` and `KLogViewerTheme.customColors` for convenient access from components
- Compact 13sp sans-serif typography applied uniformly

### 2.2 Mapping to Junie Conversation Viewer (Material 3)

| LogViewer Pattern | Junie Viewer M3 Equivalent |
|---|---|
| `KLogViewerColors` object (dual palettes) | `lightColorScheme()` / `darkColorScheme()` in `JunieViewerTheme.kt` |
| `LogLevelColors` data class (info/warn/error/debug/trace/fatal) | `ConversationColors` data class (humanAccent/junieAccent/thought/toolCall/terminal/diff/code/error/warning) |
| `LocalLogLevelColors` via `staticCompositionLocalOf` | `LocalConversationColors` via `staticCompositionLocalOf` |
| `CustomColors` data class (tab/divider/selection) | Absorbed into M3 `ColorScheme` + `ConversationColors` |
| `KLogViewerTheme` accessor object | `JunieViewerTheme` accessor object |
| `isSystemInDarkTheme()` selection | `ThemeMode` enum (Light/Dark/System) with `isSystemInDarkTheme()` fallback |
| Material 2 `MaterialTheme(colors = ...)` | Material 3 `MaterialTheme(colorScheme = ..., typography = ..., shapes = ...)` |
| Single typography (13sp sans-serif) | M3 `Typography` scale (11sp–18sp) per sprint doc section 12.2 |

### 2.3 Key Adaptation Notes

1. **M3 colour scheme replaces Material 2 colours.** LogViewer uses `MaterialTheme.colors`; we use `MaterialTheme.colorScheme` with `lightColorScheme()`/`darkColorScheme()`.
2. **Semantic tokens are domain-specific.** LogViewer's log-level colours map to our message-role/kind colours. The pattern (data class + CompositionLocal + accessor object) is identical.
3. **ThemeMode adds manual override.** LogViewer only follows system; we add Light/Dark/System with persistence via `PreferencesRepository`.
4. **Spacing tokens are new.** LogViewer has no spacing abstraction; we add `JunieViewerSpacing` data class + `LocalJunieViewerSpacing`.

---

## 3. Conversation/Chat UI Research Findings

### 3.1 Role Distinction (Slack, Discord, AI Chat Apps)

- **Accent rails/borders:** Coloured left border (2–4dp) distinguishes sender roles. Human messages use one accent, assistant/AI uses another.
- **Layout asymmetry:** Human messages are compact and right-aligned or right-inset; AI/assistant messages are full-width and left-aligned. This mirrors the asymmetric nature of conversations (short human prompts, long AI responses).
- **Non-colour-only differentiation:** Sender label + icon + position + accent colour together. Never rely on colour alone (accessibility).
- **Avatars/icons:** Optional but common. For a log/transcript viewer, a simple sender label is sufficient.

### 3.2 Turn Grouping

- **Shared metadata headers:** Consecutive same-sender messages share a single header (sender label, timestamp). Reduces visual noise.
- **Spacing hierarchy:** Tight spacing within a turn (4–8dp), larger spacing between turns (16–24dp). Creates visual rhythm.
- **Dividers between turns:** Subtle horizontal dividers or increased spacing mark turn boundaries. The current `TurnHeader` with divider lines is a good pattern.

### 3.3 Constrained Line Length

- **Max content width:** 600–720dp for long-form text. Prevents eye-tracking fatigue on wide screens.
- **Human messages narrower:** 400–500dp max width for short prompts. Already implemented at 480dp.
- **Full-width for code/diff/terminal:** These blocks benefit from available width for readability.

### 3.4 Collapsible Detail (Progressive Disclosure)

- **Collapsed by default:** Intermediate reasoning (thoughts), tool calls, and verbose output are collapsed. Already implemented in ThoughtBlock and ToolCallBlock.
- **Expand affordance:** Chevron icon (▶/▼) with a summary preview. Current text glyphs should become themed icons.
- **Smooth animation:** AnimatedVisibility (already used) provides good UX.

### 3.5 Code/Diff/Terminal Block Readability

- **Distinct backgrounds:** Code blocks use a slightly different surface colour from message cards. Already partially done with `surfaceVariant`.
- **Monospace typography:** Consistent monospace font across all code-like content. Currently hardcoded per-block; should be a single typography token.
- **Line numbers (optional):** Not required for this viewer but could be a future enhancement.
- **Copy affordance:** Consistent copy button placement (top-right of block). Already implemented.

### 3.6 Accessibility Patterns

- **WCAG AA contrast:** 4.5:1 for normal text, 3:1 for large text. Must verify both light and dark palettes.
- **Keyboard navigation:** Logical focus order through top bar → search → filters → message list → dialogs.
- **Screen-reader landmarks:** `semantics { heading() }` on section headers, `contentDescription` on icons. Partially implemented.
- **Scalable text:** Use `sp` units (already done) and avoid fixed-height containers that clip scaled text.
- **Colour not sole differentiator:** Always pair colour with icon/label/position. MessageFormatting already uses emoji+label; should become themed icon+label.

### 3.7 Visual Rhythm and Chrome

- **Restrained chrome:** Top bar and toolbars are compact; conversation content dominates viewport. Current implementation is reasonable.
- **Consistent spacing:** Use a spacing scale (xs/sm/md/lg/xl/xxl) throughout. Currently ad-hoc.
- **Subtle interaction states:** Hover highlights and selection states aid navigation without visual noise. Currently missing — should be added in Areas 3–4.

---

## 4. Confirmed Design Token Definitions

### 4.1 Light Colour Palette — ✅ Ready for Implementation

Per sprint doc section 12.1. All hex values confirmed:

| Token | Hex | Status |
|---|---|---|
| `background` | `#FFFFFF` | ✅ Ready |
| `surface` | `#F5F5F5` | ✅ Ready |
| `surfaceVariant` | `#E8E8E8` | ✅ Ready |
| `primary` | `#007ACC` | ✅ Ready |
| `primaryContainer` | `#E3F2FD` | ✅ Ready |
| `secondaryContainer` | `#F5F5F5` | ✅ Ready |
| `onBackground` | `#121212` | ✅ Ready |
| `onSurface` | `#121212` | ✅ Ready |
| `onSurfaceVariant` | `#616161` | ✅ Ready |
| `error` | `#D32F2F` | ✅ Ready |
| `outline` | `#E0E0E0` | ✅ Ready |

### 4.2 Dark Colour Palette — ✅ Ready for Implementation

| Token | Hex | Status |
|---|---|---|
| `background` | `#1E1E1E` | ✅ Ready |
| `surface` | `#2B2B2B` | ✅ Ready |
| `surfaceVariant` | `#3C3F41` | ✅ Ready |
| `primary` | `#00A3E0` | ✅ Ready |
| `primaryContainer` | `#1A3A4A` | ✅ Ready |
| `secondaryContainer` | `#2B2B2B` | ✅ Ready |
| `onBackground` | `#E0E0E0` | ✅ Ready |
| `onSurface` | `#E0E0E0` | ✅ Ready |
| `onSurfaceVariant` | `#9E9E9E` | ✅ Ready |
| `error` | `#FF5252` | ✅ Ready |
| `outline` | `#3C3F41` | ✅ Ready |

### 4.3 Semantic Conversation Tokens — ✅ Ready (with one addition)

All tokens from sprint doc section 12.1 confirmed. One new token identified during audit:

| Token | Light | Dark | Status |
|---|---|---|---|
| `humanAccent` | `#007ACC` | `#00A3E0` | ✅ Ready |
| `junieAccent` | `#4CAF50` | `#66BB6A` | ✅ Ready |
| `thoughtBackground` | `#FFF8E1` | `#3E2723` | ✅ Ready |
| `thoughtBorder` | `#FFD54F` | `#795548` | ✅ Ready |
| `toolCallBackground` | `#F3E5F5` | `#1A237E` | ✅ Ready |
| `toolCallBorder` | `#CE93D8` | `#5C6BC0` | ✅ Ready |
| `terminalBackground` | `#263238` | `#1B1B1B` | ✅ Ready |
| `terminalText` | `#4CAF50` | `#66BB6A` | ✅ Ready |
| `terminalCommand` | `#00897B` | `#4EC9B0` | ✅ **NEW** — command lines in terminal |
| `codeBackground` | `#F5F5F5` | `#2B2B2B` | ✅ Ready |
| `codeBorder` | `#E0E0E0` | `#3C3F41` | ✅ Ready |
| `diffAdded` | `#E8F5E9` | `#1B3A1B` | ✅ Ready |
| `diffRemoved` | `#FFEBEE` | `#3A1B1B` | ✅ Ready |
| `diffAddedText` | `#2E7D32` | `#66BB6A` | ✅ Ready |
| `diffRemovedText` | `#C62828` | `#EF5350` | ✅ Ready |
| `diffHunkHeader` | `#E3F2FD` | `#1A237E` | ✅ **NEW** — @@ hunk headers in diffs |
| `errorBackground` | `#FFEBEE` | `#3A1B1B` | ✅ Ready |
| `warningBackground` | `#FFF8E1` | `#3E2723` | ✅ Ready |

### 4.4 Typography Scale — ✅ Ready for Implementation

Per sprint doc section 12.2. All values confirmed.

### 4.5 Spacing Scale — ✅ Ready for Implementation

Per sprint doc section 12.4. All values confirmed: xs=2dp, sm=4dp, md=8dp, lg=12dp, xl=16dp, xxl=24dp.

### 4.6 Shape Rules — ✅ Ready for Implementation

Per sprint doc section 12.3. All values confirmed.

### 4.7 Open Questions — Resolved

| # | Question | Decision (HITL 2026-07-14) |
|---|---|---|
| Q1 | Should Sprint 3 wait for Sprint 2 completion or run against the current baseline? | **Run against current baseline.** No blocking dependency on Sprint 2 completion. |
| Q2 | Adopt LogViewer's exact accent colours (`#00A3E0`/`#007ACC`) or a Junie-branded accent? | **Use LogViewer colours.** `#007ACC` (light) and `#00A3E0` (dark) as the primary accent. |
| Q3 | Is a monospace-font token for code/diff/terminal sufficient, or should a bundled font be considered? | **`FontFamily.Monospace` is sufficient.** No bundled font needed initially. |
| Q4 | Theme toggle placement: SettingsDialog only, or also in top bar? | **Settings dialog only.** Keeps the top bar clean. |
| Q5 | Default collapse state for ThoughtBlock and ToolCallBlock? | **Collapsed by default.** Keeps conversation scannable. |
| Q6 | Should `terminalCommand` be a new semantic token? (Identified during audit — terminal commands currently use hardcoded `Color(0xFF4EC9B0)`) | **Yes, add `terminalCommand` to `ConversationColors`.** |
| Q7 | Should `diffHunkHeader` be a new semantic token? (Identified during audit — hunk headers currently use hardcoded `Color(0x220000CC)`) | **Yes, add `diffHunkHeader` to `ConversationColors`.** |

---

## 5. Project Documentation Review

### 5.1 Domain Terms (from UBIQUITOUS-LANGUAGE.md)

Confirmed canonical terms used consistently in this document and throughout Sprint 3 planning:

- **Session** — a single Junie working session (directory of events)
- **Conversation** — the ordered sequence of Messages within a Session
- **Turn** — a group of consecutive Messages from the same Sender
- **Message** — a single communication unit within a Conversation
- **Human** — the user/developer who initiated the Session
- **Junie** — the AI assistant
- **Sender** — either Human or Junie
- **MessageKind** — the type/category of a Message (Text, Markdown, Thought, Tool, Patch, Terminal, etc.)
- **MessageContent** — the payload of a Message (Text, Code, Diff, Terminal, Structured)
- **HITL** — Human-In-The-Loop reviewer

### 5.2 Testing Expectations (from TESTING.md)

- **Robot pattern** for UI tests (arrange/act/assert via robot DSL)
- **Semantic `testTag`** conventions: `message_item_human`, `message_item_junie`, `turn_header`, `thought_block`, `tool_call_block`, etc.
- **Gradle commands:** `./gradlew test`, `./gradlew :shared:jvmTest`
- **Accessibility tests** exist in `AccessibilityAndArea8Test.kt`
- **Rich content tests** exist in `RichContentRenderingTest.kt`
- All existing tests must continue to pass during Sprint 3 implementation

### 5.3 Historical Context (from project_memory.md and RECAP.md)

- Sprint 1: Core parsing and data layer
- Sprint 2: Conversation UI implementation — MVI pattern, message rendering, search/filter, session selection, rich content blocks, accessibility foundations
- Current state: functional UI with default `MaterialTheme {}`, no custom theme
- Key gotcha: `CodeTextView` (KodeView) requires `heightIn(max = ...)` to avoid infinite-height crashes in `LazyColumn`
- Key gotcha: `FontFamily.Monospace` is the only reliable cross-platform monospace option without bundling fonts

### 5.4 Sprint 2 Baseline

Sprint 2 established the component architecture that Sprint 3 will restyle:
- ConversationScreen with Scaffold, top bar, search, filters, state handling
- MessageItems with HumanMessageItem/JunieMessageItem/TurnHeader/MessageBody
- Rich content blocks: CodeBlock, DiffBlock, TerminalOutputBlock, ThoughtBlock, ToolCallBlock, StructuredOutputBlock, ErrorWarningBlock
- MarkdownContent renderer
- SessionSelector dialog, SessionContextHeader, SettingsDialog, FatalErrorDialog
- FilterBar with message kind chips
- CopyButton for clipboard operations

---

## 6. Risks and Observations

1. **High hardcoded instance count (~120).** The migration to theme tokens will touch every UI file. Recommend incremental migration per delivery part to keep changes reviewable.
2. **Emoji as icons.** 25+ emoji glyphs are used as visual indicators. These render inconsistently across platforms (macOS/Windows/Linux). Replacing with themed icon components is important for cross-platform consistency.
3. **Monospace typography duplication.** Four files independently define `FontFamily.Monospace, 12.sp, 16.sp`. A single typography token will eliminate this duplication.
4. **No hover/selection states.** Currently no hover or focus visual feedback on message cards, session items, or interactive elements beyond default Material behaviour.
5. **CodeBlock dark mode.** `SyntaxThemes.default(darkMode = false)` is hardcoded — must be wired to `ThemeMode`.

---

## 7. HITL Review Status

**Status: Awaiting HITL review.**

This document contains the complete Area 1 findings. The HITL should review:

1. ✅ Hardcoded styling audit (Section 1) — is it complete and accurate?
2. ✅ LogViewer architecture mapping (Section 2) — is the M3 adaptation approach sound?
3. ✅ Conversation/chat UI research (Section 3) — are the patterns relevant and well-captured?
4. ✅ Design token definitions (Section 4) — are the palettes, tokens, and scales ready?
5. ✅ Open questions Q1–Q7 (Section 4.7) — HITL input needed on these before implementation.
6. ✅ Risks (Section 6) — any concerns?

Task 1.6 remains unchecked until HITL approval is received.
