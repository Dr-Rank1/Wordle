package com.lexiguess.app.data.repository

import android.content.Context
import com.lexiguess.app.data.network.WordApiService
import com.lexiguess.app.domain.GameEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hybrid word repository.
 *
 * Strategy:
 *  1. Bundles 2,315 curated target words (`assets/target_words.txt`) so daily/practice
 *     words are always recognizable, fun English words.
 *  2. Bundles 12,972 valid guess words (`assets/valid_words.txt`) so players can guess
 *     any legitimate 5-letter word without getting erroneously rejected.
 *  3. Fetches word definitions from the Free Dictionary API.
 *  4. Syncs updates from remote API in background when connected.
 */
@Singleton
class WordRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordApiService: WordApiService,
    private val okHttpClient: OkHttpClient,
    private val engine: GameEngine,
) {
    private var initialized = false

    /**
     * Loads the local asset word lists into the engine, then tries to refresh
     * from the remote API in the background. Safe to call multiple times.
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

    /** Returns a random word for Practice / Unlimited mode. */
    fun randomWord(): String =
        engine.selectRandomWord()

    /**
     * Fetches a short dictionary definition for the given [word] from the Free Dictionary API.
     */
    suspend fun fetchDefinition(word: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.dictionaryapi.dev/api/v2/entries/en/${word.lowercase().trim()}"
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string() ?: return@use null
                val json = JSONArray(body)
                if (json.length() == 0) return@use null

                val firstEntry = json.getJSONObject(0)
                val meanings = firstEntry.optJSONArray("meanings") ?: return@use null
                if (meanings.length() == 0) return@use null

                val firstMeaning = meanings.getJSONObject(0)
                val partOfSpeech = firstMeaning.optString("partOfSpeech", "")
                val defs = firstMeaning.optJSONArray("definitions") ?: return@use null
                if (defs.length() == 0) return@use null

                val defText = defs.getJSONObject(0).optString("definition", "")
                if (defText.isBlank()) return@use null

                if (partOfSpeech.isNotBlank()) "($partOfSpeech) $defText" else defText
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun loadLocal() {
        // 1. Curated target words (solutions)
        try {
            val targetLines = context.assets.open("target_words.txt")
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
            if (targetLines.isNotEmpty()) {
                engine.setTargetWords(targetLines)
            }
        } catch (_: Exception) {
            engine.setTargetWords(EMERGENCY_WORDS)
        }

        // 2. Full allowed guess dictionary
        try {
            val validLines = context.assets.open("valid_words.txt")
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
            if (validLines.isNotEmpty()) {
                engine.mergeWords(validLines)
            }
        } catch (_: Exception) {
            try {
                val fallbackLines = context.assets.open("words.txt")
                    .bufferedReader()
                    .readLines()
                    .filter { it.isNotBlank() }
                engine.mergeWords(fallbackLines)
            } catch (_: Exception) {
                engine.mergeWords(EMERGENCY_WORDS)
            }
        }
    }

    private suspend fun refreshFromNetwork() {
        try {
            val raw = wordApiService.fetchWordList()
            val words = raw.lines().filter { it.isNotBlank() }
            if (words.isNotEmpty()) {
                engine.mergeWords(words)
            }
        } catch (_: Exception) {
            // Network unavailable – continue with offline asset dictionary
        }
    }

    companion object {
        private val EMERGENCY_WORDS = listOf(
            "crane", "slate", "audio", "raise", "stare",
            "snare", "trace", "arose", "least", "light",
            "blunt", "cloud", "draft", "earth", "flute",
        )
    }
}
