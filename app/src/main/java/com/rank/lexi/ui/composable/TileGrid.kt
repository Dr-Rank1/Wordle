package com.rank.lexi.ui.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.rank.lexi.domain.model.GameState
import com.rank.lexi.domain.model.GameState.Companion.MAX_ROWS
import com.rank.lexi.domain.model.TileState
import com.rank.lexi.ui.theme.*
import com.rank.lexi.ui.util.rememberDeviceTilt
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

    val maxRows = state.maxAttempts
    val verticalSpacing = if (maxRows > 6) 4.dp else 6.dp

    // Responsive tile size based on column count and row count
    val baseTileSize: Dp = when (wordLength) {
        4 -> 60.dp
        5 -> 54.dp
        6 -> 46.dp
        7 -> 40.dp
        else -> 52.dp
    }
    val tileSize: Dp = if (maxRows > 6) (baseTileSize * 0.88f) else baseTileSize

    val baseFontSize = when (wordLength) {
        4 -> 26.sp
        5 -> 22.sp
        6 -> 19.sp
        7 -> 16.sp
        else -> 22.sp
    }
    val fontSize = if (maxRows > 6) (baseFontSize.value * 0.9f).sp else baseFontSize

    val tilt = rememberDeviceTilt()
    val tiltEnabled = LocalTiltParallaxEnabled.current

    Column(
        modifier = modifier
            .graphicsLayer {
                if (tiltEnabled) {
                    rotationX = tilt.rotationX
                    rotationY = tilt.rotationY
                    cameraDistance = 18f * density
                }
            }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (row in 0 until maxRows) {
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
            popScale.snapTo(0.88f)
            popScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
    }

    val reducedMotion = LocalReducedMotion.current
    val flipProgress = remember { Animatable(if (isSubmitted || reducedMotion) 1f else 0f) }
    LaunchedEffect(isSubmitted, reducedMotion) {
        if (isSubmitted && flipProgress.value < 1f) {
            if (reducedMotion) {
                flipProgress.snapTo(1f)
                onTileFlipSound?.invoke(tileState, column)
            } else {
                delay(revealDelayMs)
                onTileFlipSound?.invoke(tileState, column)
                flipProgress.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
            }
        }
    }

    val isBackSide = flipProgress.value >= 0.5f

    val bloomAlpha = remember { Animatable(0f) }
    LaunchedEffect(isBackSide, tileState) {
        if (isBackSide && tileState == TileState.CORRECT) {
            delay(80)
            bloomAlpha.animateTo(0.9f, tween(140, easing = LinearOutSlowInEasing))
            bloomAlpha.animateTo(0.2f, tween(350, easing = FastOutSlowInEasing))
        }
    }

    val rotationXDegrees = if (!isBackSide) {
        flipProgress.value * 180f
    } else {
        (1f - flipProgress.value) * 180f
    }

    val boardTheme = LocalBoardTheme.current

    val bgColor = if (isBackSide) {
        tileState.toBackground(darkMode, boardTheme)
    } else {
        tileState.toPreRevealBackground(darkMode, boardTheme)
    }

    val textColor = if (isBackSide) {
        Color.White
    } else {
        tileState.toTextColor(darkMode, boardTheme)
    }

    val material = LocalTileMaterial.current
    val cornerShape = when (material) {
        "GLASS" -> androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
        "CARBON" -> androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
        "GOLDEN" -> androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
        "OBSIDIAN" -> androidx.compose.foundation.shape.RoundedCornerShape(5.dp)
        else -> androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
    }

    val finalBorderColor = when {
        isBackSide && tileState == TileState.CORRECT && bloomAlpha.value > 0.05f -> {
            Color.White.copy(alpha = bloomAlpha.value)
        }
        isBackSide && material == "GOLDEN" -> {
            Color(0xFFFFD700).copy(alpha = 0.6f)
        }
        isBackSide && material == "CARBON" -> {
            Color(0xFF455A64).copy(alpha = 0.5f)
        }
        isBackSide -> Color.Transparent
        else -> tileState.toBorder(darkMode, boardTheme)
    }

    val isEmptySlot = letter == ' ' && !isSubmitted

    // 3D Air-lift and dynamic lighting during flip
    val lift = if (reducedMotion) 0f else kotlin.math.sin(flipProgress.value * Math.PI.toFloat())
    val airLiftScale = 1.0f + (lift * 0.12f)
    val dynamicDarkening = lift * 0.35f

    val baseElevation = when {
        isBackSide -> 3f
        letter != ' ' -> 2f
        else -> 0f
    }
    val dynamicElevation = baseElevation + (10f * lift) + (if (bloomAlpha.value > 0.3f) 8f * bloomAlpha.value else 0f)

    val spotColor = when {
        isBackSide && tileState == TileState.CORRECT -> boardTheme.correctColor.copy(alpha = 0.65f)
        isBackSide && tileState == TileState.MISPLACED -> boardTheme.misplacedColor.copy(alpha = 0.65f)
        else -> Color.Black.copy(alpha = 0.40f)
    }

    val emptyWellBg = if (darkMode) Color(0xFF0C0E13).copy(alpha = 0.55f) else Color(0xFFE2E8F0).copy(alpha = 0.55f)
    val cellBgColor = if (isEmptySlot) emptyWellBg else bgColor

    val stateLabel = when {
        isSubmitted && tileState == TileState.CORRECT -> "correct"
        isSubmitted && tileState == TileState.MISPLACED -> "wrong spot"
        isSubmitted && tileState == TileState.ABSENT -> "absent"
        letter != ' ' -> "filled"
        else -> "empty"
    }

    Box(
        modifier = Modifier
            .size(tileSize)
            .semantics {
                contentDescription = if (letter == ' ') "Empty tile" else "Letter $letter"
                stateDescription = stateLabel
            }
            .graphicsLayer {
                rotationX = rotationXDegrees
                cameraDistance = 16f * density
                scaleX = popScale.value * airLiftScale
                scaleY = popScale.value * airLiftScale
                shadowElevation = dynamicElevation * density
                shape = cornerShape
                clip = false
                spotShadowColor = spotColor
                ambientShadowColor = Color.Black.copy(alpha = 0.25f)
            }
            .clip(cornerShape)
            .background(cellBgColor)
            .drawWithContent {
                drawContent()
                val cornerRadiusPx = 8f

                if (isEmptySlot) {
                    // Sunken recessed slot top shadow
                    drawLine(
                        color = Color.Black.copy(alpha = if (darkMode) 0.35f else 0.16f),
                        start = Offset(cornerRadiusPx, 1.5f),
                        end = Offset(size.width - cornerRadiusPx, 1.5f),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round,
                    )
                } else {
                    // Top specular highlight line (beveled edge reflection)
                    val highlight = when (material) {
                        "GLASS" -> Color.White.copy(alpha = 0.55f)
                        "GOLDEN" -> Color(0xFFFFF8DC).copy(alpha = 0.7f)
                        "OBSIDIAN" -> Color.White.copy(alpha = 0.12f)
                        else -> Color.White.copy(alpha = if (isBackSide) 0.32f else (if (darkMode) 0.20f else 0.45f))
                    }
                    drawLine(
                        color = highlight,
                        start = Offset(cornerRadiusPx, 1.5f),
                        end = Offset(size.width - cornerRadiusPx, 1.5f),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round,
                    )
                    if (material == "CARBON") {
                        val hatch = Color.White.copy(alpha = 0.08f)
                        var x = -size.height
                        while (x < size.width) {
                            drawLine(
                                color = hatch,
                                start = Offset(x, 0f),
                                end = Offset(x + size.height, size.height),
                                strokeWidth = 2f,
                            )
                            x += 8f
                        }
                    }
                    if (material == "GLASS") {
                        drawRect(Color.White.copy(alpha = 0.10f))
                    }
                    if (material == "GOLDEN") {
                        drawRect(Color(0xFFFFD700).copy(alpha = 0.12f))
                    }
                    if (material == "OBSIDIAN") {
                        drawRect(Color.Black.copy(alpha = 0.18f))
                    }
                    // Bottom extrusion lip line (3D extruded block bevel)
                    drawLine(
                        color = Color.Black.copy(alpha = if (isBackSide) 0.38f else (if (darkMode) 0.32f else 0.18f)),
                        start = Offset(cornerRadiusPx, size.height - 1.5f),
                        end = Offset(size.width - cornerRadiusPx, size.height - 1.5f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round,
                    )
                }

                // Dynamic angular light falloff scrim during 3D flip
                if (dynamicDarkening > 0.01f) {
                    drawRect(
                        color = Color.Black.copy(alpha = dynamicDarkening),
                        size = size,
                    )
                }
            }
            .border(
                width = if (isBackSide && (tileState == TileState.CORRECT || material == "GOLDEN")) 2.5.dp else 2.dp,
                color = finalBorderColor,
                shape = cornerShape,
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

private fun TileState.toBackground(dark: Boolean, theme: BoardTheme): Color = when (this) {
    TileState.CORRECT -> theme.correctColor
    TileState.MISPLACED -> theme.misplacedColor
    TileState.ABSENT -> theme.absentColor
    TileState.FILLED, TileState.EMPTY -> if (dark) theme.backgroundColor else BackgroundLight
}

private fun TileState.toPreRevealBackground(dark: Boolean, theme: BoardTheme): Color =
    if (dark) theme.backgroundColor else BackgroundLight

private fun TileState.toTextColor(dark: Boolean, theme: BoardTheme): Color = when (this) {
    TileState.CORRECT, TileState.MISPLACED, TileState.ABSENT -> Color.White
    TileState.FILLED, TileState.EMPTY -> if (dark) theme.onSurfaceColor else KeyText
}

private fun TileState.toBorder(dark: Boolean, theme: BoardTheme): Color = when (this) {
    TileState.EMPTY -> if (dark) theme.tileBorder else TileBorderEmpty
    TileState.FILLED -> if (dark) theme.tileBorder else TileBorderFilled
    TileState.CORRECT, TileState.MISPLACED, TileState.ABSENT -> Color.Transparent
}
