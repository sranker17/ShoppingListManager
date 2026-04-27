package com.sranker.shoppinglistmanager.ui.archive

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.sranker.shoppinglistmanager.ui.components.CustomHeader
import com.sranker.shoppinglistmanager.ui.components.EmptyState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sranker.shoppinglistmanager.R
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview

@Preview(widthDp = 360, name = "Small Width")
@Composable
fun ArchiveSmallPreview() {
    Surface {
        Text("Archive Responsiveness: 360dp")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArchiveScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        CustomHeader(title = stringResource(R.string.nav_archive))

        if (uiState.sessions.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.List,
                message = stringResource(R.string.no_archived_sessions),
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.sessions, key = { it.id }) { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onNavigateToDetail(session.id) },
                                onLongClick = { viewModel.openRenameDialog(session) }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val sessionName = session.name
                                Text(sessionName, style = MaterialTheme.typography.titleMedium)
                            }
                            IconButton(onClick = { viewModel.openRenameDialog(session) }) {
                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.rename_session))
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.renameDialogSession != null) {
        RenameDialog(
            initialName = uiState.renameDialogSession?.name ?: "",
            onDismiss = { viewModel.closeRenameDialog() },
            onSave = { newName ->
                uiState.renameDialogSession?.id?.let {
                    viewModel.renameSession(it, newName)
                }
            }
        )
    }
}

@Composable
fun RenameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_session)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.enter_new_name)) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(text) },
                enabled = text.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
