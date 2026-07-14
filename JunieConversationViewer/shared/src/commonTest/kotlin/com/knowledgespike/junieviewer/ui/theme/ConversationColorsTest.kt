package com.knowledgespike.junieviewer.ui.theme

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isNotEqualTo
import strikt.assertions.isEqualTo
import androidx.compose.ui.graphics.Color

/** Verifies light and dark ConversationColors have distinct, correct values. */
class ConversationColorsTest {

    private val light = lightConversationColors()
    private val dark = darkConversationColors()

    @Test
    fun `light and dark ConversationColors are not identical`() {
        expectThat(light).isNotEqualTo(dark)
    }

    @Test
    fun `light humanAccent matches sprint doc value`() {
        expectThat(light.humanAccent).isEqualTo(Color(0xFF007ACC))
    }

    @Test
    fun `dark humanAccent matches sprint doc value`() {
        expectThat(dark.humanAccent).isEqualTo(Color(0xFF00A3E0))
    }

    @Test
    fun `light and dark humanAccent differ`() {
        expectThat(light.humanAccent).isNotEqualTo(dark.humanAccent)
    }

    @Test
    fun `light and dark junieAccent differ`() {
        expectThat(light.junieAccent).isNotEqualTo(dark.junieAccent)
    }

    @Test
    fun `light and dark thoughtBackground differ`() {
        expectThat(light.thoughtBackground).isNotEqualTo(dark.thoughtBackground)
    }

    @Test
    fun `light and dark codeBackground differ`() {
        expectThat(light.codeBackground).isNotEqualTo(dark.codeBackground)
    }

    @Test
    fun `light and dark diffAdded differ`() {
        expectThat(light.diffAdded).isNotEqualTo(dark.diffAdded)
    }

    @Test
    fun `light and dark terminalBackground differ`() {
        expectThat(light.terminalBackground).isNotEqualTo(dark.terminalBackground)
    }

    @Test
    fun `terminalCommand token exists in both palettes`() {
        expectThat(light.terminalCommand).isEqualTo(Color(0xFF4EC9B0))
        expectThat(dark.terminalCommand).isEqualTo(Color(0xFF4EC9B0))
    }

    @Test
    fun `diffHunkHeader token exists in both palettes`() {
        expectThat(light.diffHunkHeader).isEqualTo(Color(0x220000CC))
        expectThat(dark.diffHunkHeader).isEqualTo(Color(0x33007ACC))
    }

    @Test
    fun `all 18 semantic tokens are present`() {
        // Verify all tokens are accessible (compile-time check via property access)
        val tokens = listOf(
            light.humanAccent, light.junieAccent,
            light.thoughtBackground, light.thoughtBorder,
            light.toolCallBackground, light.toolCallBorder,
            light.terminalBackground, light.terminalText, light.terminalCommand,
            light.codeBackground, light.codeBorder,
            light.diffAdded, light.diffRemoved, light.diffAddedText, light.diffRemovedText,
            light.diffHunkHeader,
            light.errorBackground, light.warningBackground
        )
        expectThat(tokens.size).isEqualTo(18)
    }
}
