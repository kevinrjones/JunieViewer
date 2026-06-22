---
sessionId: session-260622-110939-w8wd
---

# Requirements

### Overview & Goals
The goal is to build a Desktop application using Kotlin Multiplatform (KMP) that allows users to view and interrogate Junie conversation logs (sessions). These logs are stored in `~/.junie/sessions/` as `events.jsonl` files. The application will present these sessions as a readable "He-said/She-said" conversation, support searching, and eventually facilitate session analysis by other agents.

### Scope
- **In Scope (Sprint 0):**
    - KMP Desktop application setup.
    - Parsing `events.jsonl` from a specific hardcoded session folder.
    - MVI architecture in the `shared` module.
    - "He-said/She-said" single-stream layout.
    - Full transparency: displaying thoughts, tool calls, terminal output, and final results.
    - Live text search (filtering as you type).
    - Basic syntax highlighting for code blocks and diffs.
- **Out of Scope (Sprint 0):**
    - Session discovery/listing (listing all folders in `~/.junie/sessions/`).
    - Database ingestion (staying file-based for the walking skeleton).
    - Exporting sessions to Markdown or other formats.
    - Multi-session support.

### User Stories
- As a **Developer**, I want to view my past interactions with Junie in a clean UI so that I can easily recall decisions and code changes.
- As a **Developer**, I want to search through my Junie logs for specific keywords or code snippets to find information quickly.
- As a **Developer**, I want to see the "behind the scenes" of Junie's work (thoughts, tool calls) to better understand its reasoning.

### Functional Requirements
- **Conversation Viewer:** Display messages in chronological order.
- **Message Distinction:** Visually separate user prompts from agent responses.
- **Search:** Real-time filtering of the conversation based on user input.
- **Code Rendering:** Syntax highlighting for code snippets and unified diffs.
- **Performance:** Handle large `events.jsonl` files efficiently using lazy loading.

# Technical Design

### Architecture
The project follows a standard KMP structure with MVI (Model-View-Intent) to handle state management.

- **Shared Module:** Contains the core logic, MVI components, data parsing, and the main `App.kt` (Compose UI).
- **Desktop Module:** Minimal entry point for the JVM target.

### Data Model
The `events.jsonl` format consists of polymorphic events.

```kotlin
@Serializable
sealed interface JunieEvent {
    val kind: String
}

@Serializable
data class UserPromptEvent(val prompt: String, val requestId: String) : JunieEvent

@Serializable
data class SessionA2uxEvent(val event: AgentEventWrapper) : JunieEvent

@Serializable
data class AgentEventWrapper(val agentEvent: AgentEvent)

@Serializable
sealed interface AgentEvent {
    val kind: String
}

@Serializable
data class AgentThoughtBlockUpdatedEvent(val text: String) : AgentEvent

@Serializable
data class AgentPatchCreatedEvent(val patch: String) : AgentEvent
// ... other agent events (ToolBlock, TerminalBlock, ResultBlock)
```

### Components
- **`ConversationViewModel`:** Manages the screen state (list of messages, search query) and handles intents (searching).
- **`SessionRepository`:** Responsible for reading the `.jsonl` file and transforming raw events into a domain-friendly `Message` list.
- **`MessageItem`:** Composable that renders a single message, applying different styles based on the sender.
- **`CodeBlock`:** Specialized Composable for rendering code with syntax highlighting.

### File Structure
- `shared/src/commonMain/kotlin/com/knowledgespike/junieviewer/`
    - `domain/`: `Message`, `JunieEvent` models.
    - `data/`: `SessionRepository`, `JsonlParser`.
    - `ui/`: `ConversationViewModel`, `ConversationState`, `ConversationIntent`.
    - `App.kt`: Root Composable and screen layout.

# Sprint 0 Plan

### Sprint 0 Goals
Build a "walking skeleton" that can parse and display a single Junie session on Desktop.

### Tasks
#### 1. Project Setup
- [x] Initialize MVI interfaces and base classes.
- [x] Set up `ConversationViewModel`.
- [x] Create basic `App.kt` structure.

#### 2. Data Parsing
- [x] Define serialization models for `events.jsonl`.
- [x] Implement `JsonlParser` using `kotlinx.serialization`.
- [x] Create `SessionRepository` to read from a hardcoded path.

#### 3. UI Implementation
- [x] Implement `LazyColumn` for message list.
- [x] Style Human and Junie messages differently.
- [x] Implement "Full Transparency" view (thoughts, tools, results).
- [x] Add Search text field with live filtering logic in ViewModel.

#### 4. Code & Diffs
- [x] Implement basic syntax highlighting for code blocks.
- [x] Render `AgentPatchCreatedEvent` as a highlighted diff.

### Acceptance Criteria
- App launches on Desktop.
- A hardcoded session's events are correctly parsed and displayed.
- Human prompts are clearly distinguishable from Junie's responses.
- Search box filters the conversation in real-time.
- Code snippets and diffs are readable and highlighted.

# Delivery Steps

### ✓ Step 1: Project Setup and MVI Skeleton
App skeleton with MVI in `shared` module is ready.

- Create MVI interfaces (`State`, `Intent`, `ViewModel`) in `shared` module.
- Set up a basic `ConversationViewModel` in `shared`.
- Update `App.kt` in `shared` to use the ViewModel and show a placeholder message list.
- Verify the app launches and displays the placeholder on Desktop.

### ✓ Step 2: Data Parsing and Mapping
Data from `events.jsonl` is parsed and mapped to domain models.

- Define Kotlin `data classes` for `events.jsonl` schema using `kotlinx-serialization`.
- Implement a `JsonlParser` in `shared` that can handle the polymorphic event kinds.
- Create a `SessionRepository` in `shared` that reads from a hardcoded session path and returns a list of domain `Message` objects.
- Update `ConversationViewModel` to use the repository and expose the parsed messages.

### ✓ Step 3: UI Implementation and Search
Conversation is visible in a "He-said/She-said" layout with search.

- Implement the conversation UI in `shared/App.kt` using `LazyColumn`.
- Apply distinct styling for Human (`UserPromptEvent`) and Junie (other events) messages.
- Add a Search text field that filters the message list in real-time (Live Search).
- Ensure "Full Transparency" by rendering thoughts, tool calls, and results.

### ✓ Step 4: Code Rendering and Diffs
Code blocks and diffs are rendered with syntax highlighting.

- Integrate a basic syntax highlighting mechanism for code blocks (using a library or custom SpannedString-like logic in Compose).
- Specifically handle `AgentPatchCreatedEvent` to show formatted diffs.
- Verify rendering of complex sessions with many event types.