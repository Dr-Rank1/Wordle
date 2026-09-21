package com.rank.lexi.ui.screen

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.rank.lexi.domain.model.GameMode
import com.rank.lexi.domain.model.GameStatus
import com.rank.lexi.ui.composable.BossHealthBar
import com.rank.lexi.ui.composable.ConfettiParticleEngine
import com.rank.lexi.ui.composable.GameOverSheet
import com.rank.lexi.ui.composable.PostGameAnalysisDialog
import com.rank.lexi.ui.composable.RpgScorecardDialog
import com.rank.lexi.ui.composable.ShareButton
import com.rank.lexi.ui.composable.TileGrid
import com.rank.lexi.ui.composable.WordleKeyboard
import com.rank.lexi.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val particleEffect by viewModel.playerPreferences.particleEffectFlow.collectAsState(initial = "CONFETTI")
    val hapticsEnabled by viewModel.playerPreferences.hapticsEnabledFlow.collectAsState(initial = true)
    val steps by viewModel.guessAnalysisSteps.collectAsState()

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Enter, Key.NumPadEnter -> {
                            viewModel.onEnter()
                            true
                        }
                        Key.Backspace -> {
                            viewModel.onBackspace()
                            true
                        }
                        else -> {
                            val nativeCode = keyEvent.nativeKeyEvent.keyCode
                            if (nativeCode in AndroidKeyEvent.KEYCODE_A..AndroidKeyEvent.KEYCODE_Z) {
                                val char = ('A' + (nativeCode - AndroidKeyEvent.KEYCODE_A))
                                viewModel.onKey(char)
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
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = when (state.gameMode) {
                                        GameMode.DAILY -> "Daily"
                                        GameMode.TIMED_RUSH -> "Rush"
                                        GameMode.LEVEL -> "Level ${state.campaignLevel ?: 1}"
                                        GameMode.PRACTICE -> "Practice"
                                        GameMode.DUEL -> "Duel"
                                        GameMode.CUSTOM -> "Challenge"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                val subtitle = buildList {
                                    if (state.gameMode == GameMode.TIMED_RUSH) add("${state.rushTimeRemainingSeconds}s")
                                    if (state.gameMode == GameMode.PRACTICE) add("${state.wordLength} letters")
                                    if (state.hardMode) add("Hard")
                                }.joinToString("  ·  ")
                                if (subtitle.isNotBlank()) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            if (state.status == GameStatus.IN_PROGRESS) {
                                IconButton(
                                    onClick = viewModel::onHint,
                                    enabled = !state.hintUsed,
                                ) {
                                    Icon(
                                        Icons.Outlined.Lightbulb,
                                        contentDescription = if (state.hintUsed) "Hint used" else "Hint",
                                    )
                                }
                            }
                            if (viewModel.canRestartFromToolbar()) {
                                IconButton(onClick = { viewModel.playAgain() }) {
                                    Icon(Icons.Outlined.Refresh, contentDescription = "Restart")
                                }
                            }
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.isBossFight) {
                        BossHealthBar(
                            bossName = state.bossName,
                            bossTitle = state.bossTitle,
                            currentHp = state.bossCurrentHp,
                            maxHp = state.bossMaxHp,
                            modifierDescription = state.bossModifierDescription,
                            timeRemainingSeconds = state.bossTimeRemainingSeconds,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }

                    if (state.gameMode == GameMode.TIMED_RUSH && state.isRushActive) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = if (state.rushWordsSolved > 0) {
                                        val combo = (1.0f + (state.rushWordsSolved * 0.15f)).coerceAtMost(3.0f)
                                        "${state.rushWordsSolved} solved · x${"%.1f".format(java.util.Locale.US, combo)} Frenzy"
                                    } else {
                                        "${state.rushWordsSolved} solved"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (state.rushWordsSolved >= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (state.rushWordsSolved >= 3) FontWeight.Bold else FontWeight.Normal,
                                )
                                Text(
                                    text = "${state.rushScore}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            LinearProgressIndicator(
                                progress = {
                                    val remaining = state.rushTimeRemainingSeconds.toFloat()
                                    if (remaining.isNaN()) 0f else (remaining / 180f).coerceIn(0f, 1f)
                                },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = if (state.rushTimeRemainingSeconds < 25) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = state.message != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut(),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        ) {
                            Text(
                                text = state.message ?: "",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                            )
                        }
                    }
                }

                TileGrid(
                    state = state,
                    onTileFlipSound = { ts, col -> viewModel.soundManager.playTileFlip(ts, col) },
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(vertical = 8.dp),
                )

                if (state.status != GameStatus.IN_PROGRESS && !state.showGameOverSheet) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 8.dp),
                    ) {
                        Button(
                            onClick = { viewModel.playAgain() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(
                                text = when (state.gameMode) {
                                    GameMode.LEVEL -> "Next"
                                    GameMode.DAILY -> "Practice"
                                    else -> "Play again"
                                },
                            )
                        }
                        ShareButton(state = state)
                    }
                }

                WordleKeyboard(
                    keyStates = state.keyStates,
                    onKey = viewModel::onKey,
                    onBackspace = viewModel::onBackspace,
                    onEnter = viewModel::onEnter,
                    hapticsEnabled = hapticsEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                )
            }
        }

        ConfettiParticleEngine(
            trigger = state.showConfetti,
            particleEffect = particleEffect,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f),
        )

        if (state.showAnalysisDialog) {
            PostGameAnalysisDialog(
                targetWord = state.targetWord,
                steps = steps,
                onDismiss = { viewModel.dismissAnalysis() },
            )
        }

        if (state.showScorecardDialog) {
            RpgScorecardDialog(
                targetWord = state.targetWord,
                won = state.status == GameStatus.WON,
                attempts = state.currentRow,
                maxAttempts = state.maxAttempts,
                solveDurationMs = state.solveDurationMs,
                steps = steps,
                onDismiss = { viewModel.dismissScorecard() },
            )
        }

        GameOverSheet(
            state = state,
            onPlayAgain = { viewModel.playAgain() },
            onShowAnalysis = { viewModel.showAnalysis() },
            onShowScorecard = { viewModel.showScorecard() },
            onDismiss = { viewModel.dismissGameOverSheet() },
        )
    }
}
