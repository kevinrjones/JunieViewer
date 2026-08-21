package com.knowledgespike.junieviewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.LiveTrackingEvent
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepository
import com.knowledgespike.junieviewer.domain.AppPreferences
import com.knowledgespike.junieviewer.domain.SessionInfo
import com.knowledgespike.junieviewer.domain.TopLevelSearchQuery
import com.knowledgespike.junieviewer.domain.TopLevelSearchResults
import com.knowledgespike.junieviewer.domain.TopLevelSearchStatus
import com.knowledgespike.junieviewer.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Thin coordinator for the conversation screen. Dispatches [ConversationCommand]s and
 * [ConversationAction]s, and coordinates focused collaborators that own the actual behaviour:
 * [MessageVisibilityEngine] (filter/sort/match derivation), [BlockExpansionController]
 * (per-block collapse/expand derivation), and [LiveTrackingController] (live-tracking
 * lifecycle). Preferences persistence and error reporting are also delegated out so this
 * class stays focused on routing user intent to state transitions.
 */
class ConversationViewModel(
    private val repository: SessionRepository,
    private val preferencesRepository: PreferencesRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val liveSessionTracker: LiveSessionTracker,
    private val errorReporter: FatalErrorReporter = FatalErrorManager
) : ViewModel() {

    private val logger = Logger.withTag("ConversationViewModel")
    private val _state = MutableStateFlow(ConversationState())
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.e(throwable) { "Unhandled coroutine exception" }
        errorReporter.reportFatalError(throwable)
    }

    private val _events = Channel<ConversationEvent>()
    val events = _events.receiveAsFlow()

    /** Owns the live-tracking coroutine lifecycle and last-loaded session metadata. */
    private val liveTracking = LiveTrackingController(
        liveSessionTracker = liveSessionTracker,
        scope = viewModelScope,
        launchContext = ioDispatcher + exceptionHandler,
        logger = logger
    )

    init {
        logger.d { "ConversationViewModel initialized" }
        loadPreferences()
    }

    /**
     * Handles a [ConversationCommand] dispatched from the toolbar, menu, or keyboard shortcut.
     * Commands with a direct [ConversationAction] equivalent are routed through [onAction];
     * the rest are handled directly.
     */
    fun onCommand(command: ConversationCommand) {
        logger.d { "Command received: $command" }
        dispatch("command: $command") {
            command.toActionOrNull()?.let(::onAction) ?: handleCommandDirectly(command)
        }
    }

    /** Handles the [ConversationCommand]s that have no direct [ConversationAction] equivalent. */
    private fun handleCommandDirectly(command: ConversationCommand) {
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
            ConversationCommand.ToggleAutoRefresh -> toggleAutoRefresh()
            ConversationCommand.ToggleSortOrder -> toggleSortOrder()
            ConversationCommand.CollapseAll -> collapseAllBlocks()
            ConversationCommand.ShowAll -> showAllBlocks()
            ConversationCommand.FocusSearch -> {
                // Handled at the UI level via FocusRequester — emit event
                viewModelScope.launch { _events.send(ConversationEvent.FocusSearch) }
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
            // These commands are routed to onAction via ConversationCommand.toActionOrNull()
            // and never reach this function.
            ConversationCommand.OpenSession,
            ConversationCommand.OpenTopLevelSearch,
            ConversationCommand.FindNext,
            ConversationCommand.FindPrevious,
            ConversationCommand.Settings -> Unit
        }
    }

    fun onAction(action: ConversationAction) {
        logger.d { "Action received: $action" }
        dispatch("action: $action") {
            when (action) {
                is ConversationAction.OnSearchQueryChange -> {
                    // Single atomic emission per Search Query change (F8)
                    updateState {
                        it.copy(
                            search = it.search.copy(
                                searchQuery = action.query,
                                currentMatchIndex = if (action.query.isBlank()) -1 else 0
                            ),
                            // Clear force-expansion dismissals when Search Query changes
                            blockExpansion = it.blockExpansion.copy(dismissedForceExpandedBlockIds = emptySet())
                        )
                    }
                    logVisibleMessages()
                }
                ConversationAction.OnToggleTopLevelSearch -> {
                    val willOpen = !_state.value.isTopLevelSearchOpen
                    updateState {
                        it.copy(
                            topLevelSearch = it.topLevelSearch.copy(
                                isOpen = willOpen,
                                status = if (willOpen) it.topLevelSearch.status else TopLevelSearchStatus.Idle
                            )
                        )
                    }
                    if (willOpen) {
                        viewModelScope.launch { _events.send(ConversationEvent.FocusTopLevelSearch) }
                    }
                }
                is ConversationAction.OnTopLevelSearchQueryChange -> {
                    val normalizedQuery = TopLevelSearchQuery(action.query)
                    val nextStatus = if (normalizedQuery.isBlank) {
                        TopLevelSearchStatus.EmptyQuery
                    } else {
                        TopLevelSearchStatus.Idle
                    }

                    topLevelSearchJob?.cancel()
                    updateState {
                        it.copy(
                            topLevelSearch = it.topLevelSearch.copy(
                                query = normalizedQuery,
                                status = nextStatus,
                                results = TopLevelSearchResults(
                                    query = normalizedQuery,
                                    status = nextStatus
                                ),
                                selectedResult = null
                            )
                        )
                    }
                }
                ConversationAction.OnSubmitTopLevelSearch -> submitTopLevelSearch()
                ConversationAction.OnCancelTopLevelSearch -> {
                    topLevelSearchJob?.cancel()
                    updateState {
                        val query = it.topLevelSearch.query
                        it.copy(
                            topLevelSearch = it.topLevelSearch.copy(
                                status = TopLevelSearchStatus.Idle,
                                results = TopLevelSearchResults(
                                    query = query,
                                    status = TopLevelSearchStatus.Idle
                                ),
                                selectedResult = null
                            )
                        )
                    }
                }
                is ConversationAction.OnTopLevelSearchResultSelected -> {
                    val sessionId = action.result.session.sessionId
                    updateState {
                        it.copy(
                            topLevelSearch = it.topLevelSearch.copy(
                                selectedResult = action.result,
                                isOpen = false
                            ),
                            search = it.search.copy(searchQuery = "", currentMatchIndex = -1),
                            sessionLoad = it.sessionLoad.copy(
                                selectedSessionId = sessionId,
                                selectedSession = SessionInfo(
                                    id = sessionId,
                                    path = action.result.session.sessionPath,
                                    lastModified = action.result.session.sessionTimestampMillis ?: 0L
                                ),
                                errorMessage = null
                            )
                        )
                    }
                    saveLastSession(sessionId)
                    loadMessages()
                    viewModelScope.launch {
                        _events.send(ConversationEvent.TopLevelSearchResultSelected(sessionId))
                    }
                }
                ConversationAction.OnRetryClick -> loadMessages()
                ConversationAction.OnToggleSessionPicker -> {
                    _state.update { it.copy(dialogs = it.dialogs.copy(isSessionPickerOpen = !it.dialogs.isSessionPickerOpen)) }
                    if (_state.value.isSessionPickerOpen) {
                        loadSessions()
                    }
                }
                is ConversationAction.OnSessionSelected -> {
                    logger.i { "Session selected: ${action.session.id}" }
                    _state.update { it.copy(
                        sessionLoad = it.sessionLoad.copy(
                            selectedSessionId = action.session.id,
                            selectedSession = action.session,
                            errorMessage = null
                        ),
                        dialogs = it.dialogs.copy(isSessionPickerOpen = false)
                    ) }
                    saveLastSession(action.session.id)
                    loadMessages()
                }
                ConversationAction.OnToggleSettings -> {
                    _state.update { it.copy(dialogs = it.dialogs.copy(isSettingsOpen = !it.dialogs.isSettingsOpen)) }
                }
                is ConversationAction.OnHomePathChange -> {
                    logger.i { "Home path changed: ${action.path}" }
                    _state.update { it.copy(sessionLoad = it.sessionLoad.copy(junieHomePath = action.path)) }
                    saveHomePath(action.path)
                }
                is ConversationAction.OnToggleFilter -> {
                    updateState { currentState ->
                        val newFilter = when (action.kind) {
                            FilterKind.Human -> currentState.filter.copy(showHuman = !currentState.filter.showHuman)
                            FilterKind.Junie -> currentState.filter.copy(showJunie = !currentState.filter.showJunie)
                            FilterKind.Thought -> currentState.filter.copy(showThoughts = !currentState.filter.showThoughts)
                            FilterKind.Tool -> currentState.filter.copy(showTools = !currentState.filter.showTools)
                            FilterKind.Patch -> currentState.filter.copy(showPatches = !currentState.filter.showPatches)
                            FilterKind.Terminal -> currentState.filter.copy(showTerminal = !currentState.filter.showTerminal)
                        }
                        currentState.copy(search = currentState.search.copy(filter = newFilter))
                    }
                    logVisibleMessages()
                }
                ConversationAction.OnNextMatch -> {
                    updateState { currentState ->
                        val count = currentState.filteredMessages.size
                        val newIndex = if (count == 0) -1 else (currentState.currentMatchIndex + 1).mod(count)
                        currentState.copy(search = currentState.search.copy(currentMatchIndex = newIndex))
                    }
                }
                ConversationAction.OnPreviousMatch -> {
                    updateState { currentState ->
                        val count = currentState.filteredMessages.size
                        val newIndex = if (count == 0) -1 else (currentState.currentMatchIndex - 1).mod(count)
                        currentState.copy(search = currentState.search.copy(currentMatchIndex = newIndex))
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
        }
    }

    private var topLevelSearchJob: Job? = null

    private fun submitTopLevelSearch() {
        val query = _state.value.topLevelSearch.query
        topLevelSearchJob?.cancel()
        if (query.isBlank) {
            updateState {
                it.copy(
                    topLevelSearch = it.topLevelSearch.copy(
                        status = TopLevelSearchStatus.EmptyQuery,
                        results = TopLevelSearchResults(query = query, status = TopLevelSearchStatus.EmptyQuery),
                        selectedResult = null
                    )
                )
            }
            return
        }

        updateState {
            it.copy(
                topLevelSearch = it.topLevelSearch.copy(
                    status = TopLevelSearchStatus.Running,
                    results = TopLevelSearchResults(query = query, status = TopLevelSearchStatus.Running),
                    selectedResult = null
                )
            )
        }

        topLevelSearchJob = viewModelScope.launch(exceptionHandler) {
            val homePath = _state.value.sessionLoad.junieHomePath
            val results = withContext(ioDispatcher) { repository.searchSessions(query, homePath) }
            val finalResults = results.copy(query = TopLevelSearchQuery(query.raw))
            
            updateState {
                it.copy(
                    topLevelSearch = it.topLevelSearch.copy(
                        status = finalResults.status,
                        results = finalResults
                    )
                )
            }
            _events.send(ConversationEvent.TopLevelSearchSubmitted(finalResults))
        }
    }

    /**
     * Runs [block], logging and reporting any exception through [errorReporter] with [label]
     * as context. Shared by [onCommand] and [onAction] so error handling is defined once.
     */
    private inline fun dispatch(label: String, block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            logger.e(t) { "Error processing $label" }
            errorReporter.reportFatalError(t)
        }
    }

    /**
     * Applies [transform] to the current state and re-derives filtered messages, turns, and
     * block-expansion state so every state transition emits a single, fully-consistent
     * snapshot. This is the one place [MessageVisibilityEngine] and [BlockExpansionController]
     * derivation is wired together — call sites only describe what changed.
     */
    private fun updateState(transform: (ConversationState) -> ConversationState) {
        _state.update { current ->
            val transformed = transform(current)
            val filtered = filterMessages(transformed, transformed.searchQuery)
            filtered.copy(
                blockExpansion = filtered.blockExpansion.copy(
                    derivedBlockExpansionStates = BlockExpansionController.deriveBlockExpansionStates(filtered)
                )
            )
        }
    }

    private fun loadPreferences() {
        val prefs = preferencesRepository.load()
        logger.d { "Applying preferences to state: $prefs" }
        val themeMode = enumValueOfOrDefault(prefs.themeMode, ThemeMode.System)
        val sortOrder = enumValueOfOrDefault(prefs.sortOrder, SortOrder.OldestFirst)
        _state.update { 
            it.copy(
                sessionLoad = it.sessionLoad.copy(
                    junieHomePath = prefs.junieHomePath,
                    selectedSessionId = prefs.lastSessionId
                ),
                search = it.search.copy(sortOrder = sortOrder),
                themeMode = themeMode,
                isAutoRefreshEnabled = prefs.isAutoRefreshEnabled
            )
        }
        logger.d { "Preferences loaded: autoRefresh=${prefs.isAutoRefreshEnabled}, sortOrder=$sortOrder" }
        loadMessages()
    }

    /**
     * Parses [name] as a [T] enum constant, logging a warning and falling back to [default]
     * when [name] does not match any constant (e.g. corrupt or outdated preferences).
     */
    private inline fun <reified T : Enum<T>> enumValueOfOrDefault(name: String, default: T): T =
        try {
            enumValueOf<T>(name)
        } catch (_: IllegalArgumentException) {
            logger.w { "Invalid ${T::class.simpleName} value '$name' in preferences — defaulting to $default" }
            default
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
            val metadata = liveTracking.lastLoadedMetadata
            if (metadata.eventsFilePath != null && _state.value.selectedSessionId != null) {
                val currentMessageCount = _state.value.messages.size
                // Use current message count and recalculate approximate file offset
                logger.i { "Restarting live tracking after auto-refresh enabled" }
                startLiveTracking(metadata.eventsFilePath, metadata.fileSize, currentMessageCount)
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
                _state.update { it.copy(sessionLoad = it.sessionLoad.copy(sessions = sessions)) }
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
            _state.update { it.copy(sessionLoad = it.sessionLoad.copy(isLoading = true, errorMessage = null)) }
            try {
                val (loadResult, sessionInfo) = withContext(ioDispatcher) {
                    val result = repository.loadSession(sessionId, homePath)
                    val info = repository.getSessionInfo(sessionId, homePath)
                    result to info
                }
                updateState {
                    it.copy(
                        sessionLoad = it.sessionLoad.copy(
                            messages = loadResult.messages,
                            isLoading = false,
                            errorMessage = null,
                            selectedSession = sessionInfo ?: it.selectedSession
                        )
                    )
                }
                logVisibleMessages()
                logger.d { "Successfully loaded ${loadResult.messages.size} messages" }

                // Cache load metadata for potential live tracking restart
                liveTracking.rememberLoadedMetadata(
                    LoadedSessionMetadata(
                        eventsFilePath = loadResult.eventsFilePath,
                        fileSize = loadResult.fileSizeAfterLoad,
                        lineCount = loadResult.totalLineCount
                    )
                )

                // Start live tracking only when auto-refresh is enabled
                if (_state.value.isAutoRefreshEnabled) {
                    startLiveTracking(loadResult.eventsFilePath, loadResult.fileSizeAfterLoad, loadResult.totalLineCount + 1)
                } else {
                    logger.i { "Auto-refresh disabled — skipping live tracking after load" }
                }
            } catch (e: Exception) {
                logger.e(e) { "Failed to load messages for session $sessionId" }
                _state.update { it.copy(
                    sessionLoad = it.sessionLoad.copy(
                        isLoading = false,
                        errorMessage = "Could not load this Conversation. Check that the Session still exists and try again."
                    )
                ) }
            }
        }
    }

    /** Starts live tracking for the given events.jsonl file, applying each event to state. */
    private fun startLiveTracking(eventsFilePath: okio.Path?, initialOffset: Long, nextLineNumber: Int) {
        liveTracking.start(eventsFilePath, initialOffset, nextLineNumber) { event ->
            when (event) {
                is LiveTrackingEvent.NewMessages -> {
                    logger.d { "Live tracking: ${event.messages.size} new messages" }
                    updateState { currentState ->
                        currentState.copy(sessionLoad = currentState.sessionLoad.copy(messages = currentState.messages + event.messages))
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

    /** Cancels the current live tracking job if active. */
    internal fun stopLiveTracking() = liveTracking.stop()

    override fun onCleared() {
        super.onCleared()
        stopLiveTracking()
        logger.d { "ConversationViewModel cleared" }
    }

    // ---------------------------------------------------------------------------
    // Collapse All / Show All / per-block toggle (Area 6 — centralized expansion)
    // ---------------------------------------------------------------------------

    /** Collapses all collapsible blocks by setting every known block ID to false. */
    private fun collapseAllBlocks() {
        val allIds = MessageContentRegistry.collectCollapsibleBlockIds(_state.value.messages)
        updateState { BlockExpansionController.collapseAll(it) }
        logger.i { "Collapse All: ${allIds.size} blocks collapsed" }
    }

    /** Expands all collapsible blocks by setting every known block ID to true. */
    private fun showAllBlocks() {
        val allIds = MessageContentRegistry.collectCollapsibleBlockIds(_state.value.messages)
        updateState { BlockExpansionController.showAll(it) }
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
            _state.update { it.copy(blockExpansion = it.blockExpansion.copy(hasTextSelection = anySelection)) }
            logger.d { "Text selection state changed: hasTextSelection=$anySelection" }
        }
    }

    /**
     * Toggles the expansion state of a single block identified by its stable block ID.
     * Delegates the actual derivation to [BlockExpansionController.toggle].
     */
    private fun toggleBlockExpansion(blockId: String) {
        updateState { BlockExpansionController.toggle(it, blockId) }
        logger.d { "Block toggled: $blockId" }
    }

    /**
     * Pure derivation of the visible Messages for [currentState] and the given Search Query.
     * Delegates to [MessageVisibilityEngine] and folds the result back into the state without
     * emitting — callers fold it into a single `_state.update` via [updateState] so each user
     * action produces exactly one state emission.
     */
    private fun filterMessages(currentState: ConversationState, query: String): ConversationState {
        val result = MessageVisibilityEngine.derive(
            messages = currentState.messages,
            filter = currentState.filter,
            sortOrder = currentState.sortOrder,
            query = query,
            currentMatchIndex = currentState.currentMatchIndex
        )
        return currentState.copy(
            search = currentState.search.copy(
                filteredMessages = result.filteredMessages,
                turns = result.turns,
                currentMatchIndex = result.currentMatchIndex
            )
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
        updateState { it.copy(search = it.search.copy(sortOrder = newOrder)) }
        logger.i { "Sort order toggled: $newOrder" }
        saveSortOrder(newOrder)
    }
}
