package com.rank.lexi

import android.app.Application
import com.rank.lexi.ui.audio.SoundManager
import com.startapp.sdk.adsbase.StartAppSDK
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LexiGuessApp : Application() {

    @Inject
    lateinit var soundManager: SoundManager

    @Inject
    lateinit var wordRepository: com.rank.lexi.data.repository.WordRepository

    override fun onCreate() {
        super.onCreate()

        // ── Start.io initialisation (Live Ads) ────────────────────────────────
        StartAppSDK.setTestAdsEnabled(false)
        @Suppress("DEPRECATION")
        StartAppSDK.initParams(this, "208916292")
            .setReturnAdsEnabled(true) // Acts as App Open ad
            .init()

        // ── Daily Streak Protection Reminder ─────────────────────────────────
        com.rank.lexi.util.StreakReminderScheduler.scheduleDailyReminder(this)

        // ── Pre-load dictionaries eagerly on IO thread ────────────────────────
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            wordRepository.initialize()
        }
    }

    override fun onTerminate() {
        soundManager.release()
        super.onTerminate()
    }
}
