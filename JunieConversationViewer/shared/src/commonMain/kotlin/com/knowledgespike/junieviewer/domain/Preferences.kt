package com.knowledgespike.junieviewer.domain

import kotlinx.serialization.Serializable

@Serializable
data class WindowPreferences(
    val x: Int? = null,
    val y: Int? = null,
    val width: Int = 800,
    val height: Int = 600
)

@Serializable
data class AppPreferences(
    val window: WindowPreferences = WindowPreferences(),
    val junieHomePath: String = "~/.junie",
    val lastSessionId: String? = null
)
