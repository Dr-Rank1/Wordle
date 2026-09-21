package com.rank.lexi.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinDao {
    /** Running total of all transactions (positive = earn, negative = spend). */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM coin_transactions")
    fun getBalanceFlow(): Flow<Int>

    /** Snapshot balance for one-shot reads. */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM coin_transactions")
    suspend fun getBalance(): Int

    @Insert
    suspend fun insertTransaction(record: CoinRecord)

    @Query("SELECT * FROM coin_transactions ORDER BY timestampMs DESC LIMIT 20")
    fun getRecentTransactions(): Flow<List<CoinRecord>>
}
