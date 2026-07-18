package com.knowledgespike.junieviewer.data

import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.domain.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.FileSystem
import okio.Path

/**
 * Result emitted by [LiveSessionTracker] when new Messages are parsed from appended content,
 * or when the file is truncated/deleted requiring a full reload.
 */
sealed interface LiveTrackingEvent {
    /** New Messages parsed from appended content. */
    data class NewMessages(val messages: List<Message>) : LiveTrackingEvent

    /** File was truncated or recreated — caller should perform a full reload. */
    data object FileReset : LiveTrackingEvent

    /** File was deleted — caller should stop tracking or enter dormant state. */
    data object FileDeleted : LiveTrackingEvent
}

/**
 * Watches a Session's events.jsonl for new content and incrementally parses
 * appended Events into Messages.
 *
 * Uses [FileWatcher] for polling and reads only newly appended bytes,
 * buffering incomplete final lines across polls.
 */
class LiveSessionTracker(
    private val fileWatcher: FileWatcher = FileWatcher(),
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    private val logger = Logger.withTag("LiveSessionTracker")

    /**
     * Tracks the given [eventsJsonlPath] starting from [initialOffset] bytes.
     * Emits [LiveTrackingEvent]s as new content is appended.
     *
     * @param eventsJsonlPath path to the events.jsonl file
     * @param initialOffset byte offset after initial full load (to avoid re-reading existing content)
     * @param existingMessageCount number of messages already loaded (for id generation)
     */
    fun track(
        eventsJsonlPath: Path,
        initialOffset: Long = 0L,
        existingMessageCount: Int = 0
    ): Flow<LiveTrackingEvent> = flow {
        var partialLineBuffer = ""
        var messageIndex = existingMessageCount

        logger.i { "LiveSessionTracker started: path=$eventsJsonlPath, offset=$initialOffset, existingMessages=$existingMessageCount" }

        fileWatcher.watch(eventsJsonlPath, initialOffset).collect { event ->
            when (event) {
                is FileChangeEvent.Grew -> {
                    val newContent = readBytesFrom(eventsJsonlPath, event.previousSize, event.newSize)
                    if (newContent != null) {
                        val fullText = partialLineBuffer + newContent
                        val lines = fullText.split('\n')

                        // Last element is either empty (if content ended with \n) or a partial line
                        val completeLines = lines.dropLast(1)
                        partialLineBuffer = lines.last()

                        if (partialLineBuffer.isNotEmpty()) {
                            logger.d { "Buffered partial line (${partialLineBuffer.length} chars)" }
                        }

                        val newMessages = mutableListOf<Message>()
                        for (line in completeLines) {
                            if (line.isBlank()) continue
                            JsonlParser.parseLine(line)
                                .onRight { junieEvent ->
                                    val mapped = EventToMessageMapper.mapEventsToMessages(listOf(junieEvent))
                                    // Re-index messages to avoid id collisions with initial load
                                    mapped.forEach { msg ->
                                        newMessages.add(msg.copy(id = "${messageIndex}-live-${msg.id}"))
                                        messageIndex++
                                    }
                                }
                                .onLeft { error ->
                                    logger.w { "Skipping malformed line during live tracking: ${error.message}" }
                                }
                        }

                        if (newMessages.isNotEmpty()) {
                            logger.d { "Emitting ${newMessages.size} new live Messages" }
                            emit(LiveTrackingEvent.NewMessages(newMessages))
                        }
                    }
                }

                is FileChangeEvent.Truncated -> {
                    logger.w { "File truncated — requesting full reload" }
                    partialLineBuffer = ""
                    messageIndex = 0
                    emit(LiveTrackingEvent.FileReset)
                }

                is FileChangeEvent.Deleted -> {
                    logger.w { "File deleted — stopping live tracking" }
                    partialLineBuffer = ""
                    emit(LiveTrackingEvent.FileDeleted)
                }

                is FileChangeEvent.Error -> {
                    logger.e(event.throwable) { "File watch error — continuing polling" }
                }
            }
        }
    }

    /**
     * Reads bytes from [startOffset] to [endOffset] from the given file.
     * Returns the content as a UTF-8 string, or null if reading fails.
     */
    private fun readBytesFrom(path: Path, startOffset: Long, endOffset: Long): String? {
        return try {
            val bytesToRead = (endOffset - startOffset).toInt()
            if (bytesToRead <= 0) return null

            fileSystem.openReadOnly(path).use { handle ->
                val buffer = okio.Buffer()
                handle.source(startOffset).use { source ->
                    source.read(buffer, bytesToRead.toLong())
                }
                buffer.readUtf8()
            }
            
        } catch (e: Exception) {
            logger.e(e) { "Failed to read appended content from $path ($startOffset..$endOffset)" }
            null
        }
    }
}
