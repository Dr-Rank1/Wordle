package com.rank.lexi.ui.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BossHealthBar(
    bossName: String,
    bossTitle: String,
    currentHp: Int,
    maxHp: Int,
    modifierDescription: String,
    timeRemainingSeconds: Int? = null,
    modifier: Modifier = Modifier,
) {
    val hpFraction by animateFloatAsState(
        targetValue = (currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "hpFraction",
    )

    val hpBarColor by animateColorAsState(
        targetValue = when {
            hpFraction > 0.5f -> MaterialTheme.colorScheme.primary
            hpFraction > 0.2f -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.error
        },
        label = "hpBarColor",
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(bossName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = bossTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = if (timeRemainingSeconds != null) "${timeRemainingSeconds}s" else "$currentHp / $maxHp",
                    style = MaterialTheme.typography.labelLarge,
                    color = hpBarColor,
                )
            }

            LinearProgressIndicator(
                progress = { hpFraction },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = hpBarColor,
                trackColor = MaterialTheme.colorScheme.surface,
            )

            Text(
                text = modifierDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
}
