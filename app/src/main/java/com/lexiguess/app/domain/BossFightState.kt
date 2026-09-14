package com.lexiguess.app.domain

data class BossConfig(
    val levelNumber: Int,
    val name: String,
    val title: String,
    val maxHp: Int,
    val maxGuesses: Int,
    val wordLength: Int,
    val timeLimitSeconds: Int?,
    val modifierDescription: String,
    val requireMinDistinctVowels: Int = 0,
    val enforceHardMode: Boolean = false,
)

object BossRegistry {
    private val bosses = mapOf(
        10 to BossConfig(
            levelNumber = 10,
            name = "The Gatekeeper",
            title = "Warden of the First Realm",
            maxHp = 100,
            maxGuesses = 4,
            wordLength = 5,
            timeLimitSeconds = null,
            modifierDescription = "Only 4 guesses permitted to breach the gate",
        ),
        25 to BossConfig(
            levelNumber = 25,
            name = "The Sphinx",
            title = "Guardian of Ancient Riddles",
            maxHp = 150,
            maxGuesses = 6,
            wordLength = 5,
            timeLimitSeconds = null,
            modifierDescription = "Every guess must contain at least 2 distinct vowels",
            requireMinDistinctVowels = 2,
        ),
        40 to BossConfig(
            levelNumber = 40,
            name = "The Chronomancer",
            title = "Master of Fractured Time",
            maxHp = 200,
            maxGuesses = 6,
            wordLength = 5,
            timeLimitSeconds = 60,
            modifierDescription = "60-second time trial to solve before time shatters",
        ),
        50 to BossConfig(
            levelNumber = 50,
            name = "The Lexicon Titan",
            title = "Overlord of Vocabulary",
            maxHp = 250,
            maxGuesses = 6,
            wordLength = 6,
            timeLimitSeconds = null,
            modifierDescription = "6-letter word with strict Hard Mode enforced",
            enforceHardMode = true,
        ),
    )

    fun getBossForLevel(levelNumber: Int): BossConfig? = bosses[levelNumber]

    fun isBossLevel(levelNumber: Int): Boolean = bosses.containsKey(levelNumber)

    fun validateGuessForBoss(guess: String, config: BossConfig): String? {
        if (config.requireMinDistinctVowels > 0) {
            val vowels = setOf('A', 'E', 'I', 'O', 'U')
            val distinctVowelsCount = guess.uppercase().filter { it in vowels }.toSet().size
            if (distinctVowelsCount < config.requireMinDistinctVowels) {
                return "Sphinx Curse: Guess must have at least ${config.requireMinDistinctVowels} distinct vowels!"
            }
        }
        return null
    }
}
