package com.lexiguess.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lexiguess.app.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val winPercent: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    /** Map of attempt count (1-6) to number of wins. */
    val distribution: Map<Int, Int> = (1..6).associateWith { 0 },
    val loading: Boolean = true,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
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

            _state.value = StatsUiState(
                totalGames = total,
                totalWins = wins,
                winPercent = winPct,
                currentStreak = streak,
                bestStreak = best,
                distribution = fullDist,
                loading = false,
            )
        }
    }
}
