package com.rank.lexi.domain.model

/**
 * Immutable snapshot of the entire game state across all supported game modes.
 */
data class GameState(
    val gameMode: GameMode = GameMode.DAILY,
    val wordLength: Int = 5,
    val maxAttempts: Int = MAX_ROWS,
    val targetWord: String = "",
    val board: List<List<TileState>> = List(MAX_ROWS) { List(5) { TileState.EMPTY } },
    val boardLetters: List<List<Char>> = List(MAX_ROWS) { List(5) { ' ' } },
    val keyStates: Map<Char, TileState> = ('A'..'Z').associateWith { TileState.EMPTY },
    val currentRow: Int = 0,
    val currentInput: String = "",
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val hintUsed: Boolean = false,
    val shake: Boolean = false,
    val showConfetti: Boolean = false,
    val definition: String? = null,
    val showGameOverSheet: Boolean = false,
    val showAnalysisDialog: Boolean = false,
    val showScorecardDialog: Boolean = false,
    val solveDurationMs: Long = 0L,
    val message: String? = null,
    val remainingCandidates: Int = 0,
    val hardMode: Boolean = false,

    // Timed Rush state
    val isRushActive: Boolean = false,
    val rushTimeRemainingSeconds: Int = 120,
    val rushWordsSolved: Int = 0,
    val rushScore: Int = 0,

    // Campaign Level state
    val campaignLevel: Int? = null,
    val starsEarned: Int = 0,

    // Boss Fight state
    val isBossFight: Boolean = false,
    val bossName: String = "",
    val bossTitle: String = "",
    val bossMaxHp: Int = 100,
    val bossCurrentHp: Int = 100,
    val bossModifierDescription: String = "",
    val bossTimeLimitSeconds: Int? = null,
    val bossTimeRemainingSeconds: Int? = null,

    // Pass & Play Duel state
    val isDuelMode: Boolean = false,
    val duelCurrentRound: Int = 1,
    val duelPlayerSettingWord: Int = 1,
    val duelPlayerGuessing: Int = 2,
    val duelWaitingForWordInput: Boolean = false,
    val duelPlayer1Attempts: Int = 0,
    val duelPlayer2Attempts: Int = 0,
) {
    val isPracticeMode: Boolean
        get() = gameMode == GameMode.PRACTICE || gameMode == GameMode.CUSTOM

    fun withCurrentInput(input: String): GameState {
        val newLetters = boardLetters.map { it.toMutableList() }
        val newTiles = board.map { it.toMutableList() }
        val row = currentRow
        if (row in newLetters.indices && row in newTiles.indices) {
            for (i in 0 until wordLength) {
                val filled = i < input.length
                newLetters[row][i] = if (filled) input[i] else ' '
                newTiles[row][i] = if (filled) TileState.FILLED else TileState.EMPTY
            }
        }
        return copy(
            currentInput = input,
            boardLetters = newLetters,
            board = newTiles,
        )
    }

    companion object {
        const val MAX_ROWS = 6
        const val DEFAULT_WORD_LENGTH = 5
    }
}
