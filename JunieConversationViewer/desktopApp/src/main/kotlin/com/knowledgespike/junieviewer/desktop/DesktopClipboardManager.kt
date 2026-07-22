package com.knowledgespike.junieviewer.desktop

import co.touchlab.kermit.Logger

/**
 * Manages synthetic clipboard copy operations for the desktop application.
 * Dispatches Cmd+C / Ctrl+C key events to the focused Compose component
 * so SelectionContainer handles the copy internally.
 */
class DesktopClipboardManager {
    /**
     * Re-entrancy guard for [dispatchCopy]. When the Copy menu item carries the
     * Cmd+C / Ctrl+C accelerator, the synthetic copy key event posted by the menu action can
     * itself be matched by the menu shortcut (AWT re-dispatches unconsumed shortcuts to the
     * menu), which would re-invoke the menu action forever and hang the app. While this flag
     * is set, further dispatch requests are ignored. All access happens on the AWT EDT.
     */
    private var syntheticCopyInFlight = false

    /**
     * Dispatches a synthetic Cmd+C (macOS) or Ctrl+C (Windows/Linux) key event to the
     * given AWT window. Compose Desktop's [SelectionContainer] handles the copy internally
     * when it receives this key event, copying any currently selected text to the clipboard.
     * If no text is selected, nothing happens (standard desktop behaviour).
     *
     * @param target The AWT window to dispatch the event to.
     */
    fun dispatchCopy(target: java.awt.Window) {
        if (syntheticCopyInFlight) {
            Logger.d { "Ignoring re-entrant synthetic copy dispatch" }
            return
        }
        val modifiers = if (System.getProperty("os.name").lowercase().contains("mac"))
            java.awt.event.InputEvent.META_DOWN_MASK
        else
            java.awt.event.InputEvent.CTRL_DOWN_MASK
        // Target the current keyboard focus owner (the Compose panel) so the key event
        // reaches Compose's key handling; fall back to the window if nothing has focus.
        val focusOwner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner ?: target
        val queue = java.awt.Toolkit.getDefaultToolkit().systemEventQueue
        syntheticCopyInFlight = true
        // Post PRESSED and RELEASED asynchronously via the system event queue so the
        // dispatch happens after the menu action completes, never re-entrantly.
        queue.postEvent(
            java.awt.event.KeyEvent(
                focusOwner,
                java.awt.event.KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                modifiers,
                java.awt.event.KeyEvent.VK_C,
                'c'
            )
        )
        queue.postEvent(
            java.awt.event.KeyEvent(
                focusOwner,
                java.awt.event.KeyEvent.KEY_RELEASED,
                System.currentTimeMillis(),
                modifiers,
                java.awt.event.KeyEvent.VK_C,
                'c'
            )
        )
        // Clear the guard after both key events have been processed. invokeLater posts an
        // InvocationEvent behind the two key events on the same queue, so the flag is
        // guaranteed to still be set if the menu shortcut re-fires during their dispatch.
        java.awt.EventQueue.invokeLater { syntheticCopyInFlight = false }
        Logger.d { "Dispatched synthetic copy key event to focus owner" }
    }
}
