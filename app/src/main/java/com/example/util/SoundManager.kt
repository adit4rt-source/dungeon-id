package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.example.R

enum class RetroSound(val resId: Int) {
    MENU_CLICK(R.raw.sfx_menu_click),
    TAB_SWITCH(R.raw.sfx_tab_switch),
    COIN(R.raw.sfx_coin),
    FISH_CAST(R.raw.sfx_fish_cast),
    FISH_BITE(R.raw.sfx_fish_bite),
    FISH_CATCH(R.raw.sfx_fish_catch),
    COMBAT_SLASH(R.raw.sfx_combat_slash),
    COMBAT_HIT(R.raw.sfx_combat_hit),
    VICTORY(R.raw.sfx_victory)
}

enum class RetroBgm(val resId: Int) {
    DESA(R.raw.bgm_desa),
    KEBUN(R.raw.bgm_kebun),
    MANCING(R.raw.bgm_mancing),
    BERBURU(R.raw.bgm_berburu)
}

/**
 * Simple retro 8-bit sound manager using Android MediaPlayer.
 * Plays chiptune sound effects and looping background music (BGM).
 */
class SoundManager private constructor(private val appContext: Context) {

    private var isMuted: Boolean = false
    private var bgmPlayer: MediaPlayer? = null
    private var currentBgm: RetroBgm? = null

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val bgmAudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (isMuted) {
            pauseBgm()
        } else {
            resumeBgm()
        }
    }

    fun isMuted(): Boolean = isMuted

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            pauseBgm()
        } else {
            resumeBgm()
        }
        return isMuted
    }

    // --- Background Music (BGM) ---
    fun playBgm(bgm: RetroBgm) {
        if (currentBgm == bgm && bgmPlayer?.isPlaying == true) {
            return
        }
        currentBgm = bgm
        if (isMuted) return

        try {
            stopBgm()
            val player = MediaPlayer.create(appContext, bgm.resId) ?: return
            player.setAudioAttributes(bgmAudioAttributes)
            player.isLooping = true
            player.setVolume(0.40f, 0.40f)
            player.setOnErrorListener { mp, _, _ ->
                try { mp.release() } catch (_: Exception) {}
                if (bgmPlayer == mp) bgmPlayer = null
                true
            }
            player.start()
            bgmPlayer = player
        } catch (e: Exception) {
            Log.w("SoundManager", "Failed to play BGM ${bgm.name}: ${e.message}")
        }
    }

    fun stopBgm() {
        try {
            bgmPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (e: Exception) {
            Log.w("SoundManager", "Error stopping BGM: ${e.message}")
        } finally {
            bgmPlayer = null
        }
    }

    fun pauseBgm() {
        try {
            if (bgmPlayer?.isPlaying == true) {
                bgmPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    fun resumeBgm() {
        if (isMuted) return
        try {
            if (bgmPlayer != null) {
                if (bgmPlayer?.isPlaying == false) {
                    bgmPlayer?.start()
                }
            } else if (currentBgm != null) {
                currentBgm?.let { playBgm(it) }
            }
        } catch (_: Exception) {}
    }

    fun getCurrentBgm(): RetroBgm? = currentBgm

    // Dedicated BGM controls for desa, kebun, mancing, berburu
    fun playDesaBgm() = playBgm(RetroBgm.DESA)
    fun playKebunBgm() = playBgm(RetroBgm.KEBUN)
    fun playMancingBgm() = playBgm(RetroBgm.MANCING)
    fun playBerburuBgm() = playBgm(RetroBgm.BERBURU)

    fun play(sound: RetroSound) {
        if (isMuted) return
        try {
            val mediaPlayer = MediaPlayer.create(appContext, sound.resId) ?: return
            mediaPlayer.setAudioAttributes(audioAttributes)
            mediaPlayer.setOnCompletionListener { mp ->
                try {
                    mp.stop()
                    mp.release()
                } catch (e: Exception) {
                    Log.w("SoundManager", "Error releasing player: ${e.message}")
                }
            }
            mediaPlayer.setOnErrorListener { mp, _, _ ->
                try {
                    mp.release()
                } catch (_: Exception) {}
                true
            }
            mediaPlayer.start()
        } catch (e: Exception) {
            Log.w("SoundManager", "Failed to play ${sound.name}: ${e.message}")
        }
    }

    // Menu Navigation
    fun playMenuClick() = play(RetroSound.MENU_CLICK)
    fun playTabSwitch() = play(RetroSound.TAB_SWITCH)
    fun playCoin() = play(RetroSound.COIN)

    // Fishing
    fun playFishCast() = play(RetroSound.FISH_CAST)
    fun playFishBite() = play(RetroSound.FISH_BITE)
    fun playFishCatch() = play(RetroSound.FISH_CATCH)

    // Combat
    fun playCombatSlash() = play(RetroSound.COMBAT_SLASH)
    fun playCombatHit() = play(RetroSound.COMBAT_HIT)
    fun playVictory() = play(RetroSound.VICTORY)

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

val LocalSoundManager = staticCompositionLocalOf<SoundManager> {
    error("LocalSoundManager not provided")
}

@Composable
fun ProvideSoundManager(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember(context) { SoundManager.getInstance(context) }
    CompositionLocalProvider(LocalSoundManager provides soundManager) {
        content()
    }
}
