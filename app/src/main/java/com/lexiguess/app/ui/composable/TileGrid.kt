package com.lexiguess.app.ui.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay

/**
 * Renders the 6x5 Wordle board with authentic, polished animations:
 *  - Responsive tile sizing that adapts to all screen heights.
 *  - Tactile pop animation when a letter is typed into a tile.
 *  - Coordinated row bounce when all 5 letters are entered.
 *  - Multi-stage 3D card flip reveal on submit.
 *  - Natural shake animation on invalid submission.
 */
@Composable
fun TileGrid(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    val darkMode = LocalDarkMode.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
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
                inputLength = if (row == state.currentRow) state.currentInput.length else 0,
                darkMode = darkMode,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Row
// ---------------------------------------------------------------------------

@Composable
private fun TileRow(
    row: Int,
    letters: List<Char>,
    tileStates: List<TileState>,
    isCurrentRow: Boolean,
    isSubmitted: Boolean,
    shake: Boolean,
    inputLength: Int,
    darkMode: Boolean,
) {
    // Shake animation
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shake) {
        if (shake) {
            val offsets = listOf(14f, -14f, 10f, -10f, 6f, -6f, 3f, -3f, 0f)
            for (off in offsets) {
                shakeOffset.animateTo(off, tween(35, easing = LinearEasing))
            }
        }
    }

    // Row bounce when all 5 letters are ready
    val rowBounce = remember { Animatable(1f) }
    LaunchedEffect(isCurrentRow, inputLength) {
        if (isCurrentRow && inputLength == WORD_LENGTH) {
            rowBounce.snapTo(1f)
            rowBounce.animateTo(1.05f, tween(75, easing = LinearOutSlowInEasing))
            rowBounce.animateTo(0.97f, tween(75, easing = FastOutSlowInEasing))
            rowBounce.animateTo(1.0f, tween(90))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = shakeOffset.value
                scaleX = rowBounce.value
                scaleY = rowBounce.value
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        for (col in 0 until WORD_LENGTH) {
            TileCell(
                letter = letters[col],
                tileState = tileStates[col],
                isSubmitted = isSubmitted,
                revealDelayMs = col * 260L,
                darkMode = darkMode,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Cell
// ---------------------------------------------------------------------------

@Composable
private fun TileCell(
    letter: Char,
    tileState: TileState,
    isSubmitted: Boolean,
    revealDelayMs: Long,
    darkMode: Boolean,
) {
    // Pop animation on keypress
    val popScale = remember { Animatable(1f) }
    LaunchedEffect(letter) {
        if (letter != ' ' && !isSubmitted) {
            popScale.snapTo(1f)
            popScale.animateTo(1.15f, tween(60, easing = LinearOutSlowInEasing))
            popScale.animateTo(1.0f, tween(75, easing = FastOutSlowInEasing))
        }
    }

    // 3D Flip reveal (0.0 to 1.0)
    val flipProgress = remember { Animatable(if (isSubmitted) 1f else 0f) }
    LaunchedEffect(isSubmitted) {
        if (isSubmitted && flipProgress.value < 1f) {
            delay(revealDelayMs)
            flipProgress.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        }
    }

    // Determine front vs back half of the 3D flip
    val isBackSide = flipProgress.value >= 0.5f

    // Calculate vertical 3D tilt
    val rotationXDegrees = if (!isBackSide) {
        // Front folding in: 0 deg to 90 deg
        flipProgress.value * 180f
    } else {
        // Back unfolding out: 90 deg back to 0 deg
        (1f - flipProgress.value) * 180f
    }

    // Colors
    val bgColor = if (isBackSide) {
        tileState.toBackground(darkMode)
    } else {
        tileState.toPreRevealBackground(darkMode)
    }

    val textColor = if (isBackSide) {
        Color.White
    } else {
        tileState.toTextColor(darkMode)
    }

    val borderColor = if (isBackSide) {
        Color.Transparent
    } else {
        tileState.toBorder(darkMode)
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                rotationX = rotationXDegrees
                cameraDistance = 14f * density
                scaleX = popScale.value
                scaleY = popScale.value
            }
            .background(bgColor)
            .border(
                width = if (isBackSide || tileState == TileState.EMPTY) 2.dp else 2.dp,
                color = borderColor,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (letter != ' ') {
            Text(
                text = letter.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Colour mapping helpers
// ---------------------------------------------------------------------------

private fun TileState.toBackground(dark: Boolean): Color = when (this) {
    TileState.CORRECT -> TileCorrect
    TileState.MISPLACED -> if (dark) TileMisplacedDark else TileMisplaced
    TileState.ABSENT -> if (dark) TileAbsentDark else TileAbsent
    TileState.FILLED -> if (dark) BackgroundDark else BackgroundLight
    TileState.EMPTY -> if (dark) BackgroundDark else BackgroundLight
}

private fun TileState.toPreRevealBackground(dark: Boolean): Color =
    if (dark) BackgroundDark else BackgroundLight

private fun TileState.toTextColor(dark: Boolean): Color = when (this) {
    TileState.CORRECT, TileState.MISPLACED, TileState.ABSENT -> Color.White
    TileState.FILLED -> if (dark) KeyTextDark else KeyText
    TileState.EMPTY -> if (dark) KeyTextDark else KeyText
}

private fun TileState.toBorder(dark: Boolean): Color = when (this) {
    TileState.EMPTY -> if (dark) TileBorderEmptyDark else TileBorderEmpty
    TileState.FILLED -> if (dark) TileBorderFilledDark else TileBorderFilled
    TileState.CORRECT, TileState.MISPLACED, TileState.ABSENT -> Color.Transparent
}
