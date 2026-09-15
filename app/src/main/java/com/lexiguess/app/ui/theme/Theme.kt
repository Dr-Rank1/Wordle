package com.lexiguess.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/** Composition local so any composable can read the current dark-mode preference. */
val LocalDarkMode = staticCompositionLocalOf { false }

/** Composition local so any composable can read the active BoardTheme palette. */
val LocalBoardTheme = staticCompositionLocalOf { EmeraldTheme }

/** Composition local so any composable can read the active Tile Material. */
val LocalTileMaterial = staticCompositionLocalOf { "CLASSIC" }

/** Composition local so any composable can check if 3D tilt parallax is enabled. */
val LocalTiltParallaxEnabled = staticCompositionLocalOf { true }

@Composable
fun LexiGuessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    boardTheme: BoardTheme = EmeraldTheme,
    tileMaterial: String = "CLASSIC",
    tiltParallaxEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = boardTheme.correctColor,
            secondary = boardTheme.misplacedColor,
            tertiary = boardTheme.absentColor,
            background = boardTheme.backgroundColor,
            surface = boardTheme.surfaceColor,
            onPrimary = KeyTextDark,
            onBackground = boardTheme.onSurfaceColor,
            onSurface = boardTheme.onSurfaceColor,
        )
    } else {
        lightColorScheme(
            primary = boardTheme.correctColor,
            secondary = boardTheme.misplacedColor,
            tertiary = boardTheme.absentColor,
            background = BackgroundLight,
            surface = SurfaceLight,
            onPrimary = KeyTextDark,
            onBackground = KeyText,
            onSurface = KeyText,
        )
    }

    CompositionLocalProvider(
        LocalDarkMode provides darkTheme,
        LocalBoardTheme provides boardTheme,
        LocalTileMaterial provides tileMaterial,
        LocalTiltParallaxEnabled provides tiltParallaxEnabled,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LexiGuessTypography,
            content = content,
        )
    }
}
