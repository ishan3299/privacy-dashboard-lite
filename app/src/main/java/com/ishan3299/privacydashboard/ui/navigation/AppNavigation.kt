package com.ishan3299.privacydashboard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ishan3299.privacydashboard.ui.home.HomeScreen
import com.ishan3299.privacydashboard.ui.detail.AppDetailScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{packageName}") {
        fun createRoute(packageName: String) = "detail/$packageName"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAppClick = { packageName ->
                    navController.navigate(Screen.Detail.createRoute(packageName))
                }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: return@composable
            AppDetailScreen(
                packageName = packageName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
