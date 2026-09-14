package com.lexiguess.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        GameRecord::class,
        LevelRecord::class,
        AchievementRecord::class,
        VaultWordRecord::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun levelDao(): LevelDao
    abstract fun achievementDao(): AchievementDao
    abstract fun vaultDao(): VaultDao

    companion object {
        const val DATABASE_NAME = "lexiguess_db"
    }
}
