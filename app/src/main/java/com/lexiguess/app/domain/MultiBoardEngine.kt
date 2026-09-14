package com.lexiguess.app.domain

import com.lexiguess.app.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MultiBoardEngine @Inject constructor(
    private val gameEngine: GameEngine,
) {

    /**
     * Initializes a new multi-board match for [mode] with chosen [targetWords].
     */
    fun startNewGame(mode: MultiBoardMode, targetWords: List<String>): MultiBoardState {
        require(targetWords.size == mode.boardCount) {
            "Expected ${mode.boardCount} target words for ${mode.name}, received ${targetWords.size}"
        }

        val boards = targetWords.mapIndexed { index, target ->
            SingleBoardState(
                boardIndex = index,
                targetWord = target.uppercase(),
            )
        }

        return MultiBoardState(
            mode = mode,
            wordLength = targetWords.firstOrNull()?.length ?: 5,
            boards = boards,
            currentInput = "",
            currentRow = 0,
            status = GameStatus.IN_PROGRESS,
            keyBoardStates = ('A'..'Z').associateWith { List(mode.boardCount) { TileState.EMPTY } },
            shake = false,
            message = null,
            showConfetti = false,
        )
    }

    /**
     * Appends a character to the active input row if length not yet reached.
     */
    fun onLetterInput(state: MultiBoardState, letter: Char): MultiBoardState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (state.currentInput.length >= state.wordLength) return state
        val uppercase = letter.uppercaseChar()
        if (uppercase !in 'A'..'Z') return state

        return state.copy(
            currentInput = state.currentInput + uppercase,
            shake = false,
            message = null,
        )
    }

    /**
     * Deletes the last character from current input.
     */
    fun onDelete(state: MultiBoardState): MultiBoardState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (state.currentInput.isEmpty()) return state

        return state.copy(
            currentInput = state.currentInput.dropLast(1),
            shake = false,
            message = null,
        )
    }

    /**
     * Evaluates the current guess across all unsolved boards.
     */
    fun submitGuess(state: MultiBoardState, validWords: Set<String>): MultiBoardState {
        if (state.status != GameStatus.IN_PROGRESS) return state

        val guess = state.currentInput.uppercase()
        if (guess.length != state.wordLength) {
            return state.copy(shake = true, message = "Word must be ${state.wordLength} letters")
        }

        if (validWords.isNotEmpty() && !validWords.contains(guess)) {
            return state.copy(shake = true, message = "Not in word list")
        }

        val nextRow = state.currentRow + 1
        val updatedBoards = state.boards.map { board ->
            if (board.isSolved) {
                board
            } else {
                val evaluatedRow = gameEngine.evaluate(guess, board.targetWord)
                val isSolved = guess == board.targetWord
                board.copy(
                    guesses = board.guesses + guess,
                    rowStates = board.rowStates + listOf(evaluatedRow),
                    isSolved = isSolved,
                    solvedAtRow = if (isSolved) nextRow else null,
                )
            }
        }

        // Update multi-board keyboard statuses
        val updatedKeyBoardStates = state.keyBoardStates.toMutableMap()
        for (char in 'A'..'Z') {
            val currentList = state.keyBoardStates[char] ?: List(state.mode.boardCount) { TileState.EMPTY }
            val newList = currentList.toMutableList()

            for (b in updatedBoards.indices) {
                val board = updatedBoards[b]
                val lastGuessIndex = board.guesses.lastIndex
                if (lastGuessIndex >= 0) {
                    val lastGuess = board.guesses[lastGuessIndex]
                    val lastRowStates = board.rowStates[lastGuessIndex]
                    for (i in lastGuess.indices) {
                        if (lastGuess[i] == char) {
                            val newTileState = lastRowStates[i]
                            val existing = newList[b]
                            if (precedence(newTileState) > precedence(existing)) {
                                newList[b] = newTileState
                            }
                        }
                    }
                }
            }
            updatedKeyBoardStates[char] = newList
        }

        val allSolved = updatedBoards.all { it.isSolved }
        val ranOutOfAttempts = nextRow >= state.maxAttempts

        val newStatus = when {
            allSolved -> GameStatus.WON
            ranOutOfAttempts -> GameStatus.LOST
            else -> GameStatus.IN_PROGRESS
        }

        return state.copy(
            boards = updatedBoards,
            currentRow = nextRow,
            currentInput = "",
            status = newStatus,
            keyBoardStates = updatedKeyBoardStates,
            shake = false,
            message = if (allSolved) "Brilliant! All boards solved!" else if (ranOutOfAttempts) "Out of attempts!" else null,
            showConfetti = allSolved,
        )
    }

    private fun precedence(state: TileState): Int = when (state) {
        TileState.CORRECT -> 4
        TileState.MISPLACED -> 3
        TileState.ABSENT -> 2
        TileState.FILLED -> 1
        TileState.EMPTY -> 0
    }
}
