package com.rank.lexi.data.repository

import com.rank.lexi.data.db.QuestDao
import com.rank.lexi.data.db.QuestRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestRepository @Inject constructor(
    private val questDao: QuestDao,
    private val playerPreferences: PlayerPreferences,
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun getTodayKey(): String = dateFormat.format(Date())

    fun getTodayQuestsFlow(): Flow<List<QuestRecord>> {
        val today = getTodayKey()
        return questDao.getQuestsForDateFlow(today)
    }

    suspend fun ensureTodayQuestsSeeded(): List<QuestRecord> {
        val today = getTodayKey()
        val existing = questDao.getQuestsForDate(today)
        if (existing.isNotEmpty()) return existing

        val seeded = generateDailyQuests(today)
        questDao.insertQuests(seeded)
        return seeded
    }

    suspend fun onPuzzleSolved(
        wordLength: Int,
        attempts: Int,
        mode: String,
        solveDurationSeconds: Long,
    ) {
        val today = getTodayKey()
        val quests = questDao.getQuestsForDate(today).ifEmpty {
            ensureTodayQuestsSeeded()
        }

        for (quest in quests) {
            if (quest.isCompleted) continue

            var newProgress = quest.currentProgress
            var shouldIncrement = false

            when (quest.questType) {
                "SOLVE_ANY" -> {
                    shouldIncrement = true
                }
                "FEW_GUESSES" -> {
                    if (attempts <= 4) shouldIncrement = true
                }
                "LONG_WORD" -> {
                    if (wordLength >= 6) shouldIncrement = true
                }
                "SPEED_SOLVE" -> {
                    if (solveDurationSeconds in 1..60) shouldIncrement = true
                }
                "MULTI_BOARD" -> {
                    if (mode == "DORDLE" || mode == "QUORDLE") shouldIncrement = true
                }
            }

            if (shouldIncrement) {
                newProgress = (quest.currentProgress + 1).coerceAtMost(quest.targetCount)
                val completed = newProgress >= quest.targetCount
                questDao.updateProgress(quest.id, newProgress, completed)
            }
        }
    }

    suspend fun claimQuestReward(questId: String): Int {
        val today = getTodayKey()
        val quests = questDao.getQuestsForDate(today)
        val quest = quests.find { it.id == questId } ?: return 0
        if (!quest.isCompleted || quest.isClaimed) return 0

        questDao.markClaimed(questId)
        playerPreferences.addXp(quest.xpReward)
        return quest.xpReward
    }

    private fun generateDailyQuests(todayKey: String): List<QuestRecord> {
        return listOf(
            QuestRecord(
                id = "${todayKey}_q1",
                dateKey = todayKey,
                title = "Word Hunter",
                description = "Solve 2 puzzles in any game mode",
                questType = "SOLVE_ANY",
                targetCount = 2,
                xpReward = 150,
            ),
            QuestRecord(
                id = "${todayKey}_q2",
                dateKey = todayKey,
                title = "Sharpshooter",
                description = "Solve a puzzle in 4 guesses or fewer",
                questType = "FEW_GUESSES",
                targetCount = 1,
                xpReward = 200,
            ),
            QuestRecord(
                id = "${todayKey}_q3",
                dateKey = todayKey,
                title = "Lexicon Adventurer",
                description = "Solve a 6-letter or 7-letter word puzzle",
                questType = "LONG_WORD",
                targetCount = 1,
                xpReward = 175,
            ),
        )
    }
}
