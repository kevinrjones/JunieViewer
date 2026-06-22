package com.knowledgespike.junieviewer.data
 
import arrow.core.Either
import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.domain.JunieEvent
import kotlinx.serialization.json.Json
 
object JsonlParser {
    private val logger = Logger.withTag("JsonlParser")
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
 
    /**
     * Parses a single line from the .jsonl file into a [JunieEvent].
     */
    fun parseLine(line: String): Either<Throwable, JunieEvent> = Either.catch {
        json.decodeFromString<JunieEvent>(line)
    }.mapLeft { throwable ->
        logger.e(throwable) { "Failed to parse JSON line: $line" }
        throwable
    }
}
