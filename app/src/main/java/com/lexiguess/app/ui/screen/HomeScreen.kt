package com.lexiguess.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexiguess.app.data.repository.PlayerPreferences
import com.lexiguess.app.ui.theme.TileCorrect

@Composable
fun HomeScreen(
    playerPreferences: PlayerPreferences,
    onStartDaily: () -> Unit,
    onStartRush: () -> Unit,
    onNavigateToLevels: () -> Unit,
    onStartPractice: (Int) -> Unit,
    onStartDuel: () -> Unit,
    onNavigateToMultiBoard: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val xp by playerPreferences.xpFlow.collectAsState(initial = 0)
    val rushHighScore by playerPreferences.rushHighScoreFlow.collectAsState(initial = 0)

    val currentLevel = PlayerPreferences.calculateLevel(xp)
    val rankTitle = PlayerPreferences.rankForLevel(currentLevel)
    val progressInLevel = PlayerPreferences.calculateProgressInLevel(xp)

    var showLengthPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Top Player Profile Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = rankTitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TileCorrect,
                            letterSpacing = 1.5.sp,
                        )
                        Text(
                            text = "Level $currentLevel",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = TileCorrect.copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = "$xp XP",
                            fontWeight = FontWeight.Bold,
                            color = TileCorrect,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                // XP Progress Bar
                LinearProgressIndicator(
                    progress = { progressInLevel },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = TileCorrect,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Text(
                    text = "${(progressInLevel * 400).toInt()}/400 XP to next rank",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Daily Challenge Hero Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStartDaily() },
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TileCorrect.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = "DAILY PUZZLE",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = TileCorrect,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                    Text(
                        text = "Today's Word Challenge",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "Synchronized globally · Test your deduction",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                FilledIconButton(
                    onClick = onStartDaily,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = TileCorrect),
                    modifier = Modifier.size(52.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Daily",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }

        // Mode Grid Header
        Text(
            text = "GAME MODES & HUBS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp,
        )

        // Row 1: Timed Rush & Campaign
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeCard(
                title = "Timed Rush",
                subtitle = "60s · High Score: $rushHighScore",
                icon = Icons.Outlined.Timer,
                modifier = Modifier.weight(1f),
                onClick = onStartRush,
            )
            ModeCard(
                title = "Campaign",
                subtitle = "50 stages · 4 worlds",
                icon = Icons.Outlined.Flag,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToLevels,
            )
        }

        // Row 2: Multi-Board & Word Vault
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeCard(
                title = "Multi-Board",
                subtitle = "Dordle & Quordle",
                icon = Icons.Outlined.GridView,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToMultiBoard,
            )
            ModeCard(
                title = "Word Vault",
                subtitle = "Collected vocabulary",
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToVault,
            )
        }

        // Row 3: Custom Challenge & Word Lengths
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeCard(
                title = "Custom Puzzle",
                subtitle = "Challenge a friend",
                icon = Icons.Outlined.QrCode,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToChallenge,
            )
            ModeCard(
                title = "Practice Lengths",
                subtitle = "4, 5, 6, or 7 letters",
                icon = Icons.Outlined.Tune,
                modifier = Modifier.weight(1f),
                onClick = { showLengthPicker = true },
            )
        }

        // Quick Word of the Day Quote
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.AutoStories,
                    contentDescription = null,
                    tint = TileCorrect,
                )
                Column {
                    Text(
                        text = "LEXICON NOTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "A rich vocabulary increases deductive speed and clue elimination.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    // Modal to choose custom word length
    if (showLengthPicker) {
        AlertDialog(
            onDismissRequest = { showLengthPicker = false },
            title = { Text("Choose Word Length", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(4 to "4 Letters (Quick)", 5 to "5 Letters (Classic)", 6 to "6 Letters (Challenging)", 7 to "7 Letters (Mastermind)").forEach { (len, label) ->
                        OutlinedButton(
                            onClick = {
                                showLengthPicker = false
                                onStartPractice(len)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(label, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLengthPicker = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier.clickable { onClick() },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TileCorrect,
                modifier = Modifier.size(28.dp),
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
