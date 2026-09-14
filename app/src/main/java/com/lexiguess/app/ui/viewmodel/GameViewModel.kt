package com.lexiguess.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lexiguess.app.data.db.GameRecord
import com.lexiguess.app.data.repository.GameRepository
import com.lexiguess.app.data.repository.WordRepository
import com.lexiguess.app.domain.GameEngine
import com.lexiguess.app.domain.model.GameState
import com.lexiguess.app.domain.model.GameState.Companion.MAX_ROWS
import com.lexiguess.app.domain.model.GameState.Companion.WORD_LENGTH
import com.lexiguess.app.domain.model.GameStatus
import com.lexiguess.app.domain.model.TileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val gameRepository: GameRepository,
    private val engine: GameEngine,
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            wordRepository.initialize()

            if (gameRepository.hasActiveGameForToday()) {
                restoreSession()
            } else {
                val target = wordRepository.dailyWord()
                gameRepository.startNewGame(target)
                _state.update { it.copy(targetWord = target) }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Public input handlers
    // -------------------------------------------------------------------------

    fun onKey(char: Char) {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS) return
        if (s.currentInput.length >= WORD_LENGTH) return
        val newInput = s.currentInput + char.uppercaseChar()
        _state.update { it.copy(currentInput = newInput, message = null) }
        updateCurrentRowLetters(newInput)
    }

    fun onBackspace() {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS || s.currentInput.isEmpty()) return
        val newInput = s.currentInput.dropLast(1)
        _state.update { it.copy(currentInput = newInput, message = null) }
        updateCurrentRowLetters(newInput)
    }

    fun onEnter() {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS) return
        if (s.currentInput.length != WORD_LENGTH) {
            showMessage("Not enough letters")
            triggerShake()
            return
        }
        if (!engine.isValidWord(s.currentInput)) {
            showMessage("Not in word list")
            triggerShake()
            return
        }
        submitGuess()
    }

    fun onHint() {
        val s = _state.value
        if (s.hintUsed || s.status != GameStatus.IN_PROGRESS) return

        val revealedCorrect = mutableSetOf<Int>()
        for (row in 0 until s.currentRow) {
            for (col in 0 until WORD_LENGTH) {
                if (s.board[row][col] == TileState.CORRECT) revealedCorrect.add(col)
            }
        }

        val hint = engine.computeHint(s.targetWord, revealedCorrect)
        if (hint != null) {
            val (col, char) = hint
            showMessage("Hint: position ${col + 1} is '$char'")
            if (!s.isPracticeMode) {
                viewModelScope.launch { gameRepository.saveHintUsed() }
            }
            _state.update { it.copy(hintUsed = true) }
        } else {
            showMessage("No hint available")
        }
    }

    /**
     * Starts a new round.
     * @param practice If true, picks a fresh random target word for unlimited gameplay.
     */
    fun playAgain(practice: Boolean = true) {
        viewModelScope.launch {
            val nextWord = if (practice) wordRepository.randomWord() else wordRepository.dailyWord()
            _state.update {
                GameState(
                    targetWord = nextWord,
                    isPracticeMode = practice,
                )
            }
        }
    }

    fun dismissGameOverSheet() {
        _state.update { it.copy(showGameOverSheet = false) }
    }

    fun showGameOverSheet() {
        _state.update { it.copy(showGameOverSheet = true) }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }

    // -------------------------------------------------------------------------
    // Internal evaluation
    // -------------------------------------------------------------------------

    private fun submitGuess() {
        val s = _state.value
        val guess = s.currentInput.uppercase()
        val results = engine.evaluate(guess, s.targetWord)
        val row = s.currentRow

        val newBoard = s.board.toMutableList().map { it.toMutableList() }
        results.forEachIndexed { col, tileState -> newBoard[row][col] = tileState }

        val newKeyStates = s.keyStates.toMutableMap()
        results.forEachIndexed { col, tileState ->
            val key = guess[col]
            val current = newKeyStates[key] ?: TileState.EMPTY
            if (tileState.priority() > current.priority()) newKeyStates[key] = tileState
        }

        val won = results.all { it == TileState.CORRECT }
        val nextRow = row + 1
        val lost = !won && nextRow >= MAX_ROWS

        val newStatus = when {
            won -> GameStatus.WON
            lost -> GameStatus.LOST
            else -> GameStatus.IN_PROGRESS
        }

        val message = when (newStatus) {
            GameStatus.WON -> wonMessage(nextRow)
            GameStatus.LOST -> "The word was ${s.targetWord}"
            else -> null
        }

        _state.update {
            it.copy(
                board = newBoard,
                boardLetters = buildBoardLetters(it.boardLetters, row, guess),
                keyStates = newKeyStates,
                currentRow = nextRow,
                currentInput = "",
                status = newStatus,
                showConfetti = won,
                message = message,
            )
        }

        // When game completes, fetch definition and present end-game dialog after tiles flip
        if (newStatus != GameStatus.IN_PROGRESS) {
            viewModelScope.launch {
                // Fetch definition in background
                val def = wordRepository.fetchDefinition(s.targetWord)
                _state.update { it.copy(definition = def) }

                // Wait for the tile flip animation to complete before showing summary sheet
                delay(1_700)
                _state.update { it.copy(showGameOverSheet = true) }
            }

            if (won) {
                viewModelScope.launch {
                    delay(3_600)
                    _state.update { it.copy(showConfetti = false) }
                }
            }

            // Persist to Room if in daily mode
            if (!s.isPracticeMode) {
                viewModelScope.launch {
                    gameRepository.appendGuess(guess)
                    gameRepository.finalizeGame(
                        GameRecord(
                            datePlayed = LocalDate.now().toString(),
                            targetWord = s.targetWord,
                            won = won,
                            attempts = nextRow,
                            guesses = buildStoredGuesses(s, guess),
                        )
                    )
                }
            }
        } else if (!s.isPracticeMode) {
            viewModelScope.launch {
                gameRepository.appendGuess(guess)
            }
        }
    }

    private fun updateCurrentRowLetters(input: String) {
        val s = _state.value
        val newBoard = s.boardLetters.toMutableList().map { it.toMutableList() }
        val row = newBoard[s.currentRow]
        for (i in 0 until WORD_LENGTH) {
            row[i] = if (i < input.length) input[i] else ' '
        }
        val newTiles = s.board.toMutableList().map { it.toMutableList() }
        for (i in 0 until WORD_LENGTH) {
            newTiles[s.currentRow][i] = if (i < input.length) TileState.FILLED else TileState.EMPTY
        }
        _state.update {
            it.copy(
                boardLetters = newBoard,
                board = newTiles,
            )
        }
    }

    private fun buildBoardLetters(
        existing: List<List<Char>>,
        row: Int,
        guess: String,
    ): List<List<Char>> {
        val updated = existing.toMutableList().map { it.toMutableList() }
        for (i in 0 until WORD_LENGTH) updated[row][i] = guess[i]
        return updated
    }

    private fun buildStoredGuesses(state: GameState, lastGuess: String): String {
        val previous = (0 until state.currentRow).map { row ->
            state.boardLetters[row].joinToString("")
        }
        return (previous + lastGuess).joinToString(",")
    }

    private suspend fun restoreSession() {
        val target = gameRepository.savedTargetWord() ?: wordRepository.dailyWord()
        val savedGuesses = gameRepository.savedGuesses()
        val hintUsed = gameRepository.savedHintUsed()

        var tempState = GameState(targetWord = target, hintUsed = hintUsed)
        for (guess in savedGuesses) {
            if (guess.isBlank()) continue
            val results = engine.evaluate(guess, target)
            val row = tempState.currentRow
            val newBoard = tempState.board.toMutableList().map { it.toMutableList() }
            results.forEachIndexed { col, ts -> newBoard[row][col] = ts }

            val newKeyStates = tempState.keyStates.toMutableMap()
            results.forEachIndexed { col, ts ->
                val key = guess[col]
                val cur = newKeyStates[key] ?: TileState.EMPTY
                if (ts.priority() > cur.priority()) newKeyStates[key] = ts
            }

            val won = results.all { it == TileState.CORRECT }
            val nextRow = row + 1
            val lost = !won && nextRow >= MAX_ROWS
            tempState = tempState.copy(
                board = newBoard,
                boardLetters = buildBoardLetters(tempState.boardLetters, row, guess),
                keyStates = newKeyStates,
                currentRow = nextRow,
                status = when {
                    won -> GameStatus.WON
                    lost -> GameStatus.LOST
                    else -> GameStatus.IN_PROGRESS
                },
            )
        }
        _state.value = tempState
    }

    private fun showMessage(msg: String) {
        _state.update { it.copy(message = msg) }
        viewModelScope.launch {
            delay(2_500)
            _state.update { if (it.message == msg) it.copy(message = null) else it }
        }
    }

    private fun triggerShake() {
        _state.update { it.copy(shake = true) }
        viewModelScope.launch {
            delay(400)
            _state.update { it.copy(shake = false) }
        }
    }

    private fun wonMessage(attempts: Int): String = when (attempts) {
        1 -> "Genius! 🎉"
        2 -> "Magnificent! 🌟"
        3 -> "Impressive! 👏"
        4 -> "Splendid! 👍"
        5 -> "Great! 😊"
        else -> "Phew! 😅"
    }
}

/** Priority used to keep the best-known state for each keyboard key. */
private fun TileState.priority(): Int = when (this) {
    TileState.CORRECT -> 3
    TileState.MISPLACED -> 2
    TileState.ABSENT -> 1
    else -> 0
}
