package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.feature.about.AboutScreen
import com.example.feature.ai.AiStudioScreen
import com.example.feature.editor.EditorScreen
import com.example.feature.export.ExportScreen
import com.example.feature.home.HomeScreen
import com.example.feature.settings.SettingsScreen

object Destinations {
    const val HOME = "home"
    const val EDITOR = "editor/{projectId}"
    const val EXPORT = "export/{projectId}"
    const val AI_STUDIO = "ai_studio"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    fun editor(projectId: String) = "editor/$projectId"
    fun export(projectId: String) = "export/$projectId"
}

@Composable
fun AppNavigation(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.HOME
    ) {
        composable(Destinations.HOME) {
            HomeScreen(
                onOpenEditor = { projectId ->
                    navController.navigate(Destinations.editor(projectId))
                },
                onOpenAiStudio = {
                    navController.navigate(Destinations.AI_STUDIO)
                },
                onOpenSettings = {
                    navController.navigate(Destinations.SETTINGS)
                }
            )
        }

        composable(
            route = Destinations.EDITOR,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            EditorScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateExport = { pId ->
                    navController.navigate(Destinations.export(pId))
                }
            )
        }

        composable(
            route = Destinations.EXPORT,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ExportScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.AI_STUDIO) {
            AiStudioScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onNavigateAbout = {
                    navController.navigate(Destinations.ABOUT)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.ABOUT) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
