# Ubiquitous Language

This document defines the **ubiquitous language** for the Junie Conversation Viewer.
These terms must be used consistently across code, tests, UI copy, sprint documents,
commit messages, and conversations with the HITL (Human In The Loop).

When two terms compete, prefer the **"Use this term"** form and avoid the **"Avoid this term"** form.
When the domain evolves, update this document first, then align code and docs to match.

## How to maintain this document

- Treat this file as the single source of truth for domain vocabulary.
- Add a term the first time a new concept appears in a sprint, discussion, or code change.
- If a code symbol (class, function, test tag) disagrees with a term here, either rename the
  symbol or update this document — never let them drift silently.
- Keep definitions domain-focused; do not couple them to a specific UI widget or library.

---

## Glossary

### Conversation
- **Definition:** The complete, ordered exchange between a single Human and Junie for one Session.
- **Notes / usage:** A Conversation is what the user opens and reads in the viewer. It is composed of Turns, which are composed of Messages derived from Events.
- **Use this term:** "Conversation" for the whole exchange.
- **Avoid this term:** "chat", "log", "transcript" as domain nouns (they are informal synonyms).

### Session
- **Definition:** A single recorded Junie run, persisted on disk as a folder under the Junie home path containing an `events.jsonl` file.
- **Notes / usage:** A Session has an id (folder name) and a last-modified time. One Session produces exactly one Conversation.
- **Use this term:** "Session" for the on-disk unit and its identifier.
- **Avoid this term:** "file", "run log" when you mean the whole Session.

### Event
- **Definition:** A single raw JSON line in a Session's `events.jsonl` file.
- **Notes / usage:** Events are the source data. Not every Event becomes a Message (some are empty or non-visual). Events are parsed and mapped into Messages.
- **Use this term:** "Event" for the raw persisted record.
- **Avoid this term:** "message" for a raw JSONL line — a Message is a derived, display-level concept.

### Message
- **Definition:** A single display unit in the Conversation, derived from one Event, with a Sender, a Message Kind, and content.
- **Notes / usage:** Messages are what the UI renders in the conversation list. A Message always has exactly one Sender and one Message Kind.
- **Use this term:** "Message" for a rendered conversation item.
- **Avoid this term:** "bubble", "row", "cell" (these are UI-widget words, not domain words).

### Human
- **Definition:** The person interacting with Junie; one of the two Senders.
- **Notes / usage:** Human Messages are usually short: prompts, instructions, corrections, confirmations, or questions.
- **Use this term:** "Human".
- **Avoid this term:** "user", "me", "developer" as the Sender label (reserve "user" for the person using the viewer application).

### Junie
- **Definition:** The AI programming assistant; the other Sender in the Conversation.
- **Notes / usage:** Junie Messages are usually longer and richer (Markdown, code, diffs, tool calls, terminal output, plans, summaries, errors).
- **Use this term:** "Junie".
- **Avoid this term:** "assistant", "AI", "bot", "agent" as the Sender label in UI copy.

### Turn
- **Definition:** A contiguous span of the Conversation attributable to one Sender before control passes to the other Sender.
- **Notes / usage:** A single Human Turn (one prompt) often triggers a long Junie Turn made of many Messages (thoughts, tool calls, patches, a final response). Turns help the UI group related Junie output.
- **Use this term:** "Turn" when grouping consecutive same-Sender Messages.
- **Avoid this term:** "round", "step" (ambiguous).

### Response
- **Definition:** Junie's final, user-facing answer Message within a Turn (as opposed to intermediate Thoughts or Tool Calls).
- **Notes / usage:** Derived from a result block. A Turn may contain many intermediate Messages but typically one Response.
- **Use this term:** "Response" for Junie's final answer.
- **Avoid this term:** "reply", "result" (reserve "result" for the raw event field).

### Thought
- **Definition:** An intermediate Junie Message expressing reasoning, not a final answer to the Human.
- **Notes / usage:** Message Kind = Thought. Should be visually de-emphasised relative to a Response and should be filterable.
- **Use this term:** "Thought".
- **Avoid this term:** "reasoning log", "internal note".

### Tool Call
- **Definition:** A Junie Message representing an invocation of a tool, including its name and arguments.
- **Notes / usage:** Message Kind = Tool. Often structured (JSON-like). Distinct from the Terminal Output it may produce.
- **Use this term:** "Tool Call".
- **Avoid this term:** "function call", "action" in UI copy.

### Terminal Output
- **Definition:** A Junie Message containing a shell command and/or its captured output.
- **Notes / usage:** Message Kind = Terminal. Rendered monospaced; the command line is conventionally prefixed with `$`.
- **Use this term:** "Terminal Output".
- **Avoid this term:** "console log", "shell dump".

### Patch
- **Definition:** A Junie Message representing a proposed or applied change set to files, expressed as a Diff.
- **Notes / usage:** Message Kind = Patch. A Patch is the domain event; a Diff is its textual representation/format.
- **Use this term:** "Patch" for the change set.
- **Avoid this term:** using "diff" and "patch" interchangeably (see Diff).

### Diff
- **Definition:** The unified-diff textual format used to display a Patch.
- **Notes / usage:** A Diff is a rendering/format concern; a Patch is the domain concept it represents.
- **Use this term:** "Diff" for the format/rendering; "Patch" for the change itself.
- **Avoid this term:** calling the change set a "Diff".

### Structured Output
- **Definition:** Any machine-oriented, formatted Junie content (e.g. JSON, tables, plans, summaries) intended to be parsed or scanned rather than read as prose.
- **Notes / usage:** Tool Calls and some Responses are Structured Output. The UI may render it with formatting/highlighting.
- **Use this term:** "Structured Output".
- **Avoid this term:** "data", "payload" in UI copy.

### Message Kind
- **Definition:** The classification of a Message that determines its rendering and filterability.
- **Notes / usage:** Current kinds: Text, Thought, Tool, Patch, Terminal. Every Message has exactly one Message Kind. New kinds must be added here first.
- **Use this term:** "Message Kind".
- **Avoid this term:** "type", "category" (ambiguous; "type" collides with programming types).

### Filter
- **Definition:** A user-controlled predicate that shows or hides Messages by Sender and/or Message Kind.
- **Notes / usage:** Filters are additive toggles (Human, Junie, Thoughts, Tools, Patches, Terminal). Filtering never mutates the underlying Conversation.
- **Use this term:** "Filter".
- **Avoid this term:** "sort", "hide list".

### Search Query
- **Definition:** The free text the user enters to match Message content.
- **Notes / usage:** Search is case-insensitive substring matching over Message content and is combined (AND) with active Filters.
- **Use this term:** "Search Query".
- **Avoid this term:** "keyword", "find text" as the domain noun.

### HITL
- **Definition:** Human In The Loop — the person who reviews sprint documents, plans, and deliverables and gives feedback.
- **Notes / usage:** The HITL validates "After" sections and Reviewable Outcomes. May be the same physical person as the Human, but is a distinct role (reviewer, not conversation participant).
- **Use this term:** "HITL".
- **Avoid this term:** "reviewer", "stakeholder" when the HITL role is specifically meant.

### Sprint
- **Definition:** A planned, bounded unit of work captured in a document under `docs/sprints/`.
- **Notes / usage:** Sprints follow the project's sprint-document conventions and finish with documentation and HITL review steps.
- **Use this term:** "Sprint".
- **Avoid this term:** "milestone", "phase" when a Sprint document is meant.

### Reviewable Outcome
- **Definition:** A concrete, observable result of a Sprint part that the HITL can inspect and confirm.
- **Notes / usage:** Every Sprint part must declare at least one Reviewable Outcome, typically phrased in an "After" section ("After this part, the HITL should be able to confirm…").
- **Use this term:** "Reviewable Outcome".
- **Avoid this term:** "deliverable" (broader and vaguer), "done criteria" (reserve for Definition of Done).
