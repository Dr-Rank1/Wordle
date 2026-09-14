package com.lexiguess.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing player progress on a campaign stage.
 *
 * @param levelNumber Unique level number (1 to 50).
 * @param wordLength Number of letters in the level's target word (4, 5, 6, 7).
 * @param targetWord The secret word for this level.
 * @param stars Stars earned (0 = unplayed, 1 = 5-6 attempts, 2 = 3-4 attempts, 3 = 1-2 attempts).
 * @param bestAttempts Lowest number of guesses used to solve.
 * @param completed Whether this stage has been conquered.
 */
@Entity(tableName = "level_records")
data class LevelRecord(
    @PrimaryKey val levelNumber: Int,
    val wordLength: Int = 5,
    val targetWord: String,
    val stars: Int = 0,
    val bestAttempts: Int = 0,
    val completed: Boolean = false,
)
