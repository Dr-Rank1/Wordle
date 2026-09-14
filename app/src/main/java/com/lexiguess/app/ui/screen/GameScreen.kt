package com.lexiguess.app.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.zIndex
import com.lexiguess.app.domain.model.GameStatus
import com.lexiguess.app.ui.composable.*
import com.lexiguess.app.ui.theme.TileCorrect
import com.lexiguess.app.ui.viewmodel.GameViewModel

/**
 * Main game screen.
 *
 * Hosts the header (with mode badge and instant practice restart), tile grid,
 * hint / play again buttons, on-screen keyboard, toast messages, confetti celebration,
 * and game-over summary modal with word definitions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "LexiGuess",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (state.isPracticeMode) {
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    } else {
                                        TileCorrect.copy(alpha = 0.15f)
                                    },
                                ) {
                                    Text(
                                        text = if (state.isPracticeMode) "PRACTICE" else "DAILY",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.isPracticeMode) {
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                        } else {
                                            TileCorrect
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateToStats) {
                                Icon(Icons.Outlined.BarChart, contentDescription = "Statistics")
                            }
                        },
                        actions = {
                            // New Game / Play Again button in app bar
                            IconButton(
                                onClick = { viewModel.playAgain(practice = true) },
                            ) {
                                Icon(Icons.Outlined.Refresh, contentDescription = "New Practice Word")
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
                // Toast notification (e.g. "Not in word list", "Not enough letters", or win toast)
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

                // Main 6x5 Tile grid
                TileGrid(
                    state = state,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(vertical = 4.dp),
                )

                // Middle action bar: Hint (during game) or Play Again + Share (game over)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp),
                ) {
                    if (state.status == GameStatus.IN_PROGRESS) {
                        HintButton(
                            hintUsed = state.hintUsed,
                            onHint = viewModel::onHint,
                        )
                    } else {
                        Button(
                            onClick = { viewModel.playAgain(practice = true) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TileCorrect,
                                contentColor = Color.White,
                            ),
                            modifier = Modifier.height(46.dp),
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Play Again", fontWeight = FontWeight.Bold)
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

        // Win Confetti overlay
        ConfettiOverlay(
            active = state.showConfetti,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f),
        )

        // Game Over Bottom Sheet with Word Definition & Play Again
        GameOverSheet(
            state = state,
            onPlayAgain = { viewModel.playAgain(practice = true) },
            onDismiss = { viewModel.dismissGameOverSheet() },
        )
    }
}
