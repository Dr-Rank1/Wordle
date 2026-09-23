package com.rank.lexi

import android.app.Application
import com.rank.lexi.ui.audio.SoundManager
import com.startapp.sdk.adsbase.StartAppSDK
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LexiGuessApp : Application() {

    @Inject
    lateinit var soundManager: SoundManager

    override fun onCreate() {
        super.onCreate()

        // ── Start.io initialisation ──────────────────────────────────────────
        @Suppress("DEPRECATION")
        StartAppSDK.initParams(this, "208916292")
            .setReturnAdsEnabled(true) // Acts as App Open ad
            .init()
    }

    override fun onTerminate() {
        soundManager.release()
        super.onTerminate()
    }
}
