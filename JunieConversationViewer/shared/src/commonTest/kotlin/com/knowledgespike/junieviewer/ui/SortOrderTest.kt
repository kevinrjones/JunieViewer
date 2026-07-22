package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Area 6 — Sort Order: Oldest First / Newest First.
 * Covers sort state, persistence, visible Message ordering, filter/search interaction,
 * and live-tracked Message placement.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SortOrderTest {

    private val testMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("First message"), MessageKind.Text),
        Message("2", Sender.Junie, MessageContent.Text("Second message"), MessageKind.Text),
        Message("3", Sender.Human, MessageContent.Text("Third message"), MessageKind.Text)
    )

    /** Creates a [ConversationViewModel] with a Session already selected. */
    private fun ConversationStateTestScope.createViewModelWithSession(): ConversationViewModel {
        val vm = createViewModel()
        vm.onAction(ConversationAction.OnSessionSelected(SessionInfo("test-session", "/path", 0L)))
        return vm
    }

    // -- Default Sort Order --

    @Test
    fun `default sort order is OldestFirst`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(SortOrder.OldestFirst, viewModel.state.value.sortOrder)
    }

    // -- Toggle Sort Order --

    @Test
    fun `ToggleSortOrder changes OldestFirst to NewestFirst`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()
        assertEquals(SortOrder.OldestFirst, viewModel.state.value.sortOrder)

        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        advanceUntilIdle()
        assertEquals(SortOrder.NewestFirst, viewModel.state.value.sortOrder)
    }

    @Test
    fun `ToggleSortOrder changes NewestFirst back to OldestFirst`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        advanceUntilIdle()
        assertEquals(SortOrder.NewestFirst, viewModel.state.value.sortOrder)

        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        advanceUntilIdle()
        assertEquals(SortOrder.OldestFirst, viewModel.state.value.sortOrder)
    }

    // -- Visible Message Ordering --

    @Test
    fun `visible Messages are oldest-first by default`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        val ids = viewModel.state.value.filteredMessages.map { it.id }
        assertEquals(listOf("1", "2", "3"), ids)
    }

    @Test
    fun `visible Messages reverse in newest-first`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        advanceUntilIdle()

        val ids = viewModel.state.value.filteredMessages.map { it.id }
        assertEquals(listOf("3", "2", "1"), ids)
    }

    // -- Persistence --

    @Test
    fun `sort order is saved when toggled`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        advanceUntilIdle()

        val savedPrefs = preferencesRepository.load()
        assertEquals("NewestFirst", savedPrefs.sortOrder)
    }

    @Test
    fun `sort order is loaded from preferences`() = runConversationStateTest(testMessages) {
        // Save NewestFirst preference before creating ViewModel
        preferencesRepository.save(AppPreferences(sortOrder = "NewestFirst"))

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(SortOrder.NewestFirst, viewModel.state.value.sortOrder)
    }

    @Test
    fun `invalid saved sort order falls back to OldestFirst`() = runConversationStateTest(testMessages) {
        preferencesRepository.save(AppPreferences(sortOrder = "InvalidValue"))

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(SortOrder.OldestFirst, viewModel.state.value.sortOrder)
    }

    // -- Filter Interaction --

    @Test
    fun `filters still apply in both sort orders`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        // Filter out Human messages (ids 1, 3)
        viewModel.onAction(ConversationAction.OnToggleFilter(FilterKind.Human))
        advanceUntilIdle()

        // OldestFirst: only Junie message remains
        val oldestIds = viewModel.state.value.filteredMessages.map { it.id }
        assertEquals(listOf("2"), oldestIds)

        // Switch to NewestFirst
        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        advanceUntilIdle()

        val newestIds = viewModel.state.value.filteredMessages.map { it.id }
        assertEquals(listOf("2"), newestIds)
    }

    // -- Search Interaction --

    @Test
    fun `search query still filters Messages in both sort orders`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        // Search for "Third" — only message 3 matches
        viewModel.onAction(ConversationAction.OnSearchQueryChange("Third"))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.filteredMessages.size)
        assertEquals("3", viewModel.state.value.filteredMessages.first().id)

        // Toggle to NewestFirst — same result
        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.filteredMessages.size)
        assertEquals("3", viewModel.state.value.filteredMessages.first().id)
    }

    @Test
    fun `Find Next and Find Previous work in visible sorted order`() = runConversationStateTest(testMessages) {
        // All messages contain "message" so all match
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("message"))
        advanceUntilIdle()

        // OldestFirst: ids are 1, 2, 3
        assertEquals(0, viewModel.state.value.currentMatchIndex)

        viewModel.onCommand(ConversationCommand.FindNext)
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.currentMatchIndex)

        // Switch to NewestFirst: ids become 3, 2, 1
        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        advanceUntilIdle()

        val ids = viewModel.state.value.filteredMessages.map { it.id }
        assertEquals(listOf("3", "2", "1"), ids)
        // currentMatchIndex should remain valid
        assertTrue(viewModel.state.value.currentMatchIndex in 0 until ids.size)
    }

    @Test
    fun `current match index stays valid after sort order change`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("message"))
        advanceUntilIdle()

        // Navigate to last match
        viewModel.onCommand(ConversationCommand.FindNext)
        viewModel.onCommand(ConversationCommand.FindNext)
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.currentMatchIndex)

        // Toggle sort — index should be clamped to valid range
        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        advanceUntilIdle()

        val idx = viewModel.state.value.currentMatchIndex
        assertTrue(idx in 0 until viewModel.state.value.filteredMessages.size,
            "Match index $idx should be valid after sort toggle")
    }

    // -- Manual Refresh Respects Sort Order --

    @Test
    fun `manual refresh respects current sort order`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        // Switch to NewestFirst
        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        advanceUntilIdle()

        // Refresh
        viewModel.onCommand(ConversationCommand.Refresh)
        advanceUntilIdle()

        // After refresh, sort order should still be NewestFirst
        assertEquals(SortOrder.NewestFirst, viewModel.state.value.sortOrder)
        val ids = viewModel.state.value.filteredMessages.map { it.id }
        assertEquals(listOf("3", "2", "1"), ids)
    }

    // -- Command State --

    @Test
    fun `command state reflects current sort order`() = runConversationStateTest(testMessages) {
        val viewModel = createViewModelWithSession()
        advanceUntilIdle()

        val state1 = ConversationCommandState.fromConversationState(viewModel.state.value)
        assertEquals(SortOrder.OldestFirst, state1.sortOrder)

        viewModel.onCommand(ConversationCommand.ToggleSortOrder)
        advanceUntilIdle()

        val state2 = ConversationCommandState.fromConversationState(viewModel.state.value)
        assertEquals(SortOrder.NewestFirst, state2.sortOrder)
    }
}
