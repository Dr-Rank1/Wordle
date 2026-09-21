package com.rank.lexi.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rank.lexi.ads.AdManager
import com.rank.lexi.data.db.CoinRecord
import com.rank.lexi.data.repository.CoinRepository
import com.rank.lexi.data.repository.PlayerPreferences
import com.rank.lexi.domain.model.ShopItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ShopEvent {
    data class PurchaseSuccess(val item: ShopItem) : ShopEvent()
    data class PurchaseFailed(val reason: String) : ShopEvent()
    object CoinsEarned : ShopEvent()
}

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val coinRepository: CoinRepository,
    private val adManager: AdManager,
    private val playerPreferences: PlayerPreferences,
) : ViewModel() {

    val coinsFlow: Flow<Int> = coinRepository.coinsFlow
    val recentTransactions: Flow<List<CoinRecord>> = coinRepository.recentTransactionsFlow
    val isAdReady: StateFlow<Boolean> = adManager.isAdReady
    val shopItems: List<ShopItem> = ShopItem.allItems

    private val _events = MutableSharedFlow<ShopEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ShopEvent> = _events.asSharedFlow()

    init {
        adManager.loadRewardedAd()
    }

    fun watchAd(activity: Activity) {
        adManager.showRewardedAd(
            activity = activity,
            onRewarded = { coins ->
                viewModelScope.launch {
                    coinRepository.addCoins(
                        amount = coins,
                        type = "EARN_AD",
                        description = "Watched rewarded ad",
                    )
                    _events.emit(ShopEvent.CoinsEarned)
                }
            },
        )
    }

    fun purchase(item: ShopItem) {
        viewModelScope.launch {
            val success = coinRepository.spendCoins(
                amount = item.coinCost,
                type = item.transactionType,
                description = "Purchased: ${item.displayName}",
            )
            if (success) {
                // For StreakFreeze, apply it directly
                if (item is ShopItem.StreakFreeze) {
                    playerPreferences.addStreakFreeze(1)
                }
                _events.emit(ShopEvent.PurchaseSuccess(item))
            } else {
                _events.emit(ShopEvent.PurchaseFailed("Not enough coins. Watch an ad to earn more!"))
            }
        }
    }

    /**
     * Purchase an item and immediately apply its in-game effect via [gameViewModel].
     * Called from [com.rank.lexi.ui.screen.GameScreen]'s PowerUpBar.
     * @return `true` if coins were spent and the effect was applied.
     */
    suspend fun purchaseAndApply(
        item: ShopItem,
        gameViewModel: com.rank.lexi.ui.viewmodel.GameViewModel,
    ): Boolean {
        val success = coinRepository.spendCoins(
            amount = item.coinCost,
            type = item.transactionType,
            description = "Power-up in game: ${item.displayName}",
        )
        if (!success) {
            _events.emit(ShopEvent.PurchaseFailed("Not enough coins!"))
            return false
        }
        when (item) {
            is ShopItem.ExtraGuess -> gameViewModel.applyExtraGuess()
            is ShopItem.RevealLetter -> gameViewModel.applyRevealLetter()
            is ShopItem.SkipWord -> gameViewModel.applySkipWord()
            is ShopItem.StreakFreeze -> playerPreferences.addStreakFreeze(1)
        }
        _events.emit(ShopEvent.PurchaseSuccess(item))
        return true
    }
}
