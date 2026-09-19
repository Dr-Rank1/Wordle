package com.rank.lexi.ui.composable

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.rank.lexi.ui.theme.LocalReducedMotion
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiPiece(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotationSpeed: Float,
    val size: Float,
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
    val reducedMotion = LocalReducedMotion.current
    if (!trigger || reducedMotion) return

    val colors = PALETTES[particleEffect] ?: PALETTES["CONFETTI"]!!
    val progress = remember { Animatable(0f) }
    val isEmbers = particleEffect == "GOLDEN_EMBERS"

    val pieces = remember(particleEffect) {
        List(if (isEmbers) 85 else 70) {
            ConfettiPiece(
                initialX = Random.nextFloat(),
                initialY = if (isEmbers) 1.1f + Random.nextFloat() * 0.3f else -Random.nextFloat() * 0.25f,
                velocityX = (Random.nextFloat() - 0.5f) * (if (isEmbers) 180f else 400f),
                velocityY = if (isEmbers) -(450f + Random.nextFloat() * 400f) else (550f + Random.nextFloat() * 450f),
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                size = if (isEmbers) (5f + Random.nextFloat() * 9f) else (10f + Random.nextFloat() * 12f),
                color = colors[Random.nextInt(colors.size)],
                swayPhase = Random.nextFloat() * 6.28f,
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = if (isEmbers) 3500 else 3000, easing = LinearEasing),
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val t = progress.value

            pieces.forEach { piece ->
                val x = (piece.initialX * canvasWidth) + (piece.velocityX * t) + sin(t * 8f + piece.swayPhase) * 35f
                val y = if (isEmbers) {
                    (piece.initialY * canvasHeight) + (piece.velocityY * t)
                } else {
                    (piece.initialY * canvasHeight) + (piece.velocityY * t) + (350f * t * t)
                }
                val rotation = piece.rotationSpeed * t
                val alpha = if (isEmbers) {
                    (sin(t * Math.PI.toFloat()) * 1.1f).coerceIn(0f, 1f)
                } else {
                    (1f - t).coerceIn(0f, 1f)
                }

                if (y in -60f..(canvasHeight + 60f) && x in -40f..(canvasWidth + 40f)) {
                    rotate(rotation, pivot = Offset(x, y)) {
                        when (particleEffect) {
                            "STARLIGHT" -> drawStarParticle(x, y, piece.size, piece.color.copy(alpha = alpha))
                            "GOLDEN_EMBERS" -> drawEmberParticle(x, y, piece.size, piece.color.copy(alpha = alpha))
                            "CYBER_NEON" -> drawDiamondParticle(x, y, piece.size, piece.color.copy(alpha = alpha))
                            else -> {
                                drawRect(
                                    color = piece.color.copy(alpha = alpha),
                                    topLeft = Offset(x - piece.size / 2f, y - piece.size / 4f),
                                    size = Size(piece.size, piece.size * 0.55f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStarParticle(x: Float, y: Float, size: Float, color: Color) {
    val r = size * 0.7f
    val inner = r * 0.28f
    val path = Path().apply {
        moveTo(x, y - r)
        lineTo(x + inner, y - inner)
        lineTo(x + r, y)
        lineTo(x + inner, y + inner)
        lineTo(x, y + r)
        lineTo(x - inner, y + inner)
        lineTo(x - r, y)
        lineTo(x - inner, y - inner)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawDiamondParticle(x: Float, y: Float, size: Float, color: Color) {
    val w = size * 0.5f
    val h = size * 0.9f
    val path = Path().apply {
        moveTo(x, y - h)
        lineTo(x + w, y)
        lineTo(x, y + h)
        lineTo(x - w, y)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawEmberParticle(x: Float, y: Float, size: Float, color: Color) {
    drawCircle(color, radius = size * 0.5f, center = Offset(x, y))
    drawCircle(Color.White.copy(alpha = color.alpha * 0.75f), radius = size * 0.22f, center = Offset(x, y))
}
