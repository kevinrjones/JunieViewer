# Junie JSONL Deserialization Investigation

**Date:** 2026-07-11

---

## 1. Problem Summary

The application crashes (logs an error and silently drops events) when reading real Junie `events.jsonl` files because the sealed `JunieEvent` hierarchy only defines 2 of the 6 top-level event kinds, and the sealed `AgentEvent` hierarchy only defines 8 of the 21 nested agent event kinds found in real session data.

The error message:

```
Check if class with serial name 'TaskStartedEvent' exists and serializer is registered
in a corresponding SerializersModule.
```

---

## 2. Evidence from Logs

No `viewer.log` file was found on disk (the app writes to `logs/viewer.log` relative to the working directory, which may not have been created yet). However, the error message in the issue is a standard `kotlinx.serialization` `SerializationException` produced when a sealed class discriminator value has no registered subtype.

Analysis of two real sessions (`session-260709-111457-1utg` with 4,616 lines and `session-260708-151500-1v7e` with 1,212 lines) confirmed the scope of missing event kinds.

---

## 3. Current Parser/Model Summary

### Parser: `JsonlParser.kt`

- Parses each JSONL line via `json.decodeFromString<JunieEvent>(line)`.
- Wraps in `Either.catch` — deserialization failures return `Left(Throwable)` and are logged.
- `Json` config: `ignoreUnknownKeys = true`, `isLenient = true`.

### Repository: `SessionRepository.kt`

- Calls `JsonlParser.parseLine(line).onRight { events.add(it) }` — **silently drops** all `Left` results.
- No count of dropped events is surfaced to the UI.
- The app does **not** crash fatally — it silently loses data.

### Domain model: `JunieEvent.kt`

- `JunieEvent` — sealed interface, discriminator `"kind"`, 2 subtypes.
- `AgentEvent` — sealed interface, discriminator `"kind"`, 8 subtypes.

### Event-to-Message mapping: `SessionRepository.mapEventsToMessages()`

- Maps `UserPromptEvent` → Human Message.
- Maps `SessionA2uxEvent` → Junie Message (only for `ResultBlockUpdatedEvent`, `AgentThoughtBlockUpdatedEvent`, `AgentPatchCreatedEvent`, `ToolBlockUpdatedEvent`, `TerminalBlockUpdatedEvent`).
- Other agent events (`AgentCurrentStatusUpdatedEvent`, `AgentTaskNameUpdatedEvent`, `AgentPlanUpdatedEvent`) are mapped to `null` (ignored).

---

## 4. Event Kind Inventory

### Top-level `kind` values (from real sessions)

| Kind | Count (session 1) | Count (session 2) | Supported? |
|---|---|---|---|
| `SessionA2uxEvent` | 4,443 | 1,192 | ✅ Yes |
| `UserPromptEvent` | 7 | 1 | ✅ Yes |
| `UserMessagesCommittedToHistory` | 152 | 17 | ❌ Missing |
| `TaskStartedEvent` | 7 | 1 | ❌ Missing |
| `TaskState` | 6 | 1 | ❌ Missing |
| `UserAsyncResponseEvent` | 1 | 0 | ❌ Missing |

**4 missing top-level event kinds.**

### Nested `agentEvent.kind` values (from real sessions)

| Kind | Count (session 1) | Count (session 2) | Supported? | UI-relevant? |
|---|---|---|---|---|
| `AvailablePullRequestsEvent` | 2,292 | 965 | ❌ Missing | No (metadata) |
| `AgentCurrentStatusUpdatedEvent` | 1,080 | 118 | ✅ Yes | No (ignored in mapping) |
| `LlmResponseMetadataEvent` | 215 | 27 | ❌ Missing | No (metadata) |
| `CurrentDirectoryUpdatedEvent` | 166 | 19 | ❌ Missing | No (metadata) |
| `EnvironmentVariablesUpdatedEvent` | 166 | 19 | ❌ Missing | No (metadata) |
| `ViewFilesBlockUpdatedEvent` | 109 | 3 | ❌ Missing | Maybe |
| `ToolBlockUpdatedEvent` | 82 | 2 | ✅ Yes | Yes |
| `ContextWindowReportEvent` | 74 | 7 | ❌ Missing | No (metadata) |
| `TerminalBlockUpdatedEvent` | 71 | 16 | ✅ Yes | Yes |
| `FileChangesBlockUpdatedEvent` | 66 | 3 | ❌ Missing | Maybe |
| `TipSuggestionCreatedEvent` | 56 | 4 | ❌ Missing | No |
| `AgentThoughtBlockUpdatedEvent` | 23 | 2 | ✅ Yes | Yes |
| `ResultBlockUpdatedEvent` | 11 | 2 | ✅ Yes | Yes |
| `AgentTaskNameUpdatedEvent` | 7 | 1 | ✅ Yes | No (ignored) |
| `ShowPlanProgressEvent` | 7 | 0 | ❌ Missing | Maybe |
| `AgentPatchCreatedEvent` | 5 | 1 | ✅ Yes | Yes |
| `NextPromptSuggestionEvent` | 5 | 1 | ❌ Missing | No |
| `AskAsyncRequestUpdatedEvent` | 4 | 0 | ❌ Missing | Maybe |
| `AuthorizationAvailabilityEvent` | 2 | 2 | ❌ Missing | No |
| `AgentStartedEvent` | 1 | 0 | ❌ Missing | No |
| `SuggestPlanEvent` | 1 | 0 | ❌ Missing | Maybe |
| `AgentPlanUpdatedEvent` | 0 | 0 | ✅ Yes | No (ignored) |

**13 missing nested agent event kinds.**

### Summary

- **Top-level:** 2 of 6 supported (4 missing).
- **Nested agent:** 8 of 21 supported (13 missing).
- **Total missing:** 17 event kinds.
- **UI-relevant missing:** 0 critical, ~4 potentially useful (ViewFiles, FileChanges, ShowPlanProgress, SuggestPlan, AskAsyncRequest).

---

## 5. Crash Path Analysis

```
events.jsonl line with kind="TaskStartedEvent"
  → JsonlParser.parseLine()
    → json.decodeFromString<JunieEvent>(line)
      → kotlinx.serialization looks up "TaskStartedEvent" in JunieEvent sealed subtypes
      → NOT FOUND → throws SerializationException
    → Either.catch wraps as Left(SerializationException)
    → logger.e logs the error
  → SessionRepository: .onRight { events.add(it) } — Left is silently ignored
  → Event is DROPPED — not added to events list
```

**The same path applies to all 4 missing top-level kinds.** For missing nested agent event kinds, the failure occurs one level deeper (deserializing `AgentEvent` inside `SessionA2uxEvent`), but the entire `SessionA2uxEvent` line is dropped — meaning all data on that line (including the wrapper timestamp) is lost.

### Impact quantification

In the larger session (4,616 lines):
- **173 top-level events dropped** (TaskStartedEvent + TaskState + UserMessagesCommittedToHistory + UserAsyncResponseEvent).
- **~3,363 SessionA2uxEvent lines dropped** due to unknown nested agent events (the `SessionA2uxEvent` wrapper parses, but the nested `AgentEvent` fails).
- **Only ~1,080 SessionA2uxEvent lines survive** (those with known agent event kinds).
- **Total: ~77% of all JSONL lines are silently dropped.**

The app does not crash fatally, but it renders a severely incomplete conversation.

---

## 6. Fix Strategy Options

### Option A: Add all missing serializable event classes

- Add 4 top-level event classes and 13 agent event classes (17 total).
- Each needs `@Serializable`, `@SerialName`, and appropriate fields.
- Fields for many events are unknown without inspecting raw JSON payloads.
- **Will keep breaking** as Junie adds new event kinds in future versions.
- Requires test coverage for each new class.

| Metric | Value |
|---|---|
| Effort | Medium–Large (1–3 days) |
| Risk | High — fragile, breaks on every new Junie event kind |
| Separate sprint? | Not required but recommended |

### Option B: Add a tolerant unknown-event fallback

- Change `JunieEvent` and `AgentEvent` to handle unknown discriminator values gracefully.
- **Approach:** Use a custom `JsonContentPolymorphicSerializer` that checks the `kind` field and falls back to an `UnknownJunieEvent` / `UnknownAgentEvent` class that preserves the raw `JsonObject`.
- Unknown events would not crash the parser and would preserve the raw JSON for future use.
- The UI mapping already ignores non-UI events, so unknown events would naturally be skipped.
- **Data fidelity:** Full — raw JSON is preserved.
- **Future-proof:** New event kinds are automatically handled without code changes.

| Metric | Value |
|---|---|
| Effort | Small–Medium (0.5–1 day) |
| Risk | Low |
| Separate sprint? | No — can be done as a hardening task within the current sprint |

### Option C: Parse JSONL into a looser intermediate model first

- Parse each line as `JsonObject` first, extract `kind` and `timestampMs`, keep raw payload.
- Map only known/UI-relevant events into `Message`.
- Most flexible but requires significant refactoring of `JsonlParser`, `SessionRepository`, and all tests.

| Metric | Value |
|---|---|
| Effort | Large (2–4 days) |
| Risk | Medium — significant refactoring |
| Separate sprint? | Yes |

### Option D: Minimal hotfix to unblock the UI sprint

- Smallest safe change: the current `Either.catch` already prevents fatal crashes.
- Add logging of the count of dropped events so the HITL can see data loss.
- Optionally add `UnknownJunieEvent` and `UnknownAgentEvent` fallback classes (overlaps with Option B).
- The UI continues to work with partial data.

| Metric | Value |
|---|---|
| Effort | Small (< 0.5 day) |
| Risk | Low |
| Separate sprint? | No |
| Technical debt | High — 77% of events silently dropped |

---

## 7. Effort Estimate Summary

| Option | Effort | Risk | Sprint impact |
|---|---|---|---|
| A — Add all event classes | Medium–Large (1–3 days) | High (fragile) | Delays UI sprint |
| B — Unknown-event fallback | Small–Medium (0.5–1 day) | Low | Minimal delay |
| C — Loose intermediate model | Large (2–4 days) | Medium | Requires separate sprint |
| D — Minimal hotfix | Small (< 0.5 day) | Low | No delay |

---

## 8. Sprint Impact Assessment

### Current sprint status

- Areas 1–3 complete (Sprint Alignment, UI Baseline, Asymmetric Layout).
- Areas 4–10 remaining (Rich Content, Search/Filter, States, Accessibility, Testing, HITL Review, Final Completion).

### Impact on remaining areas

- **Area 4 (Rich Content):** Directly affected — rich content rendering needs real event data to validate. With 77% of events dropped, testing against real sessions is unreliable.
- **Area 5 (Search/Filter):** Affected — search/filter over incomplete data gives misleading results.
- **Area 6 (States):** Partially affected — error states may mask data loss.
- **Area 8 (Testing):** Affected — tests against real data will show incomplete conversations.
- **Areas 3, 7, 9, 10:** Not directly blocked.

### Can the UI sprint continue without a fix?

Yes, but with significant limitations. The UI can be developed and tested using synthetic fixture data (which already exists), but HITL validation against real sessions will show incomplete conversations. This is acceptable for layout/styling work but problematic for content rendering validation.

---

## 9. Recommendation

**Option B (tolerant unknown-event fallback) implemented as a hardening task within the current sprint, before starting Area 4.**

Rationale:
1. The app is not fatally crashing — `Either.catch` prevents that — but it silently drops 77% of events.
2. Option B is small (0.5–1 day), low-risk, and future-proof.
3. It unblocks real-session validation for Areas 4–8.
4. It does not require a separate sprint.
5. Option A is fragile and will break again. Option C is too large for a mid-sprint fix. Option D leaves too much technical debt for meaningful HITL review.

---

## 10. Proposed Next Steps

1. **HITL decision:** Approve Option B as a hardening task before Area 4, or choose a different option.
2. **If Option B approved:**
   - Add `UnknownJunieEvent(val kind: String, val raw: JsonObject)` to `JunieEvent` sealed hierarchy.
   - Add `UnknownAgentEvent(val kind: String, val raw: JsonObject)` to `AgentEvent` sealed hierarchy.
   - Implement custom `JsonContentPolymorphicSerializer` for both hierarchies.
   - Update `mapEventsToMessages` to skip unknown events (already the default behaviour for unhandled `when` branches).
   - Add logging: count of known vs unknown events per session load.
   - Add tests for unknown event fallback.
   - Run `./gradlew :shared:jvmTest`.
3. **After Option B:** Resume Area 4 (Rich Content Rendering).
4. **Future:** Consider Option C (loose intermediate model) as a separate sprint if richer event data is needed.

---

## 11. Open Questions for HITL

1. **Approve Option B?** Should the unknown-event fallback be implemented now as a hardening task before Area 4?
2. **Event visibility:** Should unknown events be completely hidden from the UI, or should they appear as a collapsed "Unknown Event" item for debugging purposes?
3. **Data loss logging:** Should the app show a banner or count when events are dropped/unknown, or is console/file logging sufficient?
4. **Real session testing:** Should HITL validation of Areas 4+ use real sessions, or is synthetic fixture data sufficient?
5. **Scope of Option B:** Should the fix also add the 4 missing top-level event kinds (`TaskStartedEvent`, `TaskState`, `UserMessagesCommittedToHistory`, `UserAsyncResponseEvent`) as proper classes since their structure is simple, or treat them all as unknown?
