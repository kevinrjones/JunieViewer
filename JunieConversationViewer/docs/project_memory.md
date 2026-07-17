# Project Memory

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
- Documentation: `docs/HOW_TO_USE.md` created, README updated, TESTING.md updated, RECAP.md updated.

### Key decisions
- Polling-only live tracking (no filesystem watchers) with configurable interval for cross-platform reliability.
- Incremental offset strategy: track byte offset, read only new bytes, buffer partial lines.
- `AgentTaskFailedEvent` uses tolerant nullable fields since no real payload examples exist — `message`, `errorCode`, `taskId`, `stepId`, `details: JsonElement?`.
- Unknown event fallback (`UnknownAgentEvent`/`UnknownJunieEvent`) preserved — new events don't break existing fallback.
- Reused existing `ErrorWarningBlock` for Task Failed rendering rather than creating a new component.

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
- All existing tests continue to pass.
- Commands: `./gradlew :shared:jvmTest`, `./gradlew test` — both BUILD SUCCESSFUL.
