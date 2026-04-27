package com.sranker.shoppinglistmanager.ui.shoppinglist

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sranker.shoppinglistmanager.R
import kotlinx.coroutines.launch
import com.sranker.shoppinglistmanager.ui.components.CustomHeader
import com.sranker.shoppinglistmanager.ui.components.EmptyState
import com.sranker.shoppinglistmanager.ui.components.LoadingShimmerList
import androidx.compose.ui.draw.shadow
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Preview(widthDp = 360, name = "Small Width")
@Composable
fun ShoppingListSmallPreview() {
    Surface {
        Text("Responsiveness Check: 360dp")
    }
}

@Preview(widthDp = 430, name = "Large Width")
@Composable
fun ShoppingListLargePreview() {
    Surface {
        Text("Responsiveness Check: 430dp")
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    UndoSnackbarHandler(
        undoEvent = uiState.undoEvent,
        snackbarHostState = snackbarHostState,
        onUndo = { viewModel.undoDelete() },
        onDismiss = { viewModel.dismissUndo() }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        CustomHeader(
            title = stringResource(R.string.app_name),
            rightIconResId = R.drawable.ic_cart,
            onRightIconClick = { /* Optional action */ }
        )

        AddItemBar(
            onAddItem = { name, qty -> viewModel.addItem(name, qty) }
        )

        if (uiState.isLoading) {
            LoadingShimmerList(modifier = Modifier.weight(1f))
        } else if (uiState.items.isEmpty()) {
            EmptyState(
                icon = Icons.Default.ShoppingCart,
                message = stringResource(R.string.no_items_yet),
                modifier = Modifier.weight(1f)
            )
        } else {
            ShoppingListContent(
                items = uiState.items,
                viewModel = viewModel,
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(
            visible = uiState.items.any { it.isPurchased },
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Button(
                onClick = { viewModel.archiveSession() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.archive_session))
            }
        }
    }
}

@Composable
fun AddItemBar(onAddItem: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text(stringResource(R.string.add_item)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedIconButton(
            onClick = {
                if (name.isNotBlank()) {
                    onAddItem(name, quantity)
                    name = ""
                    quantity = 1
                }
            },
            enabled = name.isNotBlank(),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemRow(
    item: com.sranker.shoppinglistmanager.data.db.ShoppingItem,
    elevation: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    draggableHandle: (@Composable () -> Unit)? = null,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onIncreaseQty: () -> Unit,
    onDecreaseQty: () -> Unit
) {
    SwipeToDismissBox(
        state = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                    onDelete()
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
        Row(
            modifier = modifier
                .fillMaxWidth()
                .shadow(elevation)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            draggableHandle?.invoke()

            val scale by animateFloatAsState(
                targetValue = if (item.isPurchased) 1.2f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "checkboxScale"
            )
            
            Checkbox(
                checked = item.isPurchased,
                onCheckedChange = { onToggle() },
                modifier = Modifier.scale(scale)
            )

            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isPurchased) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecreaseQty) {
                    Text("-", style = MaterialTheme.typography.titleLarge)
                }
                Text("${item.quantity}")
                IconButton(onClick = onIncreaseQty) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingListContent(
    items: List<com.sranker.shoppinglistmanager.data.db.ShoppingItem>,
    viewModel: ShoppingListViewModel,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val reorderableState = sh.calvin.reorderable.rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromItem = items.getOrNull(from.index)
        val toItem = items.getOrNull(to.index)
        if (fromItem != null && toItem != null && !fromItem.isPurchased && !toItem.isPurchased) {
            viewModel.reorderItems(from.index, to.index)
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                ShoppingItemRow(
                    item = item,
                    elevation = elevation,
                    modifier = Modifier.animateItemPlacement(),
                    draggableHandle = if (!item.isPurchased) {
                        {
                            IconButton(
                                onClick = {},
                                modifier = Modifier.draggableHandle()
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.cd_drag_reorder))
                            }
                        }
                    } else null,
                    onToggle = { viewModel.toggleItem(item.id) },
                    onDelete = { viewModel.deleteItem(item.id) },
                    onIncreaseQty = { viewModel.updateQuantity(item.id, item.quantity + 1) },
                    onDecreaseQty = { if (item.quantity > 1) viewModel.updateQuantity(item.id, item.quantity - 1) }
                )
            }
        }
    }
}

@Composable
fun UndoSnackbarHandler(
    undoEvent: Any?,
    snackbarHostState: SnackbarHostState,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val deletedMsg = stringResource(R.string.item_deleted)
    val undoMsg = stringResource(R.string.undo)

    LaunchedEffect(undoEvent) {
        undoEvent?.let { _ ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = deletedMsg,
                    actionLabel = undoMsg,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onUndo()
                } else {
                    onDismiss()
                }
            }
        }
    }
}
