package com.sranker.shoppinglistmanager.ui.archive

import app.cash.turbine.test
import com.sranker.shoppinglistmanager.data.db.ArchiveSession
import com.sranker.shoppinglistmanager.data.db.ArchivedItem
import com.sranker.shoppinglistmanager.data.repository.ArchiveRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModelTest {

    private lateinit var repository: ArchiveRepository
    private lateinit var viewModel: ArchiveViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)

        val sessions = listOf(ArchiveSession(1L, "Session 1", 1000L))
        every { repository.getSessions() } returns flowOf(sessions)

        viewModel = ArchiveViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load loads sessions`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.sessions.size)
            assertEquals("Session 1", state.sessions[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectSession loads items for session`() = runTest(testDispatcher) {
        val items = listOf(ArchivedItem(1L, 1L, "Apple", 2))
        every { repository.getItems(1L) } returns flowOf(items)

        advanceUntilIdle()
        viewModel.selectSession(1L)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.selectedSessionItems.size)
            assertEquals("Apple", state.selectedSessionItems[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `open and close rename dialog update state`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val session = ArchiveSession(1L, "Session 1", 1000L)
        
        viewModel.openRenameDialog(session)
        assertEquals(session, viewModel.uiState.value.renameDialogSession)
        
        viewModel.closeRenameDialog()
        assertNull(viewModel.uiState.value.renameDialogSession)
    }

    @Test
    fun `renameSession delegates to repository and closes dialog`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val session = ArchiveSession(1L, "Session 1", 1000L)
        viewModel.openRenameDialog(session)
        
        viewModel.renameSession(1L, "New Name")
        advanceUntilIdle()
        
        coVerify { repository.renameSession(1L, "New Name") }
        assertNull(viewModel.uiState.value.renameDialogSession)
    }

    @Test
    fun `renameSession ignores blank name`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val session = ArchiveSession(1L, "Session 1", 1000L)
        viewModel.openRenameDialog(session)
        
        viewModel.renameSession(1L, "   ")
        advanceUntilIdle()
        
        coVerify(exactly = 0) { repository.renameSession(any(), any()) }
        assertNotNull(viewModel.uiState.value.renameDialogSession)
    }

    @Test
    fun `reloadSession delegates to repository`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.reloadSession(1L)
        advanceUntilIdle()
        
        coVerify { repository.reloadSession(1L) }
    }
}
