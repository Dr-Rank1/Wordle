package com.lexiguess.app.domain

import com.lexiguess.app.domain.model.TileState

/**
 * Enhanced multi-length Wordle evaluation engine.
 *
 * Supports 4, 5, 6, and 7-letter words, Hard Mode rule verification,
 * O(1) dictionary lookups, and Wordle-Bot style candidate count tracking.
 */
class GameEngine(
    private val wordList: MutableList<String> = mutableListOf(),
    private val targetWords: MutableList<String> = mutableListOf(),
) {

    companion object {
        const val DEFAULT_WORD_LENGTH = 5
        const val MAX_ATTEMPTS = 6
    }

    // Fast O(1) lookup set for valid guesses (lowercase)
    private val validWordsSet: MutableSet<String> = HashSet(wordList.map { it.lowercase() })

    // Dictionaries partitioned by word length for multi-length modes
    private val multiLengthTargets: MutableMap<Int, MutableList<String>> = mutableMapOf()
    private val multiLengthValid: MutableMap<Int, MutableSet<String>> = mutableMapOf()

    init {
        for (w in targetWords) {
            val clean = w.lowercase().trim()
            if (clean.isNotEmpty()) {
                validWordsSet.add(clean)
                if (clean !in wordList) wordList.add(clean)
                multiLengthTargets.getOrPut(clean.length) { mutableListOf() }.add(clean.uppercase())
                multiLengthValid.getOrPut(clean.length) { HashSet() }.add(clean)
            }
        }
        for (w in wordList) {
            val clean = w.lowercase().trim()
            if (clean.isNotEmpty()) {
                multiLengthValid.getOrPut(clean.length) { HashSet() }.add(clean)
            }
        }
    }

    /**
     * Returns true in O(1) time when [guess] is found in the acceptable dictionary.
     */
    fun isValidWord(guess: String, expectedLength: Int = DEFAULT_WORD_LENGTH): Boolean {
        if (guess.length != expectedLength) return false
        val g = guess.lowercase()
        val set = multiLengthValid[expectedLength]
        return (set != null && set.contains(g)) || validWordsSet.contains(g) || wordList.any { it.equals(g, ignoreCase = true) }
    }

    /**
     * Evaluates [guess] against [target] and returns a list of [TileState] values.
     */
    fun evaluate(guess: String, target: String): List<TileState> {
        val len = target.length
        require(guess.length == len) { "Guess ($guess) and target ($target) must have matching lengths ($len)" }

        val g = guess.uppercase()
        val t = target.uppercase()

        val result = Array(len) { TileState.ABSENT }
        val targetPool = t.toCharArray()

        // Pass 1 – correct positions
        for (i in 0 until len) {
            if (g[i] == targetPool[i]) {
                result[i] = TileState.CORRECT
                targetPool[i] = '\u0000' // consume
            }
        }

        // Pass 2 – misplaced / absent
        for (i in 0 until len) {
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
     * Checks whether [guess] violates Hard Mode constraints given [previousEvaluations].
     * Returns an error message if violated, or null if guess is valid.
     */
    fun validateHardMode(
        guess: String,
        previousEvaluations: List<Pair<String, List<TileState>>>,
    ): String? {
        val g = guess.uppercase()
        for ((prevGuess, states) in previousEvaluations) {
            // 1. All CORRECT letters must remain in exact same position
            for (i in states.indices) {
                if (states[i] == TileState.CORRECT && (i >= g.length || g[i] != prevGuess[i])) {
                    val posLabel = when (i + 1) {
                        1 -> "1st"
                        2 -> "2nd"
                        3 -> "3rd"
                        else -> "${i + 1}th"
                    }
                    return "$posLabel letter must be ${prevGuess[i]}"
                }
            }

            // 2. All MISPLACED letters must be present in the new guess
            for (i in states.indices) {
                if (states[i] == TileState.MISPLACED) {
                    val requiredChar = prevGuess[i]
                    if (!g.contains(requiredChar)) {
                        return "Guess must contain $requiredChar"
                    }
                }
            }
        }
        return null
    }

    /**
     * Calculates how many possible candidate target words remain consistent with all
     * [previousEvaluations] (Wordle Bot metric).
     */
    fun countRemainingCandidates(
        previousEvaluations: List<Pair<String, List<TileState>>>,
        length: Int = DEFAULT_WORD_LENGTH,
    ): Int {
        val pool = multiLengthTargets[length] ?: (if (length == DEFAULT_WORD_LENGTH) targetWords else emptyList())
        if (pool.isEmpty() || previousEvaluations.isEmpty()) return pool.size

        return pool.count { candidate ->
            previousEvaluations.all { (prevGuess, states) ->
                evaluate(prevGuess, candidate) == states
            }
        }
    }

    /** Sets the curated target solution words. */
    fun setTargetWords(incoming: List<String>, length: Int = DEFAULT_WORD_LENGTH) {
        val list = multiLengthTargets.getOrPut(length) { mutableListOf() }
        list.clear()
        val validSet = multiLengthValid.getOrPut(length) { HashSet() }

        if (length == DEFAULT_WORD_LENGTH) {
            targetWords.clear()
        }

        for (word in incoming) {
            val clean = word.trim().uppercase()
            if (clean.length == length) {
                list.add(clean)
                validSet.add(clean.lowercase())
                validWordsSet.add(clean.lowercase())
                if (length == DEFAULT_WORD_LENGTH) {
                    targetWords.add(clean)
                    if (clean.lowercase() !in wordList) wordList.add(clean.lowercase())
                }
            }
        }
    }

    /** Selects a deterministic daily word from curated targets. */
    fun selectDailyWord(dateEpochDay: Long, length: Int = DEFAULT_WORD_LENGTH): String {
        val pool = multiLengthTargets[length] ?: (if (length == DEFAULT_WORD_LENGTH) targetWords else wordList)
        if (pool.isEmpty()) return "CRANE".take(length).padEnd(length, 'A')
        val index = (dateEpochDay % pool.size).toInt().let {
            if (it < 0) it + pool.size else it
        }
        return pool[index].uppercase()
    }

    /** Selects a random target word for unlimited practice or timed rush mode. */
    fun selectRandomWord(length: Int = DEFAULT_WORD_LENGTH): String {
        val pool = multiLengthTargets[length] ?: (if (length == DEFAULT_WORD_LENGTH) targetWords else wordList)
        return pool.randomOrNull()?.uppercase() ?: "CRANE".take(length).padEnd(length, 'A')
    }

    /** Returns a hint for the first unrevealed position. */
    fun computeHint(
        target: String,
        revealedCorrect: Set<Int>,
    ): Pair<Int, Char>? {
        val t = target.uppercase()
        for (i in 0 until target.length) {
            if (i !in revealedCorrect) {
                return Pair(i, t[i])
            }
        }
        return null
    }

    /** Merges new words into dictionary. */
    fun mergeWords(incoming: List<String>, length: Int = DEFAULT_WORD_LENGTH) {
        val set = multiLengthValid.getOrPut(length) { HashSet() }
        for (word in incoming) {
            val w = word.trim().lowercase()
            if (w.length == length && set.add(w)) {
                validWordsSet.add(w)
                if (length == DEFAULT_WORD_LENGTH) {
                    wordList.add(w)
                }
            }
        }
    }
}
