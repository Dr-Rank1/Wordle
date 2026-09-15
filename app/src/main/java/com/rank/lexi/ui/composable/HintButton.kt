package com.rank.lexi.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A single-use hint button.
 *
 * When [hintUsed] is true the button is disabled and shown as spent.
 * Tapping it calls [onHint] in the ViewModel which reveals one correct-position
 * letter in a toast message.
 */
@Composable
fun HintButton(
    hintUsed: Boolean,
    onHint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onHint,
        enabled = !hintUsed,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(40.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Lightbulb,
            contentDescription = "Hint",
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (hintUsed) "Hint used" else "Hint",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
