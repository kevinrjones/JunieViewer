package com.knowledgespike.junieviewer

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.knowledgespike.junieviewer.ui.ConversationRoot
import com.knowledgespike.junieviewer.ui.ConversationViewModel
import com.knowledgespike.junieviewer.ui.FatalErrorManager
import com.knowledgespike.junieviewer.ui.components.FatalErrorDialog

@Composable
@Preview
fun App(onExit: () -> Unit = {}) {
    MaterialTheme {
        val viewModel = viewModel { ConversationViewModel() }

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
