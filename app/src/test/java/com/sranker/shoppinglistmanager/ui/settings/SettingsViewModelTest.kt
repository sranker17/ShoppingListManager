package com.sranker.shoppinglistmanager.ui.settings

import app.cash.turbine.test
import com.sranker.shoppinglistmanager.data.repository.AppTheme
import com.sranker.shoppinglistmanager.data.repository.Language
import com.sranker.shoppinglistmanager.data.repository.SettingsRepository
import com.sranker.shoppinglistmanager.data.repository.TextSizePreference
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var repository: SettingsRepository
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val themeFlow = MutableStateFlow(AppTheme.OCEAN_DARK)
    private val languageFlow = MutableStateFlow(Language.ENGLISH)
    private val textSizeFlow = MutableStateFlow(TextSizePreference.MEDIUM)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)

        every { repository.themeFlow } returns themeFlow
        every { repository.languageFlow } returns languageFlow
        every { repository.textSizeFlow } returns textSizeFlow

        viewModel = SettingsViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState combines repository flows`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(AppTheme.OCEAN_DARK, initialState.theme)
            assertEquals(Language.ENGLISH, initialState.language)
            assertEquals(TextSizePreference.MEDIUM, initialState.textSize)

            themeFlow.value = AppTheme.FOREST_LIGHT
            val state2 = awaitItem()
            assertEquals(AppTheme.FOREST_LIGHT, state2.theme)

            languageFlow.value = Language.GERMAN
            val state3 = awaitItem()
            assertEquals(Language.GERMAN, state3.language)

            textSizeFlow.value = TextSizePreference.LARGE
            val state4 = awaitItem()
            assertEquals(TextSizePreference.LARGE, state4.textSize)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTheme delegates to repository`() = runTest(testDispatcher) {
        viewModel.setTheme(AppTheme.SNOW_LIGHT)
        advanceUntilIdle()
        coVerify { repository.setTheme(AppTheme.SNOW_LIGHT) }
    }

    @Test
    fun `setLanguage delegates to repository`() = runTest(testDispatcher) {
        viewModel.setLanguage(Language.HUNGARIAN)
        advanceUntilIdle()
        coVerify { repository.setLanguage(Language.HUNGARIAN) }
    }

    @Test
    fun `setTextSize delegates to repository`() = runTest(testDispatcher) {
        viewModel.setTextSize(TextSizePreference.SMALL)
        advanceUntilIdle()
        coVerify { repository.setTextSize(TextSizePreference.SMALL) }
    }
}
