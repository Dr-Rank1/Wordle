package com.rank.lexi.ui.theme

import androidx.compose.ui.graphics.Color

// Tile colours – light mode
val TileCorrect = Color(0xFF538D4E)       // green
val TileMisplaced = Color(0xFFB59F3B)     // yellow
val TileAbsent = Color(0xFF787C7E)        // grey
val TileFilled = Color(0xFFFFFFFF)        // white with border
val TileEmpty = Color(0xFFFFFFFF)

// Tile colours – dark mode
val TileCorrectDark = Color(0xFF538D4E)
val TileMisplacedDark = Color(0xFFB59F3B)
val TileAbsentDark = Color(0xFF3A3A3C)
val TileFilledDark = Color(0xFF121213)
val TileEmptyDark = Color(0xFF121213)

// Keyboard
val KeyDefault = Color(0xFFD3D6DA)
val KeyDefaultDark = Color(0xFF818384)
val KeyText = Color(0xFF1A1A1B)
val KeyTextDark = Color(0xFFFFFFFF)

// Background & surface
val BackgroundLight = Color(0xFFFFFFFF)
val BackgroundDark = Color(0xFF121213)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1A1A1B)

// Borders
val TileBorderEmpty = Color(0xFFD3D6DA)
val TileBorderFilled = Color(0xFF878A8C)
val TileBorderEmptyDark = Color(0xFF3A3A3C)
val TileBorderFilledDark = Color(0xFF565758)

// Header divider
val Divider = Color(0xFFD3D6DA)
val DividerDark = Color(0xFF3A3A3C)

data class BoardTheme(
    val id: String,
    val name: String,
    val description: String,
    val correctColor: Color,
    val misplacedColor: Color,
    val absentColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val onSurfaceColor: Color,
    val tileBorder: Color,
    val keyDefault: Color,
    val keyText: Color,
)

val EmeraldTheme = BoardTheme(
    id = "EMERALD",
    name = "Classic Emerald",
    description = "Traditional Wordle green and warm ochre",
    correctColor = Color(0xFF538D4E),
    misplacedColor = Color(0xFFB59F3B),
    absentColor = Color(0xFF3A3A3C),
    backgroundColor = Color(0xFF121213),
    surfaceColor = Color(0xFF1A1A1B),
    onSurfaceColor = Color(0xFFFFFFFF),
    tileBorder = Color(0xFF3A3A3C),
    keyDefault = Color(0xFF818384),
    keyText = Color(0xFFFFFFFF),
)

val MidnightOledTheme = BoardTheme(
    id = "MIDNIGHT_OLED",
    name = "Midnight OLED",
    description = "Pitch black background with neon mint accents",
    correctColor = Color(0xFF00E676),
    misplacedColor = Color(0xFFFFD600),
    absentColor = Color(0xFF263238),
    backgroundColor = Color(0xFF000000),
    surfaceColor = Color(0xFF101418),
    onSurfaceColor = Color(0xFFECEFF1),
    tileBorder = Color(0xFF263238),
    keyDefault = Color(0xFF37474F),
    keyText = Color(0xFFFFFFFF),
)

val CyberpunkTheme = BoardTheme(
    id = "CYBERPUNK",
    name = "Cyberpunk Synth",
    description = "Electric neon cyan and hot magenta",
    correctColor = Color(0xFF00E5FF),
    misplacedColor = Color(0xFFFF007F),
    absentColor = Color(0xFF2A2B3D),
    backgroundColor = Color(0xFF0C0D14),
    surfaceColor = Color(0xFF151624),
    onSurfaceColor = Color(0xFFF0F3F8),
    tileBorder = Color(0xFF2E314A),
    keyDefault = Color(0xFF2E314A),
    keyText = Color(0xFF00E5FF),
)

val SepiaTheme = BoardTheme(
    id = "SEPIA",
    name = "Warm Sepia",
    description = "Espresso, roasted coffee, and aged parchment",
    correctColor = Color(0xFF588157),
    misplacedColor = Color(0xFFDDA15E),
    absentColor = Color(0xFF4A3E38),
    backgroundColor = Color(0xFF1F1814),
    surfaceColor = Color(0xFF2E241E),
    onSurfaceColor = Color(0xFFF3E9DC),
    tileBorder = Color(0xFF4A3E38),
    keyDefault = Color(0xFF5C4D44),
    keyText = Color(0xFFF3E9DC),
)

val SunsetTheme = BoardTheme(
    id = "SUNSET",
    name = "Sunset Mirage",
    description = "Deep dusk purple, coral vermilion, and twilight gold",
    correctColor = Color(0xFF8338EC),
    misplacedColor = Color(0xFFFB5607),
    absentColor = Color(0xFF32293F),
    backgroundColor = Color(0xFF120E1C),
    surfaceColor = Color(0xFF1E172E),
    onSurfaceColor = Color(0xFFFFE5D9),
    tileBorder = Color(0xFF3E3152),
    keyDefault = Color(0xFF4A3B60),
    keyText = Color(0xFFFFE5D9),
)

val HighContrastTheme = BoardTheme(
    id = "HIGH_CONTRAST",
    name = "High Contrast",
    description = "Accessible colorblind-safe cobalt and vivid orange",
    correctColor = Color(0xFF0072B2),
    misplacedColor = Color(0xFFE69F00),
    absentColor = Color(0xFF424242),
    backgroundColor = Color(0xFF121212),
    surfaceColor = Color(0xFF1E1E1E),
    onSurfaceColor = Color(0xFFFFFFFF),
    tileBorder = Color(0xFF616161),
    keyDefault = Color(0xFF616161),
    keyText = Color(0xFFFFFFFF),
)

val ALL_BOARD_THEMES = listOf(
    EmeraldTheme,
    MidnightOledTheme,
    CyberpunkTheme,
    SepiaTheme,
    SunsetTheme,
    HighContrastTheme,
)

