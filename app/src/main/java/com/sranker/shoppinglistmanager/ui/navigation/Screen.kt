package com.sranker.shoppinglistmanager.ui.navigation

sealed class Screen(val route: String) {
    object ShoppingList : Screen("shopping_list")
    object Archive : Screen("archive")
    object ArchiveDetail : Screen("archive_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "archive_detail/$sessionId"
    }
    object Settings : Screen("settings")
}
