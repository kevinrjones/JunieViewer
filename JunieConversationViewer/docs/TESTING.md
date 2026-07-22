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

Example usage in `ConversationScreenTest.kt` (via the Sprint 6 DSL, see below):
```kotlin
@Test
fun `searching for text filters the message list`() = runConversationUiTest(testMessages) {
    setConversationContent()

    robot.typeSearchQuery("Match")
    robot.assertMessageCount(1)
    robot.assertMessageVisible("Match this")
}
```

## Sprint 6 Test Infrastructure (F11)

### The `runConversationUiTest { }` DSL

`shared/src/commonTest/kotlin/com/knowledgespike/junieviewer/ui/ConversationUiTestDsl.kt` provides shared wiring so no test hand-builds the repository/ViewModel/Robot triple. Two entry points share a common fixture:

- **`runConversationUiTest(initialMessages) { }`** — Compose-level tests. Wraps `runComposeUiTest`; the scope creates a temp-file `PreferencesRepository`, a configurable `FakeSessionRepository`, a lazily built `ConversationViewModel`, and a lazy `robot: ConversationRobot`. Call `setConversationContent()` to render `ConversationRoot`, or use the `setContent { }` helper for custom composables. The scope implements `SemanticsNodeInteractionsProvider`, so `onNodeWithTag(...)` works directly. Temp files are deleted in a `finally` block.
- **`runConversationStateTest(initialMessages) { }`** — ViewModel state tests. Wraps `runTest` with `Dispatchers.setMain`/`resetMain`; the scope exposes `createViewModel()`, `advanceUntilIdle()`, `sessionRepository`, `preferencesRepository`, and the raw `testScope: TestScope` for other coroutine-test extensions. Cleanup is automatic.

`FakeSessionRepository` is the shared configurable fake: mutable `messagesToReturn`, `sessionsToReturn`, and `sessionInfoProvider`, with read-only `loadCount`/`lastSessionId` for interaction tracking.

Because the ViewModel is built lazily, tests can seed preferences first (e.g. `preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))`) before the first `viewModel` access.

### Consolidated Behaviour-Area Suites

UI integration tests are organized into six behaviour-area suites (HITL decision Q5):

| Behaviour area | Suite | Origin |
|---|---|---|
| Search | `SearchBehaviourTest.kt` | `SearchFilterNavigationTest` + `SearchStateDerivationTest` |
| Filters | `FilterBehaviourTest.kt` | unchanged |
| Sort | `SortOrderTest.kt` | unchanged |
| Expansion | `ExpansionBehaviourTest.kt` | `CollapseShowAllTest` + `CollapsibleBlockTest` |
| Commands | `ConversationCommandTest.kt` | unchanged |
| Live tracking | `LiveTrackingBehaviourTest.kt` | `LiveTrackingViewModelTest` + `RefreshAndAutoRefreshTest` |

Every assertion from the merged source files was carried over verbatim; merged suites keep section comments marking each test's origin. Specialized files (rendering, accessibility, session states, text selection, pure component tests) remain separate.

### Parameterized Parser Tests

`data/JsonlParserTest.kt` is table-driven: a `ParserCase(name, jsonLine, verify)` list run with JUnit 4 `@RunWith(Parameterized::class)`. Adding a new event case requires one table row; failure output includes the case name. A companion `JsonlParserTestRoundTrip` class builds events via the real serializers from `EventSerializers.kt` and verifies parse symmetry.

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

## Sprint 5 Testing Additions

### Command Model Tests
`ConversationCommandTest.kt` verifies the shared command model. Tests cover command enablement state derivation (e.g., Copy is disabled if no session selected) and dispatch mapping from toolbar/menu actions to ViewModel functions.

### Area 8 Control Tests
`AccessibilityAndArea8Test.kt` was expanded to cover:
- **Copy no-crash**: Verifies that the global Copy command executes safely even with no selection.
- **Search Navigation**: Verifies `FindNext` and `FindPrevious` wrap-around behaviour and match index stability.
- **Focus Search**: Verifies that the `FocusSearch` command correctly triggers focus on the search field.

### Feature-Specific Suites
Three new test files provide exhaustive coverage for Sprint 5 logic:
- **Refresh/Auto-Refresh**: now part of `LiveTrackingBehaviourTest.kt` — covers manual refresh, auto-refresh toggle, preference persistence, and Session selection interaction.
- **Sort Order**: `SortOrderTest.kt` covers display ordering, persistence, filter/search interaction, and auto-scroll adaptation in Newest First mode.
- **Collapse/Show All**: now part of `ExpansionBehaviourTest.kt` — covers global collapse/expand commands, per-block manual overrides, and search force-expansion priority.

### Toolbar and Menu Testing
Native macOS menu items cannot be tested via the Compose Test Rule. Testing strategy for menus relies on verifying the shared `ConversationCommand` dispatch logic at the ViewModel level. Toolbar buttons are tested via the Robot pattern using stable `testTag` conventions:
- `toolbar_open_session`
- `toolbar_refresh`
- `toolbar_copy`
- `toolbar_auto_refresh`
- `toolbar_sort_order`
- `toolbar_collapse_all`
- `toolbar_show_all`
- `toolbar_search_field`

### Test Commands
```bash
./gradlew :shared:jvmTest    # Shared module JVM tests
./gradlew test                # Full project test suite
```
