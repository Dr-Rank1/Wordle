package com.rank.lexi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.lexi.data.repository.PlayerPreferences
import com.rank.lexi.domain.model.DailyStatus
import com.rank.lexi.ui.composable.HowToPlayDialog
import com.rank.lexi.ui.composable.LexiCard
import com.rank.lexi.ui.composable.LexiSectionLabel
import com.rank.lexi.ui.composable.ModeTile
import com.rank.lexi.ui.composable.StreakCalendarDialog
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    playerPreferences: PlayerPreferences,
    currentStreak: Int = 0,
    bestStreak: Int = 0,
    onStartDaily: () -> Unit,
    onStartRush: () -> Unit,
    onNavigateToLevels: () -> Unit,
    onStartPractice: (Int) -> Unit,
    onContinueCampaign: (Int) -> Unit = {},
    onNavigateToMultiBoard: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToQuests: () -> Unit = {},
    onNavigateToCosmetics: () -> Unit = {},
    onNavigateToPassAndPlay: () -> Unit = {},
    wonDates: Set<String> = emptySet(),
    shieldDates: Set<String> = emptySet(),
    dailyStatus: DailyStatus = DailyStatus.NOT_STARTED,
    continueCampaignLevel: Int? = null,
    onStartArchiveDaily: ((java.time.LocalDate) -> Unit)? = null,
    themePref: String = "SYSTEM",
    darkMode: Boolean = true,
    onToggleTheme: () -> Unit = {},
) {
    val xp by playerPreferences.xpFlow.collectAsState(initial = 0)
    val rushHighScore by playerPreferences.rushHighScoreFlow.collectAsState(initial = 0)
    val streakFreezes by playerPreferences.streakFreezesFlow.collectAsState(initial = 1)
    val hasSeenHowToPlay by playerPreferences.hasSeenHowToPlayFlow.collectAsState(initial = true)
    val scope = rememberCoroutineScope()

    val currentLevel = PlayerPreferences.calculateLevel(xp)

    var showLengthPicker by remember { mutableStateOf(false) }
    var showStreakCalendar by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }

    LaunchedEffect(hasSeenHowToPlay) {
        if (!hasSeenHowToPlay) showHowToPlay = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "LEXIGUESS",
                    style = MaterialTheme.typography.headlineLarge,
                    letterSpacing = 3.sp,
                )
                TextButton(
                    onClick = { showStreakCalendar = true },
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                ) {
                    Text(
                        text = "Level $currentLevel  ·  $currentStreak day streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showHowToPlay = true }) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = "How to play")
                }
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = when (themePref) {
                            "LIGHT" -> Icons.Outlined.LightMode
                            "DARK" -> Icons.Outlined.DarkMode
                            else -> Icons.Outlined.PhoneAndroid
                        },
                        contentDescription = "Cycle display theme",
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            }
        }

        val dailyHeadline = when (dailyStatus) {
            DailyStatus.NOT_STARTED -> "Play today’s puzzle"
            DailyStatus.IN_PROGRESS -> "Continue today’s puzzle"
            DailyStatus.WON -> "Solved — share your grid"
            DailyStatus.LOST -> "See today’s word"
        }
        val dailyAction = when (dailyStatus) {
            DailyStatus.NOT_STARTED -> "Play"
            DailyStatus.IN_PROGRESS -> "Continue"
            DailyStatus.WON -> "Result"
            DailyStatus.LOST -> "Reveal"
        }

        LexiCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onStartDaily,
            contentDescription = dailyHeadline,
        ) {
            Text(
                text = "DAILY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(dailyHeadline, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Same word for everyone. Generated on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onStartDaily,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(dailyAction)
                }
            }
        }

        if (continueCampaignLevel != null) {
            LexiCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onContinueCampaign(continueCampaignLevel) },
                contentDescription = "Continue campaign, level $continueCampaignLevel",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Continue campaign", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Level $continueCampaignLevel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.Outlined.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        LexiSectionLabel("Play")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeTile("Rush", if (rushHighScore > 0) "Best $rushHighScore" else "Two minutes", Icons.Outlined.Timer, Modifier.weight(1f), onStartRush)
            ModeTile("Campaign", "50 stages", Icons.Outlined.Flag, Modifier.weight(1f), onNavigateToLevels)
        }
        ModeTile("Practice", "4 to 7 letters", Icons.Outlined.Tune, Modifier.fillMaxWidth()) {
            showLengthPicker = true
        }

        LexiSectionLabel("Together")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeTile("Multi-board", "Two or four words", Icons.Outlined.GridView, Modifier.weight(1f), onNavigateToMultiBoard)
            ModeTile("Pass and play", "Two players, one phone", Icons.Outlined.People, Modifier.weight(1f), onNavigateToPassAndPlay)
        }
        ModeTile("Challenge", "Share a custom word", Icons.Outlined.QrCode, Modifier.fillMaxWidth(), onNavigateToChallenge)

        LexiSectionLabel("More")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeTile("Vault", "Words you’ve found", Icons.AutoMirrored.Outlined.MenuBook, Modifier.weight(1f), onNavigateToVault)
            ModeTile("Quests", "A few daily goals", Icons.AutoMirrored.Outlined.Assignment, Modifier.weight(1f), onNavigateToQuests)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeTile("Look", "Colors and tiles", Icons.Outlined.Style, Modifier.weight(1f), onNavigateToCosmetics)
            ModeTile("Stats", "Wins and streaks", Icons.Outlined.BarChart, Modifier.weight(1f), onNavigateToStats)
        }
    }

    if (showLengthPicker) {
        AlertDialog(
            onDismissRequest = { showLengthPicker = false },
            title = { Text("Word length") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(4, 5, 6, 7).forEach { len ->
                        OutlinedButton(
                            onClick = {
                                showLengthPicker = false
                                onStartPractice(len)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        ) { Text("$len letters") }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showLengthPicker = false }) { Text("Cancel") } },
        )
    }

    if (showHowToPlay) {
        HowToPlayDialog(
            onDismiss = {
                showHowToPlay = false
                scope.launch { playerPreferences.setHasSeenHowToPlay() }
            },
        )
    }

    if (showStreakCalendar) {
        StreakCalendarDialog(
            currentStreak = currentStreak,
            maxStreak = if (bestStreak > 0) bestStreak else currentStreak,
            streakFreezes = streakFreezes,
            wonDates = wonDates,
            shieldDates = shieldDates,
            onPlayArchiveDate = onStartArchiveDaily,
            onDismiss = { showStreakCalendar = false },
        )
    }
}
