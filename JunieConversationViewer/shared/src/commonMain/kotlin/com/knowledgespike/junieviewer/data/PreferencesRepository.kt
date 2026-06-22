package com.knowledgespike.junieviewer.data

import co.touchlab.kermit.Logger
import com.knowledgespike.junieviewer.domain.AppPreferences
import com.knowledgespike.junieviewer.getPlatform
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Repository for managing application preferences.
 */
class PreferencesRepository(
    private val path: Path = getPlatform().preferencesPath.toPath(),
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    private val logger = Logger.withTag("PreferencesRepository")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Loads the preferences from the platform-specific path.
     * Returns default preferences if the file doesn't exist or is invalid.
     */
    fun load(): AppPreferences {
        if (!fileSystem.exists(path)) {
            logger.i { "Preferences file not found at $path, returning defaults" }
            return AppPreferences()
        }
        
        return try {
            fileSystem.read(path) {
                val content = readUtf8()
                json.decodeFromString<AppPreferences>(content).also {
                    logger.d { "Preferences loaded from $path" }
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error loading preferences from $path" }
            AppPreferences()
        }
    }

    /**
     * Saves the preferences to the platform-specific path.
     */
    fun save(preferences: AppPreferences) {
        try {
            path.parent?.let { parent ->
                if (!fileSystem.exists(parent)) {
                    fileSystem.createDirectories(parent)
                }
            }
            fileSystem.write(path) {
                writeUtf8(json.encodeToString(AppPreferences.serializer(), preferences))
            }
            logger.d { "Preferences saved to $path" }
        } catch (e: Exception) {
            logger.e(e) { "Error saving preferences to $path" }
        }
    }
}
