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
    val renameDialogSession: ArchiveSession? = null
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: ArchiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    private var itemsJob: Job? = null

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
}
