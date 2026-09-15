package com.rank.lexi.data.di

import android.content.Context
import androidx.room.Room
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.db.AppDatabase
import com.rank.lexi.data.db.GameDao
import com.rank.lexi.data.db.LevelDao
import com.rank.lexi.data.db.QuestDao
import com.rank.lexi.data.db.VaultDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).fallbackToDestructiveMigration().build()

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
}
