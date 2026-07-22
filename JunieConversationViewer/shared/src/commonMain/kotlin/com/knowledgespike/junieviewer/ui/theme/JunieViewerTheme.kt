package com.knowledgespike.junieviewer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** CompositionLocal providing whether the currently active theme is dark. Follows the same resolution as [JunieViewerTheme]. */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

// ---------------------------------------------------------------------------
// M3 Colour Schemes — Sprint 3 section 12.1
// ---------------------------------------------------------------------------

/** Light colour scheme inspired by LogViewer Clean Light palette. */
val JunieViewerLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF007ACC),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF001D33),
    secondary = Color(0xFF4CAF50),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5F5F5),
    onSecondaryContainer = Color(0xFF121212),
    tertiary = Color(0xFFCE93D8),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF3E5F5),
    onTertiaryContainer = Color(0xFF1A0024),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF121212),
    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFF616161),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFBDBDBD),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFF410002)
)

/** Dark colour scheme inspired by LogViewer Industrial Dark palette. */
val JunieViewerDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF00A3E0),
    onPrimary = Color(0xFF003548),
    primaryContainer = Color(0xFF1A3A4A),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF66BB6A),
    onSecondary = Color(0xFF003910),
    secondaryContainer = Color(0xFF2B2B2B),
    onSecondaryContainer = Color(0xFFE0E0E0),
    tertiary = Color(0xFF5C6BC0),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF1A237E),
    onTertiaryContainer = Color(0xFFC5CAE9),
    background = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF2B2B2B),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF3C3F41),
    onSurfaceVariant = Color(0xFF9E9E9E),
    outline = Color(0xFF3C3F41),
    outlineVariant = Color(0xFF616161),
    error = Color(0xFFFF5252),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF3A1B1B),
    onErrorContainer = Color(0xFFFFDAD6)
)

// ---------------------------------------------------------------------------
// JunieViewerTheme composable
// ---------------------------------------------------------------------------

/**
 * Top-level theme composable for the Junie Conversation Viewer.
 * Resolves [ThemeMode] to a light or dark scheme, provides M3 [MaterialTheme],
 * semantic [ConversationColors], and [JunieViewerSpacing] via CompositionLocal.
 */
@Composable
fun JunieViewerTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) JunieViewerDarkColorScheme else JunieViewerLightColorScheme
    val conversationColors = if (isDark) darkConversationColors() else lightConversationColors()

    CompositionLocalProvider(
        LocalConversationColors provides conversationColors,
        LocalJunieViewerSpacing provides JunieViewerSpacing(),
        LocalIsDarkTheme provides isDark
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = JunieViewerTypography,
            content = content
        )
    }
}

// ---------------------------------------------------------------------------
// Accessor object — convenient themed property access
// ---------------------------------------------------------------------------

/**
 * Accessor object for Junie Viewer theme tokens.
 * Usage: `JunieViewerTheme.conversationColors.humanAccent`
 */
object JunieViewerTheme {
    /** Semantic conversation colour tokens for the current theme. */
    val conversationColors: ConversationColors
        @Composable
        @ReadOnlyComposable
        get() = LocalConversationColors.current

    /** Spacing scale for the current theme. */
    val spacing: JunieViewerSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalJunieViewerSpacing.current

    /** Whether the currently active theme is dark, as resolved by [JunieViewerTheme]. */
    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalIsDarkTheme.current
}
