package com.lexiguess.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lexiguess.app.data.db.QuestRecord
import com.lexiguess.app.data.repository.GameRepository
import com.lexiguess.app.data.repository.PlayerPreferences
import com.lexiguess.app.data.repository.QuestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestsViewModel @Inject constructor(
    private val questRepository: QuestRepository,
    private val gameRepository: GameRepository,
    val playerPreferences: PlayerPreferences,
) : ViewModel() {

    val quests: StateFlow<List<QuestRecord>> = questRepository.getTodayQuestsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val streakFreezes: StateFlow<Int> = playerPreferences.streakFreezesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)

    val xp: StateFlow<Int> = playerPreferences.xpFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val streak: StateFlow<Int> = gameRepository.currentStreakFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch {
            questRepository.ensureTodayQuestsSeeded()
        }
    }

    fun claimQuest(questId: String) {
        viewModelScope.launch {
            questRepository.claimQuestReward(questId)
        }
    }
}
