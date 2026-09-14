package com.lexiguess.app.ui.composable

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lexiguess.app.domain.model.GameState
import com.lexiguess.app.domain.model.GameState.Companion.MAX_ROWS
import com.lexiguess.app.domain.model.GameStatus
import com.lexiguess.app.domain.model.TileState

/**
 * Share button that builds a standard Wordle-style emoji grid and sends it to
 * the Android share sheet. Works across any word length (4, 5, 6, 7).
 */
@Composable
fun ShareButton(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    if (state.status == GameStatus.IN_PROGRESS) return

    val context = LocalContext.current

    Button(
        onClick = {
            val text = buildShareText(state)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "Share your result"))
        },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(48.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "Share",
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Share",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

private fun buildShareText(state: GameState): String {
    val attemptsLabel = if (state.status == GameStatus.WON) "${state.currentRow}" else "X"
    val header = "LexiGuess $attemptsLabel/$MAX_ROWS (${state.wordLength} Letters)"

    val rows = (0 until state.currentRow).joinToString("\n") { row ->
        (0 until state.wordLength).joinToString("") { col ->
            state.board[row].getOrElse(col) { TileState.EMPTY }.toEmoji()
        }
    }

    return "$header\n\n$rows"
}

private fun TileState.toEmoji(): String = when (this) {
    TileState.CORRECT -> "\uD83D\uDFE9"    // green square
    TileState.MISPLACED -> "\uD83D\uDFE8"  // yellow square
    else -> "\u2B1B"                        // black square
}
