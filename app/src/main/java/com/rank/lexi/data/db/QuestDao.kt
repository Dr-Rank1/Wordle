package com.rank.lexi.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM daily_quests WHERE dateKey = :dateKey ORDER BY id ASC")
    fun getQuestsForDateFlow(dateKey: String): Flow<List<QuestRecord>>

    @Query("SELECT * FROM daily_quests WHERE dateKey = :dateKey")
    suspend fun getQuestsForDate(dateKey: String): List<QuestRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<QuestRecord>)

    @Update
    suspend fun updateQuest(quest: QuestRecord)

    @Query("UPDATE daily_quests SET currentProgress = :progress, isCompleted = :completed WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, completed: Boolean)

    @Query("UPDATE daily_quests SET isClaimed = 1 WHERE id = :id")
    suspend fun markClaimed(id: String)
}
