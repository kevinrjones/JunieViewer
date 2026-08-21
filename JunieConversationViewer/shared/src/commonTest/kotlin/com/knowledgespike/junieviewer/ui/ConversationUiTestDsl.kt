package com.knowledgespike.junieviewer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.runComposeUiTest
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionLoadResult
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.SessionInfo
import com.knowledgespike.junieviewer.domain.TopLevelSearchQuery
import com.knowledgespike.junieviewer.domain.TopLevelSearchResults
import com.knowledgespike.junieviewer.domain.TopLevelSearchStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.FileSystem
import okio.Path
import kotlin.random.Random

/**
 * Configurable in-memory fake of [SessionRepository] shared by Conversation tests.
 *
 * Messages, listed sessions, and session info can be reconfigured mid-test by mutating
 * the exposed properties, and [loadCount] / [lastSessionId] record call history so tests
 * can assert on repository interactions.
 */
class FakeSessionRepository(
    initialMessages: List<Message> = emptyList(),
    initialSessions: List<SessionInfo> = emptyList()
) : SessionRepository {

    /** Messages returned by [getMessages] and [loadSession]. Mutable so tests can change it mid-test. */
    var messagesToReturn: List<Message> = initialMessages

    /** Sessions returned by [listSessions], regardless of the requested home path. */
    var sessionsToReturn: List<SessionInfo> = initialSessions

    /** Produces the [SessionInfo] returned by [getSessionInfo]; replace for custom behaviour. */
    var sessionInfoProvider: (sessionId: String, homePath: String) -> SessionInfo? =
        { sessionId, _ -> SessionInfo(sessionId, "/path/$sessionId", 123L) }

    /** Top-level search results returned by [searchSessions]. */
    var searchResultsToReturn: TopLevelSearchResults = TopLevelSearchResults(
        status = TopLevelSearchStatus.Completed
    )

    /** Number of times [loadSession] has been called. */
    var loadCount: Int = 0
        private set

    /** The most recent session id passed to [loadSession]. */
    var lastSessionId: String? = null
        private set

    override fun loadSession(sessionId: String, homePath: String): SessionLoadResult {
        lastSessionId = sessionId
        loadCount++
        return SessionLoadResult(messagesToReturn, null, 0L)
    }

    override fun listSessions(homePath: String): List<SessionInfo> = sessionsToReturn

    override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? =
        sessionInfoProvider(sessionId, homePath)

    override suspend fun searchSessions(query: TopLevelSearchQuery, homePath: String): TopLevelSearchResults {
        val normalizedQuery = TopLevelSearchQuery(query.raw)
        return searchResultsToReturn.copy(query = normalizedQuery)
    }
}

/** Builds a unique temp path for a preferences file used by a single test run. */
private fun createTempPrefsPath(prefix: String): Path =
    FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "$prefix-${System.currentTimeMillis()}-${Random.nextInt()}.json"

/** Deletes [path] if present, swallowing any error so cleanup never fails a test. */
private fun deleteQuietly(path: Path) {
    try {
        if (FileSystem.SYSTEM.exists(path)) FileSystem.SYSTEM.delete(path)
    } catch (_: Exception) {
        // Best-effort cleanup only.
    }
}

/**
 * Scope exposed inside [runConversationUiTest]. Holds the fixtures needed to configure
 * preferences and fake Session data before the [ConversationViewModel] is created and the
 * Compose content is set.
 */
@OptIn(ExperimentalTestApi::class)
class ConversationUiTestScope internal constructor(
    private val composeUiTest: ComposeUiTest,
    val sessionRepository: FakeSessionRepository,
    val preferencesRepository: PreferencesRepository,
    private val testDispatcher: TestDispatcher
) : SemanticsNodeInteractionsProvider by composeUiTest {
    /** Robot for driving and asserting on the Conversation screen. */
    val robot: ConversationRobot by lazy { ConversationRobot(composeUiTest) }

    /**
     * [ConversationViewModel] under test, created lazily on first access so preferences and
     * fake Session data can be configured beforehand.
     */
    val viewModel: ConversationViewModel by lazy {
        ConversationViewModel(sessionRepository, preferencesRepository, testDispatcher, LiveSessionTracker())
    }

    /** Sets the Compose content to [ConversationRoot] backed by [viewModel]. */
    fun setConversationContent() {
        composeUiTest.setContent {
            ConversationRoot(viewModel = viewModel)
        }
    }

    /** Sets a custom Compose content. */
    fun setContent(block: @Composable () -> Unit) {
        composeUiTest.setContent(block)
    }

    /** Wait for the UI to be idle. */
    fun waitForIdle() {
        composeUiTest.waitForIdle()
    }
}

/**
 * Runs a Conversation Compose UI test, wiring up a [FakeSessionRepository], a temp-file
 * backed [PreferencesRepository], and the [ConversationViewModel]/[ConversationRobot] pair
 * used to drive [ConversationRoot].
 *
 * Preferences and Session data can be configured on the [ConversationUiTestScope] before
 * calling [ConversationUiTestScope.setConversationContent] — the [ConversationViewModel] is
 * only created on first access, so any setup performed earlier in [block] is honoured.
 * The temp preferences file is deleted automatically once [block] completes.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
fun runConversationUiTest(
    initialMessages: List<Message> = emptyList(),
    testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    block: ConversationUiTestScope.() -> Unit
) = runComposeUiTest {
    val tempPrefsPath = createTempPrefsPath("ui-test-prefs")
    val preferencesRepository = PreferencesRepository(path = tempPrefsPath, fileSystem = FileSystem.SYSTEM)
    val sessionRepository = FakeSessionRepository(initialMessages)
    try {
        ConversationUiTestScope(this, sessionRepository, preferencesRepository, testDispatcher).block()
    } finally {
        deleteQuietly(tempPrefsPath)
    }
}

/**
 * Scope exposed inside [runConversationStateTest]. Wraps the underlying [testScope] so tests
 * can call coroutine test utilities such as [advanceUntilIdle], and exposes the fake
 * repository, preferences, and a factory for creating [ConversationViewModel]s.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConversationStateTestScope internal constructor(
    val testScope: TestScope,
    val sessionRepository: FakeSessionRepository,
    val preferencesRepository: PreferencesRepository,
    private val testDispatcher: TestDispatcher
) {

    /** Creates a fresh [ConversationViewModel] wired to [sessionRepository] and [preferencesRepository]. */
    fun createViewModel(): ConversationViewModel =
        ConversationViewModel(sessionRepository, preferencesRepository, testDispatcher, LiveSessionTracker())

    /** Advances [testScope]'s virtual clock until no more work is scheduled. */
    fun advanceUntilIdle() = testScope.advanceUntilIdle()
}

/**
 * Runs a Conversation ViewModel state test, wiring up a [FakeSessionRepository], a temp-file
 * backed [PreferencesRepository], and `Dispatchers.setMain`/`resetMain` around a [runTest]
 * block.
 *
 * Preferences and Session data can be configured on the [ConversationStateTestScope] before
 * calling [ConversationStateTestScope.createViewModel]. The temp preferences file is deleted
 * and the Main dispatcher is reset automatically once [block] completes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runConversationStateTest(
    initialMessages: List<Message> = emptyList(),
    testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    block: suspend ConversationStateTestScope.() -> Unit
): TestResult {
    Dispatchers.setMain(testDispatcher)
    val tempPrefsPath = createTempPrefsPath("state-test-prefs")
    val preferencesRepository = PreferencesRepository(path = tempPrefsPath, fileSystem = FileSystem.SYSTEM)
    val sessionRepository = FakeSessionRepository(initialMessages)
    return try {
        runTest {
            ConversationStateTestScope(this, sessionRepository, preferencesRepository, testDispatcher).block()
        }
    } finally {
        Dispatchers.resetMain()
        deleteQuietly(tempPrefsPath)
    }
}
