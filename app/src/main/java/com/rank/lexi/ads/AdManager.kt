package com.rank.lexi.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        // Replace with real rewarded ad unit ID before production release
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val TAG = "AdManager"
    }

    private var rewardedAd: RewardedAd? = null
    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

    private var _isRewardedLoading = false

    /** Pre-load the next rewarded ad. Call this eagerly (e.g. on app start or after a show). */
    fun loadRewardedAd() {
        if (_isAdReady.value || _isRewardedLoading) return
        _isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded")
                    rewardedAd = ad
                    _isAdReady.value = true
                    _isRewardedLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                    rewardedAd = null
                    _isAdReady.value = false
                    _isRewardedLoading = false
                }
            }
        )
    }

    /**
     * Show the loaded rewarded ad.
     *
     * @param activity The current foreground Activity.
     * @param onRewarded Callback with coin reward amount when the user earns the reward.
     * @param onAdClosed Called after the ad closes (reward may or may not have been granted).
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: (Int) -> Unit,
        onAdClosed: () -> Unit = {},
    ) {
        val ad = rewardedAd
        if (ad == null) {
            Log.w(TAG, "No rewarded ad available.")
            android.widget.Toast.makeText(activity, "Ad not ready yet. Trying to load...", android.widget.Toast.LENGTH_SHORT).show()
            onAdClosed()
            loadRewardedAd() // Try to reload
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _isAdReady.value = false
                onAdClosed()
                loadRewardedAd() // Pre-load next ad
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Rewarded ad failed to show: ${error.message}")
                rewardedAd = null
                _isAdReady.value = false
                onAdClosed()
                loadRewardedAd()
            }
        }

        ad.show(activity) { rewardItem ->
            val coins = rewardItem.amount.coerceAtLeast(50)
            Log.d(TAG, "User earned reward: $coins coins")
            onRewarded(coins)
        }
    }

    private var interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd? = null
    private var _isInterstitialLoading = false

    /** Pre-load the next interstitial ad. */
    fun loadInterstitialAd() {
        if (interstitialAd != null || _isInterstitialLoading) return
        _isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        com.google.android.gms.ads.interstitial.InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: com.google.android.gms.ads.interstitial.InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    _isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                    _isInterstitialLoading = false
                }
            }
        )
    }

    /** Show the loaded interstitial ad. */
    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            Log.w(TAG, "No interstitial ad available.")
            onAdClosed()
            loadInterstitialAd()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                onAdClosed()
                loadInterstitialAd() // Pre-load next ad
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Interstitial ad failed to show: ${error.message}")
                interstitialAd = null
                onAdClosed()
                loadInterstitialAd()
            }
        }
        
        ad.show(activity)
    }
}
