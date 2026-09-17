package com.rank.lexi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single completed game stored in the Room database.
 *
 * @param id          Auto-generated primary key.
 * @param datePlayed  ISO-8601 date string (e.g. "2026-09-14").
 * @param targetWord  The secret word for that session.
 * @param won         True if the player guessed correctly.
 * @param attempts    Number of guesses used (1-6). 0 if the game was abandoned.
 * @param guesses     Comma-separated list of the player's guesses.
 * @param timestamp   Unix epoch millis when the game was completed.
 */
@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val datePlayed: String,
    val targetWord: String,
    val won: Boolean,
    val attempts: Int,
    val guesses: String,        // "CRANE,BLINK,LIGHT"
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "DAILY",
)
