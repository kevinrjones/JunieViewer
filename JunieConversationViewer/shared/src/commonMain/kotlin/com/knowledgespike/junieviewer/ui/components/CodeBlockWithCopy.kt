package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.snipme.highlights.model.SyntaxLanguage

/**
 * Syntax-highlighted code block with a copy-to-clipboard affordance.
 * Delegates rendering to [CodeBlock] and adds a [CopyButton] that copies clean plain text.
 */
@Composable
fun CodeBlockWithCopy(
    code: String,
    language: SyntaxLanguage = SyntaxLanguage.KOTLIN,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = code)
        }
        CodeBlock(code = code, language = language)
    }
}
