package com.knowledgespike.junieviewer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * JVM implementation of [PlatformSelectionContainer].
 *
 * Bridges to Compose Foundation's selection-aware SelectionContainer overload via
 * [SelectionContainerBridge] so the app can observe whether text is currently selected.
 * The Selection type is Kotlin-internal, so it is held here as an opaque `Any?` — only
 * its presence (non-null) matters, which is mirrored to [onSelectionChange] as a Boolean
 * and ultimately drives the enabled state of the global Copy command.
 */
@Composable
internal actual fun PlatformSelectionContainer(
    modifier: Modifier,
    onSelectionChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    // Opaque Selection instance from Compose Foundation; null when nothing is selected.
    var selection by remember { mutableStateOf<Any?>(null) }
    val handleSelectionChange: (Any?) -> Unit = { newSelection ->
        selection = newSelection
        onSelectionChange(newSelection != null)
    }

    // The content parameter is a composable lambda, which is a Function2<Composer, Int, Unit>
    // at runtime — the cast adapts it for the Java bridge call.
    @Suppress("UNCHECKED_CAST")
    val children = content as Any as (Composer, Int) -> Unit

    // changed = 0 lets the composer compare parameters and skip/recompose as usual.
    SelectionContainerBridge.SelectionContainer(
        modifier,
        selection,
        handleSelectionChange,
        children,
        currentComposer,
        0
    )
}
