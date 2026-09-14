package com.lexiguess.app.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexiguess.app.data.db.VaultDao
import com.lexiguess.app.data.db.VaultWordRecord
import com.lexiguess.app.data.repository.PlayerPreferences
import com.lexiguess.app.data.repository.WordRepository
import com.lexiguess.app.domain.MultiBoardEngine
import com.lexiguess.app.domain.model.*
import com.lexiguess.app.ui.audio.SoundManager
import com.lexiguess.app.ui.composable.ConfettiParticleEngine
import com.lexiguess.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiBoardScreen(
    multiBoardEngine: MultiBoardEngine,
    wordRepository: WordRepository,
    soundManager: SoundManager,
    playerPreferences: PlayerPreferences,
    vaultDao: VaultDao? = null,
    onBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedMode by remember { mutableStateOf(MultiBoardMode.DORDLE) }
    var gameState by remember { mutableStateOf<MultiBoardState?>(null) }
    var soundEnabled by remember { mutableStateOf(true) }
    val particleEffect by playerPreferences.particleEffectFlow.collectAsState(initial = "CONFETTI")

    LaunchedEffect(Unit) {
        playerPreferences.soundEnabledFlow.collect { soundEnabled = it }
    }

    // Function to start a fresh match
    val startMatch: (MultiBoardMode) -> Unit = { mode ->
        val targets = (1..mode.boardCount).map {
            wordRepository.randomWord(5)
        }
        gameState = multiBoardEngine.startNewGame(mode, targets)
    }

    LaunchedEffect(selectedMode) {
        startMatch(selectedMode)
    }

    val state = gameState ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MULTI-BOARD MODE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TileCorrect,
                            letterSpacing = 1.5.sp,
                        )
                        Text(
                            text = state.mode.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Switch Dordle / Quordle toggle button
                    TextButton(onClick = {
                        selectedMode = if (selectedMode == MultiBoardMode.DORDLE) MultiBoardMode.QUORDLE else MultiBoardMode.DORDLE
                    }) {
                        Text(
                            text = if (selectedMode == MultiBoardMode.DORDLE) "Switch to 4 Boards" else "Switch to 2 Boards",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    IconButton(onClick = { startMatch(selectedMode) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restart")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Header status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Solved: ${state.solvedCount}/${state.mode.boardCount}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TileCorrect,
                    )
                    Text(
                        text = "Guess ${state.currentRow + 1} of ${state.maxAttempts}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Boards display area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.mode == MultiBoardMode.DORDLE) {
                        // 2 Boards side by side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            state.boards.forEach { board ->
                                MiniBoard(
                                    board = board,
                                    currentInput = state.currentInput,
                                    maxAttempts = state.maxAttempts,
                                    wordLength = state.wordLength,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    } else {
                        // 4 Boards in 2x2 layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MiniBoard(
                                board = state.boards[0],
                                currentInput = state.currentInput,
                                maxAttempts = state.maxAttempts,
                                wordLength = state.wordLength,
                                modifier = Modifier.weight(1f),
                            )
                            MiniBoard(
                                board = state.boards[1],
                                currentInput = state.currentInput,
                                maxAttempts = state.maxAttempts,
                                wordLength = state.wordLength,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MiniBoard(
                                board = state.boards[2],
                                currentInput = state.currentInput,
                                maxAttempts = state.maxAttempts,
                                wordLength = state.wordLength,
                                modifier = Modifier.weight(1f),
                            )
                            MiniBoard(
                                board = state.boards[3],
                                currentInput = state.currentInput,
                                maxAttempts = state.maxAttempts,
                                wordLength = state.wordLength,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // Error message banner
                AnimatedVisibility(visible = state.message != null) {
                    state.message?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                }

                // Multi-board split keyboard
                MultiBoardKeyboard(
                    mode = state.mode,
                    keyStates = state.keyBoardStates,
                    onLetter = { char ->
                        if (soundEnabled) soundManager.playKeyClick()
                        gameState = multiBoardEngine.onLetterInput(state, char)
                    },
                    onDelete = {
                        if (soundEnabled) soundManager.playKeyClick()
                        gameState = multiBoardEngine.onDelete(state)
                    },
                    onSubmit = {
                        val validSet = wordRepository.getValidWordsSet(state.wordLength)
                        val newState = multiBoardEngine.submitGuess(state, validSet)
                        gameState = newState
                        if (newState.status == GameStatus.WON) {
                            coroutineScope.launch {
                                playerPreferences.addXp(if (state.mode == MultiBoardMode.DORDLE) 120 else 250)
                                vaultDao?.let { dao ->
                                    newState.boards.forEach { b ->
                                        val existing = dao.getWord(b.targetWord)
                                        dao.upsert(
                                            VaultWordRecord(
                                                word = b.targetWord,
                                                length = state.wordLength,
                                                definition = "Solved in Multi-Board ${state.mode.title}",
                                                partOfSpeech = "word",
                                                example = "",
                                                timesSolved = (existing?.timesSolved ?: 0) + 1,
                                                bestGuesses = minOf(existing?.bestGuesses ?: 9, b.guesses.size),
                                                unlockedAt = existing?.unlockedAt ?: System.currentTimeMillis(),
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        if (soundEnabled) {
                            if (newState.status == GameStatus.WON) {
                                soundManager.playVictory()
                            } else if (newState.shake) {
                                soundManager.playError()
                            } else {
                                soundManager.playKeyClick()
                            }
                        }
                    },
                )
            }

            ConfettiParticleEngine(trigger = state.showConfetti, particleEffect = particleEffect)
        }
    }
}

@Composable
private fun MiniBoard(
    board: SingleBoardState,
    currentInput: String,
    maxAttempts: Int,
    wordLength: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Board #${board.boardIndex + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (board.isSolved) TileCorrect else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (board.isSolved) {
                    Text(
                        text = "SOLVED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = TileCorrect,
                    )
                }
            }

            // Draw rows
            for (row in 0 until maxAttempts) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val isCompletedRow = row < board.guesses.size
                    val isCurrentInputRow = !board.isSolved && row == board.guesses.size

                    for (col in 0 until wordLength) {
                        val letter = when {
                            isCompletedRow -> board.guesses[row].getOrNull(col) ?: ' '
                            isCurrentInputRow -> currentInput.getOrNull(col) ?: ' '
                            else -> ' '
                        }

                        val tileState = when {
                            isCompletedRow -> board.rowStates[row].getOrNull(col) ?: TileState.EMPTY
                            isCurrentInputRow && col < currentInput.length -> TileState.FILLED
                            else -> TileState.EMPTY
                        }

                        MiniTile(
                            letter = letter,
                            state = tileState,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniTile(
    letter: Char,
    state: TileState,
    modifier: Modifier = Modifier,
) {
    val bgColor = when (state) {
        TileState.CORRECT -> TileCorrect
        TileState.MISPLACED -> TileMisplaced
        TileState.ABSENT -> TileAbsentDark
        TileState.FILLED -> MaterialTheme.colorScheme.surface
        TileState.EMPTY -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }

    val textColor = when (state) {
        TileState.CORRECT, TileState.MISPLACED, TileState.ABSENT -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (state == TileState.FILLED) TileBorderFilledDark else TileBorderEmptyDark.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (letter != ' ') {
            Text(
                text = letter.toString(),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = textColor,
            )
        }
    }
}

@Composable
private fun MultiBoardKeyboard(
    mode: MultiBoardMode,
    keyStates: Map<Char, List<TileState>>,
    onLetter: (Char) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
) {
    val rows = listOf(
        "QWERTYUIOP",
        "ASDFGHJKL",
        "ZXCVBNM",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        rows.forEachIndexed { index, rowLetters ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (index == 2) {
                    // ENTER button
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(42.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KeyDefaultDark),
                    ) {
                        Text("ENTER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                rowLetters.forEach { char ->
                    val states = keyStates[char] ?: List(mode.boardCount) { TileState.EMPTY }
                    SplitKey(
                        letter = char,
                        states = states,
                        onClick = { onLetter(char) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                    )
                }

                if (index == 2) {
                    // DEL button
                    Button(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(42.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KeyDefaultDark),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitKey(
    letter: Char,
    states: List<TileState>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (states.size == 2) {
            // Dordle: 2 halves
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colorForTileState(states[0]))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colorForTileState(states[1]))
                )
            }
        } else {
            // Quordle: 4 quadrants
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(colorForTileState(states[0])))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(colorForTileState(states[1])))
                }
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(colorForTileState(states[2])))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(colorForTileState(states[3])))
                }
            }
        }

        Text(
            text = letter.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.White,
        )
    }
}

private fun colorForTileState(state: TileState): Color = when (state) {
    TileState.CORRECT -> TileCorrect
    TileState.MISPLACED -> TileMisplaced
    TileState.ABSENT -> TileAbsentDark
    else -> KeyDefaultDark
}
