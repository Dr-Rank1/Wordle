package com.rank.lexi.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.db.LevelDao
import com.rank.lexi.data.repository.PlayerPreferences
import com.rank.lexi.data.db.VaultDao
import com.rank.lexi.data.repository.WordRepository
import com.rank.lexi.domain.GameEngine
import com.rank.lexi.domain.MultiBoardEngine
import com.rank.lexi.ui.audio.SoundManager
import com.rank.lexi.ui.screen.*
import com.rank.lexi.ui.theme.TileCorrect
import com.rank.lexi.ui.viewmodel.GameViewModel

object Routes {
    const val HOME = "home"
    const val GAME = "game"
    const val LEVELS = "levels"
    const val STATS = "stats"
    const val BADGES = "badges"
    const val SETTINGS = "settings"
    const val VAULT = "vault"
    const val MULTI_BOARD = "multi_board"
    const val CHALLENGE = "challenge"
    const val QUESTS = "quests"
    const val COSMETICS = "cosmetics"
    const val PASS_AND_PLAY = "pass_and_play"
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
    NavItem(Routes.GAME, "Play", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle),
    NavItem(Routes.STATS, "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    NavItem(Routes.BADGES, "Badges", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
)

@Composable
fun LexiGuessNavGraph(
    playerPreferences: PlayerPreferences,
    gameRepository: com.rank.lexi.data.repository.GameRepository,
    levelDao: LevelDao,
    achievementDao: AchievementDao,
    vaultDao: VaultDao,
    wordRepository: WordRepository,
    multiBoardEngine: MultiBoardEngine,
    gameEngine: GameEngine,
    soundManager: SoundManager,
    questRepository: com.rank.lexi.data.repository.QuestRepository,
    themePref: String = "SYSTEM",
    darkMode: Boolean,
    onThemeChange: (String) -> Unit,
) {
    val navController = rememberNavController()
    val gameViewModel: GameViewModel = hiltViewModel()

    val currentStreak by gameRepository.currentStreakFlow.collectAsState(initial = 0)
    val wonDatesList by gameRepository.wonDatesFlow.collectAsState(initial = emptyList())
    var bestStreak by remember { mutableIntStateOf(0) }
    LaunchedEffect(currentStreak) {
        bestStreak = gameRepository.bestStreak()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Routes.HOME,
        Routes.LEVELS,
        Routes.GAME,
        Routes.STATS,
        Routes.BADGES,
    )

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    NAV_ITEMS.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (selected) TileCorrect else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    color = if (selected) TileCorrect else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            selected = selected,
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
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    wonDates = wonDatesList.toSet(),
                    onStartArchiveDaily = { date ->
                        gameViewModel.startDailyGame(epochDay = date.toEpochDay(), date = date.toString())
                        navController.navigate(Routes.GAME)
                    },
                    onStartDaily = {
                        gameViewModel.startDailyGame()
                        navController.navigate(Routes.GAME)
                    },
                    onStartRush = {
                        gameViewModel.startTimedRush()
                        navController.navigate(Routes.GAME)
                    },
                    onNavigateToLevels = {
                        navController.navigate(Routes.LEVELS)
                    },
                    onStartPractice = { length ->
                        gameViewModel.startPracticeGame(length)
                        navController.navigate(Routes.GAME)
                    },
                    onStartDuel = {
                        navController.navigate(Routes.PASS_AND_PLAY)
                    },
                    onNavigateToMultiBoard = {
                        navController.navigate(Routes.MULTI_BOARD)
                    },
                    onNavigateToVault = {
                        navController.navigate(Routes.VAULT)
                    },
                    onNavigateToChallenge = {
                        navController.navigate(Routes.CHALLENGE)
                    },
                    onNavigateToStats = {
                        navController.navigate(Routes.STATS)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    onNavigateToQuests = {
                        navController.navigate(Routes.QUESTS)
                    },
                    onNavigateToCosmetics = {
                        navController.navigate(Routes.COSMETICS)
                    },
                    onNavigateToPassAndPlay = {
                        navController.navigate(Routes.PASS_AND_PLAY)
                    },
                    themePref = themePref,
                    darkMode = darkMode,
                    onToggleTheme = {
                        val next = if (darkMode) "LIGHT" else "DARK"
                        onThemeChange(next)
                    },
                )
            }

            composable(Routes.LEVELS) {
                LevelsScreen(
                    levelDao = levelDao,
                    onSelectLevel = { levelNum ->
                        gameViewModel.startCampaignLevel(levelNum)
                        navController.navigate(Routes.GAME)
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.GAME) {
                GameScreen(
                    viewModel = gameViewModel,
                    onBackToHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                )
            }

            composable(Routes.MULTI_BOARD) {
                MultiBoardScreen(
                    multiBoardEngine = multiBoardEngine,
                    wordRepository = wordRepository,
                    soundManager = soundManager,
                    playerPreferences = playerPreferences,
                    vaultDao = vaultDao,
                    questRepository = questRepository,
                    achievementDao = achievementDao,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.VAULT) {
                VaultScreen(
                    vaultDao = vaultDao,
                    onPracticeWord = { word ->
                        gameViewModel.startPracticeWithTarget(word)
                        navController.navigate(Routes.GAME)
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.CHALLENGE) {
                CustomChallengeScreen(
                    wordRepository = wordRepository,
                    onStartChallengeGame = { word, attempts ->
                        gameViewModel.startCustomChallenge(word, attempts)
                        navController.navigate(Routes.GAME)
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
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.STATS) {
                StatsScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.BADGES) {
                AchievementsScreen(
                    achievementDao = achievementDao,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    playerPreferences = playerPreferences,
                    soundManager = soundManager,
                    themePref = themePref,
                    darkMode = darkMode,
                    onThemeChange = onThemeChange,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
