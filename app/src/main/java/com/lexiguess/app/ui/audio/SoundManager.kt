package com.lexiguess.app.ui.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.lexiguess.app.domain.model.TileState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Lightweight procedural audio synthesizer for LexiGuess.
 * Generates pure waveform tones on the fly via [AudioTrack] without external audio assets.
 */
@Singleton
class SoundManager @Inject constructor() {

    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    var isEnabled: Boolean = true

    private val sampleRate = 44100

    /** Tactile, subtle typewriter/wood-block click on typing a letter. */
    fun playKeyClick() {
        if (!isEnabled) return
        audioScope.launch {
            val durationMs = 15
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val decay = exp(-t * 220.0)
                val sample = sin(2.0 * PI * 320.0 * t) * decay * 0.35
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    /**
     * Ascending musical note played as tiles flip.
     * Absent = low wood tone, Misplaced = mid warm chime, Correct = high bell note.
     */
    fun playTileFlip(tileState: TileState, column: Int) {
        if (!isEnabled) return
        audioScope.launch {
            val baseFreq = when (tileState) {
                TileState.CORRECT -> 523.25 * Math.pow(1.122, column.toDouble()) // C5 scale
                TileState.MISPLACED -> 440.0 // A4
                TileState.ABSENT -> 220.0    // A3
                else -> return@launch
            }

            val durationMs = 110
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val decay = exp(-t * 30.0)
                // Fundamental + soft overtone for warm timbre
                val fundamental = sin(2.0 * PI * baseFreq * t)
                val overtone = sin(4.0 * PI * baseFreq * t) * 0.3
                val sample = (fundamental + overtone) * decay * 0.3
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    /** Triumphant 5-note rising melodic arpeggio on round victory. */
    fun playVictory() {
        if (!isEnabled) return
        audioScope.launch {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 987.77, 1046.50) // C5, E5, G5, B5, C6
            val noteDurationMs = 80
            val noteSamples = (sampleRate * noteDurationMs) / 1000
            val buffer = ShortArray(noteSamples * notes.size)

            var offset = 0
            for (freq in notes) {
                for (i in 0 until noteSamples) {
                    val t = i.toDouble() / sampleRate
                    val decay = exp(-t * 18.0)
                    val sample = sin(2.0 * PI * freq * t) * decay * 0.35
                    buffer[offset++] = (sample * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            playPcm(buffer)
        }
    }

    /** Dull double-thud when an invalid word is submitted. */
    fun playError() {
        if (!isEnabled) return
        audioScope.launch {
            val durationMs = 90
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val decay = exp(-t * 40.0)
                val sample = sin(2.0 * PI * 130.0 * t) * decay * 0.4
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    private fun playPcm(pcm: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            // Reliably release after playback finishes even on Looper-less coroutine worker threads
            val playDurationMs = (pcm.size * 1000L) / sampleRate + 80L
            audioScope.launch {
                delay(playDurationMs)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // AudioTrack allocation fallback
        }
    }
}
