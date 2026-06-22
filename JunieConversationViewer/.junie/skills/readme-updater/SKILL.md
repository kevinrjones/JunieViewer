---
name: readme-updater
description: Use this skill at the end of every sprint to ensure the README.md accurately reflects the current state of the application, its features, and usage instructions.
---

# README Updater Skill

## Purpose
To maintain an up-to-date `README.md` that reflects the project's evolution, new features, and changes in setup or usage.

## When to Use
- **End of Sprint**: Mandatory update after completing a sprint.
- **Major Feature**: After implementing a significant new capability.
- **Architectural Change**: If the project structure or build process changes.

## Procedure

### 1. Review Current State
Examine the following to identify what has changed:
- `docs/project_memory.md` (most recent entries).
- `docs/sprints/` (the sprint just completed).
- Project structure (new modules or directories).
- `AppPreferences` or configuration logic.

### 2. Update Sections

#### Features
Update the "Features" list with any new user-facing capabilities. Ensure the description is concise and benefits-oriented.

#### Getting Started / Usage
Update prerequisites, run commands, or configuration steps if they have changed.

#### Project Structure
Update the structure description if new modules or important subdirectories have been added.

### 3. Verify
- Ensure all links in the README are still valid.
- Check that the Gradle commands are correct.
- Verify that the "Prerequisites" section is accurate.

## Example
If a new "Export" feature was added:
- Add to Features: "- **Export Conversation**: Export logs to Markdown or PDF for sharing."
- Add to Usage: "Click the **Export** button in the top bar to save the current view."
