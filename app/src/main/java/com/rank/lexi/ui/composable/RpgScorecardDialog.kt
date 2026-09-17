package com.rank.lexi.ui.composable

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rank.lexi.domain.model.TileState
import com.rank.lexi.ui.theme.TileCorrect
import com.rank.lexi.ui.theme.TileMisplaced

data class RpgRankInfo(
    val rank: String,
    val title: String,
    val color: Color,
    val gradientColors: List<Color>,
)

object RpgGradeCalculator {
    fun calculateRank(won: Boolean, attempts: Int): RpgRankInfo {
        if (!won) {
            return RpgRankInfo(
                rank = "F",
                title = "Fallen",
                color = Color(0xFFEF5350),
                gradientColors = listOf(Color(0xFFE53935), Color(0xFFB71C1C)),
            )
        }
        return when (attempts) {
            1 -> RpgRankInfo(
                rank = "S+",
                title = "Legendary",
                color = Color(0xFFFFD700),
                gradientColors = listOf(Color(0xFFFFEA00), Color(0xFFFF9100)),
            )
            2 -> RpgRankInfo(
                rank = "S",
                title = "Grandmaster",
                color = Color(0xFFFFD700),
                gradientColors = listOf(Color(0xFFFFD700), Color(0xFFFF8F00)),
            )
            3 -> RpgRankInfo(
                rank = "A",
                title = "Master",
                color = Color(0xFF00E676),
                gradientColors = listOf(Color(0xFF00E676), Color(0xFF00897B)),
            )
            4 -> RpgRankInfo(
                rank = "B",
                title = "Scholar",
                color = Color(0xFF29B6F6),
                gradientColors = listOf(Color(0xFF29B6F6), Color(0xFF0277BD)),
            )
            5 -> RpgRankInfo(
                rank = "C",
                title = "Adept",
                color = Color(0xFFFFB74D),
                gradientColors = listOf(Color(0xFFFFB74D), Color(0xFFEF6C00)),
            )
            else -> RpgRankInfo(
                rank = "D",
                title = "Survivor",
                color = Color(0xFF90A4AE),
                gradientColors = listOf(Color(0xFF90A4AE), Color(0xFF455A64)),
            )
        }
    }

    fun calculateSkill(steps: List<GuessStepAnalysis>, won: Boolean): Int {
        if (steps.isEmpty()) return if (won) 85 else 30
        val avgPrune = steps.map { it.eliminationPercentage }.average()
        val baseScore = avgPrune.toInt()
        val bonus = if (won) 10 else -15
        return (baseScore + bonus).coerceIn(15, 99)
    }

    fun calculateLuck(steps: List<GuessStepAnalysis>, attempts: Int, won: Boolean): Int {
        val firstStep = steps.firstOrNull()
        val greenHits = firstStep?.rowStates?.count { it == TileState.CORRECT } ?: 0
        val yellowHits = firstStep?.rowStates?.count { it == TileState.MISPLACED } ?: 0
        val luckPoints = (greenHits * 28) + (yellowHits * 12) + ((6 - attempts).coerceAtLeast(0) * 6)
        val score = if (won) luckPoints + 20 else luckPoints
        return score.coerceIn(10, 98)
    }
}

@Composable
fun RpgScorecardDialog(
    targetWord: String,
    won: Boolean,
    attempts: Int,
    maxAttempts: Int = 6,
    solveDurationMs: Long = 0L,
    steps: List<GuessStepAnalysis>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val rankInfo = remember(won, attempts) { RpgGradeCalculator.calculateRank(won, attempts) }
    val skillScore = remember(steps, won) { RpgGradeCalculator.calculateSkill(steps, won) }
    val luckScore = remember(steps, attempts, won) { RpgGradeCalculator.calculateLuck(steps, attempts, won) }

    val durationSec = (solveDurationMs / 1000).coerceAtLeast(0)
    val durationText = "%02d:%02d".format(durationSec / 60, durationSec % 60)

    val eliminatedCount = remember(steps) {
        val first = steps.firstOrNull()?.previousCandidates ?: 0
        val last = steps.lastOrNull()?.remainingCandidates ?: 0
        (first - last).coerceAtLeast(0)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Rank Crest Badge
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(rankInfo.gradientColors))
                        .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = rankInfo.rank,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                        )
                        Text(
                            text = rankInfo.title.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 1.sp,
                        )
                    }
                }

                // Target Word
                Text(
                    text = "TARGET: $targetWord",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Stats Metrics Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ScorecardMeterRow(
                        label = "Skill Rating",
                        value = "$skillScore / 100",
                        progress = skillScore / 100f,
                        color = TileCorrect,
                        subtitle = "Pruning efficiency & candidate elimination",
                    )

                    ScorecardMeterRow(
                        label = "Luck Factor",
                        value = "$luckScore / 100",
                        progress = luckScore / 100f,
                        color = TileMisplaced,
                        subtitle = "Early clue discovery probability",
                    )
                }

                // Match details grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    StatSummaryColumn(label = "TURNS", value = "$attempts / $maxAttempts")
                    StatSummaryColumn(label = "TIME", value = durationText)
                    StatSummaryColumn(label = "PRUNED", value = "$eliminatedCount")
                }

                // Share button
                Button(
                    onClick = {
                        val scorecardText = buildString {
                            appendLine("LexiGuess")
                            appendLine("Word: $targetWord")
                            appendLine("Rank: ${rankInfo.rank} (${rankInfo.title})")
                            appendLine("Turns: $attempts / $maxAttempts")
                            appendLine("Skill: $skillScore/100 | Luck: $luckScore/100")
                            appendLine("Time: $durationText")
                            appendLine("#LexiGuess")
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("LexiGuess Scorecard", scorecardText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Scorecard copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = rankInfo.color,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Scorecard",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScorecardMeterRow(
    label: String,
    value: String,
    progress: Float,
    color: Color,
    subtitle: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                color = color,
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatSummaryColumn(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
