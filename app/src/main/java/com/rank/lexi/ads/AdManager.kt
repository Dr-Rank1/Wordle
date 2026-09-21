package com.rank.lexi.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "AdManager"
        // Official Google AdMob test ad unit IDs
        private const val REWARDED_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    }

    // ─── REWARDED & REWARDED INTERSTITIAL ADS ────────────────────────────────
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

    /** Pre-loads rewarded ads. Prefers RewardedInterstitial for reliable close buttons and timer. */
    fun loadRewardedAd() {
        if (_isAdReady.value || isRewardedLoading) return
        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()

        // 1. Try loading Rewarded Interstitial (includes 5s timer + clear exit button)
        RewardedInterstitialAd.load(
            context,
            REWARDED_INTERSTITIAL_UNIT_ID,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "Rewarded Interstitial ad loaded successfully")
                    rewardedInterstitialAd = ad
                    _isAdReady.value = true
                    isRewardedLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded Interstitial load failed: ${error.message}. Trying standard RewardedAd...")
                    rewardedInterstitialAd = null
                    // 2. Fallback to standard RewardedAd
                    RewardedAd.load(
                        context,
                        REWARDED_AD_UNIT_ID,
                        adRequest,
                        object : RewardedAdLoadCallback() {
                            override fun onAdLoaded(ad: RewardedAd) {
                                Log.d(TAG, "Standard RewardedAd loaded successfully")
                                rewardedAd = ad
                                _isAdReady.value = true
                                isRewardedLoading = false
                            }

                            override fun onAdFailedToLoad(err: LoadAdError) {
                                Log.w(TAG, "Both rewarded ad types failed to load: ${err.message}")
                                rewardedAd = null
                                _isAdReady.value = false
                                isRewardedLoading = false
                            }
                        }
                    )
                }
            }
        )
    }

    /**
     * Shows a rewarded ad. Either Rewarded Interstitial or standard Rewarded Ad.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: (Int) -> Unit,
        onAdClosed: () -> Unit = {},
    ) {
        val rInter = rewardedInterstitialAd
        val rAd = rewardedAd

        if (rInter != null) {
            rInter.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedInterstitialAd = null
                    _isAdReady.value = false
                    onAdClosed()
                    loadRewardedAd()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(TAG, "Rewarded interstitial failed to show: ${error.message}")
                    rewardedInterstitialAd = null
                    _isAdReady.value = false
                    onAdClosed()
                    loadRewardedAd()
                }
            }
            rInter.show(activity) { rewardItem ->
                val coins = rewardItem.amount.coerceAtLeast(50)
                Log.d(TAG, "User earned reward from rewarded interstitial: $coins coins")
                onRewarded(coins)
            }
            return
        }

        if (rAd != null) {
            rAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    _isAdReady.value = false
                    onAdClosed()
                    loadRewardedAd()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(TAG, "Rewarded ad failed to show: ${error.message}")
                    rewardedAd = null
                    _isAdReady.value = false
                    onAdClosed()
                    loadRewardedAd()
                }
            }
            rAd.show(activity) { rewardItem ->
                val coins = rewardItem.amount.coerceAtLeast(50)
                Log.d(TAG, "User earned reward from rewarded ad: $coins coins")
                onRewarded(coins)
            }
            return
        }

        Log.w(TAG, "No rewarded ad available to show.")
        android.widget.Toast.makeText(activity, "Ad not ready yet. Loading one now...", android.widget.Toast.LENGTH_SHORT).show()
        onAdClosed()
        loadRewardedAd()
    }

    // ─── INTERSTITIAL ADS ────────────────────────────────────────────────────
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    /** Pre-load the next interstitial ad. */
    fun loadInterstitialAd() {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                    isInterstitialLoading = false
                }
            }
        )
    }

    /** Show the loaded interstitial ad. */
    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interstitial ad not ready, proceeding immediately.")
            onAdClosed()
            loadInterstitialAd()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                onAdClosed()
                loadInterstitialAd()
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

    // ─── APP OPEN ADS ────────────────────────────────────────────────────────
    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var appOpenLoadTime: Long = 0
    var isShowingAppOpenAd = false
        private set

    /** Pre-loads an App Open ad for when the user opens or foregrounds the app. */
    fun loadAppOpenAd() {
        if (isAppOpenAdAvailable() || isAppOpenLoading) return
        isAppOpenLoading = true
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            APP_OPEN_AD_UNIT_ID,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App Open ad loaded successfully")
                    appOpenAd = ad
                    appOpenLoadTime = Date().time
                    isAppOpenLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "App Open ad failed to load: ${error.message}")
                    appOpenAd = null
                    isAppOpenLoading = false
                }
            }
        )
    }

    /** Returns true if an App Open ad is available and less than 4 hours old. */
    fun isAppOpenAdAvailable(): Boolean {
        val numHours = 4
        val dateDifference = Date().time - appOpenLoadTime
        val numMilliSecondsPerHour: Long = 3600000
        return appOpenAd != null && dateDifference < (numMilliSecondsPerHour * numHours)
    }

    /** Shows the App Open ad if available. */
    fun showAppOpenAdIfAvailable(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (isShowingAppOpenAd) {
            onAdDismissed()
            return
        }

        if (!isAppOpenAdAvailable()) {
            loadAppOpenAd()
            onAdDismissed()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAppOpenAd = false
                onAdDismissed()
                loadAppOpenAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                isShowingAppOpenAd = false
                onAdDismissed()
                loadAppOpenAd()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAppOpenAd = true
            }
        }
        isShowingAppOpenAd = true
        appOpenAd?.show(activity)
    }
}
