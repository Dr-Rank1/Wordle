package com.lexiguess.app.ui.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiPiece(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotationSpeed: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val swayPhase: Float,
)

private val PALETTES = mapOf(
    "CONFETTI" to listOf(
        Color(0xFF538D4E), Color(0xFFB59F3B), Color(0xFF00E5FF),
        Color(0xFFFF007F), Color(0xFFFFB703), Color(0xFF8338EC),
        Color(0xFF3A86FF), Color(0xFFFFFFFF),
    ),
    "STARLIGHT" to listOf(
        Color(0xFF00E5FF), Color(0xFFE0F7FA), Color(0xFF80DEEA),
        Color(0xFFFFFFFF), Color(0xFF82B1FF), Color(0xFFB388FF),
    ),
    "CYBER_NEON" to listOf(
        Color(0xFFFF007F), Color(0xFF00E5FF), Color(0xFFD500F9),
        Color(0xFF76FF03), Color(0xFFFF4081), Color(0xFFFFFFFF),
    ),
    "GOLDEN_EMBERS" to listOf(
        Color(0xFFFFD700), Color(0xFFFFB300), Color(0xFFFF8F00),
        Color(0xFFFF6F00), Color(0xFFFFE082), Color(0xFFFF3D00),
    ),
)

@Composable
fun ConfettiParticleEngine(
    trigger: Boolean,
    particleEffect: String = "CONFETTI",
    modifier: Modifier = Modifier,
) {
    if (!trigger) return

    val colors = PALETTES[particleEffect] ?: PALETTES["CONFETTI"]!!
    val progress = remember { Animatable(0f) }
    val pieces = remember(particleEffect) {
        List(70) {
            ConfettiPiece(
                initialX = Random.nextFloat(),
                initialY = -Random.nextFloat() * 200f,
                velocityX = (Random.nextFloat() - 0.5f) * 400f,
                velocityY = 600f + Random.nextFloat() * 500f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                width = 12f + Random.nextFloat() * 10f,
                height = 6f + Random.nextFloat() * 8f,
                color = colors[Random.nextInt(colors.size)],
                swayPhase = Random.nextFloat() * 6.28f,
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3200, easing = LinearEasing),
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val t = progress.value

            pieces.forEach { piece ->
                val x = (piece.initialX * canvasWidth) + (piece.velocityX * t) + sin(t * 10f + piece.swayPhase) * 40f
                val y = piece.initialY + (piece.velocityY * t) + (400f * t * t)
                val rotation = piece.rotationSpeed * t
                val alpha = (1f - t).coerceIn(0f, 1f)

                if (y in -50f..(canvasHeight + 50f)) {
                    rotate(rotation, pivot = Offset(x, y)) {
                        drawRect(
                            color = piece.color.copy(alpha = alpha),
                            topLeft = Offset(x - piece.width / 2f, y - piece.height / 2f),
                            size = Size(piece.width, piece.height),
                        )
                    }
                }
            }
        }
    }
}
