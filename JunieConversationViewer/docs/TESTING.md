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
