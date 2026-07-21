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
import com.knowledgespike.junieviewer.domain.groupMessagesIntoTurns
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
    private var lastLoadedLineCount: Int = 0

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
                    // Emit a CopyText event so the platform layer can dispatch a native
                    // copy action (synthetic Cmd+C / Ctrl+C) to the focused component.
                    logger.d { "Copy command: emitting CopyText event for platform handling" }
                    viewModelScope.launch { _events.send(ConversationEvent.CopyText) }
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
                    toggleSortOrder()
                }
                ConversationCommand.CollapseAll -> {
                    collapseAllBlocks()
                }
                ConversationCommand.ShowAll -> {
                    showAllBlocks()
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
                    // Single atomic emission per Search Query change (F8)
                    _state.update {
                        filterMessages(
                            it.copy(searchQuery = action.query, currentMatchIndex = if (action.query.isBlank()) -1 else 0),
                            action.query
                        )
                    }
                    logVisibleMessages()
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
                        filterMessages(currentState.copy(filter = newFilter), currentState.searchQuery)
                    }
                    logVisibleMessages()
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
                is ConversationAction.OnToggleBlockExpansion -> {
                    toggleBlockExpansion(action.blockId)
                }
                is ConversationAction.OnTextSelectionChanged -> {
                    updateTextSelection(action.containerId, action.hasSelection)
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
        val sortOrder = try {
            SortOrder.valueOf(prefs.sortOrder)
        } catch (_: IllegalArgumentException) {
            SortOrder.OldestFirst
        }
        _state.update { 
            it.copy(
                junieHomePath = prefs.junieHomePath,
                selectedSessionId = prefs.lastSessionId,
                themeMode = themeMode,
                isAutoRefreshEnabled = prefs.isAutoRefreshEnabled,
                sortOrder = sortOrder
            )
        }
        logger.d { "Preferences loaded: autoRefresh=${prefs.isAutoRefreshEnabled}, sortOrder=$sortOrder" }
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

    private fun saveSortOrder(sortOrder: SortOrder) =
        updatePreference { it.copy(sortOrder = sortOrder.name) }

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
                    filterMessages(
                        it.copy(
                            messages = loadResult.messages,
                            isLoading = false,
                            errorMessage = null,
                            selectedSession = sessionInfo ?: it.selectedSession
                        ),
                        it.searchQuery
                    )
                }
                logVisibleMessages()
                logger.d { "Successfully loaded ${loadResult.messages.size} messages" }

                // Cache load metadata for potential live tracking restart
                lastLoadedEventsFilePath = loadResult.eventsFilePath
                lastLoadedFileSize = loadResult.fileSizeAfterLoad
                lastLoadedLineCount = loadResult.totalLineCount

                // Start live tracking only when auto-refresh is enabled
                if (_state.value.isAutoRefreshEnabled) {
                    startLiveTracking(loadResult.eventsFilePath, loadResult.fileSizeAfterLoad, loadResult.totalLineCount + 1)
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
    private fun startLiveTracking(eventsFilePath: okio.Path?, initialOffset: Long, nextLineNumber: Int) {
        if (eventsFilePath == null) {
            logger.w { "Cannot start live tracking: no events file path" }
            return
        }

        logger.i { "Starting live tracking: path=$eventsFilePath, offset=$initialOffset, nextLine=$nextLineNumber" }
        liveTrackingJob = viewModelScope.launch(ioDispatcher + exceptionHandler) {
            liveSessionTracker.track(eventsFilePath, initialOffset, nextLineNumber)
                .collect { event ->
                    when (event) {
                        is LiveTrackingEvent.NewMessages -> {
                            logger.d { "Live tracking: ${event.messages.size} new messages" }
                            _state.update { currentState ->
                                val updatedMessages = currentState.messages + event.messages
                                filterMessages(currentState.copy(messages = updatedMessages), currentState.searchQuery)
                            }
                            logVisibleMessages()
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

    // ---------------------------------------------------------------------------
    // Collapse All / Show All / per-block toggle (Area 7)
    // ---------------------------------------------------------------------------

    /** Collapses all collapsible blocks by setting every known block ID to false. */
    private fun collapseAllBlocks() {
        val allIds = MessageContentRegistry.collectCollapsibleBlockIds(_state.value.messages)
        val collapsed = allIds.associateWith { false }
        _state.update { it.copy(blockExpansionStates = it.blockExpansionStates + collapsed) }
        logger.i { "Collapse All: ${allIds.size} blocks collapsed" }
    }

    /** Expands all collapsible blocks by setting every known block ID to true. */
    private fun showAllBlocks() {
        val allIds = MessageContentRegistry.collectCollapsibleBlockIds(_state.value.messages)
        val expanded = allIds.associateWith { true }
        _state.update { it.copy(blockExpansionStates = it.blockExpansionStates + expanded) }
        logger.i { "Show All: ${allIds.size} blocks expanded" }
    }

    /** Container ids that currently report an active text selection. */
    private val containersWithSelection = mutableSetOf<String>()

    /**
     * Records whether a tracked selection container currently holds a text selection and
     * updates [ConversationState.hasTextSelection], which drives Copy command enablement.
     */
    private fun updateTextSelection(containerId: String, hasSelection: Boolean) {
        if (hasSelection) containersWithSelection.add(containerId)
        else containersWithSelection.remove(containerId)
        val anySelection = containersWithSelection.isNotEmpty()
        if (_state.value.hasTextSelection != anySelection) {
            _state.update { it.copy(hasTextSelection = anySelection) }
            logger.d { "Text selection state changed: hasTextSelection=$anySelection" }
        }
    }

    /** Toggles the expansion state of a single block identified by its stable block ID. */
    private fun toggleBlockExpansion(blockId: String) {
        _state.update { currentState ->
            val currentExpanded = currentState.blockExpansionStates[blockId]
            // If no explicit state exists, the block is at its default — toggle from default
            val newExpanded = !(currentExpanded ?: true)
            currentState.copy(
                blockExpansionStates = currentState.blockExpansionStates + (blockId to newExpanded)
            )
        }
        logger.d { "Block toggled: $blockId" }
    }

    /**
     * Pure derivation of the visible Messages for [currentState] and the given Search Query.
     * Applies Filters, search matching, and sort order, regroups the result into Turns,
     * and returns the new state without emitting — callers fold it into a single
     * `_state.update` so each user action produces exactly one state emission.
     */
    private fun filterMessages(currentState: ConversationState, query: String): ConversationState =
        run {
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

                MessageContentRegistry.searchableText(message).contains(query, ignoreCase = true)
            }
            // Apply sort order: canonical messages list is always chronological;
            // reverse for NewestFirst display order
            val sorted = when (currentState.sortOrder) {
                SortOrder.OldestFirst -> filtered
                SortOrder.NewestFirst -> filtered.asReversed()
            }

            // Reset currentMatchIndex safely after re-derivation
            val newMatchIndex = when {
                sorted.isEmpty() -> -1
                currentState.currentMatchIndex < 0 -> if (query.isNotBlank() && sorted.isNotEmpty()) 0 else -1
                currentState.currentMatchIndex >= sorted.size -> 0
                else -> currentState.currentMatchIndex
            }

            currentState.copy(
                filteredMessages = sorted,
                turns = groupMessagesIntoTurns(sorted),
                currentMatchIndex = newMatchIndex
            )
        }

    /** Logs the size of the derived visible Message list after a state emission. */
    private fun logVisibleMessages() =
        logger.d { "Visible messages derived: ${_state.value.filteredMessages.size} (sortOrder=${_state.value.sortOrder})" }

    /**
     * Toggles sort order between OldestFirst and NewestFirst.
     * Re-derives visible Messages and persists the new preference.
     */
    private fun toggleSortOrder() {
        val newOrder = when (_state.value.sortOrder) {
            SortOrder.OldestFirst -> SortOrder.NewestFirst
            SortOrder.NewestFirst -> SortOrder.OldestFirst
        }
        _state.update { filterMessages(it.copy(sortOrder = newOrder), it.searchQuery) }
        logger.i { "Sort order toggled: $newOrder" }
        saveSortOrder(newOrder)
    }
}
