package com.knowledgespike.junieviewer.data
 
import arrow.core.Either
import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.domain.JunieEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
 
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

    /**
     * Parses a serialized `AgentStateUpdatedEvent.blob` string as a JSON object and returns
     * its `currentDirectory` field, or null when the blob is malformed or carries no such field.
     */
    fun workingDirectoryFromAgentStateBlob(blob: String): String? = try {
        Json.parseToJsonElement(blob).jsonObject["currentDirectory"]
            ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        logger.d { "Skipping malformed agent-state blob: ${e.message}" }
        null
    }
}
