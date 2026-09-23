package com.rank.lexi.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(record: GameRecord)

    /** All games, newest first. */
    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    fun getAllGames(): Flow<List<GameRecord>>

    /** Total games played. */
    @Query("SELECT COUNT(*) FROM game_records")
    suspend fun totalGames(): Int

    @Query("SELECT COUNT(*) FROM game_records")
    fun totalGamesFlow(): Flow<Int>

    /** Total games won. */
    @Query("SELECT COUNT(*) FROM game_records WHERE won = 1")
    suspend fun totalWins(): Int

    @Query("SELECT COUNT(*) FROM game_records WHERE won = 1")
    fun totalWinsFlow(): Flow<Int>

    /** Distinct dates won in Daily mode, sorted descending. */
    @Query("SELECT DISTINCT datePlayed FROM game_records WHERE won = 1 AND mode = 'DAILY' ORDER BY datePlayed DESC")
    suspend fun getWonDates(): List<String>

    @Query("SELECT DISTINCT datePlayed FROM game_records WHERE won = 1 AND mode = 'DAILY' ORDER BY datePlayed DESC")
    fun getWonDatesFlow(): Flow<List<String>>

    /**
     * Distribution of guess counts for won games.
     * Returns a map of attempt count (1-6) → number of wins with that count.
     */
    @Query(
        "SELECT attempts, COUNT(*) as count FROM game_records WHERE won = 1 AND attempts BETWEEN 1 AND 6 AND mode = 'DAILY' GROUP BY attempts"
    )
    suspend fun guessDistribution(): List<GuessDistributionRow>

    @Query(
        "SELECT attempts, COUNT(*) as count FROM game_records WHERE won = 1 AND attempts BETWEEN 1 AND 6 AND mode = 'DAILY' GROUP BY attempts"
    )
    fun guessDistributionFlow(): Flow<List<GuessDistributionRow>>

    data class GuessDistributionRow(val attempts: Int, val count: Int)
}
