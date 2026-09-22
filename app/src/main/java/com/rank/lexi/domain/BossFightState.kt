package com.rank.lexi.domain

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
    val bannedLetters: Set<Char> = emptySet(),
)

object BossRegistry {
    private val bosses = mapOf(
        5 to BossConfig(
            levelNumber = 5,
            name = "The Novice Guard",
            title = "First Challenge",
            maxHp = 50,
            maxGuesses = 5,
            wordLength = 4,
            timeLimitSeconds = null,
            modifierDescription = "Only 5 guesses permitted",
        ),
        10 to BossConfig(
            levelNumber = 10,
            name = "The Gatekeeper",
            title = "Warden of the First Realm",
            maxHp = 100,
            maxGuesses = 4,
            wordLength = 4,
            timeLimitSeconds = null,
            modifierDescription = "Only 4 guesses permitted to breach the gate",
        ),
        15 to BossConfig(
            levelNumber = 15,
            name = "The Apprentice",
            title = "Master of Words",
            maxHp = 100,
            maxGuesses = 6,
            wordLength = 5,
            timeLimitSeconds = null,
            modifierDescription = "Standard 5-letter challenge, but strikes hard",
        ),
        20 to BossConfig(
            levelNumber = 20,
            name = "The Swift Wraith",
            title = "Time's Shadow",
            maxHp = 100,
            maxGuesses = 6,
            wordLength = 5,
            timeLimitSeconds = 90,
            modifierDescription = "90-second time trial to defeat the wraith",
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
        30 to BossConfig(
            levelNumber = 30,
            name = "The Iron Knight",
            title = "Hard Mode Enforcer",
            maxHp = 150,
            maxGuesses = 6,
            wordLength = 5,
            timeLimitSeconds = null,
            modifierDescription = "Strict Hard Mode enforced",
            enforceHardMode = true,
        ),
        35 to BossConfig(
            levelNumber = 35,
            name = "The Silent Monk",
            title = "Vow of Silence",
            maxHp = 150,
            maxGuesses = 6,
            wordLength = 6,
            timeLimitSeconds = null,
            modifierDescription = "No 'S' or 'E' allowed",
            bannedLetters = setOf('S', 'E'),
        ),
        40 to BossConfig(
            levelNumber = 40,
            name = "The Chronomancer",
            title = "Master of Fractured Time",
            maxHp = 200,
            maxGuesses = 6,
            wordLength = 6,
            timeLimitSeconds = 60,
            modifierDescription = "60-second time trial; 'X' and 'Z' forbidden",
            bannedLetters = setOf('X', 'Z'),
        ),
        45 to BossConfig(
            levelNumber = 45,
            name = "The Archmage",
            title = "Master of Elements",
            maxHp = 200,
            maxGuesses = 5,
            wordLength = 7,
            timeLimitSeconds = null,
            modifierDescription = "Only 5 guesses for a 7-letter word",
        ),
        50 to BossConfig(
            levelNumber = 50,
            name = "The Lexicon Titan",
            title = "Overlord of Vocabulary",
            maxHp = 250,
            maxGuesses = 6,
            wordLength = 7,
            timeLimitSeconds = null,
            modifierDescription = "7-letter word with strict Hard Mode enforced",
            enforceHardMode = true,
        ),
    )

    fun getBossForLevel(levelNumber: Int): BossConfig? = bosses[levelNumber]

    fun isBossLevel(levelNumber: Int): Boolean = bosses.containsKey(levelNumber)

    fun validateGuessForBoss(guess: String, config: BossConfig): String? {
        if (config.bannedLetters.isNotEmpty()) {
            val upper = guess.uppercase()
            val usedBanned = config.bannedLetters.filter { it in upper }
            if (usedBanned.isNotEmpty()) {
                return "Hazard: Letter(s) ${usedBanned.joinToString(", ")} are forbidden!"
            }
        }
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
