package com.lexiguess.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lexiguess.app.data.db.LevelDao
import com.lexiguess.app.data.repository.GameRepository
import com.lexiguess.app.data.repository.PlayerPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    val loading: Boolean = true,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val playerPreferences: PlayerPreferences,
    private val levelDao: LevelDao,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            val total = gameRepository.totalGames()
            val wins = gameRepository.totalWins()
            val streak = gameRepository.currentStreak()
            val best = gameRepository.bestStreak()
            val dist = gameRepository.guessDistribution()
            val fullDist = (1..6).associateWith { dist[it] ?: 0 }
            val winPct = if (total > 0) (wins * 100 / total) else 0

            val xp = playerPreferences.xpFlow.first()
            val lvl = PlayerPreferences.calculateLevel(xp)
            val rank = PlayerPreferences.rankForLevel(lvl)
            val rushBest = playerPreferences.rushHighScoreFlow.first()
            val completedLvls = levelDao.getCompletedCount().first()
            val stars = levelDao.getTotalStars().first()

            _state.value = StatsUiState(
                totalGames = total,
                totalWins = wins,
                winPercent = winPct,
                currentStreak = streak,
                bestStreak = best,
                distribution = fullDist,
                xp = xp,
                level = lvl,
                rankTitle = rank,
                rushHighScore = rushBest,
                completedLevels = completedLvls,
                totalStars = stars,
                loading = false,
            )
        }
    }
}
