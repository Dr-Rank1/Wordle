package com.rank.lexi.domain

/**
 * Canonical campaign word lists. World 1 = 4 letters (levels 1-10),
 * World 2 = 5 letters (11-30), World 3 = 6 letters (31-40),
 * World 4 = 7 letters (41-50). Boss word lengths must match these worlds.
 */
object CampaignSeeds {
    val world1FourLetters = listOf(
        "BIRD", "COLD", "FIRE", "GOLD", "LION", "MOON", "RAIN", "STAR", "WIND", "TREE",
    )
    val world2FiveLetters = listOf(
        "APPLE", "BEACH", "CHAIR", "DREAM", "EARTH", "FLAME", "GRAPE", "HEART", "IMAGE", "JUICE",
        "KNIFE", "LEMON", "MAGIC", "NIGHT", "OCEAN", "PIZZA", "QUEEN", "RIVER", "SUGAR", "TIGER",
    )
    val world3SixLetters = listOf(
        "BRIDGE", "CASTLE", "DRAGON", "FOREST", "GALAXY", "ISLAND", "JUNGLE", "KNIGHT", "MONKEY", "PLANET",
    )
    val world4SevenLetters = listOf(
        "CHARIOT", "DIAMOND", "FANTASY", "HARMONY", "JOURNEY", "KINGDOM", "MYSTERY", "PHOENIX", "RAINBOW", "VICTORY",
    )

    data class SeededLevel(val levelNumber: Int, val wordLength: Int, val targetWord: String)

    fun allLevels(): List<SeededLevel> {
        val levels = mutableListOf<SeededLevel>()
        var n = 1
        world1FourLetters.forEach { levels.add(SeededLevel(n++, 4, it)) }
        world2FiveLetters.forEach { levels.add(SeededLevel(n++, 5, it)) }
        world3SixLetters.forEach { levels.add(SeededLevel(n++, 6, it)) }
        world4SevenLetters.forEach { levels.add(SeededLevel(n++, 7, it)) }
        return levels
    }

    fun expectedWordLength(levelNumber: Int): Int = when (levelNumber) {
        in 1..10 -> 4
        in 11..30 -> 5
        in 31..40 -> 6
        else -> 7
    }
}
