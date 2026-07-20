package com.knowledgespike.junieviewer.ui.components;

import androidx.compose.foundation.text.selection.Selection;
import androidx.compose.foundation.text.selection.SelectionContainerKt;
import androidx.compose.runtime.Composer;
import androidx.compose.ui.Modifier;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

/**
 * Java bridge to the selection-aware {@code SelectionContainer} overload in Compose Foundation.
 *
 * <p>Compose Foundation declares a stateful {@code SelectionContainer(modifier, selection,
 * onSelectionChange, children)} overload that reports selection changes, but it is marked
 * Kotlin-{@code internal}. Kotlin {@code internal} visibility is not enforced at the JVM
 * bytecode level (the function is {@code public static} in {@code SelectionContainerKt}),
 * so this Java class can call it legally. This gives the application access to real text
 * selection state, which is required to enable/disable the Edit &gt; Copy menu item the way
 * a standard desktop application does.</p>
 *
 * <p>If a future Compose Foundation version promotes the stateful overload to public API,
 * this bridge can be deleted and replaced with a direct Kotlin call.</p>
 */
public final class SelectionContainerBridge {

    private SelectionContainerBridge() {
    }

    /**
     * Invokes the internal selection-aware {@code SelectionContainer} composable.
     *
     * <p>The {@code Selection} type is Kotlin-internal and therefore cannot be named from
     * Kotlin code, so it is handled here as an opaque {@link Object}: callers hold the
     * current selection as {@code Any?} and receive selection changes as {@code Any?}.</p>
     *
     * @param modifier          modifier for the container
     * @param selection         the current selection state (an opaque {@code Selection}, nullable)
     * @param onSelectionChange callback invoked whenever the selection changes (receives the
     *                          new opaque selection, or null when the selection is cleared)
     * @param children          the composable content (a composable lambda at runtime)
     * @param composer          the current composer, obtained via {@code currentComposer}
     * @param changed           the changed flags; pass 0 to force parameter comparison
     */
    public static void SelectionContainer(
            Modifier modifier,
            Object selection,
            Function1<Object, Unit> onSelectionChange,
            Function2<? super Composer, ? super Integer, Unit> children,
            Composer composer,
            int changed) {
        Function1<Selection, Unit> adapted = onSelectionChange::invoke;
        SelectionContainerKt.SelectionContainer(modifier, (Selection) selection, adapted, children, composer, changed, 0);
    }
}
