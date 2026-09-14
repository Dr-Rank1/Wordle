package com.lexiguess.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexiguess.app.data.repository.PlayerPreferences
import com.lexiguess.app.ui.theme.TileCorrect
import com.lexiguess.app.ui.theme.TileMisplaced
import kotlinx.coroutines.launch

data class TileMaterialDef(
    val id: String,
    val name: String,
    val description: String,
    val unlockLevel: Int,
    val previewBorderColor: Color,
    val cornerRadius: Int,
)

data class ParticleEffectDef(
    val id: String,
    val name: String,
    val description: String,
    val unlockLevel: Int,
    val accentColor: Color,
)

val TILE_MATERIALS = listOf(
    TileMaterialDef("CLASSIC", "Classic Clean", "Minimalist matte with sharp responsive borders", 1, Color(0xFF6AAA64), 2),
    TileMaterialDef("CARBON", "Carbon Fiber", "Dark tactical weave with rounded edge bevels", 3, Color(0xFF455A64), 6),
    TileMaterialDef("GOLDEN", "Golden Foil", "Gilded bullion border with polished brass sheen", 6, Color(0xFFFFD700), 4),
    TileMaterialDef("GLASS", "Holographic Glass", "Frosted translucent corners with prism bloom", 10, Color(0xFF00E5FF), 10),
    TileMaterialDef("OBSIDIAN", "Obsidian Slate", "Deep volumetric stone bevel with stark contrast", 15, Color(0xFF263238), 5),
)

val PARTICLE_EFFECTS = listOf(
    ParticleEffectDef("CONFETTI", "Classic Confetti", "Festive multi-colored confetti burst", 1, TileCorrect),
    ParticleEffectDef("STARLIGHT", "Starlight Sparks", "Radiant stellar flare with trailing sparks", 4, Color(0xFF00E5FF)),
    ParticleEffectDef("CYBER_NEON", "Cyber Neon", "Electrified synthwave beams and laser dust", 8, Color(0xFFFF007F)),
    ParticleEffectDef("GOLDEN_EMBERS", "Golden Embers", "Floating glowing embers ascending from the board", 12, Color(0xFFFFD700)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticsScreen(
    playerPreferences: PlayerPreferences,
    onBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val xp by playerPreferences.xpFlow.collectAsState(initial = 0)
    val playerLevel = PlayerPreferences.calculateLevel(xp)

    val currentMaterial by playerPreferences.tileMaterialFlow.collectAsState(initial = "CLASSIC")
    val currentParticle by playerPreferences.particleEffectFlow.collectAsState(initial = "CONFETTI")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "COSMETICS STUDIO",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text = "Materials, Borders & Shaders",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Level Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "PLAYER MASTERY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TileCorrect,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = "Level $playerLevel Wordsmith",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = TileCorrect.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = "$xp XP",
                                fontWeight = FontWeight.Bold,
                                color = TileCorrect,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }

            // Section 1: Tile Materials
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Style,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "TILE MATERIALS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp,
                    )
                }
            }

            items(TILE_MATERIALS.size) { index ->
                val mat = TILE_MATERIALS[index]
                val isUnlocked = playerLevel >= mat.unlockLevel
                val isEquipped = currentMaterial == mat.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isUnlocked) {
                            coroutineScope.launch {
                                playerPreferences.setTileMaterial(mat.id)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEquipped) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                    border = if (isEquipped) {
                        androidx.compose.foundation.BorderStroke(2.dp, TileCorrect)
                    } else null,
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            // Mini Tile Preview
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(mat.cornerRadius.dp))
                                    .background(TileCorrect)
                                    .border(2.dp, mat.previewBorderColor, RoundedCornerShape(mat.cornerRadius.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "W",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                )
                            }

                            Column {
                                Text(
                                    text = mat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = mat.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                )
                            }
                        }

                        // Status Badge
                        when {
                            isEquipped -> {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TileCorrect,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Text("EQUIPPED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                            isUnlocked -> {
                                TextButton(onClick = {
                                    coroutineScope.launch { playerPreferences.setTileMaterial(mat.id) }
                                }) {
                                    Text("EQUIP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            else -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                                    Text("Lv. ${mat.unlockLevel}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Victory Shaders
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = TileMisplaced,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "VICTORY PARTICLE SHADERS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TileMisplaced,
                        letterSpacing = 1.2.sp,
                    )
                }
            }

            items(PARTICLE_EFFECTS.size) { index ->
                val effect = PARTICLE_EFFECTS[index]
                val isUnlocked = playerLevel >= effect.unlockLevel
                val isEquipped = currentParticle == effect.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isUnlocked) {
                            coroutineScope.launch {
                                playerPreferences.setParticleEffect(effect.id)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEquipped) {
                            effect.accentColor.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                    border = if (isEquipped) {
                        androidx.compose.foundation.BorderStroke(2.dp, effect.accentColor)
                    } else null,
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(effect.accentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = effect.accentColor,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            Column {
                                Text(
                                    text = effect.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = effect.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                )
                            }
                        }

                        when {
                            isEquipped -> {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = effect.accentColor,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Text("EQUIPPED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                            isUnlocked -> {
                                TextButton(onClick = {
                                    coroutineScope.launch { playerPreferences.setParticleEffect(effect.id) }
                                }) {
                                    Text("EQUIP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            else -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                                    Text("Lv. ${effect.unlockLevel}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
