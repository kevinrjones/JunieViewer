# Sprint 1 (Testing) Tasks

## 1. Planning & Setup
- [x] 1.1. Execute `/grill-with-docs` interview session to refine scope.
- [x] 1.2. Finalize Requirements and Technical Design in `docs/sprints/junie-conversation-viewer-sprint-1-testing.md`.
- [x] 1.3. Update this task list with granular subtasks based on interview.

## 2. Infrastructure & Tooling
- [x] 2.1. Add `Turbine` dependency to `libs.versions.toml` and `shared` module.
- [x] 2.2. Configure Compose UI testing for Desktop.
- [x] 2.3. Ensure all tests run consistently via `./gradlew test`.

## 3. Domain & Data Coverage
- [x] 3.1. **JsonlParser**:
    - [x] 3.1.1. Add tests for malformed JSON lines.
    - [x] 3.1.2. Add tests for missing mandatory fields.
    - [x] 3.1.3. Add tests for version/schema mismatches.
- [x] 3.2. **SessionRepository**:
    - [x] 3.2.1. Implement `FakeFileSystem` (switched to temp dirs) for directory scanning tests.
    - [x] 3.2.2. Test `listSessions` with various directory structures.
    - [x] 3.2.3. Test `setSession` logic and path expansion.
- [x] 3.3. **PreferencesRepository**:
    - [x] 3.3.1. Ensure 100% coverage for all preference fields and persistence.

## 4. ViewModel Testing
- [x] 4.1. Exhaustive tests for `ConversationViewModel`:
    - [x] 4.1.1. Test all `ConversationAction` types.
    - [x] 4.1.2. Verify state transitions for search and filtering.
    - [x] 4.1.3. Test error handling and loading states.
    - [x] 4.1.4. Use `Turbine` for Flow verification.

## 5. UI Testing (Robot Pattern)
- [x] 5.1. Implement `ConversationRobot` for `ConversationScreen`.
- [x] 5.2. Create `ConversationScreenTest`:
    - [x] 5.2.1. Test message rendering.
    - [x] 5.2.2. Test search filtering interaction.
    - [x] 5.2.3. Test navigation/settings dialog interaction.

## 6. Documentation & Standards
- [x] 6.1. Create `docs/TESTING.md` documenting the Robot pattern and testing philosophy.
- [x] 6.2. Perform final coverage audit.
- [x] 6.3. Update `README.md` and `project_memory.md`.
