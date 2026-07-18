package com.knowledgespike.junieviewer.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FileWatcherTest {
    private lateinit var tempDir: okio.Path
    private lateinit var testFile: okio.Path
    private val fileSystem = FileSystem.SYSTEM
    private val fileWatcher = FileWatcher(fileSystem = fileSystem, pollIntervalMs = 50L)

    @Before
    fun setup() {
        tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "filewatcher-test-${System.currentTimeMillis()}"
        fileSystem.createDirectories(tempDir)
        testFile = tempDir / "test.txt"
    }

    @After
    fun tearDown() {
        fileSystem.deleteRecursively(tempDir)
    }

    @Test
    fun `emits Grew when file size increases`() = runTest {
        fileSystem.write(testFile) { writeUtf8("initial") }
        val initialSize = fileSystem.metadata(testFile).size!!

        val events = mutableListOf<FileChangeEvent>()
        val job = launch {
            fileWatcher.watch(testFile, initialSize).take(1).toList(events)
        }

        // Give it a moment to start polling
        kotlinx.coroutines.delay(100)
        fileSystem.write(testFile) { writeUtf8("initial content plus more") }
        val newSize = fileSystem.metadata(testFile).size!!

        job.join()

        expectThat(events).hasSize(1)
        expectThat(events.first()).isA<FileChangeEvent.Grew>().and {
            get { path }.isEqualTo(testFile)
            get { previousSize }.isEqualTo(initialSize)
            get { newSize }.isEqualTo(newSize)
        }
    }

    @Test
    fun `emits Truncated when file size decreases`() = runTest {
        fileSystem.write(testFile) { writeUtf8("initial content") }
        val initialSize = fileSystem.metadata(testFile).size!!

        val events = mutableListOf<FileChangeEvent>()
        val job = launch {
            fileWatcher.watch(testFile, initialSize).take(1).toList(events)
        }

        kotlinx.coroutines.delay(100)
        fileSystem.write(testFile) { writeUtf8("shorter") }
        val newSize = fileSystem.metadata(testFile).size!!

        job.join()

        expectThat(events).hasSize(1)
        expectThat(events.first()).isA<FileChangeEvent.Truncated>().and {
            get { path }.isEqualTo(testFile)
            get { previousSize }.isEqualTo(initialSize)
            get { newSize }.isEqualTo(newSize)
        }
    }

    @Test
    fun `emits Deleted when file disappears`() = runTest {
        fileSystem.write(testFile) { writeUtf8("content") }
        val initialSize = fileSystem.metadata(testFile).size!!

        val events = mutableListOf<FileChangeEvent>()
        val job = launch {
            fileWatcher.watch(testFile, initialSize).take(1).toList(events)
        }

        kotlinx.coroutines.delay(100)
        fileSystem.delete(testFile)

        job.join()

        expectThat(events).hasSize(1)
        expectThat(events.first()).isA<FileChangeEvent.Deleted>().and {
            get { path }.isEqualTo(testFile)
        }
    }

    @Test
    fun `handles missing file gracefully`() = runTest {
        val missingFile = tempDir / "missing.txt"
        val events = mutableListOf<FileChangeEvent>()
        val job = launch {
            fileWatcher.watch(missingFile).take(1).toList(events)
        }

        kotlinx.coroutines.delay(150)
        fileSystem.write(missingFile) { writeUtf8("hello") }
        
        job.join()
        
        expectThat(events).hasSize(1)
        expectThat(events.first()).isA<FileChangeEvent.Grew>()
    }

    @Test
    fun `cancels cleanly`() = runTest {
        val job = launch {
            fileWatcher.watch(testFile).collect { }
        }
        kotlinx.coroutines.delay(100)
        job.cancel()
        expectThat(job.isCancelled).isTrue()
    }
}
