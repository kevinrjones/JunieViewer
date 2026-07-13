package com.knowledgespike.junieviewer.ui

import androidx.compose.ui.test.*
import com.knowledgespike.junieviewer.domain.SessionInfo
import com.knowledgespike.junieviewer.ui.components.SessionItem
import kotlin.test.Test

/**
 * Tests for [SessionItem] composable — verifies that session rows
 * render id, working directory, and the correct timestamp label.
 */
class SessionSelectorTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `session item renders session id`() = runComposeUiTest {
        val session = SessionInfo(
            id = "session-abc-123",
            path = "/home/user/.junie/sessions/session-abc-123",
            lastModified = 1720000000000L,
            workingDirectory = "/Users/dev/my-project"
        )
        setContent {
            SessionItem(session = session, isSelected = false, onClick = {})
        }
        onNodeWithText("session-abc-123").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `session item renders working directory`() = runComposeUiTest {
        val session = SessionInfo(
            id = "session-1",
            path = "/home/user/.junie/sessions/session-1",
            lastModified = 1720000000000L,
            workingDirectory = "/Users/dev/my-project/src"
        )
        setContent {
            SessionItem(session = session, isSelected = false, onClick = {})
        }
        onNodeWithText("Project: /Users/dev/my-project/src", substring = true).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `session item hides working directory when null`() = runComposeUiTest {
        val session = SessionInfo(
            id = "session-1",
            path = "/home/user/.junie/sessions/session-1",
            lastModified = 1720000000000L,
            workingDirectory = null
        )
        setContent {
            SessionItem(session = session, isSelected = false, onClick = {})
        }
        onNodeWithText("Project:", substring = true).assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `session item shows created label when createdAt is present`() = runComposeUiTest {
        val session = SessionInfo(
            id = "session-1",
            path = "/path/session-1",
            lastModified = 1720000000000L,
            createdAt = 1719000000000L,
            workingDirectory = "/Users/dev/project"
        )
        setContent {
            SessionItem(session = session, isSelected = false, onClick = {})
        }
        onNodeWithText("Created:", substring = true).assertIsDisplayed()
        onNodeWithText("Last modified:", substring = true).assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `session item shows last modified label when createdAt is absent`() = runComposeUiTest {
        val session = SessionInfo(
            id = "session-1",
            path = "/path/session-1",
            lastModified = 1720000000000L,
            createdAt = null,
            workingDirectory = "/Users/dev/project"
        )
        setContent {
            SessionItem(session = session, isSelected = false, onClick = {})
        }
        onNodeWithText("Last modified:", substring = true).assertIsDisplayed()
        onNodeWithText("Created:", substring = true).assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `selected session item uses bold font weight`() = runComposeUiTest {
        val session = SessionInfo(
            id = "selected-session",
            path = "/path/selected-session",
            lastModified = 1720000000000L,
            workingDirectory = "/Users/dev/project"
        )
        setContent {
            SessionItem(session = session, isSelected = true, onClick = {})
        }
        onNodeWithText("selected-session").assertIsDisplayed()
    }
}
