package com.rank.lexi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.db.LevelDao
import com.rank.lexi.data.db.VaultDao
import com.rank.lexi.data.repository.PlayerPreferences
import com.rank.lexi.data.repository.WordRepository
import com.rank.lexi.domain.GameEngine
import com.rank.lexi.domain.MultiBoardEngine
import com.rank.lexi.ui.audio.SoundManager
import com.rank.lexi.ui.navigation.LexiGuessNavGraph
import com.rank.lexi.ui.theme.ALL_BOARD_THEMES
import com.rank.lexi.ui.theme.EmeraldTheme
import com.rank.lexi.ui.theme.LexiGuessTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playerPreferences: PlayerPreferences

    @Inject
    lateinit var levelDao: LevelDao

    @Inject
    lateinit var achievementDao: AchievementDao

    @Inject
    lateinit var vaultDao: VaultDao

    @Inject
    lateinit var wordRepository: WordRepository

    @Inject
    lateinit var multiBoardEngine: MultiBoardEngine

    @Inject
    lateinit var gameEngine: GameEngine

    @Inject
    lateinit var gameRepository: com.rank.lexi.data.repository.GameRepository

    @Inject
    lateinit var soundManager: SoundManager

    @Inject
    lateinit var questRepository: com.rank.lexi.data.repository.QuestRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val coroutineScope = rememberCoroutineScope()
            val themePref by playerPreferences.themeFlow.collectAsState(initial = "SYSTEM")
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val darkMode = when (themePref) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemDark
            }
            val currentBoardThemeId by playerPreferences.boardThemeFlow.collectAsState(initial = "EMERALD")
            val currentTileMaterial by playerPreferences.tileMaterialFlow.collectAsState(initial = "CLASSIC")
            val tiltParallaxEnabled by playerPreferences.tiltParallaxFlow.collectAsState(initial = true)
            val activeTheme = ALL_BOARD_THEMES.find { it.id == currentBoardThemeId } ?: EmeraldTheme

            LexiGuessTheme(
                darkTheme = darkMode,
                boardTheme = activeTheme,
                tileMaterial = currentTileMaterial,
                tiltParallaxEnabled = tiltParallaxEnabled,
            ) {
                LexiGuessNavGraph(
                    playerPreferences = playerPreferences,
                    gameRepository = gameRepository,
                    levelDao = levelDao,
                    achievementDao = achievementDao,
                    vaultDao = vaultDao,
                    wordRepository = wordRepository,
                    multiBoardEngine = multiBoardEngine,
                    gameEngine = gameEngine,
                    soundManager = soundManager,
                    questRepository = questRepository,
                    themePref = themePref,
                    darkMode = darkMode,
                    onThemeChange = { newTheme ->
                        coroutineScope.launch {
                            playerPreferences.setTheme(newTheme)
                        }
                    },
                )
            }
        }
    }
}
