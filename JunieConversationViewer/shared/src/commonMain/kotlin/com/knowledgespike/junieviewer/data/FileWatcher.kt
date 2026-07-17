package com.knowledgespike.junieviewer.data

import co.touchlab.kermit.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.FileSystem
import okio.Path

/**
 * Represents a change detected on a watched file.
 */
sealed interface FileChangeEvent {
    /** File size increased — new content was appended. */
    data class Grew(val path: Path, val previousSize: Long, val newSize: Long) : FileChangeEvent

    /** File size decreased — file was truncated or replaced. */
    data class Truncated(val path: Path, val previousSize: Long, val newSize: Long) : FileChangeEvent

    /** File was deleted or is no longer accessible. */
    data class Deleted(val path: Path) : FileChangeEvent

    /** An error occurred while checking the file. */
    data class Error(val path: Path, val throwable: Throwable) : FileChangeEvent
}

/**
 * Watches a file for size changes using a polling approach.
 * Emits [FileChangeEvent]s when the file grows, shrinks, is deleted, or errors occur.
 */
class FileWatcher(
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val pollIntervalMs: Long = DEFAULT_POLL_INTERVAL_MS
) {
    private val logger = Logger.withTag("FileWatcher")

    /**
     * Polls the given [path] at a fixed interval and emits [FileChangeEvent]s
     * when the file size changes, the file is deleted, or an error occurs.
     *
     * @param path the file to watch
     * @param initialSize the known file size after initial load (avoids emitting for existing content)
     */
    fun watch(path: Path, initialSize: Long = 0L): Flow<FileChangeEvent> = flow {
        var lastKnownSize = initialSize
        var fileExisted = fileSystem.exists(path)
        logger.i { "FileWatcher started: path=$path, initialSize=$initialSize" }

        while (true) {
            delay(pollIntervalMs)
            try {
                if (!fileSystem.exists(path)) {
                    if (fileExisted) {
                        logger.w { "File deleted: $path" }
                        emit(FileChangeEvent.Deleted(path))
                        fileExisted = false
                    }
                    continue
                }

                // File appeared or still exists
                if (!fileExisted) {
                    // File was recreated — treat as truncation/reset
                    logger.i { "File appeared/recreated: $path" }
                    fileExisted = true
                    lastKnownSize = 0L
                }

                val currentSize = fileSystem.metadata(path).size ?: continue

                when {
                    currentSize > lastKnownSize -> {
                        logger.d { "File grew: $path ($lastKnownSize -> $currentSize)" }
                        emit(FileChangeEvent.Grew(path, lastKnownSize, currentSize))
                        lastKnownSize = currentSize
                    }
                    currentSize < lastKnownSize -> {
                        logger.w { "File truncated: $path ($lastKnownSize -> $currentSize)" }
                        emit(FileChangeEvent.Truncated(path, lastKnownSize, currentSize))
                        lastKnownSize = currentSize
                    }
                    // currentSize == lastKnownSize -> no change, skip
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                logger.e(e) { "Error polling file: $path" }
                emit(FileChangeEvent.Error(path, e))
            }
        }
    }

    companion object {
        /** Default polling interval in milliseconds. */
        const val DEFAULT_POLL_INTERVAL_MS = 1500L
    }
}
