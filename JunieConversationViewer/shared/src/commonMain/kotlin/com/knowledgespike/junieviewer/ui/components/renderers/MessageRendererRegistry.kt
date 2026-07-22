package com.knowledgespike.junieviewer.ui.components.renderers

import androidx.compose.runtime.Composable
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageKind

/**
 * Signature for a message content renderer.
 */
typealias MessageRenderer = @Composable (
    message: Message,
    searchQuery: String,
    isCurrentMatch: Boolean,
    blockExpansionStates: Map<String, Boolean>,
    onToggleBlock: (String) -> Unit
) -> Unit

/**
 * Registry of [MessageRenderer]s for every [MessageKind].
 * Replaces exhaustive 'when' chains in MessageItems.kt (F1).
 */
object MessageRendererRegistry {
    private val renderers: Map<MessageKind, MessageRenderer> = mapOf(
        MessageKind.Thought to { m, q, c, s, t -> ThoughtMessageRenderer(m, q, c, s, t) },
        MessageKind.Error to { m, q, c, s, t -> ErrorMessageRenderer(m, q, c, s, t) },
        MessageKind.Warning to { m, q, c, s, t -> ErrorMessageRenderer(m, q, c, s, t) },
        MessageKind.Tool to { m, q, c, s, t -> ToolMessageRenderer(m, q, c, s, t) },
        MessageKind.Mcp to { m, q, c, s, t -> ToolMessageRenderer(m, q, c, s, t) },
        MessageKind.Markdown to { m, q, c, s, t -> MarkdownMessageRenderer(m, q, c, s, t) },
        MessageKind.SubAgent to { m, q, c, s, t -> SubAgentMessageRenderer(m, q, c, s, t) },
        MessageKind.Unsupported to { m, q, c, s, t -> UnsupportedMessageRenderer(m, q, c, s, t) },

        // Default content-based renderers
        MessageKind.Text to { m, q, c, s, t -> DefaultMessageRenderer(m, q, c, s, t) },
        MessageKind.Patch to { m, q, c, s, t -> DefaultMessageRenderer(m, q, c, s, t) },
        MessageKind.Terminal to { m, q, c, s, t -> DefaultMessageRenderer(m, q, c, s, t) },
        MessageKind.StructuredOutput to { m, q, c, s, t -> DefaultMessageRenderer(m, q, c, s, t) },
        MessageKind.TestRun to { m, q, c, s, t -> DefaultMessageRenderer(m, q, c, s, t) },
        MessageKind.Question to { m, q, c, s, t -> DefaultMessageRenderer(m, q, c, s, t) },
        MessageKind.Choice to { m, q, c, s, t -> DefaultMessageRenderer(m, q, c, s, t) },
        MessageKind.SystemMessage to { m, q, c, s, t -> DefaultMessageRenderer(m, q, c, s, t) },
        MessageKind.Cancelled to { m, q, c, s, t -> DefaultMessageRenderer(m, q, c, s, t) },
        MessageKind.Status to { m, q, c, s, t -> DefaultMessageRenderer(m, q, c, s, t) }
    )

    /**
     * Renders a [Message] using the registered renderer for its [MessageKind].
     */
    @Composable
    fun Render(
        message: Message,
        searchQuery: String,
        isCurrentMatch: Boolean,
        blockExpansionStates: Map<String, Boolean>,
        onToggleBlock: (String) -> Unit
    ) {
        val renderer = renderers[message.kind] ?: { m, q, c, s, t -> DefaultMessageRenderer(m, q, c, s, t) }
        renderer(message, searchQuery, isCurrentMatch, blockExpansionStates, onToggleBlock)
    }
}
