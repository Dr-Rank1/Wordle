package com.rank.lexi.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_words ORDER BY unlockedAt DESC")
    fun getAllWords(): Flow<List<VaultWordRecord>>

    @Query("SELECT * FROM vault_words WHERE length = :length ORDER BY unlockedAt DESC")
    fun getWordsByLength(length: Int): Flow<List<VaultWordRecord>>

    @Query("SELECT * FROM vault_words WHERE word LIKE '%' || :query || '%' ORDER BY word ASC")
    fun searchWords(query: String): Flow<List<VaultWordRecord>>

    @Query("SELECT * FROM vault_words WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): VaultWordRecord?

    @Query("SELECT COUNT(*) FROM vault_words")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: VaultWordRecord)
}
