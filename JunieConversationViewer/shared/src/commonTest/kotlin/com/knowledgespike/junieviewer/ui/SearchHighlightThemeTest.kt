package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.ui.theme.darkConversationColors
import com.knowledgespike.junieviewer.ui.theme.lightConversationColors
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Tests verifying that search highlight colour tokens exist in both themes
 * and are visually distinct between regular and current-match styles.
 */
class SearchHighlightThemeTest {

    private val light = lightConversationColors()
    private val dark = darkConversationColors()

    @Test
    fun `light theme has four search highlight tokens`() {
        expectThat(light.searchHighlightBackground).isNotNull()
        expectThat(light.searchHighlightText).isNotNull()
        expectThat(light.currentMatchBackground).isNotNull()
        expectThat(light.currentMatchText).isNotNull()
    }

    @Test
    fun `dark theme has four search highlight tokens`() {
        expectThat(dark.searchHighlightBackground).isNotNull()
        expectThat(dark.searchHighlightText).isNotNull()
        expectThat(dark.currentMatchBackground).isNotNull()
        expectThat(dark.currentMatchText).isNotNull()
    }

    @Test
    fun `light search highlight background differs from current match background`() {
        expectThat(light.searchHighlightBackground).isNotEqualTo(light.currentMatchBackground)
    }

    @Test
    fun `dark search highlight background differs from current match background`() {
        expectThat(dark.searchHighlightBackground).isNotEqualTo(dark.currentMatchBackground)
    }

    @Test
    fun `light and dark search highlight backgrounds are not identical`() {
        expectThat(light.searchHighlightBackground).isNotEqualTo(dark.searchHighlightBackground)
    }

    @Test
    fun `light and dark current match backgrounds are not identical`() {
        expectThat(light.currentMatchBackground).isNotEqualTo(dark.currentMatchBackground)
    }
}
