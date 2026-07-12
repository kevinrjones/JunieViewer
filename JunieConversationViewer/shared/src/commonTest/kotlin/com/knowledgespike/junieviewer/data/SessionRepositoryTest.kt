package com.knowledgespike.junieviewer.data

import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import kotlin.random.Random

class SessionRepositoryTest {

    private val fileSystem = FileSystem.SYSTEM
    private val repository = SessionRepositoryImpl(fileSystem)
    private lateinit var testDir: Path

    @Before
    fun setup() {
        testDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "repo-test-${System.currentTimeMillis()}-${Random.nextInt()}"
        fileSystem.createDirectories(testDir)
    }

    @After
    fun tearDown() {
        if (::testDir.isInitialized) {
            fileSystem.deleteRecursively(testDir)
        }
    }

    @Test
    fun `given non-existent directory when listSessions then it returns empty list`() {
        val result = repository.listSessions((testDir / "non-existent").toString())
        expectThat(result).hasSize(0)
    }

    @Test
    fun `given sessions directory when listSessions then it returns sorted sessions`() {
        val sessionsDir = testDir / "sessions"
        fileSystem.createDirectories(sessionsDir)
        
        val session1 = sessionsDir / "session-1"
        val session2 = sessionsDir / "session-2"
        
        fileSystem.createDirectory(session1)
        Thread.sleep(100) // Ensure different timestamps on some file systems
        fileSystem.createDirectory(session2)

        val result = repository.listSessions(testDir.toString())
        expectThat(result).hasSize(2)
        expectThat(result[0].id).isEqualTo("session-2")
        expectThat(result[1].id).isEqualTo("session-1")
    }

    @Test
    fun `given a session with events when getMessages then it returns mapped messages`() {
        val sessionId = "test-session"
        val sessionDir = testDir / "sessions" / sessionId
        fileSystem.createDirectories(sessionDir)
        
        val eventsFile = sessionDir / "events.jsonl"
        val content = """
            {"kind":"UserPromptEvent","requestId":"req-1","prompt":"Hello"}
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentThoughtBlockUpdatedEvent","text":"Thinking"}}}
        """.trimIndent()
        
        fileSystem.write(eventsFile) {
            writeUtf8(content)
        }
        
        repository.setSession(sessionId, testDir.toString())
        val messages = repository.getMessages()
        
        expectThat(messages).hasSize(2)
        expectThat(messages[0]).and {
            get { sender }.isEqualTo(Sender.Human)
            get { kind }.isEqualTo(MessageKind.Text)
        }
        expectThat(messages[1]).and {
            get { sender }.isEqualTo(Sender.Junie)
            get { kind }.isEqualTo(MessageKind.Thought)
        }
    }

    @Test
    fun `given session file does not exist when getMessages then it returns empty list`() {
        repository.setSession("missing", testDir.toString())
        val messages = repository.getMessages()
        expectThat(messages).hasSize(0)
    }

    @Test
    fun `given a session with unknown events when getMessages then unknown events appear as unsupported messages`() {
        val sessionId = "test-unknown"
        val sessionDir = testDir / "sessions" / sessionId
        fileSystem.createDirectories(sessionDir)

        val eventsFile = sessionDir / "events.jsonl"
        val content = """
            {"kind":"UserPromptEvent","requestId":"req-1","prompt":"Hello"}
            {"kind":"TaskStartedEvent","taskId":"task-1","timestampMs":100}
            {"kind":"FutureTopLevelEvent","data":"something","timestampMs":200}
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ResultBlockUpdatedEvent","result":"Done"}},"timestampMs":300}
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"FutureAgentEvent","x":1}},"timestampMs":400}
        """.trimIndent()

        fileSystem.write(eventsFile) {
            writeUtf8(content)
        }

        repository.setSession(sessionId, testDir.toString())
        val messages = repository.getMessages()

        // Human prompt + result + unknown top-level + unknown nested = 4 messages
        // TaskStartedEvent is metadata-only (no message)
        expectThat(messages).hasSize(4)
        expectThat(messages[0].sender).isEqualTo(Sender.Human)
        expectThat(messages[0].kind).isEqualTo(MessageKind.Text)

        expectThat(messages[1].kind).isEqualTo(MessageKind.Unsupported)
        expectThat(messages[1].content).isA<MessageContent.Text>()
            .get { text }.isEqualTo("Unsupported event: FutureTopLevelEvent")

        expectThat(messages[2].sender).isEqualTo(Sender.Junie)
        expectThat(messages[2].kind).isEqualTo(MessageKind.Text)

        expectThat(messages[3].kind).isEqualTo(MessageKind.Unsupported)
        expectThat(messages[3].content).isA<MessageContent.Text>()
            .get { text }.isEqualTo("Unsupported event: FutureAgentEvent")
    }

    @Test
    fun `given a session with mixed known and unknown events when getMessages then no events are silently dropped`() {
        val sessionId = "test-mixed"
        val sessionDir = testDir / "sessions" / sessionId
        fileSystem.createDirectories(sessionDir)

        val eventsFile = sessionDir / "events.jsonl"
        val content = """
            {"kind":"UserPromptEvent","requestId":"req-1","prompt":"Hi"}
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentThoughtBlockUpdatedEvent","text":"Thinking"}},"timestampMs":1}
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AvailablePullRequestsEvent"}},"timestampMs":2}
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"LlmResponseMetadataEvent","model":"gpt-4"}},"timestampMs":3}
            {"kind":"UserMessagesCommittedToHistory","requestId":"req-1","timestampMs":4}
            {"kind":"TaskState","taskId":"t1","state":"DONE","timestampMs":5}
        """.trimIndent()

        fileSystem.write(eventsFile) {
            writeUtf8(content)
        }

        repository.setSession(sessionId, testDir.toString())
        val messages = repository.getMessages()

        // Human prompt + Thought = 2 messages
        // AvailablePullRequestsEvent, LlmResponseMetadataEvent, UserMessagesCommittedToHistory, TaskState are metadata-only
        expectThat(messages).hasSize(2)
        expectThat(messages[0].sender).isEqualTo(Sender.Human)
        expectThat(messages[1].kind).isEqualTo(MessageKind.Thought)
    }
}
