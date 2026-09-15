package com.rank.lexi.data.di

import com.rank.lexi.domain.GameEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideGameEngine(): GameEngine =
        GameEngine(wordList = mutableListOf())
}
