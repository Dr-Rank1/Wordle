package com.example.wordle

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Holds UI state for the Wordle screen.
 */
data class GameUiState(
    val grid: List<List<LetterState>>, // 6 rows × 5 columns
    val currentInput: String, // letters typed for the current row
    val keyboardDisabled: Set<Char>, // letters that are already guessed
    val attemptsLeft: Int,
    val gameOver: Boolean,
    val message: String?
)

/**
 * ViewModel that orchestrates the game using GameEngine.
 * It loads a word list from assets/words.txt and selects a daily word based on the date.
 */
class WordleViewModel(application: Application) : AndroidViewModel(application) {
    private val wordList: List<String>
    private val engine: GameEngine

    private val _uiState = MutableStateFlow(
        GameUiState(
            grid = List(6) { List(5) { LetterState.UNKNOWN } },
            currentInput = "",
            keyboardDisabled = emptySet(),
            attemptsLeft = 6,
            gameOver = false,
            message = null
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState

    init {
        // Load words from assets/words.txt
        wordList = loadWordList(application)
        // Deterministic daily word based on date (index into list)
        val today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        val index = (today.hashCode().let { if (it < 0) -it else it }) % wordList.size
        val targetProvider = { wordList[index] }
        engine = GameEngine(wordList, targetProvider)
    }

    private fun loadWordList(app: Application): List<String> {
        return try {
            val input = app.assets.open("words.txt")
            BufferedReader(InputStreamReader(input)).useLines { it.filter { line -> line.length == 5 }.map { it.lowercase() }.toList() }
        } catch (e: Exception) {
            // Fallback minimal list
            listOf("apple", "brand", "crane", "drive", "eagle")
        }
    }

    fun onKeyPress(key: Char) {
        if (_uiState.value.gameOver) return
        if (key == '\b') { // backspace
            val newInput = _uiState.value.currentInput.dropLast(1)
            _uiState.update { it.copy(currentInput = newInput) }
            return
        }
        if (key == '\n') { // enter
            submitCurrentGuess()
            return
        }
        // Only accept letters and if length < 5
        if (key.isLetter() && _uiState.value.currentInput.length < 5) {
            val newInput = _uiState.value.currentInput + key.uppercaseChar()
            _uiState.update { it.copy(currentInput = newInput) }
        }
    }

    private fun submitCurrentGuess() {
        val guess = _uiState.value.currentInput
        if (guess.length != 5) {
            _uiState.update { it.copy(message = "Word must be 5 letters" ) }
            return
        }
        if (!engine.isValidGuess(guess)) {
            _uiState.update { it.copy(message = "Not a valid word") }
            return
        }
        viewModelScope.launch {
            val result = engine.submitGuess(guess)
            // Update grid row with result states
            val rowIndex = engine.attempts.size - 1
            _uiState.update { state ->
                val newGrid = state.grid.toMutableList()
                val row = result.map { it.state }
                newGrid[rowIndex] = row
                val newDisabled = state.keyboardDisabled + guess.toSet()
                val attemptsLeft = 6 - engine.attempts.size
                val gameOver = engine.isGameOver()
                val msg = when {
                    engine.isGameWon() -> "You won!"
                    gameOver -> "Game over. Word was ${engine.getTargetWord()}"
                    else -> null
                }
                state.copy(
                    grid = newGrid,
                    currentInput = "",
                    keyboardDisabled = newDisabled,
                    attemptsLeft = attemptsLeft,
                    gameOver = gameOver,
                    message = msg
                )
            }
        }
    }
}
