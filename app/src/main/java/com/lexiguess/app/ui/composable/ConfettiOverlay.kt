package com.lexiguess.app.ui.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Full-screen confetti particle overlay.
 *
 * Particles are launched from the top of the screen in a spread burst, fall
 * under simulated gravity, and rotate as they descend. The overlay fades itself
 * out after [durationMs] milliseconds.
 *
 * No third-party libraries are used — everything is drawn on a Compose [Canvas].
 *
 * @param active     Whether the overlay is currently displayed.
 * @param durationMs How long (in ms) to run the animation before stopping.
 */
@Composable
fun ConfettiOverlay(
    active: Boolean,
    durationMs: Int = 3_500,
    modifier: Modifier = Modifier,
) {
    if (!active) return

    val particles = remember { generateParticles(count = 120) }

    // Infinity → we drive time ourselves so we can stop cleanly
    val elapsed = remember { Animatable(0f) }

    LaunchedEffect(active) {
        elapsed.snapTo(0f)
        elapsed.animateTo(
            targetValue = durationMs.toFloat(),
            animationSpec = tween(durationMillis = durationMs, easing = LinearEasing),
        )
    }

    val t = elapsed.value / durationMs.toFloat() // 0..1
    val alpha = if (t > 0.7f) 1f - ((t - 0.7f) / 0.3f) else 1f // fade out last 30 %

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val progress = (t - p.delay).coerceIn(0f, 1f)
            if (progress <= 0f) return@forEach

            // Horizontal drift
            val x = p.startX * w + p.driftX * w * progress
            // Vertical fall with gravity acceleration
            val y = -p.size + (h + p.size * 2f) * progress * progress * 0.9f
            // Rotation over time
            val rot = p.startRotation + p.rotationSpeed * progress * 360f

            val particleAlpha = alpha * (1f - progress * 0.3f)
            if (particleAlpha <= 0f) return@forEach

            rotate(degrees = rot, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = particleAlpha),
                    topLeft = Offset(x - p.size / 2f, y - p.size / 2f),
                    size = Size(p.size, p.size * 0.5f),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Particle model
// ---------------------------------------------------------------------------

private data class Particle(
    val startX: Float,       // 0..1 fraction of screen width
    val driftX: Float,       // how far it drifts horizontally (-0.3..0.3)
    val size: Float,         // side length in px (12..28)
    val color: Color,
    val startRotation: Float,
    val rotationSpeed: Float, // revolutions during full fall
    val delay: Float,         // 0..0.4, time offset so particles don't all start at once
)

private val CONFETTI_COLORS = listOf(
    Color(0xFF538D4E), // green
    Color(0xFFB59F3B), // yellow
    Color(0xFF3A86FF), // blue
    Color(0xFFFF006E), // pink
    Color(0xFFFFBE0B), // gold
    Color(0xFF8338EC), // purple
    Color(0xFFFF4500), // orange-red
    Color(0xFF06D6A0), // teal
)

private fun generateParticles(count: Int): List<Particle> = List(count) {
    Particle(
        startX = Random.nextFloat(),
        driftX = (Random.nextFloat() - 0.5f) * 0.4f,
        size = Random.nextFloat() * 16f + 12f,
        color = CONFETTI_COLORS.random(),
        startRotation = Random.nextFloat() * 360f,
        rotationSpeed = (Random.nextFloat() - 0.5f) * 6f,
        delay = Random.nextFloat() * 0.35f,
    )
}
