package com.rank.lexi.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon

/**
 * Wordly-style large game mode card with full-bleed gradient background,
 * emoji icon badge, title, subtitle, and optional CTA chip.
 *
 * @param title Mode name displayed in bold white.
 * @param subtitle Secondary description text.
 * @param icon Large icon displayed top-right.
 * @param gradientStart Start colour for the card gradient (darker).
 * @param gradientEnd End colour for the card gradient (lighter/brighter).
 * @param modifier Layout modifier.
 * @param ctaLabel Optional call-to-action label on a pill chip (e.g. "PLAY", "CONTINUE").
 * @param onClick Click handler.
 */
@Composable
fun GameModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientStart: Color,
    gradientEnd: Color,
    modifier: Modifier = Modifier,
    ctaLabel: String? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(gradientEnd.copy(alpha = 0.85f), gradientStart),
                )
            )
            .clickable(
                onClickLabel = "$title. $subtitle",
                onClick = onClick,
            )
            .semantics {
                role = Role.Button
                contentDescription = "$title. $subtitle"
            }
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        // Icon badge top-right
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(36.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
            )

            if (ctaLabel != null) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color.White.copy(alpha = 0.20f),
                    modifier = Modifier,
                ) {
                    Text(
                        text = ctaLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

/**
 * Small icon-tile variant for secondary actions (vault, quests, stats, etc.).
 */
@Composable
fun SmallActionTile(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
