package com.rank.lexi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Composition local so any composable can read the current dark-mode preference. */
val LocalDarkMode = staticCompositionLocalOf { false }

/** Composition local so any composable can read the active BoardTheme palette. */
val LocalBoardTheme = staticCompositionLocalOf { EmeraldTheme }

/** Composition local so any composable can read the active Tile Material. */
val LocalTileMaterial = staticCompositionLocalOf { "CLASSIC" }

/** Composition local so any composable can check if 3D tilt parallax is enabled. */
val LocalTiltParallaxEnabled = staticCompositionLocalOf { true }

/** Composition local for reduced motion (skip tile flips and confetti). */
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
fun LexiGuessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    boardTheme: BoardTheme = EmeraldTheme,
    tileMaterial: String = "CLASSIC",
    tiltParallaxEnabled: Boolean = true,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = boardTheme.correctColor,
            onPrimary = Color.White,
            secondary = boardTheme.misplacedColor,
            onSecondary = Color.White,
            tertiary = boardTheme.absentColor,
            background = boardTheme.backgroundColor,
            onBackground = boardTheme.onSurfaceColor,
            surface = boardTheme.surfaceColor,
            onSurface = boardTheme.onSurfaceColor,
            surfaceVariant = SurfaceMutedDark,
            onSurfaceVariant = InkMutedDark,
            outline = OutlineDark,
            outlineVariant = boardTheme.tileBorder,
            inverseSurface = Color(0xFFE8E8E8),
            inverseOnSurface = Color(0xFF1A1A1B),
            error = Color(0xFFEF5350),
        )
    } else {
        lightColorScheme(
            primary = TileCorrect,
            onPrimary = Color.White,
            secondary = TileMisplaced,
            onSecondary = Color.White,
            tertiary = TileAbsent,
            background = BackgroundLight,
            onBackground = InkLight,
            surface = SurfaceLight,
            onSurface = InkLight,
            surfaceVariant = SurfaceMutedLight,
            onSurfaceVariant = InkMutedLight,
            outline = OutlineLight,
            outlineVariant = TileBorderEmpty,
            inverseSurface = Color(0xFF1A1A1B),
            inverseOnSurface = Color.White,
            error = Color(0xFFC62828),
        )
    }

    CompositionLocalProvider(
        LocalDarkMode provides darkTheme,
        LocalBoardTheme provides boardTheme,
        LocalTileMaterial provides tileMaterial,
        LocalTiltParallaxEnabled provides tiltParallaxEnabled,
        LocalReducedMotion provides reducedMotion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LexiGuessTypography,
            content = content,
        )
    }
}
