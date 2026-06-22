package com.knowledgespike.junieviewer.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed interface JunieEvent {
    val kind: String
}

@Serializable
@SerialName("UserPromptEvent")
data class UserPromptEvent(
    val prompt: String,
    val requestId: String
) : JunieEvent {
    override val kind: String get() = "UserPromptEvent"
}

@Serializable
@SerialName("SessionA2uxEvent")
data class SessionA2uxEvent(
    val event: AgentEventWrapper,
    val timestampMs: Long? = null
) : JunieEvent {
    override val kind: String get() = "SessionA2uxEvent"
}

@Serializable
data class AgentEventWrapper(
    val state: String? = null,
    val agentEvent: AgentEvent
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed interface AgentEvent {
    val kind: String
}

@Serializable
@SerialName("AgentThoughtBlockUpdatedEvent")
data class AgentThoughtBlockUpdatedEvent(
    val text: String? = null
) : AgentEvent {
    override val kind: String get() = "AgentThoughtBlockUpdatedEvent"
}

@Serializable
@SerialName("AgentPatchCreatedEvent")
data class AgentPatchCreatedEvent(
    val patch: String? = null
) : AgentEvent {
    override val kind: String get() = "AgentPatchCreatedEvent"
}

@Serializable
@SerialName("ResultBlockUpdatedEvent")
data class ResultBlockUpdatedEvent(
    val result: String? = null
) : AgentEvent {
    override val kind: String get() = "ResultBlockUpdatedEvent"
}

@Serializable
@SerialName("ToolBlockUpdatedEvent")
data class ToolBlockUpdatedEvent(
    val toolCall: String? = null
) : AgentEvent {
    override val kind: String get() = "ToolBlockUpdatedEvent"
}

@Serializable
@SerialName("TerminalBlockUpdatedEvent")
data class TerminalBlockUpdatedEvent(
    val command: String? = null,
    val output: String? = null
) : AgentEvent {
    override val kind: String get() = "TerminalBlockUpdatedEvent"
}

@Serializable
@SerialName("AgentCurrentStatusUpdatedEvent")
data object AgentCurrentStatusUpdatedEvent : AgentEvent {
    override val kind: String get() = "AgentCurrentStatusUpdatedEvent"
}

@Serializable
@SerialName("AgentTaskNameUpdatedEvent")
data class AgentTaskNameUpdatedEvent(val name: String? = null) : AgentEvent {
    override val kind: String get() = "AgentTaskNameUpdatedEvent"
}

@Serializable
@SerialName("AgentPlanUpdatedEvent")
data class AgentPlanUpdatedEvent(val plan: String? = null) : AgentEvent {
    override val kind: String get() = "AgentPlanUpdatedEvent"
}
