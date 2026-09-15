package com.lexiguess.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.lexiguess.app.data.db.AchievementDao
import com.lexiguess.app.data.db.LevelDao
import com.lexiguess.app.data.db.VaultDao
import com.lexiguess.app.data.repository.PlayerPreferences
import com.lexiguess.app.data.repository.WordRepository
import com.lexiguess.app.domain.GameEngine
import com.lexiguess.app.domain.MultiBoardEngine
import com.lexiguess.app.ui.audio.SoundManager
import com.lexiguess.app.ui.navigation.LexiGuessNavGraph
import com.lexiguess.app.ui.theme.ALL_BOARD_THEMES
import com.lexiguess.app.ui.theme.EmeraldTheme
import com.lexiguess.app.ui.theme.LexiGuessTheme
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
    lateinit var gameRepository: com.lexiguess.app.data.repository.GameRepository

    @Inject
    lateinit var soundManager: SoundManager

    @Inject
    lateinit var questRepository: com.lexiguess.app.data.repository.QuestRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val coroutineScope = rememberCoroutineScope()
            val themePref by playerPreferences.themeFlow.collectAsState(initial = "DARK")
            val darkMode = themePref == "DARK"
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
                    darkMode = darkMode,
                    onDarkModeChange = { newDark ->
                        coroutineScope.launch {
                            playerPreferences.setTheme(if (newDark) "DARK" else "LIGHT")
                        }
                    },
                )
            }
        }
    }
}
