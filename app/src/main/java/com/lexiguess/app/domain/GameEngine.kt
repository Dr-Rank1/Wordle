package com.lexiguess.app.domain

import com.lexiguess.app.domain.model.TileState

/**
 * Pure-Kotlin Wordle evaluation engine.
 *
 * This class contains no Android dependencies and can be unit-tested on the JVM.
 * Pass in a mutable word list; the list may grow at runtime as new words are
 * fetched from the network.
 */
class GameEngine(private val wordList: MutableList<String>) {

    companion object {
        const val WORD_LENGTH = 5
        const val MAX_ATTEMPTS = 6
    }

    /**
     * Returns true when [guess] is exactly five letters long and appears in the
     * current word list (case-insensitive).
     */
    fun isValidWord(guess: String): Boolean =
        guess.length == WORD_LENGTH && wordList.any { it.equals(guess, ignoreCase = true) }

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
     * Selects a deterministic daily word from [wordList] using the current date
     * as the seed, falling back to a random word if the list is empty.
     */
    fun selectDailyWord(dateEpochDay: Long): String {
        if (wordList.isEmpty()) return "CRANE"
        val index = (dateEpochDay % wordList.size).toInt().let {
            if (it < 0) it + wordList.size else it
        }
        return wordList[index].uppercase()
    }

    /**
     * Returns a [TileState.CORRECT] hint for the first position that has not yet
     * been correctly identified. Returns null if the player has already found all
     * positions or if the current guess is empty.
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

    /** Appends new words to the live word list, deduplicating as it goes. */
    fun mergeWords(incoming: List<String>) {
        val existing = wordList.map { it.lowercase() }.toHashSet()
        for (word in incoming) {
            val w = word.lowercase()
            if (w.length == WORD_LENGTH && w !in existing) {
                wordList.add(w)
                existing.add(w)
            }
        }
    }
}
