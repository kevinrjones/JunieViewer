package com.knowledgespike.junieviewer.desktop

import com.knowledgespike.junieviewer.domain.AppPreferences
import com.knowledgespike.junieviewer.domain.WindowStatePreferences
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import kotlin.test.Test

class WindowStateTrackerTest {

    @Test
    fun `restores width and height from preferences`() {
        val prefs = AppPreferences(
            window = WindowStatePreferences(width = 1024, height = 768)
        )
        val restored = restoreWindowState(prefs)

        expectThat(restored.width).isEqualTo(1024)
        expectThat(restored.height).isEqualTo(768)
    }

    @Test
    fun `restores absolute position when x and y are present`() {
        val prefs = AppPreferences(
            window = WindowStatePreferences(x = 100, y = 200)
        )
        val restored = restoreWindowState(prefs)

        expectThat(restored.x).isEqualTo(100)
        expectThat(restored.y).isEqualTo(200)
    }

    @Test
    fun `uses null position when x or y are absent`() {
        val prefsXOnly = AppPreferences(
            window = WindowStatePreferences(x = 100, y = null)
        )
        val restoredXOnly = restoreWindowState(prefsXOnly)
        expectThat(restoredXOnly.y).isNull()

        val prefsYOnly = AppPreferences(
            window = WindowStatePreferences(x = null, y = 200)
        )
        val restoredYOnly = restoreWindowState(prefsYOnly)
        expectThat(restoredYOnly.x).isNull()
    }

    @Test
    fun `restores maximized state`() {
        val prefs = AppPreferences(
            window = WindowStatePreferences(isMaximized = true)
        )
        val restored = restoreWindowState(prefs)

        expectThat(restored.isMaximized).isEqualTo(true)
    }

    @Test
    fun `saves width and height`() {
        val prefs = toWindowPreferences(
            width = 1280,
            height = 1024,
            x = null,
            y = null,
            isMaximized = false
        )

        expectThat(prefs.width).isEqualTo(1280)
        expectThat(prefs.height).isEqualTo(1024)
    }

    @Test
    fun `saves absolute position`() {
        val prefs = toWindowPreferences(
            width = 800,
            height = 600,
            x = 150,
            y = 250,
            isMaximized = false
        )

        expectThat(prefs.x).isEqualTo(150)
        expectThat(prefs.y).isEqualTo(250)
    }

    @Test
    fun `saves maximized state`() {
        val prefs = toWindowPreferences(
            width = 800,
            height = 600,
            x = null,
            y = null,
            isMaximized = true
        )

        expectThat(prefs.isMaximized).isEqualTo(true)
    }

    @Test
    fun `preserves default values for missing position`() {
        val prefs = toWindowPreferences(
            width = 800,
            height = 600,
            x = null,
            y = null,
            isMaximized = false
        )

        expectThat(prefs.x).isNull()
        expectThat(prefs.y).isNull()
    }
}
