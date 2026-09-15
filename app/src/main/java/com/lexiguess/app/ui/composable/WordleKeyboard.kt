package com.lexiguess.app.ui.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexiguess.app.domain.model.TileState
import com.lexiguess.app.ui.theme.*

private val ROW1 = "QWERTYUIOP".toList()
private val ROW2 = "ASDFGHJKL".toList()
private val ROW3 = "ZXCVBNM".toList()

/**
 * Modern, responsive on-screen keyboard that dynamically stretches to fit the device width
 * with tactile haptic feedback and real-time color highlights.
 */
@Composable
fun WordleKeyboard(
    keyStates: Map<Char, TileState>,
    onKey: (Char) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    hapticsEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val darkMode = LocalDarkMode.current
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Row 1: Q W E R T Y U I O P (10 keys)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (char in ROW1) {
                LetterKey(
                    char = char,
                    tileState = keyStates[char] ?: TileState.EMPTY,
                    darkMode = darkMode,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onKey(char)
                    },
                )
            }
        }

        // Row 2: A S D F G H J K L (9 keys with half-key spacer margins)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            for (char in ROW2) {
                LetterKey(
                    char = char,
                    tileState = keyStates[char] ?: TileState.EMPTY,
                    darkMode = darkMode,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onKey(char)
                    },
                )
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }

        // Row 3: ENTER + Z X C V B N M + BACKSPACE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionKey(
                label = "ENTER",
                darkMode = darkMode,
                modifier = Modifier.weight(1.5f),
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEnter()
                },
            )
            for (char in ROW3) {
                LetterKey(
                    char = char,
                    tileState = keyStates[char] ?: TileState.EMPTY,
                    darkMode = darkMode,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onKey(char)
                    },
                )
            }
            ActionIconKey(
                darkMode = darkMode,
                modifier = Modifier.weight(1.5f),
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBackspace()
                },
            )
        }
    }
}

@Composable
private fun TactileKeyContainer(
    modifier: Modifier = Modifier,
    baseColor: Color,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed) 3.5.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "keyPress"
    )

    val cornerRadius = 6.dp
    val shape = RoundedCornerShape(cornerRadius)
    val darkerLip = remember(baseColor) { baseColor.darken(0.38f) }

    Box(
        modifier = modifier
            .height(54.dp)
            .background(darkerLip, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isPressed) 54.dp else 50.5.dp)
                .offset(y = pressOffsetY)
                .clip(shape)
                .background(baseColor)
                .drawWithContent {
                    drawContent()
                    drawLine(
                        color = Color.White.copy(alpha = if (isPressed) 0.08f else 0.22f),
                        start = Offset(cornerRadius.toPx(), 1.dp.toPx()),
                        end = Offset(size.width - cornerRadius.toPx(), 1.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx()
                    )
                },
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

private fun Color.darken(factor: Float = 0.35f): Color = Color(
    red = (red * (1f - factor)).coerceIn(0f, 1f),
    green = (green * (1f - factor)).coerceIn(0f, 1f),
    blue = (blue * (1f - factor)).coerceIn(0f, 1f),
    alpha = alpha,
)

@Composable
private fun LetterKey(
    char: Char,
    tileState: TileState,
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val boardTheme = LocalBoardTheme.current
    val bg = tileState.toKeyBackground(darkMode, boardTheme)
    val textColor = tileState.toKeyText(darkMode, boardTheme)

    TactileKeyContainer(
        modifier = modifier,
        baseColor = bg,
        onClick = onClick,
    ) {
        Text(
            text = char.toString(),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}

@Composable
private fun ActionKey(
    label: String,
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val boardTheme = LocalBoardTheme.current
    val bg = if (darkMode) boardTheme.keyDefault else KeyDefault
    val textColor = if (darkMode) boardTheme.keyText else KeyText

    TactileKeyContainer(
        modifier = modifier,
        baseColor = bg,
        onClick = onClick,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
        )
    }
}

@Composable
private fun ActionIconKey(
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val boardTheme = LocalBoardTheme.current
    val bg = if (darkMode) boardTheme.keyDefault else KeyDefault
    val iconColor = if (darkMode) boardTheme.keyText else KeyText

    TactileKeyContainer(
        modifier = modifier,
        baseColor = bg,
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Backspace,
            contentDescription = "Backspace",
            tint = iconColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

// -------------------------------------------------------------------------
// Colour helpers
// -------------------------------------------------------------------------

private fun TileState.toKeyBackground(dark: Boolean, theme: BoardTheme): Color = when (this) {
    TileState.CORRECT -> theme.correctColor
    TileState.MISPLACED -> theme.misplacedColor
    TileState.ABSENT -> theme.absentColor
    else -> if (dark) theme.keyDefault else KeyDefault
}

private fun TileState.toKeyText(dark: Boolean, theme: BoardTheme): Color = when (this) {
    TileState.CORRECT, TileState.MISPLACED, TileState.ABSENT -> Color.White
    else -> if (dark) theme.keyText else KeyText
}
