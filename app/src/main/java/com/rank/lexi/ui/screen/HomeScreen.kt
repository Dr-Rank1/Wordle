package com.rank.lexi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.lexi.data.repository.PlayerPreferences
import com.rank.lexi.domain.model.DailyStatus
import com.rank.lexi.ui.audio.SoundManager
import com.rank.lexi.ui.composable.CoinBalanceBadge
import com.rank.lexi.ui.composable.GameModeCard
import com.rank.lexi.ui.composable.HowToPlayDialog
import com.rank.lexi.ui.composable.SmallActionTile
import com.rank.lexi.ui.composable.StreakCalendarDialog
import com.rank.lexi.ui.theme.CardCampaign
import com.rank.lexi.ui.theme.CardCampaignLight
import com.rank.lexi.ui.theme.CardChallenge
import com.rank.lexi.ui.theme.CardChallengeLight
import com.rank.lexi.ui.theme.CardDaily
import com.rank.lexi.ui.theme.CardDailyLight
import com.rank.lexi.ui.theme.CardMulti
import com.rank.lexi.ui.theme.CardMultiLight
import com.rank.lexi.ui.theme.CardPassPlay
import com.rank.lexi.ui.theme.CardPassPlayLight
import com.rank.lexi.ui.theme.CardPractice
import com.rank.lexi.ui.theme.CardPracticeLight
import com.rank.lexi.ui.theme.CardRush
import com.rank.lexi.ui.theme.CardRushLight
import com.rank.lexi.ui.theme.HomeGradientBottom
import com.rank.lexi.ui.theme.HomeGradientTop
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    playerPreferences: PlayerPreferences,
    soundManager: SoundManager? = null,
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
    onNavigateToShop: () -> Unit = {},
    wonDates: Set<String> = emptySet(),
    shieldDates: Set<String> = emptySet(),
    dailyStatus: DailyStatus = DailyStatus.NOT_STARTED,
    continueCampaignLevel: Int? = null,
    onStartArchiveDaily: ((java.time.LocalDate) -> Unit)? = null,
    themePref: String = "SYSTEM",
    darkMode: Boolean = true,
    onToggleTheme: () -> Unit = {},
    coins: Int = 0,
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

    // Daily card CTA label
    val dailyCta = when (dailyStatus) {
        DailyStatus.NOT_STARTED -> "PLAY"
        DailyStatus.IN_PROGRESS -> "CONTINUE"
        DailyStatus.WON -> "RESULT"
        DailyStatus.LOST -> "REVEAL"
    }
    val dailySubtitle = when (dailyStatus) {
        DailyStatus.NOT_STARTED -> "Same word for everyone · ${currentStreak}🔥 streak"
        DailyStatus.IN_PROGRESS -> "Continue today's puzzle · ${currentStreak}🔥 streak"
        DailyStatus.WON -> "You solved it! Share your result"
        DailyStatus.LOST -> "See what today's word was"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "LEXIGUESS",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        TextButton(
                            onClick = { showStreakCalendar = true },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp),
                        ) {
                            Text(
                                text = "Lv$currentLevel  ·  $currentStreak day streak",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Coin balance badge
                        CoinBalanceBadge(coins = coins)
                        IconButton(onClick = { showHowToPlay = true }) {
                            Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = "How to play", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                        }
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = when (themePref) {
                                    "LIGHT" -> Icons.Outlined.LightMode
                                    "DARK" -> Icons.Outlined.DarkMode
                                    else -> Icons.Outlined.PhoneAndroid
                                },
                                contentDescription = "Cycle display theme",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {

        // ── Hero: Daily card ────────────────────────────────────────────────
        GameModeCard(
            title = "Daily Word",
            subtitle = dailySubtitle,
            icon = Icons.Outlined.CalendarMonth,
            gradientStart = CardDaily,
            gradientEnd = CardDailyLight,
            ctaLabel = dailyCta,
            modifier = Modifier.fillMaxWidth(),
            onClick = onStartDaily,
        )

        // Continue Campaign banner (only if in progress)
        if (continueCampaignLevel != null) {
            GameModeCard(
                title = "Campaign — Level $continueCampaignLevel",
                subtitle = "Continue where you left off",
                icon = Icons.Outlined.Flag,
                gradientStart = CardCampaign,
                gradientEnd = CardCampaignLight,
                ctaLabel = "CONTINUE",
                modifier = Modifier.fillMaxWidth(),
                onClick = { onContinueCampaign(continueCampaignLevel) },
            )
        }

        // ── Two-column: Rush | Campaign ──────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GameModeCard(
                title = "Rush",
                subtitle = if (rushHighScore > 0) "Best: $rushHighScore" else "2 minutes",
                icon = Icons.Outlined.Bolt,
                gradientStart = CardRush,
                gradientEnd = CardRushLight,
                modifier = Modifier.weight(1f),
                onClick = onStartRush,
            )
            GameModeCard(
                title = "Campaign",
                subtitle = "50 levels",
                icon = Icons.Outlined.EmojiEvents,
                gradientStart = CardCampaign,
                gradientEnd = CardCampaignLight,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToLevels,
            )
        }

        // ── Two-column: Multi-board | Practice ───────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GameModeCard(
                title = "Multi",
                subtitle = "2 or 4 words",
                icon = Icons.Outlined.GridView,
                gradientStart = CardMulti,
                gradientEnd = CardMultiLight,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToMultiBoard,
            )
            GameModeCard(
                title = "Practice",
                subtitle = "4–7 letters",
                icon = Icons.Outlined.Spellcheck,
                gradientStart = CardPractice,
                gradientEnd = CardPracticeLight,
                modifier = Modifier.weight(1f),
                onClick = { showLengthPicker = true },
            )
        }

        // ── Full-width: Challenge | Pass & Play ──────────────────────────────
        GameModeCard(
            title = "Challenge a Friend",
            subtitle = "Share your own secret word",
            icon = Icons.Outlined.Link,
            gradientStart = CardChallenge,
            gradientEnd = CardChallengeLight,
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToChallenge,
        )
        GameModeCard(
            title = "Pass & Play",
            subtitle = "Two players, one phone",
            icon = Icons.Outlined.Group,
            gradientStart = CardPassPlay,
            gradientEnd = CardPassPlayLight,
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToPassAndPlay,
        )

        // ── Small action tiles grid (3 per row) ──────────────────────────────
        Spacer(Modifier.height(2.dp))
        Text(
            "MORE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            letterSpacing = 2.sp,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SmallActionTile("Vault", Icons.Outlined.Book, Modifier.weight(1f), onClick = onNavigateToVault)
            SmallActionTile("Quests", Icons.AutoMirrored.Outlined.Assignment, Modifier.weight(1f), onClick = onNavigateToQuests)
            SmallActionTile("Shop", Icons.Outlined.Storefront, Modifier.weight(1f), onClick = onNavigateToShop)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SmallActionTile("Look", Icons.Outlined.Palette, Modifier.weight(1f), onClick = onNavigateToCosmetics)
            SmallActionTile("Stats", Icons.Outlined.BarChart, Modifier.weight(1f), onClick = onNavigateToStats)
            SmallActionTile("Tutorial", Icons.AutoMirrored.Outlined.HelpOutline, Modifier.weight(1f), onClick = { showHowToPlay = true })
        }

        Spacer(Modifier.height(8.dp))
        
        com.rank.lexi.ui.composable.BannerAd()
        
        Spacer(Modifier.height(8.dp))
    }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
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
            soundManager = soundManager,
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
