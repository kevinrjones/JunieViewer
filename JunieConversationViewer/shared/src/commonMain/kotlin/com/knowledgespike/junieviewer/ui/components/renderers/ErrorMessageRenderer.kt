package com.knowledgespike.junieviewer.ui.components.renderers

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.ui.components.ErrorWarningBlock
import com.knowledgespike.junieviewer.ui.components.blockContainsSearchHit

@Composable
fun ErrorMessageRenderer(
    message: Message,
    searchQuery: String,
    isCurrentMatch: Boolean,
    blockExpansionStates: Map<String, Boolean>,
    onToggleBlock: (String) -> Unit
) {
    val errText = when (val c = message.content) {
        is MessageContent.Text -> c.text
        else -> "Unknown error"
    }
    ErrorWarningBlock(
        text = errText,
        isWarning = message.kind == MessageKind.Warning,
        modifier = Modifier.testTag("error_warning_block"),
        searchQuery = searchQuery,
        isCurrentMatch = isCurrentMatch,
        forceExpanded = isCurrentMatch && blockContainsSearchHit(errText, searchQuery)
    )
}
