package com.rank.lexi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing milestone achievement badges.
 *
 * @param id Unique badge key.
 * @param title Display title.
 * @param description Requirement description.
 * @param iconName Symbolic icon name.
 * @param currentProgress Current numerical progress.
 * @param targetProgress Target numerical progress required for unlock.
 * @param unlocked Whether the achievement is unlocked.
 * @param unlockedAt Timestamp string when badge was earned.
 * @param coinReward Number of coins rewarded.
 * @param isClaimed Whether the coins have been claimed by the user.
 */
@Entity(tableName = "achievements")
data class AchievementRecord(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val unlocked: Boolean = false,
    val unlockedAt: String? = null,
    val coinReward: Int = 15,
    val isClaimed: Boolean = false,
)
