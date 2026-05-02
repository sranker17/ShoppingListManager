package com.sranker.shoppinglistmanager.ui.archive

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onNavigateToDetail: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    ArchiveUndoSnackbarHandler(
        undoEvent = uiState.undoEvent,
        snackbarHostState = snackbarHostState,
        onUndo = { viewModel.undoDeleteSession() },
        onDismiss = { viewModel.dismissUndo() }
    )

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
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.sessions, key = { it.id }) { session ->
                    SwipeToDismissBox(
                        state = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteSession(session.id)
                                    true
                                } else {
                                    false
                                }
                            }
                        ),
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .combinedClickable(
                                    onClick = { onNavigateToDetail(session.id) },
                                    onLongClick = { viewModel.openRenameDialog(session) }
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val sessionName = session.name
                                Text(sessionName, style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                                IconButton(
                                    onClick = { viewModel.openRenameDialog(session) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.rename_session),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
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
private fun ArchiveUndoSnackbarHandler(
    undoEvent: Any?,
    snackbarHostState: SnackbarHostState,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    val deletedMessage = stringResource(R.string.item_deleted)
    val undoLabel = stringResource(R.string.undo)

    LaunchedEffect(undoEvent) {
        undoEvent ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = deletedMessage,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            onUndo()
        } else {
            onDismiss()
        }
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
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(stringResource(R.string.enter_new_name)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                )
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
