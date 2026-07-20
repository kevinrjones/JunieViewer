package com.knowledgespike.junieviewer.data

import com.knowledgespike.junieviewer.domain.*
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Characterization tests for EventToMessageMapper covering EVERY event type the mapper
 * currently handles — both top-level [JunieEvent] subtypes and nested [AgentEvent] subtypes.
 *
 * These tests lock in CURRENT behaviour (including odd fallbacks such as `toString()`
 * output and hashCode-based ids) ahead of the Sprint 6 restructuring. If behaviour looks
 * strange, that is intentional — assert it as-is and note it in a comment.
 *
 * Existing coverage in EventToMessageMapperTest (AgentTaskFailedEvent, UnknownAgentEvent,
 * CustomAgentBlockUpdatedEvent, SubagentSpawnedEvent) is not duplicated here beyond
 * shape assertions where useful.
 */
class EventToMessageMapperCharacterizationTest {

    /** Wraps a nested agent event in a SessionA2uxEvent for mapping. */
    private fun a2ux(agentEvent: AgentEvent, timestampMs: Long? = 42L): SessionA2uxEvent =
        SessionA2uxEvent(event = AgentEventWrapper(agentEvent = agentEvent), timestampMs = timestampMs)

    /** Maps a single event and returns the resulting messages. */
    private fun map(event: JunieEvent): List<Message> =
        EventToMessageMapper.mapEventsToMessages(listOf(event))

    /** Maps a single event and returns the single resulting message. */
    private fun mapSingle(event: JunieEvent): Message {
        val messages = map(event)
        expectThat(messages).hasSize(1)
        return messages.first()
    }

    // -----------------------------------------------------------------------
    // Top-level events that produce messages
    // -----------------------------------------------------------------------

    @Test
    fun `given a UserPromptEvent when mapped then kind is Text sender is Human and content is the prompt`() {
        val message = mapSingle(UserPromptEvent(prompt = "Fix the bug", requestId = "req-1"))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Text)
            get { sender }.isEqualTo(Sender.Human)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("Fix the bug")
            get { id }.isEqualTo("0-req-1") // id uses index + requestId
        }
    }

    @Test
    fun `given a UserPromptEvent with null requestId when mapped then id uses hashCode fallback`() {
        val event = UserPromptEvent(prompt = "Hello")
        val message = mapSingle(event)

        // Characterization: fallback tag is "prompt-<hashCode>" of the event.
        expectThat(message.id).isEqualTo("0-prompt-${event.hashCode()}")
    }

    @Test
    fun `given an UnknownJunieEvent when mapped then kind is Unsupported with kind name in content`() {
        val message = mapSingle(
            UnknownJunieEvent(kind = "BrandNewEvent", timestampMs = 777L, raw = buildJsonObject { })
        )

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Unsupported)
            get { sender }.isEqualTo(Sender.Junie)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("Unsupported event: BrandNewEvent")
            get { id }.isEqualTo("0-unknown-777") // id uses timestampMs when present
        }
    }

    @Test
    fun `given a SystemMessageEvent with details when mapped then content joins text and details`() {
        val message = mapSingle(SystemMessageEvent(text = "Announcement", details = "More info"))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.SystemMessage)
            get { sender }.isEqualTo(Sender.Junie)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("Announcement\n\nMore info")
        }
    }

    @Test
    fun `given a SystemMessageEvent without details when mapped then content is text only`() {
        val message = mapSingle(SystemMessageEvent(text = "Announcement"))

        expectThat(message.content).isA<MessageContent.Text>().get { text }.isEqualTo("Announcement")
    }

    @Test
    fun `given a CancelAgentEvent when mapped then kind is Cancelled with fixed content`() {
        val message = mapSingle(CancelAgentEvent)

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Cancelled)
            get { sender }.isEqualTo(Sender.Human)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("⛔ Agent cancelled")
            get { id }.isEqualTo("0-cancel")
        }
    }

    @Test
    fun `given a TaskContinueStopped when mapped then kind is Status with fixed content`() {
        val message = mapSingle(TaskContinueStopped)

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Status)
            get { sender }.isEqualTo(Sender.Junie)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("Continue stopped")
            get { id }.isEqualTo("0-continue-stopped")
        }
    }

    @Test
    fun `given a UserResponseEvent when mapped then kind is Text sender is Human`() {
        val message = mapSingle(UserResponseEvent(prompt = "Yes, proceed", isChoice = true))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Text)
            get { sender }.isEqualTo(Sender.Human)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("Yes, proceed")
            get { id }.isEqualTo("0-response")
        }
    }

    // -----------------------------------------------------------------------
    // Top-level metadata-only events — produce NO message
    // -----------------------------------------------------------------------

    @Test
    fun `given metadata-only top-level events when mapped then no messages are produced`() {
        val events: List<JunieEvent> = listOf(
            TaskStartedEvent(taskId = "t1", timestampMs = 1L),
            TaskState(taskId = "t1", state = "RUNNING", timestampMs = 2L),
            UserMessagesCommittedToHistory(requestId = "r1", userMessageIds = listOf("m1"), timestampMs = 3L),
            UserAsyncResponseEvent(requestId = "r1", response = "ok", timestampMs = 4L),
            SendToAgentEvent,
            SessionTitleSetEvent(name = "My Session", timestampMs = 5L),
            SkillsStatusEvent(newSkills = listOf("tdd"))
        )

        expectThat(EventToMessageMapper.mapEventsToMessages(events)).isEmpty()
    }

    // -----------------------------------------------------------------------
    // Agent events that produce messages
    // -----------------------------------------------------------------------

    @Test
    fun `given a ResultBlockUpdatedEvent when mapped then kind is Text with result content`() {
        val message = mapSingle(a2ux(ResultBlockUpdatedEvent(result = "All done"), timestampMs = 100L))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Text)
            get { sender }.isEqualTo(Sender.Junie)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("All done")
            get { id }.isEqualTo("0-100") // agent ids use timestampMs when available
        }
    }

    @Test
    fun `given a ResultBlockUpdatedEvent with blank result when mapped then no message`() {
        expectThat(map(a2ux(ResultBlockUpdatedEvent(result = "   ")))).isEmpty()
        expectThat(map(a2ux(ResultBlockUpdatedEvent(result = null)))).isEmpty()
    }

    @Test
    fun `given an AgentThoughtBlockUpdatedEvent when mapped then kind is Thought`() {
        val message = mapSingle(a2ux(AgentThoughtBlockUpdatedEvent(text = "Thinking about it")))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Thought)
            get { sender }.isEqualTo(Sender.Junie)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("Thinking about it")
        }
    }

    @Test
    fun `given an AgentThoughtBlockUpdatedEvent with blank text when mapped then no message`() {
        expectThat(map(a2ux(AgentThoughtBlockUpdatedEvent(text = "")))).isEmpty()
    }

    @Test
    fun `given an AgentPatchCreatedEvent when mapped then kind is Patch with Diff content`() {
        val message = mapSingle(a2ux(AgentPatchCreatedEvent(patch = "--- a/x\n+++ b/x")))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Patch)
            get { sender }.isEqualTo(Sender.Junie)
            get { content }.isA<MessageContent.Diff>().get { diff }.isEqualTo("--- a/x\n+++ b/x")
        }
    }

    @Test
    fun `given an AgentPatchCreatedEvent with blank patch when mapped then no message`() {
        expectThat(map(a2ux(AgentPatchCreatedEvent(patch = " ")))).isEmpty()
        expectThat(map(a2ux(AgentPatchCreatedEvent(patch = null)))).isEmpty()
    }

    @Test
    fun `given a ToolBlockUpdatedEvent when mapped then kind is Tool with json Code content`() {
        val message = mapSingle(a2ux(ToolBlockUpdatedEvent(toolCall = """{"name":"grep"}""")))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Tool)
            get { sender }.isEqualTo(Sender.Junie)
            get { content }.isA<MessageContent.Code>().and {
                get { code }.isEqualTo("""{"name":"grep"}""")
                get { language }.isEqualTo("json")
            }
        }
    }

    @Test
    fun `given a ToolBlockUpdatedEvent with blank toolCall when mapped then no message`() {
        expectThat(map(a2ux(ToolBlockUpdatedEvent(toolCall = null, text = "ignored")))).isEmpty()
    }

    @Test
    fun `given a TerminalBlockUpdatedEvent with command and output when mapped then kind is Terminal`() {
        val message = mapSingle(a2ux(TerminalBlockUpdatedEvent(command = "ls", output = "file.txt")))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Terminal)
            get { sender }.isEqualTo(Sender.Junie)
            // Characterization: command is prefixed with "$ " and a newline before the output.
            get { content }.isA<MessageContent.Terminal>().get { output }.isEqualTo("$ ls\nfile.txt")
        }
    }

    @Test
    fun `given a TerminalBlockUpdatedEvent with only command when mapped then content ends with newline`() {
        val message = mapSingle(a2ux(TerminalBlockUpdatedEvent(command = "ls")))

        // Characterization: trailing newline is kept when output is missing.
        expectThat(message.content).isA<MessageContent.Terminal>().get { output }.isEqualTo("$ ls\n")
    }

    @Test
    fun `given a TerminalBlockUpdatedEvent with no command or output when mapped then no message`() {
        expectThat(map(a2ux(TerminalBlockUpdatedEvent()))).isEmpty()
    }

    @Test
    fun `given a TestRunBlockUpdatedEvent when mapped then kind is TestRun with emoji label`() {
        val message = mapSingle(a2ux(TestRunBlockUpdatedEvent(name = "jvmTest", status = "PASSED")))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.TestRun)
            get { sender }.isEqualTo(Sender.Junie)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("🧪 Test: jvmTest [PASSED]")
        }
    }

    @Test
    fun `given a TestRunBlockUpdatedEvent with null fields when mapped then content uses unknown fallback`() {
        val message = mapSingle(a2ux(TestRunBlockUpdatedEvent()))

        // Characterization: a message is ALWAYS produced, even with no data.
        expectThat(message.content).isA<MessageContent.Text>().get { text }.isEqualTo("🧪 Test: unknown")
    }

    @Test
    fun `given an McpBlockUpdatedEvent when mapped then kind is Mcp with json Code content`() {
        val message = mapSingle(
            a2ux(McpBlockUpdatedEvent(toolName = "db-query", status = "DONE", details = "rows: 3"))
        )

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Mcp)
            get { sender }.isEqualTo(Sender.Junie)
            // Characterization: plain-text label wrapped in Code content with language "json".
            get { content }.isA<MessageContent.Code>().and {
                get { code }.isEqualTo("MCP: db-query [DONE]\nrows: 3")
                get { language }.isEqualTo("json")
            }
        }
    }

    @Test
    fun `given an McpBlockUpdatedEvent with null fields when mapped then content uses unknown fallback`() {
        val message = mapSingle(a2ux(McpBlockUpdatedEvent()))

        expectThat(message.content).isA<MessageContent.Code>().get { code }.isEqualTo("MCP: unknown")
    }

    @Test
    fun `given an AgentFailureEvent when mapped then kind is Error with message content`() {
        val message = mapSingle(a2ux(AgentFailureEvent(message = "LLM connection lost", errorCode = "E42")))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Error)
            get { sender }.isEqualTo(Sender.Junie)
            // Characterization: errorCode is IGNORED — only the message is rendered.
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("LLM connection lost")
        }
    }

    @Test
    fun `given an AgentFailureEvent with null message when mapped then content uses fallback`() {
        val message = mapSingle(a2ux(AgentFailureEvent()))

        expectThat(message.content).isA<MessageContent.Text>().get { text }.isEqualTo("Agent failure")
    }

    @Test
    fun `given a MarkdownBlockUpdatedEvent when mapped then kind is Markdown with Text content`() {
        val message = mapSingle(a2ux(MarkdownBlockUpdatedEvent(text = "## Heading")))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Markdown)
            get { sender }.isEqualTo(Sender.Junie)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("## Heading")
        }
    }

    @Test
    fun `given a MarkdownBlockUpdatedEvent with blank text when mapped then no message`() {
        expectThat(map(a2ux(MarkdownBlockUpdatedEvent(text = null)))).isEmpty()
    }

    // -----------------------------------------------------------------------
    // AskRequestUpdatedEvent — JsonElement question extraction (~L133–150)
    // -----------------------------------------------------------------------

    @Test
    fun `given an AskRequestUpdatedEvent with title and question when mapped then both appear in content`() {
        val ask = buildJsonObject { put("question", "Proceed with refactor?") }
        val message = mapSingle(a2ux(AskRequestUpdatedEvent(title = "Confirm", askRequest = ask)))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Question)
            get { sender }.isEqualTo(Sender.Junie)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("Confirm\nProceed with refactor?")
        }
    }

    @Test
    fun `given an AskRequestUpdatedEvent with question only when mapped then content is the question`() {
        val ask = buildJsonObject { put("question", "Which module?") }
        val message = mapSingle(a2ux(AskRequestUpdatedEvent(askRequest = ask)))

        expectThat(message.content).isA<MessageContent.Text>().get { text }.isEqualTo("Which module?")
    }

    @Test
    fun `given an AskRequestUpdatedEvent whose askRequest is not an object when mapped then content is raw toString`() {
        // Characterization: non-object payloads trigger the catch branch which appends toString().
        val message = mapSingle(a2ux(AskRequestUpdatedEvent(askRequest = JsonPrimitive("just a string"))))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Question)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("\"just a string\"")
        }
    }

    @Test
    fun `given an AskRequestUpdatedEvent with no title and no question when mapped then no message`() {
        expectThat(map(a2ux(AskRequestUpdatedEvent()))).isEmpty()
        expectThat(map(a2ux(AskRequestUpdatedEvent(askRequest = buildJsonObject { })))).isEmpty()
    }

    // -----------------------------------------------------------------------
    // ChoiceRequestUpdatedEvent — JsonElement options extraction (~L151–172)
    // -----------------------------------------------------------------------

    @Test
    fun `given a ChoiceRequestUpdatedEvent with options when mapped then options are bulleted`() {
        val choice = buildJsonObject {
            putJsonArray("options") {
                add(buildJsonObject { put("id", "a"); put("description", "Refactor now") })
                add(buildJsonObject { put("id", "b") }) // no description — falls back to id
                add(buildJsonObject { }) // neither — falls back to literal "option"
            }
        }
        val message = mapSingle(a2ux(ChoiceRequestUpdatedEvent(title = "Pick one", choiceRequest = choice)))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Choice)
            get { sender }.isEqualTo(Sender.Junie)
            // Characterization: description preferred over id, "option" as last resort, trailing newline kept.
            get { content }.isA<MessageContent.Text>().get { text }
                .isEqualTo("Pick one\n• Refactor now\n• b\n• option\n")
        }
    }

    @Test
    fun `given a ChoiceRequestUpdatedEvent whose choiceRequest is not an object when mapped then content is raw toString`() {
        // Characterization: non-object payloads trigger the catch branch which appends toString().
        val badPayload = buildJsonArray { add("oops") }
        val message = mapSingle(a2ux(ChoiceRequestUpdatedEvent(choiceRequest = badPayload)))

        expectThat(message) {
            get { kind }.isEqualTo(MessageKind.Choice)
            get { content }.isA<MessageContent.Text>().get { text }.isEqualTo("""["oops"]""")
        }
    }

    @Test
    fun `given a ChoiceRequestUpdatedEvent with no title and no options when mapped then no message`() {
        expectThat(map(a2ux(ChoiceRequestUpdatedEvent()))).isEmpty()
        expectThat(map(a2ux(ChoiceRequestUpdatedEvent(choiceRequest = buildJsonObject { })))).isEmpty()
    }

    // -----------------------------------------------------------------------
    // Agent metadata-only events — produce NO message
    // -----------------------------------------------------------------------

    @Test
    fun `given metadata-only agent events when mapped then no messages are produced`() {
        val agentEvents: List<AgentEvent> = listOf(
            AgentCurrentStatusUpdatedEvent,
            AgentTaskNameUpdatedEvent(name = "task"),
            AgentPlanUpdatedEvent(plan = "plan"),
            AvailablePullRequestsEvent(),
            LlmResponseMetadataEvent(model = "gpt", inputTokens = 1, outputTokens = 2),
            CurrentDirectoryUpdatedEvent(directory = "/tmp"),
            EnvironmentVariablesUpdatedEvent(),
            ViewFilesBlockUpdatedEvent(status = "DONE"),
            ContextWindowReportEvent(usedTokens = 10, maxTokens = 100),
            FileChangesBlockUpdatedEvent(status = "DONE"),
            TipSuggestionCreatedEvent(tip = "tip"),
            ShowPlanProgressEvent(),
            NextPromptSuggestionEvent(),
            AskAsyncRequestUpdatedEvent(question = "even with a question, no message"),
            AuthorizationAvailabilityEvent(available = true),
            AgentStartedEvent(agentId = "a1"),
            SuggestPlanEvent(readyForReview = true),
            AgentStateUpdatedEvent(blob = "state")
        )
        val events = agentEvents.map { a2ux(it) }

        expectThat(EventToMessageMapper.mapEventsToMessages(events)).isEmpty()
    }

    // -----------------------------------------------------------------------
    // Id generation characterization
    // -----------------------------------------------------------------------

    @Test
    fun `given an agent event without timestamp when mapped then id uses tag and content hashCode`() {
        val message = mapSingle(a2ux(AgentThoughtBlockUpdatedEvent(text = "no ts"), timestampMs = null))

        // Characterization: id falls back to "<index>-<tag>-<content.hashCode()>".
        expectThat(message.id).isEqualTo("0-thought-${MessageContent.Text("no ts").hashCode()}")
    }

    @Test
    fun `given multiple events when mapped then ids embed the source event index`() {
        val messages = EventToMessageMapper.mapEventsToMessages(
            listOf(
                TaskStartedEvent(), // metadata-only, skipped, but still consumes index 0
                UserPromptEvent(prompt = "hi", requestId = "r1"),
                CancelAgentEvent
            )
        )

        expectThat(messages).hasSize(2)
        expectThat(messages[0].id).isEqualTo("1-r1")
        expectThat(messages[1].id).isEqualTo("2-cancel")
    }
}
