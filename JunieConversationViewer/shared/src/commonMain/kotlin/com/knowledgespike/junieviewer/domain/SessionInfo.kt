package com.knowledgespike.junieviewer.domain

/**
 * Metadata for a Junie session directory, used to populate the Session selector.
 *
 * @property id         The session directory name (opaque identifier).
 * @property path       Full filesystem path to the session directory.
 * @property lastModified Epoch millis of the last modification time.
 * @property createdAt  Epoch millis of the creation time, if available from the filesystem.
 *                      Null when the platform does not reliably provide birth time.
 * @property workingDirectory The directory Junie was operating in during this session,
 *                            extracted from `AgentStateUpdatedEvent.currentDirectory` in the JSONL.
 *                            Null when no such event is found.
 */
data class SessionInfo(
    val id: String,
    val path: String,
    val lastModified: Long,
    val createdAt: Long? = null,
    val workingDirectory: String? = null
)
