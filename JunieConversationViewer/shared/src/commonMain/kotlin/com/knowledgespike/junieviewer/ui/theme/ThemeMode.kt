package com.knowledgespike.junieviewer.ui.theme

/**
 * Represents the available theme modes for the application.
 * Persisted in AppPreferences via PreferencesRepository.
 */
enum class ThemeMode {
    /** Forces the light colour scheme. */
    Light,

    /** Forces the dark colour scheme. */
    Dark,

    /** Follows the operating system's current theme setting. */
    System
}
