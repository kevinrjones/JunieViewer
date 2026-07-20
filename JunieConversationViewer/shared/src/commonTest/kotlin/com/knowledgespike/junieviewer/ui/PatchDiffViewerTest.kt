package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.knowledgespike.junieviewer.data.LiveSessionTracker
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

    // -----------------------------------------------------------------------
    // Collapsible behaviour
    // -----------------------------------------------------------------------

    @Test
    fun `patch block is expanded by default`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieDiffMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("patch_block_header").assertExists()
        onNodeWithTag("patch_inline_view").assertExists()
        onNodeWithTag("selectable_diff_content").assertExists()
    }

    @Test
    fun `collapsing patch block hides body content`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieDiffMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Body visible by default
        onNodeWithTag("patch_inline_view").assertExists()
        // Collapse
        onNodeWithTag("patch_block_header").performClick()
        waitForIdle()

        onNodeWithTag("patch_inline_view").assertDoesNotExist()
    }

    // -----------------------------------------------------------------------
    // Large patch — no truncation
    // -----------------------------------------------------------------------

    @Test
    fun `large patch content includes early and late lines when expanded`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.largeDiffMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        // Already expanded by default
        // First line marker
        onNodeWithText("FIRST_LINE_MARKER", substring = true).assertExists()
        // Last line marker — proves content is not truncated
        onNodeWithText("LAST_LINE_MARKER", substring = true).assertExists()
    }

    // -----------------------------------------------------------------------
    // Copy button
    // -----------------------------------------------------------------------

    @Test
    fun `copy button is present in patch header`() = runConversationUiTest {
        sessionRepository.messagesToReturn = listOf(
            RepresentativeFixtures.humanTextMessage,
            RepresentativeFixtures.junieDiffMessage
        )
        preferencesRepository.save(AppPreferences(lastSessionId = "test-session"))
        setConversationContent()

        onNodeWithTag("copy_button").assertExists()
    }
}
