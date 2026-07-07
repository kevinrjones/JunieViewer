---
sessionId: session-260622-163200-test
---

# Requirements

### Overview & Goals
The goal of this sprint is to elevate the project's quality and maintainability by implementing a comprehensive testing suite and establishing a robust "test-first" culture. This will move the codebase from a "walking skeleton" with sparse testing to a professional-grade application with high confidence in its core logic and UI.

### Scope
- **In Scope**:
    - **Full Domain/Data Coverage**: 100% unit test coverage for `JsonlParser`, `SessionRepository`, `PreferencesRepository`, and all domain models.
    - **Edge Case Focus**: Robust handling of malformed JSON, missing fields, and large log files.
    - **ViewModel Testing**: Exhaustive tests for `ConversationViewModel` covering all actions, state transitions, and edge cases using Fakes.
    - **UI Testing**: Implementation of the **Robot Pattern** for the main `ConversationScreen`.
    - **Functional UI Tests**: Verification of message rendering, search filtering, and navigation using `ComposeTestRule`.
    - **Testing Standards**: Documentation of the project's testing philosophy (TDD, Robot Pattern) in `docs/TESTING.md`.
- **Out of Scope**:
    - Integration tests with external network APIs.
    - Performance benchmarking (beyond basic large file stability).
    - Screenshot/Visual regression testing (deferred to prioritize functional robustness).

### Acceptance Criteria
- Unit test coverage for domain and data layers is significantly increased (target: >90%).
- `ConversationViewModel` has tests for every possible `ConversationAction`.
- At least one major UI flow (e.g., "Load session and filter messages") is covered by a Robot-based UI test.
- All tests pass in a single command (e.g., `./gradlew allTests`).
- A `docs/TESTING.md` file exists, documenting the Robot pattern and testing stack.

# Technical Design

### Current Implementation
- **Unit Tests**: Exist for `JsonlParser`, `PreferencesRepository`, and basic `ConversationViewModel` logic.
- **Testing Stack**: JUnit 5, MockK, Strikt, Kotlinx-Coroutines-Test.
- **Missing**: UI tests, integration tests for complex repository discovery, and coverage for edge cases in parsing.

### Key Decisions
- **Testing Stack Extension**:
    - **Turbine**: For clean testing of `StateFlow` and `SharedFlow` in ViewModels.
    - **Compose Test Rule**: For functional UI testing on Desktop.
- **Robot Pattern**: We will implement a `ConversationRobot` to encapsulate UI interactions, separating *what* we test from *how* we interact with Compose.
- **Fakes over Mocks**: We will prefer using Fake repositories (like the existing one in `ConversationViewModelTest`) for ViewModel tests to ensure they are decoupled from repository implementation details.
- **Okio FileSystem Mocking**: Use `FakeFileSystem` from Okio for testing file-based logic in repositories.

### Proposed Changes
- **Domain/Data**:
    - `JsonlParserTest`: Add tests for malformed JSON, missing fields, and version mismatches.
    - `SessionRepositoryTest`: Add tests for `listSessions` (directory scanning) and `setSession` logic using `FakeFileSystem`.
- **UI**:
    - `shared/src/commonTest/kotlin/com/knowledgespike/junieviewer/ui/ConversationRobot.kt`: New Robot class.
    - `shared/src/commonTest/kotlin/com/knowledgespike/junieviewer/ui/ConversationScreenTest.kt`: Functional UI tests using the Robot.
- **Infrastructure**:
    - Add `Turbine` for Flow testing.
    - Configure `ComposeTestRule` for Desktop.

### Architecture Diagram
```mermaid
graph LR
    Tests[JUnit Tests] --> VMTests[ViewModel Tests]
    Tests --> RepoTests[Repository Tests]
    Tests --> UITests[Compose UI Tests]
    
    VMTests --> Turbine[Turbine (Flow)]
    VMTests --> Fakes[Fake Repositories]
    
    RepoTests --> Okio[Okio FakeFileSystem]
    
    UITests --> Robot[Robot Pattern]
    Robot --> ComposeTest[ComposeTestRule]
```

# Testing

### Validation Approach
Verification will be performed by running the entire test suite and verifying that coverage reports reflect the target improvements.

### Key Scenarios
1. **Repository Discovery**: Verify that `listSessions` correctly identifies session folders in various directory structures.
2. **Real-time Filtering**: Verify that searching and metadata toggling (Human/Junie/Tools) update the UI state correctly.
3. **UI Robustness**: Use the Robot to simulate a user loading a session, searching, and opening settings without crashes.
4. **Error Handling**: Verify that malformed log lines don't crash the parser and are reported gracefully in the UI.

# Delivery Steps

### * Step 1: Create initial Sprint 1 (Testing) documents
The initial documents for Sprint 1 will be created to serve as a baseline for the upcoming interview.

- Create `docs/sprints/junie-conversation-viewer-sprint-1-testing.md` with Requirements and Technical Design drafts.
- Create `docs/tasks/junie-conversation-viewer-tasks-sprint-1-testing.md` with a hierarchical task list.
- Rename the existing `docs/sprints/sprint-1-session-management-and-search.md` to indicate it was a previous phase or incorporate it as a completed precursor.

###   Step 2: Execute /grill-with-docs interview session
A structured interview using the `/grill-with-docs` skill will be conducted to refine the sprint's scope, goals, and acceptance criteria.

- Discuss specific testing scenarios for domain and data layers.
- Define the boundaries for UI testing and the specific screens to be covered by the Robot pattern.
- Align on the strategy for screenshot testing vs. functional UI assertions.
- Propose and agree on a "Test-First" workflow to be used going forward.

###   Step 3: Finalize Sprint 1 documentation based on interview feedback
The Sprint 1 documents will be updated with the decisions and details gathered during the interview.

- Update the Requirements tab with finalized scope and acceptance criteria.
- Refine the Technical Design with specific class names, test patterns, and tool configurations.
- Update the Task list with granular subtasks based on the agreed-upon scope.
