package com.knowledgespike.junieviewer

import co.touchlab.kermit.Logger

class JVMPlatform: Platform {
    private val logger = Logger.withTag("JVMPlatform")
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val userHome: String = System.getProperty("user.home") ?: ""
    override val preferencesPath: String by lazy {
        val os = System.getProperty("os.name").lowercase()
        val path = if (os.contains("win")) {
            val appData = System.getenv("LOCALAPPDATA") ?: userHome
            "$appData\\JunieViewer\\preferences.json"
        } else {
            "$userHome/.junieviewer/preferences.json"
        }
        logger.d { "Resolved preferences path: $path" }
        path
    }
}

actual fun getPlatform(): Platform = JVMPlatform()