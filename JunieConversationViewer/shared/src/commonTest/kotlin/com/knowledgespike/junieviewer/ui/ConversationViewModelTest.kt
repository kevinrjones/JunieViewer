package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.domain.SessionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.FileSystem
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testMessages = listOf(
        Message("1", Sender.Human, MessageContent.Text("Hello")),
        Message("2", Sender.Junie, MessageContent.Text("Hello")),
        Message("3", Sender.Human, MessageContent.Text("Other")),
        Message("4", Sender.Junie, MessageContent.Text("Other"))
    )

    private val fakeRepository = object : SessionRepository {
        override fun getMessages(): List<Message> = testMessages
        override fun listSessions(homePath: String): List<SessionInfo> = emptyList()
        override fun setSession(sessionId: String, homePath: String) {}
    }

    private val fakePreferencesRepository = PreferencesRepository(
        path = "build/test-prefs.json".toPath(),
        fileSystem = FileSystem.SYSTEM
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        FileSystem.SYSTEM.delete("build/test-prefs.json".toPath())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given placeholder messages when initialised then state contains all placeholders`() = runTest {
        fakePreferencesRepository.save(com.knowledgespike.junieviewer.domain.AppPreferences(lastSessionId = "test-session"))
        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher)
        advanceUntilIdle()

        expectThat(viewModel.state.value.messages) {
            hasSize(4)
        }
        expectThat(viewModel.state.value.filteredMessages) {
            hasSize(4)
        }
    }

    @Test
    fun `given messages when search query changes then filtered messages are updated`() = runTest {
        fakePreferencesRepository.save(com.knowledgespike.junieviewer.domain.AppPreferences(lastSessionId = "test-session"))
        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher)
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))

        expectThat(viewModel.state.value.searchQuery).isEqualTo("Hello")
        expectThat(viewModel.state.value.filteredMessages).hasSize(2)
        expectThat(viewModel.state.value.filteredMessages.map { it.sender }).containsExactly(Sender.Human, Sender.Junie)
    }

    @Test
    fun `given search query when search query cleared then all messages shown`() = runTest {
        fakePreferencesRepository.save(com.knowledgespike.junieviewer.domain.AppPreferences(lastSessionId = "test-session"))
        val viewModel = ConversationViewModel(fakeRepository, fakePreferencesRepository, testDispatcher)
        advanceUntilIdle()

        viewModel.onAction(ConversationAction.OnSearchQueryChange("Hello"))
        viewModel.onAction(ConversationAction.OnSearchQueryChange(""))

        expectThat(viewModel.state.value.filteredMessages).hasSize(4)
    }
}
