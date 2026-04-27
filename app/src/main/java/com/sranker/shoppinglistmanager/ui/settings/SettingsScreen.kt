package com.sranker.shoppinglistmanager.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sranker.shoppinglistmanager.R
import com.sranker.shoppinglistmanager.data.repository.AppTheme
import com.sranker.shoppinglistmanager.data.repository.Language
import com.sranker.shoppinglistmanager.data.repository.TextSizePreference
import com.sranker.shoppinglistmanager.util.LocaleHelper
import com.sranker.shoppinglistmanager.ui.components.CustomHeader
import com.sranker.shoppinglistmanager.ui.theme.*

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
            .padding(top = 0.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        CustomHeader(
            title = stringResource(R.string.nav_settings),
            modifier = Modifier.padding(horizontal = 0.dp)
        )

        // Theme Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    val themeColor = when (theme) {
                        AppTheme.OCEAN_DARK -> OceanTeal
                        AppTheme.FOREST_LIGHT -> ForestGreen
                        AppTheme.SUNSET_DARK -> SunsetPink
                        AppTheme.SNOW_LIGHT -> SnowSlate
                        AppTheme.SKY_LIGHT -> SkyPrimary
                        AppTheme.ROSE_LIGHT -> RosePrimary
                        AppTheme.SAND_LIGHT -> SandPrimary
                    }
                    val isSelected = uiState.theme == theme

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(themeColor)
                            .clickable { viewModel.setTheme(theme) }
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }
                    }
                }
            }
        }

        // Language Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Language.entries.forEach { lang ->
                    val isSelected = uiState.language == lang
                    val nameRes = when (lang) {
                        Language.ENGLISH -> R.string.language_english
                        Language.HUNGARIAN -> R.string.language_hungarian
                        Language.GERMAN -> R.string.language_german
                    }
                    TextButton(
                        onClick = {
                            viewModel.setLanguage(lang)
                            LocaleHelper.setLocale(lang)
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = stringResource(nameRes),
                            style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Text Size Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.text_size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextSizePreference.entries.forEach { size ->
                    val isSelected = uiState.textSize == size
                    val labelRes = when (size) {
                        TextSizePreference.SMALL -> R.string.text_size_small
                        TextSizePreference.MEDIUM -> R.string.text_size_medium
                        TextSizePreference.LARGE -> R.string.text_size_large
                    }
                    TextButton(
                        onClick = { viewModel.setTextSize(size) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = stringResource(labelRes),
                            style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
