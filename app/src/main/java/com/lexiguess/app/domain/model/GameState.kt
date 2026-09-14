package com.lexiguess.app.domain.model

/**
 * Immutable snapshot of the entire game state at any point in time.
 *
 * @param targetWord        The secret five-letter word for this session.
 * @param board             Six rows of five [TileState] values representing the grid.
 * @param boardLetters      Six rows of five characters entered by the player (blank = ' ').
 * @param keyStates         The best-known [TileState] for each letter of the alphabet.
 * @param currentRow        Zero-based index of the row currently being typed.
 * @param currentInput      Letters typed so far in the current row (max 5).
 * @param status            Whether the game is in progress, won, or lost.
 * @param hintUsed          Whether the player has already used their one hint.
 * @param shake             True for one frame when an invalid submission is attempted.
 * @param showConfetti      True while the win confetti should be displayed.
 * @param isPracticeMode    True if playing in unlimited practice mode instead of daily mode.
 * @param definition        Dictionary definition of the target word.
 * @param showGameOverSheet True when the end-game summary sheet should be visible.
 * @param message           A transient one-shot toast message (null = no message).
 */
data class GameState(
    val targetWord: String = "",
    val board: List<List<TileState>> = List(MAX_ROWS) { List(WORD_LENGTH) { TileState.EMPTY } },
    val boardLetters: List<List<Char>> = List(MAX_ROWS) { List(WORD_LENGTH) { ' ' } },
    val keyStates: Map<Char, TileState> = ('A'..'Z').associateWith { TileState.EMPTY },
    val currentRow: Int = 0,
    val currentInput: String = "",
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val hintUsed: Boolean = false,
    val shake: Boolean = false,
    val showConfetti: Boolean = false,
    val isPracticeMode: Boolean = false,
    val definition: String? = null,
    val showGameOverSheet: Boolean = false,
    val message: String? = null,
) {
    companion object {
        const val MAX_ROWS = 6
        const val WORD_LENGTH = 5
    }
}
