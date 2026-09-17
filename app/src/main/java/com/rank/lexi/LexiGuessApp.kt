package com.rank.lexi

import android.app.Application
import com.rank.lexi.ui.audio.SoundManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LexiGuessApp : Application() {

    @Inject
    lateinit var soundManager: SoundManager

    override fun onTerminate() {
        soundManager.release()
        super.onTerminate()
    }
}
