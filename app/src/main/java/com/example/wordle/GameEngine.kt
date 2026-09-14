package com.example.wordle

import java.util.Random

/**
 * Pure‑Kotlin game engine that contains the Wordle logic.
 * It does not depend on any Android framework classes, making it easy to unit‑test.
 */
class GameEngine(private val wordList: List<String>, private val targetWordProvider: (() -> String)? = null) {
    private val targetWord: String = targetWordProvider?.invoke() ?: wordList.random().uppercase()
    private val maxAttempts = 6
    private val wordLength = 5

    var attempts = mutableListOf<String>()
        private set

    /**
     * Returns true if the guess is valid (exists in the word list and has the correct length).
     */
    fun isValidGuess(guess: String): Boolean {
        return guess.length == wordLength && wordList.contains(guess.lowercase())
    }

    /**
     * Submits a guess and returns a list of [LetterResult] describing each character.
     * The list size is always equal to [wordLength].
     * If the guess is invalid an [IllegalArgumentException] is thrown.
     */
    fun submitGuess(rawGuess: String): List<LetterResult> {
        val guess = rawGuess.uppercase()
        require(isValidGuess(guess)) { "Invalid guess: $guess" }
        require(attempts.size < maxAttempts) { "No attempts left" }
        attempts.add(guess)
        return evaluateGuess(guess)
    }

    /**
     * Checks whether the latest guess solved the puzzle.
     */
    fun isGameWon(): Boolean = attempts.lastOrNull()?.equals(targetWord) == true

    /**
     * Checks whether the player has exhausted all attempts.
     */
    fun isGameOver(): Boolean = attempts.size >= maxAttempts || isGameWon()

    /**
     * Returns the answer word – useful for displaying after a loss.
     */
    fun getTargetWord(): String = targetWord

    private fun evaluateGuess(guess: String): List<LetterResult> {
        // First pass – mark correct positions
        val result = mutableListOf<LetterResult>()
        val targetChars = targetWord.toCharArray()
        val used = BooleanArray(wordLength)
        // Correct (green)
        for (i in 0 until wordLength) {
            if (guess[i] == targetChars[i]) {
                result.add(LetterResult(guess[i], LetterState.CORRECT))
                used[i] = true
                targetChars[i] = '\u0000' // invalidate so it won't be matched as misplaced
            } else {
                result.add(LetterResult(guess[i], LetterState.UNKNOWN)) // placeholder
            }
        }
        // Second pass – check misplaced (yellow) and wrong (gray)
        for (i in 0 until wordLength) {
            if (result[i].state == LetterState.UNKNOWN) {
                val idx = targetChars.indexOfFirst { it == guess[i] && it != '\u0000' }
                if (idx != -1) {
                    result[i] = LetterResult(guess[i], LetterState.MISPLACED)
                    targetChars[idx] = '\u0000'
                } else {
                    result[i] = LetterResult(guess[i], LetterState.ABSENT)
                }
            }
        }
        return result
    }
}

data class LetterResult(val char: Char, val state: LetterState)

enum class LetterState { CORRECT, MISPLACED, ABSENT, UNKNOWN }
