package com.knowledgespike.junieviewer

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import org.jetbrains.skia.Image
import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepositoryImpl
import com.knowledgespike.junieviewer.desktop.*
import com.knowledgespike.junieviewer.domain.AppPreferences
import com.knowledgespike.junieviewer.ui.ConversationCommandState
import com.knowledgespike.junieviewer.ui.ConversationViewModel

/**
 * Loads a bitmap image bundled on the Java classpath and wraps it in a [Painter].
 *
 * Replaces the deprecated `androidx.compose.ui.res.painterResource(String)`; per the Compose
 * migration guidance, classpath resources are decoded directly via Skia rather than the
 * Compose resources generator (the icon lives under `src/main/resources`, not `composeResources`).
 */
private fun classpathPainter(resourcePath: String): Painter {
    val bytes = checkNotNull(object {}.javaClass.getResourceAsStream(resourcePath)) {
        "Classpath resource not found: $resourcePath"
    }.use { it.readBytes() }
    return BitmapPainter(Image.makeFromEncoded(bytes).toComposeImageBitmap())
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val platform = getPlatform()
    setupDesktopLogging(platform)
    installDesktopExceptionHandler()

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

        val restoredWindowState = restoreWindowState(initialPrefs)
        val windowState = rememberWindowState(
            width = restoredWindowState.width.dp,
            height = restoredWindowState.height.dp,
            position = if (restoredWindowState.x != null && restoredWindowState.y != null) {
                WindowPosition(restoredWindowState.x.dp, restoredWindowState.y.dp)
            } else {
                WindowPosition(Alignment.Center)
            },
            placement = if (restoredWindowState.isMaximized) WindowPlacement.Maximized else WindowPlacement.Floating
        )

        val clipboardManager = remember { DesktopClipboardManager() }
        val windowIcon = remember { classpathPainter("/icons/icon.png") }

        fun saveAndExit() {
            val finalPrefs = AppPreferences(
                window = toWindowPreferences(
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
            icon = windowIcon,
        ) {
            JunieMenuBar(
                commandState = commandState,
                onCommand = { viewModel.onCommand(it) },
                onCopy = { clipboardManager.dispatchCopy(window) },
                onQuit = ::saveAndExit
            )

            App(
                externalViewModel = viewModel,
                onExit = ::exitApplication,
                onCopyText = {
                    clipboardManager.dispatchCopy(window)
                }
            )
        }
    }
}
