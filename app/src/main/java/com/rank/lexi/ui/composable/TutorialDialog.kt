package com.rank.lexi.ui.composable

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rank.lexi.domain.model.TileState
import com.rank.lexi.ui.audio.SoundManager
import com.rank.lexi.ui.theme.TileAbsent
import com.rank.lexi.ui.theme.TileCorrect
import com.rank.lexi.ui.theme.TileMisplaced
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TutorialDialog(
    soundManager: SoundManager? = null,
    onDismiss: () -> Unit,
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val totalPages = 4

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Top navigation bar (Page indicator + Skip/Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Dot indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        for (i in 0 until totalPages) {
                            val isActive = i == currentPage
                            Box(
                                modifier = Modifier
                                    .size(if (isActive) 20.dp else 8.dp, 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isActive) TileCorrect else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = if (currentPage == totalPages - 1) "Close" else "Skip",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Main Page Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    when (currentPage) {
                        0 -> TutorialPageWelcome()
                        1 -> TutorialPageColors()
                        2 -> TutorialPageInteractivePractice(soundManager = soundManager)
                        3 -> TutorialPageGameModes()
                    }
                }

                // Bottom navigation controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentPage > 0) {
                        OutlinedButton(
                            onClick = { currentPage-- },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(44.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (currentPage < totalPages - 1) {
                                currentPage++
                            } else {
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TileCorrect),
                        modifier = Modifier.height(44.dp),
                    ) {
                        Text(
                            text = if (currentPage == totalPages - 1) "Start Playing!" else "Next",
                            fontWeight = FontWeight.Bold,
                        )
                        if (currentPage < totalPages - 1) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialPageWelcome() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TileCorrect.copy(alpha = 0.12f),
            modifier = Modifier.padding(bottom = 4.dp),
        ) {
            Text(
                text = "TUTORIAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = TileCorrect,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }

        Text(
            text = "Welcome to LexiGuess",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Your goal is to deduce the mystery 5-letter word in 6 guesses or fewer.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // Animated Glossy Tiles Demo
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 12.dp),
        ) {
            listOf('W', 'O', 'R', 'D', 'S').forEachIndexed { idx, char ->
                val bg = when (idx) {
                    0, 4 -> TileCorrect
                    1 -> TileMisplaced
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val textColor = if (idx == 2 || idx == 3) MaterialTheme.colorScheme.onSurface else Color.White

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = char.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                    )
                }
            }
        }

        // Rule Callout Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            RuleCard(
                icon = Icons.Default.Casino,
                title = "6 Guesses Allowed",
                desc = "Each round gives you up to 6 chances to crack the word.",
            )
            RuleCard(
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                title = "Valid Dictionary Words",
                desc = "Every guess must be a real English word. Letters can repeat (e.g. ROBOT, SPEED).",
            )
            RuleCard(
                icon = Icons.Default.Palette,
                title = "Color Coded Hints",
                desc = "After each guess, the tiles change color to reveal clues.",
            )
        }
    }
}

@Composable
private fun TutorialPageColors() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Decode the Colors",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )

        Text(
            text = "Tile colors show how close each letter in your guess is to the secret word:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // 1. Green (Correct)
        ColorExplanationCard(
            title = "GREEN — Spot On",
            caption = "The letter is in the word and in the EXACT right spot.",
            accentColor = TileCorrect,
            sampleWord = "SPARK",
            highlightIndex = 0,
            highlightColor = TileCorrect,
        )

        // 2. Yellow (Misplaced)
        ColorExplanationCard(
            title = "YELLOW — Wrong Position",
            caption = "The letter is in the word, but belongs in a DIFFERENT position.",
            accentColor = TileMisplaced,
            sampleWord = "CRANE",
            highlightIndex = 1,
            highlightColor = TileMisplaced,
        )

        // 3. Gray (Absent)
        ColorExplanationCard(
            title = "GRAY — Not in Word",
            caption = "The letter is NOT in the secret word anywhere.",
            accentColor = TileAbsent,
            sampleWord = "POUND",
            highlightIndex = 1,
            highlightColor = TileAbsent,
        )

        // Keyboard tracking tip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Tip: The on-screen keyboard automatically lights up with these colors so you never waste a guess!",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun TutorialPageInteractivePractice(
    soundManager: SoundManager? = null,
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Interactive Practice State
    // Mystery word is "LIGHT"
    var guess1Submitted by remember { mutableStateOf(false) }
    var guess2Submitted by remember { mutableStateOf(false) }
    var confettiTrigger by remember { mutableStateOf(false) }

    val row1Letters = if (guess1Submitted) "PLANT" else "     "
    val row1States = if (guess1Submitted) listOf(TileState.ABSENT, TileState.MISPLACED, TileState.ABSENT, TileState.ABSENT, TileState.CORRECT) else List(5) { TileState.EMPTY }

    val row2Letters = if (guess2Submitted) "LIGHT" else "     "
    val row2States = if (guess2Submitted) listOf(TileState.CORRECT, TileState.CORRECT, TileState.CORRECT, TileState.CORRECT, TileState.CORRECT) else List(5) { TileState.EMPTY }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Interactive Practice",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Try testing a guess to see the tile engine in action!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // Mini 2-Row Game Board
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            // Row 1
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until 5) {
                    val char = row1Letters.getOrNull(i) ?: ' '
                    val state = row1States[i]
                    PracticeCell(letter = char, state = state)
                }
            }

            // Row 2
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until 5) {
                    val char = row2Letters.getOrNull(i) ?: ' '
                    val state = row2States[i]
                    PracticeCell(letter = char, state = state)
                }
            }
        }

        // Action Buttons to simulate guesses
        if (!guess1Submitted) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    soundManager?.playKeyClick()
                    guess1Submitted = true
                    coroutineScope.launch {
                        delay(250)
                        soundManager?.playKeyClick()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Step 1: Test Guess 'PLANT'", fontWeight = FontWeight.Bold)
            }
        } else if (!guess2Submitted) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Analysis from 'PLANT':", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Text("• T is GREEN — It ends the word!", fontSize = 12.sp, color = TileCorrect, fontWeight = FontWeight.Bold)
                    Text("• L is YELLOW — It's in the word, but not in spot 2!", fontSize = 12.sp, color = TileMisplaced, fontWeight = FontWeight.Bold)
                    Text("• P, A, N are GRAY — Eliminated completely.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    soundManager?.playVictory()
                    guess2Submitted = true
                    confettiTrigger = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TileCorrect),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Step 2: Solve with 'LIGHT'", fontWeight = FontWeight.Bold)
            }
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TileCorrect.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, TileCorrect),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "🎉 Excellent Work!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TileCorrect,
                    )
                    Text(
                        text = "All 5 letters turned green. You've solved the practice puzzle and grasped the core mechanics!",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            TextButton(
                onClick = {
                    guess1Submitted = false
                    guess2Submitted = false
                    confettiTrigger = false
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Replay Practice")
            }
        }

        ConfettiParticleEngine(trigger = confettiTrigger)
    }
}

@Composable
private fun PracticeCell(
    letter: Char,
    state: TileState,
) {
    val bg = when (state) {
        TileState.CORRECT -> TileCorrect
        TileState.MISPLACED -> TileMisplaced
        TileState.ABSENT -> TileAbsent
        else -> Color.Transparent
    }
    val border = if (state == TileState.EMPTY) {
        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    } else null

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .then(if (border != null) Modifier.border(border, RoundedCornerShape(8.dp)) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (letter != ' ') letter.toString() else "",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = if (state == TileState.EMPTY) MaterialTheme.colorScheme.onSurface else Color.White,
        )
    }
}

@Composable
private fun TutorialPageGameModes() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Exciting Game Modes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )

        Text(
            text = "LexiGuess offers multiple game modes to challenge your vocabulary:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ModeInfoCard(
                icon = Icons.Outlined.WbSunny,
                title = "Daily Puzzle",
                desc = "One shared mystery puzzle each calendar day. Maintain daily streaks and earn Streak Shields!",
                badge = "Core",
            )
            ModeInfoCard(
                icon = Icons.Outlined.Flag,
                title = "Campaign (50 Levels)",
                desc = "Progress through 4 worlds (4 to 7 letters) and conquer 4 legendary Boss Battles with custom curses!",
                badge = "Adventure",
            )
            ModeInfoCard(
                icon = Icons.Outlined.Timer,
                title = "Timed Rush",
                desc = "Race against the clock in 2 frantic minutes to solve as many words as humanly possible.",
                badge = "Action",
            )
            ModeInfoCard(
                icon = Icons.Outlined.GridView,
                title = "Multi-Board (Dordle & Quordle)",
                desc = "Solve 2 or 4 secret words simultaneously using a synchronized split keyboard.",
                badge = "Strategy",
            )
            ModeInfoCard(
                icon = Icons.Outlined.People,
                title = "Pass and Play Duel",
                desc = "Hand your phone to a friend. Pick secret words for each other and race for the fewest guesses!",
                badge = "2-Player",
            )
            ModeInfoCard(
                icon = Icons.Outlined.AutoAwesome,
                title = "Cosmetics & Vault",
                desc = "Gain XP and level up to unlock tile materials (Carbon, Glass, Obsidian) and floating particles!",
                badge = "Rewards",
            )
        }
    }
}

@Composable
private fun RuleCard(
    icon: ImageVector,
    title: String,
    desc: String,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ColorExplanationCard(
    title: String,
    caption: String,
    accentColor: Color,
    sampleWord: String,
    highlightIndex: Int,
    highlightColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = title, fontWeight = FontWeight.Black, color = accentColor, style = MaterialTheme.typography.titleSmall)

                // Mini row of 5 letters
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    sampleWord.forEachIndexed { idx, char ->
                        val isHighlighted = idx == highlightIndex
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isHighlighted) highlightColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = char.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Text(text = caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModeInfoCard(
    icon: ImageVector,
    title: String,
    desc: String,
    badge: String,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }
                }
                Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
