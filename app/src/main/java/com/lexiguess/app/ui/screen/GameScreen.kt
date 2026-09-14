package com.lexiguess.app.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lexiguess.app.domain.model.GameStatus
import com.lexiguess.app.ui.composable.*
import com.lexiguess.app.ui.viewmodel.GameViewModel

/**
 * Main game screen. Hosts the header, tile grid, hint + share buttons, the
 * on-screen keyboard, and transient toast messages.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "LexiGuess",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateToStats) {
                            Icon(Icons.Outlined.BarChart, contentDescription = "Statistics")
                        }
                    },
                    actions = {
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Toast message
            AnimatedVisibility(
                visible = state.message != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut(),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        text = state.message ?: "",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }

            // Tile grid
            TileGrid(
                state = state,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .widthIn(max = 350.dp),
            )

            // Action buttons (hint + share)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                HintButton(
                    hintUsed = state.hintUsed,
                    onHint = viewModel::onHint,
                )
                if (state.status != GameStatus.IN_PROGRESS) {
                    ShareButton(state = state)
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
                    .padding(bottom = 16.dp),
            )
        }
    }
}
