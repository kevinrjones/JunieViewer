package com.knowledgespike.junieviewer.domain

import kotlinx.serialization.Serializable

@Serializable
data class WindowStatePreferences(
    val x: Int? = null,
    val y: Int? = null,
    val width: Int = 800,
    val height: Int = 600,
    val isMaximized: Boolean = false
)

/**
 * Application preferences persisted to disk.
 * New fields must have defaults for backwards compatibility with existing preference files.
 */
@Serializable
data class AppPreferences(
    val window: WindowStatePreferences = WindowStatePreferences(),
    val junieHomePath: String = "~/.junie",
    val lastSessionId: String? = null,
    /** Persisted theme mode — "Light", "Dark", or "System". Defaults to "System" if missing or invalid. */
    val themeMode: String = "System"
)
