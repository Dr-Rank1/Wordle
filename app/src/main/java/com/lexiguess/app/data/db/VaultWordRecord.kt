package com.lexiguess.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an unlocked vocabulary entry in the Word Vault.
 *
 * @param word The solved word in uppercase (e.g. "CRANE").
 * @param length Length of the word (4, 5, 6, or 7).
 * @param definition Short dictionary definition or meaning.
 * @param partOfSpeech e.g. "noun", "verb", "adjective".
 * @param example An example sentence using the word.
 * @param timesSolved Number of times the player has solved this word across any mode.
 * @param bestGuesses Fewest attempts taken to solve this word.
 * @param unlockedAt Timestamp in milliseconds when the word was first discovered.
 */
@Entity(tableName = "vault_words")
data class VaultWordRecord(
    @PrimaryKey val word: String,
    val length: Int,
    val definition: String,
    val partOfSpeech: String = "noun",
    val example: String = "",
    val timesSolved: Int = 1,
    val bestGuesses: Int = 6,
    val unlockedAt: Long = System.currentTimeMillis(),
)
