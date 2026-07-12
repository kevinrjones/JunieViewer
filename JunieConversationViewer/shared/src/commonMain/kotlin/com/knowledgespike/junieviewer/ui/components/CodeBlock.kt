package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.snipme.kodeview.view.CodeTextView
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxThemes

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
            .heightIn(max = 600.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    )
}
