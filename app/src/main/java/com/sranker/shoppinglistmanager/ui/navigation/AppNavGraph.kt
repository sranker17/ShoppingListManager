package com.sranker.shoppinglistmanager.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.SnackbarHostState
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
        modifier = modifier,
        enterTransition = { fadeIn(tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
        exitTransition = { fadeOut(tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
        popEnterTransition = { fadeIn(tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
        popExitTransition = { fadeOut(tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
    ) {
        composable(Screen.ShoppingList.route) {
            com.sranker.shoppinglistmanager.ui.shoppinglist.ShoppingListScreen(snackbarHostState = snackbarHostState)
        }
        composable(Screen.Archive.route) {
            com.sranker.shoppinglistmanager.ui.archive.ArchiveScreen(onNavigateToDetail = { sessionId ->
                navController.navigate(Screen.ArchiveDetail.createRoute(sessionId))
            }, snackbarHostState = snackbarHostState)
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
