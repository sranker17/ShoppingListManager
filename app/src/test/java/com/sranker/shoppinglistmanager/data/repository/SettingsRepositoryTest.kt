package com.sranker.shoppinglistmanager.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import app.cash.turbine.test

class SettingsRepositoryTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepository

    @BeforeEach
    fun setup() {
        val testFile = File(tempDir, "test.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { testFile }
        )
        repository = SettingsRepository(dataStore)
    }

    @Test
    fun `themeFlow returns default theme when empty`() = runTest {
        assertEquals(AppTheme.OCEAN_DARK, repository.themeFlow.first())
    }

    @Test
    fun `languageFlow returns default language when empty`() = runTest {
        assertEquals(Language.ENGLISH, repository.languageFlow.first())
    }

    @Test
    fun `textSizeFlow returns default size when empty`() = runTest {
        assertEquals(TextSizePreference.MEDIUM, repository.textSizeFlow.first())
    }

    @Test
    fun `setTheme updates preferences`() = runTest {
        repository.setTheme(AppTheme.FOREST_LIGHT)
        assertEquals(AppTheme.FOREST_LIGHT, repository.themeFlow.first())
    }

    @Test
    fun `setLanguage updates preferences`() = runTest {
        repository.setLanguage(Language.GERMAN)
        assertEquals(Language.GERMAN, repository.languageFlow.first())
    }

    @Test
    fun `setTextSize updates preferences`() = runTest {
        repository.setTextSize(TextSizePreference.LARGE)
        assertEquals(TextSizePreference.LARGE, repository.textSizeFlow.first())
    }
}
