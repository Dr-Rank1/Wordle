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
        val result = engine.evaluate("stare", "bring") // no letter overlap expected
        // s not in bring, t not in bring, a not in bring, r in bring -> MISPLACED, e not in bring
        // Actually let's verify individually
        val r = engine.evaluate("bunch", "stare")
        // b,u,n,c,h – none in stare
        assertEquals(TileState.ABSENT, r[0])
        assertEquals(TileState.ABSENT, r[1])
        assertEquals(TileState.ABSENT, r[2])
        assertEquals(TileState.ABSENT, r[3])
        assertEquals(TileState.ABSENT, r[4])
    }

    @Test
    fun `evaluate handles duplicate letters correctly`() {
        // target = "speed", guess = "seedy"
        // s(0) correct, e(1) misplaced (one e left), e(2) misplaced? -> only 2 e's in target
        // Actually we test with words we know
        val localEngine = GameEngine(mutableListOf("abbey", "speed", "spree"))
        // guess "speed" vs target "spree" (s, p, r, e, e)
        // s(0) CORRECT, p(1) CORRECT, e(2) MISPLACED (e at 4), e(3) CORRECT, d(4) ABSENT
        val result = localEngine.evaluate("speed", "spree")
        assertEquals(TileState.CORRECT, result[0])
        assertEquals(TileState.CORRECT, result[1])
        assertEquals(TileState.MISPLACED, result[2])
        assertEquals(TileState.CORRECT, result[3])
        assertEquals(TileState.ABSENT, result[4])
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
        assertEquals('C', hint!!.second) // position 0
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
        // crane was already there
        assertTrue(engine.isValidWord("crane"))
    }
}
