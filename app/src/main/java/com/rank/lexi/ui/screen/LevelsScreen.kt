package com.rank.lexi.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.lexi.data.db.LevelDao
import com.rank.lexi.data.db.LevelRecord
import com.rank.lexi.ui.theme.TileCorrect
import com.rank.lexi.ui.theme.TileMisplaced

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelsScreen(
    levelDao: LevelDao,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val levels by levelDao.getAllLevels().collectAsState(initial = emptyList())
    val totalStars by levelDao.getTotalStars().collectAsState(initial = 0)
    val completedCount by levelDao.getCompletedCount().collectAsState(initial = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Campaign",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = TileMisplaced.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = TileMisplaced,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "$totalStars / 150",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TileMisplaced,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "$completedCount / 50 stages cleared",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                        )
                        val nextBoss = listOf(10, 25, 40, 50).firstOrNull { boss ->
                            levels.none { it.levelNumber == boss && it.completed }
                        }
                        Text(
                            text = if (nextBoss != null) {
                                "Next boss: Level $nextBoss. Earn up to 3 stars per level."
                            } else {
                                "All bosses defeated. Earn up to 3 stars per level."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // World 1: 4 Letters (1-10)
            item {
                WorldSection(
                    worldName = "4 letters",
                    wordLengthDesc = "Levels 1–10",
                    levels = levels.filter { it.levelNumber in 1..10 },
                    allLevels = levels,
                    onSelectLevel = onSelectLevel,
                )
            }

            // World 2: 5 Letters (11-30)
            item {
                WorldSection(
                    worldName = "5 letters",
                    wordLengthDesc = "Levels 11–30",
                    levels = levels.filter { it.levelNumber in 11..30 },
                    allLevels = levels,
                    onSelectLevel = onSelectLevel,
                )
            }

            // World 3: 6 Letters (31-40)
            item {
                WorldSection(
                    worldName = "6 letters",
                    wordLengthDesc = "Levels 31–40",
                    levels = levels.filter { it.levelNumber in 31..40 },
                    allLevels = levels,
                    onSelectLevel = onSelectLevel,
                )
            }

            // World 4: 7 Letters (41-50)
            item {
                WorldSection(
                    worldName = "7 letters",
                    wordLengthDesc = "Levels 41–50",
                    levels = levels.filter { it.levelNumber in 41..50 },
                    allLevels = levels,
                    onSelectLevel = onSelectLevel,
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun WorldSection(
    worldName: String,
    wordLengthDesc: String,
    levels: List<LevelRecord>,
    allLevels: List<LevelRecord>,
    onSelectLevel: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = worldName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = wordLengthDesc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // 5-column grid layout for levels
        val rows = levels.chunked(5)
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (lvl in row) {
                    val isUnlocked = lvl.levelNumber == 1 ||
                            allLevels.any { it.levelNumber == lvl.levelNumber - 1 && it.completed }

                    LevelNode(
                        record = lvl,
                        isUnlocked = isUnlocked,
                        modifier = Modifier.weight(1f),
                        onClick = { if (isUnlocked) onSelectLevel(lvl.levelNumber) },
                    )
                }
                // Fill remainder if row is not full
                for (empty in row.size until 5) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LevelNode(
    record: LevelRecord,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bossConfig = com.rank.lexi.domain.BossRegistry.getBossForLevel(record.levelNumber)
    val isBoss = bossConfig != null

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "bossPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(900, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "bossPulseAlpha",
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when {
            record.completed -> if (isBoss) Color(0xFFE53935).copy(alpha = 0.22f) else TileCorrect.copy(alpha = 0.15f)
            isUnlocked -> if (isBoss) Color(0xFFE53935).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        },
        border = if (isBoss && isUnlocked) {
            val borderAlpha = if (!record.completed) pulseAlpha else 0.8f
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE53935).copy(alpha = borderAlpha))
        } else null,
        modifier = modifier
            .aspectRatio(0.85f)
            .clickable(enabled = isUnlocked, onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            if (isUnlocked) {
                if (isBoss) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(11.dp),
                        )
                        Text(
                            text = "BOSS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE53935),
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
                Text(
                    text = "${record.levelNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isBoss) Color(0xFFE53935) else if (record.completed) TileCorrect else MaterialTheme.colorScheme.onSurface,
                )

                // Stars or Boss Name
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 1..3) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i <= record.stars) TileMisplaced else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                            modifier = Modifier.size(10.dp),
                        )
                    }
                }
            } else {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
