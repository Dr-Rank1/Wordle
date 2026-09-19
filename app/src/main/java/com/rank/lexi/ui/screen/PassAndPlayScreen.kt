package com.rank.lexi.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.repository.WordRepository
import com.rank.lexi.domain.GameEngine
import com.rank.lexi.domain.model.GameState
import com.rank.lexi.domain.model.TileState
import com.rank.lexi.ui.composable.ConfettiParticleEngine
import com.rank.lexi.ui.composable.TileGrid
import com.rank.lexi.ui.composable.WordleKeyboard
import com.rank.lexi.ui.theme.TileCorrect
import com.rank.lexi.ui.theme.TileMisplaced
import kotlinx.coroutines.launch
import java.time.LocalDate

import com.rank.lexi.ui.audio.SoundManager
import com.rank.lexi.domain.model.GameStatus
import kotlinx.coroutines.delay
import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.foundation.focusable
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import com.rank.lexi.ui.util.ShareResult

enum class DuelPhase {
    P1_SET_WORD,
    HANDOFF_TO_P2,
    P2_SOLVING,
    P2_SET_WORD,
    HANDOFF_TO_P1,
    P1_SOLVING,
    MATCH_OVER,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassAndPlayScreen(
    wordRepository: WordRepository,
    engine: GameEngine,
    achievementDao: AchievementDao? = null,
    soundManager: SoundManager? = null,
    onBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    var phase by remember { mutableStateOf(DuelPhase.P1_SET_WORD) }
    var secretWordP1 by remember { mutableStateOf("") }
    var secretWordP2 by remember { mutableStateOf("") }

    var p1Attempts by remember { mutableIntStateOf(0) }
    var p2Attempts by remember { mutableIntStateOf(0) }
    var p1Won by remember { mutableStateOf(false) }
    var p2Won by remember { mutableStateOf(false) }
    var p1TimeSeconds by remember { mutableLongStateOf(0L) }
    var p2TimeSeconds by remember { mutableLongStateOf(0L) }

    var roundStartTime by remember { mutableLongStateOf(0L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var secretInput by remember { mutableStateOf("") }

    // Active board state
    var activeState by remember { mutableStateOf(GameState(wordLength = 5)) }

    LaunchedEffect(phase) {
        if (phase == DuelPhase.P1_SOLVING || phase == DuelPhase.P2_SOLVING) {
            focusRequester.requestFocus()
        }
    }

    fun startSolving(secretWord: String) {
        roundStartTime = System.currentTimeMillis()
        activeState = GameState(
            wordLength = 5,
            targetWord = secretWord.uppercase(),
        )
    }

    fun onKey(char: Char) {
        if (activeState.status != GameStatus.IN_PROGRESS || activeState.isRevealing) return
        if (activeState.currentInput.length < 5) {
            soundManager?.playKeyClick()
            val newInput = activeState.currentInput + char
            activeState = activeState.withCurrentInput(newInput)
        }
    }

    fun onBackspace() {
        if (activeState.isRevealing || activeState.currentInput.isEmpty()) return
        soundManager?.playKeyClick()
        activeState = activeState.withCurrentInput(activeState.currentInput.dropLast(1))
    }

    fun onEnter() {
        if (activeState.isRevealing) return
        val input = activeState.currentInput.uppercase()
        if (input.length != 5) {
            soundManager?.playError()
            errorMessage = "Word must be 5 letters"
            return
        }
        if (!wordRepository.isValidWord(input)) {
            soundManager?.playError()
            errorMessage = "Not in word list"
            return
        }
        errorMessage = null

        val row = activeState.currentRow
        val results = engine.evaluate(input, activeState.targetWord)

        val newBoard = activeState.board.toMutableList().map { it.toMutableList() }
        results.forEachIndexed { col, s -> newBoard[row][col] = s }

        val newBoardLetters = activeState.boardLetters.toMutableList().map { it.toMutableList() }
        input.forEachIndexed { col, c -> newBoardLetters[row][col] = c }

        val won = results.all { it == TileState.CORRECT }
        val nextRow = row + 1
        val lost = !won && nextRow >= 6

        activeState = activeState.copy(
            board = newBoard,
            boardLetters = newBoardLetters,
            currentRow = nextRow,
            currentInput = "",
            isRevealing = true,
        )

        coroutineScope.launch {
            for (col in 0 until 5) {
                delay(250L)
                val k = input[col]
                val s = results[col]
                val updatedKeys = activeState.keyStates.toMutableMap()
                val curr = updatedKeys[k] ?: TileState.EMPTY
                if (s.priority() > curr.priority()) {
                    updatedKeys[k] = s
                    activeState = activeState.copy(keyStates = updatedKeys)
                }
            }
            delay(380L)

            activeState = activeState.copy(
                isRevealing = false,
                status = if (won) GameStatus.WON else if (lost) GameStatus.LOST else GameStatus.IN_PROGRESS,
                winningRow = if (won) row else null,
            )

            if (won) {
                soundManager?.playVictory()
            }

            if (won || lost) {
                val durationSec = (System.currentTimeMillis() - roundStartTime) / 1000
                delay(1600L)
                if (phase == DuelPhase.P2_SOLVING) {
                    p2Attempts = if (won) nextRow else 7
                    p2Won = won
                    p2TimeSeconds = durationSec
                    phase = DuelPhase.P2_SET_WORD
                } else if (phase == DuelPhase.P1_SOLVING) {
                    p1Attempts = if (won) nextRow else 7
                    p1Won = won
                    p1TimeSeconds = durationSec
                    phase = DuelPhase.MATCH_OVER
                    achievementDao?.let { dao ->
                        val existing = dao.getAchievement("DUEL_PLAYED")
                        if (existing != null && !existing.unlocked) {
                            dao.upsertAchievement(
                                existing.copy(
                                    unlocked = true,
                                    currentProgress = 1,
                                    unlockedAt = LocalDate.now().toString(),
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Outlined.People, contentDescription = null, tint = TileCorrect)
                        Text("Pass and play")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    if ((phase == DuelPhase.P1_SOLVING || phase == DuelPhase.P2_SOLVING) && keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.Enter, Key.NumPadEnter -> {
                                onEnter()
                                true
                            }
                            Key.Backspace -> {
                                onBackspace()
                                true
                            }
                            else -> {
                                val nativeCode = keyEvent.nativeKeyEvent.keyCode
                                if (nativeCode in AndroidKeyEvent.KEYCODE_A..AndroidKeyEvent.KEYCODE_Z) {
                                    val char = ('A' + (nativeCode - AndroidKeyEvent.KEYCODE_A))
                                    onKey(char)
                                    true
                                } else {
                                    false
                                }
                            }
                        }
                    } else {
                        false
                    }
                }
                .padding(padding)
                .padding(16.dp),
        ) {
            when (phase) {
                DuelPhase.P1_SET_WORD -> {
                    WordSetupCard(
                        playerNumber = 1,
                        opponentNumber = 2,
                        secretInput = secretInput,
                        onInputChange = { if (it.length <= 5 && it.all { c -> c.isLetter() }) secretInput = it.uppercase() },
                        onRandomWord = { secretInput = wordRepository.randomWord(5) },
                        onConfirm = {
                            if (secretInput.length == 5 && wordRepository.isValidWord(secretInput)) {
                                secretWordP1 = secretInput
                                secretInput = ""
                                errorMessage = null
                                phase = DuelPhase.HANDOFF_TO_P2
                            } else {
                                errorMessage = "Must be a valid 5-letter dictionary word"
                            }
                        },
                        errorMessage = errorMessage,
                    )
                }

                DuelPhase.HANDOFF_TO_P2 -> {
                    HandoffCard(
                        handToPlayer = 2,
                        onReady = {
                            startSolving(secretWordP1)
                            phase = DuelPhase.P2_SOLVING
                        },
                    )
                }

                DuelPhase.P2_SOLVING -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TileMisplaced.copy(alpha = 0.2f),
                            modifier = Modifier.padding(bottom = 8.dp),
                        ) {
                            Text(
                                text = "PLAYER 2'S TURN TO GUESS",
                                fontWeight = FontWeight.Bold,
                                color = TileMisplaced,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            )
                        }

                        TileGrid(
                            state = activeState,
                            onTileFlipSound = { ts, col -> soundManager?.playTileFlip(ts, col) },
                        )

                        WordleKeyboard(
                            keyStates = activeState.keyStates,
                            onKey = { onKey(it) },
                            onBackspace = { onBackspace() },
                            onEnter = { onEnter() },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        )
                    }
                }

                DuelPhase.P2_SET_WORD -> {
                    WordSetupCard(
                        playerNumber = 2,
                        opponentNumber = 1,
                        secretInput = secretInput,
                        onInputChange = { if (it.length <= 5 && it.all { c -> c.isLetter() }) secretInput = it.uppercase() },
                        onRandomWord = { secretInput = wordRepository.randomWord(5) },
                        onConfirm = {
                            if (secretInput.length == 5 && wordRepository.isValidWord(secretInput)) {
                                secretWordP2 = secretInput
                                secretInput = ""
                                errorMessage = null
                                phase = DuelPhase.HANDOFF_TO_P1
                            } else {
                                errorMessage = "Must be a valid 5-letter dictionary word"
                            }
                        },
                        errorMessage = errorMessage,
                    )
                }

                DuelPhase.HANDOFF_TO_P1 -> {
                    HandoffCard(
                        handToPlayer = 1,
                        onReady = {
                            startSolving(secretWordP2)
                            phase = DuelPhase.P1_SOLVING
                        },
                    )
                }

                DuelPhase.P1_SOLVING -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TileCorrect.copy(alpha = 0.2f),
                            modifier = Modifier.padding(bottom = 8.dp),
                        ) {
                            Text(
                                text = "PLAYER 1'S TURN TO GUESS",
                                fontWeight = FontWeight.Bold,
                                color = TileCorrect,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            )
                        }

                        TileGrid(
                            state = activeState,
                            onTileFlipSound = { ts, col -> soundManager?.playTileFlip(ts, col) },
                        )

                        WordleKeyboard(
                            keyStates = activeState.keyStates,
                            onKey = { onKey(it) },
                            onBackspace = { onBackspace() },
                            onEnter = { onEnter() },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        )
                    }
                }

                DuelPhase.MATCH_OVER -> {
                    var confettiOn by remember { mutableStateOf(true) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(3600)
                        confettiOn = false
                    }
                    ConfettiParticleEngine(trigger = confettiOn)

                    val winnerText = when {
                        p1Attempts < p2Attempts -> "PLAYER 1 WINS THE DUEL!"
                        p2Attempts < p1Attempts -> "PLAYER 2 WINS THE DUEL!"
                        p1TimeSeconds < p2TimeSeconds -> "PLAYER 1 WINS (FASTER TIME)!"
                        p2TimeSeconds < p1TimeSeconds -> "PLAYER 2 WINS (FASTER TIME)!"
                        else -> "IT'S A DEAD TIE!"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                tint = TileCorrect,
                                modifier = Modifier.size(56.dp),
                            )

                            Text(
                                text = winnerText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                color = TileCorrect,
                            )

                            // Split Scorecard Table
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Player 1", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (p1Won) "$p1Attempts guesses" else "Failed",
                                        fontWeight = FontWeight.Bold,
                                        color = if (p1Won) TileCorrect else MaterialTheme.colorScheme.error,
                                    )
                                    Text(
                                        text = "${p1TimeSeconds}s elapsed",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                VerticalDivider(modifier = Modifier.height(50.dp))

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Player 2", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (p2Won) "$p2Attempts guesses" else "Failed",
                                        fontWeight = FontWeight.Bold,
                                        color = if (p2Won) TileCorrect else MaterialTheme.colorScheme.error,
                                    )
                                    Text(
                                        text = "${p2TimeSeconds}s elapsed",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Button(
                                    onClick = {
                                        phase = DuelPhase.P1_SET_WORD
                                        secretInput = ""
                                        secretWordP1 = ""
                                        secretWordP2 = ""
                                        p1Attempts = 0
                                        p2Attempts = 0
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rematch", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        ShareResult.shareDuel(
                                            context = context,
                                            p1Won = p1Won,
                                            p1Attempts = p1Attempts,
                                            p1TimeSeconds = p1TimeSeconds,
                                            p2Won = p2Won,
                                            p2Attempts = p2Attempts,
                                            p2TimeSeconds = p2TimeSeconds,
                                            winnerText = winnerText,
                                        )
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onBack,
                                    modifier = Modifier.weight(0.8f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text("Home", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordSetupCard(
    playerNumber: Int,
    opponentNumber: Int,
    secretInput: String,
    onInputChange: (String) -> Unit,
    onRandomWord: () -> Unit,
    onConfirm: () -> Unit,
    errorMessage: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "PLAYER $playerNumber: SET SECRET WORD",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )

            Text(
                text = "Choose a tricky 5-letter word for Player $opponentNumber to solve. Keep the screen hidden from them!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = secretInput,
                onValueChange = onInputChange,
                label = { Text("Secret 5-Letter Word") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onRandomWord,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Random", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TileCorrect),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HandoffCard(
    handToPlayer: Int,
    onReady: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(TileCorrect.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.People,
                    contentDescription = null,
                    tint = TileCorrect,
                    modifier = Modifier.size(44.dp),
                )
            }

            Text(
                text = "PASS PHONE TO PLAYER $handToPlayer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Player $handToPlayer, get ready! You have 6 attempts to deduce the secret word.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = onReady,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TileCorrect),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("I Am Ready — Start Round", fontWeight = FontWeight.Bold)
            }
        }
    }
}
