package com.sranker.shoppinglistmanager.ui.archive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sranker.shoppinglistmanager.R
import com.sranker.shoppinglistmanager.ui.components.CustomHeader
import kotlinx.coroutines.launch

@Composable
fun ArchiveDetailScreen(
    sessionId: Long,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(sessionId) {
        viewModel.selectSession(sessionId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CustomHeader(
            title = stringResource(R.string.nav_archive),
            onBackClick = onNavigateBack
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.selectedSessionItems, key = { it.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(item.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${stringResource(R.string.quantity)}: ${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        OutlinedButton(
            onClick = {
                viewModel.reloadSession(sessionId)
                scope.launch {
                    snackbarHostState.showSnackbar("Session restored")
                }
                onNavigateBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.restore_session))
        }
    }
}
