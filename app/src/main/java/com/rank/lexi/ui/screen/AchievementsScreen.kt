package com.rank.lexi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.db.AchievementRecord
import com.rank.lexi.ui.theme.TileCorrect
import com.rank.lexi.ui.theme.TileMisplaced

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    achievementDao: AchievementDao,
    onBack: () -> Unit,
) {
    val achievements by achievementDao.getAllAchievements().collectAsState(initial = emptyList())
    val unlockedCount by achievementDao.getUnlockedCount().collectAsState(initial = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Achievements",
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
                        color = TileCorrect.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 12.dp),
                    ) {
                        Text(
                            text = "$unlockedCount / ${achievements.size.coerceAtLeast(16)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TileCorrect,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text(
                        text = "Unlock milestone badges across daily challenges, timed rush, campaign stages, and special deduction feats.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }

            items(achievements) { badge ->
                AchievementCard(badge = badge)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun AchievementCard(badge: AchievementRecord) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (badge.unlocked) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Badge Icon container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.unlocked) TileCorrect.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconForName(badge.iconName),
                    contentDescription = null,
                    tint = if (badge.unlocked) TileCorrect else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp),
                )
            }

            // Info & Progress
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )

                    if (badge.unlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TileCorrect.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = "UNLOCKED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TileCorrect,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!badge.unlocked && badge.targetProgress > 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { badge.currentProgress.toFloat() / badge.targetProgress.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = TileCorrect,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}

private fun iconForName(name: String): ImageVector = when (name) {
    "Star" -> Icons.Default.Star
    "EmojiEvents" -> Icons.Default.EmojiEvents
    "MilitaryTech" -> Icons.Default.MilitaryTech
    "TrendingUp" -> Icons.AutoMirrored.Filled.TrendingUp
    "DateRange" -> Icons.Default.DateRange
    "Shield" -> Icons.Default.Shield
    "Bolt" -> Icons.Default.Bolt
    "Favorite" -> Icons.Default.Favorite
    "Timer" -> Icons.Default.Timer
    "Speed" -> Icons.Default.Speed
    "WorkspacePremium" -> Icons.Default.WorkspacePremium
    "CheckCircle" -> Icons.Default.CheckCircle
    "Grade" -> Icons.Default.Grade
    "Lock" -> Icons.Default.Lock
    "People" -> Icons.Default.People
    "Dashboard" -> Icons.Default.Dashboard
    "Psychology" -> Icons.Default.Psychology
    "Whatshot" -> Icons.Default.Whatshot
    else -> Icons.Default.Stars
}
