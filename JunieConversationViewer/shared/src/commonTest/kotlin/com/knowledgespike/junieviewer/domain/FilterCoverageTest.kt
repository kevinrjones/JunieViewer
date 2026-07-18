package com.knowledgespike.junieviewer.domain

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Unit tests verifying that every MessageKind maps to the correct FilterCategory.
 * Covers Area 4 task 4.4 — ensures no MessageKind is left unmapped and grouped
 * kinds (StructuredOutput, Mcp, SubAgent, TestRun) map correctly.
 */
class FilterCoverageTest {

    // -----------------------------------------------------------------------
    // 4.4 — Every MessageKind maps to the expected FilterCategory
    // -----------------------------------------------------------------------

    @Test
    fun `Text maps to Junie filter category`() {
        expectThat(MessageKind.Text.filterCategory).isEqualTo(FilterCategory.Junie)
    }

    @Test
    fun `Markdown maps to Junie filter category`() {
        expectThat(MessageKind.Markdown.filterCategory).isEqualTo(FilterCategory.Junie)
    }

    @Test
    fun `Thought maps to Thought filter category`() {
        expectThat(MessageKind.Thought.filterCategory).isEqualTo(FilterCategory.Thought)
    }

    @Test
    fun `Tool maps to Tool filter category`() {
        expectThat(MessageKind.Tool.filterCategory).isEqualTo(FilterCategory.Tool)
    }

    @Test
    fun `Patch maps to Patch filter category`() {
        expectThat(MessageKind.Patch.filterCategory).isEqualTo(FilterCategory.Patch)
    }

    @Test
    fun `Terminal maps to Terminal filter category`() {
        expectThat(MessageKind.Terminal.filterCategory).isEqualTo(FilterCategory.Terminal)
    }

    @Test
    fun `StructuredOutput maps to Tool filter category`() {
        expectThat(MessageKind.StructuredOutput.filterCategory).isEqualTo(FilterCategory.Tool)
    }

    @Test
    fun `Mcp maps to Tool filter category`() {
        expectThat(MessageKind.Mcp.filterCategory).isEqualTo(FilterCategory.Tool)
    }

    @Test
    fun `SubAgent maps to Tool filter category`() {
        expectThat(MessageKind.SubAgent.filterCategory).isEqualTo(FilterCategory.Tool)
    }

    @Test
    fun `TestRun maps to Terminal filter category`() {
        expectThat(MessageKind.TestRun.filterCategory).isEqualTo(FilterCategory.Terminal)
    }

    // -----------------------------------------------------------------------
    // AlwaysShow kinds — must remain visible regardless of filter toggles
    // -----------------------------------------------------------------------

    @Test
    fun `Error maps to AlwaysShow`() {
        expectThat(MessageKind.Error.filterCategory).isEqualTo(FilterCategory.AlwaysShow)
    }

    @Test
    fun `Warning maps to AlwaysShow`() {
        expectThat(MessageKind.Warning.filterCategory).isEqualTo(FilterCategory.AlwaysShow)
    }

    @Test
    fun `Unsupported maps to AlwaysShow`() {
        expectThat(MessageKind.Unsupported.filterCategory).isEqualTo(FilterCategory.AlwaysShow)
    }

    @Test
    fun `Question maps to AlwaysShow`() {
        expectThat(MessageKind.Question.filterCategory).isEqualTo(FilterCategory.AlwaysShow)
    }

    @Test
    fun `Choice maps to AlwaysShow`() {
        expectThat(MessageKind.Choice.filterCategory).isEqualTo(FilterCategory.AlwaysShow)
    }

    @Test
    fun `SystemMessage maps to AlwaysShow`() {
        expectThat(MessageKind.SystemMessage.filterCategory).isEqualTo(FilterCategory.AlwaysShow)
    }

    @Test
    fun `Cancelled maps to AlwaysShow`() {
        expectThat(MessageKind.Cancelled.filterCategory).isEqualTo(FilterCategory.AlwaysShow)
    }

    @Test
    fun `Status maps to AlwaysShow`() {
        expectThat(MessageKind.Status.filterCategory).isEqualTo(FilterCategory.AlwaysShow)
    }

    // -----------------------------------------------------------------------
    // Exhaustiveness — no MessageKind is left unmapped
    // -----------------------------------------------------------------------

    @Test
    fun `all 18 MessageKind values have a non-null filterCategory`() {
        val allKinds = MessageKind.entries
        expectThat(allKinds).hasSize(18)
        allKinds.forEach { kind ->
            expectThat(kind.filterCategory).isNotNull()
        }
    }

    @Test
    fun `FilterCategory has exactly 7 values`() {
        expectThat(FilterCategory.entries).hasSize(7)
    }
}
