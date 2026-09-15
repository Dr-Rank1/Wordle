package com.lexiguess.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.playerDataStore: DataStore<Preferences> by preferencesDataStore(name = "player_prefs")

@Singleton
class PlayerPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.playerDataStore

    val xpFlow: Flow<Int> = dataStore.data.map { it[KEY_XP] ?: 0 }
    val soundEnabledFlow: Flow<Boolean> = dataStore.data.map { it[KEY_SOUND] ?: true }
    val hapticsEnabledFlow: Flow<Boolean> = dataStore.data.map { it[KEY_HAPTICS] ?: true }
    val hardModeFlow: Flow<Boolean> = dataStore.data.map { it[KEY_HARD_MODE] ?: false }
    val themeFlow: Flow<String> = dataStore.data.map { it[KEY_THEME] ?: "DARK" }
    val boardThemeFlow: Flow<String> = dataStore.data.map { it[KEY_BOARD_THEME] ?: "EMERALD" }
    val rushHighScoreFlow: Flow<Int> = dataStore.data.map { it[KEY_RUSH_HIGH_SCORE] ?: 0 }
    val streakFreezesFlow: Flow<Int> = dataStore.data.map { it[KEY_STREAK_FREEZES] ?: 1 }
    val lastPlayedDateFlow: Flow<String?> = dataStore.data.map { it[KEY_LAST_PLAYED_DATE] }
    val tileMaterialFlow: Flow<String> = dataStore.data.map { it[KEY_TILE_MATERIAL] ?: "CLASSIC" }
    val particleEffectFlow: Flow<String> = dataStore.data.map { it[KEY_PARTICLE_EFFECT] ?: "CONFETTI" }
    val tiltParallaxFlow: Flow<Boolean> = dataStore.data.map { it[KEY_TILT_PARALLAX] ?: true }

    suspend fun addXp(amount: Int) {
        dataStore.edit {
            val current = it[KEY_XP] ?: 0
            it[KEY_XP] = current + amount
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_SOUND] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_HAPTICS] = enabled }
    }

    suspend fun setHardMode(enabled: Boolean) {
        dataStore.edit { it[KEY_HARD_MODE] = enabled }
    }

    suspend fun setTheme(themeName: String) {
        dataStore.edit { it[KEY_THEME] = themeName }
    }

    suspend fun setBoardTheme(themeId: String) {
        dataStore.edit { it[KEY_BOARD_THEME] = themeId }
    }

    suspend fun setTileMaterial(material: String) {
        dataStore.edit { it[KEY_TILE_MATERIAL] = material }
    }

    suspend fun setParticleEffect(effect: String) {
        dataStore.edit { it[KEY_PARTICLE_EFFECT] = effect }
    }

    suspend fun setTiltParallaxEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_TILT_PARALLAX] = enabled }
    }

    suspend fun addStreakFreeze(count: Int = 1) {
        dataStore.edit {
            val current = it[KEY_STREAK_FREEZES] ?: 1
            it[KEY_STREAK_FREEZES] = (current + count).coerceAtMost(2)
        }
    }

    suspend fun consumeStreakFreeze(): Boolean {
        var used = false
        dataStore.edit {
            val current = it[KEY_STREAK_FREEZES] ?: 1
            if (current > 0) {
                it[KEY_STREAK_FREEZES] = current - 1
                used = true
            }
        }
        return used
    }

    suspend fun updateLastPlayedDate(dateKey: String) {
        dataStore.edit { it[KEY_LAST_PLAYED_DATE] = dateKey }
    }

    suspend fun updateRushHighScore(score: Int) {
        dataStore.edit {
            val current = it[KEY_RUSH_HIGH_SCORE] ?: 0
            if (score > current) it[KEY_RUSH_HIGH_SCORE] = score
        }
    }

    companion object {
        private val KEY_XP = intPreferencesKey("player_xp")
        private val KEY_SOUND = booleanPreferencesKey("pref_sound")
        private val KEY_HAPTICS = booleanPreferencesKey("pref_haptics")
        private val KEY_HARD_MODE = booleanPreferencesKey("pref_hard_mode")
        private val KEY_THEME = stringPreferencesKey("pref_theme")
        private val KEY_BOARD_THEME = stringPreferencesKey("pref_board_theme")
        private val KEY_RUSH_HIGH_SCORE = intPreferencesKey("rush_high_score")
        private val KEY_STREAK_FREEZES = intPreferencesKey("pref_streak_freezes")
        private val KEY_LAST_PLAYED_DATE = stringPreferencesKey("pref_last_played_date")
        private val KEY_TILE_MATERIAL = stringPreferencesKey("pref_tile_material")
        private val KEY_PARTICLE_EFFECT = stringPreferencesKey("pref_particle_effect")
        private val KEY_TILT_PARALLAX = booleanPreferencesKey("pref_tilt_parallax")

        fun calculateLevel(xp: Int): Int = (xp / 400) + 1
        fun calculateProgressInLevel(xp: Int): Float = (xp % 400).toFloat() / 400f
        fun rankForLevel(level: Int): String = when {
            level < 4 -> "Novice"
            level < 8 -> "Wordsmith"
            level < 15 -> "Vocab Virtuoso"
            level < 25 -> "Lexicon Master"
            else -> "Grandmaster"
        }
    }
}
