package com.lexiguess.app.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
private fun LetterKey(
    char: Char,
    tileState: TileState,
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg = tileState.toKeyBackground(darkMode)
    val textColor = tileState.toKeyText(darkMode)

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
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
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (darkMode) KeyDefaultDark else KeyDefault)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = if (darkMode) KeyTextDark else KeyText,
        )
    }
}

@Composable
private fun ActionIconKey(
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (darkMode) KeyDefaultDark else KeyDefault)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Backspace,
            contentDescription = "Backspace",
            tint = if (darkMode) KeyTextDark else KeyText,
            modifier = Modifier.size(20.dp),
        )
    }
}

// -------------------------------------------------------------------------
// Colour helpers
// -------------------------------------------------------------------------

private fun TileState.toKeyBackground(dark: Boolean): Color = when (this) {
    TileState.CORRECT -> TileCorrect
    TileState.MISPLACED -> if (dark) TileMisplacedDark else TileMisplaced
    TileState.ABSENT -> if (dark) TileAbsentDark else TileAbsent
    else -> if (dark) KeyDefaultDark else KeyDefault
}

private fun TileState.toKeyText(dark: Boolean): Color = when (this) {
    TileState.CORRECT, TileState.MISPLACED, TileState.ABSENT -> Color.White
    else -> if (dark) KeyTextDark else KeyText
}
