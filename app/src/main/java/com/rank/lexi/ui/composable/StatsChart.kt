package com.rank.lexi.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Horizontal bar chart showing how many times the player won in 1–6 guesses.
 *
 * @param distribution Map of guess count (1-6) to number of wins.
 * @param lastAttempts  The number of guesses used in the most recent game
 *                      (highlights that bar in green). Null if last game was lost.
 */
@Composable
fun StatsChart(
    distribution: Map<Int, Int>,
    lastAttempts: Int? = null,
    modifier: Modifier = Modifier,
) {
    val maxCount = distribution.values.maxOrNull()?.takeIf { it > 0 } ?: 1

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (attempts in 1..6) {
            val count = distribution[attempts] ?: 0
            val fraction = count.toFloat() / maxCount
            val highlight = lastAttempts == attempts

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "$attempts",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(20.dp),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceAtLeast(0.06f))
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (highlight) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        text = "$count",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (highlight) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }
        }
    }
}
