package com.example.wordle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordle.GameUiState
import com.example.wordle.LetterState
import com.example.wordle.WordleViewModel

@Composable
fun WordleScreen(viewModel: WordleViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wordle Clone",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Grid(grid = uiState.grid, currentInput = uiState.currentInput)
        Spacer(modifier = Modifier.height(8.dp))
        Keyboard(
            disabledKeys = uiState.keyboardDisabled,
            onKeyPress = { viewModel.onKeyPress(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        uiState.message?.let { msg ->
            Text(text = msg, color = Color.Red, fontSize = 16.sp)
        }
    }
}

@Composable
fun Grid(grid: List<List<LetterState>>, currentInput: String) {
    Column {
        grid.forEachIndexed { rowIndex, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                row.forEachIndexed { colIndex, state ->
                    // If this is the current row being typed, show the character from currentInput
                    val displayChar = if (rowIndex == grid.indexOfFirst { it.all { it == LetterState.UNKNOWN } } && colIndex < currentInput.length) {
                        currentInput[colIndex]
                    } else {
                        ' '
                    }
                    Tile(state = state, char = displayChar)
                }
            }
        }
    }
}

@Composable
fun Tile(state: LetterState, char: Char) {
    val background = when (state) {
        LetterState.CORRECT -> Color(0xFF6AAA64) // green
        LetterState.MISPLACED -> Color(0xFFC9B458) // yellow
        LetterState.ABSENT -> Color(0xFF787C7E) // gray
        LetterState.UNKNOWN -> Color(0xFFDDDDDD) // light gray
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .background(background, shape = RoundedCornerShape(4.dp))
    ) {
        Text(text = char.uppercaseChar().toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Composable
fun Keyboard(disabledKeys: Set<Char>, onKeyPress: (Char) -> Unit) {
    val rows = listOf(
        "QWERTYUIOP",
        "ASDFGHJKL",
        "ZXCVBNM"
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { rowString ->
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                rowString.forEach { ch ->
                    val disabled = disabledKeys.contains(ch)
                    Button(
                        onClick = { onKeyPress(ch) },
                        enabled = !disabled,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(text = ch.toString())
                    }
                }
            }
        }
        // Bottom row with Backspace and Enter
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { onKeyPress('\b') },
                modifier = Modifier.size(72.dp, 36.dp)
            ) { Text("⌫") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onKeyPress('\n') },
                modifier = Modifier.size(72.dp, 36.dp)
            ) { Text("Enter") }
        }
    }
}
