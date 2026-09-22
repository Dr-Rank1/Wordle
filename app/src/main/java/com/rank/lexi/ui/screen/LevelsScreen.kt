package com.rank.lexi.ui.screen

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.lexi.data.db.LevelDao
import com.rank.lexi.data.db.LevelRecord
import com.rank.lexi.domain.BossRegistry
import com.rank.lexi.ui.theme.TileCorrect
import com.rank.lexi.ui.theme.TileMisplaced
import kotlin.math.sin
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelsScreen(
    levelDao: LevelDao,
    onSelectLevel: (Int) -> Unit,
    onBack: (() -> Unit)? = null,
) {
    val levels by levelDao.getAllLevels().collectAsState(initial = emptyList<LevelRecord>())
    val totalStars by levelDao.getTotalStars().collectAsState(initial = 0)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Adventure", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Star, contentDescription = "Total Stars", tint = TileMisplaced)
                        Text(" $totalStars", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        }
    ) { padding ->
        if (levels.isEmpty()) return@Scaffold

        val scrollState = rememberScrollState()
        val config = LocalConfiguration.current
        val screenWidthDp = config.screenWidthDp.dp
        val density = LocalDensity.current

        // Dimensions
        val levelSpacingDp = 110.dp
        val nodeSizeDp = 64.dp
        val topPaddingDp = 150.dp
        val bottomPaddingDp = 150.dp
        
        val totalHeightDp = topPaddingDp + bottomPaddingDp + (levelSpacingDp * (levels.size - 1))
        
        // Highest unlocked level
        val highestUnlocked = levels.lastOrNull { 
            it.levelNumber == 1 || levels.any { prev -> prev.levelNumber == it.levelNumber - 1 && prev.completed } 
        }?.levelNumber ?: 1

        // Scroll to the highest unlocked level automatically
        LaunchedEffect(highestUnlocked) {
            val targetIndex = highestUnlocked - 1
            val yOffsetDp = topPaddingDp + (levelSpacingDp * (levels.size - 1 - targetIndex))
            with(density) {
                val scrollY = yOffsetDp.toPx().toInt() - (config.screenHeightDp / 2 * density.density).toInt()
                scrollState.scrollTo(scrollY.coerceAtLeast(0))
            }
        }

        // 5 Worlds Gradient (Cosmos -> Sky -> Ocean -> Desert -> Forest)
        // Since Y=0 is the TOP (Level 50), Cosmos is at 0.0, Forest is at 1.0
        val worldGradient = Brush.verticalGradient(
            0.00f to Color(0xFF120A2A), // Cosmos Deep Purple
            0.15f to Color(0xFF301934), // Cosmos Transition
            0.25f to Color(0xFF4A90E2), // Sky Blue
            0.40f to Color(0xFF87CEEB), // Light Sky
            0.50f to Color(0xFF00B4D8), // Ocean Light
            0.65f to Color(0xFF006994), // Ocean Deep
            0.75f to Color(0xFFFFD166), // Desert Sand
            0.85f to Color(0xFFE2A76F), // Desert Dark
            0.95f to Color(0xFF558B2F), // Forest Light
            1.00f to Color(0xFF2D4A22), // Forest Deep
        )

        // Animations
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
        
        val avatarBob by infiniteTransition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "avatar_bob"
        )

        val particleOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "particles"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // Fallback
        ) {
            // Draw the huge world gradient spanning the entire scrollable height
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Box(
                    modifier = Modifier
                        .width(screenWidthDp)
                        .height(totalHeightDp)
                        .background(worldGradient)
                ) {
                    // Background Floating Particles matching the Worlds
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val baseAlpha = 0.3f
                        for (i in 0..60) {
                            val yPos = (size.height / 60) * i
                            val speed = 0.5f + (i % 3) * 0.5f
                            val startX = (i * 73f) % size.width
                            val currentX = (startX + particleOffset * size.width * speed) % size.width
                            
                            drawCircle(
                                color = Color.White.copy(alpha = baseAlpha),
                                radius = (4f + (i % 4) * 3f),
                                center = Offset(currentX, yPos)
                            )
                        }
                        
                        // Draw Path
                        val screenWidthPx = screenWidthDp.toPx()
                        val amplitudePx = (screenWidthPx / 2f) - 80.dp.toPx()
                        val spacingPx = levelSpacingDp.toPx()
                        
                        val path = Path()
                        val completedPath = Path()

                        for (i in 0 until levels.size - 1) {
                            val currentLevel = levels[i]
                            val isCompleted = currentLevel.completed

                            val x1 = (screenWidthPx / 2f) + (sin(i * 0.9) * amplitudePx).toFloat()
                            val y1 = size.height - bottomPaddingDp.toPx() - (i * spacingPx)
                            
                            val x2 = (screenWidthPx / 2f) + (sin((i + 1) * 0.9) * amplitudePx).toFloat()
                            val y2 = size.height - bottomPaddingDp.toPx() - ((i + 1) * spacingPx)

                            val cp1x = x1
                            val cp1y = y1 - (spacingPx / 2f)
                            val cp2x = x2
                            val cp2y = y2 + (spacingPx / 2f)

                            if (i == 0) {
                                path.moveTo(x1, y1)
                                completedPath.moveTo(x1, y1)
                            } else {
                                path.moveTo(x1, y1)
                                if (isCompleted) {
                                    completedPath.moveTo(x1, y1)
                                }
                            }
                            
                            path.cubicTo(cp1x, cp1y, cp2x, cp2y, x2, y2)
                            if (isCompleted) {
                                completedPath.cubicTo(cp1x, cp1y, cp2x, cp2y, x2, y2)
                            }
                        }

                        // Path Styles
                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = 0.3f),
                            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawPath(
                            path = completedPath,
                            color = TileCorrect,
                            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Nodes
                    levels.forEachIndexed { index, level ->
                        val isUnlocked = level.levelNumber == 1 || levels.any { prev -> prev.levelNumber == level.levelNumber - 1 && prev.completed }
                        val isBoss = BossRegistry.isBossLevel(level.levelNumber)
                        val isCurrentLevel = level.levelNumber == highestUnlocked

                        // Calculate position
                        val xOffsetDp = with(density) {
                            val screenWidthPx = screenWidthDp.toPx()
                            val amplitudePx = (screenWidthPx / 2f) - 80.dp.toPx()
                            val cx = (screenWidthPx / 2f) + (sin(index * 0.9) * amplitudePx).toFloat()
                            cx.toDp() - (nodeSizeDp / 2)
                        }
                        val yOffsetDp = totalHeightDp - bottomPaddingDp - (levelSpacingDp * index.toFloat()) - (nodeSizeDp / 2)
                        
                        // Star Gate Logic for Bosses at World Ends (10, 20, 30, 40, 50)
                        val isWorldEnd = level.levelNumber % 10 == 0
                        val requiredStars = if (isWorldEnd) (level.levelNumber - 1) * 2 else 0 // E.g., Lvl 10 needs 18 stars
                        val isStarLocked = isWorldEnd && totalStars < requiredStars && !level.completed

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(xOffsetDp.roundToPx(), yOffsetDp.roundToPx()) }
                                .size(nodeSizeDp)
                                .scale(if (isCurrentLevel && !isStarLocked) pulseScale else 1f)
                        ) {
                            // Player Token (Current Position Indicator)
                            if (isCurrentLevel && !isStarLocked) {
                                Text(
                                    text = "🚀",
                                    fontSize = 40.sp,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = (-45 + avatarBob).dp)
                                        .shadow(8.dp, CircleShape)
                                )
                            }

                            // The Level Node
                            LevelNode(
                                record = level,
                                isUnlocked = isUnlocked,
                                isBoss = isBoss,
                                isStarLocked = isStarLocked,
                                requiredStars = requiredStars,
                                onClick = {
                                    if (isStarLocked && isUnlocked) {
                                        Toast.makeText(context, "You need $requiredStars stars to unlock this gate! (You have $totalStars)", Toast.LENGTH_LONG).show()
                                    } else if (isUnlocked) {
                                        onSelectLevel(level.levelNumber)
                                    }
                                }
                            )

                            // Treasure Chest Node (Rendered next to Boss nodes visually)
                            if (isBoss && level.completed) {
                                Text(
                                    text = "🎁",
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .offset(x = 35.dp, y = (-20).dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Re-render Top App Bar so it floats over the gradient properly
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Adventure", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Star, contentDescription = "Total Stars", tint = TileMisplaced)
                        Text(" $totalStars", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.3f),
                ),
            )
        }
    }
}

@Composable
private fun LevelNode(
    record: LevelRecord,
    isUnlocked: Boolean,
    isBoss: Boolean,
    isStarLocked: Boolean,
    requiredStars: Int,
    onClick: () -> Unit,
) {
    val bossColor = Color(0xFFE53935)
    
    val bgColor = when {
        record.completed -> if (isBoss) bossColor else TileCorrect
        isUnlocked -> if (isStarLocked) Color.DarkGray else if (isBoss) bossColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primaryContainer
        else -> Color.DarkGray.copy(alpha = 0.8f)
    }
    
    val textColor = when {
        record.completed -> Color.White
        isUnlocked -> if (isBoss || isStarLocked) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
        else -> Color.LightGray.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(if (isUnlocked) 8.dp else 2.dp, CircleShape)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(enabled = isUnlocked, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isUnlocked) {
                if (isStarLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Star Gate",
                        tint = TileMisplaced,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$requiredStars",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = TileMisplaced,
                    )
                } else {
                    if (isBoss) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Boss",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "${record.levelNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                    )
                    if (record.completed) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            for (i in 1..3) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i <= record.stars) TileMisplaced else Color.Black.copy(alpha = 0.2f),
                                    modifier = Modifier.size(10.dp),
                                )
                            }
                        }
                    }
                }
            } else {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = textColor,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}
