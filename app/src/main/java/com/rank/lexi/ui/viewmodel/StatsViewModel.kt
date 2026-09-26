package com.rank.lexi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rank.lexi.data.db.LevelDao
import com.rank.lexi.data.repository.GameRepository
import com.rank.lexi.data.repository.PlayerPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatsUiState(
    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val winPercent: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val distribution: Map<Int, Int> = (1..6).associateWith { 0 },
    val xp: Int = 0,
    val level: Int = 1,
    val rankTitle: String = "Novice",
    val rushHighScore: Int = 0,
    val completedLevels: Int = 0,
    val totalStars: Int = 0,
    val loading: Boolean = false,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    gameRepository: GameRepository,
    playerPreferences: PlayerPreferences,
    levelDao: LevelDao,
) : ViewModel() {

    val state: StateFlow<StatsUiState> = combine(
        gameRepository.totalGamesFlow(),
        gameRepository.totalWinsFlow(),
        gameRepository.currentStreakFlow(playerPreferences.streakShieldDatesFlow),
        gameRepository.bestStreakFlow(playerPreferences.streakShieldDatesFlow),
        gameRepository.guessDistributionFlow(),
        playerPreferences.xpFlow,
        playerPreferences.rushHighScoreFlow,
        levelDao.getCompletedCount(),
        levelDao.getTotalStars()
    ) { params ->
        val totalGames = params[0] as Int
        val totalWins = params[1] as Int
        val currentStreak = params[2] as Int
        val bestStreak = params[3] as Int
        @Suppress("UNCHECKED_CAST")
        val dist = params[4] as Map<Int, Int>
        val xp = params[5] as Int
        val rushHighScore = params[6] as Int
        val completedLevels = params[7] as Int
        val totalStars = params[8] as Int

        val fullDist = (1..6).associateWith { dist[it] ?: 0 }
        val winPct = if (totalGames > 0) (totalWins * 100 / totalGames) else 0
        val lvl = PlayerPreferences.calculateLevel(xp)
        val rank = PlayerPreferences.rankForLevel(lvl)

        StatsUiState(
            totalGames = totalGames,
            totalWins = totalWins,
            winPercent = winPct,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            distribution = fullDist,
            xp = xp,
            level = lvl,
            rankTitle = rank,
            rushHighScore = rushHighScore,
            completedLevels = completedLevels,
            totalStars = totalStars,
            loading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState(loading = true)
    )
}
