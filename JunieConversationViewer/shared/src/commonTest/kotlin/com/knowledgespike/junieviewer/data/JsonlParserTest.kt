package com.knowledgespike.junieviewer.data

import arrow.core.Either
import com.knowledgespike.junieviewer.domain.*
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

/**
 * A single table-driven [JsonlParser] test case: a raw JSONL [jsonLine] input paired with a
 * [verify] lambda that asserts the expected [Either] result. [verify] receives the full
 * [Either] (rather than just the parsed event) so both success cases (asserting on
 * `getOrNull()`) and malformed-input cases (asserting `isLeft()`) share the same table.
 *
 * Adding coverage for a new event shape or edge case only requires one new row in
 * [JsonlParserTest.cases] — no new test method is needed.
 */
data class ParserCase(
    val name: String,
    val jsonLine: String,
    val verify: (Either<Throwable, JunieEvent>) -> Unit
) {
    // Parameterized's `{0}` name template calls toString() on this parameter — override it so
    // JUnit test reports and failures show the readable case name instead of a full data dump.
    override fun toString(): String = name
}

/**
 * Table-driven characterization tests for [JsonlParser].
 *
 * Every row in [cases] runs as its own JUnit test instance (via [Parameterized]), so a failing
 * case reports its descriptive [ParserCase.name] directly in the test output. Coverage mirrors
 * the previous one-test-per-shape suite: every known top-level event, every nested [AgentEvent]
 * shape, minimal/extra-field tolerance, unknown-kind fallbacks, and malformed-line handling.
 */
@RunWith(Parameterized::class)
class JsonlParserTest(private val case: ParserCase) {

    @Test
    fun `parses line as expected`() {
        val result = JsonlParser.parseLine(case.jsonLine)
        case.verify(result)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<ParserCase> = listOf(

            ParserCase(
                name = "given a UserPromptEvent line when parsed then it returns UserPromptEvent",
                jsonLine = """{"kind":"UserPromptEvent","requestId":"req-1","prompt":"Hello"}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<UserPromptEvent>()
                        .and {
                            get { requestId }.isEqualTo("req-1")
                            get { prompt }.isEqualTo("Hello")
                        }
                }
            ),

            ParserCase(
                name = "given a SessionA2uxEvent line with AgentThoughtBlockUpdatedEvent when parsed then it returns SessionA2uxEvent",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"state":"IN_PROGRESS","agentEvent":{"kind":"AgentThoughtBlockUpdatedEvent","text":"Thinking..."}},"timestampMs":123456789}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .and {
                            get { timestampMs }.isEqualTo(123456789L)
                            get { event.agentEvent }.isA<AgentThoughtBlockUpdatedEvent>()
                                .get { text }.isEqualTo("Thinking...")
                        }
                }
            ),

            ParserCase(
                name = "given a SessionA2uxEvent with AgentTaskFailedEvent when parsed then it returns correct event",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentTaskFailedEvent","message":"something failed","errorCode":"ERR_1","taskId":"task-123","stepId":"step-456","details":{"foo":"bar"}}},"timestampMs":789}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .and {
                            get { timestampMs }.isEqualTo(789L)
                            get { event.agentEvent }.isA<AgentTaskFailedEvent>()
                                .and {
                                    get { message }.isEqualTo("something failed")
                                    get { errorCode }.isEqualTo("ERR_1")
                                    get { taskId }.isEqualTo("task-123")
                                    get { stepId }.isEqualTo("step-456")
                                    get { details }.isNotNull()
                                }
                        }
                }
            ),

            ParserCase(
                name = "given a SessionA2uxEvent with AgentTaskFailedEvent with minimal fields when parsed then it returns correct event",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentTaskFailedEvent"}}}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<AgentTaskFailedEvent>()
                        .and {
                            get { message }.isEqualTo(null)
                            get { errorCode }.isEqualTo(null)
                            get { taskId }.isEqualTo(null)
                            get { details }.isEqualTo(null)
                        }
                }
            ),

            ParserCase(
                name = "given a SessionA2uxEvent with AgentTaskFailedEvent with extra fields when parsed then it still works",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentTaskFailedEvent","message":"fail","extra":"field"}}}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<AgentTaskFailedEvent>()
                        .get { message }.isEqualTo("fail")
                }
            ),

            // -- SubagentSpawnedEvent cases --

            ParserCase(
                name = "given a SessionA2uxEvent with SubagentSpawnedEvent when parsed then it returns correct event",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"state":"IN_PROGRESS","agentEvent":{"kind":"SubagentSpawnedEvent","name":"doc-reader","task":"Read files","stepId":"step-1","agent":{"kind":"MainAgent","id":"main","name":"main","type":"LINEAR"}}},"timestampMs":1234}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .and {
                            get { timestampMs }.isEqualTo(1234L)
                            get { event.agentEvent }.isA<SubagentSpawnedEvent>()
                                .and {
                                    get { name }.isEqualTo("doc-reader")
                                    get { task }.isEqualTo("Read files")
                                    get { stepId }.isEqualTo("step-1")
                                    get { agent }.isNotNull()
                                }
                        }
                }
            ),

            ParserCase(
                name = "given a SessionA2uxEvent with SubagentSpawnedEvent with minimal fields when parsed then it returns correct event",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"SubagentSpawnedEvent"}}}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<SubagentSpawnedEvent>()
                        .and {
                            get { name }.isEqualTo(null)
                            get { task }.isEqualTo(null)
                            get { stepId }.isEqualTo(null)
                            get { agent }.isEqualTo(null)
                        }
                }
            ),

            ParserCase(
                name = "given a SessionA2uxEvent with SubagentSpawnedEvent with extra fields when parsed then it still works",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"SubagentSpawnedEvent","name":"explorer","futureField":"value"}}}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<SubagentSpawnedEvent>()
                        .get { name }.isEqualTo("explorer")
                }
            ),

            // -- Phase B: Unknown event fallback cases --

            ParserCase(
                name = "given an unknown top-level event kind when parsed then it returns UnknownJunieEvent",
                jsonLine = """{"kind":"FutureNewEvent","data":"something","timestampMs":999}""",
                verify = { result ->
                    val event = expectThat(result.getOrNull()).isA<UnknownJunieEvent>()
                    event.get { kind }.isEqualTo("FutureNewEvent")
                    event.get { timestampMs }.isEqualTo(999L)
                    event.get { raw }.isNotNull()
                }
            ),

            ParserCase(
                name = "given a SessionA2uxEvent with unknown nested agent event when parsed then it returns UnknownAgentEvent",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"FutureAgentEvent","someField":"value"}},"timestampMs":555}""",
                verify = { result ->
                    val event = expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                    event.get { timestampMs }.isEqualTo(555L)
                    val agentEvent = event.get { this.event.agentEvent }.isA<UnknownAgentEvent>()
                    agentEvent.get { kind }.isEqualTo("FutureAgentEvent")
                    agentEvent.get { raw }.isNotNull()
                }
            ),

            ParserCase(
                name = "given a malformed JSON line when parsed then it returns an error",
                jsonLine = """{"kind":"UserPromptEvent" "missing_colon"}""",
                verify = { result -> expectThat(result.isLeft()).isEqualTo(true) }
            ),

            ParserCase(
                // prompt is required for UserPromptEvent
                name = "given a missing required field when parsed then it returns an error",
                jsonLine = """{"kind":"UserPromptEvent","requestId":"req-1"}""",
                verify = { result -> expectThat(result.isLeft()).isEqualTo(true) }
            ),

            ParserCase(
                name = "given an empty string when parsed then it returns an error",
                jsonLine = "",
                verify = { result -> expectThat(result.isLeft()).isEqualTo(true) }
            ),

            ParserCase(
                name = "given a SessionA2uxEvent with AgentPatchCreatedEvent when parsed then it returns correct event",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentPatchCreatedEvent","patch":"diff --git ..."}}}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<AgentPatchCreatedEvent>()
                        .get { patch }.isEqualTo("diff --git ...")
                }
            ),

            ParserCase(
                name = "given a SessionA2uxEvent with TerminalBlockUpdatedEvent when parsed then it returns correct event",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"TerminalBlockUpdatedEvent","command":"ls","output":"file.txt"}}}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<TerminalBlockUpdatedEvent>()
                        .and {
                            get { command }.isEqualTo("ls")
                            get { output }.isEqualTo("file.txt")
                        }
                }
            ),

            // -- Phase A: Known event class cases --

            ParserCase(
                name = "given a TaskStartedEvent when parsed then it returns TaskStartedEvent",
                jsonLine = """{"kind":"TaskStartedEvent","taskId":"task-123","timestampMs":1000}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<TaskStartedEvent>()
                        .and {
                            get { taskId }.isEqualTo("task-123")
                            get { timestampMs }.isEqualTo(1000L)
                        }
                }
            ),

            ParserCase(
                name = "given a TaskState event when parsed then it returns TaskState",
                jsonLine = """{"kind":"TaskState","taskId":"task-123","state":"COMPLETED","timestampMs":2000}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<TaskState>()
                        .and {
                            get { taskId }.isEqualTo("task-123")
                            get { state }.isEqualTo("COMPLETED")
                        }
                }
            ),

            ParserCase(
                name = "given a UserMessagesCommittedToHistory event when parsed then it returns correct type",
                jsonLine = """{"kind":"UserMessagesCommittedToHistory","requestId":"req-5","timestampMs":3000}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<UserMessagesCommittedToHistory>()
                        .get { requestId }.isEqualTo("req-5")
                }
            ),

            ParserCase(
                name = "given a UserAsyncResponseEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"UserAsyncResponseEvent","requestId":"req-7","response":"approved","timestampMs":4000}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<UserAsyncResponseEvent>()
                        .and {
                            get { requestId }.isEqualTo("req-7")
                            get { response }.isEqualTo("approved")
                        }
                }
            ),

            ParserCase(
                name = "given an AvailablePullRequestsEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AvailablePullRequestsEvent"}},"timestampMs":100}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<AvailablePullRequestsEvent>()
                }
            ),

            ParserCase(
                name = "given an LlmResponseMetadataEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"LlmResponseMetadataEvent","model":"gpt-4","inputTokens":100,"outputTokens":50}},"timestampMs":200}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<LlmResponseMetadataEvent>()
                        .and {
                            get { model }.isEqualTo("gpt-4")
                            get { inputTokens }.isEqualTo(100)
                            get { outputTokens }.isEqualTo(50)
                        }
                }
            ),

            ParserCase(
                name = "given a CurrentDirectoryUpdatedEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"CurrentDirectoryUpdatedEvent","directory":"/home/user"}},"timestampMs":300}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<CurrentDirectoryUpdatedEvent>()
                        .get { directory }.isEqualTo("/home/user")
                }
            ),

            ParserCase(
                name = "given an AgentStartedEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentStartedEvent","agentId":"agent-1"}},"timestampMs":400}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<AgentStartedEvent>()
                        .get { agentId }.isEqualTo("agent-1")
                }
            ),

            ParserCase(
                name = "given a ContextWindowReportEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ContextWindowReportEvent","usedTokens":5000,"maxTokens":8000}},"timestampMs":500}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<ContextWindowReportEvent>()
                        .and {
                            get { usedTokens }.isEqualTo(5000)
                            get { maxTokens }.isEqualTo(8000)
                        }
                }
            ),

            ParserCase(
                name = "given unknown event with extra fields when parsed then raw JSON is preserved",
                jsonLine = """{"kind":"BrandNewEvent","field1":"a","field2":42,"nested":{"x":true}}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<UnknownJunieEvent>()
                        .and {
                            get { kind }.isEqualTo("BrandNewEvent")
                            get { raw.toString() }.isEqualTo("""{"kind":"BrandNewEvent","field1":"a","field2":42,"nested":{"x":true}}""")
                        }
                }
            ),

            ParserCase(
                name = "given known events with extra unknown fields when parsed then ignoreUnknownKeys works",
                jsonLine = """{"kind":"TaskStartedEvent","taskId":"t1","timestampMs":1,"extraField":"ignored"}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<TaskStartedEvent>()
                        .get { taskId }.isEqualTo("t1")
                }
            ),

            // -- New top-level event cases --

            ParserCase(
                name = "given a SystemMessageEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SystemMessageEvent","text":"Free Google AI","details":"Powered by Google"}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SystemMessageEvent>()
                        .and {
                            get { text }.isEqualTo("Free Google AI")
                            get { details }.isEqualTo("Powered by Google")
                        }
                }
            ),

            ParserCase(
                name = "given a SendToAgentEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SendToAgentEvent"}""",
                verify = { result -> expectThat(result.getOrNull()).isA<SendToAgentEvent>() }
            ),

            ParserCase(
                name = "given a CancelAgentEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"CancelAgentEvent"}""",
                verify = { result -> expectThat(result.getOrNull()).isA<CancelAgentEvent>() }
            ),

            ParserCase(
                name = "given a SessionTitleSetEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionTitleSetEvent","name":"LogViewer","timestampMs":1000}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionTitleSetEvent>()
                        .and {
                            get { name }.isEqualTo("LogViewer")
                            get { timestampMs }.isEqualTo(1000L)
                        }
                }
            ),

            ParserCase(
                name = "given a SkillsStatusEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SkillsStatusEvent","newSkills":["android-data-layer","android-testing"]}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SkillsStatusEvent>()
                        .get { newSkills }.isNotNull().hasSize(2)
                }
            ),

            ParserCase(
                name = "given a TaskContinueStopped when parsed then it returns correct type",
                jsonLine = """{"kind":"TaskContinueStopped"}""",
                verify = { result -> expectThat(result.getOrNull()).isA<TaskContinueStopped>() }
            ),

            ParserCase(
                name = "given a UserResponseEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"UserResponseEvent","prompt":"Confirm the plan","isChoice":true}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<UserResponseEvent>()
                        .and {
                            get { prompt }.isEqualTo("Confirm the plan")
                            get { isChoice }.isEqualTo(true)
                        }
                }
            ),

            // -- New agent event cases --

            ParserCase(
                name = "given a TestRunBlockUpdatedEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"TestRunBlockUpdatedEvent","stepId":"s1","status":"IN_PROGRESS","name":"Run test MyTest"}},"timestampMs":100}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<TestRunBlockUpdatedEvent>()
                        .and {
                            get { stepId }.isEqualTo("s1")
                            get { status }.isEqualTo("IN_PROGRESS")
                            get { name }.isEqualTo("Run test MyTest")
                        }
                }
            ),

            ParserCase(
                name = "given a McpBlockUpdatedEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"McpBlockUpdatedEvent","stepId":"s2","toolName":"Context7/resolve-library-id","status":"COMPLETED","details":"{\"libraryName\":\"Ktor\"}"}},"timestampMs":200}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<McpBlockUpdatedEvent>()
                        .and {
                            get { toolName }.isEqualTo("Context7/resolve-library-id")
                            get { status }.isEqualTo("COMPLETED")
                        }
                }
            ),

            ParserCase(
                name = "given a CustomAgentBlockUpdatedEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"CustomAgentBlockUpdatedEvent","stepId":"s3","name":"android-qa-agent","status":"STARTED"}},"timestampMs":300}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<CustomAgentBlockUpdatedEvent>()
                        .and {
                            get { name }.isEqualTo("android-qa-agent")
                            get { status }.isEqualTo("STARTED")
                        }
                }
            ),

            ParserCase(
                name = "given an AgentFailureEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentFailureEvent","message":"Unable to connect","errorCode":"ConnectionFailed"}},"timestampMs":400}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<AgentFailureEvent>()
                        .and {
                            get { message }.isEqualTo("Unable to connect")
                            get { errorCode }.isEqualTo("ConnectionFailed")
                        }
                }
            ),

            ParserCase(
                name = "given an AgentStateUpdatedEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentStateUpdatedEvent","blob":"{\"state\":\"data\"}"}},"timestampMs":500}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<AgentStateUpdatedEvent>()
                        .get { blob }.isNotNull()
                }
            ),

            ParserCase(
                name = "given an AskRequestUpdatedEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AskRequestUpdatedEvent","stepId":"s4","title":"Junie asks","askRequest":{"id":"a1","question":"What next?"},"status":"IN_PROGRESS"}},"timestampMs":600}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<AskRequestUpdatedEvent>()
                        .and {
                            get { title }.isEqualTo("Junie asks")
                            get { status }.isEqualTo("IN_PROGRESS")
                        }
                }
            ),

            ParserCase(
                name = "given a ChoiceRequestUpdatedEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ChoiceRequestUpdatedEvent","stepId":"s5","title":"How to proceed?","choiceRequest":{"id":"c1","options":[{"id":"AgreeWithCode","description":"Confirm"}]},"status":"IN_PROGRESS"}},"timestampMs":700}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<ChoiceRequestUpdatedEvent>()
                        .and {
                            get { title }.isEqualTo("How to proceed?")
                            get { status }.isEqualTo("IN_PROGRESS")
                        }
                }
            ),

            ParserCase(
                name = "given a MarkdownBlockUpdatedEvent when parsed then it returns correct type",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"MarkdownBlockUpdatedEvent","stepId":"s6","text":"No changes were undone."}},"timestampMs":800}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<MarkdownBlockUpdatedEvent>()
                        .get { text }.isEqualTo("No changes were undone.")
                }
            ),

            // -- Field discrepancy cases --

            ParserCase(
                name = "given a UserPromptEvent with presentablePrompt and customAttachments when parsed then fields are present",
                jsonLine = """{"kind":"UserPromptEvent","prompt":"fix tests","presentablePrompt":"fix tests","customAttachments":[]}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<UserPromptEvent>()
                        .and {
                            get { presentablePrompt }.isEqualTo("fix tests")
                            get { customAttachments }.isNotNull()
                        }
                }
            ),

            ParserCase(
                name = "given a ToolBlockUpdatedEvent with stepId and status when parsed then fields are present",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ToolBlockUpdatedEvent","stepId":"s1","text":"search_file","status":"COMPLETED","details":"found 3 results"}},"timestampMs":100}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<ToolBlockUpdatedEvent>()
                        .and {
                            get { stepId }.isEqualTo("s1")
                            get { status }.isEqualTo("COMPLETED")
                            get { text }.isEqualTo("search_file")
                            get { details }.isEqualTo("found 3 results")
                        }
                }
            ),

            ParserCase(
                name = "given a ResultBlockUpdatedEvent with extra fields when parsed then fields are present",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ResultBlockUpdatedEvent","result":"Done","stepId":"s1","cancelled":false,"errorCode":"NONE"}},"timestampMs":100}""",
                verify = { result ->
                    val event = expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<ResultBlockUpdatedEvent>()
                    event.get { this.result }.isEqualTo("Done")
                    event.get { stepId }.isEqualTo("s1")
                    event.get { cancelled }.isEqualTo(false)
                    event.get { errorCode }.isEqualTo("NONE")
                }
            ),

            // -- Regression: agent field as JSON object (viewer.log 2026-07-13) --

            ParserCase(
                name = "given AuthorizationAvailabilityEvent with agent as object when parsed then it succeeds",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"state":"IN_PROGRESS","agentEvent":{"kind":"AuthorizationAvailabilityEvent","agent":{"kind":"MainAgent","id":"main","name":"main"},"authorized":true}},"timestampMs":1783947457317}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<AuthorizationAvailabilityEvent>()
                        .and {
                            get { authorized }.isEqualTo(true)
                            get { agent }.isNotNull()
                        }
                }
            ),

            ParserCase(
                name = "given AvailablePullRequestsEvent with agent as object when parsed then it succeeds",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"state":"IN_PROGRESS","agentEvent":{"kind":"AvailablePullRequestsEvent","agent":{"kind":"MainAgent","id":"main","name":"main"},"pullRequests":[]}},"timestampMs":1783947457391}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<AvailablePullRequestsEvent>()
                        .and {
                            get { agent }.isNotNull()
                            get { pullRequests }.isNotNull()
                        }
                }
            ),

            ParserCase(
                name = "given UserPromptEvent without requestId when parsed then it succeeds",
                jsonLine = """{"kind":"UserPromptEvent","prompt":"quit","presentablePrompt":"quit","customAttachments":[{"kind":"BashCommandAttachment","mode":"Direct"}]}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<UserPromptEvent>()
                        .and {
                            get { prompt }.isEqualTo("quit")
                            get { requestId }.isEqualTo(null)
                        }
                }
            ),

            ParserCase(
                name = "given AgentStartedEvent with agent as object when parsed then it succeeds",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentStartedEvent","agent":{"kind":"MainAgent","id":"main","name":"main"},"stepId":"s1"}},"timestampMs":100}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<AgentStartedEvent>()
                        .and {
                            get { agent }.isNotNull()
                            get { stepId }.isEqualTo("s1")
                        }
                }
            ),

            // -- Crash-fix regression cases --

            ParserCase(
                // Real-world format: suggestion is a JsonArray, not a String
                name = "given a NextPromptSuggestionEvent with array suggestion when parsed then it succeeds",
                jsonLine = """{"kind":"SessionA2uxEvent","event":{"state":"COMPLETED","agentEvent":{"kind":"NextPromptSuggestionEvent","agent":{"kind":"MainAgent","id":"main","name":"main"},"suggestion":[{"text":"update project_memory.md now"}]}},"timestampMs":1782134697259}""",
                verify = { result ->
                    expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
                        .get { event.agentEvent }.isA<NextPromptSuggestionEvent>()
                        .get { suggestion }.isNotNull()
                }
            ),
        )
    }
}

/**
 * Round-trip symmetry checks: JSON generated via the real domain serializers (see
 * `EventSerializers.kt` and the event data classes) must be parseable back into an equal
 * event by [JsonlParser]. These complement — but intentionally do not replace — the
 * raw-JSON-string characterization cases in [JsonlParserTest], which capture the exact shapes
 * seen in real Junie log lines.
 */
class JsonlParserTestRoundTrip {

    @Test
    fun `given a UserPromptEvent encoded via its serializer when parsed then fields round-trip`() {
        val original = UserPromptEvent(prompt = "round trip", requestId = "rt-1")
        val line = encodeWithKind(UserPromptEvent.serializer(), original, "UserPromptEvent")

        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<UserPromptEvent>()
            .and {
                get { prompt }.isEqualTo("round trip")
                get { requestId }.isEqualTo("rt-1")
            }
    }

    @Test
    fun `given a TaskStartedEvent encoded via its serializer when parsed then fields round-trip`() {
        val original = TaskStartedEvent(taskId = "task-rt", timestampMs = 42L)
        val line = encodeWithKind(TaskStartedEvent.serializer(), original, "TaskStartedEvent")

        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<TaskStartedEvent>()
            .and {
                get { taskId }.isEqualTo("task-rt")
                get { timestampMs }.isEqualTo(42L)
            }
    }

    @Test
    fun `given a TaskState encoded via its serializer when parsed then fields round-trip`() {
        val original = TaskState(taskId = "task-rt2", state = "COMPLETED", timestampMs = 100L)
        val line = encodeWithKind(TaskState.serializer(), original, "TaskState")

        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<TaskState>()
            .and {
                get { taskId }.isEqualTo("task-rt2")
                get { state }.isEqualTo("COMPLETED")
            }
    }

    @Test
    fun `given a SkillsStatusEvent encoded via its serializer when parsed then fields round-trip`() {
        val original = SkillsStatusEvent(newSkills = listOf("android-testing", "kotlin-engineer"))
        val line = encodeWithKind(SkillsStatusEvent.serializer(), original, "SkillsStatusEvent")

        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SkillsStatusEvent>()
            .get { newSkills }.isNotNull().hasSize(2)
    }

    @Test
    fun `given a SessionA2uxEvent wrapping AgentThoughtBlockUpdatedEvent encoded via serializers when parsed then it round-trips`() {
        val innerEvent = AgentThoughtBlockUpdatedEvent(text = "round-trip thought", stepId = "step-rt")
        val innerJson = encodeWithKind(AgentThoughtBlockUpdatedEvent.serializer(), innerEvent, "AgentThoughtBlockUpdatedEvent")
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":$innerJson},"timestampMs":999}"""

        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .and {
                get { timestampMs }.isEqualTo(999L)
                get { event.agentEvent }.isA<AgentThoughtBlockUpdatedEvent>()
                    .and {
                        get { text }.isEqualTo("round-trip thought")
                        get { stepId }.isEqualTo("step-rt")
                    }
            }
    }

    companion object {
        /** JSON config mirroring [JsonlParser]'s own settings, used to build round-trip fixtures. */
        private val roundTripJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

        /**
         * Serializes [value] with its real domain [serializer], then injects the `kind`
         * discriminator that [JsonlParser] expects on real JSONL lines. `kind` is not itself a
         * serialized property of the domain data classes — it is always present as literal data
         * in the log file — so it is added here to mimic a genuine log line.
         */
        private fun <T> encodeWithKind(serializer: SerializationStrategy<T>, value: T, kind: String): String {
            val body = roundTripJson.encodeToJsonElement(serializer, value).jsonObject
            return JsonObject(body + ("kind" to JsonPrimitive(kind))).toString()
        }
    }
}
