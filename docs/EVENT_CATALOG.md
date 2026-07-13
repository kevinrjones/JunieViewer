# Junie events.jsonl — Complete Event Catalog

> Generated 2026-07-13 by scanning all `~/.junie/sessions/*/events.jsonl` files.
> This document catalogues **every** event kind found, with sample JSON and field descriptions.
> Events modelled in the app are marked ✅.
>
> **Last Updated:** 2026-07-13 — All events implemented. No missing events remain.

---

## Implementation Status Summary

| Category | Total | Implemented ✅ | Missing ❌ |
|----------|-------|---------------|-----------|
| Top-Level Events | 13 | 13 | 0 |
| Nested Agent Events | 30 | 30 | 0 |
| Field Discrepancies | 19 | 19 resolved | 0 |

All known event kinds from real JSONL logs are now parsed by a dedicated model class.
Unknown/future event kinds are handled by `UnknownJunieEvent` (top-level) and `UnknownAgentEvent` (nested).

---

## Top-Level Events

These are the root-level JSON objects in `events.jsonl`. Each has a `"kind"` discriminator field.

### ✅ `UserPromptEvent`
Human prompt submitted to Junie. **UI-visible** as a Human message.

```json
{
  "kind": "UserPromptEvent",
  "prompt": "fix the tests",
  "presentablePrompt": "fix the tests",
  "customAttachments": []
}
```

| Field | Type | Description |
|-------|------|-------------|
| `prompt` | `String` | The raw prompt text (required) |
| `requestId` | `String?` | Request identifier |
| `presentablePrompt` | `String?` | Display-friendly version of the prompt |
| `customAttachments` | `JsonElement?` | Attached files/context |

---

### ✅ `SessionA2uxEvent`
Wrapper for nested agent events. By far the most common event (~1M occurrences).

```json
{
  "kind": "SessionA2uxEvent",
  "event": {
    "state": "...",
    "agentEvent": { "kind": "...", ... }
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `event` | `AgentEventWrapper` | Contains `state` and nested `agentEvent` |
| `timestampMs` | `Long?` | Timestamp |

---

### ✅ `TaskStartedEvent`
Indicates a Junie task has started. **Metadata-only** — parsed but not rendered.

```json
{
  "kind": "TaskStartedEvent",
  "taskId": "abc-123",
  "timestampMs": 1783935114896
}
```

---

### ✅ `TaskState`
Represents a change in Junie task state. **Metadata-only** — parsed but not rendered.

```json
{
  "kind": "TaskState",
  "state": "WORKING"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `state` | `String?` | The new task state |
| `taskId` | `String?` | Task identifier |
| `timestampMs` | `Long?` | Timestamp |

---

### ✅ `UserMessagesCommittedToHistory`
Records that user messages have been committed to conversation history. **Metadata-only** — parsed but not rendered.

```json
{
  "kind": "UserMessagesCommittedToHistory",
  "userMessageIds": ["id1", "id2"],
  "timestampMs": 1783935114896
}
```

| Field | Type | Description |
|-------|------|-------------|
| `requestId` | `String?` | Request identifier |
| `userMessageIds` | `List<String>?` | IDs of committed messages |
| `timestampMs` | `Long?` | Timestamp |

---

### ✅ `UserAsyncResponseEvent`
Async response event from the user (e.g. HITL approval). **Metadata-only** — parsed but not rendered.

```json
{
  "kind": "UserAsyncResponseEvent",
  "entries": [{"requestId": "...", "response": "..."}]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `requestId` | `String?` | Request identifier (legacy format) |
| `response` | `String?` | Response text (legacy format) |
| `entries` | `JsonElement?` | List of response entries (current format) |
| `timestampMs` | `Long?` | Timestamp |

---

### ✅ `SystemMessageEvent` (256 occurrences)
System-level messages displayed to the user (announcements, notifications, warnings). **UI-visible** as a system message card.

```json
{
  "kind": "SystemMessageEvent",
  "text": "Free Google AI — On the House (Limited Time!)",
  "details": "Junie is powered by Google's models, and you can use them for free..."
}
```

| Field | Type | Description |
|-------|------|-------------|
| `text` | `String` | Short message title/summary |
| `details` | `String?` | Extended message body |

---

### ✅ `SendToAgentEvent` (361 occurrences)
Signals that a message/task is being sent to the agent. **Metadata-only** — parsed but not rendered (flow marker).

```json
{
  "kind": "SendToAgentEvent"
}
```

---

### ✅ `CancelAgentEvent` (101 occurrences)
Signals that the user cancelled the agent's current operation. **UI-visible** as a cancellation indicator.

```json
{
  "kind": "CancelAgentEvent"
}
```

---

### ✅ `SessionTitleSetEvent` (5 occurrences)
Sets or updates the session title. **Metadata-only** — parsed but not rendered. TODO: use to update session title in app state.

```json
{
  "kind": "SessionTitleSetEvent",
  "name": "LogViewer",
  "timestampMs": 1783935114896
}
```

| Field | Type | Description |
|-------|------|-------------|
| `name` | `String` | The session title |
| `timestampMs` | `Long?` | Timestamp |

---

### ✅ `SkillsStatusEvent` (13 occurrences)
Reports which agent skills were newly discovered/loaded. **Metadata-only** — parsed but not rendered.

```json
{
  "kind": "SkillsStatusEvent",
  "newSkills": [
    "/Users/kevinjones/.junie/skills/android-data-layer",
    "/Users/kevinjones/.junie/skills/android-testing"
  ]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `newSkills` | `List<String>?` | Paths to newly loaded skill directories |

---

### ✅ `TaskContinueStopped` (11 occurrences)
Indicates that a "continue" operation on a task was stopped. **UI-visible** as a status indicator.

```json
{
  "kind": "TaskContinueStopped"
}
```

---

### ✅ `UserResponseEvent` (110 occurrences)
User's response to a choice or question from the agent. **UI-visible** as a Human message.

```json
{
  "kind": "UserResponseEvent",
  "prompt": "Confirm the plan and implement it",
  "isChoice": true
}
```

| Field | Type | Description |
|-------|------|-------------|
| `prompt` | `String` | The user's response text |
| `isChoice` | `Boolean` | Whether this was a choice selection (vs free-form text) |

---

## Nested Agent Events

These appear inside `SessionA2uxEvent.event.agentEvent`. Each has a `"kind"` discriminator.

### ✅ Existing Agent Events

| Kind | Occurrences | UI Rendering | Description |
|------|-------------|-------------|-------------|
| `AgentThoughtBlockUpdatedEvent` | 7,146 | Thought block | Agent's internal reasoning. Fields: `text`, `stepId` |
| `AgentPatchCreatedEvent` | 1,443 | Diff block | Diff/patch created. Fields: `patch` |
| `ResultBlockUpdatedEvent` | 2,993 | Text message | Final result block. Fields: `result`, `stepId`, `cancelled`, `changes`, `errorCode` |
| `ToolBlockUpdatedEvent` | 41,406 | Tool call block | Tool invocation. Fields: `toolCall`, `stepId`, `text`, `status`, `details` |
| `TerminalBlockUpdatedEvent` | 17,445 | Terminal block | Terminal command execution. Fields: `command`, `output`, `stepId`, `status` |
| `AgentCurrentStatusUpdatedEvent` | 538,833 | Metadata-only | Status update |
| `AgentTaskNameUpdatedEvent` | 1,548 | Metadata-only | Task name update. Fields: `name` |
| `AgentPlanUpdatedEvent` | 6,352 | Metadata-only | Plan update. Fields: `plan`, `items` |
| `AvailablePullRequestsEvent` | 34,667 | Metadata-only | PR metadata. Fields: `pullRequests`, `agent` |
| `LlmResponseMetadataEvent` | 83,994 | Metadata-only | Token/model metadata. Fields: `model`, `inputTokens`, `outputTokens`, `modelUsage` |
| `CurrentDirectoryUpdatedEvent` | 116,465 | Metadata-only | CWD update. Fields: `directory` |
| `EnvironmentVariablesUpdatedEvent` | 104,582 | Metadata-only | Env vars update. Fields: `variables` |
| `ViewFilesBlockUpdatedEvent` | 45,230 | Metadata-only | Files being examined. Fields: `files`, `stepId`, `status` |
| `ContextWindowReportEvent` | 9,163 | Metadata-only | Context window usage. Fields: `usedTokens`, `maxTokens`, `percentage` |
| `FileChangesBlockUpdatedEvent` | 24,152 | Metadata-only | Files modified. Fields: `changes`, `stepId`, `status` |
| `TipSuggestionCreatedEvent` | 14,374 | Metadata-only | Tip for user. Fields: `tip`, `id`, `description` |
| `ShowPlanProgressEvent` | 242 | Metadata-only | Plan progress. Fields: `progress`, `items` |
| `NextPromptSuggestionEvent` | 1,340 | Metadata-only | Suggested next prompt. Fields: `suggestion` |
| `AskAsyncRequestUpdatedEvent` | 908 | Metadata-only | Async approval request. Fields: `requestId`, `question`, `stepId`, `title`, `request`, `status` |
| `AuthorizationAvailabilityEvent` | 20 | Metadata-only | Auth status. Fields: `available`, `agent`, `authorized` |
| `AgentStartedEvent` | 3 | Metadata-only | Agent started. Fields: `agentId`, `agent`, `stepId`, `agentType` |
| `SuggestPlanEvent` | 52 | Metadata-only | Plan suggestion. Fields: `plan`, `sections`, `deliveryPlan`, `readyForReview` |

---

### ✅ `TestRunBlockUpdatedEvent` (5,229 occurrences)
Represents a test execution block — when Junie runs tests. **UI-visible** as a test run card.

```json
{
  "kind": "TestRunBlockUpdatedEvent",
  "stepId": "d97d2e27-166d-49bf-aec1-b7362da0426b",
  "status": "IN_PROGRESS",
  "name": "Run test com.example.MyTest#test something"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `stepId` | `String?` | Unique step identifier |
| `status` | `String?` | One of: `IN_PROGRESS`, `COMPLETED`, `CANCELED`, `REJECTED` |
| `name` | `String?` | Test name/description being run |

---

### ✅ `McpBlockUpdatedEvent` (205 occurrences)
Represents an MCP (Model Context Protocol) tool invocation. **UI-visible** as a tool call block (MCP-labelled).

```json
{
  "kind": "McpBlockUpdatedEvent",
  "stepId": "1d6ba604-ed4f-4653-8b1e-08d85c2e5957",
  "toolName": "Context7/resolve-library-id",
  "status": "IN_PROGRESS",
  "details": "{\n    \"libraryName\": \"Ktor\",\n    \"query\": \"...\" \n}"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `stepId` | `String?` | Unique step identifier |
| `toolName` | `String?` | MCP server/tool name |
| `status` | `String?` | One of: `IN_PROGRESS`, `COMPLETED` |
| `details` | `String?` | JSON string with tool input/output details |

---

### ✅ `CustomAgentBlockUpdatedEvent` (168 occurrences)
Represents a custom subagent being invoked. **UI-visible** as a subagent status card.

```json
{
  "kind": "CustomAgentBlockUpdatedEvent",
  "stepId": "7b4116b2-297b-47e7-b201-a8022175c158",
  "name": "android-qa-agent",
  "status": "STARTED"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `stepId` | `String?` | Unique step identifier |
| `name` | `String?` | Name of the custom agent |
| `status` | `String?` | One of: `STARTED`, `FINISHED` |

---

### ✅ `AgentFailureEvent` (40 occurrences)
Reports an agent-level failure (LLM connection issues, errors). **UI-visible** as an error message.

```json
{
  "kind": "AgentFailureEvent",
  "message": "Junie: Unable to connect to LLM service",
  "errorCode": "ConnectionFailed"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `message` | `String?` | Human-readable error message |
| `errorCode` | `String?` | Machine-readable error code |

---

### ✅ `AgentStateUpdatedEvent` (12,211 occurrences)
Serialized snapshot of the agent's internal state. **Metadata-only** — parsed but not rendered (internal state for session restore).

```json
{
  "kind": "AgentStateUpdatedEvent",
  "blob": "{\"lastAgentState\":{...very large JSON string...}}"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `blob` | `String?` | Serialized JSON string of agent internal state |

---

### ✅ `AskRequestUpdatedEvent` (15 occurrences)
Synchronous question from the agent to the user. **UI-visible** as a question card.

```json
{
  "kind": "AskRequestUpdatedEvent",
  "stepId": "c95ed04a-d1b6-4b89-94e3-4d3ccb8ecff3",
  "title": "Junie asks questions",
  "askRequest": {
    "id": "a2f3c98c-c245-4c64-959f-ed8e6b859528",
    "question": "What should be done differently?"
  },
  "status": "IN_PROGRESS"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `stepId` | `String?` | Unique step identifier |
| `title` | `String?` | Title for the question block |
| `askRequest` | `JsonElement?` | Contains `id` and `question` |
| `status` | `String?` | e.g. `IN_PROGRESS` |

---

### ✅ `ChoiceRequestUpdatedEvent` (368 occurrences)
Presents the user with a set of choices (e.g. plan confirmation). **UI-visible** as a choice card with options.

```json
{
  "kind": "ChoiceRequestUpdatedEvent",
  "stepId": "a3c20353-0cda-42d7-905d-f9769bccba05",
  "title": "How would you like to proceed?",
  "choiceRequest": {
    "id": "dfed906b-d859-4d80-8d29-32b67da79d48",
    "options": [
      {
        "id": "AgreeWithCode",
        "description": "Confirm the plan and implement it"
      },
      {
        "id": "Disagree",
        "description": "Reject the plan and suggest improvements"
      }
    ]
  },
  "status": "IN_PROGRESS"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `stepId` | `String?` | Unique step identifier |
| `title` | `String?` | Question/prompt title |
| `choiceRequest` | `JsonElement?` | Contains `id` and `options` (list of `{id, description}`) |
| `status` | `String?` | e.g. `IN_PROGRESS` |

---

### ✅ `MarkdownBlockUpdatedEvent` (207 occurrences)
A standalone markdown text block from the agent. **UI-visible** as markdown content.

```json
{
  "kind": "MarkdownBlockUpdatedEvent",
  "stepId": "b5a95227-c875-44a4-823a-3b96612cefaa",
  "text": "No changes were undone because there were no edits to revert."
}
```

| Field | Type | Description |
|-------|------|-------------|
| `stepId` | `String?` | Unique step identifier |
| `text` | `String?` | Markdown content |

---

## Unknown Event Fallback

The app preserves tolerant parsing for future/unknown events:

- **`UnknownJunieEvent`**: Any top-level event with an unrecognised `kind` is captured with its raw JSON preserved. Rendered in the UI as an "Unsupported event" indicator.
- **`UnknownAgentEvent`**: Any nested agent event with an unrecognised `kind` is captured with its raw JSON preserved. Rendered in the UI as an "Unsupported event" indicator.

No JSONL line is silently dropped. Unknown events remain visible unless explicitly filtered by the user.

---

## Status Values

Common `status` values observed across block events:

| Event | Status Values |
|-------|--------------|
| `TestRunBlockUpdatedEvent` | `IN_PROGRESS`, `COMPLETED`, `CANCELED`, `REJECTED` |
| `McpBlockUpdatedEvent` | `IN_PROGRESS`, `COMPLETED` |
| `CustomAgentBlockUpdatedEvent` | `STARTED`, `FINISHED` |
| `ToolBlockUpdatedEvent` | `IN_PROGRESS`, `COMPLETED`, `REJECTED` |
| `TerminalBlockUpdatedEvent` | `IN_PROGRESS`, `COMPLETED`, `FAILED`, `CANCELED`, `REJECTED` |
| `ViewFilesBlockUpdatedEvent` | `IN_PROGRESS`, `COMPLETED`, `CANCELED` |
| `FileChangesBlockUpdatedEvent` | `IN_PROGRESS`, `COMPLETED`, `CANCELED` |
