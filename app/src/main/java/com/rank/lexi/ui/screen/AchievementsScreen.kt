package com.rank.lexi.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.lexi.data.db.AchievementDao
import com.rank.lexi.data.db.AchievementRecord
import com.rank.lexi.data.repository.CoinRepository
import com.rank.lexi.ui.theme.TileCorrect
import com.rank.lexi.ui.theme.TileMisplaced
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    achievementDao: AchievementDao,
    coinRepository: CoinRepository,
    onBack: (() -> Unit)? = null,
) {
    val achievements by achievementDao.getAllAchievements().collectAsState(initial = emptyList())
    val unlockedCount by achievementDao.getUnlockedCount().collectAsState(initial = 0)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trophy Cabinet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Text(
                        text = "Unlock milestones to fill your cabinet and earn coins!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            items(achievements) { badge ->
                TrophyItem(
                    badge = badge,
                    onClaim = {
                        scope.launch {
                            coinRepository.addCoins(badge.coinReward, "ACHIEVEMENT_REWARD", "Claimed badge: ${badge.title}")
                            achievementDao.upsertAchievement(badge.copy(isClaimed = true))
                        }
                    }
                )
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TrophyItem(
    badge: AchievementRecord,
    onClaim: () -> Unit
) {
    val canClaim = badge.unlocked && !badge.isClaimed
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = canClaim, onClick = onClaim)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .scale(if (canClaim) pulseScale else 1f)
                .shadow(if (badge.unlocked) 8.dp else 0.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    if (badge.unlocked) TileMisplaced.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                )
                .border(
                    width = if (canClaim) 2.dp else 0.dp,
                    color = if (canClaim) TileCorrect else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Circular progress if locked
            if (!badge.unlocked && badge.targetProgress > 1) {
                CircularProgressIndicator(
                    progress = { badge.currentProgress.toFloat() / badge.targetProgress.toFloat() },
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    color = TileMisplaced.copy(alpha = 0.5f),
                    trackColor = Color.Transparent,
                    strokeWidth = 3.dp
                )
            }

            Icon(
                imageVector = if (badge.unlocked) iconForName(badge.iconName) else Icons.Default.Lock,
                contentDescription = badge.title,
                tint = if (badge.unlocked) TileMisplaced else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp),
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = badge.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (badge.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
        
        if (canClaim) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = TileCorrect,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "CLAIM +${badge.coinReward}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
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
