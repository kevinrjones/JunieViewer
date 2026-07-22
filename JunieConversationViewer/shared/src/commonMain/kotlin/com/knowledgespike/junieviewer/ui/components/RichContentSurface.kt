package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.knowledgespike.junieviewer.ui.theme.RICH_CONTENT_BORDER_WIDTH
import com.knowledgespike.junieviewer.ui.theme.RICH_CONTENT_SHAPE

/**
 * Shared "rich content box" modifier chain, previously hand-rolled in each of
 * DiffBlock, StructuredOutputBlock, TerminalOutputBlock, ToolCallBlock, and CodeBlock:
 * fills the available width, clips to [shape], draws a themed border, fills the
 * [backgroundColor], applies [padding], and optionally allows horizontal scrolling
 * for long unwrapped lines (e.g. long diff or terminal lines).
 */
fun Modifier.richContentBox(
    backgroundColor: Color,
    borderColor: Color,
    padding: Dp,
    shape: Shape = RICH_CONTENT_SHAPE,
    borderWidth: Dp = RICH_CONTENT_BORDER_WIDTH,
    scrollable: Boolean = false
): Modifier {
    val styled = fillMaxWidth()
        .clip(shape)
        .border(width = borderWidth, color = borderColor, shape = shape)
        .background(backgroundColor)
        .padding(padding)
    return if (scrollable) {
        styled.composed { horizontalScroll(rememberScrollState()) }
    } else {
        styled
    }
}
