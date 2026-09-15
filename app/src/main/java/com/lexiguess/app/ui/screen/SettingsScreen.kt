package com.lexiguess.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexiguess.app.data.repository.PlayerPreferences
import com.lexiguess.app.ui.audio.SoundManager
import com.lexiguess.app.ui.theme.ALL_BOARD_THEMES
import com.lexiguess.app.ui.theme.TileCorrect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    playerPreferences: PlayerPreferences,
    soundManager: SoundManager,
    themePref: String = "SYSTEM",
    darkMode: Boolean,
    onThemeChange: (String) -> Unit,
    onBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val soundEnabled by playerPreferences.soundEnabledFlow.collectAsState(initial = true)
    val hapticsEnabled by playerPreferences.hapticsEnabledFlow.collectAsState(initial = true)
    val hardMode by playerPreferences.hardModeFlow.collectAsState(initial = false)
    val tiltParallaxEnabled by playerPreferences.tiltParallaxFlow.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Gameplay Section
            Text(
                text = "GAMEPLAY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
            )

            // Hard Mode Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text("Hard Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Any revealed hints must be used in subsequent guesses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = hardMode,
                    onCheckedChange = { checked ->
                        coroutineScope.launch { playerPreferences.setHardMode(checked) }
                    },
                )
            }

            HorizontalDivider()

            // Feedback Section
            Text(
                text = "AUDIO & HAPTICS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
            )

            // Sound Effects Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text("Sound Effects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Procedural keystroke clicks and chorded flip arpeggios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { checked ->
                        soundManager.isEnabled = checked
                        coroutineScope.launch { playerPreferences.setSoundEnabled(checked) }
                    },
                )
            }

            // Haptics Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text("Vibration Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Tactile haptic pulses when tapping keyboard keys.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = { checked ->
                        coroutineScope.launch { playerPreferences.setHapticsEnabled(checked) }
                    },
                )
            }

            HorizontalDivider()

            // Theme Section
            Text(
                text = "APPEARANCE & THEME",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
            )

            Text(
                text = "Display Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            // 3-Way Mode Selector: System, Light, Dark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val modes = listOf(
                    Triple("SYSTEM", "System", Icons.Outlined.PhoneAndroid),
                    Triple("LIGHT", "Light", Icons.Outlined.LightMode),
                    Triple("DARK", "Dark", Icons.Outlined.DarkMode),
                )

                modes.forEach { (modeId, label, icon) ->
                    val isSelected = themePref == modeId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) BorderStroke(2.dp, TileCorrect) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onThemeChange(modeId)
                                coroutineScope.launch { playerPreferences.setTheme(modeId) }
                            },
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) TileCorrect else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp),
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) TileCorrect else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            // 3D Tilt Parallax Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text("3D Tilt Parallax", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Subtle holographic perspective tilt on the game board as you angle your phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = tiltParallaxEnabled,
                    onCheckedChange = { checked ->
                        coroutineScope.launch {
                            playerPreferences.setTiltParallaxEnabled(checked)
                        }
                    },
                )
            }

            // Board Theme Palette Selector
            val currentBoardThemeId by playerPreferences.boardThemeFlow.collectAsState(initial = "EMERALD")

            Text(
                text = "Color Theme Palette",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ALL_BOARD_THEMES.forEach { theme ->
                    val isSelected = currentBoardThemeId == theme.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) BorderStroke(2.dp, theme.correctColor) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch { playerPreferences.setBoardTheme(theme.id) }
                            },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(theme.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(theme.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // Color swatches preview
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    drawCircle(theme.correctColor)
                                }
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    drawCircle(theme.misplacedColor)
                                }
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    drawCircle(theme.backgroundColor)
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // ==========================================
            // LEGAL & COMPLIANCE
            // ==========================================
            Text(
                text = "LEGAL & COMPLIANCE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
            )

            // Trademark Disclaimer Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = TileCorrect,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "TRADEMARK DISCLAIMER",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "Wordle is a registered trademark of The New York Times Company. LexiGuess is an independent game and is not affiliated with, sponsored by, authorized by, or endorsed by The New York Times Company.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
            }

            // Privacy Policy Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = TileCorrect,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "PRIVACY & ON-DEVICE STORAGE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "LexiGuess does not collect, track, sell, or transmit any personal identifiable information (PII). All user statistics, streaks, and game progress are stored locally on your device in an encrypted Room SQLite database. Network calls are only used for anonymous dictionary definitions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
            }

            // Offline Dictionary & Origins Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "OFFLINE LEXICON ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "15,287 offline English words loaded across 4, 5, 6, and 7-letter dictionaries. Gameplay mechanics inspired by classic deductive word games Jotto (1955) and Mastermind (1970).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "LexiGuess v2.0 · Open Source & Privacy First",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}
