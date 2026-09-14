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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexiguess.app.domain.model.GameState
import com.lexiguess.app.domain.model.GameState.Companion.MAX_ROWS
import com.lexiguess.app.domain.model.TileState
import com.lexiguess.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Renders the multi-length Wordle board (4, 5, 6, or 7 letters).
 */
@Composable
fun TileGrid(
    state: GameState,
    onTileFlipSound: ((TileState, Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val darkMode = LocalDarkMode.current
    val wordLength = state.wordLength

    // Responsive tile size based on column count
    val tileSize: Dp = when (wordLength) {
        4 -> 60.dp
        5 -> 54.dp
        6 -> 46.dp
        7 -> 40.dp
        else -> 52.dp
    }

    val fontSize = when (wordLength) {
        4 -> 26.sp
        5 -> 22.sp
        6 -> 19.sp
        7 -> 16.sp
        else -> 22.sp
    }

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
                wordLength = wordLength,
                tileSize = tileSize,
                fontSize = fontSize,
                isCurrentRow = row == state.currentRow,
                isSubmitted = row < state.currentRow,
                shake = state.shake && row == state.currentRow,
                inputLength = if (row == state.currentRow) state.currentInput.length else 0,
                onTileFlipSound = onTileFlipSound,
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
    wordLength: Int,
    tileSize: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    isCurrentRow: Boolean,
    isSubmitted: Boolean,
    shake: Boolean,
    inputLength: Int,
    onTileFlipSound: ((TileState, Int) -> Unit)?,
    darkMode: Boolean,
) {
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shake) {
        if (shake) {
            val offsets = listOf(14f, -14f, 10f, -10f, 6f, -6f, 3f, -3f, 0f)
            for (off in offsets) {
                shakeOffset.animateTo(off, tween(35, easing = LinearEasing))
            }
        }
    }

    val rowBounce = remember { Animatable(1f) }
    LaunchedEffect(isCurrentRow, inputLength) {
        if (isCurrentRow && inputLength == wordLength) {
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
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
    ) {
        for (col in 0 until wordLength) {
            TileCell(
                letter = letters.getOrElse(col) { ' ' },
                tileState = tileStates.getOrElse(col) { TileState.EMPTY },
                tileSize = tileSize,
                fontSize = fontSize,
                column = col,
                isSubmitted = isSubmitted,
                revealDelayMs = col * 250L,
                onTileFlipSound = onTileFlipSound,
                darkMode = darkMode,
            )
        }
    }
}

@Composable
private fun TileCell(
    letter: Char,
    tileState: TileState,
    tileSize: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    column: Int,
    isSubmitted: Boolean,
    revealDelayMs: Long,
    onTileFlipSound: ((TileState, Int) -> Unit)?,
    darkMode: Boolean,
) {
    val popScale = remember { Animatable(1f) }
    LaunchedEffect(letter) {
        if (letter != ' ' && !isSubmitted) {
            popScale.snapTo(1f)
            popScale.animateTo(1.15f, tween(60, easing = LinearOutSlowInEasing))
            popScale.animateTo(1.0f, tween(75, easing = FastOutSlowInEasing))
        }
    }

    val flipProgress = remember { Animatable(if (isSubmitted) 1f else 0f) }
    LaunchedEffect(isSubmitted) {
        if (isSubmitted && flipProgress.value < 1f) {
            delay(revealDelayMs)
            onTileFlipSound?.invoke(tileState, column)
            flipProgress.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
        }
    }

    val isBackSide = flipProgress.value >= 0.5f

    val rotationXDegrees = if (!isBackSide) {
        flipProgress.value * 180f
    } else {
        (1f - flipProgress.value) * 180f
    }

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
            .size(tileSize)
            .graphicsLayer {
                rotationX = rotationXDegrees
                cameraDistance = 14f * density
                scaleX = popScale.value
                scaleY = popScale.value
            }
            .background(bgColor)
            .border(
                width = 2.dp,
                color = borderColor,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (letter != ' ') {
            Text(
                text = letter.toString(),
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                color = textColor,
            )
        }
    }
}

private fun TileState.toBackground(dark: Boolean): Color = when (this) {
    TileState.CORRECT -> TileCorrect
    TileState.MISPLACED -> if (dark) TileMisplacedDark else TileMisplaced
    TileState.ABSENT -> if (dark) TileAbsentDark else TileAbsent
    TileState.FILLED, TileState.EMPTY -> if (dark) BackgroundDark else BackgroundLight
}

private fun TileState.toPreRevealBackground(dark: Boolean): Color =
    if (dark) BackgroundDark else BackgroundLight

private fun TileState.toTextColor(dark: Boolean): Color = when (this) {
    TileState.CORRECT, TileState.MISPLACED, TileState.ABSENT -> Color.White
    TileState.FILLED, TileState.EMPTY -> if (dark) KeyTextDark else KeyText
}

private fun TileState.toBorder(dark: Boolean): Color = when (this) {
    TileState.EMPTY -> if (dark) TileBorderEmptyDark else TileBorderEmpty
    TileState.FILLED -> if (dark) TileBorderFilledDark else TileBorderFilled
    TileState.CORRECT, TileState.MISPLACED, TileState.ABSENT -> Color.Transparent
}
