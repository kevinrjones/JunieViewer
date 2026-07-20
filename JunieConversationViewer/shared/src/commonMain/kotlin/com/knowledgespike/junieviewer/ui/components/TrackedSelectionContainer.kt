package com.knowledgespike.junieviewer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Reports whether a tracked selection container currently holds a text selection.
 * The first argument is a stable container id, the second is true while text is selected.
 */
typealias TextSelectionReporter = (containerId: String, hasSelection: Boolean) -> Unit

/**
 * CompositionLocal providing the reporter that [TrackedSelectionContainer] instances use
 * to publish text selection changes up to the ViewModel. Defaults to null (no tracking),
 * which keeps components usable in previews and tests without extra setup.
 */
val LocalTextSelectionReporter = staticCompositionLocalOf<TextSelectionReporter?> { null }

/** Monotonic counter used to give each tracked container a unique, stable id. */
private var nextTrackedContainerId = 0

/**
 * A drop-in replacement for [androidx.compose.foundation.text.selection.SelectionContainer]
 * that additionally reports whether text is currently selected inside it via
 * [LocalTextSelectionReporter]. This drives the enabled state of the global Copy command
 * (Edit menu and toolbar) so Copy behaves like a standard desktop application.
 *
 * When the container leaves the composition (for example through LazyColumn recycling),
 * it reports "no selection" so stale entries never keep Copy enabled.
 */
@Composable
fun TrackedSelectionContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val containerId = remember { "selection_container_${nextTrackedContainerId++}" }
    val reporter = rememberUpdatedState(LocalTextSelectionReporter.current)

    DisposableEffect(containerId) {
        onDispose { reporter.value?.invoke(containerId, false) }
    }

    PlatformSelectionContainer(
        modifier = modifier,
        onSelectionChange = { hasSelection -> reporter.value?.invoke(containerId, hasSelection) },
        content = content
    )
}

/**
 * Platform-specific selection container that reports selection presence changes.
 * The JVM implementation bridges to Compose Foundation's selection-aware overload.
 */
@Composable
internal expect fun PlatformSelectionContainer(
    modifier: Modifier,
    onSelectionChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
)
