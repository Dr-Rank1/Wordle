package com.lexiguess.app.data.repository

import android.content.Context
import com.lexiguess.app.data.db.AchievementDao
import com.lexiguess.app.data.db.AchievementRecord
import com.lexiguess.app.data.db.LevelDao
import com.lexiguess.app.data.db.LevelRecord
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

@Singleton
class WordRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordApiService: WordApiService,
    private val okHttpClient: OkHttpClient,
    private val engine: GameEngine,
    private val levelDao: LevelDao,
    private val achievementDao: AchievementDao,
) {
    private var initialized = false

    suspend fun initialize() {
        if (initialized) return
        withContext(Dispatchers.IO) {
            loadAllLocalDictionaries()
            seedCampaignLevelsIfEmpty()
            seedAchievementsIfEmpty()
            refreshFromNetwork()
            initialized = true
        }
    }

    fun dailyWord(length: Int = 5): String =
        engine.selectDailyWord(LocalDate.now().toEpochDay(), length)

    fun randomWord(length: Int = 5): String =
        engine.selectRandomWord(length)

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

    private fun loadAllLocalDictionaries() {
        // 4-letter words
        loadAssetFile("words_4.txt") { lines ->
            engine.setTargetWords(lines.take(800), 4)
            engine.mergeWords(lines, 4)
        }

        // 5-letter target words
        loadAssetFile("target_words.txt") { lines ->
            engine.setTargetWords(lines, 5)
        }

        // 5-letter valid words
        loadAssetFile("valid_words.txt") { lines ->
            engine.mergeWords(lines, 5)
        }

        // 6-letter words
        loadAssetFile("words_6.txt") { lines ->
            engine.setTargetWords(lines.take(1500), 6)
            engine.mergeWords(lines, 6)
        }

        // 7-letter words
        loadAssetFile("words_7.txt") { lines ->
            engine.setTargetWords(lines.take(2000), 7)
            engine.mergeWords(lines, 7)
        }
    }

    private inline fun loadAssetFile(filename: String, block: (List<String>) -> Unit) {
        try {
            val lines = context.assets.open(filename)
                .bufferedReader()
                .readLines()
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
            if (lines.isNotEmpty()) {
                block(lines)
            }
        } catch (_: Exception) {
            // Asset load fallback
        }
    }

    private suspend fun seedCampaignLevelsIfEmpty() {
        val count = levelDao.getLevel(1)
        if (count != null) return

        val sample4 = listOf("BIRD", "COLD", "FIRE", "GOLD", "LION", "MOON", "RAIN", "STAR", "WIND", "TREE")
        val sample5 = listOf(
            "APPLE", "BEACH", "CHAIR", "DREAM", "EARTH", "FLAME", "GRAPE", "HEART", "IMAGE", "JUICE",
            "KNIFE", "LEMON", "MAGIC", "NIGHT", "OCEAN", "PIZZA", "QUEEN", "RIVER", "SUGAR", "TIGER"
        )
        val sample6 = listOf("BRIDGE", "CASTLE", "DRAGON", "FOREST", "GALAXY", "ISLAND", "JUNGLE", "KNIGHT", "MONKEY", "PLANET")
        val sample7 = listOf("CHAMPION", "DIAMOND", "FANTASY", "HARMONY", "JOURNEY", "KINGDOM", "MYSTERY", "PHOENIX", "RAINBOW", "VICTORY")

        val levels = mutableListOf<LevelRecord>()
        var lvl = 1

        // World 1: 4 letters (1-10)
        for (w in sample4) {
            levels.add(LevelRecord(levelNumber = lvl++, wordLength = 4, targetWord = w))
        }
        // World 2: 5 letters (11-30)
        for (w in sample5) {
            levels.add(LevelRecord(levelNumber = lvl++, wordLength = 5, targetWord = w))
        }
        // World 3: 6 letters (31-40)
        for (w in sample6) {
            levels.add(LevelRecord(levelNumber = lvl++, wordLength = 6, targetWord = w))
        }
        // World 4: 7 letters (41-50)
        for (w in sample7) {
            levels.add(LevelRecord(levelNumber = lvl++, wordLength = 7, targetWord = w))
        }

        levelDao.insertInitialLevels(levels)
    }

    private suspend fun seedAchievementsIfEmpty() {
        val check = achievementDao.getAchievement("FIRST_WIN")
        if (check != null) return

        val initial = listOf(
            AchievementRecord("FIRST_WIN", "First Triumph", "Win your first game in any mode", "Star", 0, 1),
            AchievementRecord("WIN_5", "Word Enthusiast", "Win 5 games", "EmojiEvents", 0, 5),
            AchievementRecord("WIN_25", "Vocab Veteran", "Win 25 games", "MilitaryTech", 0, 25),
            AchievementRecord("STREAK_3", "On a Roll", "Reach a 3-day daily streak", "TrendingUp", 0, 3),
            AchievementRecord("STREAK_7", "Week Warrior", "Reach a 7-day daily streak", "DateRange", 0, 7),
            AchievementRecord("STREAK_30", "Iron Mind", "Reach a 30-day daily streak", "Shield", 0, 30),
            AchievementRecord("GENIUS_1", "Pucker Up", "Solve a puzzle on Attempt 1", "Bolt", 0, 1),
            AchievementRecord("CLUTCH_6", "Clutch King", "Solve a puzzle on Attempt 6", "Favorite", 0, 1),
            AchievementRecord("SPEED_DEMON", "Speed Demon", "Solve a puzzle in under 45 seconds", "Timer", 0, 1),
            AchievementRecord("RUSH_3", "Rush Runner", "Solve 3 words in one Timed Rush session", "Speed", 0, 3),
            AchievementRecord("RUSH_6", "Rush Maestro", "Solve 6 words in one Timed Rush session", "WorkspacePremium", 0, 6),
            AchievementRecord("LEVEL_10", "World 1 Conqueror", "Complete all Level 1-10 stages", "CheckCircle", 0, 10),
            AchievementRecord("LEVEL_30", "World 2 Conqueror", "Complete all Level 11-30 stages", "CheckCircle", 0, 20),
            AchievementRecord("LEVEL_50", "Grandmaster of Words", "Complete all 50 Campaign stages", "Grade", 0, 50),
            AchievementRecord("HARD_MODE_WIN", "Steel Resolve", "Win a game with Hard Mode enabled", "Lock", 0, 1),
            AchievementRecord("DUEL_PLAYED", "Friendly Rivalry", "Play a Pass & Play 2-Player Duel", "People", 0, 1),
        )
        achievementDao.insertInitialAchievements(initial)
    }

    private suspend fun refreshFromNetwork() {
        try {
            val raw = wordApiService.fetchWordList()
            val words = raw.lines().filter { it.isNotBlank() }
            if (words.isNotEmpty()) {
                engine.mergeWords(words, 5)
            }
        } catch (_: Exception) {}
    }
}
