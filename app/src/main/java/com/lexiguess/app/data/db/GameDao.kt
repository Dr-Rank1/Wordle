package com.lexiguess.app.data.db

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

    /** Total games won. */
    @Query("SELECT COUNT(*) FROM game_records WHERE won = 1")
    suspend fun totalWins(): Int

    /**
     * Returns the current win streak: the number of consecutive won games
     * ending at the most recent record.
     */
    @Query(
        """
        SELECT COUNT(*) FROM game_records
        WHERE id >= (
            SELECT COALESCE(MAX(id), 0) FROM game_records WHERE won = 0
        )
        AND won = 1
        """
    )
    suspend fun currentStreak(): Int

    /** Maximum streak ever achieved. */
    @Query(
        """
        SELECT MAX(streak) FROM (
            SELECT COUNT(*) as streak
            FROM game_records
            WHERE won = 1
            GROUP BY (id - (SELECT COUNT(*) FROM game_records g2 WHERE g2.won = 0 AND g2.id <= game_records.id))
        )
        """
    )
    suspend fun bestStreak(): Int

    /**
     * Distribution of guess counts for won games.
     * Returns a map of attempt count (1-6) → number of wins with that count.
     */
    @Query(
        "SELECT attempts, COUNT(*) as count FROM game_records WHERE won = 1 GROUP BY attempts"
    )
    suspend fun guessDistribution(): List<GuessDistributionRow>

    data class GuessDistributionRow(val attempts: Int, val count: Int)
}
