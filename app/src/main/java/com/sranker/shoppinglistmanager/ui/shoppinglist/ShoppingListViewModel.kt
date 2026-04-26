package com.sranker.shoppinglistmanager.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sranker.shoppinglistmanager.data.db.ShoppingItem
import com.sranker.shoppinglistmanager.data.repository.ShoppingRepository
import com.sranker.shoppinglistmanager.data.repository.SuggestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingListUiState(
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = true,
    val undoEvent: ShoppingItem? = null,
    val suggestions: List<String> = emptyList()
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingRepository,
    private val suggestionRepository: SuggestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    private var undoBuffer: ShoppingItem? = null
    private var suggestionJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getItems().collect { items ->
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
        }
    }

    fun addItem(name: String, quantity: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addItem(name, quantity)
        }
    }

    fun toggleItem(id: Long) {
        viewModelScope.launch {
            val currentItems = _uiState.value.items.map {
                if (it.id == id) it.copy(isPurchased = !it.isPurchased) else it
            }
            
            val unchecked = currentItems.filter { !it.isPurchased }.sortedBy { it.sortOrder }
            val checked = currentItems.filter { it.isPurchased }.sortedBy { it.sortOrder }
            
            val updatedItems = (unchecked + checked).mapIndexed { index, item ->
                item.copy(sortOrder = index)
            }
            
            repository.reorderItems(updatedItems)
        }
    }

    fun updateQuantity(id: Long, qty: Int) {
        if (qty < 1) return
        viewModelScope.launch {
            repository.updateQuantity(id, qty)
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            val deletedItem = repository.deleteItem(id)
            if (deletedItem != null) {
                undoBuffer = deletedItem
                _uiState.update { it.copy(undoEvent = deletedItem) }
            }
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            undoBuffer?.let { item ->
                repository.restoreItem(item)
                dismissUndo()
            }
        }
    }

    fun dismissUndo() {
        undoBuffer = null
        _uiState.update { it.copy(undoEvent = null) }
    }

    fun reorderItems(fromIndex: Int, toIndex: Int) {
        val currentItems = _uiState.value.items.toMutableList()
        if (fromIndex !in currentItems.indices || toIndex !in currentItems.indices) return

        val item = currentItems.removeAt(fromIndex)
        currentItems.add(toIndex, item)

        val updatedItems = currentItems.mapIndexed { index, shoppingItem ->
            shoppingItem.copy(sortOrder = index)
        }
        
        // Optimistically update UI
        _uiState.update { it.copy(items = updatedItems) }
        
        viewModelScope.launch {
            repository.reorderItems(updatedItems)
        }
    }

    fun archiveSession() {
        viewModelScope.launch {
            repository.archiveSession()
        }
    }

    fun onQueryChanged(text: String) {
        suggestionJob?.cancel()
        if (text.isBlank()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }
        
        suggestionJob = viewModelScope.launch {
            suggestionRepository.getSuggestions(text).collect { suggestions ->
                _uiState.update { it.copy(suggestions = suggestions) }
            }
        }
    }
}
