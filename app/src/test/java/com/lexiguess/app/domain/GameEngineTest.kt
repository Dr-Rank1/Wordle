package com.lexiguess.app.domain

import com.lexiguess.app.domain.model.TileState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        engine = GameEngine(
            wordList = mutableListOf("crane", "slate", "audio", "raise", "stare", "light")
        )
    }

    @Test
    fun `isValidWord returns true for word in list`() {
        assertTrue(engine.isValidWord("crane"))
        assertTrue(engine.isValidWord("CRANE"))
    }

    @Test
    fun `isValidWord returns false for word not in list`() {
        assertFalse(engine.isValidWord("zzzzz"))
    }

    @Test
    fun `isValidWord returns false for wrong length`() {
        assertFalse(engine.isValidWord("cat"))
    }

    @Test
    fun `evaluate returns all CORRECT for exact match`() {
        val result = engine.evaluate("crane", "crane")
        assertEquals(List(5) { TileState.CORRECT }, result)
    }

    @Test
    fun `evaluate returns all ABSENT for no matching letters`() {
        val r = engine.evaluate("bunch", "stare")
        assertEquals(TileState.ABSENT, r[0])
        assertEquals(TileState.ABSENT, r[1])
        assertEquals(TileState.ABSENT, r[2])
        assertEquals(TileState.ABSENT, r[3])
        assertEquals(TileState.ABSENT, r[4])
    }

    @Test
    fun `evaluate handles duplicate letters correctly`() {
        val localEngine = GameEngine(mutableListOf("abbey", "speed", "spree"))
        val result = localEngine.evaluate("speed", "spree")
        assertEquals(TileState.CORRECT, result[0])
        assertEquals(TileState.CORRECT, result[1])
        assertEquals(TileState.MISPLACED, result[2])
        assertEquals(TileState.CORRECT, result[3])
        assertEquals(TileState.ABSENT, result[4])
    }

    @Test
    fun `evaluate supports 4-letter words`() {
        val r = engine.evaluate("bird", "bard")
        assertEquals(TileState.CORRECT, r[0])
        assertEquals(TileState.ABSENT, r[1])
        assertEquals(TileState.CORRECT, r[2])
        assertEquals(TileState.CORRECT, r[3])
    }

    @Test
    fun `evaluate supports 6-letter words`() {
        val r = engine.evaluate("castle", "cattle")
        assertEquals(TileState.CORRECT, r[0])
        assertEquals(TileState.CORRECT, r[1])
        assertEquals(TileState.ABSENT, r[2]) // 's' absent in cattle
        assertEquals(TileState.CORRECT, r[3])
        assertEquals(TileState.CORRECT, r[4])
        assertEquals(TileState.CORRECT, r[5])
    }

    @Test
    fun `validateHardMode catches missing green letter`() {
        // First guess: "crane" vs target "clash" -> 'C' at index 0 is CORRECT
        val prev = listOf(
            Pair("CRANE", listOf(TileState.CORRECT, TileState.ABSENT, TileState.ABSENT, TileState.ABSENT, TileState.ABSENT))
        )
        // Guess starting with 'P' violates hard mode
        val err = engine.validateHardMode("PLANE", prev)
        assertNotNull(err)
        assertTrue(err!!.contains("1st letter must be C"))
    }

    @Test
    fun `validateHardMode catches missing yellow letter`() {
        // First guess: "crane" vs target "beach" -> 'A' at index 2 is MISPLACED
        val prev = listOf(
            Pair("CRANE", listOf(TileState.ABSENT, TileState.ABSENT, TileState.MISPLACED, TileState.ABSENT, TileState.ABSENT))
        )
        // Guess missing 'A' violates hard mode
        val err = engine.validateHardMode("PILOT", prev)
        assertNotNull(err)
        assertTrue(err!!.contains("Guess must contain A"))
    }

    @Test
    fun `validateHardMode catches insufficient repeated letters`() {
        // Target has 2 'E's, guess revealed 2 'E's (1 CORRECT, 1 MISPLACED)
        val prev = listOf(
            Pair("ENTER", listOf(TileState.CORRECT, TileState.ABSENT, TileState.ABSENT, TileState.MISPLACED, TileState.ABSENT))
        )
        // Guess with only 1 'E' violates hard mode (starts with E, but only 1 E)
        val err = engine.validateHardMode("ELBOW", prev)
        assertNotNull(err)
        assertTrue(err!!.contains("Guess must contain at least 2 'E's"))
    }

    @Test
    fun `selectDailyWord returns consistent result for same epoch day`() {
        val w1 = engine.selectDailyWord(1000L)
        val w2 = engine.selectDailyWord(1000L)
        assertEquals(w1, w2)
    }

    @Test
    fun `computeHint returns a position not yet correctly found`() {
        val hint = engine.computeHint("crane", revealedCorrect = emptySet())
        assertNotNull(hint)
        assertEquals('C', hint!!.second)
    }

    @Test
    fun `computeHint skips already revealed positions`() {
        val hint = engine.computeHint("crane", revealedCorrect = setOf(0))
        assertNotNull(hint)
        assertEquals(1, hint!!.first)
        assertEquals('R', hint.second)
    }

    @Test
    fun `mergeWords adds new words and deduplicates`() {
        val before = engine.isValidWord("alpha")
        assertFalse(before)
        engine.mergeWords(listOf("alpha", "crane", "bravo"))
        assertTrue(engine.isValidWord("alpha"))
        assertTrue(engine.isValidWord("bravo"))
        assertTrue(engine.isValidWord("crane"))
    }
}
