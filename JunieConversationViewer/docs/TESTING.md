# Testing Standards and Patterns

This project follows a "Test-First" culture, ensuring that all business logic and UI interactions are verified through automated tests.

## Testing Stack

- **JUnit 4**: The primary test runner.
- **MockK**: For mocking and spying on objects.
- **Turbine**: For testing Kotlin Flows (StateFlow, SharedFlow) in ViewModels.
- **Compose Test Rule**: For functional UI testing on Desktop.
- **Okio FileSystem**: Used for testing file-based logic, preferring temporary directories for isolation.

## Architectural Patterns for Testability

### Fakes over Mocks
We prefer using **Fake implementations** for repositories in ViewModel tests. Fakes provide more realistic behavior and are less brittle than mocks when implementation details change.

Example Fake Repository in `ConversationViewModelTest.kt`:
```kotlin
private val fakeRepository = object : SessionRepository {
    override fun getMessages(): List<Message> = testMessages
    // ...
}
```

### Robot Pattern for UI Tests
We use the **Robot Pattern** to encapsulate UI interactions and assertions. This separates *what* we test from *how* we interact with Compose, making tests more readable and maintainable.

The `ConversationRobot` provides a high-level API for interacting with the conversation screen.

Example usage in `ConversationScreenTest.kt`:
```kotlin
@Test
fun `searching for text filters the message list`() = runComposeUiTest {
    // ... setup ...
    val robot = ConversationRobot(this)

    robot.typeSearchQuery("Match")
    robot.assertMessageCount(1)
    robot.assertMessageVisible("Match this")
}
```

## How to Run Tests

### Run all tests
```bash
./gradlew test
```

### Run specific module tests
```bash
./gradlew :shared:jvmTest
```

## Adding New Tests

1. **Unit Tests**: Place in `shared/src/commonTest/kotlin/...`.
2. **UI Tests**: Place in `shared/src/commonTest/kotlin/...` using `runComposeUiTest`.
3. **Test Tags**: When adding new UI components, use `Modifier.testTag("tag_name")` to make them accessible to the Robot.

## Sprint 4 Testing Additions

### Agent Event Parser Tests
Tests for new event types (e.g., `AgentTaskFailedEvent`) follow the existing `JsonlParserTest` pattern. Nested agent events are wrapped in the `SessionA2uxEvent`/`AgentEventWrapper` JSONL structure. Cover valid, minimal (all nulls), extra-field, and structured-details payloads.

### Event-to-Message Mapper Tests
`EventToMessageMapperTest` verifies that each event type maps to the correct `Sender`, `MessageKind`, and content. Use helper functions to construct `SessionA2uxEvent` wrappers for agent events.

### Live Tracking Flow Tests
Live tracking uses polling-based file watching with incremental offset parsing. Flow tests use Turbine to verify that new events appended to `events.jsonl` are emitted as Messages.

### Robot Pattern Helpers
New Robot helpers added during Sprint 4:
- Error/failure block assertions via `error_warning_block` test tag.
- Search highlighting verification.
- Filter toggle helpers.

### LazyColumn Virtualization Caveat
Off-screen items in `LazyColumn` are not rendered during UI tests. Assertions must target visible items only, or scroll to the target item before asserting.

### Test Commands
```bash
./gradlew :shared:jvmTest    # Shared module JVM tests
./gradlew test                # Full project test suite
```
