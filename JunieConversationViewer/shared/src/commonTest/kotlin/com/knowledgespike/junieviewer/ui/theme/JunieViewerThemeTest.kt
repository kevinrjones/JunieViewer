package com.knowledgespike.junieviewer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo

/** Compose UI tests verifying JunieViewerTheme provides correct theme locals. */
class JunieViewerThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `Light mode provides light semantic colours`() {
        var capturedColors: ConversationColors? = null

        composeTestRule.setContent {
            JunieViewerTheme(themeMode = ThemeMode.Light) {
                capturedColors = LocalConversationColors.current
                Text("Light")
            }
        }

        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
        expectThat(capturedColors).isEqualTo(lightConversationColors())
    }

    @Test
    fun `Dark mode provides dark semantic colours`() {
        var capturedColors: ConversationColors? = null

        composeTestRule.setContent {
            JunieViewerTheme(themeMode = ThemeMode.Dark) {
                capturedColors = LocalConversationColors.current
                Text("Dark")
            }
        }

        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
        expectThat(capturedColors).isEqualTo(darkConversationColors())
    }

    @Test
    fun `Light and Dark modes provide different M3 colour schemes`() {
        var lightPrimary by mutableStateOf(androidx.compose.ui.graphics.Color.Unspecified)
        var darkPrimary by mutableStateOf(androidx.compose.ui.graphics.Color.Unspecified)

        composeTestRule.setContent {
            JunieViewerTheme(themeMode = ThemeMode.Light) {
                lightPrimary = MaterialTheme.colorScheme.primary
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.setContent {
            JunieViewerTheme(themeMode = ThemeMode.Dark) {
                darkPrimary = MaterialTheme.colorScheme.primary
            }
        }
        composeTestRule.waitForIdle()

        expectThat(lightPrimary).isNotEqualTo(darkPrimary)
    }

    @Test
    fun `System mode resolves without crashing and provides valid theme`() {
        var capturedColors: ConversationColors? = null
        var capturedSpacing: JunieViewerSpacing? = null

        composeTestRule.setContent {
            JunieViewerTheme(themeMode = ThemeMode.System) {
                capturedColors = LocalConversationColors.current
                capturedSpacing = LocalJunieViewerSpacing.current
                Text("System")
            }
        }

        composeTestRule.onNodeWithText("System").assertIsDisplayed()
        // System mode should resolve to either light or dark — both are valid
        val isLightOrDark = capturedColors == lightConversationColors() || capturedColors == darkConversationColors()
        expectThat(isLightOrDark).isEqualTo(true)
        expectThat(capturedSpacing).isEqualTo(JunieViewerSpacing())
    }

    @Test
    fun `spacing is provided via CompositionLocal`() {
        var capturedSpacing: JunieViewerSpacing? = null

        composeTestRule.setContent {
            JunieViewerTheme(themeMode = ThemeMode.Light) {
                capturedSpacing = LocalJunieViewerSpacing.current
                Text("Spacing")
            }
        }

        composeTestRule.onNodeWithText("Spacing").assertIsDisplayed()
        expectThat(capturedSpacing).isEqualTo(JunieViewerSpacing())
    }
}
