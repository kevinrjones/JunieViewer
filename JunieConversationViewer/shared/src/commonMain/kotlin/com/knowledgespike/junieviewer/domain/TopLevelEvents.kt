package com.knowledgespike.junieviewer.domain

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Top-level events in events.jsonl
// ---------------------------------------------------------------------------

/** Human prompt submitted to Junie. */
@Serializable
data class UserPromptEvent(
    val prompt: String,
    val requestId: String? = null,
    val presentablePrompt: String? = null,
    val customAttachments: PayloadValue? = null
) : JunieEvent {
    override fun toMessage(context: MappingContext): Message = Message(
        id = "line-${context.lineNumber}",
        sender = Sender.Human,
        content = MessageContent.Text(prompt),
        kind = MessageKind.Text
    )
}

/** Wrapper for nested agent events within a session. */
@Serializable
data class SessionA2uxEvent(
    val event: AgentEventWrapper,
    val timestampMs: Long? = null
) : JunieEvent {
    override fun toMessage(context: MappingContext): Message? =
        event.agentEvent.toMessage(context.copy(timestampMs = timestampMs))
}

/** Indicates a Junie task has started. */
@Serializable
data class TaskStartedEvent(
    val taskId: String? = null,
    val timestampMs: Long? = null
) : JunieEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Represents a change in Junie task state. */
@Serializable
data class TaskState(
    val taskId: String? = null,
    val state: String? = null,
    val timestampMs: Long? = null
) : JunieEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Records that user messages have been committed to conversation history. */
@Serializable
data class UserMessagesCommittedToHistory(
    val requestId: String? = null,
    val userMessageIds: List<String>? = null,
    val timestampMs: Long? = null
) : JunieEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Async response event from the user (e.g. HITL approval). */
@Serializable
data class UserAsyncResponseEvent(
    val requestId: String? = null,
    val response: String? = null,
    val entries: List<ResponseEntry>? = null,
    val timestampMs: Long? = null
) : JunieEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** System-level message displayed to the user (announcements, notifications). */
@Serializable
data class SystemMessageEvent(
    val text: String,
    val details: String? = null
) : JunieEvent {
    override fun toMessage(context: MappingContext): Message {
        val content = buildString {
            append(text)
            if (!details.isNullOrBlank()) append("\n\n$details")
        }
        return Message(
            id = "line-${context.lineNumber}",
            sender = Sender.Junie,
            content = MessageContent.Text(content),
            kind = MessageKind.SystemMessage
        )
    }
}

/** Signals that a message/task is being sent to the agent. */
@Serializable
data object SendToAgentEvent : JunieEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Signals that the user cancelled the agent's current operation. */
@Serializable
data object CancelAgentEvent : JunieEvent {
    override fun toMessage(context: MappingContext): Message = Message(
        id = "line-${context.lineNumber}",
        sender = Sender.Human,
        content = MessageContent.Text("⛔ Agent cancelled"),
        kind = MessageKind.Cancelled
    )
}

/** Sets or updates the session title. */
@Serializable
data class SessionTitleSetEvent(
    val name: String,
    val timestampMs: Long? = null
) : JunieEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Reports which agent skills were newly discovered/loaded. */
@Serializable
data class SkillsStatusEvent(
    val newSkills: List<String>? = null
) : JunieEvent {
    override fun toMessage(context: MappingContext): Message? = null
}

/** Indicates that a "continue" operation on a task was stopped. */
@Serializable
data object TaskContinueStopped : JunieEvent {
    override fun toMessage(context: MappingContext): Message = Message(
        id = "line-${context.lineNumber}",
        sender = Sender.Junie,
        content = MessageContent.Text("Continue stopped"),
        kind = MessageKind.Status
    )
}

/** User's response to a choice or question from the agent. */
@Serializable
data class UserResponseEvent(
    val prompt: String,
    val isChoice: Boolean = false
) : JunieEvent {
    override fun toMessage(context: MappingContext): Message = Message(
        id = "line-${context.lineNumber}",
        sender = Sender.Human,
        content = MessageContent.Text(prompt),
        kind = MessageKind.Text
    )
}

/**
 * Fallback for any top-level event kind not yet modelled.
 * Preserves the raw JSON so no data is lost.
 */
data class UnknownJunieEvent(
    override val kind: String,
    val timestampMs: Long? = null,
    val raw: PayloadValue.ObjectValue
) : JunieEvent {
    override fun toMessage(context: MappingContext): Message = Message(
        id = "line-${context.lineNumber}",
        sender = Sender.Junie,
        content = MessageContent.Text("Unsupported event: $kind"),
        kind = MessageKind.Unsupported
    )
}
