package com.rank.lexi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.db.VaultDao
import com.rank.lexi.data.db.VaultWordRecord
import com.rank.lexi.data.repository.PlayerPreferences
import com.rank.lexi.data.repository.QuestRepository
import com.rank.lexi.data.repository.WordRepository
import com.rank.lexi.domain.MultiBoardEngine
import com.rank.lexi.domain.model.GameStatus
import com.rank.lexi.domain.model.MultiBoardMode
import com.rank.lexi.domain.model.MultiBoardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MultiBoardViewModel @Inject constructor(
    private val multiBoardEngine: MultiBoardEngine,
    private val wordRepository: WordRepository,
    val playerPreferences: PlayerPreferences,
    private val vaultDao: VaultDao,
    private val questRepository: QuestRepository,
    private val achievementDao: AchievementDao,
) : ViewModel() {

    private val _mode = MutableStateFlow(MultiBoardMode.DORDLE)
    val mode: StateFlow<MultiBoardMode> = _mode.asStateFlow()

    private val _state = MutableStateFlow<MultiBoardState?>(null)
    val state: StateFlow<MultiBoardState?> = _state.asStateFlow()

    private val _showEndSheet = MutableStateFlow(false)
    val showEndSheet: StateFlow<Boolean> = _showEndSheet.asStateFlow()

    init {
        viewModelScope.launch {
            wordRepository.initialize()
            startMatch(MultiBoardMode.DORDLE)
        }
    }

    fun startMatch(mode: MultiBoardMode = _mode.value) {
        _mode.value = mode
        _showEndSheet.value = false
        val targets = wordRepository.pickDistinctWords(5, mode.boardCount)
        _state.value = multiBoardEngine.startNewGame(mode, targets)
    }

    fun toggleMode() {
        val next = if (_mode.value == MultiBoardMode.DORDLE) MultiBoardMode.QUORDLE else MultiBoardMode.DORDLE
        startMatch(next)
    }

    fun onLetter(char: Char) {
        val current = _state.value ?: return
        if (current.status != GameStatus.IN_PROGRESS) return
        _state.value = multiBoardEngine.onLetterInput(current, char)
    }

    fun onDelete() {
        val current = _state.value ?: return
        if (current.status != GameStatus.IN_PROGRESS) return
        _state.value = multiBoardEngine.onDelete(current)
    }

    fun onSubmit() {
        val current = _state.value ?: return
        if (current.status != GameStatus.IN_PROGRESS) return
        val validSet = wordRepository.getValidWordsSet(current.wordLength)
        val newState = multiBoardEngine.submitGuess(current, validSet)
        _state.value = newState
        if (newState.status != GameStatus.IN_PROGRESS) {
            _showEndSheet.value = true
            if (newState.status == GameStatus.WON) {
                viewModelScope.launch { awardWin(newState) }
            }
        }
    }

    fun dismissEndSheet() {
        _showEndSheet.value = false
    }

    private suspend fun awardWin(newState: MultiBoardState) {
        playerPreferences.addXp(if (newState.mode == MultiBoardMode.DORDLE) 120 else 250)
        questRepository.onPuzzleSolved(
            wordLength = newState.wordLength,
            attempts = newState.currentRow,
            mode = newState.mode.name,
            solveDurationSeconds = 0L,
        )
        try {
            val existing = achievementDao.getAchievement("MULTI_MASTER")
            if (existing != null && !existing.unlocked) {
                achievementDao.upsertAchievement(
                    existing.copy(
                        unlocked = true,
                        currentProgress = 1,
                        unlockedAt = LocalDate.now().toString(),
                    )
                )
                playerPreferences.addXp(150)
            }
        } catch (_: Exception) {}

        newState.boards.forEach { board ->
            val existing = vaultDao.getWord(board.targetWord)
            vaultDao.upsert(
                VaultWordRecord(
                    word = board.targetWord,
                    length = newState.wordLength,
                    definition = "Solved in Multi-Board ${newState.mode.title}",
                    partOfSpeech = "word",
                    example = "",
                    timesSolved = (existing?.timesSolved ?: 0) + 1,
                    bestGuesses = minOf(existing?.bestGuesses ?: 9, board.guesses.size),
                    unlockedAt = existing?.unlockedAt ?: System.currentTimeMillis(),
                )
            )
        }
    }
}
