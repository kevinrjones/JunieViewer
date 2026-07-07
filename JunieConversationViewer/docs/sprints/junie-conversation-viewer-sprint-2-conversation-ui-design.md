---
sprint: 2
name: Conversation UI Design
status: reviewed
---

# 1. Title

Sprint 2 — Conversation UI Design: The First Dedicated Conversation UI

# 2. Sprint Goal

Design and plan a cross-platform desktop UI for viewing Junie Conversation data on
**macOS**, **Windows**, and **Linux**.

The UI must make an inherently **asymmetric** Conversation — short Human Messages and long,
rich Junie Messages — easy to **read, scan, search, filter, and verify**. This sprint produces
a high-quality design and delivery plan; it does **not** implement application code.

# 3. Background / Context

The Junie Conversation Viewer already has a working "walking skeleton" (Sprint 0) and a mature
testing foundation (Sprint 1 — Testing):

- A Compose Multiplatform desktop app with an MVI `ConversationViewModel`.
- A `SessionRepository` that reads `events.jsonl`, parses Events, and maps them to Messages.
- Real-time Search and Filtering by Sender and Message Kind (Text, Thought, Tool, Patch, Terminal).
- A Robot-pattern UI test harness and `docs/TESTING.md`.

The current UI is a single "he-said/she-said" stream that was adequate for a skeleton but was
never explicitly *designed* for the asymmetry of the data. This sprint defines that first
dedicated Conversation UI so a HITL can read and verify long Junie Turns without losing context.

Key reference documents:
- `docs/RECAP.md` — chronological project history.
- `docs/project_memory.md` — decisions, gotchas, and shipped work.
- `docs/TESTING.md` — testing stack and Robot pattern.
- `docs/UBIQUITOUS-LANGUAGE.md` — canonical domain terms (see next section).

# 4. Ubiquitous Language References

This sprint uses the canonical terms defined in
[`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md). All UI copy, code, tests, and
review discussion must use these terms. The most load-bearing terms for this sprint are:

- **Conversation**, **Session**, **Turn**, **Message**, **Message Kind**
- **Human**, **Junie**, **Response**, **Thought**, **Tool Call**, **Terminal Output**
- **Patch** vs **Diff** (the change set vs its textual format)
- **Structured Output**, **Filter**, **Search Query**
- **HITL**, **Reviewable Outcome**

Any new term introduced during this sprint must be added to `docs/UBIQUITOUS-LANGUAGE.md`
**before** it appears in code or UI copy.

# 5. Scope

**In scope (this sprint — design and planning only):**

- A documented layout design for the Conversation view that distinguishes Human and Junie Messages.
- A design for grouping consecutive Junie Messages into a readable Turn.
- Rendering strategy for each current Message Kind: Text, Thought, Tool Call, Terminal Output, Patch/Diff.
- A Markdown rendering strategy (headings, lists, inline code, fenced code blocks) for Junie Text.
- Navigation and review design: scrolling, Search, Filtering, and jumping between matches / notable Messages.
- Cross-platform desktop design requirements (window sizing, fonts, scroll, clipboard, shortcuts, file paths).
- Accessibility requirements.
- A Compose Multiplatform desktop testing strategy (semantic tags, Robot helpers, representative-content tests).
- Documentation expectations, including how the ubiquitous language is maintained.

**Out of scope (this sprint):** see section 6. Application code is explicitly out of scope.

# 6. Out of Scope

- Implementing any production Compose UI or ViewModel code (this is a design/planning sprint).
- Persisting or exporting Conversations (Markdown/HTML export).
- Multi-Session comparison or cross-Session search.
- Editing, annotating, or replaying Conversations.
- Network/streaming/live-tailing of an in-progress Session.
- Full-fidelity language-aware syntax highlighting beyond what already exists.
- Theming/skinning beyond a single light and dark scheme.
- Rich rendering of Structured Output beyond code/JSON formatting (deferred; see section 10).

# 7. User Stories

- As a **HITL**, I want Human Messages and Junie Messages to be visually distinct so I can tell at a glance who "spoke".
- As a **HITL**, I want short Human prompts to not dominate the screen so the Conversation reads naturally.
- As a **HITL**, I want long Junie Responses to stay readable so I can review reasoning and results without fatigue.
- As a **HITL**, I want Junie Thoughts, Tool Calls, and Terminal Output visually separated from the final Response so I can skim or dive in.
- As a **HITL**, I want a Patch/Diff to render clearly so I can verify what code changed.
- As a **HITL**, I want to Search and Filter the Conversation and jump between matches so I can find things fast.
- As a **HITL**, I want to stay oriented inside a very long Junie Turn so I don't lose my place.
- As a **HITL** on macOS, Windows, or Linux, I want native-feeling window sizing, scrolling, copy, and shortcuts.
- As a **HITL**, I want to copy code, a Diff, or terminal text cleanly so I can reuse it elsewhere.

# 8. UX Principles

1. **Asymmetry-aware:** the layout is designed around short Human input and long Junie output, not symmetric chat bubbles.
2. **Human never dominates:** Human Messages are compact and clearly anchored; screen space favours Junie content.
3. **Scannability first:** a HITL can skim a long Conversation and locate Turns, Responses, code, and errors quickly.
4. **Verifiability:** code, Diffs, and Terminal Output are rendered so they can be trusted and copied accurately.
5. **Context preservation:** the HITL can always tell where they are (which Turn, which Session, how far through).
6. **Progressive disclosure:** intermediate Junie output (Thoughts, Tool Calls) can be de-emphasised or collapsed.
7. **Platform-neutral, platform-respectful:** one visual design, but honouring per-platform conventions where they matter.
8. **Consistent language:** every visible label uses the ubiquitous language.

# 9. UI Design Requirements

- The Conversation view is a single vertically scrolling stream in chronological Message order.
- Human Messages are visually anchored (e.g. distinct alignment/accent) and kept compact.
- Junie Messages use the full readable content width and support long-form content.
- Consecutive Junie Messages within one Turn are visually grouped (e.g. shared Turn container / header).
- Each Message clearly signals its Message Kind (Text, Thought, Tool Call, Terminal Output, Patch).
- Message order must remain unambiguous at all times, including while Filters are active.
- Session context (selected Session id, and timestamp if available) is visible without scrolling to the top.
- A persistent chrome hosts the Session selector, Search field, and Filter toggles.
- A single light and dark scheme is defined using theme tokens (no hardcoded colours or `dp`/`sp` literals).

# 10. Conversation Rendering Requirements

For each content type, this sprint defines the rendering approach and whether it is **included now**
or **deferred**.

| Content | Included now | Rendering approach |
|---|---|---|
| Plain text | Yes | Readable body typography, wrapped, selectable. |
| Markdown-like content | Yes (core subset) | Headings, bold/italic, lists, inline code, links-as-text; complex tables deferred. |
| Fenced code blocks | Yes | Monospace block with existing highlighting, horizontal scroll, copy affordance. |
| Code Diffs / Patches | Yes | Unified-Diff styling (added/removed line emphasis), copy affordance. |
| Terminal Output | Yes | Monospace block, `$`-prefixed command line, preserved whitespace, copy affordance. |
| Tool Calls | Yes | Rendered as Structured Output (JSON-style formatting); collapsible header with tool name. |
| Structured Output (general) | Partial | JSON/code formatting now; rich tables/plans rendering deferred. |
| Errors and warnings | Yes | Visually distinct (accent + icon/label), never silently blended into plain text. |
| Plans / summaries | Deferred | Rendered as Markdown for now; dedicated affordances deferred to a later sprint. |

Rendering rules:
- Long content must wrap or scroll without breaking Message order or Turn grouping.
- Thoughts are de-emphasised relative to a Response and can be collapsed.
- Every code/Diff/Terminal block exposes a reliable copy action (see Cross-Platform, section 11).
- Deferred items must remain readable as plain/Markdown text — nothing becomes invisible.

# 11. Cross-Platform Desktop Requirements

Design must account for macOS, Windows, and Linux:

- **Window sizing:** sensible default size and minimum size; remembers last size/position where the platform allows.
- **Font rendering:** use platform default UI and monospace fonts via theme tokens; verify legibility on all three OSes.
- **Scroll behaviour:** smooth vertical scrolling with trackpad, mouse wheel, and scrollbar; respect platform scroll direction.
- **Clipboard / copy:** copying text, code, a Diff, or Terminal Output yields clean, unstyled plain text on every OS.
- **Keyboard shortcuts:** define cross-platform shortcuts using the platform-appropriate modifier (Cmd on macOS, Ctrl on Windows/Linux) for at least: focus Search, next/previous match, clear Search.
- **File path display:** show Session and file paths in the platform-native style; do not mangle `~`, drive letters, or separators.
- **Platform-neutral visual design:** one coherent visual language that does not look broken or alien on any OS.

# 12. Accessibility Requirements

- All interactive controls and important Messages expose semantic labels (also used as test tags).
- Colour is never the only signal for Sender, Message Kind, or error state (pair with label/icon/shape).
- Text is selectable and respects a readable minimum size and line length; layout tolerates larger font scales.
- Full keyboard operability: Search, Filters, and match navigation are reachable and operable without a mouse.
- Sufficient contrast in both light and dark schemes.
- Focus order follows reading order (top to bottom, chronological).

# 13. Testing Strategy

Grounded in `docs/TESTING.md` (JUnit, Turbine, Compose Test Rule, Robot pattern, semantic test tags).

- **Semantic tags:** every important element (Message container, Sender marker, Message-Kind marker, Search field, Filter toggle, match indicator) gets a stable `testTag` that doubles as an accessibility label.
- **Robot helpers:** extend `ConversationRobot` with intent-level helpers (e.g. select Session, type Search Query, toggle a Filter, go to next match, assert a Message of a given Kind is visible).
- **Search / Filter tests:** verify Search + Filter combine (AND), that clearing restores the full Conversation, and that Message order is preserved.
- **Representative content tests:** a fixture Session exercising every current Message Kind (Human Text, Junie Text/Markdown, fenced code, Patch/Diff, Terminal Output, Tool Call, error) renders without crashing and with the correct Kind markers.
- **Long-response checks:** an automated smoke test for a very long Junie Turn plus a manual/visual check that scrolling and Turn grouping hold up.
- **HITL visual review:** each Sprint part's "After" section is validated by the HITL against its Reviewable Outcome.
- **ViewModel tests:** any new navigation/state (e.g. current match index) is covered with Turbine, preferring Fakes over Mocks.

# 14. Risks and Open Questions

Risks:
- **R1 — Turn grouping ambiguity:** Events may not cleanly delimit Turns; grouping heuristics could mis-group Messages.
- **R2 — Long-Turn performance:** very long Junie Turns may stress the lazy list and scrolling.
- **R3 — Cross-platform copy fidelity:** styled content copying as rich text instead of clean plain text.
- **R4 — Markdown scope creep:** "Markdown-like" can expand indefinitely; must hold the line on the core subset.
- **R5 — Scope size:** the UI ambition may exceed one sprint if rendering fidelity is over-specified.

Open questions (for the HITL — see section 16):
- **Q1:** Do Timestamps exist reliably per Message, and should they be shown inline, on hover, or only per Turn?
- **Q2:** Should Thoughts and Tool Calls be collapsed by default, or expanded with an option to collapse?
- **Q3:** Is match-to-match navigation (next/previous) required this sprint, or is Filter + Search enough?
- **Q4:** Is a Turn-level outline / jump-to-Turn navigator in scope now, or deferred?
- **Q5:** Is the current syntax highlighting sufficient, or is language-aware highlighting expected?

# 15. Delivery Plan / Sprint Parts

Each part is design/documentation only and has an explicit, HITL-verifiable "After" section.

## Part A — Domain & Ubiquitous Language alignment
Confirm the domain vocabulary and align existing code/UI copy terminology with `docs/UBIQUITOUS-LANGUAGE.md`.

- Finalise `docs/UBIQUITOUS-LANGUAGE.md`.
- Note any current code symbols that disagree with the language (as follow-up, not fixed here).

**After running this part of the sprint, the HITL should be able to** open `docs/UBIQUITOUS-LANGUAGE.md`
and confirm that every term used in this sprint document is defined, unambiguous, and has
"Use this term" / "Avoid this term" guidance where useful.

## Part B — Conversation layout wireframe
Produce a documented layout proposal (annotated wireframe / description) for the Conversation view.

- Show Human vs Junie distinction, compact Human Messages, full-width Junie content, and Turn grouping.
- Show the persistent chrome (Session selector, Search, Filters) and where Session context appears.

**After running this part of the sprint, the HITL should be able to** open the documented layout proposal
and confirm that Human Messages are visually distinct from Junie Responses, that long Junie Responses
are readable, and that code/Diff content has an obvious rendering strategy.

## Part C — Message Kind rendering specification
Specify how each Message Kind renders, per the table in section 10.

- Define styling for Text, Thought, Tool Call, Terminal Output, Patch/Diff, and error/warning.
- Explicitly mark what is included now vs deferred.

**After reviewing this part of the sprint, the HITL should be able to confirm** that each current
Message Kind has a defined rendering, that errors/warnings are visually distinct, and that deferred
rendering is clearly listed and still readable as text.

## Part D — Navigation & review design
Specify scrolling, Search, Filtering, match navigation, and long-Turn orientation.

- Define how Search + Filters combine, how matches are indicated, and how the HITL stays oriented.

**After running this part of the sprint, you should be able to** read a description of how a HITL
scrolls a long Conversation, runs a Search, applies Filters, moves between notable Messages, and
always knows where they are in the current Turn.

## Part E — Cross-platform desktop specification
Document window, font, scroll, clipboard, shortcut, and file-path behaviour for macOS/Windows/Linux.

**After reviewing this part of the sprint, the HITL should be able to confirm** that each of the three
target OSes has defined expectations for window sizing, fonts, scrolling, copy/clipboard, keyboard
shortcuts, and file-path display, and that the visual design is platform-neutral.

## Part F — Accessibility specification
Document the accessibility requirements from section 12 as concrete, checkable rules.

**After reviewing this part of the sprint, the HITL should be able to confirm** that colour is never
the sole signal, that the UI is fully keyboard operable, and that semantic labels are defined for
every important element.

## Part G — Testing strategy & HITL review plan
Document the Compose desktop testing strategy and the HITL review checklist.

- List required semantic tags, Robot helpers, and representative-content fixtures.
- Define the long-response check and the HITL visual-review steps.

**After running this part of the sprint, you should see** a written testing plan naming the semantic
tags, Robot helpers, Search/Filter tests, representative Message-Kind tests, and long-response checks,
such that a future implementation sprint can follow it directly.

## Part H — HITL grill & incorporation
Use the `grill-with-docs` skill to obtain HITL feedback, then incorporate or explicitly defer it.

**After running this part of the sprint, the HITL should be able to confirm** that their feedback on the
six required review points has been captured and either incorporated into this document or recorded in
the "Deferred / Not Incorporated" section with a reason.

# 16. HITL Review Checklist

The HITL is asked (via `grill-with-docs`) to review and confirm:

1. Is the Sprint Goal clear?
2. Are the "After" sections specific enough to validate?
3. Is the UI scope too large, too small, or appropriate?
4. Are the ubiquitous language terms accurate and useful?
5. Are any important Junie output types missing?
6. Does the testing and review plan give enough confidence?

Feedback outcomes are recorded in section 18 (HITL Feedback Log) and, where deferred, in section 19.

# 17. Definition of Done

This sprint is done when:

- This sprint document exists under `docs/sprints/` with all 17 required sections.
- Every Sprint part has at least one concrete Reviewable Outcome ("After" section).
- `docs/UBIQUITOUS-LANGUAGE.md` exists, defines all required terms, and is referenced here.
- Included-vs-deferred rendering is explicit (section 10).
- The testing strategy is suitable for Compose Multiplatform desktop (section 13).
- HITL feedback has been requested via `grill-with-docs`, and each point is incorporated or deferred with a reason.
- Documentation expectations (below) are recorded.
- No application code was implemented in this sprint.

### Documentation expectations (for the later implementation sprint)

- Update `docs/UBIQUITOUS-LANGUAGE.md` first whenever a new term appears; keep code and UI copy aligned to it.
- Document new semantic tags and Robot helpers in/alongside `docs/TESTING.md`.
- Record architectural decisions (e.g. Turn grouping strategy, Markdown subset) as ADRs.
- Update `README.md` (via the `readme-updater` skill) and `docs/project_memory.md` (via the `project-memory` skill) at sprint completion.

# 18. HITL Feedback Log

Feedback gathered via the `grill-with-docs` skill on 2026-07-07.

| # | Review point | HITL response | Resulting change |
|---|---|---|---|
| 1 | Is the Sprint Goal clear? | Clear as-is. | No change; goal confirmed. |
| 2 | Are the "After" sections specific enough to validate? | Specific enough. | No change; "After" outcomes confirmed reviewable. |
| 3 | Is the UI scope appropriate? | Appropriate; deferrals in section 10 keep it achievable. | No change; scope confirmed. |
| 4 | Are the ubiquitous language terms accurate and useful? | Accurate and useful (incl. Patch vs Diff, Turn, Response vs Thought, Human vs user). | No change; glossary confirmed. |
| 5 | Are any important Junie output types missing? | Nothing missing. | No change; section 10 list confirmed complete. |
| 6 | Does the testing/review plan give enough confidence? | Enough confidence; visual-regression testing intentionally remains deferred. | No change; strategy confirmed. |
| — | Open questions Q1–Q5. | Keep them open for the implementation sprint. | Q1–Q5 remain open by explicit HITL decision. |

# 19. Deferred / Not Incorporated

- **Screenshot / visual-regression testing** — deferred (consistent with Sprint 1's decision); the HITL confirmed the functional + visual-review plan gives enough confidence.
- **Open questions Q1–Q5** — deliberately left open by the HITL to be resolved during the implementation sprint (timestamp display, default collapse of Thoughts/Tool Calls, match-to-match navigation, Turn-outline navigator, language-aware highlighting).
