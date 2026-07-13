package com.knowledgespike.junieviewer

import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.domain.AppPreferences
import com.knowledgespike.junieviewer.domain.WindowStatePreferences
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    val platform = getPlatform()
    setupLogging(platform)

    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        Logger.e(throwable) { "Unhandled exception on thread ${thread.name}" }
        System.err.println("CRITICAL: Unhandled exception on thread ${thread.name}")
        throwable.printStackTrace(System.err)

        // If the error was already reported through the UI manager, don't show another dialog
        if (com.knowledgespike.junieviewer.ui.FatalErrorManager.isErrorReported) {
            Logger.i { "Error already reported via FatalErrorManager, suppressing global dialog" }
            return@setDefaultUncaughtExceptionHandler
        }

        // Use Swing to show a dialog since the Compose runtime might be unstable
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
            App(onExit = ::exitApplication)
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