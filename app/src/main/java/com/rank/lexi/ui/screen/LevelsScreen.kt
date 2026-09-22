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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.rank.lexi.ui.theme.CoinGold
import com.rank.lexi.ui.theme.TileCorrect
import com.rank.lexi.ui.theme.TileMisplaced
import kotlin.math.roundToInt
import kotlin.math.sin

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface

private data class StarParticle(
    val relX: Float,
    val yPx: Float,
    val radius: Float,
    val alpha: Float,
)

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

    if (levels.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF120A2A))
        )
        return
    }

    val scrollState = rememberScrollState()
    val config = LocalConfiguration.current
    val density = LocalDensity.current

    // Dimensions
    val levelSpacingDp = 110.dp
    val nodeSizeDp = 64.dp
    val topPaddingDp = 130.dp
    val bottomPaddingDp = 100.dp
    
    val totalHeightDp = topPaddingDp + bottomPaddingDp + (levelSpacingDp * (levels.size - 1))
    
    // Highest unlocked level
    val highestUnlocked = remember(levels) {
        levels.lastOrNull { 
            it.levelNumber == 1 || levels.any { prev -> prev.levelNumber == it.levelNumber - 1 && prev.completed } 
        }?.levelNumber ?: 1
    }

    // Scroll to the highest unlocked level automatically
    LaunchedEffect(highestUnlocked, levels.size) {
        val targetIndex = (highestUnlocked - 1).coerceIn(0, (levels.size - 1).coerceAtLeast(0))
        val yOffsetDp = topPaddingDp + (levelSpacingDp * (levels.size - 1 - targetIndex))
        val targetScrollY = with(density) {
            (yOffsetDp - (config.screenHeightDp.dp / 2)).roundToPx()
        }
        scrollState.scrollTo(targetScrollY.coerceAtLeast(0))
    }

    // 5 Worlds Gradient (Cosmos -> Sky -> Ocean -> Desert -> Forest)
    // Since Y=0 is the TOP (Level 50), Cosmos is at 0.0, Forest is at 1.0
    val worldGradient = remember {
        Brush.verticalGradient(
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
    }

    // Light & smooth pulse animation for active level only
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val avatarBob by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_bob"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenWidthDp = maxWidth
        val screenWidthPx = with(density) { screenWidthDp.toPx() }
        val horizontalMarginPx = with(density) { 72.dp.toPx() }
        val amplitudePx = ((screenWidthPx / 2f) - horizontalMarginPx).coerceAtLeast(0f)
        val spacingPx = with(density) { levelSpacingDp.toPx() }
        val totalHeightPx = with(density) { totalHeightDp.toPx() }
        val bottomPaddingPx = with(density) { bottomPaddingDp.toPx() }

        // Precompute path geometry to avoid heavy re-allocation on every frame/scroll
        val (path, completedPath, starParticles) = remember(levels, screenWidthDp, totalHeightDp, density) {
            val p = Path()
            val cp = Path()

            for (i in 0 until levels.size - 1) {
                val currentLevel = levels[i]
                val isCompleted = currentLevel.completed

                val x1 = (screenWidthPx / 2f) + (sin(i * 0.9) * amplitudePx).toFloat()
                val y1 = totalHeightPx - bottomPaddingPx - (i * spacingPx)
                
                val x2 = (screenWidthPx / 2f) + (sin((i + 1) * 0.9) * amplitudePx).toFloat()
                val y2 = totalHeightPx - bottomPaddingPx - ((i + 1) * spacingPx)

                val cp1x = x1
                val cp1y = y1 - (spacingPx / 2f)
                val cp2x = x2
                val cp2y = y2 + (spacingPx / 2f)

                if (i == 0) {
                    p.moveTo(x1, y1)
                    cp.moveTo(x1, y1)
                } else {
                    p.moveTo(x1, y1)
                    if (isCompleted) {
                        cp.moveTo(x1, y1)
                    }
                }
                
                p.cubicTo(cp1x, cp1y, cp2x, cp2y, x2, y2)
                if (isCompleted) {
                    cp.cubicTo(cp1x, cp1y, cp2x, cp2y, x2, y2)
                }
            }

            // Generate ambient stars once instead of continuous infinite recalculations
            val particles = (0..50).map { i ->
                StarParticle(
                    relX = ((i * 73f) % 1000f) / 1000f,
                    yPx = (totalHeightPx / 50f) * i,
                    radius = 3f + (i % 3) * 2f,
                    alpha = 0.2f + (i % 4) * 0.08f,
                )
            }

            Triple(p, cp, particles)
        }

        // 1. Scrollable Map Background & Track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeightDp)
                    .background(worldGradient)
            ) {
                // Background Track and Ambient World Stars
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (particle in starParticles) {
                        drawCircle(
                            color = Color.White.copy(alpha = particle.alpha),
                            radius = particle.radius,
                            center = Offset(particle.relX * size.width, particle.yPx)
                        )
                    }

                    // Path Styles
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.3f),
                        style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = completedPath,
                        color = TileCorrect,
                        style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Nodes
                levels.forEachIndexed { index, level ->
                    val isUnlocked = level.levelNumber == 1 || levels.any { prev -> prev.levelNumber == level.levelNumber - 1 && prev.completed }
                    val isBoss = BossRegistry.isBossLevel(level.levelNumber)
                    val isCurrentLevel = level.levelNumber == highestUnlocked

                    // Calculate position safely within screen bounds
                    val cx = (screenWidthPx / 2f) + (sin(index * 0.9) * amplitudePx).toFloat()
                    val xOffsetDp = with(density) { cx.toDp() } - (nodeSizeDp / 2)
                    val yOffsetDp = totalHeightDp - bottomPaddingDp - (levelSpacingDp * index.toFloat()) - (nodeSizeDp / 2)
                    
                    // Star Gate Logic for Bosses at World Ends (10, 20, 30, 40, 50)
                    val isWorldEnd = level.levelNumber % 10 == 0
                    val requiredStars = if (isWorldEnd) (level.levelNumber - 1) * 2 else 0
                    val isStarLocked = isWorldEnd && totalStars < requiredStars && !level.completed

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(xOffsetDp.roundToPx(), yOffsetDp.roundToPx()) }
                            .size(nodeSizeDp)
                            .scale(if (isCurrentLevel && !isStarLocked) pulseScale else 1f)
                    ) {
                        // Player Token (Current Position Indicator: Sleek Badge instead of emoji)
                        if (isCurrentLevel && !isStarLocked) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-36 + avatarBob).dp)
                                    .size(32.dp)
                                    .shadow(8.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(CoinGold),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Current Position",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(180f),
                                )
                            }
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

                        // Boss Clear Reward Badge (Placed neatly on top-right shoulder of the node to avoid clipping)
                        if (isBoss && level.completed) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-6).dp)
                                    .size(24.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(CoinGold),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = "Boss Reward",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Floating Sleek Top App Bar (extends behind status bar without empty gap)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = Color.Black.copy(alpha = 0.72f),
            shadowElevation = 6.dp,
        ) {
            TopAppBar(
                title = { 
                    Text(
                        text = "Adventure",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp,
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Total Stars",
                            tint = TileMisplaced,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalStars",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                windowInsets = WindowInsets.statusBars,
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
