package com.lexiguess.app.data.repository

import android.content.Context
import com.lexiguess.app.data.network.WordApiService
import com.lexiguess.app.domain.GameEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hybrid word repository.
 *
 * Strategy:
 *  1. On first access, load words from the bundled `assets/words.txt`.
 *  2. In the background, attempt to fetch the latest list from the GitHub API
 *     and merge it into the live list via [GameEngine.mergeWords].
 *  3. The daily word is always selected deterministically from the merged list.
 */
@Singleton
class WordRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordApiService: WordApiService,
    private val engine: GameEngine,
) {
    private var initialized = false

    /**
     * Loads the local asset word list into the engine, then tries to refresh
     * from the remote API. Safe to call multiple times (no-op after first call).
     */
    suspend fun initialize() {
        if (initialized) return
        withContext(Dispatchers.IO) {
            loadLocal()
            refreshFromNetwork()
            initialized = true
        }
    }

    /** Returns the daily target word based on today's date. */
    fun dailyWord(): String =
        engine.selectDailyWord(LocalDate.now().toEpochDay())

    private fun loadLocal() {
        try {
            val lines = context.assets.open("words.txt")
                .bufferedReader()
                .readLines()
            engine.mergeWords(lines)
        } catch (e: Exception) {
            // Seed with a small emergency list if the asset is missing
            engine.mergeWords(EMERGENCY_WORDS)
        }
    }

    private suspend fun refreshFromNetwork() {
        try {
            val raw = wordApiService.fetchWordList()
            val words = raw.lines().filter { it.isNotBlank() }
            engine.mergeWords(words)
        } catch (_: Exception) {
            // Network unavailable – silently continue with local list
        }
    }

    companion object {
        private val EMERGENCY_WORDS = listOf(
            "crane", "slate", "audio", "raise", "stare",
            "snare", "trace", "arose", "least", "light",
        )
    }
}
