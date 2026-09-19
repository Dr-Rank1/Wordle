package com.rank.lexi.ui.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.rank.lexi.domain.model.TileState
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Lightweight procedural audio synthesizer for LexiGuess.
 * Pre-computes pure waveform tones once and reuses static [AudioTrack] instances
 * for zero-allocation, low-latency audio playback without AudioFlinger track exhaustion.
 */
@Singleton
class SoundManager @Inject constructor() {

    var isEnabled: Boolean = true
    private val sampleRate = 44100

    private val keyClickTrack: AudioTrack? by lazy { createStaticTrack(generateKeyClickPcm()) }
    private val errorTrack: AudioTrack? by lazy { createStaticTrack(generateErrorPcm()) }
    private val victoryTrack: AudioTrack? by lazy { createStaticTrack(generateVictoryPcm()) }
    private val absentTrack: AudioTrack? by lazy { createStaticTrack(generateTileFlipPcm(220.0)) }
    private val misplacedTrack: AudioTrack? by lazy { createStaticTrack(generateTileFlipPcm(440.0)) }
    private val correctTracks: List<AudioTrack?> by lazy {
        (0..6).map { col ->
            val freq = 523.25 * Math.pow(1.122, col.toDouble())
            createStaticTrack(generateTileFlipPcm(freq))
        }
    }

    /** Tactile, subtle typewriter/wood-block click on typing a letter. */
    fun playKeyClick() {
        playTrack(keyClickTrack)
    }

    /**
     * Ascending musical note played as tiles flip.
     * Absent = low wood tone, Misplaced = mid warm chime, Correct = high bell note.
     */
    fun playTileFlip(tileState: TileState, column: Int) {
        when (tileState) {
            TileState.CORRECT -> {
                val track = correctTracks.getOrNull(column.coerceIn(0, 6))
                playTrack(track)
            }
            TileState.MISPLACED -> playTrack(misplacedTrack)
            TileState.ABSENT -> playTrack(absentTrack)
            else -> {}
        }
    }

    /** Triumphant 5-note rising melodic arpeggio on round victory. */
    fun playVictory() {
        playTrack(victoryTrack)
    }

    /** Dull double-thud when an invalid word is submitted. */
    fun playError() {
        playTrack(errorTrack)
    }

    private fun playTrack(track: AudioTrack?) {
        if (!isEnabled || track == null) return
        try {
            if (track.state != AudioTrack.STATE_INITIALIZED) return
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
            track.setPlaybackHeadPosition(0)
            track.play()
        } catch (_: Exception) {}
    }

    private fun createStaticTrack(pcm: ShortArray): AudioTrack? {
        return try {
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
            track
        } catch (_: Exception) {
            null
        }
    }

    private fun generateKeyClickPcm(): ShortArray {
        val durationMs = 15
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 220.0)
            val sample = sin(2.0 * PI * 320.0 * t) * decay * 0.35
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateTileFlipPcm(baseFreq: Double): ShortArray {
        val durationMs = 110
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 30.0)
            val fundamental = sin(2.0 * PI * baseFreq * t)
            val overtone = sin(4.0 * PI * baseFreq * t) * 0.3
            val sample = (fundamental + overtone) * decay * 0.3
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateVictoryPcm(): ShortArray {
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
        return buffer
    }

    private fun generateErrorPcm(): ShortArray {
        val durationMs = 90
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 40.0)
            val sample = sin(2.0 * PI * 130.0 * t) * decay * 0.4
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    fun release() {
        listOfNotNull(keyClickTrack, errorTrack, victoryTrack, absentTrack, misplacedTrack)
            .plus(correctTracks.filterNotNull())
            .forEach {
                try {
                    it.stop()
                    it.release()
                } catch (_: Exception) {}
            }
    }
}
