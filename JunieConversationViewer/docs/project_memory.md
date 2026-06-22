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
- Discovered that `compose-material-icons-extended` was not in the classpath; opted for text-based buttons for now to maintain focus on functionality.
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
