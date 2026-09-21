package com.rank.lexi.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.platform.LocalContext
import com.rank.lexi.ui.util.ShareResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rank.lexi.ui.composable.StatsChart
import com.rank.lexi.ui.theme.TileCorrect
import com.rank.lexi.ui.theme.TileMisplaced
import com.rank.lexi.ui.viewmodel.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: (() -> Unit)? = null,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            com.rank.lexi.ui.composable.LexiTopBar(
                title = "Statistics",
                onBack = onBack,
                actions = {
                    if (!state.loading) {
                        IconButton(
                            onClick = {
                                ShareResult.shareStats(
                                    context = context,
                                    rankTitle = state.rankTitle,
                                    level = state.level,
                                    xp = state.xp,
                                    totalGames = state.totalGames,
                                    winPercent = state.winPercent,
                                    currentStreak = state.currentStreak,
                                    bestStreak = state.bestStreak,
                                )
                            }
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share Statistics")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Profile Rank Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = state.rankTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Level ${state.level}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${state.xp} XP",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Daily Stats Summary Row
            Text(
                text = "Daily",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatItem(label = "Played", value = "${state.totalGames}")
                StatItem(label = "Win %", value = "${state.winPercent}")
                StatItem(label = "Current\nStreak", value = "${state.currentStreak}")
                StatItem(label = "Best\nStreak", value = "${state.bestStreak}")
            }

            HorizontalDivider()

            // Mode Records Summary
            Text(
                text = "Records",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RecordCard(
                    title = "Rush best",
                    value = if (state.rushHighScore > 0) "${state.rushHighScore} pts" else "—",
                    accentColor = TileMisplaced,
                    modifier = Modifier.weight(1f),
                )
                RecordCard(
                    title = "Campaign",
                    value = "${state.completedLevels} / 50  ·  ${state.totalStars} stars",
                    accentColor = TileCorrect,
                    modifier = Modifier.weight(1f),
                )
            }

            HorizontalDivider()

            // Guess Distribution Chart
            Text(
                text = "Guess distribution",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.fillMaxWidth(),
            )

            StatsChart(
                distribution = state.distribution,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RecordCard(
    title: String,
    value: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
        }
    }
}
