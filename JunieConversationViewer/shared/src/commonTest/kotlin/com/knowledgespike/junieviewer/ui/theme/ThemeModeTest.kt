package com.knowledgespike.junieviewer.ui.theme

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.hasSize

/** Verifies ThemeMode enum values and their declaration order. */
class ThemeModeTest {

    @Test
    fun `ThemeMode has exactly three values in Light Dark System order`() {
        val values = ThemeMode.entries
        expectThat(values).hasSize(3)
        expectThat(values[0]).isEqualTo(ThemeMode.Light)
        expectThat(values[1]).isEqualTo(ThemeMode.Dark)
        expectThat(values[2]).isEqualTo(ThemeMode.System)
    }

    @Test
    fun `ThemeMode valueOf round-trips correctly`() {
        ThemeMode.entries.forEach { mode ->
            expectThat(ThemeMode.valueOf(mode.name)).isEqualTo(mode)
        }
    }
}
