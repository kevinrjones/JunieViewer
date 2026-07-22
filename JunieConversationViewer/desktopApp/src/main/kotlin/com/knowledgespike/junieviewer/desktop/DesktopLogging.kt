package com.knowledgespike.junieviewer.desktop

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.knowledgespike.junieviewer.Platform
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Configures desktop logging: creates logs directory, sets LOG_DIR property,
 * honours external logback configuration, and wires Kermit to SLF4J.
 *
 * @param platform The platform information for finding log paths.
 */
fun setupDesktopLogging(platform: Platform) {
    val logsDir = File(platform.logsPath)
    if (!logsDir.exists()) {
        logsDir.mkdirs()
    }

    System.setProperty("LOG_DIR", platform.logsPath)

    // Check for external logback.xml in the same parent directory as preferences
    val configDir = File(platform.preferencesPath).parentFile
    val externalLogback = File(configDir, "logback.xml")
    if (externalLogback.exists()) {
        System.setProperty("logback.configurationFile", externalLogback.absolutePath)
    }

    Logger.setLogWriters(Slf4jLogger())
    Logger.i { "Logging initialized. Logs directory: ${platform.logsPath}" }
}

/**
 * A Kermit [LogWriter] that bridges logs to SLF4J.
 */
private class Slf4jLogger : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val logger = LoggerFactory.getLogger(tag)
        when (severity) {
            Severity.Verbose -> logger.trace(message, throwable)
            Severity.Debug -> logger.debug(message, throwable)
            Severity.Info -> logger.info(message, throwable)
            Severity.Warn -> logger.warn(message, throwable)
            Severity.Error -> logger.error(message, throwable)
            Severity.Assert -> logger.error("ASSERT: $message", throwable)
        }
    }
}
