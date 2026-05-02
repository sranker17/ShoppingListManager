package com.sranker.shoppinglistmanager.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sranker.shoppinglistmanager.data.db.ArchiveSession
import com.sranker.shoppinglistmanager.data.db.ArchivedItem
import com.sranker.shoppinglistmanager.data.repository.ArchiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveUiState(
    val sessions: List<ArchiveSession> = emptyList(),
    val selectedSessionItems: List<ArchivedItem> = emptyList(),
    val renameDialogSession: ArchiveSession? = null,
    val undoEvent: ArchiveSession? = null
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: ArchiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    private var itemsJob: Job? = null
    private var undoBuffer: ArchiveRepository.DeletedArchiveSession? = null

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            repository.getSessions().collect { sessions ->
                _uiState.update { it.copy(sessions = sessions) }
            }
        }
    }

    fun selectSession(sessionId: Long) {
        itemsJob?.cancel()
        itemsJob = viewModelScope.launch {
            repository.getItems(sessionId).collect { items ->
                _uiState.update { it.copy(selectedSessionItems = items) }
            }
        }
    }

    fun openRenameDialog(session: ArchiveSession) {
        _uiState.update { it.copy(renameDialogSession = session) }
    }

    fun closeRenameDialog() {
        _uiState.update { it.copy(renameDialogSession = null) }
    }

    fun renameSession(id: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.renameSession(id, name)
            closeRenameDialog()
        }
    }

    fun reloadSession(sessionId: Long) {
        viewModelScope.launch {
            repository.reloadSession(sessionId)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            val deletedSession = repository.deleteSession(sessionId)
            if (deletedSession != null) {
                // Check if any items can be restored (not duplicates)
                val currentItems = repository.getCurrentShoppingItems()
                val hasRestorableItems = deletedSession.restoredItems.any { archivedItem ->
                    !currentItems.any { shoppingItem ->
                        shoppingItem.name.trim().lowercase() == archivedItem.name.trim().lowercase()
                    }
                }

                // Only show snackbar if there are items to restore
                if (hasRestorableItems) {
                    undoBuffer = deletedSession
                    _uiState.update { it.copy(undoEvent = deletedSession.session) }
                }
            }
        }
    }

    fun undoDeleteSession() {
        viewModelScope.launch {
            undoBuffer?.let { deletedSession ->
                repository.restoreSession(deletedSession)
                dismissUndo()
            }
        }
    }

    fun dismissUndo() {
        undoBuffer = null
        _uiState.update { it.copy(undoEvent = null) }
    }
}
