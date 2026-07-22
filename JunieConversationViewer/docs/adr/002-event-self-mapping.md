# ADR 002: Event Self-Mapping and Stable IDs

## Status
Accepted

## Context
Mapping raw Junie events to UI `Message` objects was previously handled by a centralized `EventToMessageMapper` using a large `when` chain. This approach made it difficult to maintain mapping logic alongside event definitions and resulted in unstable message IDs (e.g., using `hashCode()` fallbacks) that broke UI state preservation during live reloads.

## Decision
We adopted the Strategy pattern for event mapping and established a stable ID scheme.

Key aspects of the design:
- **Self-Mapping Events (F3/Q1)**: Each `JunieEvent` and `AgentEvent` implements a `toMessage(context: MappingContext)` method, owning its transformation to a domain `Message`.
- **Stable Message IDs (F9/Q3)**: Message IDs are derived from the event's source position (session path + line number) provided via `MappingContext`, ensuring deterministic identity across reloads.
- **Orchestration Mapper**: `EventToMessageMapper` is reduced to pure orchestration (ordering, line-based ID assignment, and filtering of no-message events).

## Consequences
- **Encapsulation**: Event definitions and their corresponding UI mapping logic are now co-located, improving discoverability and maintainability.
- **UI State Preservation**: Stable IDs enable the UI to correctly preserve expansion state and search highlights even as the session file is updated during live tracking.
- **Extensibility**: Adding a new event type only requires defining the event class and its `toMessage` implementation, with no changes needed to the central mapper.

## Alternatives Considered
- **Visitor Pattern**: Provides type safety but is more boilerplate-heavy and separates logic from the event data.
- **Centralized `when` chain**: Hard to scale and maintain as the number of event types grows.

## References
- Sprint 6 Area 4 (F3, F9)
- HITL Decision Q1, Q3
