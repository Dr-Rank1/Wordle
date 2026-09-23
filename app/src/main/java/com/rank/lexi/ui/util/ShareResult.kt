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
        try {
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
            val chooser = Intent.createChooser(intent, "Share your result").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            try {
                val text = buildShareText(state)
                val textIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(textIntent, "Share your result").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (_: Exception) {}
        }
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
        val cell = 56
        val gap = 7
        val padX = 28
        val topPad = 60
        val botPad = 40
        val gridWidth = state.wordLength * cell + (state.wordLength - 1) * gap
        val width = padX * 2 + gridWidth
        val rows = state.currentRow.coerceIn(1, state.maxAttempts)
        val height = topPad + rows * cell + (rows - 1) * gap + botPad

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark background with rounded feel
        canvas.drawColor(0xFF121213.toInt())

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.LEFT
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            color = 0xFFFFFFFF.toInt()
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.RIGHT
            textSize = 14f
            color = 0xFF9CA3AF.toInt()
        }

        val modeLabel = when (state.gameMode) {
            GameMode.DAILY -> "Daily"
            GameMode.PRACTICE -> "Practice"
            GameMode.LEVEL -> "Lvl ${state.campaignLevel ?: 1}"
            GameMode.TIMED_RUSH -> "Rush"
            GameMode.DUEL -> "Duel"
            GameMode.CUSTOM -> "Challenge"
        }
        val scoreText = if (state.status == GameStatus.WON) "${state.currentRow}/${state.maxAttempts}" else "X/${state.maxAttempts}"

        canvas.drawText("LEXIGUESS", padX.toFloat(), 34f, titlePaint)
        canvas.drawText("$modeLabel • $scoreText", (width - padX).toFloat(), 34f, subPaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33FFFFFF
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        for (row in 0 until rows) {
            for (col in 0 until state.wordLength) {
                val tile = state.board[row].getOrElse(col) { TileState.EMPTY }
                fillPaint.color = when (tile) {
                    TileState.CORRECT -> 0xFF538D4E.toInt()
                    TileState.MISPLACED -> 0xFFB59F3B.toInt()
                    else -> 0xFF3A3A3C.toInt()
                }
                val left = padX + col * (cell + gap)
                val top = topPad + row * (cell + gap)
                val right = left + cell
                val bottom = top + cell

                canvas.drawRoundRect(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    bottom.toFloat(),
                    8f,
                    8f,
                    fillPaint,
                )
                // Top beveled specular highlight
                canvas.drawLine(
                    left + 8f,
                    top + 2f,
                    right - 8f,
                    top + 2f,
                    highlightPaint,
                )
            }
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = 11f
            color = 0xFF6B7280.toInt()
        }
        canvas.drawText("https://play.google.com/store/apps/details?id=com.rank.lexi", width / 2f, height - 14f, footerPaint)

        return bitmap
    }

    fun shareMultiBoard(context: Context, state: com.rank.lexi.domain.model.MultiBoardState) {
        try {
            val text = buildMultiBoardShareText(state)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share your result").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {}
    }

    fun buildMultiBoardShareText(state: com.rank.lexi.domain.model.MultiBoardState): String {
        val title = if (state.mode == com.rank.lexi.domain.model.MultiBoardMode.DORDLE) "LexiGuess Two Boards" else "LexiGuess Four Boards"
        val summary = state.boards.mapIndexed { idx, b ->
            val score = if (b.isSolved) "${b.solvedAtRow ?: b.guesses.size}" else "X"
            "#${idx + 1}: $score/${state.maxAttempts}"
        }.joinToString("  ")

        val sb = StringBuilder()
        sb.append("$title ($summary)\n\n")

        if (state.mode == com.rank.lexi.domain.model.MultiBoardMode.DORDLE) {
            for (r in 0 until state.currentRow) {
                val rowStr = state.boards.joinToString("  ") { board ->
                    if (r < board.rowStates.size) {
                        board.rowStates[r].joinToString("") { it.toEmoji() }
                    } else {
                        "\u2B1B".repeat(state.wordLength)
                    }
                }
                sb.append(rowStr).append("\n")
            }
        } else {
            val topBoards = listOfNotNull(state.boards.getOrNull(0), state.boards.getOrNull(1))
            val botBoards = listOfNotNull(state.boards.getOrNull(2), state.boards.getOrNull(3))
            for (r in 0 until state.currentRow) {
                val rowStr1 = topBoards.joinToString("  ") { board ->
                    if (r < board.rowStates.size) board.rowStates[r].joinToString("") { it.toEmoji() }
                    else "\u2B1B".repeat(state.wordLength)
                }
                sb.append(rowStr1).append("\n")
            }
            sb.append("\n")
            for (r in 0 until state.currentRow) {
                val rowStr2 = botBoards.joinToString("  ") { board ->
                    if (r < board.rowStates.size) board.rowStates[r].joinToString("") { it.toEmoji() }
                    else "\u2B1B".repeat(state.wordLength)
                }
                sb.append(rowStr2).append("\n")
            }
        }
        sb.append("\nhttps://play.google.com/store/apps/details?id=com.rank.lexi")
        return sb.toString()
    }

    fun shareDuel(
        context: Context,
        p1Won: Boolean,
        p1Attempts: Int,
        p1TimeSeconds: Long,
        p2Won: Boolean,
        p2Attempts: Int,
        p2TimeSeconds: Long,
        winnerText: String,
    ) {
        try {
            val p1Result = if (p1Won) "$p1Attempts guesses (${p1TimeSeconds}s)" else "Failed"
            val p2Result = if (p2Won) "$p2Attempts guesses (${p2TimeSeconds}s)" else "Failed"
            val text = "⚔️ LexiGuess Pass and Play Duel\n\nPlayer 1: $p1Result\nPlayer 2: $p2Result\nOutcome: $winnerText\n\nhttps://play.google.com/store/apps/details?id=com.rank.lexi"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share duel result").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {}
    }

    fun shareStats(
        context: Context,
        rankTitle: String,
        level: Int,
        xp: Int,
        totalGames: Int,
        winPercent: Int,
        currentStreak: Int,
        bestStreak: Int,
    ) {
        try {
            val text = """
                🏆 LexiGuess Career Profile 🏆
                
                🎖️ Rank: $rankTitle (Level $level)
                🧠 Puzzles Solved: $totalGames
                🎯 Win Rate: $winPercent%
                🔥 Streak: $currentStreak (Best: $bestStreak)
                ✨ XP: $xp
                
                Can you beat my stats? Download on Google Play:
                https://play.google.com/store/apps/details?id=com.rank.lexi
            """.trimIndent()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share career stats").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {}
    }
}
