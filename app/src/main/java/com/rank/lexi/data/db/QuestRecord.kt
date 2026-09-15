package com.rank.lexi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_quests")
data class QuestRecord(
    @PrimaryKey val id: String,
    val dateKey: String,
    val title: String,
    val description: String,
    val questType: String,
    val targetCount: Int,
    val currentProgress: Int = 0,
    val xpReward: Int = 150,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
)
