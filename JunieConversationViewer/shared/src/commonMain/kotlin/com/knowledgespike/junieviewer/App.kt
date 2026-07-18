package com.knowledgespike.junieviewer

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.knowledgespike.junieviewer.data.LiveSessionTracker
import com.knowledgespike.junieviewer.data.PreferencesRepository
import com.knowledgespike.junieviewer.data.SessionRepositoryImpl
import com.knowledgespike.junieviewer.ui.ConversationRoot
import com.knowledgespike.junieviewer.ui.ConversationViewModel
import com.knowledgespike.junieviewer.ui.FatalErrorManager
import com.knowledgespike.junieviewer.ui.components.FatalErrorDialog
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/**
 * Application entry point composable.
 * Wraps the entire UI in [JunieViewerTheme] with the persisted theme mode from preferences.
 */
@Composable
@Preview
fun App(onExit: () -> Unit = {}) {
    val viewModel = viewModel {
        ConversationViewModel(
            repository = SessionRepositoryImpl(),
            preferencesRepository = PreferencesRepository(),
            liveSessionTracker = LiveSessionTracker()
        )
    }
    val state by viewModel.state.collectAsState()

    JunieViewerTheme(themeMode = state.themeMode) {
        var fatalError by remember { mutableStateOf<Throwable?>(null) }

        LaunchedEffect(Unit) {
            FatalErrorManager.errors.collect {
                fatalError = it
            }
        }

        if (fatalError != null) {
            FatalErrorDialog(
                throwable = fatalError!!,
                onDismiss = onExit
            )
        }

        ConversationRoot(viewModel = viewModel)
    }
}
