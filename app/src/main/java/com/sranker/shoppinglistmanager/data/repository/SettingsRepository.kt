package com.sranker.shoppinglistmanager.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppTheme { OCEAN_DARK, FOREST_LIGHT, SUNSET_DARK, SNOW_LIGHT, SKY_LIGHT, ROSE_LIGHT, SAND_LIGHT }
enum class Language { ENGLISH, HUNGARIAN, GERMAN }
enum class TextSizePreference { SMALL, MEDIUM, LARGE }

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val TEXT_SIZE_KEY = stringPreferencesKey("text_size")
    }

    val themeFlow: Flow<AppTheme> = dataStore.data.map { preferences ->
        val themeString = preferences[THEME_KEY] ?: AppTheme.OCEAN_DARK.name
        runCatching { AppTheme.valueOf(themeString) }.getOrDefault(AppTheme.OCEAN_DARK)
    }

    val languageFlow: Flow<Language> = dataStore.data.map { preferences ->
        val langString = preferences[LANGUAGE_KEY] ?: Language.ENGLISH.name
        runCatching { Language.valueOf(langString) }.getOrDefault(Language.ENGLISH)
    }

    val textSizeFlow: Flow<TextSizePreference> = dataStore.data.map { preferences ->
        val sizeString = preferences[TEXT_SIZE_KEY] ?: TextSizePreference.MEDIUM.name
        runCatching { TextSizePreference.valueOf(sizeString) }.getOrDefault(TextSizePreference.MEDIUM)
    }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.name
        }
    }

    suspend fun setTextSize(size: TextSizePreference) {
        dataStore.edit { preferences ->
            preferences[TEXT_SIZE_KEY] = size.name
        }
    }
}
