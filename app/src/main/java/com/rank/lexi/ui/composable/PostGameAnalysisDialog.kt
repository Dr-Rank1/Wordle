package com.rank.lexi.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rank.lexi.domain.model.TileState
import com.rank.lexi.ui.theme.TileCorrect
import com.rank.lexi.ui.theme.TileMisplaced

data class GuessStepAnalysis(
    val roundNumber: Int,
    val guessWord: String,
    val rowStates: List<TileState>,
    val remainingCandidates: Int,
    val previousCandidates: Int,
) {
    val eliminationPercentage: Float
        get() = if (previousCandidates <= 0) 100f
        else {
            val calc = (previousCandidates - remainingCandidates).toFloat() / previousCandidates * 100f
            if (calc.isNaN()) 0f else calc.coerceIn(0f, 100f)
        }
}

private data class MoveEvaluation(
    val label: String,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
)

private fun evaluateMove(step: GuessStepAnalysis): MoveEvaluation {
    val elim = step.eliminationPercentage
    return when {
        step.remainingCandidates <= 1 -> MoveEvaluation(
            label = "Winning Move",
            containerColor = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.2f),
            contentColor = androidx.compose.ui.graphics.Color(0xFF10B981),
        )
        elim >= 85f -> MoveEvaluation(
            label = "Brilliant",
            containerColor = androidx.compose.ui.graphics.Color(0xFF6366F1).copy(alpha = 0.2f),
            contentColor = androidx.compose.ui.graphics.Color(0xFF818CF8),
        )
        elim >= 60f -> MoveEvaluation(
            label = "Strong",
            containerColor = androidx.compose.ui.graphics.Color(0xFF0284C7).copy(alpha = 0.2f),
            contentColor = androidx.compose.ui.graphics.Color(0xFF38BDF8),
        )
        elim >= 35f -> MoveEvaluation(
            label = "Fair",
            containerColor = androidx.compose.ui.graphics.Color(0xFFD97706).copy(alpha = 0.2f),
            contentColor = androidx.compose.ui.graphics.Color(0xFFFBBF24),
        )
        else -> MoveEvaluation(
            label = "Inefficient",
            containerColor = androidx.compose.ui.graphics.Color(0xFFE11D48).copy(alpha = 0.2f),
            contentColor = androidx.compose.ui.graphics.Color(0xFFFB7185),
        )
    }
}

@Composable
fun PostGameAnalysisDialog(
    targetWord: String,
    steps: List<GuessStepAnalysis>,
    onDismiss: () -> Unit,
) {
    val avgElimination = if (steps.isNotEmpty()) steps.map { it.eliminationPercentage }.average().toFloat() else 0f
    val overallRating = when {
        steps.size <= 3 && (steps.lastOrNull()?.remainingCandidates ?: 0) <= 1 -> "Mastermind" to androidx.compose.ui.graphics.Color(0xFF10B981)
        avgElimination >= 75f -> "Strategic Pro" to androidx.compose.ui.graphics.Color(0xFF6366F1)
        avgElimination >= 50f -> "Sharp Solver" to androidx.compose.ui.graphics.Color(0xFF0284C7)
        else -> "Determined" to androidx.compose.ui.graphics.Color(0xFFD97706)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "LEXI BOT COACH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TileCorrect,
                            letterSpacing = 1.5.sp,
                        )
                        Text(
                            text = targetWord.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = overallRating.second.copy(alpha = 0.16f),
                    ) {
                        Text(
                            text = overallRating.first,
                            color = overallRating.second,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = targetWord.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TileCorrect,
                            )
                            Text(
                                text = "Target Word",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${steps.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                text = "Attempts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        val lastElim = steps.lastOrNull()?.remainingCandidates ?: 0
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (lastElim <= 1) "100%" else "$lastElim Left",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TileMisplaced,
                            )
                            Text(
                                text = "Resolution",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Text(
                    text = "Turn-by-Turn Move Evaluation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(steps) { index, step ->
                        val eval = evaluateMove(step)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "#${step.roundNumber}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = step.guessWord.uppercase(),
                                                fontWeight = FontWeight.Black,
                                                style = MaterialTheme.typography.bodyLarge,
                                                letterSpacing = 1.sp,
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = eval.containerColor,
                                            ) {
                                                Text(
                                                    text = eval.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = eval.contentColor,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                )
                                            }
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            modifier = Modifier.padding(top = 3.dp),
                                        ) {
                                            step.rowStates.forEach { tileState ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(
                                                            when (tileState) {
                                                                TileState.CORRECT -> TileCorrect
                                                                TileState.MISPLACED -> TileMisplaced
                                                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                                            }
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${step.remainingCandidates} left",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (step.remainingCandidates <= 1) TileCorrect else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "-${"%.1f".format(step.eliminationPercentage)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = eval.contentColor,
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Close Analysis", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
