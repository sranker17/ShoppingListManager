package com.sranker.shoppinglistmanager.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sranker.shoppinglistmanager.R
import com.sranker.shoppinglistmanager.data.repository.AppTheme
import com.sranker.shoppinglistmanager.data.repository.Language
import com.sranker.shoppinglistmanager.data.repository.TextSizePreference
import com.sranker.shoppinglistmanager.util.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Theme Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    val nameRes = when (theme) {
                        AppTheme.OCEAN_DARK -> R.string.theme_ocean_dark
                        AppTheme.FOREST_LIGHT -> R.string.theme_forest_light
                        AppTheme.SUNSET_DARK -> R.string.theme_sunset_dark
                        AppTheme.SNOW_LIGHT -> R.string.theme_snow_light
                    }
                    FilterChip(
                        selected = uiState.theme == theme,
                        onClick = { viewModel.setTheme(theme) },
                        label = { Text(stringResource(nameRes)) }
                    )
                }
            }
        }

        // Language Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                Language.entries.forEachIndexed { index, lang ->
                    val nameRes = when (lang) {
                        Language.ENGLISH -> R.string.language_english
                        Language.HUNGARIAN -> R.string.language_hungarian
                        Language.GERMAN -> R.string.language_german
                    }
                    SegmentedButton(
                        selected = uiState.language == lang,
                        onClick = {
                            viewModel.setLanguage(lang)
                            LocaleHelper.setLocale(lang)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = Language.entries.size)
                    ) {
                        Text(stringResource(nameRes))
                    }
                }
            }
        }

        // Text Size Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.text_size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            val sliderValue = when (uiState.textSize) {
                TextSizePreference.SMALL -> 0f
                TextSizePreference.MEDIUM -> 1f
                TextSizePreference.LARGE -> 2f
            }
            
            Slider(
                value = sliderValue,
                onValueChange = { value ->
                    val newSize = when (value.toInt()) {
                        0 -> TextSizePreference.SMALL
                        1 -> TextSizePreference.MEDIUM
                        else -> TextSizePreference.LARGE
                    }
                    viewModel.setTextSize(newSize)
                },
                valueRange = 0f..2f,
                steps = 1
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.text_size_small), style = MaterialTheme.typography.labelMedium)
                Text(stringResource(R.string.text_size_medium), style = MaterialTheme.typography.labelMedium)
                Text(stringResource(R.string.text_size_large), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
