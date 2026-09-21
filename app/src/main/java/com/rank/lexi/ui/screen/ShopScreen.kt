package com.rank.lexi.ui.screen

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rank.lexi.data.db.CoinRecord
import com.rank.lexi.domain.model.ShopItem
import com.rank.lexi.ui.composable.LexiTopBar
import com.rank.lexi.ui.theme.CoinGold
import com.rank.lexi.ui.theme.ShopAccent
import com.rank.lexi.ui.viewmodel.ShopEvent
import com.rank.lexi.ui.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onBack: () -> Unit,
    viewModel: ShopViewModel = hiltViewModel(),
) {
    val coins by viewModel.coinsFlow.collectAsState(initial = 0)
    val isAdReady by viewModel.isAdReady.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState(initial = emptyList())
    val activity = LocalContext.current as Activity
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is ShopEvent.CoinsEarned -> snackbarHostState.showSnackbar("🎉 +${ShopItem.COIN_REWARD_PER_AD} coins earned!")
                is ShopEvent.PurchaseSuccess -> snackbarHostState.showSnackbar("✅ ${event.item.displayName} applied!")
                is ShopEvent.PurchaseFailed -> snackbarHostState.showSnackbar(event.reason)
            }
        }
    }

    Scaffold(
        topBar = {
            LexiTopBar(
                title = "Shop",
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Coin balance header
            item {
                Spacer(Modifier.height(8.dp))
                CoinBalanceHeader(coins = coins)
            }

            // Earn Coins section
            item {
                Text(
                    "EARN COINS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                WatchAdCard(
                    isReady = isAdReady,
                    onWatch = { viewModel.watchAd(activity) },
                )
            }

            // Power-ups section
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "POWER-UPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(viewModel.shopItems) { item ->
                ShopItemCard(
                    item = item,
                    currentCoins = coins,
                    onBuy = { viewModel.purchase(item) },
                )
            }

            // Transaction history
            if (recentTransactions.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "RECENT ACTIVITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(recentTransactions) { record ->
                    TransactionRow(record)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun CoinBalanceHeader(coins: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF7C4DFF), Color(0xFF651FFF))
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Outlined.Storefront,
                contentDescription = "Shop",
                tint = CoinGold,
                modifier = Modifier.size(42.dp),
            )
            Spacer(Modifier.height(8.dp))
            AnimatedContent(
                targetState = coins,
                transitionSpec = {
                    (slideInVertically { -it } + fadeIn()) togetherWith
                        (slideOutVertically { it } + fadeOut())
                },
                label = "coinCount",
            ) { count ->
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = CoinGold,
                )
            }
            Text(
                text = "coins available",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun WatchAdCard(isReady: Boolean, onWatch: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B5E20),
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Watch a Short Ad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "+${ShopItem.COIN_REWARD_PER_AD} coins",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoinGold,
                )
            }
            Button(
                onClick = onWatch,
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoinGold,
                    contentColor = Color.Black,
                    disabledContainerColor = MaterialTheme.colorScheme.outline,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text("Watch", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    currentCoins: Int,
    onBuy: () -> Unit,
) {
    val canAfford = currentCoins >= item.coinCost
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Item icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ShopAccent.copy(alpha = 0.18f))
                    .border(1.dp, ShopAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                val icon = when (item) {
                    is ShopItem.ExtraGuess -> androidx.compose.material.icons.Icons.Outlined.AddCircleOutline
                    is ShopItem.RevealLetter -> androidx.compose.material.icons.Icons.Outlined.Lightbulb
                    is ShopItem.SkipWord -> androidx.compose.material.icons.Icons.Outlined.FastForward
                    is ShopItem.StreakFreeze -> androidx.compose.material.icons.Icons.Outlined.Shield
                }
                Icon(
                    imageVector = icon,
                    contentDescription = item.displayName,
                    tint = ShopAccent,
                    modifier = Modifier.size(28.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Coin cost pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (canAfford) CoinGold.copy(alpha = 0.18f) else Color.Gray.copy(alpha = 0.15f))
                        .border(1.dp, if (canAfford) CoinGold.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 13.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${item.coinCost}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) CoinGold else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Button(
                    onClick = onBuy,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShopAccent,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.outline,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.height(34.dp),
                ) {
                    Text("Buy", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(record: CoinRecord) {
    val dateStr = remember(record.timestampMs) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(record.timestampMs))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(record.description, style = MaterialTheme.typography.bodySmall)
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (record.amount > 0) "+${record.amount} 🪙" else "${record.amount} 🪙",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (record.amount > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
        )
    }
}
