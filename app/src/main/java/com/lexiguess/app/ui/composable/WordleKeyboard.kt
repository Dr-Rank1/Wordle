package com.lexiguess.app.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexiguess.app.domain.model.TileState
import com.lexiguess.app.ui.theme.*

private val ROW1 = "QWERTYUIOP".toList()
private val ROW2 = "ASDFGHJKL".toList()
private val ROW3 = "ZXCVBNM".toList()

/**
 * On-screen keyboard that colours each key based on the best-known [TileState]
 * for that letter. Includes Backspace and Enter keys.
 */
@Composable
fun WordleKeyboard(
    keyStates: Map<Char, TileState>,
    onKey: (Char) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkMode = LocalDarkMode.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KeyRow(keys = ROW1, keyStates = keyStates, onKey = onKey, darkMode = darkMode)
        KeyRow(keys = ROW2, keyStates = keyStates, onKey = onKey, darkMode = darkMode)

        // Bottom row: ENTER + letters + BACKSPACE
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionKey(label = "ENTER", darkMode = darkMode, onClick = onEnter)
            for (char in ROW3) {
                LetterKey(
                    char = char,
                    tileState = keyStates[char] ?: TileState.EMPTY,
                    darkMode = darkMode,
                    onClick = { onKey(char) },
                )
            }
            ActionKey(label = "\u232B", darkMode = darkMode, onClick = onBackspace)
        }
    }
}

@Composable
private fun KeyRow(
    keys: List<Char>,
    keyStates: Map<Char, TileState>,
    onKey: (Char) -> Unit,
    darkMode: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (char in keys) {
            LetterKey(
                char = char,
                tileState = keyStates[char] ?: TileState.EMPTY,
                darkMode = darkMode,
                onClick = { onKey(char) },
            )
        }
    }
}

@Composable
private fun LetterKey(
    char: Char,
    tileState: TileState,
    darkMode: Boolean,
    onClick: () -> Unit,
) {
    val bg = tileState.toKeyBackground(darkMode)
    val textColor = tileState.toKeyText(darkMode)

    Box(
        modifier = Modifier
            .width(32.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = char.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}

@Composable
private fun ActionKey(
    label: String,
    darkMode: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (darkMode) KeyDefaultDark else KeyDefault)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (darkMode) KeyTextDark else KeyText,
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
