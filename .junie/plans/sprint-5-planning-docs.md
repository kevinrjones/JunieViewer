---
sessionId: session-260718-084659-1dxn
---

# Requirements

### Overview & Goals

Create Sprint 5 planning documentation (sprint document + task breakdown) for adding a full application toolbar and standard application menu to the Junie Conversation Viewer.

**No code implementation** — only two Markdown documents.

### Scope

#### In Scope
- Sprint 5 sprint document at `JunieConversationViewer/docs/sprints/junie-conversation-viewer-sprint-5-toolbar-menu-and-navigation-controls.md`
- Sprint 5 task breakdown at `JunieConversationViewer/docs/tasks/junie-conversation-viewer-tasks-sprint-5-toolbar-menu-and-navigation-controls.md`

#### Out of Scope
- Any code changes
- Modifying existing sprint/task documents (except adding cross-links if clearly appropriate)

### Document Content Requirements

Both documents must:
- Follow the exact structure and style of the Sprint 4 documents
- Use canonical project terms from `UBIQUITOUS-LANGUAGE.md`
- Reference the LogViewer app (`/Users/kevinjones/Dropbox/projects/utilities/LogViewer`) as a visual/interaction guideline
- Cover all 10 task areas specified in the issue (Discovery, Command Model, Toolbar UI, Menu Bar, Refresh/Auto-Refresh, Sort Order, Collapse All/Show All, Copy/Search Integration, Documentation Updates, Testing/Review/Completion)
- Include all 12 open questions with recommendations and HITL markers
- Include HITL review checkpoints throughout

# Technical Design

### Current Implementation

- **Entry point:** `desktopApp/src/main/kotlin/com/knowledgespike/junieviewer/main.kt` — `Window` block with no `MenuBar`
- **No toolbar exists** — the app currently has search/filter chrome but no dedicated toolbar component
- **LogViewer reference app** uses:
  - `FilterBar.kt` as a toolbar with `filterBarIcon()` composable (28dp `IconButton`, 18dp `Icon`, 2dp vertical padding)
  - `Surface` with `elevation = 2.dp`, `MaterialTheme.colors.surface` background
  - `Divider` separators between icon groups
  - `MenuBar` in `Main.kt` with File/Edit/View menus, keyboard shortcuts via `menuShortcutSetForOs()`
  - `AppMenuActionKey` enum mapping menu items to intents

### Key Decisions for Documents

The sprint document will:
1. **Recommend a shared command/action model** — sealed interface `ConversationCommand` mapping toolbar buttons, menu items, and keyboard shortcuts to existing `ConversationAction`/ViewModel functions (inspired by LogViewer's `AppMenuActionKey` pattern)
2. **Recommend toolbar styling** matching LogViewer: ~28dp icon buttons, surface-coloured background, 2dp elevation, divider separators, 8dp horizontal padding
3. **Recommend Compose Desktop `MenuBar`** for native menu support (same API as LogViewer)
4. **Include all 12 open questions** with recommendations and HITL-required markers

### File Structure

Two new files:
- `JunieConversationViewer/docs/sprints/junie-conversation-viewer-sprint-5-toolbar-menu-and-navigation-controls.md`
- `JunieConversationViewer/docs/tasks/junie-conversation-viewer-tasks-sprint-5-toolbar-menu-and-navigation-controls.md`

### Reference Documents to Follow

- Sprint 4 sprint doc: 26 numbered sections (Title through Definition of Done)
- Sprint 4 task doc: 11 sections (Related Sprint through Notes/Decisions Log), with per-task format: checkbox, Description, Source, Dependencies, Likely files/areas, Completion criteria, Testing expectations

# Delivery Steps

### ✓ Step 1: Create Sprint 5 sprint document
The sprint document exists at `JunieConversationViewer/docs/sprints/junie-conversation-viewer-sprint-5-toolbar-menu-and-navigation-controls.md` with all required sections.

- Create the file following Sprint 4's exact section structure (26 sections: Title, Related Documents, Sprint Goal, Current Baseline, Design Findings, Scope, Out of Scope, User Stories, Functional Requirements, Non-Functional Requirements, Design Principles, Proposed Visual System Additions, Theme Architecture Additions, Proposed Changes sections for each feature area, Accessibility, Cross-Platform Considerations, Testing Strategy, Incremental Delivery Plan with 10 parts, Risks and Mitigations, Open Questions with all 12 questions, Definition of Done)
- Document LogViewer toolbar/menu styling findings (28dp icons, surface background, 2dp elevation, divider separators, `filterBarIcon` pattern)
- Define toolbar requirements: Copy, Refresh, Open Session, Auto-Refresh toggle, Sort Order toggle, Collapse All/Show All, Search Messages field
- Define menu requirements: File, Edit, View, Session, Help menus with scoped/deferred items decided
- Recommend shared command model (sealed interface `ConversationCommand` with `AppMenuActionKey`-style mapping)
- Define keyboard shortcut expectations (macOS primary, Windows/Linux noted)
- Include all 12 open questions with recommendations and HITL-required markers
- Use canonical terms from UBIQUITOUS-LANGUAGE.md throughout

### ✓ Step 2: Create Sprint 5 task breakdown document
The task breakdown document exists at `JunieConversationViewer/docs/tasks/junie-conversation-viewer-tasks-sprint-5-toolbar-menu-and-navigation-controls.md` with all required sections and task areas.

- Create the file following Sprint 4 task doc's exact structure (Related Sprint, Related Documents table, Purpose, How to Use, Progress Summary table, Task Status Legend, Implementation Task List, HITL Review Checkpoints, Acceptance Criteria, Deferred/Out-of-Scope Items, Notes/Decisions Log)
- Define tasks for all 10 areas: Discovery (Area 1), Command/Action Model (Area 2), Toolbar UI (Area 3), Menu Bar (Area 4), Refresh/Auto-Refresh (Area 5), Sort Order (Area 6), Collapse All/Show All (Area 7), Copy/Search Integration (Area 8), Documentation Updates (Area 9), Testing/Review/Completion (Area 10)
- Each task follows Sprint 4 per-task format: checkbox `- [ ]`, Description, Source (sprint doc section reference), Dependencies, Likely files/areas, Completion criteria, Testing expectations
- Include HITL review checkpoints at discovery, toolbar visual design, menu layout, sort-order UX, collapse/show-all, and final approval
- All checkboxes unchecked (`- [ ]`), Progress Summary showing 0/N for each area
- Reference sprint document and existing project docs in Related Documents table