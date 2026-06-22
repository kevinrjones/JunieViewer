package com.knowledgespike.junieviewer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform