---
name: junie-log-format
description: Use when parsing or analyzing Junie events.jsonl files.
---
# Junie Log Format Skill

Use this skill when parsing or analyzing Junie `events.jsonl` files.

## Overview
Junie stores session logs in `~/.junie/sessions/[SESSION_ID]/events.jsonl`. Each line is a JSON object representing a polymorphic event.

## Event Hierarchy

### Top-level Event (`JunieEvent`)
- `kind`: Discriminator string.

#### Types:
- `UserPromptEvent`:
    - `prompt`: The text entered by the user.
    - `requestId`: Unique ID for the turn.
- `SessionA2uxEvent`:
    - `event`: Wrapper for agent-side events.

### Agent Events (`AgentEvent`)
Contained within `SessionA2uxEvent` -> `event` -> `agentEvent`.

#### Types:
- `AgentThoughtBlockUpdatedEvent`:
    - `text`: Current text of the agent's thought process.
- `AgentPatchCreatedEvent`:
    - `patch`: A unified diff representing code changes.
- `AgentToolBlockStartedEvent`:
    - `toolName`: Name of the tool being called.
    - `arguments`: JSON string of arguments.
- `AgentToolBlockResultEvent`:
    - `result`: The output of the tool call.

## Parsing Recommendations
- Use `kotlinx.serialization` with a `sealed interface` hierarchy.
- Use `Json { ignoreUnknownKeys = true }` to maintain compatibility with new event types.
- Events are chronological; use a `LazyColumn` for rendering to handle potentially large logs.

## Domain Mapping
Transform raw events into a higher-level `Message` model for the UI:
- `UserPromptEvent` -> `Message.Human`
- Agent events (`Thought`, `Tool`, `Patch`) -> `Message.Junie` (often aggregated until a final response is reached).
