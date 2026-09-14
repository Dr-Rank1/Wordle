package com.lexiguess.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.lexiguess.app.data.db.GameDao
import com.lexiguess.app.data.db.GameRecord
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_prefs")

/**
 * Manages persisted game state and history.
 *
 *  - Active-game state (current input, guesses for today) is stored in DataStore.
 *  - Completed game records are stored in Room via [GameDao].
 */
@Singleton
class GameRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameDao: GameDao,
) {
    // -------------------------------------------------------------------------
    // DataStore keys
    // -------------------------------------------------------------------------
    private object Keys {
        val DATE_KEY = stringPreferencesKey("date")
        val TARGET_WORD = stringPreferencesKey("target_word")
        val GUESSES = stringPreferencesKey("guesses")       // comma-separated
        val HINT_USED = booleanPreferencesKey("hint_used")
        val GAME_OVER = booleanPreferencesKey("game_over")
        val WON = booleanPreferencesKey("won")
    }

    // -------------------------------------------------------------------------
    // Active game state
    // -------------------------------------------------------------------------

    /** Saves the target word and resets the active session for today. */
    suspend fun startNewGame(targetWord: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DATE_KEY] = LocalDate.now().toString()
            prefs[Keys.TARGET_WORD] = targetWord
            prefs[Keys.GUESSES] = ""
            prefs[Keys.HINT_USED] = false
            prefs[Keys.GAME_OVER] = false
            prefs[Keys.WON] = false
        }
    }

    /** Returns true if there is an active game for today. */
    suspend fun hasActiveGameForToday(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.DATE_KEY] == LocalDate.now().toString() &&
                prefs[Keys.GAME_OVER] != true
    }

    /** Returns the saved target word for the active session, or null. */
    suspend fun savedTargetWord(): String? =
        context.dataStore.data.first()[Keys.TARGET_WORD]

    /** Returns the list of guesses submitted so far today. */
    suspend fun savedGuesses(): List<String> {
        val raw = context.dataStore.data.first()[Keys.GUESSES] ?: return emptyList()
        return if (raw.isBlank()) emptyList() else raw.split(",")
    }

    suspend fun savedHintUsed(): Boolean =
        context.dataStore.data.first()[Keys.HINT_USED] ?: false

    /** Appends a guess to the stored list. */
    suspend fun appendGuess(guess: String) {
        context.dataStore.edit { prefs ->
            val existing = prefs[Keys.GUESSES] ?: ""
            prefs[Keys.GUESSES] = if (existing.isBlank()) guess else "$existing,$guess"
        }
    }

    suspend fun saveHintUsed() {
        context.dataStore.edit { it[Keys.HINT_USED] = true }
    }

    /** Marks the active game as finished and writes a [GameRecord] to Room. */
    suspend fun finalizeGame(record: GameRecord) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GAME_OVER] = true
            prefs[Keys.WON] = record.won
        }
        gameDao.insertGame(record)
    }

    // -------------------------------------------------------------------------
    // History (Room)
    // -------------------------------------------------------------------------

    fun allGames(): Flow<List<GameRecord>> = gameDao.getAllGames()

    suspend fun totalGames(): Int = gameDao.totalGames()

    suspend fun totalWins(): Int = gameDao.totalWins()

    suspend fun currentStreak(): Int = gameDao.currentStreak()

    suspend fun bestStreak(): Int = gameDao.bestStreak()

    suspend fun guessDistribution(): Map<Int, Int> =
        gameDao.guessDistribution().associate { it.attempts to it.count }
}
