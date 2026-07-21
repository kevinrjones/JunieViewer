# ADR 001: Message Content Registry

## Status
Accepted

## Context
The application handles various types of messages (`MessageKind`), each requiring specific logic for searchable text extraction, default collapsibility, and UI rendering. Previously, this logic was scattered across multiple exhaustive `when` chains in `ConversationViewModel.kt` and `MessageItems.kt`. This led to code duplication, increased complexity, and made adding new message types error-prone.

## Decision
We implemented a `MessageContentRegistry` to centralize metadata and logic for all `MessageKind` variants. 

Key aspects of the design:
- **Registry of Descriptors**: A plain map-based registry of `MessageContentDescriptor` objects.
- **Split Responsibility (HITL Q2)**: 
    - The shared registry (in `commonMain`) owns non-UI metadata: default collapsibility and searchable-text extraction.
    - The UI layer owns a separate `MessageRendererRegistry` for lookups of `@Composable` renderers.
- **No Reflection/DI**: The registries use plain Kotlin objects and maps to maintain simplicity, performance, and transparency.
- **Decomposition**: Large UI files (like `MessageItems.kt`) were decomposed into focused renderers registered in the UI-layer registry.

## Consequences
- **Improved Maintainability**: Adding a new `MessageKind` now requires exactly one descriptor registration and one renderer registration, instead of updating multiple `when` chains.
- **Reduced Complexity**: Exhaustive `when` chains are eliminated from the ViewModel and main UI components.
- **Better Testability**: Registry behavior (collapsibility, search extraction) can be tested in isolation.
- **UI Independence**: The core domain remains free of UI dependencies by splitting the registries.

## Alternatives Considered
- **Exhaustive `when` chains**: Simple but don't scale well and lead to duplication.
- **Reflection-based discovery**: Automates registration but adds runtime overhead and reduces transparency.
- **DI-based registration**: Overkill for a local registry and adds unnecessary complexity.

## References
- Sprint 6 Area 5 (F1)
- HITL Decision Q2
