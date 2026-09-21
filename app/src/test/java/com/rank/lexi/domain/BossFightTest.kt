package com.rank.lexi.domain

import org.junit.Assert.*
import org.junit.Test

class BossFightTest {

    @Test
    fun `milestone levels 10, 25, 40, and 50 are identified as boss levels`() {
        assertTrue(BossRegistry.isBossLevel(10))
        assertTrue(BossRegistry.isBossLevel(25))
        assertTrue(BossRegistry.isBossLevel(40))
        assertTrue(BossRegistry.isBossLevel(50))

        assertFalse(BossRegistry.isBossLevel(1))
        assertFalse(BossRegistry.isBossLevel(9))
        assertFalse(BossRegistry.isBossLevel(11))
    }

    @Test
    fun `Level 10 The Gatekeeper restricts guesses to 4`() {
        val boss = BossRegistry.getBossForLevel(10)
        assertNotNull(boss)
        assertEquals("The Gatekeeper", boss!!.name)
        assertEquals(4, boss.maxGuesses)
        assertEquals(4, boss.wordLength)
    }

    @Test
    fun `Level 25 The Sphinx enforces minimum distinct vowels`() {
        val boss = BossRegistry.getBossForLevel(25)
        assertNotNull(boss)
        assertEquals("The Sphinx", boss!!.name)
        assertEquals(2, boss.requireMinDistinctVowels)

        // "CRANE" has 'A' and 'E' (2 vowels) -> Valid
        assertNull(BossRegistry.validateGuessForBoss("CRANE", boss))

        // "AUDIO" has 'A', 'U', 'I', 'O' (4 vowels) -> Valid
        assertNull(BossRegistry.validateGuessForBoss("AUDIO", boss))

        // "STERN" has only 'E' (1 vowel) -> Invalid
        val err1 = BossRegistry.validateGuessForBoss("STERN", boss)
        assertNotNull(err1)
        assertTrue(err1!!.contains("2 distinct vowels"))

        // "MYTHS" has 0 vowels -> Invalid
        val err2 = BossRegistry.validateGuessForBoss("MYTHS", boss)
        assertNotNull(err2)
    }

    @Test
    fun `Level 40 The Chronomancer has 60 second timer`() {
        val boss = BossRegistry.getBossForLevel(40)
        assertNotNull(boss)
        assertEquals("The Chronomancer", boss!!.name)
        assertEquals(60, boss.timeLimitSeconds)
    }

    @Test
    fun `Level 50 The Lexicon Titan enforces 7 letters and Hard Mode`() {
        val boss = BossRegistry.getBossForLevel(50)
        assertNotNull(boss)
        assertEquals("The Lexicon Titan", boss!!.name)
        assertEquals(7, boss.wordLength)
        assertTrue(boss.enforceHardMode)
    }

    @Test
    fun `boss word lengths match campaign worlds`() {
        assertEquals(CampaignSeeds.expectedWordLength(10), BossRegistry.getBossForLevel(10)!!.wordLength)
        assertEquals(CampaignSeeds.expectedWordLength(25), BossRegistry.getBossForLevel(25)!!.wordLength)
        assertEquals(CampaignSeeds.expectedWordLength(40), BossRegistry.getBossForLevel(40)!!.wordLength)
        assertEquals(CampaignSeeds.expectedWordLength(50), BossRegistry.getBossForLevel(50)!!.wordLength)
    }

    @Test
    fun `Level 40 The Chronomancer enforces banned letters hazard`() {
        val boss = BossRegistry.getBossForLevel(40)
        assertNotNull(boss)
        assertTrue(boss!!.bannedLetters.contains('X'))
        assertTrue(boss.bannedLetters.contains('Z'))

        // "ZEBRAS" contains 'Z' -> Invalid
        val err1 = BossRegistry.validateGuessForBoss("ZEBRAS", boss)
        assertNotNull(err1)
        assertTrue(err1!!.contains("forbidden"))

        // "PLANET" does not contain banned letters -> Valid
        val err2 = BossRegistry.validateGuessForBoss("PLANET", boss)
        assertNull(err2)
    }
}
