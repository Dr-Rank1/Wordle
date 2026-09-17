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
val SurfaceMutedLight = Color(0xFFF4F5F7)
val SurfaceMutedDark = Color(0xFF222327)
val OutlineLight = Color(0xFFD4D6DA)
val OutlineDark = Color(0xFF3A3A3C)
val InkLight = Color(0xFF1A1A1B)
val InkMutedLight = Color(0xFF6B7280)
val InkMutedDark = Color(0xFF9CA3AF)

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
    name = "Obsidian Night",
    description = "Pitch black OLED background with neon mint accents",
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
    name = "Cyberpunk Neon",
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

val RoseGoldTheme = BoardTheme(
    id = "ROSE_GOLD",
    name = "Rose Gold",
    description = "Soft pastel rose, warm amber, and slate",
    correctColor = Color(0xFFC98474),
    misplacedColor = Color(0xFFD4A373),
    absentColor = Color(0xFF4A3F45),
    backgroundColor = Color(0xFF1A1416),
    surfaceColor = Color(0xFF261C20),
    onSurfaceColor = Color(0xFFF7EDE8),
    tileBorder = Color(0xFF5C4A50),
    keyDefault = Color(0xFF6B535A),
    keyText = Color(0xFFF7EDE8),
)

val RoyalGoldTheme = BoardTheme(
    id = "ROYAL_GOLD",
    name = "Royal Gold",
    description = "Deep sapphire navy with lustrous imperial gold",
    correctColor = Color(0xFFD4AF37),
    misplacedColor = Color(0xFF4FC3F7),
    absentColor = Color(0xFF1A2744),
    backgroundColor = Color(0xFF0B1220),
    surfaceColor = Color(0xFF152238),
    onSurfaceColor = Color(0xFFFFF6D8),
    tileBorder = Color(0xFF2A3B63),
    keyDefault = Color(0xFF24345A),
    keyText = Color(0xFFFFF6D8),
)

val SolarFlareTheme = BoardTheme(
    id = "SOLAR_FLARE",
    name = "Solar Flare",
    description = "Radiant solar amber and fiery crimson",
    correctColor = Color(0xFFFFB703),
    misplacedColor = Color(0xFFFB5607),
    absentColor = Color(0xFF3D1F12),
    backgroundColor = Color(0xFF140C08),
    surfaceColor = Color(0xFF24140E),
    onSurfaceColor = Color(0xFFFFF3E0),
    tileBorder = Color(0xFF5C2E18),
    keyDefault = Color(0xFF6B3A1F),
    keyText = Color(0xFFFFF3E0),
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
    RoseGoldTheme,
    RoyalGoldTheme,
    SolarFlareTheme,
    HighContrastTheme,
)

fun resolveBoardThemeId(id: String): String = when (id) {
    "SEPIA" -> "ROSE_GOLD"
    "SUNSET" -> "SOLAR_FLARE"
    else -> id
}

fun boardThemeById(id: String): BoardTheme =
    ALL_BOARD_THEMES.find { it.id == resolveBoardThemeId(id) } ?: EmeraldTheme

