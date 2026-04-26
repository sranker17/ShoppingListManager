package com.sranker.shoppinglistmanager.ui.shoppinglist

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sranker.shoppinglistmanager.R
import com.sranker.shoppinglistmanager.data.db.ShoppingItem
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    val deletedMsg = stringResource(R.string.item_deleted)
    val undoMsg = stringResource(R.string.undo)

    LaunchedEffect(uiState.undoEvent) {
        uiState.undoEvent?.let { item ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = deletedMsg,
                    actionLabel = undoMsg,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDelete()
                } else {
                    viewModel.dismissUndo()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AddItemBar(
            onAddItem = { name, qty -> viewModel.addItem(name, qty) }
        )

        if (uiState.items.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_items_yet), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val lazyListState = rememberLazyListState()
            val reorderableState = sh.calvin.reorderable.rememberReorderableLazyListState(lazyListState) { from, to ->
                val fromItem = uiState.items.getOrNull(from.index)
                val toItem = uiState.items.getOrNull(to.index)
                if (fromItem != null && toItem != null && !fromItem.isPurchased && !toItem.isPurchased) {
                    viewModel.reorderItems(from.index, to.index)
                }
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    ReorderableItem(reorderableState, key = item.id) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                        
                        SwipeToDismissBox(
                            state = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteItem(item.id)
                                        true
                                    } else false
                                }
                            ),
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.errorContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        ) {
                            Card(
                                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!item.isPurchased) {
                                        IconButton(
                                            onClick = {},
                                            modifier = Modifier.draggableHandle()
                                        ) {
                                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.cd_drag_reorder))
                                        }
                                    }
                                    
                                    Checkbox(
                                        checked = item.isPurchased,
                                        onCheckedChange = { viewModel.toggleItem(item.id) }
                                    )
                                    
                                    Text(
                                        text = item.name,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        textDecoration = if (item.isPurchased) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { if (item.quantity > 1) viewModel.updateQuantity(item.id, item.quantity - 1) }) {
                                            Text("-", style = MaterialTheme.typography.titleLarge)
                                        }
                                        Text("${item.quantity}")
                                        IconButton(onClick = { viewModel.updateQuantity(item.id, item.quantity + 1) }) {
                                            Text("+", style = MaterialTheme.typography.titleLarge)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val checkedItemsCount = uiState.items.count { it.isPurchased }
        Button(
            onClick = { viewModel.archiveSession() },
            enabled = checkedItemsCount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.archive_session))
        }
    }
}

@Composable
fun AddItemBar(onAddItem: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf(1) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.add_item)) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                if (name.isNotBlank()) {
                    onAddItem(name, quantity)
                    name = ""
                    quantity = 1
                }
            },
            enabled = name.isNotBlank()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}
