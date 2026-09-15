package com.lexiguess.app.data

import com.lexiguess.app.data.db.GameDao
import com.lexiguess.app.data.db.GameRecord
import com.lexiguess.app.domain.GameEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class StreakCalendarTest {

    private class FakeGameDao : GameDao {
        val records = mutableListOf<GameRecord>()

        override suspend fun insertGame(record: GameRecord) {
            records.add(record)
        }

        override fun getAllGames(): Flow<List<GameRecord>> = flowOf(records)

        override suspend fun totalGames(): Int = records.size

        override suspend fun totalWins(): Int = records.count { it.won }

        override suspend fun getWonDates(): List<String> =
            records.filter { it.won }.map { it.datePlayed }.distinct().sortedDescending()

        override fun getWonDatesFlow(): Flow<List<String>> =
            flowOf(records.filter { it.won }.map { it.datePlayed }.distinct().sortedDescending())

        override suspend fun guessDistribution(): List<GameDao.GuessDistributionRow> =
            records.filter { it.won && it.attempts in 1..6 }
                .groupBy { it.attempts }
                .map { (att, list) -> GameDao.GuessDistributionRow(att, list.size) }
    }

    private fun computeCurrentStreak(wonDatesList: List<String>): Int {
        val wonDates = wonDatesList.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()
        if (wonDates.isEmpty()) return 0
        val today = LocalDate.now()
        var checkDate = when {
            wonDates.contains(today) -> today
            wonDates.contains(today.minusDays(1)) -> today.minusDays(1)
            else -> return 0
        }
        var streak = 0
        while (wonDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }

    private fun computeBestStreak(wonDatesList: List<String>): Int {
        val wonDates = wonDatesList
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .distinct()
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

    @Test
    fun `current streak counts consecutive days ending today`() {
        val today = LocalDate.now()
        val dates = listOf(
            today.toString(),
            today.minusDays(1).toString(),
            today.minusDays(2).toString(),
            today.minusDays(5).toString(), // gap
        )
        val streak = computeCurrentStreak(dates)
        assertEquals(3, streak)
    }

    @Test
    fun `current streak survives if today not yet played but yesterday won`() {
        val today = LocalDate.now()
        val dates = listOf(
            today.minusDays(1).toString(),
            today.minusDays(2).toString(),
            today.minusDays(3).toString(),
        )
        val streak = computeCurrentStreak(dates)
        assertEquals(3, streak)
    }

    @Test
    fun `current streak resets to 0 if last win was 2 or more days ago`() {
        val today = LocalDate.now()
        val dates = listOf(
            today.minusDays(2).toString(),
            today.minusDays(3).toString(),
        )
        val streak = computeCurrentStreak(dates)
        assertEquals(0, streak)
    }

    @Test
    fun `best streak finds longest continuous run across history`() {
        val start = LocalDate.of(2026, 1, 1)
        val dates = listOf(
            // Run of 2
            start.toString(),
            start.plusDays(1).toString(),
            // Gap
            start.plusDays(5).toString(),
            start.plusDays(6).toString(),
            start.plusDays(7).toString(),
            start.plusDays(8).toString(),
            start.plusDays(9).toString(), // Run of 5
            // Gap
            start.plusDays(20).toString(),
        )
        val best = computeBestStreak(dates)
        assertEquals(5, best)
    }

    @Test
    fun `fake dao records games and reports valid guess distribution`() = runTest {
        val dao = FakeGameDao()
        dao.insertGame(GameRecord(datePlayed = "2026-09-01", targetWord = "CRANE", won = true, attempts = 3, guesses = "A,B,C"))
        dao.insertGame(GameRecord(datePlayed = "2026-09-02", targetWord = "SLATE", won = true, attempts = 3, guesses = "A,B,C"))
        dao.insertGame(GameRecord(datePlayed = "2026-09-03", targetWord = "AUDIO", won = true, attempts = 5, guesses = "A,B,C,D,E"))
        dao.insertGame(GameRecord(datePlayed = "2026-09-04", targetWord = "LIGHT", won = false, attempts = 6, guesses = "A,B,C,D,E,F"))
        // Streak shield synthetic record
        dao.insertGame(GameRecord(datePlayed = "2026-09-05", targetWord = "SHIELD", won = true, attempts = 0, guesses = "SHIELD"))

        assertEquals(5, dao.totalGames())
        assertEquals(4, dao.totalWins())

        val dist = dao.guessDistribution()
        assertEquals(2, dist.first { it.attempts == 3 }.count)
        assertEquals(1, dist.first { it.attempts == 5 }.count)
        assertFalse("Attempts = 0 from shield must not appear in distribution", dist.any { it.attempts == 0 })
    }

    @Test
    fun `game engine provides target words count`() {
        val engine = GameEngine(mutableListOf("crane", "slate", "audio", "raise", "stare"))
        engine.setTargetWords(listOf("crane", "slate"), 5)
        assertEquals(2, engine.getTargetWordsCount(5))
        assertEquals(0, engine.getTargetWordsCount(6))
    }
}
