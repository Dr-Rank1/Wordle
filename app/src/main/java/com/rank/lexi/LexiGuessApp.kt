package com.rank.lexi

import android.app.Application
import com.rank.lexi.ui.audio.SoundManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LexiGuessApp : Application() {

    @Inject
    lateinit var soundManager: SoundManager

    override fun onCreate() {
        super.onCreate()
        val testDeviceIds = listOf("7F25E14F4DEF18B55074D4170E424E3A")
        val configuration = com.google.android.gms.ads.RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        com.google.android.gms.ads.MobileAds.setRequestConfiguration(configuration)
        com.google.android.gms.ads.MobileAds.initialize(this) {}
    }

    override fun onTerminate() {
        soundManager.release()
        super.onTerminate()
    }
}
