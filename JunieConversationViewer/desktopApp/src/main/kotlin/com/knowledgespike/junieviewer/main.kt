package com.knowledgespike.junieviewer

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepositoryImpl
import com.knowledgespike.junieviewer.domain.AppPreferences
import com.knowledgespike.junieviewer.domain.WindowStatePreferences
import com.knowledgespike.junieviewer.ui.ConversationCommand
import com.knowledgespike.junieviewer.ui.ConversationCommandState
import com.knowledgespike.junieviewer.ui.ConversationViewModel
import com.knowledgespike.junieviewer.ui.SortOrder
import org.slf4j.LoggerFactory
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val platform = getPlatform()
    setupLogging(platform)

    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        Logger.e(throwable) { "Unhandled exception on thread ${thread.name}" }
        System.err.println("CRITICAL: Unhandled exception on thread ${thread.name}")
        throwable.printStackTrace(System.err)

        if (com.knowledgespike.junieviewer.ui.FatalErrorManager.hasBeenReported(throwable)) {
            Logger.i { "Error already reported via FatalErrorManager, suppressing global dialog" }
            return@setDefaultUncaughtExceptionHandler
        }

        javax.swing.SwingUtilities.invokeLater {
            javax.swing.JOptionPane.showMessageDialog(
                null,
                "The application encountered an unexpected technical problem and needs to close.\n\nDetails have been logged to the application logs.",
                "Unexpected Error",
                javax.swing.JOptionPane.ERROR_MESSAGE
            )
            System.exit(1)
        }
    }

    application {
        val prefsRepository = remember { PreferencesRepository() }
        val initialPrefs = remember {
            try {
                prefsRepository.load()
            } catch (e: Exception) {
                Logger.e(e) { "Failed to load initial preferences" }
                AppPreferences()
            }
        }

        // Create ViewModel at the application level so both MenuBar and App can access it
        val viewModel = remember {
            ConversationViewModel(
                repository = SessionRepositoryImpl(),
                preferencesRepository = PreferencesRepository(),
                liveSessionTracker = LiveSessionTracker()
            )
        }
        val state by viewModel.state.collectAsState()
        val commandState = ConversationCommandState.fromConversationState(state)

        val windowState = rememberWindowState(
            width = initialPrefs.window.width.dp,
            height = initialPrefs.window.height.dp,
            position = if (initialPrefs.window.x != null && initialPrefs.window.y != null) {
                WindowPosition(initialPrefs.window.x!!.dp, initialPrefs.window.y!!.dp)
            } else {
                WindowPosition(Alignment.Center)
            },
            placement =
                if (initialPrefs.window.isMaximized) WindowPlacement.Maximized else WindowPlacement.Floating
        )

        fun saveAndExit() {
            val finalPrefs = AppPreferences(
                window = WindowStatePreferences(
                    x = (windowState.position as? WindowPosition.Absolute)?.x?.value?.toInt(),
                    y = (windowState.position as? WindowPosition.Absolute)?.y?.value?.toInt(),
                    width = windowState.size.width.value.toInt(),
                    height = windowState.size.height.value.toInt(),
                    isMaximized = windowState.placement == WindowPlacement.Maximized
                )
            )
            prefsRepository.save(finalPrefs)
            exitApplication()
        }

        Window(
            onCloseRequest = ::saveAndExit,
            state = windowState,
            title = "Junie Conversation Viewer",
        ) {
            MenuBar {
                // File menu
                Menu("File") {
                    Item(
                        "Open Session…",
                        shortcut = KeyShortcut(Key.O, meta = true),
                        enabled = commandState.openSessionEnabled,
                        onClick = { viewModel.onCommand(ConversationCommand.OpenSession) }
                    )
                    Item(
                        "Refresh",
                        shortcut = KeyShortcut(Key.R, meta = true),
                        enabled = commandState.refreshEnabled,
                        onClick = { viewModel.onCommand(ConversationCommand.Refresh) }
                    )
                    Separator()
                    Item(
                        "Quit",
                        shortcut = KeyShortcut(Key.Q, meta = true),
                        onClick = { saveAndExit() }
                    )
                }

                // Edit menu
                Menu("Edit") {
                    Item(
                        "Copy",
                        shortcut = KeyShortcut(Key.C, meta = true),
                        enabled = commandState.copyEnabled,
                        onClick = { viewModel.onCommand(ConversationCommand.Copy) }
                    )
                    Separator()
                    Item(
                        "Find…",
                        shortcut = KeyShortcut(Key.F, meta = true),
                        enabled = commandState.focusSearchEnabled,
                        onClick = { viewModel.onCommand(ConversationCommand.FocusSearch) }
                    )
                    Item(
                        "Find Next",
                        shortcut = KeyShortcut(Key.G, meta = true),
                        enabled = commandState.findNextEnabled,
                        onClick = { viewModel.onCommand(ConversationCommand.FindNext) }
                    )
                    Item(
                        "Find Previous",
                        shortcut = KeyShortcut(Key.G, meta = true, shift = true),
                        enabled = commandState.findPreviousEnabled,
                        onClick = { viewModel.onCommand(ConversationCommand.FindPrevious) }
                    )
                }

                // View menu
                Menu("View") {
                    val sortLabel = when (commandState.sortOrder) {
                        SortOrder.OldestFirst -> "Switch to Newest First"
                        SortOrder.NewestFirst -> "Switch to Oldest First"
                    }
                    Item(
                        sortLabel,
                        enabled = commandState.toggleSortOrderEnabled,
                        onClick = { viewModel.onCommand(ConversationCommand.ToggleSortOrder) }
                    )
                    Separator()
                    Item(
                        "Collapse All",
                        shortcut = KeyShortcut(Key.Minus, meta = true, shift = true),
                        enabled = commandState.collapseAllEnabled,
                        onClick = { viewModel.onCommand(ConversationCommand.CollapseAll) }
                    )
                    Item(
                        "Show All",
                        shortcut = KeyShortcut(Key.Equals, meta = true, shift = true),
                        enabled = commandState.showAllEnabled,
                        onClick = { viewModel.onCommand(ConversationCommand.ShowAll) }
                    )
                    Separator()
                    val autoRefreshLabel = if (commandState.isAutoRefreshActive)
                        "Disable Auto-Refresh" else "Enable Auto-Refresh"
                    Item(
                        autoRefreshLabel,
                        shortcut = KeyShortcut(Key.R, meta = true, shift = true),
                        enabled = commandState.toggleAutoRefreshEnabled,
                        onClick = { viewModel.onCommand(ConversationCommand.ToggleAutoRefresh) }
                    )
                }

                // Session menu
                Menu("Session") {
                    Item(
                        "Reload from Disk",
                        enabled = commandState.refreshEnabled,
                        onClick = { viewModel.onCommand(ConversationCommand.Refresh) }
                    )
                }

                // Help menu
                Menu("Help") {
                    Item(
                        "How to Use",
                        onClick = { viewModel.onCommand(ConversationCommand.HowToUse) }
                    )
                    Item(
                        "About Junie Conversation Viewer",
                        onClick = { viewModel.onCommand(ConversationCommand.About) }
                    )
                }
            }

            App(externalViewModel = viewModel, onExit = ::exitApplication)
        }
    }
}

private fun setupLogging(platform: Platform) {
    val logsDir = File(platform.logsPath)
    if (!logsDir.exists()) {
        logsDir.mkdirs()
    }

    System.setProperty("LOG_DIR", platform.logsPath)

    // Check for external logback.xml in the same parent directory as preferences
    val configDir = File(platform.preferencesPath).parentFile
    val externalLogback = File(configDir, "logback.xml")
    if (externalLogback.exists()) {
        System.setProperty("logback.configurationFile", externalLogback.absolutePath)
    }

    Logger.setLogWriters(Slf4jLogger())
    Logger.i { "Logging initialized. Logs directory: ${platform.logsPath}" }
}

private class Slf4jLogger : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val logger = LoggerFactory.getLogger(tag)
        when (severity) {
            Severity.Verbose -> logger.trace(message, throwable)
            Severity.Debug -> logger.debug(message, throwable)
            Severity.Info -> logger.info(message, throwable)
            Severity.Warn -> logger.warn(message, throwable)
            Severity.Error -> logger.error(message, throwable)
            Severity.Assert -> logger.error("ASSERT: $message", throwable)
        }
    }
}