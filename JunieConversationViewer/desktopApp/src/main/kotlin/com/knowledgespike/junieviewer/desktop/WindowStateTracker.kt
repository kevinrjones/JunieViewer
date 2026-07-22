package com.knowledgespike.junieviewer.desktop

import com.knowledgespike.junieviewer.domain.AppPreferences
import com.knowledgespike.junieviewer.domain.WindowStatePreferences

/**
 * Represents restored window state from persisted preferences.
 *
 * @property width The width of the window in dp.
 * @property height The height of the window in dp.
 * @property x The absolute x-position of the window, or null if not set.
 * @property y The absolute y-position of the window, or null if not set.
 * @property isMaximized Whether the window should be opened in a maximized state.
 */
data class RestoredWindowState(
    val width: Int,
    val height: Int,
    val x: Int?,
    val y: Int?,
    val isMaximized: Boolean
)

/**
 * Restores window state from application preferences.
 *
 * @param preferences The application preferences containing window state.
 * @return A [RestoredWindowState] object with the values to apply.
 */
fun restoreWindowState(preferences: AppPreferences): RestoredWindowState {
    return RestoredWindowState(
        width = preferences.window.width,
        height = preferences.window.height,
        x = preferences.window.x,
        y = preferences.window.y,
        isMaximized = preferences.window.isMaximized
    )
}

/**
 * Converts current window dimensions and position to preferences for persistence.
 *
 * @param width The current width of the window.
 * @param height The current height of the window.
 * @param x The current x-position of the window.
 * @param y The current y-position of the window.
 * @param isMaximized Whether the window is currently maximized.
 * @return A [WindowStatePreferences] object suitable for saving.
 */
fun toWindowPreferences(
    width: Int,
    height: Int,
    x: Int?,
    y: Int?,
    isMaximized: Boolean
): WindowStatePreferences {
    return WindowStatePreferences(
        x = x,
        y = y,
        width = width,
        height = height,
        isMaximized = isMaximized
    )
}
