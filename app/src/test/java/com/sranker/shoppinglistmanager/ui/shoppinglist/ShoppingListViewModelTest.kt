package com.sranker.shoppinglistmanager.ui.shoppinglist

import app.cash.turbine.test
import com.sranker.shoppinglistmanager.data.db.ShoppingItem
import com.sranker.shoppinglistmanager.data.repository.ShoppingRepository
import com.sranker.shoppinglistmanager.data.repository.SuggestionRepository
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
class ShoppingListViewModelTest {

    private lateinit var repository: ShoppingRepository
    private lateinit var suggestionRepository: SuggestionRepository
    private lateinit var viewModel: ShoppingListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        suggestionRepository = mockk(relaxed = true)

        val items = listOf(
            ShoppingItem(1, "Apple", 2, false, 0),
            ShoppingItem(2, "Banana", 1, true, 1)
        )
        every { repository.getItems() } returns flowOf(items)
        
        viewModel = ShoppingListViewModel(repository, suggestionRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads items`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initialState = awaitItem()
            if (initialState.isLoading) {
                val loadedState = awaitItem()
                assertEquals(false, loadedState.isLoading)
                assertEquals(2, loadedState.items.size)
            } else {
                assertEquals(false, initialState.isLoading)
                assertEquals(2, initialState.items.size)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addItem delegates to repository`() = runTest(testDispatcher) {
        viewModel.addItem("Milk", 1)
        advanceUntilIdle()
        coVerify { repository.addItem("Milk", 1) }
    }

    @Test
    fun `toggleItem auto-sorts and updates all items`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        viewModel.toggleItem(1)
        advanceUntilIdle()

        val capturedItems = slot<List<ShoppingItem>>()
        coVerify { repository.reorderItems(capture(capturedItems)) }
        
        val updatedList = capturedItems.captured
        assertEquals(2, updatedList.size)
        assertTrue(updatedList.all { it.isPurchased })
    }

    @Test
    fun `updateQuantity delegates to repository`() = runTest(testDispatcher) {
        viewModel.updateQuantity(1, 5)
        advanceUntilIdle()
        coVerify { repository.updateQuantity(1, 5) }
    }

    @Test
    fun `deleteItem emits undo event`() = runTest(testDispatcher) {
        val item = ShoppingItem(1, "Apple", 2, false, 0)
        coEvery { repository.deleteItem(1) } returns item
        
        advanceUntilIdle()

        viewModel.uiState.test {
            val state1 = awaitItem()
            viewModel.deleteItem(1)
            val state2 = awaitItem()
            
            assertEquals(item, state2.undoEvent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `undoDelete restores item and dismisses snackbar`() = runTest(testDispatcher) {
        val item = ShoppingItem(1, "Apple", 2, false, 0)
        coEvery { repository.deleteItem(1) } returns item
        
        advanceUntilIdle()
        viewModel.deleteItem(1)
        advanceUntilIdle()
        
        viewModel.undoDelete()
        advanceUntilIdle()
        
        coVerify { repository.restoreItem(item) }
        assertNull(viewModel.uiState.value.undoEvent)
    }

    @Test
    fun `dismissUndo clears buffer`() = runTest(testDispatcher) {
        val item = ShoppingItem(1, "Apple", 2, false, 0)
        coEvery { repository.deleteItem(1) } returns item
        
        advanceUntilIdle()
        viewModel.deleteItem(1)
        advanceUntilIdle()
        
        viewModel.dismissUndo()
        advanceUntilIdle()
        
        assertNull(viewModel.uiState.value.undoEvent)
    }

    @Test
    fun `reorderItems updates list optimistically and calls repository`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.reorderItems(0, 1)
        advanceUntilIdle()
        
        val capturedItems = slot<List<ShoppingItem>>()
        coVerify { repository.reorderItems(capture(capturedItems)) }
        
        assertEquals(2, capturedItems.captured.size)
        assertEquals("Banana", capturedItems.captured[0].name)
        assertEquals("Apple", capturedItems.captured[1].name)
    }

    @Test
    fun `archiveSession delegates to repository`() = runTest(testDispatcher) {
        viewModel.archiveSession()
        advanceUntilIdle()
        coVerify { repository.archiveSession() }
    }

    @Test
    fun `onQueryChanged updates suggestions`() = runTest(testDispatcher) {
        every { suggestionRepository.getSuggestions("Ap") } returns flowOf(listOf("Apple", "Apricot"))
        
        advanceUntilIdle()
        viewModel.uiState.test {
            val state1 = awaitItem()
            
            viewModel.onQueryChanged("Ap")
            
            val state2 = awaitItem()
            assertEquals(listOf("Apple", "Apricot"), state2.suggestions)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
