package com.knowledgespike.junieviewer.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colour tokens for conversation UI elements.
 * Provided via [LocalConversationColors] and accessed through [JunieViewerTheme.conversationColors].
 */
@Immutable
data class ConversationColors(
    val humanAccent: Color,
    val junieAccent: Color,
    val thoughtBackground: Color,
    val thoughtBorder: Color,
    val toolCallBackground: Color,
    val toolCallBorder: Color,
    val terminalBackground: Color,
    val terminalText: Color,
    val terminalCommand: Color,
    val codeBackground: Color,
    val codeBorder: Color,
    val diffAdded: Color,
    val diffRemoved: Color,
    val diffAddedText: Color,
    val diffRemovedText: Color,
    val diffHunkHeader: Color,
    val errorBackground: Color,
    val errorBorder: Color,
    val warningBackground: Color,
    val warningBorder: Color,
    val searchHighlightBackground: Color,
    val searchHighlightText: Color,
    val currentMatchBackground: Color,
    val currentMatchText: Color
)

/** Light conversation colour tokens — inspired by LogViewer Clean Light palette. */
fun lightConversationColors(): ConversationColors = ConversationColors(
    humanAccent = Color(0xFF007ACC),
    junieAccent = Color(0xFF4CAF50),
    thoughtBackground = Color(0xFFFFF8E1),
    thoughtBorder = Color(0xFFFFD54F),
    toolCallBackground = Color(0xFFF3E5F5),
    toolCallBorder = Color(0xFFCE93D8),
    terminalBackground = Color(0xFF263238),
    terminalText = Color(0xFF4CAF50),
    terminalCommand = Color(0xFF4EC9B0),
    codeBackground = Color(0xFFF5F5F5),
    codeBorder = Color(0xFFE0E0E0),
    diffAdded = Color(0xFFE8F5E9),
    diffRemoved = Color(0xFFFFEBEE),
    diffAddedText = Color(0xFF2E7D32),
    diffRemovedText = Color(0xFFC62828),
    diffHunkHeader = Color(0x220000CC),
    errorBackground = Color(0xFFFFEBEE),
    errorBorder = Color(0xFFEF9A9A),
    warningBackground = Color(0xFFFFF8E1),
    warningBorder = Color(0xFFFFE082),
    searchHighlightBackground = Color(0xFFFFF176),
    searchHighlightText = Color(0xFF1B1B1B),
    currentMatchBackground = Color(0xFFFF8F00),
    currentMatchText = Color(0xFFFFFFFF)
)

/** Dark conversation colour tokens — inspired by LogViewer Industrial Dark palette. */
fun darkConversationColors(): ConversationColors = ConversationColors(
    humanAccent = Color(0xFF00A3E0),
    junieAccent = Color(0xFF66BB6A),
    thoughtBackground = Color(0xFF3E2723),
    thoughtBorder = Color(0xFF795548),
    toolCallBackground = Color(0xFF1A237E),
    toolCallBorder = Color(0xFF5C6BC0),
    terminalBackground = Color(0xFF1B1B1B),
    terminalText = Color(0xFF66BB6A),
    terminalCommand = Color(0xFF4EC9B0),
    codeBackground = Color(0xFF2B2B2B),
    codeBorder = Color(0xFF3C3F41),
    diffAdded = Color(0xFF1B3A1B),
    diffRemoved = Color(0xFF3A1B1B),
    diffAddedText = Color(0xFF66BB6A),
    diffRemovedText = Color(0xFFEF5350),
    diffHunkHeader = Color(0x33007ACC),
    errorBackground = Color(0xFF3A1B1B),
    errorBorder = Color(0xFFE57373),
    warningBackground = Color(0xFF3E2723),
    warningBorder = Color(0xFFFFB74D),
    searchHighlightBackground = Color(0xFF827717),
    searchHighlightText = Color(0xFFFFFFFF),
    currentMatchBackground = Color(0xFFFF6F00),
    currentMatchText = Color(0xFFFFFFFF)
)

/** CompositionLocal providing the current [ConversationColors] instance. */
val LocalConversationColors = staticCompositionLocalOf { lightConversationColors() }
