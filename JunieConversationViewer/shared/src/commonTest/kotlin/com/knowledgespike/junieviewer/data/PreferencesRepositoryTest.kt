package com.knowledgespike.junieviewer.data

import com.knowledgespike.junieviewer.domain.AppPreferences
import com.knowledgespike.junieviewer.domain.WindowStatePreferences
import okio.FileSystem
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class PreferencesRepositoryTest {

    private val fileSystem = FileSystem.SYSTEM
    private val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "prefs_test_${System.currentTimeMillis()}"
    private val path = tempDir / "preferences.json"
    private val repository = PreferencesRepository(path = path, fileSystem = fileSystem)

    @Before
    fun setup() {
        fileSystem.createDirectories(tempDir)
    }

    @After
    fun tearDown() {
        fileSystem.deleteRecursively(tempDir)
    }

    @Test
    fun `given no preferences file when loading then it returns default preferences`() {
        val result = repository.load()
        expectThat(result).isEqualTo(AppPreferences())
    }

    @Test
    fun `given preferences saved when loading then it returns saved preferences`() {
        val preferences = AppPreferences(
            window = WindowStatePreferences(x = 100, y = 200, width = 1024, height = 768)
        )
        
        repository.save(preferences)
        val result = repository.load()
        
        expectThat(result).isEqualTo(preferences)
    }

    @Test
    fun `given invalid preferences file when loading then it returns default preferences`() {
        fileSystem.write(path) {
            writeUtf8("invalid json")
        }
        
        val result = repository.load()
        expectThat(result).isEqualTo(AppPreferences())
    }

    @Test
    fun `given preferences with themeMode when loading then it returns saved themeMode`() {
        val preferences = AppPreferences(themeMode = "Dark")
        repository.save(preferences)
        val result = repository.load()
        expectThat(result.themeMode).isEqualTo("Dark")
    }

    @Test
    fun `given preferences without themeMode field when loading then it defaults to System`() {
        // Simulate a legacy preferences file without the themeMode field
        fileSystem.write(path) {
            writeUtf8("""{"window":{},"junieHomePath":"~/.junie"}""")
        }
        val result = repository.load()
        expectThat(result.themeMode).isEqualTo("System")
    }

    @Test
    fun `given preferences with all fields when round-tripping then all fields are preserved`() {
        val preferences = AppPreferences(
            window = WindowStatePreferences(x = 50, y = 60, width = 1280, height = 720),
            junieHomePath = "/custom/path",
            lastSessionId = "session-123",
            themeMode = "Light"
        )
        repository.save(preferences)
        val result = repository.load()
        expectThat(result).isEqualTo(preferences)
    }
}
