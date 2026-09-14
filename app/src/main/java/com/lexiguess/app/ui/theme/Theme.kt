package com.lexiguess.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/** Composition local so any composable can read the current dark-mode preference. */
val LocalDarkMode = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = TileCorrect,
    secondary = TileMisplacedDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = KeyTextDark,
    onBackground = KeyTextDark,
    onSurface = KeyTextDark,
)

private val LightColorScheme = lightColorScheme(
    primary = TileCorrect,
    secondary = TileMisplaced,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = KeyTextDark,
    onBackground = KeyText,
    onSurface = KeyText,
)

@Composable
fun LexiGuessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalDarkMode provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LexiGuessTypography,
            content = content,
        )
    }
}
