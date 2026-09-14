package com.lexiguess.app.domain

import com.lexiguess.app.domain.model.TileState

/**
 * Pure-Kotlin Wordle evaluation engine.
 *
 * This class contains no Android dependencies and can be unit-tested on the JVM.
 * Separates curated [targetWords] (solution candidates) from the broad [validWordsSet]
 * (dictionary of acceptable guesses) for authentic Wordle gameplay.
 */
class GameEngine(
    private val wordList: MutableList<String> = mutableListOf(),
    private val targetWords: MutableList<String> = mutableListOf(),
) {

    companion object {
        const val WORD_LENGTH = 5
        const val MAX_ATTEMPTS = 6
    }

    // Fast O(1) lookup set for valid guesses (lowercase)
    private val validWordsSet: MutableSet<String> = HashSet(wordList.map { it.lowercase() })

    init {
        // Ensure any initial target words are also considered valid guesses
        for (w in targetWords) {
            val clean = w.lowercase().trim()
            if (clean.length == WORD_LENGTH) {
                validWordsSet.add(clean)
                if (clean !in wordList) wordList.add(clean)
            }
        }
    }

    /**
     * Returns true in O(1) time when [guess] is exactly five letters long and appears
     * in the acceptable dictionary.
     */
    fun isValidWord(guess: String): Boolean {
        if (guess.length != WORD_LENGTH) return false
        val g = guess.lowercase()
        return validWordsSet.contains(g) || wordList.any { it.equals(g, ignoreCase = true) }
    }

    /**
     * Evaluates [guess] against [target] and returns a list of [TileState] values
     * in the same order as the characters of [guess].
     *
     * The algorithm uses two passes:
     *  1. Mark every character that is in the correct position as [TileState.CORRECT].
     *  2. For each remaining character, mark it [TileState.MISPLACED] if it appears
     *     elsewhere in [target] (accounting for already-consumed occurrences), or
     *     [TileState.ABSENT] otherwise.
     */
    fun evaluate(guess: String, target: String): List<TileState> {
        require(guess.length == WORD_LENGTH) { "Guess must be $WORD_LENGTH letters" }
        require(target.length == WORD_LENGTH) { "Target must be $WORD_LENGTH letters" }

        val g = guess.uppercase()
        val t = target.uppercase()

        val result = Array(WORD_LENGTH) { TileState.ABSENT }
        val targetPool = t.toCharArray()

        // Pass 1 – correct positions
        for (i in 0 until WORD_LENGTH) {
            if (g[i] == targetPool[i]) {
                result[i] = TileState.CORRECT
                targetPool[i] = '\u0000' // consume
            }
        }

        // Pass 2 – misplaced / absent
        for (i in 0 until WORD_LENGTH) {
            if (result[i] == TileState.CORRECT) continue
            val idx = targetPool.indexOfFirst { it == g[i] }
            if (idx != -1) {
                result[i] = TileState.MISPLACED
                targetPool[idx] = '\u0000'
            }
        }

        return result.toList()
    }

    /**
     * Sets the curated target solution words (e.g. 2,315 familiar words).
     */
    fun setTargetWords(incoming: List<String>) {
        targetWords.clear()
        for (word in incoming) {
            val clean = word.trim().uppercase()
            if (clean.length == WORD_LENGTH) {
                targetWords.add(clean)
                val lower = clean.lowercase()
                validWordsSet.add(lower)
                if (lower !in wordList) wordList.add(lower)
            }
        }
    }

    /**
     * Selects a deterministic daily word from [targetWords] (or fallback to [wordList])
     * using the current date seed.
     */
    fun selectDailyWord(dateEpochDay: Long): String {
        val pool = if (targetWords.isNotEmpty()) targetWords else wordList
        if (pool.isEmpty()) return "CRANE"
        val index = (dateEpochDay % pool.size).toInt().let {
            if (it < 0) it + pool.size else it
        }
        return pool[index].uppercase()
    }

    /**
     * Selects a random target word for unlimited / practice mode.
     */
    fun selectRandomWord(): String {
        val pool = if (targetWords.isNotEmpty()) targetWords else wordList
        return pool.randomOrNull()?.uppercase() ?: "CRANE"
    }

    /**
     * Returns a [TileState.CORRECT] hint for the first position that has not yet
     * been correctly identified.
     */
    fun computeHint(
        target: String,
        revealedCorrect: Set<Int>,
    ): Pair<Int, Char>? {
        val t = target.uppercase()
        for (i in 0 until WORD_LENGTH) {
            if (i !in revealedCorrect) {
                return Pair(i, t[i])
            }
        }
        return null
    }

    /** Appends new valid words to the live dictionary, deduplicating as it goes. */
    fun mergeWords(incoming: List<String>) {
        for (word in incoming) {
            val w = word.trim().lowercase()
            if (w.length == WORD_LENGTH && validWordsSet.add(w)) {
                wordList.add(w)
            }
        }
    }
}
