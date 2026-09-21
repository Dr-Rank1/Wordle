package com.rank.lexi.data.repository

import com.rank.lexi.data.db.CoinDao
import com.rank.lexi.data.db.CoinRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinRepository @Inject constructor(
    private val coinDao: CoinDao,
) {
    /** Reactive coin balance. */
    val coinsFlow: Flow<Int> = coinDao.getBalanceFlow()

    /** Recent transactions for history display. */
    val recentTransactionsFlow: Flow<List<CoinRecord>> = coinDao.getRecentTransactions()

    /** Credit coins. Always succeeds. */
    suspend fun addCoins(amount: Int, type: String, description: String) {
        coinDao.insertTransaction(
            CoinRecord(type = type, amount = amount, description = description)
        )
    }

    /**
     * Deduct [amount] coins.
     * @return `true` if successful, `false` if insufficient balance.
     */
    suspend fun spendCoins(amount: Int, type: String, description: String): Boolean {
        val balance = coinDao.getBalance()
        if (balance < amount) return false
        coinDao.insertTransaction(
            CoinRecord(type = type, amount = -amount, description = description)
        )
        return true
    }

    suspend fun getBalance(): Int = coinDao.getBalance()
}
