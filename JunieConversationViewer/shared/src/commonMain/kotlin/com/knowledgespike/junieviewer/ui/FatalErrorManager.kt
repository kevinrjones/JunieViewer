package com.knowledgespike.junieviewer.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object FatalErrorManager {
    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 1)
    val errors = _errors.asSharedFlow()

    @Volatile
    var isErrorReported = false
        private set

    fun reportFatalError(t: Throwable) {
        isErrorReported = true
        _errors.tryEmit(t)
    }
}
