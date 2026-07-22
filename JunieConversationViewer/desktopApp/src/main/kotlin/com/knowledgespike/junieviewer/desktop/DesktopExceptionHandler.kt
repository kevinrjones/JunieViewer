package com.knowledgespike.junieviewer.desktop

import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.ui.FatalErrorManager

/**
 * Installs the global uncaught exception handler for the desktop application.
 * Logs exceptions, checks FatalErrorManager for duplicate suppression,
 * and shows a Swing error dialog before exiting.
 */
fun installDesktopExceptionHandler() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        Logger.e(throwable) { "Unhandled exception on thread ${thread.name}" }
        System.err.println("CRITICAL: Unhandled exception on thread ${thread.name}")
        throwable.printStackTrace(System.err)

        if (FatalErrorManager.hasBeenReported(throwable)) {
            Logger.i { "Error already reported via FatalErrorManager, suppressing global dialog" }
            return@setDefaultUncaughtExceptionHandler
        }

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
}
