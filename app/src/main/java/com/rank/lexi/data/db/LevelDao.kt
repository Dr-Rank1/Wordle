package com.rank.lexi.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {

    @Query("SELECT * FROM level_records ORDER BY levelNumber ASC")
    fun getAllLevels(): Flow<List<LevelRecord>>

    @Query("SELECT * FROM level_records WHERE levelNumber = :levelNumber LIMIT 1")
    suspend fun getLevel(levelNumber: Int): LevelRecord?

    @Query("SELECT COUNT(*) FROM level_records WHERE completed = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(stars), 0) FROM level_records")
    fun getTotalStars(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLevel(record: LevelRecord)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialLevels(records: List<LevelRecord>)
}
