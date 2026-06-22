package com.knowledgespike.junieviewer.data

import com.knowledgespike.junieviewer.domain.SessionA2uxEvent
import com.knowledgespike.junieviewer.domain.UserPromptEvent
import com.knowledgespike.junieviewer.domain.AgentThoughtBlockUpdatedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

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

    @Test
    fun `given an unknown event kind when parsed then it returns an error`() {
        val line = """{"kind":"UnknownEvent","data":"something"}"""
        val result = JsonlParser.parseLine(line)

        expectThat(result.isLeft()).isEqualTo(true)
    }
}
