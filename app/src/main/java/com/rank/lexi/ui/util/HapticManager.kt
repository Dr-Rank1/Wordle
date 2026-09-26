package com.rank.lexi.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight tactile feedback controller supporting modern VibrationEffect
 * waveforms and backward-compatible vibration patterns.
 */
@Singleton
class HapticManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /** Subtle crisp tick when typing a character or clicking a key */
    fun keyTick() {
        vibrate(durationMs = 12, amplitude = 50)
    }

    /** Distinctive double buzz for invalid words, illegal moves, or hard mode violations */
    fun errorBuzz() {
        if (vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 40, 35, 60)
            val amplitudes = intArrayOf(0, 180, 0, 240)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 40, 35, 60), -1)
        }
    }

    /** Celebratory pulse sequence played on puzzle victory */
    fun victoryPulse() {
        if (vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 35, 50, 45, 50, 80)
            val amplitudes = intArrayOf(0, 140, 0, 190, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 35, 50, 45, 50, 80), -1)
        }
    }

    /** Micro tick as individual tiles flip */
    fun tileFlipTick() {
        vibrate(durationMs = 8, amplitude = 35)
    }

    private fun vibrate(durationMs: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        if (vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }
}
