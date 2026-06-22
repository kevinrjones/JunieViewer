# Project Tasks

## Infrastructure
- [x] 1. Initialize project documentation structure (`docs/project_memory.md`, `docs/tasks/junie-conversation-viewer-tasks-sprint-0.md`).
- [x] 2. Acknowledge and implement the requirement to update `docs/project_memory.md` after each task.
- [x] 3. Organize documentation into `docs/sprints` and `docs/tasks` subdirectories.
- [x] 4. Review `AGENTS.md` and update with new documentation structure and skills.
- [x] 5. Generate local skills for `project-memory` and `junie-log-format`.

## Sprint 0 - Walking Skeleton
### 1. Project Setup
- [x] 1.1. Initialize MVI interfaces and base classes.
- [x] 1.2. Set up `ConversationViewModel`.
- [x] 1.3. Create basic `App.kt` structure.

### 2. Data Parsing
- [x] 2.1. Define serialization models for `events.jsonl`.
- [x] 2.2. Implement `JsonlParser` using `kotlinx.serialization`.
- [x] 2.3. Create `SessionRepository` to read from a hardcoded path.

### 3. UI Implementation
- [x] 3.1. Implement `LazyColumn` for message list.
- [x] 3.2. Style Human and Junie messages differently.
- [x] 3.3. Implement "Full Transparency" view (thoughts, tools, results).
- [x] 3.4. Add Search text field with live filtering logic in ViewModel.

### 4. Code & Diffs
- [x] 4.1. Implement basic syntax highlighting for code blocks.
- [x] 4.2. Render `AgentPatchCreatedEvent` as a highlighted diff.
