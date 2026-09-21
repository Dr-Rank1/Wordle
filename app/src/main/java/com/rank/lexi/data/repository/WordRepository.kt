package com.rank.lexi.data.repository

import android.content.Context
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.db.AchievementRecord
import com.rank.lexi.data.db.LevelDao
import com.rank.lexi.data.db.LevelRecord
import com.rank.lexi.data.db.VaultDao
import com.rank.lexi.data.db.VaultWordRecord
import com.rank.lexi.data.network.WordApiService
import com.rank.lexi.domain.CampaignSeeds
import com.rank.lexi.domain.GameEngine
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
    private val vaultDao: VaultDao,
) {
    private var initialized = false

    suspend fun initialize() {
        if (initialized) return
        withContext(Dispatchers.IO) {
            loadAllLocalDictionaries()
            seedCampaignLevelsIfEmpty()
            repairCampaignWordLengths()
            seedAchievementsIfEmpty()
            refreshFromNetwork()
            initialized = true
        }
    }

    fun dailyWord(length: Int = 5, epochDay: Long = LocalDate.now().toEpochDay()): String =
        engine.selectDailyWord(epochDay, length)

    fun randomWord(length: Int = 5): String =
        engine.selectRandomWord(length)

    fun isValidWord(guess: String, length: Int = 5): Boolean =
        engine.isValidWord(guess, length)

    fun getValidWordsSet(length: Int = 5): Set<String> =
        engine.getValidWordsSet(length)

    fun getTargetWordsCount(length: Int = 5): Int =
        engine.getTargetWordsCount(length)

    suspend fun fetchDefinition(word: String): String? = withContext(Dispatchers.IO) {
        val upper = word.uppercase().trim()
        val cached = vaultDao.getWord(upper)?.definition
        if (!cached.isNullOrBlank()) {
            return@withContext cached
        }
        val fallback = BUILT_IN_DEFINITIONS[upper]

        try {
            val url = "https://api.dictionaryapi.dev/api/v2/entries/en/${word.lowercase().trim()}"
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use fallback
                val body = response.body?.string() ?: return@use fallback
                val json = JSONArray(body)
                if (json.length() == 0) return@use fallback

                val firstEntry = json.getJSONObject(0)
                val meanings = firstEntry.optJSONArray("meanings") ?: return@use fallback
                if (meanings.length() == 0) return@use fallback

                val firstMeaning = meanings.getJSONObject(0)
                val partOfSpeech = firstMeaning.optString("partOfSpeech", "noun")
                val defs = firstMeaning.optJSONArray("definitions") ?: return@use fallback
                if (defs.length() == 0) return@use fallback

                val defText = defs.getJSONObject(0).optString("definition", "")
                if (defText.isBlank()) return@use fallback

                val formatted = if (partOfSpeech.isNotBlank()) "($partOfSpeech) $defText" else defText
                val existingRecord = vaultDao.getWord(upper)
                if (existingRecord != null) {
                    vaultDao.upsert(existingRecord.copy(definition = formatted, partOfSpeech = partOfSpeech))
                } else {
                    vaultDao.upsert(
                        VaultWordRecord(
                            word = upper,
                            length = upper.length,
                            definition = formatted,
                            partOfSpeech = partOfSpeech,
                        )
                    )
                }
                formatted
            }
        } catch (_: Exception) {
            fallback
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

    fun pickDistinctWords(length: Int, count: Int): List<String> =
        engine.pickDistinctWords(length, count)

    private suspend fun seedCampaignLevelsIfEmpty() {
        val count = levelDao.getLevel(1)
        if (count != null) return

        val levels = CampaignSeeds.allLevels().map { seed ->
            LevelRecord(
                levelNumber = seed.levelNumber,
                wordLength = seed.wordLength,
                targetWord = seed.targetWord,
            )
        }
        levelDao.insertInitialLevels(levels)
    }

    private suspend fun repairCampaignWordLengths() {
        for (seed in CampaignSeeds.allLevels()) {
            val existing = levelDao.getLevel(seed.levelNumber) ?: continue
            if (existing.targetWord.length != seed.wordLength || existing.wordLength != seed.wordLength) {
                levelDao.upsertLevel(
                    existing.copy(
                        wordLength = seed.wordLength,
                        targetWord = seed.targetWord,
                    )
                )
            }
        }
    }

    private suspend fun seedAchievementsIfEmpty() {
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
            AchievementRecord("MULTI_MASTER", "Multi-Board Master", "Solve any Dordle or Quordle puzzle", "Dashboard", 0, 1),
            AchievementRecord("RARE_HUNTER", "Cipher Breaker", "Solve a word containing Q, X, or Z", "Psychology", 0, 1),
            AchievementRecord("FLAWLESS_SWEEP", "Deadeye", "Solve a puzzle in 2 guesses or fewer", "Whatshot", 0, 1),
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

    companion object {
        private val BUILT_IN_DEFINITIONS = mapOf(
            "CRANE" to "(noun) A large, tall machine used for moving heavy objects, or a tall wading bird.",
            "SLATE" to "(noun) A fine-grained grey, green, or bluish-purple metamorphic rock.",
            "LIGHT" to "(noun) The natural agent that stimulates sight and makes things visible.",
            "AUDIO" to "(noun) Sound, especially when recorded, transmitted, or reproduced.",
            "ROAST" to "(verb) Cook food by prolonged exposure to heat in an oven or over a fire.",
            "HEART" to "(noun) A hollow muscular organ that pumps blood through the circulatory system.",
            "OCEAN" to "(noun) A very large expanse of sea, in particular each of the main areas into which the sea is divided.",
            "DREAM" to "(noun) A series of thoughts, images, and sensations occurring in a person's mind during sleep.",
            "FLAME" to "(noun) A hot glowing body of ignited gas that is generated by something on fire.",
            "SHIELD" to "(noun) A broad piece of armor carried on the arm or in the hand for protection.",
            "MAGIC" to "(noun) The power of apparently influencing events by using mysterious or supernatural forces.",
            "PEACH" to "(noun) A round juicy fruit with yellow or reddish skin and a hard rough stone.",
            "SPARK" to "(noun) A small fiery particle thrown off from a fire, alight in ash, or produced by striking stone on metal.",
            "BLAZE" to "(noun) A very large or fiercely burning fire.",
            "SWORD" to "(noun) A weapon with a long metal blade and a hilt with a hand guard.",
            "CLOUD" to "(noun) A visible mass of condensed water vapor floating in the atmosphere.",
            "RIVER" to "(noun) A large natural stream of water flowing in a channel to the sea, a lake, or another stream.",
            "NIGHT" to "(noun) The period of darkness in each twenty-four hours; the time between evening and morning.",
            "STARS" to "(noun) A fixed luminous point in the night sky which is a large, remote incandescent body.",
            "PLANT" to "(noun) A living organism of the kind exemplified by trees, shrubs, herbs, grasses, and ferns."
        )
    }
}
