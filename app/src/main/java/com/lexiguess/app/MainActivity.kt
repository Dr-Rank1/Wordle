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
import com.lexiguess.app.domain.MultiBoardEngine
import com.lexiguess.app.ui.audio.SoundManager
import com.lexiguess.app.ui.navigation.LexiGuessNavGraph
import com.lexiguess.app.ui.theme.ALL_BOARD_THEMES
import com.lexiguess.app.ui.theme.EmeraldTheme
import com.lexiguess.app.ui.theme.LexiGuessTheme
import dagger.hilt.android.AndroidEntryPoint
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
    lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkMode by rememberSaveable { mutableStateOf(false) }
            val currentBoardThemeId by playerPreferences.boardThemeFlow.collectAsState(initial = "EMERALD")
            val activeTheme = ALL_BOARD_THEMES.find { it.id == currentBoardThemeId } ?: EmeraldTheme

            LexiGuessTheme(darkTheme = darkMode, boardTheme = activeTheme) {
                LexiGuessNavGraph(
                    playerPreferences = playerPreferences,
                    levelDao = levelDao,
                    achievementDao = achievementDao,
                    vaultDao = vaultDao,
                    wordRepository = wordRepository,
                    multiBoardEngine = multiBoardEngine,
                    soundManager = soundManager,
                    darkMode = darkMode,
                    onDarkModeChange = { darkMode = it },
                )
            }
        }
    }
}
