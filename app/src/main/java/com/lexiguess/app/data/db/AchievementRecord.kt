package com.lexiguess.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing milestone achievement badges.
 *
 * @param id Unique badge key (e.g. "FIRST_WIN", "STREAK_7", "SPEED_DEMON").
 * @param title Display title.
 * @param description Requirement description.
 * @param iconName Symbolic icon name.
 * @param currentProgress Current numerical progress.
 * @param targetProgress Target numerical progress required for unlock.
 * @param unlocked Whether the achievement is unlocked.
 * @param unlockedAt Timestamp string when badge was earned.
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
)
