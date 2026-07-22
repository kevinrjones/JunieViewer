package com.knowledgespike.junieviewer.data

import com.knowledgespike.junieviewer.domain.*
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Coverage for Sprint 6 Area 3's typed domain events (F2): verifies real-log-shaped payloads
 * decode into the typed models in `EventPayloads.kt` instead of raw kotlinx JSON types, and
 * that the ask/choice/unknown-event fallbacks remain explicit and logged at the deserialization
 * boundary ([AskRequestSerializer], [ChoiceRequestSerializer], [UnknownAgentEventSerializer]).
 */
class TypedPayloadParsingTest {

    /** Parses [line] and asserts it succeeded, returning the parsed event. */
    private fun parse(line: String): JunieEvent =
        JsonlParser.parseLine(line).getOrNull() ?: error("Expected successful parse of: $line")

    /** Unwraps a [SessionA2uxEvent]'s nested agent event, asserting the event parsed as one. */
    private fun agentEventOf(line: String): AgentEvent =
        (parse(line) as SessionA2uxEvent).event.agentEvent

    // -----------------------------------------------------------------------
    // Positive cases — real-log shapes decode into typed models
    // -----------------------------------------------------------------------

    @Test
    fun `given a ResultBlockUpdatedEvent with before and after changes when parsed then changes are typed FileChange list`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ResultBlockUpdatedEvent","result":"Done","changes":[{"beforeContent":{"kind":"TextFileContent","text":"old"},"beforeRelativePath":"a.txt","afterContent":{"kind":"TextFileContent","text":"new"},"afterRelativePath":"a.txt"},{"afterContent":{"kind":"TextFileContent","text":"created"},"afterRelativePath":"b.txt"}]}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<ResultBlockUpdatedEvent>().get { changes }.isNotNull().and {
            hasSize(2)
            get { first().beforeRelativePath }.isEqualTo("a.txt")
            get { first().beforeContent?.text }.isEqualTo("old")
            get { first().afterContent?.text }.isEqualTo("new")
            get { get(1).beforeContent }.isEqualTo(null)
            get { get(1).afterRelativePath }.isEqualTo("b.txt")
        }
    }

    @Test
    fun `given a ResultBlockUpdatedEvent with empty changes array when parsed then changes is an empty list`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ResultBlockUpdatedEvent","result":"Done","changes":[]}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<ResultBlockUpdatedEvent>().get { changes }.isNotNull().isEmpty()
    }

    @Test
    fun `given an AgentPlanUpdatedEvent with items when parsed then items are typed PlanItem list`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AgentPlanUpdatedEvent","items":[{"description":"Write tests","status":"DONE"},{"description":"Implement fix","status":"IN_PROGRESS"}]}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<AgentPlanUpdatedEvent>().get { items }.isNotNull().and {
            hasSize(2)
            get { first().description }.isEqualTo("Write tests")
            get { first().status }.isEqualTo("DONE")
        }
    }

    @Test
    fun `given an LlmResponseMetadataEvent with modelUsage when parsed then usage is typed ModelUsage list`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"LlmResponseMetadataEvent","modelUsage":[{"model":"gpt-4","cost":0.05,"inputTokens":100,"cacheInputTokens":10,"cacheCreateTokens":5,"outputTokens":50,"time":1.2}]}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<LlmResponseMetadataEvent>().get { modelUsage }.isNotNull().and {
            hasSize(1)
            get { first().model }.isEqualTo("gpt-4")
            get { first().cost }.isEqualTo(0.05)
            get { first().inputTokens }.isEqualTo(100)
            get { first().outputTokens }.isEqualTo(50)
        }
    }

    @Test
    fun `given a ViewFilesBlockUpdatedEvent with and without line ranges when parsed then files are typed ViewedFile list`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ViewFilesBlockUpdatedEvent","files":[{"relativePath":"a.kt","lineFrom":1,"lineTo":10},{"relativePath":"b.kt"}]}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<ViewFilesBlockUpdatedEvent>().get { files }.isNotNull().and {
            hasSize(2)
            get { first().relativePath }.isEqualTo("a.kt")
            get { first().lineFrom }.isEqualTo(1)
            get { first().lineTo }.isEqualTo(10)
            get { get(1).relativePath }.isEqualTo("b.kt")
            get { get(1).lineFrom }.isEqualTo(null)
        }
    }

    @Test
    fun `given a ContextWindowReportEvent with a float percentage when parsed then percentage is a Double`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ContextWindowReportEvent","percentage":5.07025}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<ContextWindowReportEvent>().get { percentage }.isEqualTo(5.07025)
    }

    @Test
    fun `given a NextPromptSuggestionEvent with an array suggestion when parsed then suggestion is typed PromptSuggestion list`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"NextPromptSuggestionEvent","suggestion":[{"text":"update project_memory.md now"}]}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<NextPromptSuggestionEvent>().get { suggestion }.isNotNull()
            .and { hasSize(1); get { first().text }.isEqualTo("update project_memory.md now") }
    }

    @Test
    fun `given an AskAsyncRequestUpdatedEvent with options when parsed then request is a typed AsyncRequest`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AskAsyncRequestUpdatedEvent","request":{"id":"req-1","name":"confirm","question":"Proceed?","isRequired":true,"allowMultiple":false,"options":[{"id":"yes","description":"Yes, proceed"}]}}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<AskAsyncRequestUpdatedEvent>().get { request }.isNotNull().and {
            get { id }.isEqualTo("req-1")
            get { question }.isEqualTo("Proceed?")
            get { isRequired }.isEqualTo(true)
            get { options }.isNotNull().hasSize(1)
        }
    }

    @Test
    fun `given a SuggestPlanEvent with sections and deliveryPlan when parsed then both are typed lists`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"SuggestPlanEvent","sections":[{"name":"Overview","content":"Refactor the domain"}],"deliveryPlan":[{"name":"Step 1","description":"Create typed models","status":"DONE"}]}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<SuggestPlanEvent>().and {
            get { sections }.isNotNull().hasSize(1)
            get { sections!!.first().name }.isEqualTo("Overview")
            get { deliveryPlan }.isNotNull().hasSize(1)
            get { deliveryPlan!!.first().status }.isEqualTo("DONE")
        }
    }

    @Test
    fun `given a SubagentSpawnedEvent with an agent object when parsed then agent is a typed AgentIdentity`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"SubagentSpawnedEvent","name":"doc-reader","agent":{"id":"main","kind":"MainAgent","name":"main","type":"LINEAR"}}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<SubagentSpawnedEvent>().get { agent }.isNotNull().and {
            get { id }.isEqualTo("main")
            get { kind }.isEqualTo("MainAgent")
            get { type }.isEqualTo("LINEAR")
        }
    }

    @Test
    fun `given a UserAsyncResponseEvent with entries when parsed then entries are typed ResponseEntry list`() {
        val line = """{"kind":"UserAsyncResponseEvent","requestId":"r1","entries":[{"question":"Continue?","answer":"Yes"}]}"""

        val event = parse(line)

        expectThat(event).isA<UserAsyncResponseEvent>().get { entries }.isNotNull()
            .and { hasSize(1); get { first().question }.isEqualTo("Continue?"); get { first().answer }.isEqualTo("Yes") }
    }

    @Test
    fun `given an AskRequestUpdatedEvent with id and question when parsed then askRequest is structured`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AskRequestUpdatedEvent","askRequest":{"id":"a1","question":"What next?"}}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<AskRequestUpdatedEvent>().get { askRequest }.isNotNull().and {
            get { id }.isEqualTo("a1")
            get { question }.isEqualTo("What next?")
            get { unstructuredText }.isEqualTo(null)
        }
    }

    @Test
    fun `given a ChoiceRequestUpdatedEvent with id and options when parsed then choiceRequest is structured`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ChoiceRequestUpdatedEvent","choiceRequest":{"id":"c1","options":[{"id":"a","description":"Agree"}]}}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<ChoiceRequestUpdatedEvent>().get { choiceRequest }.isNotNull().and {
            get { id }.isEqualTo("c1")
            get { unstructuredText }.isEqualTo(null)
            get { options }.isNotNull().hasSize(1)
            get { options!!.first().id }.isEqualTo("a")
            get { options!!.first().description }.isEqualTo("Agree")
        }
    }

    // -----------------------------------------------------------------------
    // Negative / malformed cases
    // -----------------------------------------------------------------------

    @Test
    fun `given an AskRequestUpdatedEvent whose askRequest is a plain string when parsed then unstructuredText preserves the raw text`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"AskRequestUpdatedEvent","askRequest":"just a string"}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<AskRequestUpdatedEvent>().get { askRequest }.isNotNull().and {
            get { unstructuredText }.isEqualTo("\"just a string\"")
            get { id }.isEqualTo(null)
            get { question }.isEqualTo(null)
        }
    }

    @Test
    fun `given a ChoiceRequestUpdatedEvent whose options contains a non-object when parsed then unstructuredText preserves the raw text`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"ChoiceRequestUpdatedEvent","choiceRequest":{"id":"c1","options":["oops"]}}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<ChoiceRequestUpdatedEvent>().get { choiceRequest }.isNotNull().and {
            get { unstructuredText }.isEqualTo("""{"id":"c1","options":["oops"]}""")
            get { options }.isEqualTo(null)
        }
    }

    @Test
    fun `given an unknown agent event when parsed then raw round-trips the exact compact JSON`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"FutureAgentEvent","field1":"a","field2":42,"nested":{"x":true}}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<UnknownAgentEvent>().and {
            get { kind }.isEqualTo("FutureAgentEvent")
            get { raw.toString() }.isEqualTo("""{"kind":"FutureAgentEvent","field1":"a","field2":42,"nested":{"x":true}}""")
        }
    }

    @Test
    fun `given an unknown agent event with a direct currentDirectory when parsed then it is readable via textOrNull`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"FutureAgentEvent","currentDirectory":"/Users/dev/project"}}}"""

        val event = agentEventOf(line)

        expectThat(event).isA<UnknownAgentEvent>().get { raw.textOrNull("currentDirectory") }
            .isEqualTo("/Users/dev/project")
    }

    @Test
    fun `given an unknown agent event with a blob when parsed then it is readable via textOrNull`() {
        val line = """{"kind":"SessionA2uxEvent","event":{"agentEvent":{"kind":"FutureAgentEvent","blob":"{\"currentDirectory\":\"/Users/dev/blob-project\"}"}}}"""

        val event = agentEventOf(line)

        val blob = expectThat(event).isA<UnknownAgentEvent>().get { raw.textOrNull("blob") }.subject
        expectThat(blob).isNotNull()
        expectThat(JsonlParser.workingDirectoryFromAgentStateBlob(blob!!)).isEqualTo("/Users/dev/blob-project")
    }

    @Test
    fun `given an entirely malformed line when parsed then it returns Either Left`() {
        val result = JsonlParser.parseLine("""{"kind":"UserPromptEvent" "missing_colon"}""")

        expectThat(result.isLeft()).isEqualTo(true)
    }

    // -----------------------------------------------------------------------
    // JsonlParser.workingDirectoryFromAgentStateBlob
    // -----------------------------------------------------------------------

    @Test
    fun `given a valid agent-state blob when workingDirectoryFromAgentStateBlob then it returns the directory`() {
        val blob = """{"currentDirectory":"/Users/dev/my-project","other":1}"""

        expectThat(JsonlParser.workingDirectoryFromAgentStateBlob(blob)).isEqualTo("/Users/dev/my-project")
    }

    @Test
    fun `given a malformed agent-state blob when workingDirectoryFromAgentStateBlob then it returns null`() {
        expectThat(JsonlParser.workingDirectoryFromAgentStateBlob("not-json currentDirectory")).isEqualTo(null)
    }
}
