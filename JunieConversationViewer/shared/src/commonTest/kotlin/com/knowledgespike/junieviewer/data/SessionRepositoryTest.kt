package com.knowledgespike.junieviewer.data

import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.domain.TopLevelSearchQuery
import com.knowledgespike.junieviewer.domain.TopLevelSearchStatus
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
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
    fun `given blank top-level search query when searchSessions then it returns empty-query status safely`() = runTest {
        val result = repository.searchSessions(TopLevelSearchQuery("   \n \t"))

        expectThat(result.status).isEqualTo(TopLevelSearchStatus.EmptyQuery)
        expectThat(result.query.normalized).isEqualTo("")
        expectThat(result.sessionResults).hasSize(0)
        expectThat(result.partialFailures).hasSize(0)
    }

    @Test
    fun `given non-blank top-level search query when searchSessions then contract returns structured empty completed result`() = runTest {
        val result = repository.searchSessions(TopLevelSearchQuery("build error"), testDir.toString())

        expectThat(result.status).isEqualTo(TopLevelSearchStatus.Completed)
        expectThat(result.query.normalized).isEqualTo("build error")
        expectThat(result.sessionResults).hasSize(0)
        expectThat(result.partialFailures).hasSize(0)
    }

    @Test
    fun `searchSessions finds matches across multiple sessions with case-insensitive matching and snippets`() = runTest {
        val sessionsDir = testDir / "sessions"
        val session1 = sessionsDir / "session-1"
        val session2 = sessionsDir / "session-2"
        fileSystem.createDirectories(session1)
        fileSystem.createDirectories(session2)

        fileSystem.write(session1 / "events.jsonl") {
            writeUtf8("""{"kind":"UserPromptEvent","requestId":"req-1","prompt":"Error compiling project build failure"}""")
        }
        fileSystem.write(session2 / "events.jsonl") {
            writeUtf8("""{"kind":"UserPromptEvent","requestId":"req-2","prompt":"BUILD FAILURE occurred again"}""")
        }

        val result = repository.searchSessions(TopLevelSearchQuery("build failure"), testDir.toString())

        expectThat(result.status).isEqualTo(TopLevelSearchStatus.Completed)
        expectThat(result.sessionResults).hasSize(2)
        expectThat(result.partialFailures).hasSize(0)
    }

    @Test
    fun `searchSessions handles missing session file as partial failure`() = runTest {
        val sessionsDir = testDir / "sessions"
        val session1 = sessionsDir / "session-missing"
        fileSystem.createDirectories(session1)
        // No events.jsonl created

        val result = repository.searchSessions(TopLevelSearchQuery("hello"), testDir.toString())

        expectThat(result.status).isEqualTo(TopLevelSearchStatus.Completed)
        expectThat(result.sessionResults).hasSize(0)
        expectThat(result.partialFailures).hasSize(1)
        expectThat(result.partialFailures[0].sessionId).isEqualTo("session-missing")
    }

    @Test
    fun `searchSessions handles mixed success and partial failure`() = runTest {
        val sessionsDir = testDir / "sessions"
        val session1 = sessionsDir / "session-match"
        val session2 = sessionsDir / "session-missing"
        fileSystem.createDirectories(session1)
        fileSystem.createDirectories(session2)

        fileSystem.write(session1 / "events.jsonl") {
            writeUtf8("""{"kind":"UserPromptEvent","requestId":"req-1","prompt":"Success match keyword"}""")
        }
        // session-2 has no events.jsonl

        val result = repository.searchSessions(TopLevelSearchQuery("keyword"), testDir.toString())

        expectThat(result.status).isEqualTo(TopLevelSearchStatus.Completed)
        expectThat(result.sessionResults).hasSize(1)
        expectThat(result.sessionResults[0].session.sessionId).isEqualTo("session-match")
        expectThat(result.partialFailures).hasSize(1)
        expectThat(result.partialFailures[0].sessionId).isEqualTo("session-missing")
    }

    @Test
    fun `given a session with a direct currentDirectory field when listSessions then workingDirectory is extracted`() {
        val sessionDir = testDir / "sessions" / "session-direct"
        fileSystem.createDirectories(sessionDir)
        fileSystem.write(sessionDir / "events.jsonl") {
            writeUtf8(
                """
                {"kind":"UserPromptEvent","requestId":"req-1","prompt":"Hello"}
                {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"CurrentDirectoryUpdatedEvent","currentDirectory":"/Users/dev/my-project"}},"timestampMs":1}
                """.trimIndent()
            )
        }

        val result = repository.listSessions(testDir.toString())
        expectThat(result).hasSize(1)
        expectThat(result[0].workingDirectory).isEqualTo("/Users/dev/my-project")
    }

    @Test
    fun `given a session with currentDirectory nested in blob when listSessions then workingDirectory is extracted`() {
        val sessionDir = testDir / "sessions" / "session-blob"
        fileSystem.createDirectories(sessionDir)
        fileSystem.write(sessionDir / "events.jsonl") {
            writeUtf8(
                """
                {"kind":"UserPromptEvent","requestId":"req-1","prompt":"Hello"}
                {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentStateUpdatedEvent","blob":"{\"currentDirectory\":\"/Users/dev/blob-project\",\"other\":1}"}},"timestampMs":1}
                """.trimIndent()
            )
        }

        val result = repository.listSessions(testDir.toString())
        expectThat(result).hasSize(1)
        expectThat(result[0].workingDirectory).isEqualTo("/Users/dev/blob-project")
    }

    @Test
    fun `given a session without a working directory when listSessions then workingDirectory is null`() {
        val sessionDir = testDir / "sessions" / "session-none"
        fileSystem.createDirectories(sessionDir)
        fileSystem.write(sessionDir / "events.jsonl") {
            writeUtf8("""{"kind":"UserPromptEvent","requestId":"req-1","prompt":"Hello"}""")
        }

        val result = repository.listSessions(testDir.toString())
        expectThat(result).hasSize(1)
        expectThat(result[0].workingDirectory).isNull()
    }

    @Test
    fun `given malformed lines before the working directory when listSessions then they are skipped without throwing`() {
        val sessionDir = testDir / "sessions" / "session-malformed"
        fileSystem.createDirectories(sessionDir)
        fileSystem.write(sessionDir / "events.jsonl") {
            writeUtf8(
                """
                this is not json but mentions currentDirectory
                {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentStateUpdatedEvent","blob":"not-json currentDirectory"}},"timestampMs":1}
                {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"CurrentDirectoryUpdatedEvent","currentDirectory":"/Users/dev/after-malformed"}},"timestampMs":2}
                """.trimIndent()
            )
        }

        val result = repository.listSessions(testDir.toString())
        expectThat(result).hasSize(1)
        expectThat(result[0].workingDirectory).isEqualTo("/Users/dev/after-malformed")
    }

    @Test
    fun `given multiple working directory events when listSessions then the first hit wins`() {
        val sessionDir = testDir / "sessions" / "session-first"
        fileSystem.createDirectories(sessionDir)
        fileSystem.write(sessionDir / "events.jsonl") {
            writeUtf8(
                """
                {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"CurrentDirectoryUpdatedEvent","currentDirectory":"/Users/dev/first"}},"timestampMs":1}
                {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"CurrentDirectoryUpdatedEvent","currentDirectory":"/Users/dev/second"}},"timestampMs":2}
                """.trimIndent()
            )
        }

        val result = repository.listSessions(testDir.toString())
        expectThat(result).hasSize(1)
        expectThat(result[0].workingDirectory).isEqualTo("/Users/dev/first")
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
        
        val messages = repository.loadSession(sessionId, testDir.toString()).messages
        
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
        val messages = repository.loadSession("missing", testDir.toString()).messages
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

        val messages = repository.loadSession(sessionId, testDir.toString()).messages

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

        val messages = repository.loadSession(sessionId, testDir.toString()).messages

        // Human prompt + Thought = 2 messages
        // AvailablePullRequestsEvent, LlmResponseMetadataEvent, UserMessagesCommittedToHistory, TaskState are metadata-only
        expectThat(messages).hasSize(2)
        expectThat(messages[0].sender).isEqualTo(Sender.Human)
        expectThat(messages[1].kind).isEqualTo(MessageKind.Thought)
    }

    @Test
    fun `given a session with SystemMessageEvent when getMessages then it renders as SystemMessage`() {
        val sessionId = "test-system"
        val sessionDir = testDir / "sessions" / sessionId
        fileSystem.createDirectories(sessionDir)

        val eventsFile = sessionDir / "events.jsonl"
        fileSystem.write(eventsFile) {
            writeUtf8("""{"kind":"SystemMessageEvent","text":"Free Google AI","details":"Powered by Google"}""")
        }

        val messages = repository.loadSession(sessionId, testDir.toString()).messages

        expectThat(messages).hasSize(1)
        expectThat(messages[0]).and {
            get { sender }.isEqualTo(Sender.Junie)
            get { kind }.isEqualTo(MessageKind.SystemMessage)
            get { content }.isA<MessageContent.Text>()
                .get { text }.isEqualTo("Free Google AI\n\nPowered by Google")
        }
    }

    @Test
    fun `given a session with CancelAgentEvent when getMessages then it renders as Cancelled`() {
        val sessionId = "test-cancel"
        val sessionDir = testDir / "sessions" / sessionId
        fileSystem.createDirectories(sessionDir)

        val eventsFile = sessionDir / "events.jsonl"
        fileSystem.write(eventsFile) {
            writeUtf8("""{"kind":"CancelAgentEvent"}""")
        }

        val messages = repository.loadSession(sessionId, testDir.toString()).messages

        expectThat(messages).hasSize(1)
        expectThat(messages[0].kind).isEqualTo(MessageKind.Cancelled)
        expectThat(messages[0].sender).isEqualTo(Sender.Human)
    }

    @Test
    fun `given a session with UserResponseEvent when getMessages then it renders as Human text`() {
        val sessionId = "test-response"
        val sessionDir = testDir / "sessions" / sessionId
        fileSystem.createDirectories(sessionDir)

        val eventsFile = sessionDir / "events.jsonl"
        fileSystem.write(eventsFile) {
            writeUtf8("""{"kind":"UserResponseEvent","prompt":"Confirm the plan","isChoice":true}""")
        }

        val messages = repository.loadSession(sessionId, testDir.toString()).messages

        expectThat(messages).hasSize(1)
        expectThat(messages[0]).and {
            get { sender }.isEqualTo(Sender.Human)
            get { kind }.isEqualTo(MessageKind.Text)
            get { content }.isA<MessageContent.Text>()
                .get { text }.isEqualTo("Confirm the plan")
        }
    }

    @Test
    fun `given a session with metadata-only new events when getMessages then they are not rendered`() {
        val sessionId = "test-metadata"
        val sessionDir = testDir / "sessions" / sessionId
        fileSystem.createDirectories(sessionDir)

        val eventsFile = sessionDir / "events.jsonl"
        val content = """
            {"kind":"SendToAgentEvent"}
            {"kind":"SessionTitleSetEvent","name":"LogViewer","timestampMs":1000}
            {"kind":"SkillsStatusEvent","newSkills":["android-data-layer"]}
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentStateUpdatedEvent","blob":"{}"}},"timestampMs":100}
        """.trimIndent()

        fileSystem.write(eventsFile) {
            writeUtf8(content)
        }

        val messages = repository.loadSession(sessionId, testDir.toString()).messages

        expectThat(messages).hasSize(0)
    }

    @Test
    fun `given a session with new agent events when getMessages then UI-visible ones render correctly`() {
        val sessionId = "test-new-agent"
        val sessionDir = testDir / "sessions" / sessionId
        fileSystem.createDirectories(sessionDir)

        val eventsFile = sessionDir / "events.jsonl"
        val content = """
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"TestRunBlockUpdatedEvent","name":"MyTest","status":"COMPLETED"}},"timestampMs":1}
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentFailureEvent","message":"Connection failed"}},"timestampMs":2}
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"MarkdownBlockUpdatedEvent","text":"Some markdown"}},"timestampMs":3}
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"McpBlockUpdatedEvent","toolName":"Context7/query","status":"COMPLETED"}},"timestampMs":4}
            {"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"CustomAgentBlockUpdatedEvent","name":"qa-agent","status":"STARTED"}},"timestampMs":5}
        """.trimIndent()

        fileSystem.write(eventsFile) {
            writeUtf8(content)
        }

        val messages = repository.loadSession(sessionId, testDir.toString()).messages

        expectThat(messages).hasSize(5)
        expectThat(messages[0].kind).isEqualTo(MessageKind.TestRun)
        expectThat(messages[1].kind).isEqualTo(MessageKind.Error)
        expectThat(messages[2].kind).isEqualTo(MessageKind.Markdown)
        expectThat(messages[3].kind).isEqualTo(MessageKind.Mcp)
        expectThat(messages[4].kind).isEqualTo(MessageKind.SubAgent)
    }

    @Test
    fun `given a session with AskRequestUpdatedEvent when getMessages then it renders as Question`() {
        val sessionId = "test-ask"
        val sessionDir = testDir / "sessions" / sessionId
        fileSystem.createDirectories(sessionDir)

        val eventsFile = sessionDir / "events.jsonl"
        fileSystem.write(eventsFile) {
            writeUtf8("""{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AskRequestUpdatedEvent","title":"Junie asks","askRequest":{"id":"a1","question":"What next?"},"status":"IN_PROGRESS"}},"timestampMs":1}""")
        }

        val messages = repository.loadSession(sessionId, testDir.toString()).messages

        expectThat(messages).hasSize(1)
        expectThat(messages[0]).and {
            get { kind }.isEqualTo(MessageKind.Question)
            get { content }.isA<MessageContent.Text>()
                .get { text }.isEqualTo("Junie asks\nWhat next?")
        }
    }

    @Test
    fun `given a session with ChoiceRequestUpdatedEvent when getMessages then it renders as Choice`() {
        val sessionId = "test-choice"
        val sessionDir = testDir / "sessions" / sessionId
        fileSystem.createDirectories(sessionDir)

        val eventsFile = sessionDir / "events.jsonl"
        fileSystem.write(eventsFile) {
            writeUtf8("""{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ChoiceRequestUpdatedEvent","title":"How to proceed?","choiceRequest":{"id":"c1","options":[{"id":"Agree","description":"Confirm plan"}]},"status":"IN_PROGRESS"}},"timestampMs":1}""")
        }

        val messages = repository.loadSession(sessionId, testDir.toString()).messages

        expectThat(messages).hasSize(1)
        expectThat(messages[0]).and {
            get { kind }.isEqualTo(MessageKind.Choice)
            get { content }.isA<MessageContent.Text>()
                .get { text }.isEqualTo("How to proceed?\n• Confirm plan\n")
        }
    }
}
