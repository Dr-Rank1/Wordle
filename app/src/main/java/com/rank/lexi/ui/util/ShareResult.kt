package com.rank.lexi.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.rank.lexi.domain.model.GameMode
import com.rank.lexi.domain.model.GameState
import com.rank.lexi.domain.model.GameStatus
import com.rank.lexi.domain.model.TileState
import java.io.File

object ShareResult {
    fun share(context: Context, state: GameState) {
        val text = buildShareText(state)
        val bitmap = renderGrid(state)
        val cacheDir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(cacheDir, "lexiguess-result.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share your result"))
    }

    fun buildShareText(state: GameState): String {
        val attemptsLabel = if (state.status == GameStatus.WON) "${state.currentRow}" else "X"
        val hardModeStar = if (state.hardMode) "*" else ""
        val modeName = when (state.gameMode) {
            GameMode.DAILY -> "Daily"
            GameMode.PRACTICE -> "Practice"
            GameMode.LEVEL -> "Level ${state.campaignLevel ?: ""}"
            GameMode.TIMED_RUSH -> "Rush"
            GameMode.DUEL -> "Duel"
            GameMode.CUSTOM -> "Custom"
        }
        val header = "LexiGuess $modeName $attemptsLabel/${state.maxAttempts}$hardModeStar (${state.wordLength} Letters)"
        val rows = (0 until state.currentRow).joinToString("\n") { row ->
            (0 until state.wordLength).joinToString("") { col ->
                state.board[row].getOrElse(col) { TileState.EMPTY }.toEmoji()
            }
        }
        return "$header\n\n$rows"
    }

    private fun TileState.toEmoji(): String = when (this) {
        TileState.CORRECT -> "\uD83D\uDFE9"
        TileState.MISPLACED -> "\uD83D\uDFE8"
        else -> "\u2B1B"
    }

    private fun renderGrid(state: GameState): Bitmap {
        val cell = 72
        val gap = 8
        val pad = 24
        val width = pad * 2 + state.wordLength * cell + (state.wordLength - 1) * gap
        val rows = state.currentRow.coerceAtLeast(1)
        val height = pad * 2 + rows * cell + (rows - 1) * gap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFF121213.toInt())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            color = 0xFFFFFFFF.toInt()
        }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        for (row in 0 until rows) {
            for (col in 0 until state.wordLength) {
                val tile = state.board[row].getOrElse(col) { TileState.EMPTY }
                fill.color = when (tile) {
                    TileState.CORRECT -> 0xFF538D4E.toInt()
                    TileState.MISPLACED -> 0xFFB59F3B.toInt()
                    else -> 0xFF3A3A3C.toInt()
                }
                val left = pad + col * (cell + gap)
                val top = pad + row * (cell + gap)
                canvas.drawRoundRect(
                    left.toFloat(),
                    top.toFloat(),
                    (left + cell).toFloat(),
                    (top + cell).toFloat(),
                    8f,
                    8f,
                    fill,
                )
                val letter = state.boardLetters[row].getOrElse(col) { ' ' }
                if (letter != ' ') {
                    canvas.drawText(
                        letter.toString(),
                        left + cell / 2f,
                        top + cell / 2f + 12f,
                        paint,
                    )
                }
            }
        }
        return bitmap
    }
}
