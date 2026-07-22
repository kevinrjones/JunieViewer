package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import dev.snipme.kodeview.view.CodeTextView
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxThemes

/** Maximum height for code blocks to prevent infinite-height measurement in LazyColumn. */
private val CODE_BLOCK_MAX_HEIGHT = 600.dp

/**
 * Renders a syntax-highlighted code block.
 * Uses [heightIn] with a max bound to prevent infinite-height measurement crashes
 * when hosted inside a [LazyColumn] (CodeTextView uses internal vertical scrolling).
 * The syntax theme follows the app's active dark/light theme.
 */
@Composable
fun CodeBlock(
    code: String,
    language: SyntaxLanguage = SyntaxLanguage.KOTLIN,
    modifier: Modifier = Modifier
) {
    val colors = JunieViewerTheme.conversationColors
    val spacing = JunieViewerTheme.spacing
    val darkMode = JunieViewerTheme.isDark

    val highlights = remember(code, language, darkMode) {
        Highlights.Builder()
            .code(code)
            .language(language)
            .theme(SyntaxThemes.default(darkMode = darkMode))
            .build()
    }

    CodeTextView(
        highlights = highlights,
        modifier = modifier
            .heightIn(max = CODE_BLOCK_MAX_HEIGHT)
            .richContentBox(
                backgroundColor = colors.codeBackground,
                borderColor = colors.codeBorder,
                padding = spacing.md
            )
    )
}
