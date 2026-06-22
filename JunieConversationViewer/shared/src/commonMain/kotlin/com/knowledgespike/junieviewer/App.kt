package com.knowledgespike.junieviewer

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.knowledgespike.junieviewer.ui.ConversationRoot
import com.knowledgespike.junieviewer.ui.ConversationViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel = viewModel { ConversationViewModel() }
        ConversationRoot(viewModel = viewModel)
    }
}
