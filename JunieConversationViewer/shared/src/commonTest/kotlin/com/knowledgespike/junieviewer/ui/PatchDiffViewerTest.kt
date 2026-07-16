package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.domain.*
import com.knowledgespike.junieviewer.fixtures.RepresentativeFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okio.FileSystem
import org.junit.Test

/**
 * UI tests for the Patch/Diff viewer improvements:
 * - Collapsible blocks (collapsed by default)
 * - Full content display (no truncation)
 * - Copy button and search highlighting preservation
 *
 * Note: side-by-side diff view toggle is hidden/deferred for now.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class PatchDiffViewerTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    /** Creates a ViewModel with the given messages for testing. */
    private fun createViewModel(messages: List<Message>): Pair<ConversationViewModel, okio.Path> {
        val tempPrefsPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "patch-diff-test-${System.currentTimeMillis()}.json"
        val fakePreferencesRepository = PreferencesRepository(
            path = tempPrefsPath,
            fileSystem = FileSystem.SYSTEM
        )
        fakePreferencesRepository.save(AppPreferences(lastSessionId = "test-session"))

        val fakeRepository = object : SessionRepository {
            override fun getMessages(): List<Message> = messages
            override fun listSessions(homePath: String): List<SessionInfo> = emptyList()
            override fun setSession(sessionId: String, homePath: String) {}
            override fun getSessionInfo(sessionId: String, homePath: String): SessionInfo? = null
        }

        return ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher) to tempPrefsPath
    }

    // -----------------------------------------------------------------------
    // Collapsible behaviour
    // -----------------------------------------------------------------------

    @Test
    fun `patch block is collapsed by default`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieDiffMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("patch_block_header").assertExists()
        onNodeWithTag("patch_inline_view").assertDoesNotExist()
        onNodeWithTag("selectable_diff_content").assertDoesNotExist()
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun `expanding patch block reveals body content`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieDiffMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("patch_block_header").performClick()
        waitForIdle()

        onNodeWithTag("patch_inline_view").assertExists()
        onNodeWithTag("selectable_diff_content").assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // Large patch — no truncation
    // -----------------------------------------------------------------------

    @Test
    fun `large patch content includes early and late lines after expansion`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.largeDiffMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("patch_block_header").performClick()
        waitForIdle()

        // First line marker
        onNodeWithText("FIRST_LINE_MARKER", substring = true).assertExists()
        // Last line marker — proves content is not truncated
        onNodeWithText("LAST_LINE_MARKER", substring = true).assertExists()
        FileSystem.SYSTEM.delete(path)
    }

    // -----------------------------------------------------------------------
    // Copy button
    // -----------------------------------------------------------------------

    @Test
    fun `copy button is present in patch header`() = runComposeUiTest {
        val (viewModel, path) = createViewModel(listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieDiffMessage
        ))
        setContent { ConversationRoot(viewModel = viewModel) }

        onNodeWithTag("copy_button").assertExists()
        FileSystem.SYSTEM.delete(path)
    }
}
