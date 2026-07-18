package com.knowledgespike.junieviewer.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.buffer
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import com.knowledgespike.junieviewer.domain.MessageContent

@OptIn(ExperimentalCoroutinesApi::class)
class LiveSessionTrackerTest {
    private lateinit var tempDir: okio.Path
    private lateinit var eventsFile: okio.Path
    private val fileSystem = FileSystem.SYSTEM
    private val fileWatcher = FileWatcher(fileSystem = fileSystem, pollIntervalMs = 50L)
    private val tracker = LiveSessionTracker(fileWatcher = fileWatcher, fileSystem = fileSystem)

    private val validEventLine = """{"kind":"UserPromptEvent","prompt":"Hello","requestId":"req-1"}"""

    @Before
    fun setup() {
        tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tracker-test-${System.currentTimeMillis()}"
        fileSystem.createDirectories(tempDir)
        eventsFile = tempDir / "events.jsonl"
    }

    @After
    fun tearDown() {
        fileSystem.deleteRecursively(tempDir)
    }

    @Test
    fun `emits new messages for appended complete lines`() = runTest {
        fileSystem.write(eventsFile) { writeUtf8(validEventLine + "\n") }
        val initialSize = fileSystem.metadata(eventsFile).size!!

        val events = mutableListOf<LiveTrackingEvent>()
        val job = launch {
            tracker.track(eventsFile, initialSize).take(1).toList(events)
        }

        delay(100)
        fileSystem.appendingSink(eventsFile).buffer().use { buffer ->
            buffer.writeUtf8(validEventLine + "\n")
        }

        job.join()

        expectThat(events).hasSize(1)
        val firstEvent = events.first()
        expectThat(firstEvent).isA<LiveTrackingEvent.NewMessages>()
        expectThat((firstEvent as LiveTrackingEvent.NewMessages).messages).hasSize(1)
    }

    @Test
    fun `does not re-emit existing lines from initial load`() = runTest {
        fileSystem.write(eventsFile) { writeUtf8(validEventLine + "\n") }
        val initialSize = fileSystem.metadata(eventsFile).size!!

        val events = mutableListOf<LiveTrackingEvent>()
        val job = launch {
            tracker.track(eventsFile, initialSize).toList(events)
        }

        delay(150) // Wait multiple poll cycles
        job.cancel()

        expectThat(events).hasSize(0)
    }

    @Test
    fun `buffers incomplete final line and emits when completed`() = runTest {
        fileSystem.write(eventsFile) { writeUtf8(validEventLine + "\n") }
        val initialSize = fileSystem.metadata(eventsFile).size!!

        val events = mutableListOf<LiveTrackingEvent>()
        val job = launch {
            tracker.track(eventsFile, initialSize).take(1).toList(events)
        }

        delay(100)
        // Write partial line
        fileSystem.appendingSink(eventsFile).buffer().use { buffer ->
            buffer.writeUtf8("""{"kind":"UserPromptEvent","prompt":"Part""")
        }
        
        delay(100)
        expectThat(events).hasSize(0)

        // Complete the line
        fileSystem.appendingSink(eventsFile).buffer().use { buffer ->
            buffer.writeUtf8("""ial","requestId":"req-2"}""" + "\n")
        }

        job.join()

        expectThat(events).hasSize(1)
        val messages = (events.first() as LiveTrackingEvent.NewMessages).messages
        expectThat(messages.first().content).isA<MessageContent.Text>().get { text }.isEqualTo("Partial")
    }

    @Test
    fun `handles malformed appended line without stopping`() = runTest {
        fileSystem.write(eventsFile) { writeUtf8(validEventLine + "\n") }
        val initialSize = fileSystem.metadata(eventsFile).size!!

        val events = mutableListOf<LiveTrackingEvent>()
        val job = launch {
            tracker.track(eventsFile, initialSize).take(1).toList(events)
        }

        delay(100)
        fileSystem.appendingSink(eventsFile).buffer().use { buffer ->
            buffer.writeUtf8("malformed json\n")
            buffer.writeUtf8(validEventLine + "\n")
        }

        job.join()

        // Should have skipped malformed and emitted the valid one
        expectThat(events).hasSize(1)
        expectThat((events.first() as LiveTrackingEvent.NewMessages).messages).hasSize(1)
    }

    @Test
    fun `emits FileReset on truncation`() = runTest {
        fileSystem.write(eventsFile) { writeUtf8(validEventLine + "\n") }
        val initialSize = fileSystem.metadata(eventsFile).size!!

        val events = mutableListOf<LiveTrackingEvent>()
        val job = launch {
            tracker.track(eventsFile, initialSize).take(1).toList(events)
        }

        delay(100)
        fileSystem.write(eventsFile) { writeUtf8("reset\n") } // Smaller than initial

        job.join()

        expectThat(events).hasSize(1)
        expectThat(events.first()).isA<LiveTrackingEvent.FileReset>()
    }

    @Test
    fun `emits FileDeleted on deletion`() = runTest {
        fileSystem.write(eventsFile) { writeUtf8(validEventLine + "\n") }
        val initialSize = fileSystem.metadata(eventsFile).size!!

        val events = mutableListOf<LiveTrackingEvent>()
        val job = launch {
            tracker.track(eventsFile, initialSize).take(1).toList(events)
        }

        delay(100)
        fileSystem.delete(eventsFile)

        job.join()

        expectThat(events).hasSize(1)
        expectThat(events.first()).isA<LiveTrackingEvent.FileDeleted>()
    }
}
