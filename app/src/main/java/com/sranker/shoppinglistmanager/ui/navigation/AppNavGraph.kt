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
            // Placeholder for Shopping List Screen
            Text("Shopping List Screen")
        }
        composable(Screen.Archive.route) {
            // Placeholder for Archive Screen
            Text("Archive Screen")
        }
        composable(
            route = Screen.ArchiveDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            // Placeholder for Archive Detail
            Text("Archive Detail Screen $sessionId")
        }
        composable(Screen.Settings.route) {
            // Placeholder for Settings Screen
            Text("Settings Screen")
        }
    }
}
