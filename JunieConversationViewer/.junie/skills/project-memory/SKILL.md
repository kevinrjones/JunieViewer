---
name: project-memory
description: Use when completing a task or sprint to ensure docs/project_memory.md is updated correctly.
---
# Project Memory Skill

Use this skill when completing a task or sprint to ensure `docs/project_memory.md` is updated according to the project's standards.

## Purpose
Maintain a historical record of significant changes, decisions, and outcomes to provide context for future sessions and other agents.

## When to Use
- After completing a major feature or bug fix.
- At the end of every sprint.
- When significant architectural decisions are made.

## Update Procedure

### 1. Identify the Completion Details
Gather the following information:
- **Title**: A clear, concise name for the completed work.
- **Date/Time**: Current timestamp (e.g., `2026-06-22 14:30`).
- **What was shipped**: Bullet points of implemented features or fixes.
- **Key decisions**: Rationale for architecture, libraries, or patterns chosen.
- **Gotchas**: Unexpected challenges, edge cases, or known limitations.
- **Test coverage areas**: Description of what was tested and how.

### 2. Format the Entry
Append the new entry to `docs/project_memory.md` using the following Markdown structure:

```markdown
## [Title]
**Date/Time:** [YYYY-MM-DD HH:MM]

### What was shipped
- [Feature 1]
- [Feature 2]

### Key decisions
- [Decision 1]
- [Decision 2]

### Gotchas
- [Gotcha 1]

### Test coverage areas
- [Area 1]
```

## Example
```markdown
## Setup MVI Architecture
**Date/Time:** 2026-06-22 11:30

### What was shipped
- Initialized `ConversationViewModel`, `ConversationState`, and `ConversationIntent`.
- Integrated ViewModel into `App.kt`.

### Key decisions
- Chose MVI to ensure unidirectional data flow and easier state management in KMP.

### Gotchas
- StateFlow collection in Compose Desktop requires careful handling of lifecycle.

### Test coverage areas
- ViewModel state transitions.
```
