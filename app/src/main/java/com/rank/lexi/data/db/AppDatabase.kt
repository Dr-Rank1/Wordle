package com.rank.lexi.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        GameRecord::class,
        LevelRecord::class,
        AchievementRecord::class,
        VaultWordRecord::class,
        QuestRecord::class,
        CoinRecord::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun levelDao(): LevelDao
    abstract fun achievementDao(): AchievementDao
    abstract fun vaultDao(): VaultDao
    abstract fun questDao(): QuestDao
    abstract fun coinDao(): CoinDao

    companion object {
        const val DATABASE_NAME = "lexiguess_db"
    }
}
