package com.knowledgespike.junieviewer

import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.domain.AppPreferences
import com.knowledgespike.junieviewer.domain.WindowPreferences

fun main() = application {
    val prefsRepo = remember { PreferencesRepository() }
    val initialPrefs = remember { prefsRepo.load() }

    val windowState = rememberWindowState(
        position = if (initialPrefs.window.x != null && initialPrefs.window.y != null) {
            WindowPosition(initialPrefs.window.x!!.dp, initialPrefs.window.y!!.dp)
        } else {
            WindowPosition.Aligned(Alignment.Center)
        },
        size = DpSize(initialPrefs.window.width.dp, initialPrefs.window.height.dp)
    )

    Window(
        onCloseRequest = {
            val position = windowState.position
            val size = windowState.size
            val x = (position as? WindowPosition.Absolute)?.x?.value?.toInt()
            val y = (position as? WindowPosition.Absolute)?.y?.value?.toInt()

            val finalPrefs = AppPreferences(
                window = WindowPreferences(
                    x = x,
                    y = y,
                    width = size.width.value.toInt(),
                    height = size.height.value.toInt()
                )
            )
            prefsRepo.save(finalPrefs)
            exitApplication()
        },
        state = windowState,
        title = "Junie Conversation Viewer",
    ) {
        App()
    }
}