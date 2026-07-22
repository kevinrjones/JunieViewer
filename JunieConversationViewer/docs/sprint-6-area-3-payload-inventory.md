# Sprint 6 — Area 3 Payload-Shape Inventory (Task 3.1)

> **Date:** 2026-07-20
> **Author:** Junie (automated audit)
> **Corpus:** 245 real `~/.junie/sessions/*/events.jsonl` files (~5.6 GB). No `.jsonl` fixtures exist in the repo, so all shapes below come from real logs. Includes the Q8 serializer field-name audit.

---

## 1. Field-by-field shape inventory

`n` = occurrences across the corpus.

### ResultBlockUpdatedEvent — `changes` (n=3161, always present)

Array of file-change objects. Element schema:
`{beforeContent?: {kind: "TextFileContent", text: string}, beforeRelativePath?: string, afterContent?: {kind: string, text: string}, afterRelativePath?: string}` — `before*` absent for created files, `after*` absent for deleted files. Empty array observed 538×.

### AgentPlanUpdatedEvent — `items` (n=6352, always present)

Single stable shape: `[{description: string, status: string}]` (status e.g. `DONE`, `IN_PROGRESS`). No `name` key (unlike ShowPlanProgress items). `plan` was **never** observed.

### AvailablePullRequestsEvent — `pullRequests`, `agent` (n=62698)

- `pullRequests`: always `[]` when present (48534×); absent 14164×. Element shape unobservable.
- `agent`: always present — `{id: string, kind: string, name: string, type?: string}`.

### LlmResponseMetadataEvent — `modelUsage` (n=90032, always present)

`[{model: string, cost: number, inputTokens: number, cacheInputTokens: number, cacheCreateTokens: number, outputTokens: number, time: number}]`. Top-level `model`/`inputTokens`/`outputTokens` were **never** observed — they live inside `modelUsage[]`.

### EnvironmentVariablesUpdatedEvent (n=110254)

- `variables`: **never** present. The real key is **`env`** (present in all occurrences); value is always an array, empty in every sample inspected.

### ViewFilesBlockUpdatedEvent — `files` (n=49066, always present)

`[{relativePath: string, lineFrom?: number, lineTo?: number}]` — line range present in ~half the occurrences.

### ContextWindowReportEvent — `percentage` (n=10876, always present)

Always a number (float), e.g. `5.07025`. `usedTokens`/`maxTokens` never observed.

### FileChangesBlockUpdatedEvent — `changes` (n=25875, always present)

Same element schema as `ResultBlockUpdatedEvent.changes`; never observed empty.

### ShowPlanProgressEvent — `items` (n=253)

- `progress`: **never** present.
- `items`: always — `[{name: string, description: string, status: string}]` (note `name`, unlike `AgentPlanUpdatedEvent.items`).

### NextPromptSuggestionEvent — `suggestion` (n=1409, always present)

Always an **array of objects**: `[{text: string}]` — never a plain string.

### AskAsyncRequestUpdatedEvent — `request` (n=1096, always present)

`{id: string, name: string, question?: string, isRequired: boolean, allowMultiple?: boolean, options?: [{id: string, description: string, title?: string}]}`.

### AuthorizationAvailabilityEvent — `agent` (n=33, always present)

`{id, kind, name, type?}`. Sibling key is `authorized` (32×); `available` never observed.

### AgentStartedEvent — `agent` (n=8, always present)

`{id: string, kind: string, name: string, type?: string}`. `agentId` never observed (id lives inside `agent`). Extra key `continueInMode` seen 2×.

### SuggestPlanEvent — `sections`, `deliveryPlan` (n=55)

- `plan`: **never** present.
- `sections`: always — `[{name: string, content: string}]`.
- `deliveryPlan`: always — `[{name: string, description: string, status: string}]`.

### AskRequestUpdatedEvent — `askRequest` (n=15, always present)

Single shape: `{id: string, question: string}`.

### ChoiceRequestUpdatedEvent — `choiceRequest` (n=368, always present)

Single shape: `{id: string, options: [{id: string, description: string}]}`.

### SubagentSpawnedEvent — `agent` (n=38, always present)

`{id, kind, name, type}` (all strings).

### AgentTaskFailedEvent — `details` (n=2)

`details` **never** present. Only occurrences are minimal: `{"kind":"AgentTaskFailedEvent","timestampMs":…}` — none of the expected fields appear.

### UserPromptEvent — `customAttachments` (n=1683 events; present 160×)

Array of polymorphic objects discriminated by `kind`, e.g. `[{kind: "BashCommandAttachment", mode: "Direct"}]`, `[{kind, plan: {sections: […], deliveryPlan: […]}}]`, `[{kind, diffCommand: {type}, includeInlineCommentToolInstructions: boolean}]`. Genuinely open-ended.

### UserAsyncResponseEvent — `entries` (n=91, always present)

Single shape: `[{question: string, answer: string}]`.

---

## 2. Q8 field-name audit — highlights

- Near-universal optional `agent: {id, kind, name, type?}` key on agent events (tolerated by `ignoreUnknownKeys`).
- **Key drift:** `EnvironmentVariablesUpdatedEvent` uses `env` (never `variables`); `CurrentDirectoryUpdatedEvent` always uses `currentDirectory` (the `@JsonNames` alias from Area 1 is essential; `directory` never observed).
- **Dead expected fields (never observed):** `ShowPlanProgressEvent.progress`, `SuggestPlanEvent.plan`, `AgentPlanUpdatedEvent.plan`, `TipSuggestionCreatedEvent.tip`, `ToolBlockUpdatedEvent.toolCall`, `AuthorizationAvailabilityEvent.available`, `ContextWindowReportEvent.usedTokens/maxTokens`, `LlmResponseMetadataEvent.model/inputTokens/outputTokens`, `AskAsyncRequestUpdatedEvent.requestId/question`, `AgentStartedEvent.agentId`, all of `AgentTaskFailedEvent`'s expected fields.
- **Extra real-world keys (tolerated, not modelled):** `TerminalBlockUpdatedEvent` (`presentableOutput`, `outputDelta`, `outputFile`, `outputLinesCount`, `approvalRequest`, `cancelRequest`, `details`); `ToolBlockUpdatedEvent.output/toolType`; `TestRunBlockUpdatedEvent.result/resultEx`; `McpBlockUpdatedEvent.input/cancelRequest`; `CustomAgentBlockUpdatedEvent.details/model`; `UserPromptEvent.requestId/requiresConfirmation/attachments`.
- `AgentStateUpdatedEvent.blob` is the only field carrying nested JSON-in-a-string (two-level decode); no unknown kind carries `currentDirectory` or `blob`.
- Only genuinely unknown top-level kind observed: `UserMessagesDroppedFromHistory` (1×).

These audit findings are documentation only; behaviour-affecting renames (e.g. mapping `env`) are deliberately **not** made in Area 3 beyond typed payload modelling, to preserve behaviour.
