package com.knowledgespike.junieviewer

interface Platform {
    val name: String
    val preferencesPath: String
    val logsPath: String
    val userHome: String
}

expect fun getPlatform(): Platform