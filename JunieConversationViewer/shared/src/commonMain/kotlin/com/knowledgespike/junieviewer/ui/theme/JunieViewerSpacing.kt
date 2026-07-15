package com.knowledgespike.junieviewer.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale for the Junie Conversation Viewer.
 * Values from Sprint 3 section 12.4.
 */
@Immutable
data class JunieViewerSpacing(
    /** 2dp — tight internal padding. */
    val xs: Dp = 2.dp,
    /** 4dp — chip padding, icon gaps. */
    val sm: Dp = 4.dp,
    /** 8dp — standard internal padding. */
    val md: Dp = 8.dp,
    /** 12dp — card padding, section gaps. */
    val lg: Dp = 12.dp,
    /** 16dp — screen-edge padding, major section gaps. */
    val xl: Dp = 16.dp,
    /** 24dp — between-turn spacing. */
    val xxl: Dp = 24.dp
)

/** CompositionLocal providing the current [JunieViewerSpacing] instance. */
val LocalJunieViewerSpacing = staticCompositionLocalOf { JunieViewerSpacing() }
