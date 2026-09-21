package com.rank.lexi.domain.model

enum class MultiBoardMode(val boardCount: Int, val maxAttempts: Int, val title: String) {
    DORDLE(2, 7, "Two boards"),
    QUORDLE(4, 9, "Four boards"),
}

data class SingleBoardState(
    val boardIndex: Int,
    val targetWord: String,
    val guesses: List<String> = emptyList(),
    val rowStates: List<List<TileState>> = emptyList(),
    val isSolved: Boolean = false,
    val solvedAtRow: Int? = null,
)

data class MultiBoardState(
    val mode: MultiBoardMode = MultiBoardMode.DORDLE,
    val wordLength: Int = 5,
    val boards: List<SingleBoardState> = emptyList(),
    val currentInput: String = "",
    val currentRow: Int = 0,
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val keyBoardStates: Map<Char, List<TileState>> = ('A'..'Z').associateWith {
        List(mode.boardCount) { TileState.EMPTY }
    },
    val shake: Boolean = false,
    val message: String? = null,
    val showConfetti: Boolean = false,
    val bonusAttempts: Int = 0,
) {
    val maxAttempts: Int get() = mode.maxAttempts + bonusAttempts
    val solvedCount: Int get() = boards.count { it.isSolved }
}
