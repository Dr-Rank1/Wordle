package com.lexiguess.app.ui.composable

import androidx.compose.animation.animateColorAsState
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
 * Renders the 6x5 Wordle board.
 *
 * Animations:
 *  - Tiles pop (scale up then down) when a letter is typed.
 *  - Submitted rows flip column-by-column; the reveal colour cross-fades in
 *    during the back-half of each flip rather than snapping.
 *  - When all 5 letters are entered the row bounces to signal it is ready
 *    to submit.
 *  - The current row shakes when an invalid word is submitted.
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
    // ---- Shake animation ---------------------------------------------------
    val shakeOffset by produceState(0f, shake) {
        if (shake) {
            for (offset in listOf(12f, -12f, 9f, -9f, 6f, -6f, 3f, -3f, 0f)) {
                value = offset
                delay(45)
            }
        } else {
            value = 0f
        }
    }

    // ---- Bounce animation when row is full ---------------------------------
    // Key on inputLength reaching WORD_LENGTH while the row is the current one
    val bounceKey = remember { mutableStateOf(0) }
    LaunchedEffect(isCurrentRow, inputLength) {
        if (isCurrentRow && inputLength == WORD_LENGTH) {
            bounceKey.value++
        }
    }
    val bounceScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = keyframes {
            durationMillis = 300
            1.0f at 0
            1.05f at 80
            0.97f at 160
            1.0f at 300
        },
        label = "row_bounce",
    )
    // Re-trigger by using the key
    val (actualBounce) = remember(bounceKey.value) { mutableStateOf(bounceScale) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = shakeOffset
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
    ) {
        for (col in 0 until WORD_LENGTH) {
            TileCell(
                letter = letters[col],
                tileState = tileStates[col],
                isSubmitted = isSubmitted,
                revealDelayMs = col * 300L,
                bounceKey = bounceKey.value,
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
    bounceKey: Int,
    darkMode: Boolean,
) {
    // ---- Flip animation ----------------------------------------------------
    // Tracks whether THIS cell has completed its flip reveal.
    var flipped by remember { mutableStateOf(false) }
    LaunchedEffect(isSubmitted) {
        if (isSubmitted && !flipped) {
            delay(revealDelayMs)
            flipped = true
        }
    }

    val rotationX by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "flip",
    )

    // Show result colour only after the flip passes 90°
    val showResult = rotationX > 90f

    // ---- Smooth colour cross-fade ------------------------------------------
    // Target colour changes as flipped transitions and showResult becomes true.
    val targetBgColor = if (showResult) tileState.toBackground(darkMode)
    else tileState.toPreRevealBackground(darkMode)

    val bgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 120),
        label = "tile_color",
    )

    val targetTextColor = if (showResult || tileState == TileState.ABSENT ||
        tileState == TileState.CORRECT || tileState == TileState.MISPLACED
    ) Color.White
    else tileState.toTextColor(darkMode)

    val textColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(durationMillis = 120),
        label = "text_color",
    )

    val borderColor = tileState.toBorder(darkMode)

    // ---- Pop animation when letter is typed --------------------------------
    // Triggered every time letter changes from ' ' to a char.
    var popKey by remember { mutableStateOf(0) }
    val prevLetter = remember { mutableStateOf(letter) }
    if (letter != ' ' && prevLetter.value == ' ') {
        popKey++
    }
    prevLetter.value = letter

    val popScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = keyframes {
            durationMillis = 120
            1.0f at 0
            1.15f at 50
            1.0f at 120
        },
        label = "pop_$popKey",
    )
    // Re-reading popKey ensures recomposition fires and the keyframe restarts
    val effectivePop = remember(popKey) { popScale }

    // ---- Bounce animation for full row ------------------------------------
    val bounceScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = keyframes {
            durationMillis = 280
            1.0f at 0
            1.06f at 70
            0.97f at 150
            1.0f at 280
        },
        label = "bounce_$bounceKey",
    )
    val effectiveBounce = remember(bounceKey) { bounceScale }

    val finalScale = when {
        !isSubmitted && letter != ' ' -> effectivePop * effectiveBounce
        else -> 1f
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                // Flip
                this.rotationX = if (rotationX <= 90f) rotationX else 180f - rotationX
                cameraDistance = 12f * density
                // Pop / bounce scale
                scaleX = finalScale
                scaleY = finalScale
            }
            .background(bgColor)
            .border(
                width = when (tileState) {
                    TileState.EMPTY, TileState.FILLED -> 2.dp
                    else -> 0.dp
                },
                color = borderColor,
            ),
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

// ---------------------------------------------------------------------------
// Colour helpers
// ---------------------------------------------------------------------------

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

private fun TileState.toTextColor(dark: Boolean): Color =
    if (dark) Color.White else Color.Black
