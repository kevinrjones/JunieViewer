package com.knowledgespike.junieviewer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Monospace font family token for code, diff, terminal, and structured output blocks.
 * Uses the platform default monospace font (no bundled font — confirmed Q3 decision).
 */
val MonospaceFont: FontFamily = FontFamily.Monospace

/**
 * Custom Material 3 Typography for the Junie Conversation Viewer.
 * Adapted from LogViewer's compact 13sp approach per Sprint 3 section 12.2.
 */
val JunieViewerTypography = Typography(
    headlineSmall = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif
    ),
    titleMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.SansSerif
    ),
    titleSmall = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.SansSerif
    ),
    bodyLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.SansSerif
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.SansSerif
    )
)
