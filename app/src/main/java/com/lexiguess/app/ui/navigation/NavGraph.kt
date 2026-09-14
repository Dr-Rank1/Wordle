package com.lexiguess.app.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lexiguess.app.ui.screen.GameScreen
import com.lexiguess.app.ui.screen.SettingsScreen
import com.lexiguess.app.ui.screen.StatsScreen
import com.lexiguess.app.ui.theme.LexiGuessTheme
import com.lexiguess.app.ui.viewmodel.GameViewModel

private object Routes {
    const val GAME = "game"
    const val STATS = "stats"
    const val SETTINGS = "settings"
}

/**
 * Root navigation graph for LexiGuess.
 *
 * Also owns the dark-mode toggle state so it can be passed down to
 * [LexiGuessTheme] in [MainActivity] via [onDarkModeChange], and can be
 * surfaced to [SettingsScreen].
 */
@Composable
fun LexiGuessNavGraph(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    val navController = rememberNavController()

    // GameViewModel is scoped to the nav graph so it survives screen transitions
    val gameViewModel: GameViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Routes.GAME) {
        composable(Routes.GAME) {
            GameScreen(
                viewModel = gameViewModel,
                onNavigateToStats = { navController.navigate(Routes.STATS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                darkMode = darkMode,
                onToggle = onDarkModeChange,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
