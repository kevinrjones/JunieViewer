# Sprint 4 Area 1 — Discovery and Scope Confirmation Findings

> **Date:** 2026-07-16
> **Author:** Junie (automated discovery)
> **Status:** Awaiting HITL review (task 1.9)

---

## 1. Documentation Baseline

### Canonical Terms for Sprint 4

The following terms from `UBIQUITOUS-LANGUAGE.md` must be used consistently:

| Term | Definition (summary) |
|---|---|
| **Conversation** | The complete ordered exchange for one Session. |
| **Session** | A single recorded Junie run; folder under `~/.junie/sessions/` containing `events.jsonl`. |
| **Event** | A single raw JSON line in `events.jsonl`. |
| **Message** | A display unit derived from one Event, with a Sender, Message Kind, and content. |
| **Human** | The person interacting with Junie (Sender). Avoid "user" as Sender label. |
| **Junie** | The AI assistant (Sender). Avoid "assistant", "AI", "bot", "agent". |
| **Turn** | Contiguous span of Messages from one Sender. |
| **Response** | Junie's final user-facing answer within a Turn. |
| **Thought** | Intermediate Junie reasoning Message. Filterable, visually de-emphasised. |
| **Tool Call** | Junie Message representing a tool invocation. |
| **Terminal Output** | Shell command and/or captured output. |
| **Patch** | A change set to files (domain concept). |
| **Diff** | The unified-diff textual format (rendering concern). |
| **Structured Output** | Machine-oriented formatted content (JSON, tables, plans). |
| **Message Kind** | Classification determining rendering and filterability. |
| **Filter** | User-controlled predicate showing/hiding Messages by Sender/Kind. |
| **Search Query** | Free text for case-insensitive substring matching. |
| **HITL** | Human In The Loop — reviewer of sprint deliverables. |

### Testing Conventions

- **Stack:** JUnit 4, MockK, Turbine (Flow testing), Compose Test Rule, Okio temp dirs.
- **Patterns:** Fakes over mocks for repositories; Robot Pattern for UI tests.
- **Tags:** `testTag("...")` on all interactive/state UI elements.
- **Commands:** `./gradlew :shared:jvmTest` (shared module), `./gradlew test` (full suite).
- **Current count:** 142+ tests, 0 failures as of Sprint 3 completion.

### Prior Decisions Affecting Sprint 4

- **D4 (deferred from Sprint 3):** Syntax highlighting theme wiring — `SyntaxThemes.default(darkMode = false)` in `CodeBlock.kt` remains hardcoded.
- **D5 (deferred):** Advanced syntax highlighting.
- **Sprint 3 HITL decisions:** ThoughtBlock/ToolCallBlock collapsed by default; theme toggle in Settings only; Junie messages 90% width right-aligned, Human 33% left-aligned.
- **CollapsibleBlock:** Shared component used by ThoughtBlock and ToolCallBlock — clickable headers will conflict with `SelectionContainer`.

### Gotchas from project_memory.md / RECAP.md

- `StateFlow` updates can emit intermediate states — Turbine tests must consume multiple items.
- LazyColumn virtualisation means off-screen items are not rendered in UI tests — assertions must target visible items only.
- `compose-material-icons-extended` is now in the classpath (added pre-Sprint 4) — Material Icons can be used instead of text-based buttons.
- Windows/Linux cross-platform verification still pending from Sprint 3.
- No configured cyclomatic complexity tool in project.

---

## 2. Filter Coverage Audit

### MessageKind → FilterCategory → Filter Toggle Mapping

| MessageKind | FilterCategory | Current toggle | Current behaviour | Recommendation |
|---|---|---|---|---|
| Text | Junie | Junie chip | Filtered by Junie toggle (or Human toggle if sender is Human) | No change needed |
| Markdown | Junie | Junie chip | Filtered by Junie toggle | No change needed |
| Thought | Thought | Thoughts chip | Filtered by Thoughts toggle | No change needed |
| Tool | Tool | Tools chip | Filtered by Tools toggle | No change needed |
| Patch | Patch | Patches chip | Filtered by Patches toggle | No change needed |
| Terminal | Terminal | Terminal chip | Filtered by Terminal toggle | No change needed |
| StructuredOutput | Tool | Tools chip | Grouped under Tools toggle | Acceptable — structured output is tool-adjacent |
| Error | AlwaysShow | None | Always visible | No change needed — errors should never be hidden |
| Warning | AlwaysShow | None | Always visible | No change needed |
| Unsupported | AlwaysShow | None | Always visible | No change needed |
| TestRun | Terminal | Terminal chip | Grouped under Terminal toggle | Acceptable — test runs are terminal-adjacent |
| Mcp | Tool | Tools chip | Grouped under Tools toggle | Acceptable — MCP is a tool variant |
| SubAgent | Tool | Tools chip | Grouped under Tools toggle | **Consider dedicated toggle** — sub-agent activity may be worth isolating (Q2) |
| Question | AlwaysShow | None | Always visible | No change needed — questions require attention |
| Choice | AlwaysShow | None | Always visible | No change needed — choices require attention |
| SystemMessage | AlwaysShow | None | Always visible | No change needed |
| Cancelled | AlwaysShow | None | Always visible | No change needed |
| Status | AlwaysShow | None | Always visible | No change needed |

### Summary

- **18 MessageKind values** exist.
- **6 filter toggles** exist: Human, Junie, Thoughts, Tools, Patches, Terminal.
- **8 kinds** are AlwaysShow (bypass all filters).
- **4 kinds** are grouped under existing toggles (StructuredOutput/Mcp/SubAgent → Tool; TestRun → Terminal).
- **No kinds are missing from filters** — all are either explicitly toggled or AlwaysShow.
- **Open question:** Should SubAgent get its own toggle? See Q2.

### Special case: Text kind with Human sender

The `filterMessages()` logic has a special case: when `filterCategory == Junie` but `sender == Human`, it uses the Human toggle instead. This correctly handles `UserResponseEvent` which maps to `MessageKind.Text` with `Sender.Human`.

---

## 3. Text Selection and Copy Behaviour Audit

| Content area | File/component | Current copy support | Current text selection support | Notes/risks |
|---|---|---|---|---|
| Human text messages | `MessageItems.kt` → `ContentRenderer` → `Text()` | None | **No** — no `SelectionContainer` | Straightforward to wrap |
| Junie text / Markdown | `MessageItems.kt` → `ContentRenderer` → `MarkdownContent` or `Text()` | None | **No** — no `SelectionContainer` | Markdown rendering may need special handling |
| Code blocks | `CodeBlockWithCopy.kt` → `CodeBlock.kt` | **Yes** — `CopyButton` copies full block | **No** — no `SelectionContainer` | CopyButton must remain outside SelectionContainer |
| Diff/Patch blocks | `DiffBlock.kt` | **Yes** — `CopyButton` copies full diff | **No** — no `SelectionContainer` | CopyButton must remain outside SelectionContainer |
| Terminal Output | `TerminalOutputBlock.kt` | **Yes** — `CopyButton` copies full output | **No** — no `SelectionContainer` | CopyButton must remain outside SelectionContainer |
| Thought blocks | `ThoughtBlock.kt` → `CollapsibleBlock` | None | **No** — no `SelectionContainer` | **Risk:** `CollapsibleBlock` has clickable header — `SelectionContainer` must wrap only the body, not the header |
| Tool Call blocks | `ToolCallBlock.kt` → `CollapsibleBlock` | **Yes** — `CopyButton` in header trailing | **No** — no `SelectionContainer` | **Risk:** Same `CollapsibleBlock` clickable header conflict |
| Structured Output | `StructuredOutputBlock.kt` | **Yes** — `CopyButton` copies full data | **No** — no `SelectionContainer` | CopyButton must remain outside SelectionContainer |
| Error/Warning blocks | `ErrorWarningBlock.kt` | None | **No** — no `SelectionContainer` | Straightforward to wrap |
| Unsupported event cards | `MessageItems.kt` → `UnsupportedEventCard` | None | **No** — no `SelectionContainer` | Low priority — short text |

### Key Findings

1. **No `SelectionContainer` is used anywhere** in the current codebase. Text selection is completely unsupported.
2. **CopyButton** exists on 5 block types (Code, Diff, Terminal, StructuredOutput, ToolCall) — copies the entire block content.
3. **CollapsibleBlock conflict:** `ThoughtBlock` and `ToolCallBlock` use `CollapsibleBlock` which has a clickable header row. Wrapping the entire block in `SelectionContainer` would cause click/selection conflicts. Solution: wrap only the body content, not the header.
4. **MarkdownContent** uses a custom renderer — `SelectionContainer` may not work with all rendered elements. Needs investigation during implementation.

---

## 4. Search Implementation Audit

### Current Search Flow

```
User types in search_field
  → ConversationAction.OnSearchQueryChange(query)
    → ViewModel._state.update(searchQuery = query, currentMatchIndex = 0 or -1)
    → filterMessages(query) called
      → Iterates all messages
      → Applies filter predicate (kind + sender match)
      → Applies search predicate: messageContentText(content).contains(query, ignoreCase = true)
      → Updates state.filteredMessages
  → UI re-renders with filtered list
```

### Search Query Input

- `OutlinedTextField` in `SearchAndFilterChrome` with `testTag("search_field")`.
- Trailing icon: clear button (×) when query is non-empty.
- Placeholder: "Search Messages..."

### State Updates

- `searchQuery: String` — the current query text.
- `currentMatchIndex: Int` — zero-based index into `filteredMessages`, or -1 if no query.
- `filteredMessages: List<Message>` — messages passing both filter and search predicates.

### Matching Logic

- Case-insensitive substring match via `String.contains(query, ignoreCase = true)`.
- `messageContentText()` extracts plain text from all `MessageContent` variants:
  - `Text.text`, `Code.code`, `Diff.diff`, `Terminal.output`, `Structured.data`.
- Search is combined (AND) with active Filters.

### Match Navigation

- `OnNextMatch` / `OnPreviousMatch` actions cycle `currentMatchIndex` with `mod(count)` wrap-around.
- `LaunchedEffect(state.currentMatchIndex)` scrolls the LazyColumn to the matched message.
- Match indicator: `"${currentMatchIndex + 1} / $matchCount"` displayed when `matchCount > 1`.
- Navigation buttons: `▲` (previous) and `▼` (next) with content descriptions.

### Where Search Highlighting Could Be Inserted (Area 5)

1. **State:** Add `searchQuery` and `currentMatchIndex` (or a per-message match index) to the composable parameters passed down to `MessageBody` and content renderers.
2. **Highlight utility:** Create `highlightSearchMatches(text, query, matchIndex, currentMatchIndex)` → `AnnotatedString` with background spans.
3. **Plain text:** In `ContentRenderer`, replace `Text(text = content.text)` with `Text(text = highlightSearchMatches(...))`.
4. **Code/Diff/Terminal blocks:** These render line-by-line with styled `Text` composables — highlighting would need to be applied per-line within existing rendering logic.
5. **Markdown:** `MarkdownContent` uses a custom renderer — highlighting within rendered Markdown is complex and may need to be deferred or limited to plain-text fallback.
6. **Current match distinction:** Pass a flag or index so the highlight utility can use `currentMatchBackground` vs `searchHighlightBackground` tokens.

### Recommendation for Highlighting State Flow

```
ConversationState
  ├── searchQuery: String
  ├── currentMatchIndex: Int (message-level index)
  └── filteredMessages: List<Message>

MessageBody(message, searchQuery, isCurrentMatch)
  └── ContentRenderer(content, searchQuery, isCurrentMatch)
      └── highlightSearchMatches(text, query, isCurrentMatch) → AnnotatedString
```

The current `currentMatchIndex` tracks which **message** is the current match, not which text occurrence within a message. For Area 5, this is sufficient — the current match message gets `currentMatchBackground`, all other matching messages get `searchHighlightBackground`.

---

## 5. Session Loading and Live Tracking Insertion Points

### Current Session Loading Flow

```
User selects Session (OnSessionSelected action)
  → ViewModel.loadMessages()
    → _state.update(isLoading = true)
    → withContext(ioDispatcher):
        → repository.setSession(sessionId, homePath)
            → Constructs path: ~/.junie/sessions/{id}/events.jsonl
            → Stores as currentSessionPath
        → repository.getMessages()
            → fileSystem.source(path).buffer().use { source → }
            → Line-by-line: source.readUtf8Line()
            → Each line: JsonlParser.parseLine(line) → Either<Error, JunieEvent>
            → Collects events into mutableListOf<JunieEvent>()
            → EventToMessageMapper.mapEventsToMessages(events) → List<Message>
        → repository.getSessionInfo(sessionId, homePath)
    → _state.update(messages, filteredMessages, isLoading = false)
    → filterMessages(searchQuery)
```

### Key Characteristics

- **Full-file read:** Entire `events.jsonl` is read and parsed on every session load.
- **No incremental parsing:** No byte offset or line count is maintained between loads.
- **No live tracking:** No file watching or polling mechanism exists.
- **Okio buffered source:** Uses `okio.buffer()` for efficient I/O.
- **Error handling:** Parse errors are counted but don't stop processing; file-not-found returns empty list; exceptions caught and shown as error state.

### Where a FileWatcher / LiveSessionTracker Could Be Inserted

1. **FileWatcher interface** (new `data/FileWatcher.kt`):
   - Watches `currentSessionPath` for modifications.
   - Emits change events via `Flow<FileChangeEvent>`.

2. **LiveSessionTracker** (new `data/LiveSessionTracker.kt`):
   - Wraps `FileWatcher` + incremental parsing.
   - Maintains byte offset (or line count) to read only new content.
   - Parses new lines via `JsonlParser.parseLine()`.
   - Maps via `EventToMessageMapper`.
   - Emits `Flow<List<Message>>` of new messages.

3. **ViewModel integration:**
   - After initial `loadMessages()`, start collecting from `LiveSessionTracker.newMessages`.
   - Append new messages to `state.messages`.
   - Re-apply filters and search.
   - Cancel collection when session changes.

4. **ConversationScreen:**
   - Auto-scroll to bottom when user is near bottom.
   - Preserve scroll position when user is scrolled up.

### Live Tracking Options and Trade-offs

| Approach | Pros | Cons |
|---|---|---|
| **JVM `WatchService`** | Native OS-level file change notifications; low latency; low CPU | Not available in `commonMain`; unreliable on some platforms (macOS uses polling internally); doesn't detect partial writes |
| **Polling fallback** | Simple; cross-platform; predictable | Higher latency; wastes CPU if interval too short; misses rapid changes if interval too long |
| **Polling-only** | Simplest implementation; fully cross-platform | Same cons as polling fallback; no benefit from OS notifications |
| **Hybrid (WatchService + polling)** | Best of both; polling catches missed events | More complex; need platform abstraction |

**Recommended approach:** Polling-only with configurable interval (e.g., 1–2 seconds). Rationale:
- macOS `WatchService` uses polling internally anyway.
- Simpler implementation, fewer platform-specific edge cases.
- `events.jsonl` files grow incrementally — 1-second polling is sufficient for "near real time".

### Partial Line Buffering

During active Junie runs, `events.jsonl` may have an incomplete final line. The incremental reader must:
- Attempt to parse the last line.
- If parsing fails, buffer it and retry on next poll.
- Only advance the byte offset past successfully parsed lines.

### File Truncation/Deletion

- If file size decreases (truncation): reset offset to 0, re-read entire file.
- If file is deleted: stop tracking, log warning, show status indicator.

---

## 6. Sub-Agent Event Flow Audit

### Current Sub-Agent Representation

**Raw event:** `CustomAgentBlockUpdatedEvent` (nested agent event within `SessionA2uxEvent`).

**Event flow:**
```
events.jsonl line
  → JsonlParser.parseLine()
    → SessionA2uxEvent { event: AgentEventWrapper { agentEvent: CustomAgentBlockUpdatedEvent } }
  → EventToMessageMapper.mapAgentEventToMessage()
    → Message(
        sender = Sender.Junie,
        content = MessageContent.Text("🤖 Subagent: {name} [{status}]"),
        kind = MessageKind.SubAgent
      )
```

**Fields available:**
- `stepId: String?` — unique step identifier.
- `name: String?` — name of the custom agent (e.g., "android-qa-agent").
- `status: String?` — one of: `STARTED`, `FINISHED`.

### Current UI Representation

- Sub-agent messages are rendered as **plain text** via `ContentRenderer` → `Text()` (falls through the `else` branch in `MessageBody`).
- The text includes an emoji prefix: `"🤖 Subagent: {name} [{status}]"`.
- The `MessageKindMarker` shows a tertiary-coloured dot with "SubAgent" label.
- **No distinct visual treatment** beyond the kind marker and emoji in text.

### Current Filterability

- `MessageKind.SubAgent` maps to `FilterCategory.Tool`.
- Sub-agent messages are toggled by the **Tools** filter chip.
- **No dedicated sub-agent filter** exists.

### Risks and Ambiguity

1. **Emoji in text:** Sprint 3 removed emoji from all block headers and labels, but the sub-agent message text still contains `🤖`. This should be removed in Area 3.
2. **No grouping:** Multiple sub-agent events (STARTED → FINISHED) for the same `stepId` appear as separate messages with no visual grouping.
3. **No nested content:** Sub-agent activity (the agent's own thoughts, tool calls, etc.) is not visually nested or indented under the sub-agent header.
4. **168 occurrences** in sample data — significant enough to warrant visual distinction.

### Recommendations for Area 3

- Remove emoji from sub-agent message text.
- Add a distinct visual marker (badge, icon, or indented lane) to distinguish sub-agent messages from regular tool calls.
- Consider grouping STARTED/FINISHED pairs visually.
- Consider whether sub-agent messages should have their own filter toggle (see Q2).

---

## 7. AgentTaskFailedEvent Investigation

### Search Results

- **No real `AgentTaskFailedEvent` payload found** in:
  - `EVENT_CATALOG.md` — not listed among 13 top-level or 30 nested agent events.
  - Sample `events.jsonl` files — not found in any session data.
  - Codebase — no data class, serializer, or test fixture exists.
  - All references are in Sprint 4 planning documents only.

### Similar Existing Events

| Event | Kind | Similarity |
|---|---|---|
| `AgentFailureEvent` | Nested agent event | Reports agent-level failures (LLM connection issues). Has `message: String?` and `errorCode: String?`. Mapped to `MessageKind.Error`. |
| `UnknownAgentEvent` | Fallback | Catches any unrecognised nested agent event kind. Preserves raw JSON. |

### Recommendation: Tolerant Model Approach

Since no real payload exists, use tolerant nullable fields:

```kotlin
@Serializable
data class AgentTaskFailedEvent(
    val message: String? = null,
    val details: JsonElement? = null,
    val taskId: String? = null,
    val errorCode: String? = null,
    val stepId: String? = null
) : AgentEvent {
    override val kind: String get() = "AgentTaskFailedEvent"
}
```

**Rationale:**
- `message` and `errorCode` mirror `AgentFailureEvent` for consistency.
- `details: JsonElement?` captures any unknown structured payload.
- `taskId` and `stepId` are common fields across agent events.
- All fields nullable with defaults — tolerant of any payload shape.

### Where Support Should Be Added (Area 7)

1. **Event data class:** `AgentEvents.kt` — add `AgentTaskFailedEvent` as shown above.
2. **Serializer registration:** `EventSerializers.kt` — add `"AgentTaskFailedEvent" to AgentTaskFailedEvent.serializer()` to `agentEventRegistry`.
3. **Parser tests:** Add deserialization tests for valid, minimal, and extra-fields payloads.
4. **Mapper:** `EventToMessageMapper.kt` — add `is AgentTaskFailedEvent →` branch mapping to `MessageKind.Error` with `Sender.Junie`.
5. **UI rendering:** Will use existing `ErrorWarningBlock` — no new UI component needed. The `MessageBody` dispatch already handles `MessageKind.Error`.

### Current Unknown Event Handling

If `AgentTaskFailedEvent` appeared in a real `events.jsonl` today, it would be caught by `UnknownAgentEventSerializer` and rendered as an "Unsupported event: AgentTaskFailedEvent" card. No data would be lost, but the error details would not be displayed.

---

## 8. Open Questions

| ID | Question | Context | Recommendation | Needs HITL? | HITL Decision |
|---|---|---|---|---|---|
| Q1 | How should sub-agent messages be visually distinguished? | Currently plain text with emoji and tertiary dot marker. Options: badge/label, indented lane, nested grouping, icon. | Propose 2–3 options in Area 3 task 3.2 and let HITL choose. | Yes | **Badge/Label** — use a small "Sub-Agent" label in the message header. |
| Q2 | Should SubAgent get a dedicated filter toggle? | Currently grouped under Tools. 168 occurrences in sample data. | Keep under Tools for now — add dedicated toggle only if HITL requests it. Sub-agent messages are infrequent enough that a separate toggle may add clutter. | Yes | **Keep under Tools** — no dedicated toggle needed. |
| Q3 | Should TestRun get a dedicated filter toggle? | Currently grouped under Terminal. 5,229 occurrences in sample data. | Keep under Terminal — test runs are terminal-adjacent and the grouping is intuitive. | No | N/A (no HITL needed) |
| Q4 | How deep should search highlighting go into rich content? | Plain text is straightforward. Code/diff/terminal have line-by-line rendering with existing styling. Markdown uses a custom renderer. | Highlight plain text and code/terminal blocks. Defer Markdown highlighting to a future sprint if complex. | Yes | **Text + code/terminal** — highlight plain text, code blocks, and terminal output. Defer Markdown highlighting. |
| Q5 | What polling interval for live tracking? | Trade-off between latency and CPU usage. | 1–2 seconds. Configurable via a constant, not user-facing setting. | No | N/A (no HITL needed) |
| Q6 | Should live tracking be opt-in or always-on? | Always-on is simpler UX but uses resources even when not needed. | Always-on when a session is selected. Minimal resource usage with polling-only approach. | Yes | **Always-on** — automatically track the selected session file. |
| Q7 | What happens when the watched file is replaced (not appended)? | Some tools may write a new file and rename. | Detect file size decrease → full re-read. Detect inode change → restart watcher. | No | N/A (no HITL needed) |
| Q8 | What is the real `AgentTaskFailedEvent` payload shape? | No real payload found in any sample data or documentation. | Use tolerant nullable fields. Update model when real payloads are discovered. | No | N/A (no HITL needed) |

---

## 9. Design Recommendations

### Area 2 — Text Selection and Partial Copy

- Wrap message body content (not headers) in `SelectionContainer` at the `MessageBody` or `ContentRenderer` level.
- For `CollapsibleBlock`-based components (ThoughtBlock, ToolCallBlock), wrap only the body slot content, not the clickable header.
- Keep existing `CopyButton` components — they copy the full block and remain useful alongside partial text selection.
- Test `SelectionContainer` interaction with `MarkdownContent` — if conflicts arise, consider wrapping only the plain-text fallback path.
- Verify clipboard output is clean plain text on macOS.

### Area 3 — Sub-Agent and Event Representation

- Remove the `🤖` emoji from sub-agent message text (Sprint 3 removed emoji from all other labels).
- **HITL decision: Badge/Label.** Use a small "Sub-Agent" label in the message header to distinguish sub-agent messages.
- All options must use colour + text/icon (not colour alone) per accessibility requirements.
- Consider grouping STARTED/FINISHED pairs for the same `stepId` into a single collapsible block.

### Area 4 — Filter Coverage and Top Controls

- Current filter coverage is complete — no MessageKind is unreachable.
- No new filter toggles needed. **HITL confirmed:** SubAgent stays under Tools.
- The groupings (SubAgent→Tool, TestRun→Terminal, Mcp→Tool, StructuredOutput→Tool) are confirmed as reasonable.
- Verify all filter labels match `UBIQUITOUS-LANGUAGE.md` terms.

### Area 5 — Search Highlighting

- Add 4 colour tokens to `ConversationColors`: `searchHighlightBackground`, `searchHighlightText`, `currentMatchBackground`, `currentMatchText`.
- Create `highlightSearchMatches(text: String, query: String, isCurrentMatch: Boolean): AnnotatedString` utility.
- Pass `searchQuery` and `isCurrentMatch` down through `MessageBody` → `ContentRenderer` → individual block composables.
- Apply highlighting to: plain text, code blocks, terminal output, structured output, error/warning blocks.
- Defer Markdown highlighting if integration with the custom renderer is complex.
- The current match navigation (message-level index) is sufficient — no need for intra-message match tracking in this sprint.

### Area 6 — Live Session Tracking

- Use polling-only approach with 1–2 second interval.
- Maintain byte offset in `LiveSessionTracker` to read only new content.
- Buffer partial lines (incomplete JSON at EOF) and retry on next poll.
- Detect file truncation (size decrease) → full re-read.
- Detect file deletion → stop tracking, log warning.
- ViewModel subscribes to `Flow<List<Message>>` from tracker; appends to state; re-applies filters/search.
- Auto-scroll to bottom only when user is near bottom (within ~100px).
- Cancel tracking when session changes or is deselected.

### Area 7 — AgentTaskFailedEvent Support

- Add `AgentTaskFailedEvent` data class with tolerant nullable fields (see Section 7).
- Register in `EventSerializers.agentEventRegistry`.
- Map to `Message(kind = MessageKind.Error, sender = Sender.Junie)` in `EventToMessageMapper`.
- Render via existing `ErrorWarningBlock` — no new UI component needed.
- Add parser tests: valid payload, minimal (all nulls), unknown extra fields.
- Verify `UnknownAgentEvent` fallback still works for truly unknown events.

---

## 10. HITL Review Summary

### What Was Audited

- All project documentation (UBIQUITOUS-LANGUAGE, TESTING, project_memory, RECAP, EVENT_CATALOG, JSONL investigation, Sprint 3 docs).
- All 18 `MessageKind` values and their filter mappings.
- Text selection and copy support across all 10 content area types.
- Search implementation: query input, matching, navigation, state management.
- Session loading flow: file discovery, parsing, mapping, state updates.
- Sub-agent event flow: `CustomAgentBlockUpdatedEvent` → `MessageKind.SubAgent`.
- `AgentTaskFailedEvent`: searched all code, docs, and sample data — not found.

### Key Findings

1. **Filter coverage is complete** — all 18 MessageKind values are either explicitly toggled or AlwaysShow. No gaps.
2. **No text selection exists** — `SelectionContainer` is not used anywhere. Only `CopyButton` (full-block copy) exists on 5 block types.
3. **No search highlighting exists** — search filters messages but does not highlight matching text within messages.
4. **Session loading is full-file, one-shot** — no incremental parsing or live tracking infrastructure exists.
5. **Sub-agent messages are plain text** with emoji prefix and no distinct visual treatment beyond the kind marker.
6. **`AgentTaskFailedEvent` does not exist** in any real data — tolerant model approach recommended.
7. **`CollapsibleBlock` clickable headers** will conflict with `SelectionContainer` — must wrap body only.

### HITL Decisions (Resolved 2026-07-16)

All 4 open questions have been answered:

1. **Q1:** Sub-agent visual representation → **Badge/Label** in message header.
2. **Q2:** SubAgent filter toggle → **Keep under Tools** (no dedicated toggle).
3. **Q4:** Search highlighting depth → **Text + code/terminal** (defer Markdown).
4. **Q6:** Live tracking mode → **Always-on** when a session is selected.

### Recommended Next Step

All HITL decisions are resolved. Proceed to Area 2 (Text Selection and Partial Copy). Areas 2–7 can proceed in dependency order as documented in the Sprint 4 task breakdown.
