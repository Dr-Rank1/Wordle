package com.lexiguess.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lexiguess.app.data.db.AchievementDao
import com.lexiguess.app.data.db.GameRecord
import com.lexiguess.app.data.db.LevelDao
import com.lexiguess.app.data.db.LevelRecord
import com.lexiguess.app.data.db.VaultDao
import com.lexiguess.app.data.db.VaultWordRecord
import com.lexiguess.app.data.repository.DictionaryHelper
import com.lexiguess.app.data.repository.GameRepository
import com.lexiguess.app.data.repository.PlayerPreferences
import com.lexiguess.app.data.repository.QuestRepository
import com.lexiguess.app.data.repository.WordRepository
import com.lexiguess.app.domain.GameEngine
import com.lexiguess.app.domain.model.GameMode
import com.lexiguess.app.domain.model.GameState
import com.lexiguess.app.domain.model.GameState.Companion.MAX_ROWS
import com.lexiguess.app.domain.model.GameStatus
import com.lexiguess.app.domain.model.TileState
import com.lexiguess.app.ui.audio.SoundManager
import com.lexiguess.app.ui.composable.GuessStepAnalysis
import dagger.hilt.android.lifecycle.HiltViewModel
import com.lexiguess.app.domain.BossRegistry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val gameRepository: GameRepository,
    val playerPreferences: PlayerPreferences,
    private val levelDao: LevelDao,
    private val achievementDao: AchievementDao,
    private val vaultDao: VaultDao,
    private val questRepository: QuestRepository,
    val soundManager: SoundManager,
    private val engine: GameEngine,
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _guessAnalysisSteps = MutableStateFlow<List<GuessStepAnalysis>>(emptyList())
    val guessAnalysisSteps: StateFlow<List<GuessStepAnalysis>> = _guessAnalysisSteps.asStateFlow()

    private var rushTimerJob: Job? = null
    private var bossTimerJob: Job? = null
    private var roundStartTimeMs: Long = System.currentTimeMillis()

    init {
        viewModelScope.launch {
            wordRepository.initialize()
            val soundPref = playerPreferences.soundEnabledFlow.first()
            soundManager.isEnabled = soundPref
            val hardModePref = playerPreferences.hardModeFlow.first()
            _state.update { it.copy(hardMode = hardModePref) }

            if (gameRepository.hasActiveGameForToday()) {
                restoreSession()
            } else {
                startDailyGame()
            }
        }
    }

    // -------------------------------------------------------------------------
    // Mode Launchers
    // -------------------------------------------------------------------------

    fun startDailyGame() {
        cancelRushTimer()
        _guessAnalysisSteps.value = emptyList()
        viewModelScope.launch {
            val target = wordRepository.dailyWord(5)
            gameRepository.startNewGame(target)
            roundStartTimeMs = System.currentTimeMillis()
            _state.value = GameState(
                gameMode = GameMode.DAILY,
                wordLength = 5,
                targetWord = target,
                hardMode = playerPreferences.hardModeFlow.first(),
            )
        }
    }

    fun startPracticeGame(length: Int = 5) {
        cancelRushTimer()
        _guessAnalysisSteps.value = emptyList()
        val target = wordRepository.randomWord(length)
        roundStartTimeMs = System.currentTimeMillis()
        _state.value = createEmptyGameState(
            mode = GameMode.PRACTICE,
            length = length,
            target = target,
        )
    }

    fun startPracticeWithTarget(word: String) {
        cancelRushTimer()
        _guessAnalysisSteps.value = emptyList()
        val clean = word.trim().uppercase()
        roundStartTimeMs = System.currentTimeMillis()
        _state.value = createEmptyGameState(
            mode = GameMode.PRACTICE,
            length = clean.length,
            target = clean,
        )
    }

    fun startCustomChallenge(word: String, maxAttempts: Int = 6) {
        cancelRushTimer()
        _guessAnalysisSteps.value = emptyList()
        val clean = word.trim().uppercase()
        roundStartTimeMs = System.currentTimeMillis()
        val attempts = if (maxAttempts in 3..10) maxAttempts else 6
        _state.value = createEmptyGameState(
            mode = GameMode.PRACTICE,
            length = clean.length,
            target = clean,
            maxAttempts = attempts,
        )
    }

    fun startCampaignLevel(levelNumber: Int) {
        cancelRushTimer()
        cancelBossTimer()
        _guessAnalysisSteps.value = emptyList()
        viewModelScope.launch {
            val level = levelDao.getLevel(levelNumber)
            val boss = BossRegistry.getBossForLevel(levelNumber)
            val length = boss?.wordLength ?: level?.wordLength ?: 5
            val target = level?.targetWord ?: wordRepository.randomWord(length)
            val maxAttempts = boss?.maxGuesses ?: 6
            roundStartTimeMs = System.currentTimeMillis()
            _state.value = createEmptyGameState(
                mode = GameMode.LEVEL,
                length = length,
                target = target,
                maxAttempts = maxAttempts,
            ).copy(
                campaignLevel = levelNumber,
                isBossFight = boss != null,
                bossName = boss?.name ?: "",
                bossTitle = boss?.title ?: "",
                bossMaxHp = boss?.maxHp ?: 100,
                bossCurrentHp = boss?.maxHp ?: 100,
                bossModifierDescription = boss?.modifierDescription ?: "",
                bossTimeLimitSeconds = boss?.timeLimitSeconds,
                hardMode = boss?.enforceHardMode ?: playerPreferences.hardModeFlow.first(),
            )
            if (boss?.timeLimitSeconds != null) {
                launchBossTimer(boss.timeLimitSeconds)
            }
        }
    }

    private fun cancelBossTimer() {
        bossTimerJob?.cancel()
        bossTimerJob = null
    }

    private fun launchBossTimer(seconds: Int) {
        cancelBossTimer()
        bossTimerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _state.update { it.copy(bossTimeLimitSeconds = remaining) }
                if (remaining <= 0) {
                    _state.update {
                        it.copy(
                            status = GameStatus.LOST,
                            message = "Time's up! The boss defeated you.",
                            showGameOverSheet = true,
                        )
                    }
                    break
                }
            }
        }
    }

    fun startTimedRush() {
        cancelRushTimer()
        _guessAnalysisSteps.value = emptyList()
        val length = 5
        val target = wordRepository.randomWord(length)
        roundStartTimeMs = System.currentTimeMillis()

        _state.value = createEmptyGameState(
            mode = GameMode.TIMED_RUSH,
            length = length,
            target = target,
        ).copy(
            isRushActive = true,
            rushTimeRemainingSeconds = 120,
            rushWordsSolved = 0,
            rushScore = 0,
        )

        launchRushTimer()
    }

    fun startDuel(secretWord: String) {
        cancelRushTimer()
        val clean = secretWord.trim().uppercase()
        roundStartTimeMs = System.currentTimeMillis()
        _state.value = createEmptyGameState(
            mode = GameMode.DUEL,
            length = clean.length,
            target = clean,
        ).copy(isDuelMode = true)
    }

    private fun launchRushTimer() {
        rushTimerJob?.cancel()
        rushTimerJob = viewModelScope.launch {
            while (_state.value.rushTimeRemainingSeconds > 0 && _state.value.isRushActive) {
                delay(1000)
                _state.update {
                    val remaining = it.rushTimeRemainingSeconds - 1
                    if (remaining <= 0) {
                        it.copy(
                            rushTimeRemainingSeconds = 0,
                            isRushActive = false,
                            status = GameStatus.LOST,
                            showGameOverSheet = true,
                            message = "Time's up!",
                        )
                    } else {
                        it.copy(rushTimeRemainingSeconds = remaining)
                    }
                }
            }
        }
    }

    private fun cancelRushTimer() {
        rushTimerJob?.cancel()
        rushTimerJob = null
    }

    // -------------------------------------------------------------------------
    // Player Input Handlers
    // -------------------------------------------------------------------------

    fun onKey(char: Char) {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS) return
        if (s.currentInput.length >= s.wordLength) return

        soundManager.playKeyClick()
        val newInput = s.currentInput + char.uppercaseChar()
        _state.update { it.copy(currentInput = newInput, message = null) }
        updateCurrentRowLetters(newInput)
    }

    fun onBackspace() {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS || s.currentInput.isEmpty()) return

        soundManager.playKeyClick()
        val newInput = s.currentInput.dropLast(1)
        _state.update { it.copy(currentInput = newInput, message = null) }
        updateCurrentRowLetters(newInput)
    }

    fun onEnter() {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS) return
        if (s.currentInput.length != s.wordLength) {
            showMessage("Not enough letters")
            soundManager.playError()
            triggerShake()
            return
        }
        if (!engine.isValidWord(s.currentInput, s.wordLength)) {
            showMessage("Not in word list")
            soundManager.playError()
            triggerShake()
            return
        }

        // Hard mode rule check
        if (s.hardMode && s.currentRow > 0) {
            val previousEvals = (0 until s.currentRow).map { r ->
                Pair(
                    s.boardLetters[r].joinToString(""),
                    s.board[r],
                )
            }
            val hardModeError = engine.validateHardMode(s.currentInput, previousEvals)
            if (hardModeError != null) {
                showMessage(hardModeError)
                soundManager.playError()
                triggerShake()
                return
            }
        }

        // Boss Battle modifier validation
        if (s.isBossFight && s.campaignLevel != null) {
            val boss = BossRegistry.getBossForLevel(s.campaignLevel)
            if (boss != null) {
                val bossError = BossRegistry.validateGuessForBoss(s.currentInput, boss)
                if (bossError != null) {
                    showMessage(bossError)
                    soundManager.playError()
                    triggerShake()
                    return
                }
            }
        }

        submitGuess()
    }

    fun onHint() {
        val s = _state.value
        if (s.hintUsed || s.status != GameStatus.IN_PROGRESS) return

        val revealedCorrect = mutableSetOf<Int>()
        for (row in 0 until s.currentRow) {
            for (col in 0 until s.wordLength) {
                if (s.board[row][col] == TileState.CORRECT) revealedCorrect.add(col)
            }
        }

        val hint = engine.computeHint(s.targetWord, revealedCorrect)
        if (hint != null) {
            val (col, char) = hint
            showMessage("Hint: Position ${col + 1} is '$char'")
            _state.update { it.copy(hintUsed = true) }
        } else {
            showMessage("No hint available")
        }
    }

    fun playAgain() {
        val s = _state.value
        when (s.gameMode) {
            GameMode.DAILY -> startPracticeGame(s.wordLength)
            GameMode.TIMED_RUSH -> startTimedRush()
            GameMode.LEVEL -> {
                val nextLevel = (s.campaignLevel ?: 1) + 1
                if (nextLevel <= 50) startCampaignLevel(nextLevel) else startPracticeGame(5)
            }
            GameMode.PRACTICE -> startPracticeGame(s.wordLength)
            GameMode.DUEL -> startPracticeGame(5)
        }
    }

    fun dismissGameOverSheet() {
        _state.update { it.copy(showGameOverSheet = false) }
    }

    fun showGameOverSheet() {
        _state.update { it.copy(showGameOverSheet = true) }
    }

    fun showAnalysis() {
        _state.update { it.copy(showAnalysisDialog = true) }
    }

    fun dismissAnalysis() {
        _state.update { it.copy(showAnalysisDialog = false) }
    }

    fun showScorecard() {
        _state.update { it.copy(showScorecardDialog = true) }
    }

    fun dismissScorecard() {
        _state.update { it.copy(showScorecardDialog = false) }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }

    // -------------------------------------------------------------------------
    // Internal Evaluation
    // -------------------------------------------------------------------------

    private fun submitGuess() {
        val s = _state.value
        val guess = s.currentInput.uppercase()
        val results = engine.evaluate(guess, s.targetWord)
        val row = s.currentRow
        val len = s.wordLength

        val newBoard = s.board.toMutableList().map { it.toMutableList() }
        results.forEachIndexed { col, tileState -> newBoard[row][col] = tileState }

        val newKeyStates = s.keyStates.toMutableMap()
        results.forEachIndexed { col, tileState ->
            val key = guess[col]
            val current = newKeyStates[key] ?: TileState.EMPTY
            if (tileState.priority() > current.priority()) newKeyStates[key] = tileState
        }

        val isBoss = s.isBossFight && s.campaignLevel != null
        val maxGuessesAllowed = s.maxAttempts

        val won = results.all { it == TileState.CORRECT }
        val nextRow = row + 1
        val lost = !won && nextRow >= maxGuessesAllowed

        val newStatus = when {
            won -> GameStatus.WON
            lost -> GameStatus.LOST
            else -> GameStatus.IN_PROGRESS
        }

        val newBossHp = if (isBoss) {
            if (won) 0
            else {
                val hpChunk = s.bossMaxHp / maxGuessesAllowed
                (s.bossMaxHp - (nextRow * hpChunk)).coerceAtLeast(1)
            }
        } else s.bossCurrentHp

        // Remaining candidates metric
        val previousEvals = (0..row).map { r ->
            Pair(
                if (r == row) guess else s.boardLetters[r].joinToString(""),
                if (r == row) results else s.board[r],
            )
        }
        val remaining = if (won) 1 else engine.countRemainingCandidates(previousEvals, len)

        val prevCandidates = if (_guessAnalysisSteps.value.isEmpty()) {
            wordRepository.getValidWordsSet(len).size
        } else {
            _guessAnalysisSteps.value.last().remainingCandidates
        }
        val stepAnalysis = GuessStepAnalysis(
            roundNumber = nextRow,
            guessWord = guess,
            rowStates = results,
            remainingCandidates = remaining,
            previousCandidates = prevCandidates,
        )
        _guessAnalysisSteps.update { it + stepAnalysis }

        val message = when (newStatus) {
            GameStatus.WON -> wonMessage(nextRow)
            GameStatus.LOST -> "The word was ${s.targetWord}"
            else -> null
        }

        _state.update {
            it.copy(
                board = newBoard,
                boardLetters = buildBoardLetters(it.boardLetters, row, guess, len),
                keyStates = newKeyStates,
                currentRow = nextRow,
                currentInput = "",
                status = newStatus,
                showConfetti = won,
                remainingCandidates = remaining,
                message = message,
                bossCurrentHp = newBossHp,
            )
        }

        if (newStatus != GameStatus.IN_PROGRESS) {
            cancelBossTimer()
        }

        if (won) {
            soundManager.playVictory()
        }

        // Handle game completion
        if (newStatus != GameStatus.IN_PROGRESS) {
            val durationMs = System.currentTimeMillis() - roundStartTimeMs
            val solveDurationSeconds = durationMs / 1000
            _state.update { it.copy(solveDurationMs = durationMs) }

            viewModelScope.launch {
                // Fetch definition in background
                val def = wordRepository.fetchDefinition(s.targetWord)
                _state.update { it.copy(definition = def) }

                // Award XP and check achievements
                handleGameEndRewards(s, won, nextRow, solveDurationSeconds)

                // If Timed Rush and won, grant +15s bonus and load next word immediately!
                if (s.gameMode == GameMode.TIMED_RUSH && won) {
                    delay(1_400)
                    advanceTimedRushWord()
                    return@launch
                }

                delay(1_600)
                _state.update { it.copy(showGameOverSheet = true) }
            }

            if (won) {
                viewModelScope.launch {
                    delay(3_600)
                    _state.update { it.copy(showConfetti = false) }
                }
            }
        }
    }

    private fun advanceTimedRushWord() {
        val s = _state.value
        val nextWord = wordRepository.randomWord(5)
        val newSolved = s.rushWordsSolved + 1
        val scoreBonus = 200 + (s.rushTimeRemainingSeconds * 2)
        val newScore = s.rushScore + scoreBonus
        val newTime = (s.rushTimeRemainingSeconds + 15).coerceAtMost(180)

        _state.value = createEmptyGameState(
            mode = GameMode.TIMED_RUSH,
            length = 5,
            target = nextWord,
        ).copy(
            isRushActive = true,
            rushTimeRemainingSeconds = newTime,
            rushWordsSolved = newSolved,
            rushScore = newScore,
            message = "+15s! Score: $newScore",
        )

        viewModelScope.launch {
            playerPreferences.updateRushHighScore(newScore)
            if (newSolved >= 3) unlockAchievement("RUSH_3")
            if (newSolved >= 6) unlockAchievement("RUSH_6")
        }
    }

    private suspend fun handleGameEndRewards(
        s: GameState,
        won: Boolean,
        attempts: Int,
        durationSeconds: Long,
    ) {
        if (won) {
            val xpGain = (MAX_ROWS - attempts + 1) * 35 + 50
            playerPreferences.addXp(xpGain)

            unlockAchievement("FIRST_WIN")
            if (attempts == 1) unlockAchievement("GENIUS_1")
            if (attempts == 6) unlockAchievement("CLUTCH_6")
            if (durationSeconds < 45) unlockAchievement("SPEED_DEMON")
            if (s.hardMode) unlockAchievement("HARD_MODE_WIN")

            // Word Vault record
            try {
                val defInfo = DictionaryHelper.resolveDefinition(s.targetWord)
                val existing = vaultDao.getWord(s.targetWord)
                vaultDao.upsert(
                    VaultWordRecord(
                        word = s.targetWord,
                        length = s.wordLength,
                        definition = defInfo.definition,
                        partOfSpeech = defInfo.partOfSpeech,
                        example = defInfo.example,
                        timesSolved = (existing?.timesSolved ?: 0) + 1,
                        bestGuesses = minOf(existing?.bestGuesses ?: 6, attempts),
                        unlockedAt = existing?.unlockedAt ?: System.currentTimeMillis(),
                    ),
                )
            } catch (_: Exception) {}

            // Daily Quests progression
            try {
                questRepository.onPuzzleSolved(
                    wordLength = s.wordLength,
                    attempts = attempts,
                    mode = s.gameMode.name,
                    solveDurationSeconds = durationSeconds,
                )
            } catch (_: Exception) {}

            // Streak Freeze milestone award (1 per 7-day streak)
            try {
                val currentStreak = gameRepository.currentStreak()
                if (currentStreak > 0 && currentStreak % 7 == 0) {
                    playerPreferences.addStreakFreeze(1)
                }
            } catch (_: Exception) {}

            // Campaign level completion
            if (s.gameMode == GameMode.LEVEL && s.campaignLevel != null) {
                val stars = when (attempts) {
                    1, 2 -> 3
                    3, 4 -> 2
                    else -> 1
                }
                levelDao.upsertLevel(
                    LevelRecord(
                        levelNumber = s.campaignLevel,
                        wordLength = s.wordLength,
                        targetWord = s.targetWord,
                        stars = stars,
                        bestAttempts = attempts,
                        completed = true,
                    )
                )
                if (s.campaignLevel >= 10) unlockAchievement("LEVEL_10")
                if (s.campaignLevel >= 30) unlockAchievement("LEVEL_30")
                if (s.campaignLevel >= 50) unlockAchievement("LEVEL_50")
            }

            // Daily record
            if (s.gameMode == GameMode.DAILY) {
                gameRepository.finalizeGame(
                    GameRecord(
                        datePlayed = LocalDate.now().toString(),
                        targetWord = s.targetWord,
                        won = true,
                        attempts = attempts,
                        guesses = buildStoredGuesses(s, s.targetWord),
                    )
                )
            }

            // Milestone wins and streaks achievements
            try {
                val totalWins = gameRepository.totalWins()
                if (totalWins >= 5) unlockAchievement("WIN_5")
                if (totalWins >= 25) unlockAchievement("WIN_25")

                val currentStreak = gameRepository.currentStreak()
                if (currentStreak >= 3) unlockAchievement("STREAK_3")
                if (currentStreak >= 7) unlockAchievement("STREAK_7")
                if (currentStreak >= 30) unlockAchievement("STREAK_30")
            } catch (_: Exception) {}
        } else {
            // Daily record on loss
            if (s.gameMode == GameMode.DAILY) {
                gameRepository.finalizeGame(
                    GameRecord(
                        datePlayed = LocalDate.now().toString(),
                        targetWord = s.targetWord,
                        won = false,
                        attempts = 0,
                        guesses = buildStoredGuesses(s, s.currentInput),
                    )
                )
            }
        }
    }

    suspend fun unlockAchievement(id: String) {
        val existing = achievementDao.getAchievement(id) ?: return
        if (!existing.unlocked) {
            achievementDao.upsertAchievement(
                existing.copy(
                    unlocked = true,
                    currentProgress = existing.targetProgress,
                    unlockedAt = LocalDate.now().toString(),
                )
            )
            playerPreferences.addXp(150) // Badge bonus XP
        }
    }

    private fun createEmptyGameState(
        mode: GameMode,
        length: Int,
        target: String,
        maxAttempts: Int = MAX_ROWS,
    ): GameState {
        return GameState(
            gameMode = mode,
            wordLength = length,
            maxAttempts = maxAttempts,
            targetWord = target,
            board = List(maxAttempts) { List(length) { TileState.EMPTY } },
            boardLetters = List(maxAttempts) { List(length) { ' ' } },
            keyStates = ('A'..'Z').associateWith { TileState.EMPTY },
        )
    }

    private fun updateCurrentRowLetters(input: String) {
        val s = _state.value
        val len = s.wordLength
        val newBoard = s.boardLetters.toMutableList().map { it.toMutableList() }
        val row = newBoard[s.currentRow]
        for (i in 0 until len) {
            row[i] = if (i < input.length) input[i] else ' '
        }
        val newTiles = s.board.toMutableList().map { it.toMutableList() }
        for (i in 0 until len) {
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
        length: Int,
    ): List<List<Char>> {
        val updated = existing.toMutableList().map { it.toMutableList() }
        for (i in 0 until length) updated[row][i] = guess[i]
        return updated
    }

    private fun buildStoredGuesses(state: GameState, lastGuess: String): String {
        val previous = (0 until state.currentRow).map { row ->
            state.boardLetters[row].joinToString("")
        }
        return (previous + lastGuess).joinToString(",")
    }

    private suspend fun restoreSession() {
        val target = gameRepository.savedTargetWord() ?: wordRepository.dailyWord(5)
        val savedGuesses = gameRepository.savedGuesses()
        val hintUsed = gameRepository.savedHintUsed()

        var tempState = createEmptyGameState(GameMode.DAILY, 5, target).copy(hintUsed = hintUsed)
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
            val lost = !won && nextRow >= tempState.maxAttempts
            tempState = tempState.copy(
                board = newBoard,
                boardLetters = buildBoardLetters(tempState.boardLetters, row, guess, 5),
                keyStates = newKeyStates,
                currentRow = nextRow,
                status = when {
                    won -> GameStatus.WON
                    lost -> GameStatus.LOST
                    else -> GameStatus.IN_PROGRESS
        }
        if (tempState.status != GameStatus.IN_PROGRESS) {
            tempState = tempState.copy(showGameOverSheet = true)
            viewModelScope.launch {
                val def = wordRepository.fetchDefinition(target)
                _state.update { it.copy(definition = def) }
            }
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
        1 -> "Genius"
        2 -> "Magnificent"
        3 -> "Impressive"
        4 -> "Splendid"
        5 -> "Great"
        else -> "Phew"
    }

    override fun onCleared() {
        super.onCleared()
        cancelRushTimer()
        cancelBossTimer()
    }
}
