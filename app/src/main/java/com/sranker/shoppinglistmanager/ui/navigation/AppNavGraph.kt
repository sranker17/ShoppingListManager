package com.sranker.shoppinglistmanager.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ShoppingList.route,
        modifier = modifier
    ) {
        composable(Screen.ShoppingList.route) {
            com.sranker.shoppinglistmanager.ui.shoppinglist.ShoppingListScreen(snackbarHostState = snackbarHostState)
        }
        composable(Screen.Archive.route) {
            com.sranker.shoppinglistmanager.ui.archive.ArchiveScreen(onNavigateToDetail = { sessionId ->
                navController.navigate(Screen.ArchiveDetail.createRoute(sessionId))
            })
        }
        composable(
            route = Screen.ArchiveDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            com.sranker.shoppinglistmanager.ui.archive.ArchiveDetailScreen(
                sessionId = sessionId,
                snackbarHostState = snackbarHostState,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            com.sranker.shoppinglistmanager.ui.settings.SettingsScreen()
        }
    }
}
