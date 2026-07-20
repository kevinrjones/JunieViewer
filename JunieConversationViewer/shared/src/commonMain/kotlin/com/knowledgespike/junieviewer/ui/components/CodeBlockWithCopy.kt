package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.selection.SelectionContainer
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme
import dev.snipme.highlights.model.SyntaxLanguage

/**
 * Syntax-highlighted code block in a collapsible wrapper with copy affordance.
 * Expanded by default; full content visible without truncation.
 */
@Composable
fun CodeBlockWithCopy(
    code: String,
    language: SyntaxLanguage = SyntaxLanguage.KOTLIN,
    modifier: Modifier = Modifier,
    forceExpanded: Boolean = false,
    externalExpanded: Boolean? = null,
    onToggle: (() -> Unit)? = null
) {
    val colors = JunieViewerTheme.conversationColors

    CollapsibleBlock(
        label = "Code",
        backgroundColor = colors.codeBackground,
        borderColor = colors.codeBorder,
        headerTestTag = "code_block_header",
        bodyTestTag = "code_block_body",
        forceExpanded = forceExpanded,
        externalExpanded = externalExpanded,
        onToggle = onToggle,
        headerTrailing = {
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = code)
        },
        body = {
            SelectionContainer(modifier = Modifier.testTag("selectable_code_content")) {
                CodeBlock(code = code, language = language)
            }
        },
        modifier = modifier
    )
}
