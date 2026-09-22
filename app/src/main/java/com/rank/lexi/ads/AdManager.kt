package com.rank.lexi.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "AdManager"
    }

    var isShowingFullscreenAd = false
        private set

    // StartAppAd instances
    private val interstitialAd = StartAppAd(context)
    private val rewardedAd = StartAppAd(context)

    private val _isRewardedReady = MutableStateFlow(false)
    val isAnyRewardedReady: Flow<Boolean> = _isRewardedReady.asStateFlow()

    private val _isInterstitialReady = MutableStateFlow(false)

    fun loadRewardedAd() {
        if (_isRewardedReady.value) return
        rewardedAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                Log.d(TAG, "Start.io Rewarded Video loaded")
                _isRewardedReady.value = true
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                Log.e(TAG, "Start.io Rewarded Video failed to load: ${ad?.errorMessage}")
                _isRewardedReady.value = false
            }
        })
    }

    fun showRewardedAd(
        activity: Activity,
        onRewarded: (Int) -> Unit,
        onAdClosed: () -> Unit = {},
    ) {
        if (!_isRewardedReady.value) {
            Toast.makeText(activity, "Ad not ready yet. Loading one now...", Toast.LENGTH_SHORT).show()
            onAdClosed()
            loadRewardedAd()
            return
        }

        var rewardEarned = false

        rewardedAd.setVideoListener {
            Log.d(TAG, "Start.io Rewarded Video completed")
            rewardEarned = true
            onRewarded(50)
        }

        isShowingFullscreenAd = true
        val showed = rewardedAd.showAd(object : AdDisplayListener {
            override fun adHidden(ad: Ad) {
                isShowingFullscreenAd = false
                _isRewardedReady.value = false
                if (!rewardEarned) {
                    Log.d(TAG, "Start.io Rewarded Video closed before completion")
                }
                onAdClosed()
                loadRewardedAd()
            }
            override fun adDisplayed(ad: Ad) {}
            override fun adClicked(ad: Ad) {}
            override fun adNotDisplayed(ad: Ad) {
                isShowingFullscreenAd = false
                _isRewardedReady.value = false
                onAdClosed()
                loadRewardedAd()
            }
        })

        if (!showed) {
            isShowingFullscreenAd = false
            _isRewardedReady.value = false
            onAdClosed()
            loadRewardedAd()
        }
    }

    fun loadInterstitialAd() {
        if (_isInterstitialReady.value) return
        interstitialAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                _isInterstitialReady.value = true
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                _isInterstitialReady.value = false
            }
        })
    }

    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        if (!_isInterstitialReady.value) {
            onAdClosed()
            loadInterstitialAd()
            return
        }

        isShowingFullscreenAd = true
        val showed = interstitialAd.showAd(object : AdDisplayListener {
            override fun adHidden(ad: Ad) {
                isShowingFullscreenAd = false
                _isInterstitialReady.value = false
                onAdClosed()
                loadInterstitialAd()
            }
            override fun adDisplayed(ad: Ad) {}
            override fun adClicked(ad: Ad) {}
            override fun adNotDisplayed(ad: Ad) {
                isShowingFullscreenAd = false
                _isInterstitialReady.value = false
                onAdClosed()
                loadInterstitialAd()
            }
        })

        if (!showed) {
            isShowingFullscreenAd = false
            _isInterstitialReady.value = false
            onAdClosed()
            loadInterstitialAd()
        }
    }

    fun loadAppOpenAd() {
        // Handled automatically by Start.io Return Ads
    }

    fun showAppOpenAdIfAvailable(activity: Activity, onAdDismissed: () -> Unit = {}) {
        // Handled automatically by Start.io Return Ads
        onAdDismissed()
    }
}
