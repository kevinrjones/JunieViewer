package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Focused unit tests for [BlockExpansionController], the pure block-expansion derivation
 * logic extracted from [ConversationViewModel]. Operates only on [ConversationState]
 * snapshots, so it is directly unit-testable without any ViewModel machinery.
 */
class BlockExpansionControllerTest {

    private val messages = listOf(
        Message("msg-1", Sender.Human, MessageContent.Text("Hello"), MessageKind.Text),
        Message("msg-2", Sender.Junie, MessageContent.Text("Thinking..."), MessageKind.Thought),
        Message("msg-3", Sender.Junie, MessageContent.Terminal("\$ ls"), MessageKind.Terminal)
    )

    private fun stateWithMessages(
        blockExpansionStates: Map<String, Boolean> = emptyMap(),
        dismissedForceExpandedBlockIds: Set<String> = emptySet(),
        searchQuery: String = "",
        currentMatchIndex: Int = -1
    ) = ConversationState(
        sessionLoad = SessionLoadState(messages = messages),
        search = SearchState(
            filteredMessages = messages,
            searchQuery = searchQuery,
            currentMatchIndex = currentMatchIndex
        ),
        blockExpansion = BlockExpansionState(
            blockExpansionStates = blockExpansionStates,
            dismissedForceExpandedBlockIds = dismissedForceExpandedBlockIds
        )
    )

    @Test
    fun `currentForceExpandedBlockId is null when search query is blank`() {
        val state = stateWithMessages(searchQuery = "", currentMatchIndex = 2)
        assertNull(BlockExpansionController.currentForceExpandedBlockId(state))
    }

    @Test
    fun `currentForceExpandedBlockId returns the block id of the current match`() {
        val state = stateWithMessages(searchQuery = "\$ ls", currentMatchIndex = 2)
        assertEquals("msg-3:terminal", BlockExpansionController.currentForceExpandedBlockId(state))
    }

    @Test
    fun `currentForceExpandedBlockId is null when match index is out of bounds`() {
        val state = stateWithMessages(searchQuery = "\$ ls", currentMatchIndex = 99)
        assertNull(BlockExpansionController.currentForceExpandedBlockId(state))
    }

    @Test
    fun `isBlockForceExpanded delegates to currentForceExpandedBlockId`() {
        val state = stateWithMessages(searchQuery = "\$ ls", currentMatchIndex = 2)
        assertTrue(BlockExpansionController.isBlockForceExpanded(state, "msg-3:terminal"))
        assertFalse(BlockExpansionController.isBlockForceExpanded(state, "msg-2:thought"))
    }

    @Test
    fun `deriveBlockExpansionStates defaults unset blocks to expanded`() {
        val state = stateWithMessages()
        val derived = BlockExpansionController.deriveBlockExpansionStates(state)
        assertEquals(true, derived["msg-2:thought"])
        assertEquals(true, derived["msg-3:terminal"])
    }

    @Test
    fun `deriveBlockExpansionStates honours manual collapse`() {
        val state = stateWithMessages(blockExpansionStates = mapOf("msg-2:thought" to false))
        val derived = BlockExpansionController.deriveBlockExpansionStates(state)
        assertEquals(false, derived["msg-2:thought"])
    }

    @Test
    fun `deriveBlockExpansionStates force-expands the current search match even when manually collapsed`() {
        val state = stateWithMessages(
            blockExpansionStates = mapOf("msg-3:terminal" to false),
            searchQuery = "\$ ls",
            currentMatchIndex = 2
        )
        val derived = BlockExpansionController.deriveBlockExpansionStates(state)
        assertEquals(true, derived["msg-3:terminal"])
    }

    @Test
    fun `deriveBlockExpansionStates respects dismissal of force-expansion`() {
        val state = stateWithMessages(
            blockExpansionStates = mapOf("msg-3:terminal" to false),
            dismissedForceExpandedBlockIds = setOf("msg-3:terminal"),
            searchQuery = "\$ ls",
            currentMatchIndex = 2
        )
        val derived = BlockExpansionController.deriveBlockExpansionStates(state)
        assertEquals(false, derived["msg-3:terminal"])
    }

    @Test
    fun `collapseAll sets every known block to collapsed and clears dismissals`() {
        val state = stateWithMessages(dismissedForceExpandedBlockIds = setOf("msg-3:terminal"))
        val updated = BlockExpansionController.collapseAll(state)
        assertEquals(false, updated.blockExpansionStates["msg-2:thought"])
        assertEquals(false, updated.blockExpansionStates["msg-3:terminal"])
        assertTrue(updated.dismissedForceExpandedBlockIds.isEmpty())
    }

    @Test
    fun `showAll sets every known block to expanded and clears dismissals`() {
        val state = stateWithMessages(
            blockExpansionStates = mapOf("msg-2:thought" to false),
            dismissedForceExpandedBlockIds = setOf("msg-3:terminal")
        )
        val updated = BlockExpansionController.showAll(state)
        assertEquals(true, updated.blockExpansionStates["msg-2:thought"])
        assertEquals(true, updated.blockExpansionStates["msg-3:terminal"])
        assertTrue(updated.dismissedForceExpandedBlockIds.isEmpty())
    }

    @Test
    fun `toggle flips manual state from the default expanded value`() {
        val state = stateWithMessages().let {
            it.copy(blockExpansion = it.blockExpansion.copy(derivedBlockExpansionStates = BlockExpansionController.deriveBlockExpansionStates(it)))
        }
        val updated = BlockExpansionController.toggle(state, "msg-2:thought")
        assertEquals(false, updated.blockExpansionStates["msg-2:thought"])
    }

    @Test
    fun `toggle of a force-expanded block records a dismissal`() {
        var state = stateWithMessages(searchQuery = "\$ ls", currentMatchIndex = 2)
        state = state.copy(blockExpansion = state.blockExpansion.copy(derivedBlockExpansionStates = BlockExpansionController.deriveBlockExpansionStates(state)))

        val updated = BlockExpansionController.toggle(state, "msg-3:terminal")

        assertTrue(updated.dismissedForceExpandedBlockIds.contains("msg-3:terminal"))
        assertEquals(false, updated.blockExpansionStates["msg-3:terminal"])
    }
}
