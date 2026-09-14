package com.lexiguess.app.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.lexiguess.app.domain.model.GameMode
import com.lexiguess.app.domain.model.GameStatus
import com.lexiguess.app.ui.composable.*
import com.lexiguess.app.ui.theme.TileCorrect
import com.lexiguess.app.ui.theme.TileMisplaced
import com.lexiguess.app.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    var showDuelDialog by remember { mutableStateOf(false) }
    var duelWordInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "LexiGuess",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                )

                                // Game Mode Badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when (state.gameMode) {
                                        GameMode.TIMED_RUSH -> TileMisplaced.copy(alpha = 0.18f)
                                        GameMode.LEVEL -> MaterialTheme.colorScheme.primaryContainer
                                        GameMode.DAILY -> TileCorrect.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                ) {
                                    Text(
                                        text = when (state.gameMode) {
                                            GameMode.DAILY -> "DAILY"
                                            GameMode.TIMED_RUSH -> "RUSH · ${state.rushTimeRemainingSeconds}s"
                                            GameMode.LEVEL -> "LEVEL ${state.campaignLevel ?: 1}"
                                            GameMode.PRACTICE -> "${state.wordLength}L PRACTICE"
                                            GameMode.DUEL -> "DUEL"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (state.gameMode) {
                                            GameMode.TIMED_RUSH -> TileMisplaced
                                            GameMode.DAILY -> TileCorrect
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackToHome) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.playAgain() }) {
                                Icon(Icons.Outlined.Refresh, contentDescription = "Restart / Next")
                            }
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                    )
                    HorizontalDivider()
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Boss Encounter Live Bar (if active)
                if (state.isBossFight) {
                    BossHealthBar(
                        bossName = state.bossName,
                        bossTitle = state.bossTitle,
                        currentHp = state.bossCurrentHp,
                        maxHp = state.bossMaxHp,
                        modifierDescription = state.bossModifierDescription,
                        timeRemainingSeconds = state.bossTimeLimitSeconds,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                // Timed Rush Live Bar (if active)
                if (state.gameMode == GameMode.TIMED_RUSH && state.isRushActive) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Solved: ${state.rushWordsSolved}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Score: ${state.rushScore}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TileCorrect,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (state.rushTimeRemainingSeconds.toFloat() / 120f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = if (state.rushTimeRemainingSeconds < 25) MaterialTheme.colorScheme.error else TileCorrect,
                        )
                    }
                }

                // Toast notification
                AnimatedVisibility(
                    visible = state.message != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut(),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.inverseSurface,
                        shadowElevation = 4.dp,
                    ) {
                        Text(
                            text = state.message ?: "",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    }
                }

                // Wordle Bot elimination hint
                if (state.currentRow > 0 && state.status == GameStatus.IN_PROGRESS && state.remainingCandidates > 0) {
                    Text(
                        text = "${state.remainingCandidates} possible solutions remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }

                // Main multi-length Tile Grid
                TileGrid(
                    state = state,
                    onTileFlipSound = { ts, col -> viewModel.soundManager.playTileFlip(ts, col) },
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(vertical = 4.dp),
                )

                // Middle Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    if (state.status == GameStatus.IN_PROGRESS) {
                        HintButton(
                            hintUsed = state.hintUsed,
                            onHint = viewModel::onHint,
                        )
                    } else {
                        Button(
                            onClick = { viewModel.playAgain() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TileCorrect,
                                contentColor = Color.White,
                            ),
                            modifier = Modifier.height(46.dp),
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.gameMode == GameMode.LEVEL) "Next Level" else "Play Again",
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        val steps by viewModel.guessAnalysisSteps.collectAsState()
                        if (steps.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { viewModel.showScorecard() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(46.dp),
                            ) {
                                Text("Scorecard", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { viewModel.showAnalysis() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(46.dp),
                            ) {
                                Text("Analysis", fontWeight = FontWeight.Bold)
                            }
                        }

                        ShareButton(
                            state = state,
                            modifier = Modifier.height(46.dp),
                        )
                    }
                }

                // Keyboard
                WordleKeyboard(
                    keyStates = state.keyStates,
                    onKey = viewModel::onKey,
                    onBackspace = viewModel::onBackspace,
                    onEnter = viewModel::onEnter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                )
            }
        }

        // Win Confetti Particle Engine
        ConfettiParticleEngine(
            trigger = state.showConfetti,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f),
        )

        // Post Game Analysis Dialog
        val steps by viewModel.guessAnalysisSteps.collectAsState()
        if (state.showAnalysisDialog) {
            PostGameAnalysisDialog(
                targetWord = state.targetWord,
                steps = steps,
                onDismiss = { viewModel.dismissAnalysis() },
            )
        }

        // RPG Performance Scorecard Dialog
        if (state.showScorecardDialog) {
            RpgScorecardDialog(
                targetWord = state.targetWord,
                won = state.status == GameStatus.WON,
                attempts = state.currentRow,
                maxAttempts = if (state.isBossFight) 4 else 6,
                solveDurationMs = state.solveDurationMs,
                steps = steps,
                onDismiss = { viewModel.dismissScorecard() },
            )
        }

        // Game Over Bottom Sheet with Word Definition & Play Again
        GameOverSheet(
            state = state,
            onPlayAgain = { viewModel.playAgain() },
            onShowAnalysis = { viewModel.showAnalysis() },
            onShowScorecard = { viewModel.showScorecard() },
            onDismiss = { viewModel.dismissGameOverSheet() },
        )
    }
}
