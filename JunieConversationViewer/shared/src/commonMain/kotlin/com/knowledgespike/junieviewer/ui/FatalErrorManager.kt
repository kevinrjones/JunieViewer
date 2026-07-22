package com.knowledgespike.junieviewer.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages fatal error reporting. Tracks which errors have already been reported
 * to avoid duplicate handling while still allowing new distinct errors through.
 */
interface FatalErrorReporter {
    val errors: SharedFlow<Throwable>
    fun hasBeenReported(t: Throwable): Boolean
    fun reportFatalError(t: Throwable)
}

/**
 * Default implementation backed by a ConcurrentHashMap of reported error identities.
 * Unlike the previous global singleton with a permanent write-once flag, this tracks
 * individual errors so subsequent distinct exceptions are not silently swallowed.
 */
class DefaultFatalErrorReporter : FatalErrorReporter {
    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 1)
    override val errors: SharedFlow<Throwable> = _errors.asSharedFlow()

    private val reportedErrors: MutableSet<Throwable> = ConcurrentHashMap.newKeySet()

    override fun hasBeenReported(t: Throwable): Boolean = t in reportedErrors

    override fun reportFatalError(t: Throwable) {
        reportedErrors.add(t)
        _errors.tryEmit(t)
    }
}

/**
 * Global default instance for use in places where injection is not yet practical
 * (e.g. uncaught exception handler in main.kt). Prefer constructor injection where possible.
 * Implements [FatalErrorReporter] so it can be used directly as the production default for
 * constructor-injected consumers such as [ConversationViewModel].
 */
object FatalErrorManager : FatalErrorReporter {
    private val delegate = DefaultFatalErrorReporter()

    override val errors: SharedFlow<Throwable> get() = delegate.errors

    /** @return true if this specific throwable instance was already reported. */
    override fun hasBeenReported(t: Throwable): Boolean = delegate.hasBeenReported(t)

    override fun reportFatalError(t: Throwable) = delegate.reportFatalError(t)
}
