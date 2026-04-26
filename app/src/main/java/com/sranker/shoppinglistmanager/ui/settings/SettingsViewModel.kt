package com.sranker.shoppinglistmanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sranker.shoppinglistmanager.data.repository.AppTheme
import com.sranker.shoppinglistmanager.data.repository.Language
import com.sranker.shoppinglistmanager.data.repository.SettingsRepository
import com.sranker.shoppinglistmanager.data.repository.TextSizePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val theme: AppTheme = AppTheme.OCEAN_DARK,
    val language: Language = Language.ENGLISH,
    val textSize: TextSizePreference = TextSizePreference.MEDIUM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.themeFlow,
        repository.languageFlow,
        repository.textSizeFlow
    ) { theme, language, textSize ->
        SettingsUiState(theme, language, textSize)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            repository.setTheme(theme)
        }
    }

    fun setLanguage(language: Language) {
        viewModelScope.launch {
            repository.setLanguage(language)
        }
    }

    fun setTextSize(size: TextSizePreference) {
        viewModelScope.launch {
            repository.setTextSize(size)
        }
    }
}
