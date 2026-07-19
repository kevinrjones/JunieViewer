package com.knowledgespike.junieviewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.data.FileWatcher
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.LiveTrackingEvent
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.data.SessionRepositoryImpl
import com.knowledgespike.junieviewer.domain.AppPreferences
import com.knowledgespike.junieviewer.domain.FilterCategory
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileSystem

class ConversationViewModel(
    private val repository: SessionRepository,
    private val preferencesRepository: PreferencesRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val liveSessionTracker: LiveSessionTracker
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

    /** Job for the current live tracking coroutine — cancelled when Session changes. */
    private var liveTrackingJob: Job? = null

    /** Cached metadata from the last successful load, used to restart live tracking. */
    private var lastLoadedEventsFilePath: okio.Path? = null
    private var lastLoadedFileSize: Long = 0L
    private var lastLoadedMessageCount: Int = 0

    init {
        logger.d { "ConversationViewModel initialized" }
        loadPreferences()
    }

    /**
     * Handles a [ConversationCommand] dispatched from the toolbar, menu, or keyboard shortcut.
     * Maps each command to existing [ConversationAction] handling or updates state directly.
     */
    fun onCommand(command: ConversationCommand) {
        logger.d { "Command received: $command" }
        try {
            when (command) {
                ConversationCommand.Copy -> {
                    // TODO Area 8: selected-text copy via OS shortcut passthrough.
                    // Currently a no-op — Compose Desktop does not expose selected text.
                    logger.d { "Copy command: no-op (selected-text detection not available)" }
                }
                ConversationCommand.Refresh -> {
                    logger.i { "Refresh command: reloading current Session" }
                    refreshSession()
                }
                ConversationCommand.OpenSession -> {
                    onAction(ConversationAction.OnToggleSessionPicker)
                }
                ConversationCommand.ToggleAutoRefresh -> {
                    toggleAutoRefresh()
                }
                ConversationCommand.ToggleSortOrder -> {
                    // TODO Area 6: apply actual sort to filteredMessages
                    _state.update { currentState ->
                        val newOrder = when (currentState.sortOrder) {
                            SortOrder.OldestFirst -> SortOrder.NewestFirst
                            SortOrder.NewestFirst -> SortOrder.OldestFirst
                        }
                        currentState.copy(sortOrder = newOrder)
                    }
                    logger.i { "Sort order toggled: ${_state.value.sortOrder}" }
                }
                ConversationCommand.CollapseAll -> {
                    // TODO Area 7: emit global collapse event to all CollapsibleBlock instances
                    logger.d { "CollapseAll command: stub (full implementation in Area 7)" }
                }
                ConversationCommand.ShowAll -> {
                    // TODO Area 7: emit global expand event to all CollapsibleBlock instances
                    logger.d { "ShowAll command: stub (full implementation in Area 7)" }
                }
                ConversationCommand.FocusSearch -> {
                    // Handled at the UI level via FocusRequester — emit event
                    viewModelScope.launch { _events.send(ConversationEvent.FocusSearch) }
                }
                ConversationCommand.FindNext -> {
                    onAction(ConversationAction.OnNextMatch)
                }
                ConversationCommand.FindPrevious -> {
                    onAction(ConversationAction.OnPreviousMatch)
                }
                ConversationCommand.Settings -> {
                    onAction(ConversationAction.OnToggleSettings)
                }
                ConversationCommand.Quit -> {
                    // Handled at the platform/Window level
                    logger.d { "Quit command: handled at platform level" }
                }
                ConversationCommand.About -> {
                    viewModelScope.launch { _events.send(ConversationEvent.ShowAbout) }
                }
                ConversationCommand.HowToUse -> {
                    viewModelScope.launch { _events.send(ConversationEvent.ShowHowToUse) }
                }
            }
        } catch (t: Throwable) {
            logger.e(t) { "Error processing command: $command" }
            FatalErrorManager.reportFatalError(t)
        }
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
                themeMode = themeMode,
                isAutoRefreshEnabled = prefs.isAutoRefreshEnabled
            )
        }
        logger.d { "Auto-refresh preference loaded: ${prefs.isAutoRefreshEnabled}" }
        loadMessages()
    }

    private val prefsMutex = Any()

    /** Atomically loads, transforms, and saves preferences. */
    private fun updatePreference(transform: (AppPreferences) -> AppPreferences) =
        synchronized(prefsMutex) {
            preferencesRepository.save(transform(preferencesRepository.load()))
        }

    private fun saveLastSession(sessionId: String) =
        updatePreference { it.copy(lastSessionId = sessionId) }

    private fun saveHomePath(path: String) =
        updatePreference { it.copy(junieHomePath = path) }

    private fun saveThemeMode(themeMode: ThemeMode) =
        updatePreference { it.copy(themeMode = themeMode.name) }

    private fun saveAutoRefresh(enabled: Boolean) =
        updatePreference { it.copy(isAutoRefreshEnabled = enabled) }

    /**
     * Toggles auto-refresh on/off, starting or stopping live tracking accordingly.
     * Persists the new preference to disk.
     */
    private fun toggleAutoRefresh() {
        val newEnabled = !_state.value.isAutoRefreshEnabled
        _state.update { it.copy(isAutoRefreshEnabled = newEnabled) }
        logger.i { "Auto-refresh toggled: $newEnabled" }
        saveAutoRefresh(newEnabled)

        if (newEnabled) {
            // Restart live tracking for the current Session from cached metadata
            val path = lastLoadedEventsFilePath
            if (path != null && _state.value.selectedSessionId != null) {
                val currentMessageCount = _state.value.messages.size
                // Use current message count and recalculate approximate file offset
                logger.i { "Restarting live tracking after auto-refresh enabled" }
                startLiveTracking(path, lastLoadedFileSize, currentMessageCount)
            }
        } else {
            // Stop live tracking but keep Messages visible
            logger.i { "Stopping live tracking after auto-refresh disabled" }
            stopLiveTracking()
        }
    }

    /**
     * Refreshes the current Session by reloading from disk.
     * Preserves Search Query, Filters, and auto-refresh state.
     * Restarts live tracking only when auto-refresh is enabled.
     */
    private fun refreshSession() {
        if (_state.value.selectedSessionId == null) {
            logger.d { "Refresh skipped: no Session selected" }
            return
        }
        logger.i { "Refreshing current Session" }
        loadMessages()
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

        // Cancel any existing live tracking before starting a new load
        stopLiveTracking()
        
        logger.i { "Loading messages for session: $sessionId" }
        viewModelScope.launch(exceptionHandler) {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val (loadResult, sessionInfo) = withContext(ioDispatcher) {
                    repository.setSession(sessionId, homePath)
                    val result = repository.loadSession()
                    val info = repository.getSessionInfo(sessionId, homePath)
                    result to info
                }
                _state.update { 
                    it.copy(
                        messages = loadResult.messages,
                        filteredMessages = loadResult.messages,
                        isLoading = false,
                        errorMessage = null,
                        selectedSession = sessionInfo ?: it.selectedSession
                    )
                }
                filterMessages(_state.value.searchQuery)
                logger.d { "Successfully loaded ${loadResult.messages.size} messages" }

                // Cache load metadata for potential live tracking restart
                lastLoadedEventsFilePath = loadResult.eventsFilePath
                lastLoadedFileSize = loadResult.fileSizeAfterLoad
                lastLoadedMessageCount = loadResult.messages.size

                // Start live tracking only when auto-refresh is enabled
                if (_state.value.isAutoRefreshEnabled) {
                    startLiveTracking(loadResult.eventsFilePath, loadResult.fileSizeAfterLoad, loadResult.messages.size)
                } else {
                    logger.i { "Auto-refresh disabled — skipping live tracking after load" }
                }
            } catch (e: Exception) {
                logger.e(e) { "Failed to load messages for session $sessionId" }
                _state.update { it.copy(
                    isLoading = false,
                    errorMessage = "Could not load this Conversation. Check that the Session still exists and try again."
                ) }
            }
        }
    }

    /** Starts live tracking for the given events.jsonl file. */
    private fun startLiveTracking(eventsFilePath: okio.Path?, initialOffset: Long, existingMessageCount: Int) {
        if (eventsFilePath == null) {
            logger.w { "Cannot start live tracking: no events file path" }
            return
        }

        logger.i { "Starting live tracking: path=$eventsFilePath, offset=$initialOffset" }
        liveTrackingJob = viewModelScope.launch(ioDispatcher + exceptionHandler) {
            liveSessionTracker.track(eventsFilePath, initialOffset, existingMessageCount)
                .collect { event ->
                    when (event) {
                        is LiveTrackingEvent.NewMessages -> {
                            logger.d { "Live tracking: ${event.messages.size} new messages" }
                            _state.update { currentState ->
                                val updatedMessages = currentState.messages + event.messages
                                currentState.copy(messages = updatedMessages)
                            }
                            filterMessages(_state.value.searchQuery)
                        }
                        is LiveTrackingEvent.FileReset -> {
                            logger.i { "Live tracking: file reset — reloading session" }
                            withContext(Dispatchers.Main) { loadMessages() }
                        }
                        is LiveTrackingEvent.FileDeleted -> {
                            logger.w { "Live tracking: file deleted — stopping" }
                            // Don't crash, just stop tracking
                        }
                    }
                }
        }
    }

    /** Cancels the current live tracking job if active. */
    internal fun stopLiveTracking() {
        liveTrackingJob?.let {
            logger.i { "Stopping live tracking" }
            it.cancel()
            liveTrackingJob = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLiveTracking()
        logger.d { "ConversationViewModel cleared" }
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
