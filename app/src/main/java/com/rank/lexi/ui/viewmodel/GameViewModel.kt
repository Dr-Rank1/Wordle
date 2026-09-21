package com.rank.lexi.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.db.GameRecord
import com.rank.lexi.data.db.LevelDao
import com.rank.lexi.data.db.LevelRecord
import com.rank.lexi.data.db.VaultDao
import com.rank.lexi.data.db.VaultWordRecord
import com.rank.lexi.data.repository.DictionaryHelper
import com.rank.lexi.data.repository.GameRepository
import com.rank.lexi.data.repository.PlayerPreferences
import com.rank.lexi.data.repository.QuestRepository
import com.rank.lexi.data.repository.WordRepository
import com.rank.lexi.domain.BossRegistry
import com.rank.lexi.domain.GameEngine
import com.rank.lexi.domain.model.GameMode
import com.rank.lexi.domain.model.GameState
import com.rank.lexi.domain.model.GameState.Companion.MAX_ROWS
import com.rank.lexi.domain.model.GameStatus
import com.rank.lexi.domain.model.TileState
import com.rank.lexi.ui.audio.SoundManager
import com.rank.lexi.ui.composable.GuessStepAnalysis
import dagger.hilt.android.lifecycle.HiltViewModel
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
    savedStateHandle: SavedStateHandle,
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

    private val launchMode = savedStateHandle.get<String>("mode") ?: GameMode.DAILY.name
    private val launchLength = savedStateHandle.get<String>("length")?.toIntOrNull()
    private val launchLevel = savedStateHandle.get<String>("level")?.toIntOrNull()
    private val launchWord = savedStateHandle.get<String>("word").orEmpty()
    private val launchAttempts = savedStateHandle.get<String>("attempts")?.toIntOrNull()
    private val launchEpoch = savedStateHandle.get<String>("epoch")?.toLongOrNull()

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _guessAnalysisSteps = MutableStateFlow<List<GuessStepAnalysis>>(emptyList())
    val guessAnalysisSteps: StateFlow<List<GuessStepAnalysis>> = _guessAnalysisSteps.asStateFlow()

    private var rushTimerJob: Job? = null
    private var bossTimerJob: Job? = null
    private var roundStartTimeMs: Long = System.currentTimeMillis()
    private var persistDaily: Boolean = false

    init {
        viewModelScope.launch {
            wordRepository.initialize()
            gameRepository.checkAndApplyStreakShield(playerPreferences)
            soundManager.isEnabled = playerPreferences.soundEnabledFlow.first()
            when (launchMode) {
                GameMode.TIMED_RUSH.name -> startTimedRush()
                GameMode.LEVEL.name -> startCampaignLevel(launchLevel ?: 1)
                GameMode.CUSTOM.name -> startCustomChallenge(launchWord, launchAttempts ?: 6)
                GameMode.PRACTICE.name -> {
                    if (launchWord.isNotBlank()) startPracticeWithTarget(launchWord)
                    else startPracticeGame(launchLength ?: 5)
                }
                else -> startDailyGame(launchEpoch ?: LocalDate.now().toEpochDay())
            }
        }
    }

    fun startDailyGame(epochDay: Long = LocalDate.now().toEpochDay()) {
        cancelRushTimer()
        cancelBossTimer()
        _guessAnalysisSteps.value = emptyList()
        viewModelScope.launch {
            val isToday = epochDay == LocalDate.now().toEpochDay()
            persistDaily = isToday
            if (isToday && gameRepository.hasSessionForToday()) {
                restoreSession()
                return@launch
            }
            val target = wordRepository.dailyWord(5, epochDay)
            if (isToday) {
                gameRepository.startNewGame(target)
            }
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
        cancelBossTimer()
        persistDaily = false
        _guessAnalysisSteps.value = emptyList()
        val target = wordRepository.randomWord(length)
        roundStartTimeMs = System.currentTimeMillis()
        viewModelScope.launch {
            _state.value = createEmptyGameState(
                mode = GameMode.PRACTICE,
                length = length,
                target = target,
            ).copy(hardMode = playerPreferences.hardModeFlow.first())
        }
    }

    fun startPracticeWithTarget(word: String) {
        cancelRushTimer()
        cancelBossTimer()
        persistDaily = false
        _guessAnalysisSteps.value = emptyList()
        val clean = word.trim().uppercase()
        roundStartTimeMs = System.currentTimeMillis()
        viewModelScope.launch {
            _state.value = createEmptyGameState(
                mode = GameMode.PRACTICE,
                length = clean.length,
                target = clean,
            ).copy(hardMode = playerPreferences.hardModeFlow.first())
        }
    }

    fun startCustomChallenge(word: String, maxAttempts: Int = 6) {
        cancelRushTimer()
        cancelBossTimer()
        persistDaily = false
        _guessAnalysisSteps.value = emptyList()
        val clean = word.trim().uppercase()
        roundStartTimeMs = System.currentTimeMillis()
        val attempts = if (maxAttempts in 3..10) maxAttempts else 6
        viewModelScope.launch {
            _state.value = createEmptyGameState(
                mode = GameMode.CUSTOM,
                length = clean.length.coerceIn(4, 7),
                target = clean,
                maxAttempts = attempts,
            ).copy(hardMode = playerPreferences.hardModeFlow.first())
        }
    }

    fun startCampaignLevel(levelNumber: Int) {
        cancelRushTimer()
        cancelBossTimer()
        persistDaily = false
        _guessAnalysisSteps.value = emptyList()
        viewModelScope.launch {
            val level = levelDao.getLevel(levelNumber)
            val boss = BossRegistry.getBossForLevel(levelNumber)
            val target = (level?.targetWord ?: wordRepository.randomWord(level?.wordLength ?: 5))
                .trim()
                .uppercase()
            val length = target.length
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
                bossTimeRemainingSeconds = boss?.timeLimitSeconds,
                hardMode = boss?.enforceHardMode ?: playerPreferences.hardModeFlow.first(),
            )
            val limit = boss?.timeLimitSeconds
            if (limit != null) {
                launchBossTimer(limit)
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
                _state.update { it.copy(bossTimeRemainingSeconds = remaining) }
                if (remaining <= 0) {
                    val snapshot = _state.value
                    _state.update {
                        it.copy(
                            status = GameStatus.LOST,
                            message = "Time's up! The boss defeated you.",
                            showGameOverSheet = true,
                        )
                    }
                    handleGameEndRewards(snapshot, won = false, attempts = snapshot.currentRow, durationSeconds = seconds.toLong())
                    break
                }
            }
        }
    }

    fun startTimedRush() {
        cancelRushTimer()
        cancelBossTimer()
        persistDaily = false
        _guessAnalysisSteps.value = emptyList()
        val length = 5
        val target = wordRepository.randomWord(length)
        roundStartTimeMs = System.currentTimeMillis()

        viewModelScope.launch {
            _state.value = createEmptyGameState(
                mode = GameMode.TIMED_RUSH,
                length = length,
                target = target,
            ).copy(
                isRushActive = true,
                rushTimeRemainingSeconds = 120,
                rushWordsSolved = 0,
                rushScore = 0,
                hardMode = playerPreferences.hardModeFlow.first(),
            )
            launchRushTimer()
        }
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

    fun onKey(char: Char) {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS || s.isRevealing) return
        if (s.currentInput.length >= s.wordLength) return

        soundManager.playKeyClick()
        val newInput = s.currentInput + char.uppercaseChar()
        _state.update { it.withCurrentInput(newInput).copy(message = null) }
    }

    fun onBackspace() {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS || s.isRevealing || s.currentInput.isEmpty()) return

        soundManager.playKeyClick()
        val newInput = s.currentInput.dropLast(1)
        _state.update { it.withCurrentInput(newInput).copy(message = null) }
    }

    fun onEnter() {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS || s.isRevealing) return
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
            if (persistDaily) {
                viewModelScope.launch { gameRepository.saveHintUsed() }
            }
        } else {
            showMessage("No hint available")
        }
    }

    fun playAgain() {
        val s = _state.value
        when (s.gameMode) {
            GameMode.DAILY, GameMode.CUSTOM -> startPracticeGame(s.wordLength)
            GameMode.TIMED_RUSH -> startTimedRush()
            GameMode.LEVEL -> {
                val nextLevel = (s.campaignLevel ?: 1) + 1
                if (nextLevel <= 50) startCampaignLevel(nextLevel) else startPracticeGame(5)
            }
            GameMode.PRACTICE -> startPracticeGame(s.wordLength)
            GameMode.DUEL -> startPracticeGame(5)
        }
    }

    fun canRestartFromToolbar(): Boolean = _state.value.gameMode != GameMode.DAILY

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

    private fun submitGuess() {
        val s = _state.value
        val guess = s.currentInput.uppercase()
        if (guess.length != s.targetWord.length) {
            showMessage("Not enough letters")
            return
        }
        val results = engine.evaluate(guess, s.targetWord)
        val row = s.currentRow
        val len = s.wordLength

        val newBoard = s.board.toMutableList().map { it.toMutableList() }.toMutableList()
        while (newBoard.size <= row) {
            newBoard.add(MutableList(len) { TileState.EMPTY })
        }
        val targetRow = newBoard[row]
        while (targetRow.size < len) targetRow.add(TileState.EMPTY)
        results.forEachIndexed { col, tileState ->
            if (col < targetRow.size) targetRow[col] = tileState
        }

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

        val previousEvals = (0..row).map { r ->
            Pair(
                if (r == row) guess else s.boardLetters.getOrNull(r)?.joinToString("") ?: "",
                if (r == row) results else s.board.getOrNull(r) ?: emptyList(),
            )
        }
        val remaining = if (won) 1 else engine.countRemainingCandidates(previousEvals, len)

        val prevCandidates = if (_guessAnalysisSteps.value.isEmpty()) {
            val targetCount = wordRepository.getTargetWordsCount(len)
            if (targetCount > 0) targetCount else wordRepository.getValidWordsSet(len).size
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

        // Advance board letters and row immediately so tile flip begins,
        // but lock input and keep keys unchanged until flipped
        _state.update {
            it.copy(
                board = newBoard,
                boardLetters = buildBoardLetters(it.boardLetters, row, guess, len),
                currentRow = nextRow,
                currentInput = "",
                isRevealing = true,
                remainingCandidates = remaining,
            )
        }

        if (persistDaily && s.gameMode == GameMode.DAILY) {
            viewModelScope.launch { gameRepository.appendGuess(guess) }
        }

        viewModelScope.launch {
            // Synchronize keyboard key color reveal with tile flips (staggered delay)
            for (col in 0 until len) {
                delay(250L)
                val key = guess[col]
                val ts = results[col]
                _state.update { current ->
                    val updatedKeys = current.keyStates.toMutableMap()
                    val cur = updatedKeys[key] ?: TileState.EMPTY
                    if (ts.priority() > cur.priority()) {
                        updatedKeys[key] = ts
                        current.copy(keyStates = updatedKeys)
                    } else current
                }
            }

            // Wait for the final tile flip animation to settle
            delay(380L)

            // Reveal sequence is complete - now update status, message, and trigger victory!
            _state.update {
                it.copy(
                    isRevealing = false,
                    status = newStatus,
                    message = message,
                    bossCurrentHp = newBossHp,
                )
            }

            if (newStatus != GameStatus.IN_PROGRESS) {
                cancelBossTimer()
            }

            if (won) {
                soundManager.playVictory()
                _state.update { it.copy(showConfetti = true, winningRow = row) }
            }

            if (newStatus != GameStatus.IN_PROGRESS) {
                val durationMs = System.currentTimeMillis() - roundStartTimeMs
                val solveDurationSeconds = durationMs / 1000
                _state.update { it.copy(solveDurationMs = durationMs) }

                val def = wordRepository.fetchDefinition(s.targetWord)
                _state.update { it.copy(definition = def) }

                handleGameEndRewards(s, won, nextRow, solveDurationSeconds)

                if (s.gameMode == GameMode.TIMED_RUSH && won) {
                    delay(1_400)
                    advanceTimedRushWord(nextRow)
                    return@launch
                }

                delay(if (won) 1_800 else 1_400)
                _state.update { it.copy(showGameOverSheet = true) }
            }

            if (won) {
                delay(3_600)
                _state.update { it.copy(showConfetti = false, winningRow = null) }
            }
        }
    }

    private fun advanceTimedRushWord(attempts: Int = 4) {
        val s = _state.value
        val nextWord = wordRepository.randomWord(5)
        val newSolved = s.rushWordsSolved + 1
        val timeBounty = when {
            attempts <= 3 -> 15
            attempts == 4 -> 10
            else -> 5
        }
        val comboMultiplier = (1.0f + (newSolved * 0.15f)).coerceAtMost(3.0f)
        val baseScore = 200 + (s.rushTimeRemainingSeconds * 2)
        val scoreBonus = (baseScore * comboMultiplier).toInt()
        val newScore = s.rushScore + scoreBonus
        val newTime = (s.rushTimeRemainingSeconds + timeBounty).coerceAtMost(180)

        viewModelScope.launch {
            _state.value = createEmptyGameState(
                mode = GameMode.TIMED_RUSH,
                length = 5,
                target = nextWord,
            ).copy(
                isRushActive = true,
                rushTimeRemainingSeconds = newTime,
                rushWordsSolved = newSolved,
                rushScore = newScore,
                message = "+${timeBounty}s! (x${"%.1f".format(java.util.Locale.US, comboMultiplier)}) +$scoreBonus",
                hardMode = playerPreferences.hardModeFlow.first(),
            )
            playerPreferences.updateRushHighScore(newScore)
            syncAchievementProgress("RUSH_3", newSolved)
            syncAchievementProgress("RUSH_6", newSolved)
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

            incrementAchievement("FIRST_WIN")
            if (attempts == 1) incrementAchievement("GENIUS_1")
            if (attempts <= 2) incrementAchievement("FLAWLESS_SWEEP")
            if (attempts == 6) incrementAchievement("CLUTCH_6")
            if (durationSeconds < 45) incrementAchievement("SPEED_DEMON")
            if (s.hardMode) incrementAchievement("HARD_MODE_WIN")
            if (s.targetWord.any { it in "QXZ" }) incrementAchievement("RARE_HUNTER")

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

            try {
                questRepository.onPuzzleSolved(
                    wordLength = s.wordLength,
                    attempts = attempts,
                    mode = s.gameMode.name,
                    solveDurationSeconds = durationSeconds,
                )
            } catch (_: Exception) {}

            try {
                val shields = playerPreferences.streakShieldDates()
                val currentStreak = gameRepository.currentStreak(shields)
                if (currentStreak > 0 && currentStreak % 7 == 0) {
                    playerPreferences.addStreakFreeze(1)
                }
            } catch (_: Exception) {}

            if (s.gameMode == GameMode.LEVEL && s.campaignLevel != null) {
                val stars = when (attempts) {
                    1, 2 -> 3
                    3, 4 -> 2
                    else -> 1
                }
                _state.update { it.copy(starsEarned = stars) }
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
                val completed = levelDao.getCompletedCountOnce()
                syncAchievementProgress("LEVEL_10", completed)
                syncAchievementProgress("LEVEL_30", completed)
                syncAchievementProgress("LEVEL_50", completed)
            }
        }

        recordFinishedGame(s, won, attempts)

        if (won) {
            try {
                val totalWins = gameRepository.totalWins()
                syncAchievementProgress("WIN_5", totalWins)
                syncAchievementProgress("WIN_25", totalWins)

                val shields = playerPreferences.streakShieldDates()
                val currentStreak = gameRepository.currentStreak(shields)
                syncAchievementProgress("STREAK_3", currentStreak)
                syncAchievementProgress("STREAK_7", currentStreak)
                syncAchievementProgress("STREAK_30", currentStreak)
            } catch (_: Exception) {}
        }
    }

    private suspend fun recordFinishedGame(s: GameState, won: Boolean, attempts: Int) {
        val lastGuess = if (s.currentInput.isNotBlank()) s.currentInput else s.targetWord
        val record = GameRecord(
            datePlayed = LocalDate.now().toString(),
            targetWord = s.targetWord,
            won = won,
            attempts = attempts,
            guesses = buildStoredGuesses(s, lastGuess),
            mode = s.gameMode.name,
        )
        when {
            s.gameMode == GameMode.DAILY && persistDaily -> gameRepository.finalizeGame(record)
            else -> gameRepository.insertHistory(record)
        }
    }

    private suspend fun incrementAchievement(id: String) {
        val existing = achievementDao.getAchievement(id) ?: return
        if (existing.unlocked) return
        syncAchievementProgress(id, existing.currentProgress + 1)
    }

    private suspend fun syncAchievementProgress(id: String, progress: Int) {
        val existing = achievementDao.getAchievement(id) ?: return
        if (existing.unlocked && existing.currentProgress >= existing.targetProgress) return
        val clamped = progress.coerceAtLeast(existing.currentProgress)
        val unlocked = clamped >= existing.targetProgress
        if (unlocked && !existing.unlocked) {
            achievementDao.upsertAchievement(
                existing.copy(
                    unlocked = true,
                    currentProgress = existing.targetProgress,
                    unlockedAt = LocalDate.now().toString(),
                )
            )
            playerPreferences.addXp(150)
        } else if (clamped != existing.currentProgress) {
            achievementDao.upsertAchievement(existing.copy(currentProgress = clamped.coerceAtMost(existing.targetProgress)))
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

    private fun buildBoardLetters(
        existing: List<List<Char>>,
        row: Int,
        guess: String,
        length: Int,
    ): List<List<Char>> {
        val updated = existing.toMutableList().map { it.toMutableList() }.toMutableList()
        while (updated.size <= row) {
            updated.add(MutableList(length) { ' ' })
        }
        val targetRow = updated[row].toMutableList()
        while (targetRow.size < length) {
            targetRow.add(' ')
        }
        for (i in 0 until minOf(length, guess.length)) {
            if (i < targetRow.size) {
                targetRow[i] = guess[i]
            }
        }
        updated[row] = targetRow
        return updated
    }

    private fun buildStoredGuesses(state: GameState, lastGuess: String): String {
        val previous = (0 until state.currentRow).map { row ->
            state.boardLetters.getOrNull(row)?.joinToString("") ?: ""
        }
        val last = lastGuess.uppercase()
        return if (previous.lastOrNull() == last) previous.joinToString(",")
        else (previous + last).joinToString(",")
    }

    private suspend fun restoreSession() {
        persistDaily = true
        val target = gameRepository.savedTargetWord() ?: wordRepository.dailyWord(5)
        val savedGuesses = gameRepository.savedGuesses()
        val hintUsed = gameRepository.savedHintUsed()

        var tempState = createEmptyGameState(GameMode.DAILY, 5, target).copy(
            hintUsed = hintUsed,
            hardMode = playerPreferences.hardModeFlow.first(),
        )
        for (guess in savedGuesses) {
            if (tempState.currentRow >= tempState.maxAttempts) break
            if (guess.isBlank() || guess.length != 5) continue
            val results = engine.evaluate(guess, target)
            val row = tempState.currentRow
            val newBoard = tempState.board.toMutableList().map { it.toMutableList() }.toMutableList()
            while (newBoard.size <= row) {
                newBoard.add(MutableList(5) { TileState.EMPTY })
            }
            results.forEachIndexed { col, ts ->
                if (col < newBoard[row].size) {
                    newBoard[row][col] = ts
                }
            }

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
                },
            )
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
