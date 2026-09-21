package com.rank.lexi.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.db.LevelDao
import com.rank.lexi.data.db.VaultDao
import com.rank.lexi.data.repository.CoinRepository
import com.rank.lexi.data.repository.GameRepository
import com.rank.lexi.data.repository.PlayerPreferences
import com.rank.lexi.data.repository.WordRepository
import com.rank.lexi.domain.GameEngine
import com.rank.lexi.domain.model.DailyStatus
import com.rank.lexi.ui.audio.SoundManager
import com.rank.lexi.ui.screen.*
import com.rank.lexi.ui.viewmodel.GameViewModel
import com.rank.lexi.ui.viewmodel.MultiBoardViewModel
import com.rank.lexi.ui.viewmodel.ShopViewModel
import java.time.LocalDate

object Routes {
    const val HOME = "home"
    const val GAME = GameRoutes.PATTERN
    const val LEVELS = "levels"
    const val MULTI_BOARD = "multi_board"
    const val CHALLENGE = "challenge?code={code}"
    const val QUESTS = "quests"
    const val COSMETICS = "cosmetics"
    const val PASS_AND_PLAY = "pass_and_play"
    const val SHOP = "shop"
    const val STATS = "stats"
    const val BADGES = "badges"
    const val SETTINGS = "settings"
    const val VAULT = "vault"
}

data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val NAV_ITEMS = listOf(
    NavItem(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(Routes.LEVELS, "Levels", Icons.Filled.Flag, Icons.Outlined.Flag),
    NavItem(Routes.SHOP, "Shop", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    NavItem(Routes.STATS, "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    NavItem(Routes.BADGES, "Awards", Icons.Filled.MilitaryTech, Icons.Outlined.MilitaryTech),
)

@Composable
fun LexiGuessNavGraph(
    playerPreferences: PlayerPreferences,
    gameRepository: GameRepository,
    coinRepository: CoinRepository,
    levelDao: LevelDao,
    vaultDao: VaultDao,
    achievementDao: AchievementDao,
    wordRepository: WordRepository,
    gameEngine: GameEngine,
    soundManager: SoundManager,
    themePref: String = "SYSTEM",
    darkMode: Boolean,
    onThemeChange: (String) -> Unit,
) {
    val navController = rememberNavController()

    val shieldDates by playerPreferences.streakShieldDatesFlow.collectAsState(initial = emptySet())
    val currentStreak by gameRepository.currentStreakFlow(playerPreferences.streakShieldDatesFlow)
        .collectAsState(initial = 0)
    val wonDatesList by gameRepository.wonDatesFlow.collectAsState(initial = emptyList())
    val dailyStatus by gameRepository.dailyStatusFlow.collectAsState(initial = DailyStatus.NOT_STARTED)
    val nextLevel by levelDao.getNextIncompleteLevel().collectAsState(initial = null)
    val coins by coinRepository.coinsFlow.collectAsState(initial = 0)
    var bestStreak by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        wordRepository.initialize()
    }
    LaunchedEffect(currentStreak, shieldDates) {
        bestStreak = gameRepository.bestStreak(shieldDates)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Routes.HOME,
        Routes.LEVELS,
        Routes.SHOP,
        Routes.STATS,
        Routes.BADGES,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    NAV_ITEMS.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(text = item.label) },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        )
                    }
                }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    playerPreferences = playerPreferences,
                    soundManager = soundManager,
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    wonDates = wonDatesList.toSet(),
                    shieldDates = shieldDates,
                    dailyStatus = dailyStatus,
                    continueCampaignLevel = nextLevel?.levelNumber,
                    onStartArchiveDaily = { date ->
                        val route = if (date == LocalDate.now()) {
                            GameRoutes.daily()
                        } else {
                            GameRoutes.archive(date.toEpochDay())
                        }
                        navController.navigate(route)
                    },
                    onStartDaily = { navController.navigate(GameRoutes.daily()) },
                    onStartRush = { navController.navigate(GameRoutes.rush()) },
                    onNavigateToLevels = { navController.navigate(Routes.LEVELS) },
                    onStartPractice = { length -> navController.navigate(GameRoutes.practice(length)) },
                    onContinueCampaign = { level -> navController.navigate(GameRoutes.level(level)) },
                    onNavigateToMultiBoard = { navController.navigate(Routes.MULTI_BOARD) },
                    onNavigateToVault = { navController.navigate(Routes.VAULT) },
                    onNavigateToChallenge = { navController.navigate("challenge?code=") },
                    onNavigateToStats = { navController.navigate(Routes.STATS) },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                    onNavigateToQuests = { navController.navigate(Routes.QUESTS) },
                    onNavigateToCosmetics = { navController.navigate(Routes.COSMETICS) },
                    onNavigateToPassAndPlay = { navController.navigate(Routes.PASS_AND_PLAY) },
                    onNavigateToShop = { navController.navigate(Routes.SHOP) },
                    themePref = themePref,
                    darkMode = darkMode,
                    onToggleTheme = {
                        val next = when (themePref) {
                            "LIGHT" -> "DARK"
                            "DARK" -> "SYSTEM"
                            else -> "LIGHT"
                        }
                        onThemeChange(next)
                    },
                    coins = coins,
                )
            }

            composable(Routes.SHOP) {
                ShopScreen(onBack = null)
            }

            composable(Routes.LEVELS) {
                LevelsScreen(
                    levelDao = levelDao,
                    onSelectLevel = { navController.navigate(GameRoutes.level(it)) },
                    onBack = null,
                )
            }

            composable(
                route = Routes.GAME,
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType; defaultValue = "DAILY" },
                    navArgument("length") { type = NavType.StringType; defaultValue = "" },
                    navArgument("level") { type = NavType.StringType; defaultValue = "" },
                    navArgument("word") { type = NavType.StringType; defaultValue = "" },
                    navArgument("attempts") { type = NavType.StringType; defaultValue = "" },
                    navArgument("epoch") { type = NavType.StringType; defaultValue = "" },
                ),
            ) {
                GameScreen(
                    viewModel = hiltViewModel<GameViewModel>(),
                    onBackToHome = { navController.popBackStack() },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }

            composable(Routes.MULTI_BOARD) {
                MultiBoardScreen(
                    viewModel = hiltViewModel<MultiBoardViewModel>(),
                    soundManager = soundManager,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.VAULT) {
                VaultScreen(
                    vaultDao = vaultDao,
                    onPracticeWord = { navController.navigate(GameRoutes.practiceWord(it)) },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.CHALLENGE,
                arguments = listOf(
                    navArgument("code") { type = NavType.StringType; defaultValue = "" },
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "lexiguess://challenge/{code}" },
                ),
            ) { entry ->
                CustomChallengeScreen(
                    wordRepository = wordRepository,
                    initialCode = entry.arguments?.getString("code").orEmpty(),
                    onStartChallengeGame = { word, attempts ->
                        navController.navigate(GameRoutes.custom(word, attempts))
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.QUESTS) {
                QuestsScreen(
                    viewModel = hiltViewModel(),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.COSMETICS) {
                CosmeticsScreen(
                    playerPreferences = playerPreferences,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.PASS_AND_PLAY) {
                PassAndPlayScreen(
                    wordRepository = wordRepository,
                    engine = gameEngine,
                    achievementDao = achievementDao,
                    soundManager = soundManager,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.STATS) {
                StatsScreen(onBack = null)
            }

            composable(Routes.BADGES) {
                AchievementsScreen(
                    achievementDao = achievementDao,
                    onBack = null,
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    playerPreferences = playerPreferences,
                    soundManager = soundManager,
                    themePref = themePref,
                    darkMode = darkMode,
                    onThemeChange = onThemeChange,
                    onNavigateToCosmetics = { navController.navigate(Routes.COSMETICS) },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
