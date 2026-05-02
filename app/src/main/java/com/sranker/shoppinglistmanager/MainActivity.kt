package com.sranker.shoppinglistmanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.sranker.shoppinglistmanager.ui.AppScaffold
import com.sranker.shoppinglistmanager.ui.settings.SettingsViewModel
import com.sranker.shoppinglistmanager.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()

            AppTheme(
                theme = settingsState.theme,
                textSize = settingsState.textSize
            ) {
                AppScaffold()
            }
        }
    }
}
