package com.rank.lexi.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CampaignSeedsTest {

    @Test
    fun `all seeded campaign words match their world lengths`() {
        val levels = CampaignSeeds.allLevels()
        assertEquals(50, levels.size)
        levels.forEach { seed ->
            assertEquals(
                "Level ${seed.levelNumber} word ${seed.targetWord}",
                seed.wordLength,
                seed.targetWord.length,
            )
            assertEquals(CampaignSeeds.expectedWordLength(seed.levelNumber), seed.wordLength)
        }
    }

    @Test
    fun `world 4 no longer seeds eight-letter CHAMPION`() {
        val level41 = CampaignSeeds.allLevels().first { it.levelNumber == 41 }
        assertEquals(7, level41.targetWord.length)
        assertTrue(level41.targetWord != "CHAMPION")
    }
}
