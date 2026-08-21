# Project Memory

## Implement Sprint 8: Top-Level Session Search
**Date/Time:** 2026-08-21 09:30

### What was shipped
- Cross-session search pipeline traversing discovered sessions and scanning `events.jsonl` files with resilient error isolation.
- Case-insensitive substring matching and stable bounded preview snippet generation.
- Dedicated top-level search dialog/panel with toolbar entry point and menu accelerator (`Cmd+Shift+F` / `Ctrl+Shift+F`).
- Session-level result rows with match count badges, snippets, keyboard navigation, and test tags.
- Automatic propagation of top-level search queries to newly opened sessions and navigation to the first matching entry.
- Resilient file handling and detailed WARN-level logging for missing, empty, unreadable, or malformed session files.
- Domain support for additional event types (`PlanReviewResolvedEvent`, `UserMessagesDroppedFromHistory`).

### Key decisions
- Used on-demand session scanning MVP strategy with explicit deferral of persistent indexing.
- Implemented deterministic ordering (match count descending → session timestamp descending → stable session ID tie-break).
- Kept top-level search strictly isolated from current-session conversation search (`Search Messages`).

### Gotchas
- Empty and missing session files must be quietly ignored rather than treated as malformed partial failures.

### Test coverage areas
- Repository search tests covering multiple sessions, edge cases, partial failures, missing files, and empty files.
- ViewModel coroutine tests covering debounce, cancellation, state transitions, and result selection propagation.
- Compose UI tests verifying dialog states, result rows, keybindings, and search independence.

## Acknowledge project memory requirement
**Date/Time:** 2026-06-22 14:26

### What was shipped
- Initialized `docs` directory.
- Created `docs/project_memory.md` to track project history as required by `AGENTS.md`.

### Key decisions
- Decided to initialize the `docs` folder and `project_memory.md` immediately to demonstrate compliance with the project guidelines.
- Located `AGENTS.md` in `~/.junie/AGENTS.md`.

### Gotchas
- `AGENTS.md` and `docs/` were not initially present in the project root; `AGENTS.md` is a global configuration file in the user's home directory.

### Test coverage areas
- Documentation only; no code changes.

## Organize documentation structure
**Date/Time:** 2026-06-22 14:31

### What was shipped
- Created `docs/sprints` and `docs/tasks` subdirectories.
- Moved `docs/TASKS.md` to `docs/tasks/TASKS.md`.

### Key decisions
- Organized documentation to separate sprint and task documents as requested by the user.
- Updated existing documentation references to the new paths.

### Gotchas
- None.

### Test coverage areas
- Documentation only; no code changes.

## Synchronize sprint and task documents
**Date/Time:** 2026-06-22 14:35

### What was shipped
- Updated `docs/sprints/junie-conversation-viewer-sprint-0.md` to reflect completed tasks in Sprint 0.
- Updated `docs/tasks/junie-conversation-viewer-tasks-sprint-0.md` with detailed tasks for Sprint 0 and synchronized their status.

### Key decisions
- Verified implementation details in the codebase (MVI, parsing, UI, search, code highlighting) before ticking off tasks.
- Synchronized both the sprint planning document and the task tracking document to ensure consistency.

### Test coverage areas
- Documentation only; no code changes.

## Update guidelines and generate skills
**Date/Time:** 2026-06-22 14:48

### What was shipped
- Updated `~/.junie/AGENTS.md` to reflect the new `docs/tasks/` and `docs/sprints/` structure.
- Created local skill `project-memory` to help with `project_memory.md` updates.
- Created local skill `junie-log-format` to document the `events.jsonl` schema.

### Key decisions
- Decided to create project-specific skills in `.junie/skills` to encapsulate knowledge discovered during development.
- Updated the global `AGENTS.md` to ensure consistency between project structure and general guidelines.

### Gotchas
- None.

### Test coverage areas
- Documentation and local skill configuration.

## Update skills front-matter and prepare improvement questions
**Date/Time:** 2026-06-22 15:05

### What was shipped
- Added YAML front-matter (name, description) to `.junie/skills/project-memory/SKILL.md` and `.junie/skills/junie-log-format/SKILL.md`.
- Analyzed codebase for Sprint 0 "Walking Skeleton" completeness.

### Key decisions
- Decided to focus on session management, real-time updates, and event aggregation as key improvement areas.
- Maintained consistency with project guidelines for skill documentation.

### Gotchas
- Discovered that the current implementation of `SessionRepository` treats every update event as a new message, which might lead to a cluttered UI for long-running agent thoughts.

### Test coverage areas
- Skill file formatting and repository logic analysis.

## Implement Sprint 1: Session Management and Metadata Search
**Date/Time:** 2026-06-22 15:45

### What was shipped
- Dynamic session discovery and selection from `~/.junie/sessions/`.
- Persistent user preferences for `junieHomePath` and `lastSessionId`.
- Metadata-based filtering (Human, Junie, Thoughts, Tools, Patches, Terminal).
- New UI components: `SessionSelector`, `SettingsDialog`, `FilterBar`.

### Key decisions
- Extended `Platform` interface to provide `userHome` for path expansion.
- Used `ModalBottomSheet`-like `Dialog` for session selection and settings on Desktop.
- Implemented in-memory filtering in `ConversationViewModel` for responsiveness.
- Added `kind` and `timestamp` to `Message` domain model to support advanced filtering and future sorting.

### Gotchas
- `compose-material-icons-extended` has been added to the classpath (via `libs.versions.toml`). Material Icons are now available — text-based buttons can be replaced with icon buttons where appropriate.
- `System.getProperty("user.home")` is JVM-specific, so it was abstracted behind the `Platform` interface to keep `commonMain` clean.

### Test coverage areas
- `ConversationViewModelTest` updated to cover session initialization and filtering logic.
- `PreferencesRepositoryTest` verifies persistence of new preference fields.

## Enhance project documentation and automate README updates
**Date/Time:** 2026-06-22 15:55

### What was shipped
- Updated `README.md` with detailed project description, features, and usage instructions.
- Created `readme-updater` skill to automate README maintenance.
- Updated global `AGENTS.md` to require README updates at the end of every sprint.

### Key decisions
- Decided to move from a generic KMP template to a product-focused `README.md`.
- Integrated `readme-updater` skill into the standard "Sprint / task completion" workflow in `AGENTS.md`.

### Gotchas
- None.

### Test coverage areas
- Documentation and global configuration.
 
## Add centralized logging using Kermit
**Date/Time:** 2026-06-22 17:15
 
### What was shipped
- Integrated Kermit logging library into the project.
- Added instrumentation to `JsonlParser`, `SessionRepository`, `PreferencesRepository`, and `ConversationViewModel`.
- Added platform-specific logging in `JVMPlatform`.
- Fixed a redundant `else` warning in `SessionRepository`.
 
### Key decisions
- Chose **Kermit** by Touchlab as the logging library due to its excellent KMP support and ease of use with platform-specific loggers.
- Used tagged loggers for each component to make it easier to filter logs by source.
 
### Gotchas
- None.
 
### Test coverage areas
- Core logic remains covered by existing tests; logging was verified through build success and manual review of instrumentation.
 
## Implement Rolling File Logging
**Date/Time:** 2026-06-22 17:45
 
### What was shipped
- Configured Logback for rolling file logging based on date and size.
- Implemented `Slf4jLogger` adapter for Kermit in `desktopApp`.
- Extended `Platform` interface with `logsPath`.
- Added support for external `logback.xml` configuration.
 
### Key decisions
- Chose **Logback** as the logging backend for the JVM/Desktop app due to its robust support for rolling files and XML-based configuration.
- Placed default logs in `~/.junieviewer/logs` (platform-specific standard locations).
- Implemented a custom Kermit `LogWriter` instead of using a third-party extension to minimize dependency issues and keep the implementation lightweight.
 
### Gotchas
- `kermit-slf4j` artifact was not found on Maven Central for version 2.0.4, necessitating a custom (but simple) adapter.
 
### Test coverage areas
- Build verification and manual validation of log file creation and rolling configuration.

## Implement Sprint 1: Global Error Handling
**Date/Time:** 2026-06-22 18:15

### What was shipped
- Global uncaught exception handler using `Thread.setDefaultUncaughtExceptionHandler`.
- `FatalErrorManager` singleton for centralized error reporting in common code.
- `FatalErrorDialog` Compose component for user-friendly error messages.
- Fallback `JOptionPane` dialog for fatal crashes where the UI thread is compromised.
- Defensive coding in `SessionRepository` to handle invalid paths gracefully.

### Key decisions
- Used `Thread.setDefaultUncaughtExceptionHandler` as the ultimate safety net to satisfy the "caught at the top" requirement.
- Implemented a two-tier error UI: a Compose dialog for managed errors and a Swing `JOptionPane` for unmanaged crashes.
- Avoided vague "Something went wrong" messages in favor of more professional "Unexpected technical problem" wording.

### Gotchas
- Unhandled exceptions in Compose click handlers might not always reach the global handler depending on the event loop state; added explicit `try-catch` in `onAction` to compensate.
- Default Compose for Desktop `AlertDialog` may show a generic "Error" title; switched to a standard `Dialog` + `Surface` for the fatal error message to ensure a professional header.
- Avoided duplicate dialogs by tracking error reporting status in `FatalErrorManager` and checking it in the global `UncaughtExceptionHandler`.

### Test coverage areas
- Build verification and static analysis of error propagation paths.

## Implement Sprint 1 (Testing)
**Date/Time:** 2026-06-22 19:30

### What was shipped
- Expanded `JsonlParserTest` with exhaustive edge cases (malformed JSON, missing fields).
- Implemented `SessionRepositoryTest` with temporary directory isolation for file system logic.
- Achieved 100% test coverage for `ConversationViewModel` actions and state transitions using `Turbine`.
- Implemented the **Robot Pattern** for UI testing.
- Added functional UI tests for `ConversationScreen` verifying search and filtering.
- Created `docs/TESTING.md` documenting testing standards and patterns.

### Key decisions
- Adopted the **Robot Pattern** to decouple UI tests from Compose implementation details.
- Used **Turbine** for state-flow verification, ensuring all intermediate state transitions are correct.
- Switched from `FakeFileSystem` to temporary directories for repository tests due to binary compatibility issues with Kotlin 2.4.0.
- Decided to use `UnconfinedTestDispatcher` in ViewModel tests to simplify immediate state verification.

### Gotchas
- Discovered that `StateFlow` updates in the ViewModel can emit intermediate states (e.g. updating query then updating filtered list), requiring Turbine to consume multiple items.
- `LibraryLoadException` during UI tests on Desktop was resolved by explicitly adding `compose.desktop.currentOs` native dependencies to `jvmTest`.

### Test coverage areas
- Data Layer: `JsonlParser`, `SessionRepository`.
- Presentation Layer: `ConversationViewModel`.
- UI Layer: `ConversationScreen` (Robot-based).

---

## Area 7 & 8 — Accessibility, Desktop Polish, and Automated Testing

### Title
Area 7 (Accessibility & Cross-Platform Desktop Polish) and Area 8 (Automated Testing)

### Date/time completed
2026-07-13 14:45

### What was shipped
- Added `semantics { contentDescription = "..." }` to 12 key composables in `ConversationScreen.kt` (session picker, settings, search clear, match nav buttons, loading/error/empty/no-session/no-results states, retry button, app title heading).
- Verified keyboard focus order follows logical reading order via natural Compose layout.
- Verified non-colour-only status indicators: all MessageKind markers use emoji+text labels.
- Expanded `ConversationRobot.kt` with 8 new intent-level helpers: `goToNextMatch()`, `goToPreviousMatch()`, `assertMatchIndicator()`, `assertMessageOfKindVisible()`, `assertSenderMarkerVisible()`, `assertTurnHeaderVisible(text)`, `assertTagExists()`, `assertContentDescriptionExists()`.
- Created `AccessibilityAndArea8Test.kt` with 16 new tests covering semantic labels, sender/kind markers, turn grouping, match navigation with wrap-around, stable testTag coverage, long response smoke test, non-colour-only indicators, and unsupported event card.
- Total test count: 142 (up from 126), 0 failures.

### Key decisions
- Used `semantics { contentDescription }` rather than separate accessibility labels to keep semantic info co-located with testTags.
- Long-response test verifies no-crash and turn header presence rather than counting all items (LazyColumn virtualises off-screen items).
- Manual-review Area 7 tasks (7.3, 7.4, 7.6–7.13) left unchecked — require HITL verification on each platform.

### Gotchas
- LazyColumn virtualisation means off-screen items are not rendered in UI tests — assertions must target visible items only.
- `hasText` filter on `turn_header` tag doesn't match because the text "Junie Turn" is in a child node — use `assertTagExists("turn_header")` instead.

### Test coverage areas
- Accessibility: content descriptions on all interactive controls and state surfaces (6 tests).
- Human/Junie rendering: sender markers, kind markers, turn grouping (3 tests).
- Match navigation: forward/backward with wrap-around, content descriptions (3 tests).
- Tag coverage: all important controls findable by stable testTag (1 test).
- Long response: smoke test for crash-free rendering (1 test).
- Non-colour-only indicators: error/warning text labels, unsupported event card (2 tests).


## Code quality refactoring — all 10 thermo-nuclear findings
**Date/Time:** 2026-07-13 15:30

### What was shipped
- Fixed all 10 findings from the thermo-nuclear code quality review
- ConversationScreen.kt: 728→343 lines (split into 5 files)
- JunieEvent.kt: 600→21 lines (split into TopLevelEvents.kt, AgentEvents.kt, EventSerializers.kt)
- SessionRepository.kt: 405→168 lines (extracted EventToMessageMapper.kt)
- Serializer dispatch tables replaced with map-based lookups
- FilterCategory enum added to MessageKind, eliminating rotting when-expression
- FatalErrorManager made injectable with per-error tracking
- Unified HumanMessageItem/JunieMessageItem via shared MessageCard
- MessageBody flattened — no nested when-in-when or post-when escape hatch
- Preferences save made atomic with synchronized block

### Key decisions
- Kept looksLikeMarkdown in UI layer (MessageFormatting.kt) rather than moving to mapping time — changing it would alter message kinds in existing test fixtures
- FatalErrorManager kept as global singleton for main.kt compatibility but backed by injectable DefaultFatalErrorReporter
- Turn and groupMessagesIntoTurns moved to domain package since they are domain logic not UI

### Gotchas
- TurnGroupingTest needed import fix after Turn moved from ui to domain package
- EventSerializers map types needed to match covariant DeserializationStrategy — no cast needed

### Test coverage areas
- All 142 existing tests pass unchanged (except one import fix)
- No new tests added — this was a pure refactoring with no behaviour change

## Sprint 3 — UI Polish and Theme Refresh
**Date/Time:** 2026-07-15 07:45

### What was shipped
- **Theme foundation:** `JunieViewerTheme` composable with Light/Dark/System modes, M3 `lightColorScheme`/`darkColorScheme` inspired by LogViewer palettes, 18 semantic `ConversationColors` tokens (humanAccent, junieAccent, thought/tool/code/diff/terminal/error/warning backgrounds/borders/text), `JunieViewerSpacing` (xs–xxl), custom `JunieViewerTypography` with `MonospaceFont` token, all exposed via `CompositionLocal` + accessor object.
- **Persisted theme preference:** `themeMode` added to `AppPreferences` (backwards-compatible String field), Settings dialog radio button selector (test tags `theme_mode_light/dark/system`).
- **Chrome polish:** Removed crowded top bar; replaced with compact `SearchAndFilterChrome` (search field + TextButton session/settings controls + pill-shaped filter chips separated by dividers). Added `SessionContextFooter` — one-line footer with Session id, date, project path spread evenly with ellipsis.
- **Conversation surface redesign:** Asymmetric layout — Human messages 33% width left-aligned, Junie messages 90% width right-aligned. Accent rails using `humanAccent`/`junieAccent`. `Card` with 8dp rounded corners, 1dp elevation + `outlineVariant` border. `MessageKindMarker` composable (coloured dot + clean text label replacing emoji glyphs). Turn headers with `titleMedium` + `semantics { heading() }`.
- **Rich content styling:** All blocks (code, diff, terminal, thought, tool, structured, error/warning, Markdown) restyled with semantic tokens, `MonospaceFont`, 6dp rounded corners, 1dp borders. Emoji labels removed from all block headers and copy button.
- **State polish:** Loading, error, empty, no-results states restyled with theme tokens. `FatalErrorDialog` themed with `errorContainer` colours and test tags.
- **Accessibility:** WCAG AA contrast verified (28 pairs, all ≥4.0:1). Keyboard focus via M3 defaults. Heading semantics on Turn headers. Colour-not-sole-differentiator verified across all elements.
- **Testing:** 5 new themed component tests (light/dark message rendering, dark rich content, dark state surfaces, footer metadata). Total test count increased. `./gradlew :shared:jvmTest` and `./gradlew test` both BUILD SUCCESSFUL.

### Key decisions
- LogViewer accent colours adopted: `#007ACC` (light), `#00A3E0` (dark).
- `FontFamily.Monospace` used (no bundled font) per HITL decision.
- Theme toggle in Settings dialog only (not top bar) per HITL decision.
- ThoughtBlock/ToolCallBlock collapsed by default per HITL decision.
- `terminalCommand` and `diffHunkHeader` added as new semantic tokens per HITL decision.
- HITL feedback during Area 3: removed app title bar, moved Session metadata to footer.
- Junie messages 90% width right-aligned, Human messages 33% left-aligned per HITL feedback.

### Gotchas
- `SyntaxThemes.default(darkMode = false)` in `CodeBlock.kt` remains hardcoded — wiring to ThemeMode deferred (D4).
- `Dark: surfaceVariant/onSurfaceVariant` contrast is 4.0:1 — passes large text AA but borderline for small text; acceptable for muted secondary labels.
- Windows and Linux cross-platform verification pending HITL — only macOS verified locally.
- No configured cyclomatic complexity tool found in project; lightweight manual review performed.

### Test coverage areas
- Theme: `ThemeModeTest`, `ConversationColorsTest`, `JunieViewerSpacingTest`, `JunieViewerThemeTest` (Compose UI).
- Preferences: `PreferencesRepositoryTest` (themeMode persistence).
- ViewModel: `ConversationViewModelTest` (theme action handling).
- Themed components: `AccessibilityAndArea8Test` (5 new tests: light/dark messages, dark rich content, dark states, footer).
- All existing tests continue to pass.

## Sprint 4 — Interaction, Live Tracking, and Event Coverage

**Date/Time:** 2026-07-17 16:28

### What was shipped
- Text selection and partial copy across all Message content with copy buttons on rich blocks.
- Search highlighting with current-match distinction and theme-aware colours.
- Live Session tracking via polling-based file watching with incremental offset parsing.
- Sub-agent representation (`CustomAgentBlockUpdatedEvent` → `MessageKind.SubAgent`).
- Filter coverage audit — all MessageKind values mapped to FilterCategory with AlwaysShow bypass.
- `AgentTaskFailedEvent` support: tolerant nullable model, serializer registration, mapper to `MessageKind.Error`, rendered via `ErrorWarningBlock` with "Task Failed" label.
- `SubagentSpawnedEvent` support: tolerant nullable model (`name`, `task`, `stepId`, `agent: JsonElement?`), registered in serializer, mapped to `MessageKind.SubAgent` with "Sub-agent spawned: {name}" label and truncated task preview.
- Documentation: `docs/HOW_TO_USE.md` created, README updated, TESTING.md updated, RECAP.md updated.

### Key decisions
- Polling-only live tracking (no filesystem watchers) with configurable interval for cross-platform reliability.
- Incremental offset strategy: track byte offset, read only new bytes, buffer partial lines.
- `AgentTaskFailedEvent` uses tolerant nullable fields since no real payload examples exist — `message`, `errorCode`, `taskId`, `stepId`, `details: JsonElement?`.
- Unknown event fallback (`UnknownAgentEvent`/`UnknownJunieEvent`) preserved — new events don't break existing fallback.
- Reused existing `ErrorWarningBlock` for Task Failed rendering rather than creating a new component.
- `SubagentSpawnedEvent` reuses `MessageKind.SubAgent` and truncates task preview at 200 characters.

### Gotchas
- LazyColumn virtualisation means off-screen items are not rendered in UI tests — assertions must target visible items.
- `AgentTaskFailedEvent` has no real payload examples in EVENT_CATALOG.md — model is speculative but tolerant.
- No configured cyclomatic complexity tool in project; manual review performed.
- Syntax highlighting theme wiring deferred (D4 from Sprint 3).

### Test coverage areas
- Parser: `AgentTaskFailedEvent` deserialization (valid, minimal, extra fields, structured details).
- Mapper: `AgentTaskFailedEvent` → `Sender.Junie`, `MessageKind.Error`, content includes "Task Failed".
- UI: Error block rendering for task failed messages via `CollapsibleBlockTest`.
- Unknown fallback: fabricated unknown event still produces `UnknownAgentEvent`.
- Parser: `SubagentSpawnedEvent` deserialization (valid, minimal, extra fields) — 3 tests.
- Mapper: `SubagentSpawnedEvent` → `Sender.Junie`, `MessageKind.SubAgent`, content/truncation — 4 tests.
- All existing tests continue to pass.
- Commands: `./gradlew :shared:jvmTest`, `./gradlew test` — both BUILD SUCCESSFUL.

## Code Quality Refactoring — Thermo-Nuclear Review Fixes

**Date/Time:** 2026-07-17 17:13

### What was shipped
- **B1**: Eliminated 46 duplicated `override val kind` properties across `AgentEvents.kt` and `TopLevelEvents.kt`. Added default implementation `this::class.simpleName ?: "unknown"` to sealed interfaces.
- **B3**: Extracted `themedHighlightSearchMatches()` composable that resolves colours from theme internally, removing ~40 lines of repeated colour-passing across 7 components.
- **H2**: Refactored `parseMarkdownBlocks` from a 64-line while loop into a `MarkdownBlockParser` class with dedicated methods per block type.
- **H3**: Removed default concrete implementations from `ConversationViewModel` constructor — dependencies now injected explicitly at call site (`App.kt`).
- **M1**: Moved `lazyColumnIndexForMessage` from `domain/Turn.kt` to `ui/ConversationListMapper.kt` — it's a UI/presentation concern.
- **M4**: Replaced `else -> null` catch-all in agent event mapping with exhaustive listing of all 18 metadata-only agent events, enabling compiler enforcement.
- **M6**: Extracted `MessageExpansionState` and `rememberMessageExpansionState` helper from `MessageCard` to encapsulate auto-expand-on-search + manual collapse logic.
- **L1**: Added logger to `EventToMessageMapper` and replaced swallowed exceptions with `logger.w()` calls for `askRequest` and `choiceRequest` JSON parsing.

### Key decisions
- Kept registry maps in `EventSerializers.kt` (B2) rather than migrating to `SerializersModule` — the map approach works well and a full migration risks breaking unknown-event fallback behaviour.
- Deferred H1 (JsonElement in domain) — requires typed data classes for `askRequest`/`choiceRequest` which changes the serialization contract.
- Deferred H4 (unstable message IDs) — requires careful migration of ID generation strategy across mapper and live tracker.
- Deferred M2 (FilterCategory/label to UI), M3 (SessionRepository parser reuse), M5 (design tokens), M7 (getMessages waste), L2 (FilterBar DRY), L3 (color mapping) — lower priority, higher risk of test breakage.

### Gotchas
- `UnknownAgentEvent` and `UnknownJunieEvent` must keep their `override val kind` as constructor parameters since they receive the kind from JSON at runtime.
- `MessageExpansionState.userDismissedForce` needs `internal` visibility (not `private`) so `rememberMessageExpansionState` can reset it.
- Test files constructing `ConversationViewModel` needed a 4th `liveSessionTracker` parameter added after removing defaults.

### Test coverage areas
- All existing tests pass: `./gradlew :shared:jvmTest` and `./gradlew test` both BUILD SUCCESSFUL.
- Markdown parser tests continue to pass with refactored `MarkdownBlockParser` class.
- No new tests added — this was a pure refactoring with no behaviour changes.

## Sprint 5 — Toolbar, Menu, and Navigation Controls (Planning)
**Date/Time:** 2026-07-18 09:15

### What was shipped
- Created `docs/tasks/junie-conversation-viewer-tasks-sprint-5-toolbar-menu-and-navigation-controls.md` with 67 trackable tasks.
- Task breakdown follows the exact 11-section structure of Sprint 4.
- Documented 10 implementation areas, 6 HITL review checkpoints, and 12 open questions.

### Key decisions
- Decided to reuse the shared command model pattern from LogViewer to unify menu, toolbar, and shortcut actions.
- Integrated the search field directly into the toolbar to declutter the main conversation surface.
- Deferred "Open Recent", "Reveal in Finder", and Settings dialog to Sprint 6 to maintain focus on the core navigation framework.

### Gotchas
- None.

### Test coverage areas
- Documentation only; no code changes.


## Sprint 5 — Toolbar, Menu, and Navigation Controls
**Date/Time:** 2026-07-20 07:15

### What was shipped
- Application toolbar with 7 command buttons + integrated search field
- Native Compose Desktop MenuBar with 5 menus and keyboard shortcuts
- Manual refresh and auto-refresh toggle with preference persistence
- Sort order (OldestFirst/NewestFirst) with preference persistence
- Global Collapse All / Show All for all collapsible blocks
- Copy/Search integration (selected-text-only global Copy, per-block copy preserved)
- About and How to Use dialogs

### Key decisions
- Copy command is selected-text only — Compose Desktop does not expose selected text to external code; OS-level Cmd+C handles actual copying
- Per-block copy buttons remain separate from global Copy command
- Toolbar uses icons-only with tooltips (LogViewer-inspired 28dp buttons, 18dp icons, 2dp elevation)
- Filter chips remain below toolbar (not moved into toolbar or menu)
- Search field is last item in toolbar, fills remaining width
- Collapse All/Show All affects all collapsible blocks including Text blocks
- Search force-expands matching collapsed blocks even after Collapse All
- Auto-refresh and sort order preferences persisted in AppPreferences JSON
- Keyboard shortcuts follow IntelliJ conventions for Collapse/Show All (Cmd+Shift+−/+)
- Shared ConversationCommand sealed interface maps toolbar, menu, and keyboard actions to single dispatch

### Gotchas
- Compose Desktop MenuBar renders in macOS system menu bar, not in the window frame
- Compose Desktop does not expose selected text from SelectionContainer — no programmatic way to query current selection
- horizontalScroll modifier is incompatible with weight() in Row — toolbar uses fixed buttons + weighted search field instead
- LazyColumn recycling requires hoisted expansion state (in ViewModel) for global Collapse All/Show All to work reliably
- StateFlow updates can emit intermediate states — Turbine tests must consume multiple items

### Test coverage areas
- ConversationCommandTest: 24 tests (command enablement, dispatch, Area 8 copy/search)
- RefreshAndAutoRefreshTest: 13 tests (manual refresh, auto-refresh toggle, preference persistence)
- SortOrderTest: 15 tests (ordering, persistence, filter/search interaction)
- CollapseShowAllTest: 10 tests (global commands, per-block override, search force-expand)
- AccessibilityAndArea8Test: toolbar content descriptions, search navigation
- All existing Sprint 4 tests continue to pass

## Sprint 6, Phase 3 — UI/Compose Code-Quality Refactor
**Date/Time:** 2026-07-22 11:12

### What was shipped
- Extracted a single pure `findCaseInsensitiveMatches(text, query)` in a new `search` package; both `applySearchHighlight` (MarkdownContent.kt) and `highlightSearchMatches` (SearchHighlight.kt) now delegate to it instead of duplicating the scan loop.
- Extracted `Modifier.richContentBox(...)` (RichContentSurface.kt) and moved `RICH_CONTENT_SHAPE`/`RICH_CONTENT_BORDER_WIDTH` out of `CodeBlock.kt` into `ui/theme/RichContentTokens.kt`; updated all 5 hand-rolled call sites (DiffBlock, StructuredOutputBlock, TerminalOutputBlock, ToolCallBlock, CodeBlock) plus `CollapsibleBlock`'s header.
- Moved `MarkdownBlock`/`parseMarkdownBlocks` out of `MarkdownContent.kt` into `com.knowledgespike.junieviewer.markdown`, and `SideBySideDiffParser` out of `ui.components` into `com.knowledgespike.junieviewer.diff` — composables are now thin importers of these pure parsers.
- Resolved the long-standing D4 gotcha: `CodeBlock.kt` no longer hardcodes `SyntaxThemes.default(darkMode = false)` — added `LocalIsDarkTheme`/`JunieViewerTheme.isDark` so syntax highlighting now follows the active app theme.
- `ConversationScreen.kt`: precomputed a single `currentMatchMessageId` per `filteredMessages`/`currentMatchIndex` change (replacing an O(n) `indexOf` per row), and collapsed the duplicated Human/Junie turn dispatch into one `items(...)` block parameterized by a renderer lambda.
- Added `errorBorder`/`warningBorder` tokens to `ConversationColors` (mirroring codeBorder/toolCallBorder) — fixes the invisible border in `ErrorWarningBlock` where border colour equalled background colour.
- `ConversationToolbar.kt`: replaced magic `4.dp` literals with `spacing.sm`, and promoted `20.dp` (divider height) / `14.dp` (clear-icon size) into named private constants.

### Key decisions
- Chose an explicit typed `@Composable` lambda (`val renderer: @Composable (...) -> Unit = { ... }`) over a bare `::HumanMessageItem` / `::JunieMessageItem` function reference for the unified turn renderer — function references to composables produced a `KComposableFunction5` class missing at test runtime.
- Kept `CodeBlock`'s `heightIn(max = ...)` outside of `richContentBox` (chained before it) since the shared modifier owns `fillMaxWidth`/clip/border/background/padding/scroll, not arbitrary size constraints.
- New error/warning border colours chosen to be visually distinct from their matching background (light: red-200/amber-200; dark: red-300/orange-300) rather than reusing existing tokens.

### Gotchas
- Composable function references (`::SomeComposable`) assigned to a `val` and later invoked inside a `LazyColumn` `items` block crash at runtime with `NoClassDefFoundError: androidx/compose/runtime/internal/KComposableFunction5` even though the code compiles cleanly — always wrap in an explicit `@Composable (...) -> Unit` lambda instead.
- `ConversationColorsTest`'s "all 18 semantic tokens" test only asserts a literal list size, not the full token count, so adding new tokens (`errorBorder`/`warningBorder`) doesn't require touching that test.

### Test coverage areas
- New `SearchMatcherTest` (8 tests) covering the extracted `findCaseInsensitiveMatches` pure function: blank/empty inputs, multiple matches, case-insensitivity, non-overlapping adjacency, literal special-character queries.
- New `ConversationColorsTest` cases verifying `errorBorder`/`warningBorder` are visually distinct from their backgrounds in both palettes.
- Existing `MarkdownParserTest` and `SideBySideDiffParserTest` updated to import from the new `markdown`/`diff` packages; behaviour unchanged.
- Full suite: `./gradlew :shared:allTests` — 488 tests, 0 failures. `./gradlew :shared:compileKotlinJvm :desktopApp:compileKotlin` — BUILD SUCCESSFUL.

## Sprint 7 — Area 4: Versioning and Artifact Naming
**Date/Time:** 2026-07-22 17:51

### What was shipped
- Made tag-in-name versioning explicit in `.github/workflows/tag-build.yml`: a "Derive release tag" step (`id: tag`) reads the pushed tag from `github.ref_name` (leading `v` preserved) and exposes it as both a step output (`steps.tag.outputs.tag`) and an environment variable (`TAG`).
- Rewired the installer copy/rename, distributable zip, `.sha256` sidecar, and GitHub Release title/body to consume the derived tag, so every artifact and Release asset follows `JunieConversationViewer-<tag>-<suffix>` (e.g. `JunieConversationViewer-v1.2.0-macos.dmg`).

### Key decisions
- Current Sprint 7 behaviour embeds the Git tag in **artifact and Release names only**; the Compose Desktop / Gradle `packageVersion` remains `1.0.0` and `desktopApp/build.gradle.kts` is unchanged.
- **Deferred — true tag-driven package versioning:** a future enhancement could pass the version from the tag into Gradle (e.g. `./gradlew -PappVersion=<version> ...`) and wire that property into `packageVersion`, so the installer's internal version also tracks the tag. This is intentionally out of scope for Sprint 7.

### Gotchas
- The leading `v` in the tag is deliberately not stripped — it appears verbatim in artifact/Release names.
- Windows steps read the value as `$env:TAG` (PowerShell); Linux/macOS bash steps use `$TAG`; the Release action uses `${{ steps.tag.outputs.tag }}`.

### Test coverage areas
- Workflow-only change: YAML re-validated with PyYAML (parses cleanly, 14 steps). No application code changed; existing test suite unaffected. Full artifact-name verification is deferred to Area 7 (requires a real tag push).

## Sprint 7 — CI, GitHub Automation, and README (Completion)
**Date/Time:** 2026-07-23 06:50

### What was shipped
- `.github/workflows/tag-build.yml` — `Tag Build and Release`, a `v*`-tag-only cross-platform CI/release workflow (JDK 21, `./gradlew test` gate, per-OS installers + zipped distributables + `.sha256` checksums, GitHub Release publishing).
- GitHub-facing `README.md` — overview, badges, install-from-Releases, run/build-from-source, usage overview, shortcuts, sessions/logs paths, troubleshooting, docs links, status/limitations.
- `docs/GITHUB_SETUP.md` — step-by-step operator guide for wiring up CI and publishing releases.
- Supporting doc updates: `docs/HOW_TO_USE.md` (README cross-link), `docs/TESTING.md` (Sprint 7 CI test-execution section), `docs/RECAP.md` (Sprint 7 milestone), and the Sprint 7 task document.

### Key decisions
- **`v*` tag-only workflow** — no PR/`main` CI this sprint; the workflow triggers only on pushed version tags.
- **Release publishing via GitHub Releases** — `softprops/action-gh-release@v2`, gated on `refs/tags/`.
- **Tag embedded in artifact/Release names only** — assets follow `JunieConversationViewer-<tag>-<platform>`; the internal `packageVersion` stays `1.0.0` (no tag-driven package versioning — deferred).
- **Linux Xvfb** — Ubuntu runners run tests and packaging under `xvfb-run` with GL libraries installed.
- **`contents: write` permission** — required so the built-in `GITHUB_TOKEN` can publish Releases; no custom secrets needed.
- **Windows ARM64 uses Microsoft OpenJDK** — the `windows-11-arm` matrix row pins `distribution: microsoft` because Temurin ships no Windows AArch64 JDK 21.
- **Hyphenated tags → prereleases** — e.g. `v1.0.0-rc1` publishes as a prerelease.

### Gotchas
- **ARM runner availability** — `windows-11-arm` and `ubuntu-24.04-arm` runners depend on GitHub hosted-runner availability; the guide notes fallbacks.
- **Unsigned/unnotarized installers** — the `.dmg`/`.msi`/`.deb` are neither code-signed nor notarized and are not published to any package manager; OSes may warn on first launch.
- **Real tag push required** — end-to-end release verification (installer production on every runner, incl. Windows/Linux ARM) has not been exercised; it needs an actual `v*` tag push.
- **No automatic semantic package versioning yet** — the tag affects names only.

### Test/verification status
- `./gradlew :shared:jvmTest` — BUILD SUCCESSFUL.
- `./gradlew test` — BUILD SUCCESSFUL (full suite).
- `./gradlew :desktopApp:packageDistributionForCurrentOS` — BUILD SUCCESSFUL on macOS; produced `desktopApp/build/compose/binaries/main/dmg/com.knowledgespike.junieviewer-1.0.0.dmg` and the app image under `.../main/app/`.
- Workflow validated by static/YAML review (PyYAML parse; trigger, permissions, matrix, step order, checksums, prerelease gating confirmed). Windows/Linux (incl. ARM) packaging on real runners is **not** locally verifiable and remains pending.
- Cyclomatic-complexity check: no complexity tool (e.g. Detekt) is configured in the build; no production code changed this sprint, so no remediation is required.

## Sprint 8 — Area 4: UI Entry Point and Search Results
**Date/Time:** 2026-08-20 08:30

### What was shipped
- Dedicated top-level search toolbar button (`top_level_search_entry`) and desktop menu item (`Search All Sessions…` with `Cmd+Shift+F` / `Ctrl+Shift+F`).
- `TopLevelSearchDialog` rendering all global search states: initial/idle, empty query, loading/running, results list, empty results, and partial-results warning.
- Session-level result rows featuring session identity, match count badges, bounded preview snippets with ellipsis/truncation rules, mouse click and keyboard activation (Enter/Space) dispatching result selection.
- Comprehensive test coverage (`TopLevelSessionSearchUiTest`) and non-regression verification of existing Conversation Search controls.

### Key decisions
- Top-Level Session Search is hosted in a dedicated dialog/panel, keeping it completely separated from current-Session Conversation Search.
- Partial search failures per session are surfaced as prominent warning notices without hiding successful matching session results.
- Result rows display aggregated match counts and deterministic preview snippets.

### Gotchas
- None; Compose UI testing harness (`runConversationUiTest`) cleanly tests dialog visibility, input handling, and state transitions.

### Test coverage areas
- `TopLevelSessionSearchUiTest`: dialog open/close, entry point invocation, partial warning rendering, and Conversation Search independence.
- Full gradle test suite (`./gradlew :shared:jvmTest`, `./gradlew test`): all tests passed successfully.
