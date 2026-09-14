package com.lexiguess.app.domain

import com.lexiguess.app.domain.model.GameStatus
import com.lexiguess.app.domain.model.MultiBoardMode
import com.lexiguess.app.domain.model.TileState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MultiBoardEngineTest {

    private lateinit var gameEngine: GameEngine
    private lateinit var multiEngine: MultiBoardEngine

    @Before
    fun setup() {
        gameEngine = GameEngine(
            wordList = mutableListOf("CRANE", "SLOTH", "ABOUT", "REACT", "TRACE")
        )
        multiEngine = MultiBoardEngine(gameEngine)
    }

    @Test
    fun `dordle initializes with 2 boards and 7 attempts`() {
        val state = multiEngine.startNewGame(
            mode = MultiBoardMode.DORDLE,
            targetWords = listOf("CRANE", "SLOTH"),
        )

        assertEquals(2, state.boards.size)
        assertEquals(7, state.maxAttempts)
        assertEquals(0, state.currentRow)
        assertEquals(GameStatus.IN_PROGRESS, state.status)
        assertFalse(state.boards[0].isSolved)
        assertFalse(state.boards[1].isSolved)
    }

    @Test
    fun `dordle solving first board freezes first board while second continues`() {
        var state = multiEngine.startNewGame(
            mode = MultiBoardMode.DORDLE,
            targetWords = listOf("CRANE", "SLOTH"),
        )

        // Guess CRANE
        "CRANE".forEach { state = multiEngine.onLetterInput(state, it) }
        state = multiEngine.submitGuess(state, setOf("CRANE", "SLOTH", "ABOUT"))

        assertEquals(1, state.currentRow)
        assertTrue(state.boards[0].isSolved)
        assertFalse(state.boards[1].isSolved)
        assertEquals(GameStatus.IN_PROGRESS, state.status)

        // Guess SLOTH
        "SLOTH".forEach { state = multiEngine.onLetterInput(state, it) }
        state = multiEngine.submitGuess(state, setOf("CRANE", "SLOTH", "ABOUT"))

        assertEquals(2, state.currentRow)
        assertTrue(state.boards[0].isSolved)
        assertTrue(state.boards[1].isSolved)
        assertEquals(GameStatus.WON, state.status)
        assertTrue(state.showConfetti)
        // Board 0 should still only have 1 guess because it was solved on attempt 1
        assertEquals(1, state.boards[0].guesses.size)
        // Board 1 took 2 guesses
        assertEquals(2, state.boards[1].guesses.size)
    }

    @Test
    fun `keyboard states track each board independently`() {
        var state = multiEngine.startNewGame(
            mode = MultiBoardMode.DORDLE,
            targetWords = listOf("CRANE", "SLOTH"),
        )

        "CRANE".forEach { state = multiEngine.onLetterInput(state, it) }
        state = multiEngine.submitGuess(state, setOf("CRANE", "SLOTH"))

        // Letter 'C' is in CRANE (board 0: CORRECT), not in SLOTH (board 1: ABSENT)
        val cStates = state.keyBoardStates['C']
        assertNotNull(cStates)
        assertEquals(TileState.CORRECT, cStates!![0])
        assertEquals(TileState.ABSENT, cStates[1])
    }
}
