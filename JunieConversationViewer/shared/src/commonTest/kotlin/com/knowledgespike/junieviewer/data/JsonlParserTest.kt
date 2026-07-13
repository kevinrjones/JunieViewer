package com.knowledgespike.junieviewer.data

import com.knowledgespike.junieviewer.domain.*
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class JsonlParserTest {

    @Test
    fun `given a UserPromptEvent line when parsed then it returns UserPromptEvent`() {
        val line = """{"kind":"UserPromptEvent","requestId":"req-1","prompt":"Hello"}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<UserPromptEvent>()
            .and {
                get { requestId }.isEqualTo("req-1")
                get { prompt }.isEqualTo("Hello")
            }
    }

    @Test
    fun `given a SessionA2uxEvent line with AgentThoughtBlockUpdatedEvent when parsed then it returns SessionA2uxEvent`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"state":"IN_PROGRESS","agentEvent":{"kind":"AgentThoughtBlockUpdatedEvent","text":"Thinking..."}},"timestampMs":123456789}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .and {
                get { timestampMs }.isEqualTo(123456789L)
                get { event.agentEvent }.isA<AgentThoughtBlockUpdatedEvent>()
                    .get { text }.isEqualTo("Thinking...")
            }
    }

    // -- Phase B: Unknown event fallback tests --

    @Test
    fun `given an unknown top-level event kind when parsed then it returns UnknownJunieEvent`() {
        val line = """{"kind":"FutureNewEvent","data":"something","timestampMs":999}"""
        val result = JsonlParser.parseLine(line)

        val event = expectThat(result.getOrNull()).isA<UnknownJunieEvent>()
        event.get { kind }.isEqualTo("FutureNewEvent")
        event.get { timestampMs }.isEqualTo(999L)
        event.get { raw }.isNotNull()
    }

    @Test
    fun `given a SessionA2uxEvent with unknown nested agent event when parsed then it returns UnknownAgentEvent`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"FutureAgentEvent","someField":"value"}},"timestampMs":555}"""
        val result = JsonlParser.parseLine(line)

        val event = expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
        event.get { timestampMs }.isEqualTo(555L)
        val agentEvent = event.get { this.event.agentEvent }.isA<UnknownAgentEvent>()
        agentEvent.get { kind }.isEqualTo("FutureAgentEvent")
        agentEvent.get { raw }.isNotNull()
    }

    @Test
    fun `given a malformed JSON line when parsed then it returns an error`() {
        val line = """{"kind":"UserPromptEvent" "missing_colon"}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.isLeft()).isEqualTo(true)
    }

    @Test
    fun `given a missing required field when parsed then it returns an error`() {
        // prompt is required for UserPromptEvent
        val line = """{"kind":"UserPromptEvent","requestId":"req-1"}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.isLeft()).isEqualTo(true)
    }

    @Test
    fun `given an empty string when parsed then it returns an error`() {
        val line = ""
        val result = JsonlParser.parseLine(line)

        expectThat(result.isLeft()).isEqualTo(true)
    }

    @Test
    fun `given a SessionA2uxEvent with AgentPatchCreatedEvent when parsed then it returns correct event`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentPatchCreatedEvent","patch":"diff --git ..."}}}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<AgentPatchCreatedEvent>()
            .get { patch }.isEqualTo("diff --git ...")
    }

    @Test
    fun `given a SessionA2uxEvent with TerminalBlockUpdatedEvent when parsed then it returns correct event`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"TerminalBlockUpdatedEvent","command":"ls","output":"file.txt"}}}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<TerminalBlockUpdatedEvent>()
            .and {
                get { command }.isEqualTo("ls")
                get { output }.isEqualTo("file.txt")
            }
    }

    // -- Phase A: Known event class tests --

    @Test
    fun `given a TaskStartedEvent when parsed then it returns TaskStartedEvent`() {
        val line = """{"kind":"TaskStartedEvent","taskId":"task-123","timestampMs":1000}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<TaskStartedEvent>()
            .and {
                get { taskId }.isEqualTo("task-123")
                get { timestampMs }.isEqualTo(1000L)
            }
    }

    @Test
    fun `given a TaskState event when parsed then it returns TaskState`() {
        val line = """{"kind":"TaskState","taskId":"task-123","state":"COMPLETED","timestampMs":2000}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<TaskState>()
            .and {
                get { taskId }.isEqualTo("task-123")
                get { state }.isEqualTo("COMPLETED")
            }
    }

    @Test
    fun `given a UserMessagesCommittedToHistory event when parsed then it returns correct type`() {
        val line = """{"kind":"UserMessagesCommittedToHistory","requestId":"req-5","timestampMs":3000}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<UserMessagesCommittedToHistory>()
            .get { requestId }.isEqualTo("req-5")
    }

    @Test
    fun `given a UserAsyncResponseEvent when parsed then it returns correct type`() {
        val line = """{"kind":"UserAsyncResponseEvent","requestId":"req-7","response":"approved","timestampMs":4000}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<UserAsyncResponseEvent>()
            .and {
                get { requestId }.isEqualTo("req-7")
                get { response }.isEqualTo("approved")
            }
    }

    @Test
    fun `given an AvailablePullRequestsEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AvailablePullRequestsEvent"}},"timestampMs":100}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<AvailablePullRequestsEvent>()
    }

    @Test
    fun `given an LlmResponseMetadataEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"LlmResponseMetadataEvent","model":"gpt-4","inputTokens":100,"outputTokens":50}},"timestampMs":200}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<LlmResponseMetadataEvent>()
            .and {
                get { model }.isEqualTo("gpt-4")
                get { inputTokens }.isEqualTo(100)
                get { outputTokens }.isEqualTo(50)
            }
    }

    @Test
    fun `given a CurrentDirectoryUpdatedEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"CurrentDirectoryUpdatedEvent","directory":"/home/user"}},"timestampMs":300}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<CurrentDirectoryUpdatedEvent>()
            .get { directory }.isEqualTo("/home/user")
    }

    @Test
    fun `given an AgentStartedEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentStartedEvent","agentId":"agent-1"}},"timestampMs":400}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<AgentStartedEvent>()
            .get { agentId }.isEqualTo("agent-1")
    }

    @Test
    fun `given a ContextWindowReportEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ContextWindowReportEvent","usedTokens":5000,"maxTokens":8000}},"timestampMs":500}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<ContextWindowReportEvent>()
            .and {
                get { usedTokens }.isEqualTo(5000)
                get { maxTokens }.isEqualTo(8000)
            }
    }

    @Test
    fun `given unknown event with extra fields when parsed then raw JSON is preserved`() {
        val line = """{"kind":"BrandNewEvent","field1":"a","field2":42,"nested":{"x":true}}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<UnknownJunieEvent>()
            .and {
                get { kind }.isEqualTo("BrandNewEvent")
                get { raw.toString() }.isEqualTo(line)
            }
    }

    @Test
    fun `given known events with extra unknown fields when parsed then ignoreUnknownKeys works`() {
        val line = """{"kind":"TaskStartedEvent","taskId":"t1","timestampMs":1,"extraField":"ignored"}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<TaskStartedEvent>()
            .get { taskId }.isEqualTo("t1")
    }

    // -- New top-level event tests --

    @Test
    fun `given a SystemMessageEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SystemMessageEvent","text":"Free Google AI","details":"Powered by Google"}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SystemMessageEvent>()
            .and {
                get { text }.isEqualTo("Free Google AI")
                get { details }.isEqualTo("Powered by Google")
            }
    }

    @Test
    fun `given a SendToAgentEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SendToAgentEvent"}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SendToAgentEvent>()
    }

    @Test
    fun `given a CancelAgentEvent when parsed then it returns correct type`() {
        val line = """{"kind":"CancelAgentEvent"}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<CancelAgentEvent>()
    }

    @Test
    fun `given a SessionTitleSetEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionTitleSetEvent","name":"LogViewer","timestampMs":1000}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionTitleSetEvent>()
            .and {
                get { name }.isEqualTo("LogViewer")
                get { timestampMs }.isEqualTo(1000L)
            }
    }

    @Test
    fun `given a SkillsStatusEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SkillsStatusEvent","newSkills":["android-data-layer","android-testing"]}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SkillsStatusEvent>()
            .get { newSkills }.isNotNull().hasSize(2)
    }

    @Test
    fun `given a TaskContinueStopped when parsed then it returns correct type`() {
        val line = """{"kind":"TaskContinueStopped"}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<TaskContinueStopped>()
    }

    @Test
    fun `given a UserResponseEvent when parsed then it returns correct type`() {
        val line = """{"kind":"UserResponseEvent","prompt":"Confirm the plan","isChoice":true}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<UserResponseEvent>()
            .and {
                get { prompt }.isEqualTo("Confirm the plan")
                get { isChoice }.isEqualTo(true)
            }
    }

    // -- New agent event tests --

    @Test
    fun `given a TestRunBlockUpdatedEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"TestRunBlockUpdatedEvent","stepId":"s1","status":"IN_PROGRESS","name":"Run test MyTest"}},"timestampMs":100}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<TestRunBlockUpdatedEvent>()
            .and {
                get { stepId }.isEqualTo("s1")
                get { status }.isEqualTo("IN_PROGRESS")
                get { name }.isEqualTo("Run test MyTest")
            }
    }

    @Test
    fun `given a McpBlockUpdatedEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"McpBlockUpdatedEvent","stepId":"s2","toolName":"Context7/resolve-library-id","status":"COMPLETED","details":"{\"libraryName\":\"Ktor\"}"}},"timestampMs":200}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<McpBlockUpdatedEvent>()
            .and {
                get { toolName }.isEqualTo("Context7/resolve-library-id")
                get { status }.isEqualTo("COMPLETED")
            }
    }

    @Test
    fun `given a CustomAgentBlockUpdatedEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"CustomAgentBlockUpdatedEvent","stepId":"s3","name":"android-qa-agent","status":"STARTED"}},"timestampMs":300}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<CustomAgentBlockUpdatedEvent>()
            .and {
                get { name }.isEqualTo("android-qa-agent")
                get { status }.isEqualTo("STARTED")
            }
    }

    @Test
    fun `given an AgentFailureEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentFailureEvent","message":"Unable to connect","errorCode":"ConnectionFailed"}},"timestampMs":400}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<AgentFailureEvent>()
            .and {
                get { message }.isEqualTo("Unable to connect")
                get { errorCode }.isEqualTo("ConnectionFailed")
            }
    }

    @Test
    fun `given an AgentStateUpdatedEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentStateUpdatedEvent","blob":"{\"state\":\"data\"}"}},"timestampMs":500}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<AgentStateUpdatedEvent>()
            .get { blob }.isNotNull()
    }

    @Test
    fun `given an AskRequestUpdatedEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AskRequestUpdatedEvent","stepId":"s4","title":"Junie asks","askRequest":{"id":"a1","question":"What next?"},"status":"IN_PROGRESS"}},"timestampMs":600}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<AskRequestUpdatedEvent>()
            .and {
                get { title }.isEqualTo("Junie asks")
                get { status }.isEqualTo("IN_PROGRESS")
            }
    }

    @Test
    fun `given a ChoiceRequestUpdatedEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ChoiceRequestUpdatedEvent","stepId":"s5","title":"How to proceed?","choiceRequest":{"id":"c1","options":[{"id":"AgreeWithCode","description":"Confirm"}]},"status":"IN_PROGRESS"}},"timestampMs":700}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<ChoiceRequestUpdatedEvent>()
            .and {
                get { title }.isEqualTo("How to proceed?")
                get { status }.isEqualTo("IN_PROGRESS")
            }
    }

    @Test
    fun `given a MarkdownBlockUpdatedEvent when parsed then it returns correct type`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"MarkdownBlockUpdatedEvent","stepId":"s6","text":"No changes were undone."}},"timestampMs":800}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<MarkdownBlockUpdatedEvent>()
            .get { text }.isEqualTo("No changes were undone.")
    }

    // -- Field discrepancy tests --

    @Test
    fun `given a UserPromptEvent with presentablePrompt and customAttachments when parsed then fields are present`() {
        val line = """{"kind":"UserPromptEvent","prompt":"fix tests","presentablePrompt":"fix tests","customAttachments":[]}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<UserPromptEvent>()
            .and {
                get { presentablePrompt }.isEqualTo("fix tests")
                get { customAttachments }.isNotNull()
            }
    }

    @Test
    fun `given a ToolBlockUpdatedEvent with stepId and status when parsed then fields are present`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ToolBlockUpdatedEvent","stepId":"s1","text":"search_file","status":"COMPLETED","details":"found 3 results"}},"timestampMs":100}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<ToolBlockUpdatedEvent>()
            .and {
                get { stepId }.isEqualTo("s1")
                get { status }.isEqualTo("COMPLETED")
                get { text }.isEqualTo("search_file")
                get { details }.isEqualTo("found 3 results")
            }
    }

    @Test
    fun `given a ResultBlockUpdatedEvent with extra fields when parsed then fields are present`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ResultBlockUpdatedEvent","result":"Done","stepId":"s1","cancelled":false,"errorCode":"NONE"}},"timestampMs":100}"""
        val result = JsonlParser.parseLine(line)

        val event = expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<ResultBlockUpdatedEvent>()
        event.get { this.result }.isEqualTo("Done")
        event.get { stepId }.isEqualTo("s1")
        event.get { cancelled }.isEqualTo(false)
        event.get { errorCode }.isEqualTo("NONE")
    }

    // -- Crash-fix regression tests --

    @Test
    fun `given a NextPromptSuggestionEvent with array suggestion when parsed then it succeeds`() {
        // Real-world format: suggestion is a JsonArray, not a String
        val line = """{"kind":"SessionA2uxEvent","event":{"state":"COMPLETED","agentEvent":{"kind":"NextPromptSuggestionEvent","agent":{"kind":"MainAgent","id":"main","name":"main"},"suggestion":[{"text":"update project_memory.md now"}]}},"timestampMs":1782134697259}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.getOrNull()).isA<SessionA2uxEvent>()
            .get { event.agentEvent }.isA<NextPromptSuggestionEvent>()
            .get { suggestion }.isNotNull()
    }
}
