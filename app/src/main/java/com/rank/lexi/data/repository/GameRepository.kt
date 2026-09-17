package com.rank.lexi.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.rank.lexi.data.db.GameDao
import com.rank.lexi.data.db.GameRecord
import com.rank.lexi.domain.model.DailyStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    private object Keys {
        val DATE_KEY = stringPreferencesKey("date")
        val TARGET_WORD = stringPreferencesKey("target_word")
        val GUESSES = stringPreferencesKey("guesses")
        val HINT_USED = booleanPreferencesKey("hint_used")
        val GAME_OVER = booleanPreferencesKey("game_over")
        val WON = booleanPreferencesKey("won")
    }

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

    suspend fun hasActiveGameForToday(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.DATE_KEY] == LocalDate.now().toString() &&
                prefs[Keys.GAME_OVER] != true &&
                !prefs[Keys.TARGET_WORD].isNullOrBlank()
    }

    /** True when today's daily was started (in progress or finished). */
    suspend fun hasSessionForToday(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.DATE_KEY] == LocalDate.now().toString() &&
                !prefs[Keys.TARGET_WORD].isNullOrBlank()
    }

    val dailyStatusFlow: Flow<DailyStatus> = context.dataStore.data.map { prefs ->
        val today = LocalDate.now().toString()
        if (prefs[Keys.DATE_KEY] != today || prefs[Keys.TARGET_WORD].isNullOrBlank()) {
            DailyStatus.NOT_STARTED
        } else if (prefs[Keys.GAME_OVER] == true) {
            if (prefs[Keys.WON] == true) DailyStatus.WON else DailyStatus.LOST
        } else {
            DailyStatus.IN_PROGRESS
        }
    }

    suspend fun savedTargetWord(): String? =
        context.dataStore.data.first()[Keys.TARGET_WORD]

    suspend fun savedGuesses(): List<String> {
        val raw = context.dataStore.data.first()[Keys.GUESSES] ?: return emptyList()
        return if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
    }

    suspend fun savedHintUsed(): Boolean =
        context.dataStore.data.first()[Keys.HINT_USED] ?: false

    suspend fun appendGuess(guess: String) {
        context.dataStore.edit { prefs ->
            val existing = prefs[Keys.GUESSES] ?: ""
            prefs[Keys.GUESSES] = if (existing.isBlank()) guess else "$existing,$guess"
        }
    }

    suspend fun saveHintUsed() {
        context.dataStore.edit { it[Keys.HINT_USED] = true }
    }

    suspend fun finalizeGame(record: GameRecord) {
        if (record.mode == "DAILY") {
            context.dataStore.edit { prefs ->
                prefs[Keys.GAME_OVER] = true
                prefs[Keys.WON] = record.won
            }
        }
        gameDao.insertGame(record)
    }

    suspend fun insertHistory(record: GameRecord) {
        gameDao.insertGame(record)
    }

    fun allGames(): Flow<List<GameRecord>> = gameDao.getAllGames()

    suspend fun totalGames(): Int = gameDao.totalGames()

    suspend fun totalWins(): Int = gameDao.totalWins()

    suspend fun getWonDates(): List<String> = gameDao.getWonDates()

    val wonDatesFlow: Flow<List<String>> = gameDao.getWonDatesFlow()

    private fun streakDates(wonDates: Collection<String>, shieldDates: Collection<String>): Set<LocalDate> {
        return (wonDates + shieldDates)
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .toSet()
    }

    suspend fun currentStreak(shieldDates: Collection<String> = emptySet()): Int {
        return computeCurrentStreak(gameDao.getWonDates(), shieldDates)
    }

    val currentStreakFlow: Flow<Int> = combine(
        gameDao.getWonDatesFlow(),
        context.dataStore.data,
    ) { dates, _ ->
        computeCurrentStreak(dates, emptySet())
    }

    fun currentStreakFlow(shieldDates: Flow<Set<String>>): Flow<Int> =
        combine(gameDao.getWonDatesFlow(), shieldDates) { dates, shields ->
            computeCurrentStreak(dates, shields)
        }

    private fun computeCurrentStreak(wonDatesList: List<String>, shieldDates: Collection<String>): Int {
        val dates = streakDates(wonDatesList, shieldDates)
        if (dates.isEmpty()) return 0
        val today = LocalDate.now()
        var checkDate = when {
            dates.contains(today) -> today
            dates.contains(today.minusDays(1)) -> today.minusDays(1)
            else -> return 0
        }
        var streak = 0
        while (dates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }

    suspend fun bestStreak(shieldDates: Collection<String> = emptySet()): Int {
        val wonDates = streakDates(gameDao.getWonDates(), shieldDates)
            .toList()
            .sorted()
        if (wonDates.isEmpty()) return 0
        var maxStreak = 1
        var current = 1
        for (i in 1 until wonDates.size) {
            if (wonDates[i] == wonDates[i - 1].plusDays(1)) {
                current++
                if (current > maxStreak) maxStreak = current
            } else if (wonDates[i] != wonDates[i - 1]) {
                current = 1
            }
        }
        return maxStreak
    }

    suspend fun checkAndApplyStreakShield(playerPreferences: PlayerPreferences): Boolean {
        val shieldDates = playerPreferences.streakShieldDates()
        val wonDates = gameDao.getWonDates()
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .toSet()
        val covered = wonDates + shieldDates.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (covered.isEmpty()) return false
        val today = LocalDate.now()
        if (covered.contains(today) || covered.contains(today.minusDays(1))) {
            return false
        }
        if (covered.contains(today.minusDays(2))) {
            val freezes = playerPreferences.streakFreezesFlow.first()
            if (freezes > 0) {
                val used = playerPreferences.consumeStreakFreeze()
                if (used) {
                    playerPreferences.addStreakShieldDate(today.minusDays(1).toString())
                    return true
                }
            }
        }
        return false
    }

    suspend fun guessDistribution(): Map<Int, Int> =
        gameDao.guessDistribution().associate { it.attempts to it.count }
}
