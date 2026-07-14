package com.knowledgespike.junieviewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.data.SessionRepositoryImpl
import com.knowledgespike.junieviewer.domain.FilterCategory
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConversationViewModel(
    private val repository: SessionRepository = SessionRepositoryImpl(),
    private val preferencesRepository: PreferencesRepository = PreferencesRepository(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val logger = Logger.withTag("ConversationViewModel")
    private val _state = MutableStateFlow(ConversationState())
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.e(throwable) { "Unhandled coroutine exception" }
        FatalErrorManager.reportFatalError(throwable)
    }

    private val _events = Channel<ConversationEvent>()
    val events = _events.receiveAsFlow()

    init {
        logger.d { "ConversationViewModel initialized" }
        loadPreferences()
    }

    fun onAction(action: ConversationAction) {
        logger.d { "Action received: $action" }
        try {
            when (action) {
                is ConversationAction.OnSearchQueryChange -> {
                    _state.update { it.copy(searchQuery = action.query, currentMatchIndex = if (action.query.isBlank()) -1 else 0) }
                    filterMessages(action.query)
                }
                ConversationAction.OnRetryClick -> loadMessages()
                ConversationAction.OnToggleSessionPicker -> {
                    _state.update { it.copy(isSessionPickerOpen = !it.isSessionPickerOpen) }
                    if (_state.value.isSessionPickerOpen) {
                        loadSessions()
                    }
                }
                is ConversationAction.OnSessionSelected -> {
                    logger.i { "Session selected: ${action.session.id}" }
                    _state.update { it.copy(
                        selectedSessionId = action.session.id,
                        selectedSession = action.session,
                        isSessionPickerOpen = false,
                        errorMessage = null
                    ) }
                    saveLastSession(action.session.id)
                    loadMessages()
                }
                ConversationAction.OnToggleSettings -> {
                    _state.update { it.copy(isSettingsOpen = !it.isSettingsOpen) }
                }
                is ConversationAction.OnHomePathChange -> {
                    logger.i { "Home path changed: ${action.path}" }
                    _state.update { it.copy(junieHomePath = action.path) }
                    saveHomePath(action.path)
                }
                is ConversationAction.OnToggleFilter -> {
                    _state.update { currentState ->
                        val newFilter = when (action.kind) {
                            FilterKind.Human -> currentState.filter.copy(showHuman = !currentState.filter.showHuman)
                            FilterKind.Junie -> currentState.filter.copy(showJunie = !currentState.filter.showJunie)
                            FilterKind.Thought -> currentState.filter.copy(showThoughts = !currentState.filter.showThoughts)
                            FilterKind.Tool -> currentState.filter.copy(showTools = !currentState.filter.showTools)
                            FilterKind.Patch -> currentState.filter.copy(showPatches = !currentState.filter.showPatches)
                            FilterKind.Terminal -> currentState.filter.copy(showTerminal = !currentState.filter.showTerminal)
                        }
                        currentState.copy(filter = newFilter)
                    }
                    filterMessages(_state.value.searchQuery)
                }
                ConversationAction.OnNextMatch -> {
                    _state.update { currentState ->
                        val count = currentState.filteredMessages.size
                        if (count == 0) currentState.copy(currentMatchIndex = -1)
                        else currentState.copy(currentMatchIndex = (currentState.currentMatchIndex + 1).mod(count))
                    }
                }
                ConversationAction.OnPreviousMatch -> {
                    _state.update { currentState ->
                        val count = currentState.filteredMessages.size
                        if (count == 0) currentState.copy(currentMatchIndex = -1)
                        else currentState.copy(currentMatchIndex = (currentState.currentMatchIndex - 1).mod(count))
                    }
                }
                is ConversationAction.OnThemeModeChange -> {
                    logger.i { "Theme mode changed: ${action.themeMode}" }
                    _state.update { it.copy(themeMode = action.themeMode) }
                    saveThemeMode(action.themeMode)
                }
            }
        } catch (t: Throwable) {
            logger.e(t) { "Error processing action: $action" }
            FatalErrorManager.reportFatalError(t)
        }
    }

    private fun loadPreferences() {
        val prefs = preferencesRepository.load()
        logger.d { "Applying preferences to state: $prefs" }
        val themeMode = try {
            ThemeMode.valueOf(prefs.themeMode)
        } catch (_: IllegalArgumentException) {
            ThemeMode.System
        }
        _state.update { 
            it.copy(
                junieHomePath = prefs.junieHomePath,
                selectedSessionId = prefs.lastSessionId,
                themeMode = themeMode
            )
        }
        loadMessages()
    }

    private val prefsMutex = Any()

    private fun saveLastSession(sessionId: String) = synchronized(prefsMutex) {
        val currentPrefs = preferencesRepository.load()
        preferencesRepository.save(currentPrefs.copy(lastSessionId = sessionId))
    }

    private fun saveHomePath(path: String) = synchronized(prefsMutex) {
        val currentPrefs = preferencesRepository.load()
        preferencesRepository.save(currentPrefs.copy(junieHomePath = path))
    }

    private fun saveThemeMode(themeMode: ThemeMode) = synchronized(prefsMutex) {
        val currentPrefs = preferencesRepository.load()
        preferencesRepository.save(currentPrefs.copy(themeMode = themeMode.name))
    }

    private fun loadSessions() {
        viewModelScope.launch(exceptionHandler) {
            try {
                val sessions = withContext(ioDispatcher) {
                    repository.listSessions(_state.value.junieHomePath)
                }
                _state.update { it.copy(sessions = sessions) }
            } catch (e: Exception) {
                logger.e(e) { "Failed to load sessions" }
            }
        }
    }

    private fun loadMessages() {
        val sessionId = _state.value.selectedSessionId ?: run {
            logger.d { "loadMessages skipped: no session selected" }
            return
        }
        val homePath = _state.value.junieHomePath
        
        logger.i { "Loading messages for session: $sessionId" }
        viewModelScope.launch(exceptionHandler) {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val (messages, sessionInfo) = withContext(ioDispatcher) {
                    repository.setSession(sessionId, homePath)
                    val msgs = repository.getMessages()
                    val info = repository.getSessionInfo(sessionId, homePath)
                    msgs to info
                }
                _state.update { 
                    it.copy(
                        messages = messages,
                        filteredMessages = messages,
                        isLoading = false,
                        errorMessage = null,
                        selectedSession = sessionInfo ?: it.selectedSession
                    )
                }
                filterMessages(_state.value.searchQuery)
                logger.d { "Successfully loaded ${messages.size} messages" }
            } catch (e: Exception) {
                logger.e(e) { "Failed to load messages for session $sessionId" }
                _state.update { it.copy(
                    isLoading = false,
                    errorMessage = "Could not load this Conversation. Check that the Session still exists and try again."
                ) }
            }
        }
    }

    private fun filterMessages(query: String) {
        _state.update { currentState ->
            val filtered = currentState.messages.filter { message ->
                val kindMatch = when (message.kind.filterCategory) {
                    FilterCategory.Human -> currentState.filter.showHuman
                    FilterCategory.Junie -> {
                        if (message.sender == Sender.Human) currentState.filter.showHuman
                        else currentState.filter.showJunie
                    }
                    FilterCategory.Thought -> currentState.filter.showThoughts
                    FilterCategory.Tool -> currentState.filter.showTools
                    FilterCategory.Patch -> currentState.filter.showPatches
                    FilterCategory.Terminal -> currentState.filter.showTerminal
                    FilterCategory.AlwaysShow -> true
                }

                if (!kindMatch) return@filter false

                if (query.isBlank()) return@filter true

                messageContentText(message.content).contains(query, ignoreCase = true)
            }
            currentState.copy(filteredMessages = filtered)
        }
    }

    /** Extracts searchable plain text from any MessageContent variant. */
    private fun messageContentText(content: MessageContent): String = when (content) {
        is MessageContent.Text -> content.text
        is MessageContent.Code -> content.code
        is MessageContent.Diff -> content.diff
        is MessageContent.Terminal -> content.output
        is MessageContent.Structured -> content.data
    }
}
