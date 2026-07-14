package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import dev.snipme.kodeview.view.CodeTextView
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxThemes

/** Shape for rich content blocks — 6dp rounded corners per Sprint 3 section 12.3. */
internal val RICH_CONTENT_SHAPE = RoundedCornerShape(6.dp)

/** Border width for rich content blocks — 1dp per Sprint 3 section 12.3. */
internal val RICH_CONTENT_BORDER_WIDTH = 1.dp

/** Alias for code block shape. */
private val CODE_BLOCK_SHAPE = RICH_CONTENT_SHAPE

/** Alias for code block border width. */
private val CODE_BLOCK_BORDER_WIDTH = RICH_CONTENT_BORDER_WIDTH

/** Maximum height for code blocks to prevent infinite-height measurement in LazyColumn. */
private val CODE_BLOCK_MAX_HEIGHT = 600.dp

/**
 * Renders a syntax-highlighted code block.
 * Uses [heightIn] with a max bound to prevent infinite-height measurement crashes
 * when hosted inside a [LazyColumn] (CodeTextView uses internal vertical scrolling).
 */
@Composable
fun CodeBlock(
    code: String,
    language: SyntaxLanguage = SyntaxLanguage.KOTLIN,
    modifier: Modifier = Modifier
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing

    val highlights = remember(code, language) {
        Highlights.Builder()
            .code(code)
            .language(language)
            .theme(SyntaxThemes.default(darkMode = false))
            .build()
    }

    CodeTextView(
        highlights = highlights,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = CODE_BLOCK_MAX_HEIGHT)
            .clip(CODE_BLOCK_SHAPE)
            .border(width = CODE_BLOCK_BORDER_WIDTH, color = colors.codeBorder, shape = CODE_BLOCK_SHAPE)
            .background(colors.codeBackground)
            .padding(spacing.md)
    )
}
