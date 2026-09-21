package com.rank.lexi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.google.android.gms.ads.MobileAds
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.db.LevelDao
import com.rank.lexi.data.db.VaultDao
import com.rank.lexi.data.repository.CoinRepository
import com.rank.lexi.data.repository.GameRepository
import com.rank.lexi.data.repository.PlayerPreferences
import com.rank.lexi.data.repository.WordRepository
import com.rank.lexi.domain.GameEngine
import com.rank.lexi.ui.audio.SoundManager
import com.rank.lexi.ui.navigation.LexiGuessNavGraph
import com.rank.lexi.ui.theme.LexiGuessTheme
import com.rank.lexi.ui.theme.boardThemeById
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playerPreferences: PlayerPreferences

    @Inject
    lateinit var gameRepository: GameRepository

    @Inject
    lateinit var coinRepository: CoinRepository

    @Inject
    lateinit var levelDao: LevelDao

    @Inject
    lateinit var vaultDao: VaultDao

    @Inject
    lateinit var achievementDao: AchievementDao

    @Inject
    lateinit var wordRepository: WordRepository

    @Inject
    lateinit var gameEngine: GameEngine

    @Inject
    lateinit var soundManager: SoundManager

    @Inject
    lateinit var adManager: com.rank.lexi.ads.AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        adManager.loadRewardedAd()
        adManager.loadInterstitialAd()
        adManager.loadAppOpenAd()

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
            val reducedMotion by playerPreferences.reducedMotionFlow.collectAsState(initial = false)
            val activeTheme = boardThemeById(currentBoardThemeId)

            LexiGuessTheme(
                darkTheme = darkMode,
                boardTheme = activeTheme,
                tileMaterial = currentTileMaterial,
                tiltParallaxEnabled = tiltParallaxEnabled,
                reducedMotion = reducedMotion,
            ) {
                LexiGuessNavGraph(
                    playerPreferences = playerPreferences,
                    gameRepository = gameRepository,
                    coinRepository = coinRepository,
                    levelDao = levelDao,
                    vaultDao = vaultDao,
                    achievementDao = achievementDao,
                    wordRepository = wordRepository,
                    gameEngine = gameEngine,
                    soundManager = soundManager,
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

    override fun onStart() {
        super.onStart()
        adManager.showAppOpenAdIfAvailable(this)
    }
}

