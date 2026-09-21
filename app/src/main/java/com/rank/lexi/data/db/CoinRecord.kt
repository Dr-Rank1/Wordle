package com.rank.lexi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records a single coin transaction (earn or spend).
 *
 * @param id Auto-generated primary key.
 * @param type Transaction type: "EARN_AD", "SPEND_EXTRA_GUESS", "SPEND_REVEAL", "SPEND_SKIP", "SPEND_FREEZE".
 * @param amount Positive for earnings, negative for spending.
 * @param timestampMs Unix epoch milliseconds.
 * @param description Human-readable reason.
 */
@Entity(tableName = "coin_transactions")
data class CoinRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Int,
    val timestampMs: Long = System.currentTimeMillis(),
    val description: String,
)
