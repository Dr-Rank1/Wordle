package com.rank.lexi.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.db.AppDatabase
import com.rank.lexi.data.db.CoinDao
import com.rank.lexi.data.db.GameDao
import com.rank.lexi.data.db.LevelDao
import com.rank.lexi.data.db.QuestDao
import com.rank.lexi.data.db.VaultDao
import com.rank.lexi.data.repository.CoinRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE game_records ADD COLUMN mode TEXT NOT NULL DEFAULT 'DAILY'")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `coin_transactions` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `type` TEXT NOT NULL,
                    `amount` INTEGER NOT NULL,
                    `timestampMs` INTEGER NOT NULL,
                    `description` TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE achievements ADD COLUMN coinReward INTEGER NOT NULL DEFAULT 15")
            db.execSQL("ALTER TABLE achievements ADD COLUMN isClaimed INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        )
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    fun provideGameDao(database: AppDatabase): GameDao = database.gameDao()

    @Provides
    fun provideLevelDao(database: AppDatabase): LevelDao = database.levelDao()

    @Provides
    fun provideAchievementDao(database: AppDatabase): AchievementDao = database.achievementDao()

    @Provides
    fun provideVaultDao(database: AppDatabase): VaultDao = database.vaultDao()

    @Provides
    fun provideQuestDao(database: AppDatabase): QuestDao = database.questDao()

    @Provides
    fun provideCoinDao(database: AppDatabase): CoinDao = database.coinDao()

    @Provides
    @Singleton
    fun provideCoinRepository(coinDao: CoinDao): CoinRepository = CoinRepository(coinDao)
}
