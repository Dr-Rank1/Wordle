package com.rank.lexi.ui.composable

import com.rank.lexi.domain.model.TileState
import org.junit.Assert.*
import org.junit.Test

class RpgGradeCalculatorTest {

    @Test
    fun `calculateRank assigns proper grades based on attempts`() {
        assertEquals("S+", RpgGradeCalculator.calculateRank(won = true, attempts = 1).rank)
        assertEquals("S", RpgGradeCalculator.calculateRank(won = true, attempts = 2).rank)
        assertEquals("A", RpgGradeCalculator.calculateRank(won = true, attempts = 3).rank)
        assertEquals("B", RpgGradeCalculator.calculateRank(won = true, attempts = 4).rank)
        assertEquals("C", RpgGradeCalculator.calculateRank(won = true, attempts = 5).rank)
        assertEquals("D", RpgGradeCalculator.calculateRank(won = true, attempts = 6).rank)
        assertEquals("F", RpgGradeCalculator.calculateRank(won = false, attempts = 6).rank)
    }

    @Test
    fun `calculateSkill handles pruning steps`() {
        val step1 = GuessStepAnalysis(
            roundNumber = 1,
            guessWord = "SLATE",
            rowStates = listOf(TileState.ABSENT, TileState.ABSENT, TileState.CORRECT, TileState.ABSENT, TileState.ABSENT),
            remainingCandidates = 80,
            previousCandidates = 2000,
        )
        val step2 = GuessStepAnalysis(
            roundNumber = 2,
            guessWord = "CRANE",
            rowStates = listOf(TileState.CORRECT, TileState.CORRECT, TileState.CORRECT, TileState.CORRECT, TileState.CORRECT),
            remainingCandidates = 1,
            previousCandidates = 80,
        )

        val skill = RpgGradeCalculator.calculateSkill(listOf(step1, step2), won = true)
        assertTrue(skill in 80..99)
    }

    @Test
    fun `calculateLuck scales with early correct letter discovery`() {
        val luckyStep = GuessStepAnalysis(
            roundNumber = 1,
            guessWord = "AUDIO",
            rowStates = listOf(TileState.CORRECT, TileState.CORRECT, TileState.MISPLACED, TileState.ABSENT, TileState.ABSENT),
            remainingCandidates = 10,
            previousCandidates = 2000,
        )
        val luckScore = RpgGradeCalculator.calculateLuck(listOf(luckyStep), attempts = 2, won = true)
        assertTrue(luckScore >= 80)
    }
}
