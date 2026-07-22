package com.knowledgespike.junieviewer.ui

import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.LiveTrackingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Immutable snapshot of metadata from the last successful session load.
 * Cached so live tracking can be restarted (e.g. after auto-refresh is re-enabled)
 * without needing to reload the whole session from disk.
 */
data class LoadedSessionMetadata(
    val eventsFilePath: okio.Path? = null,
    val fileSize: Long = 0L,
    val lineCount: Int = 0
)

/**
 * Owns the live-tracking coroutine lifecycle for the currently loaded session: starting and
 * stopping the background tracking job, and remembering the [LoadedSessionMetadata] needed to
 * restart tracking. Keeps this lifecycle concern out of [ConversationViewModel].
 */
class LiveTrackingController(
    private val liveSessionTracker: LiveSessionTracker,
    private val scope: CoroutineScope,
    private val launchContext: CoroutineContext,
    private val logger: Logger
) {
    private var trackingJob: Job? = null

    /** Metadata captured from the most recent successful session load. */
    var lastLoadedMetadata: LoadedSessionMetadata = LoadedSessionMetadata()
        private set

    /** Records [metadata] so a later [start] call can resume tracking without a fresh load. */
    fun rememberLoadedMetadata(metadata: LoadedSessionMetadata) {
        lastLoadedMetadata = metadata
    }

    /**
     * Starts tracking [eventsFilePath] from [initialOffset], dispatching each observed
     * [LiveTrackingEvent] to [onEvent]. No-ops when [eventsFilePath] is null.
     */
    fun start(
        eventsFilePath: okio.Path?,
        initialOffset: Long,
        nextLineNumber: Int,
        onEvent: suspend (LiveTrackingEvent) -> Unit
    ) {
        if (eventsFilePath == null) {
            logger.w { "Cannot start live tracking: no events file path" }
            return
        }

        logger.i { "Starting live tracking: path=$eventsFilePath, offset=$initialOffset, nextLine=$nextLineNumber" }
        trackingJob = scope.launch(launchContext) {
            liveSessionTracker.track(eventsFilePath, initialOffset, nextLineNumber)
                .collect { event -> onEvent(event) }
        }
    }

    /** Cancels the current tracking job if active. */
    fun stop() {
        trackingJob?.let {
            logger.i { "Stopping live tracking" }
            it.cancel()
            trackingJob = null
        }
    }
}
