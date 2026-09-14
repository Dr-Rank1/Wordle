package com.lexiguess.app.ui.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexiguess.app.domain.model.GameState
import com.lexiguess.app.domain.model.GameState.Companion.MAX_ROWS
import com.lexiguess.app.domain.model.GameState.Companion.WORD_LENGTH
import com.lexiguess.app.domain.model.TileState
import com.lexiguess.app.ui.theme.*

/**
 * Renders the 6x5 Wordle board.
 *
 * Each submitted row flips its tiles one-by-one (column-staggered) to reveal
 * the result colours. The active (current-input) row shakes when an invalid
 * word is submitted.
 */
@Composable
fun TileGrid(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    val darkMode = LocalDarkMode.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (row in 0 until MAX_ROWS) {
            TileRow(
                row = row,
                letters = state.boardLetters[row],
                tileStates = state.board[row],
                isCurrentRow = row == state.currentRow,
                isSubmitted = row < state.currentRow,
                shake = state.shake && row == state.currentRow,
                darkMode = darkMode,
            )
        }
    }
}

@Composable
private fun TileRow(
    row: Int,
    letters: List<Char>,
    tileStates: List<TileState>,
    isCurrentRow: Boolean,
    isSubmitted: Boolean,
    shake: Boolean,
    darkMode: Boolean,
) {
    // Shake animation for the current row
    val shakeOffset by produceState(0f, shake) {
        if (shake) {
            val shakes = listOf(10f, -10f, 8f, -8f, 6f, -6f, 0f)
            for (offset in shakes) {
                value = offset
                kotlinx.coroutines.delay(50)
            }
        } else {
            value = 0f
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = shakeOffset },
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
    ) {
        for (col in 0 until WORD_LENGTH) {
            TileCell(
                letter = letters[col],
                tileState = tileStates[col],
                isSubmitted = isSubmitted,
                revealDelay = col * 250,
                darkMode = darkMode,
            )
        }
    }
}

@Composable
private fun TileCell(
    letter: Char,
    tileState: TileState,
    isSubmitted: Boolean,
    revealDelay: Int,
    darkMode: Boolean,
) {
    // Flip animation: only triggered once isSubmitted becomes true
    var flipped by remember { mutableStateOf(false) }
    LaunchedEffect(isSubmitted) {
        if (isSubmitted) {
            kotlinx.coroutines.delay(revealDelay.toLong())
            flipped = true
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "tile_flip",
    )

    // While the front face is visible (rotation < 90°) show FILLED/EMPTY colours;
    // once past 90° show the result colour.
    val showResult = rotation > 90f
    val bgColor = if (showResult) tileState.toBackground(darkMode)
    else tileState.toPreRevealBackground(darkMode)
    val borderColor = tileState.toBorder(darkMode)
    val textColor = if (showResult) Color.White else tileState.toTextColor(darkMode)

    Box(
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                rotationX = if (rotation <= 90f) rotation else 180f - rotation
                cameraDistance = 12f * density
            }
            .background(bgColor)
            .border(width = if (tileState == TileState.EMPTY) 2.dp else if (tileState == TileState.FILLED) 2.dp else 0.dp, color = borderColor),
        contentAlignment = Alignment.Center,
    ) {
        if (letter != ' ') {
            Text(
                text = letter.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        }
    }
}

// -------------------------------------------------------------------------
// Colour helpers
// -------------------------------------------------------------------------

private fun TileState.toBackground(dark: Boolean): Color = when (this) {
    TileState.CORRECT -> TileCorrect
    TileState.MISPLACED -> if (dark) TileMisplacedDark else TileMisplaced
    TileState.ABSENT -> if (dark) TileAbsentDark else TileAbsent
    TileState.FILLED -> if (dark) TileFilledDark else TileFilled
    TileState.EMPTY -> if (dark) TileEmptyDark else TileEmpty
}

private fun TileState.toPreRevealBackground(dark: Boolean): Color = when (this) {
    TileState.FILLED -> if (dark) TileFilledDark else TileFilled
    else -> if (dark) TileEmptyDark else TileEmpty
}

private fun TileState.toBorder(dark: Boolean): Color = when (this) {
    TileState.FILLED -> if (dark) TileBorderFilledDark else TileBorderFilled
    TileState.EMPTY -> if (dark) TileBorderEmptyDark else TileBorderEmpty
    else -> Color.Transparent
}

private fun TileState.toTextColor(dark: Boolean): Color = when (this) {
    TileState.FILLED -> if (dark) Color.White else Color.Black
    else -> if (dark) Color.White else Color.Black
}
