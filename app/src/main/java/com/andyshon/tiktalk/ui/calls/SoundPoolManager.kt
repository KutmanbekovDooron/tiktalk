package com.andyshon.tiktalk.ui.calls

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import com.andyshon.tiktalk.R

class SoundPoolManager {

    companion object {
        private var instance: SoundPoolManager? = null
        var mContext: Context? = null

        fun getInstance(context: Context): SoundPoolManager {
            if (instance == null) {
                mContext = context
                instance =
                    SoundPoolManager()
            }
            return instance as SoundPoolManager
        }
    }

    private var playing = false
    private var loaded = false
    private var playingCalled = false
    private var actualVolume: Float = 0.0f
    private var maxVolume: Float = 0.0f
    private var volume: Float = 0.0f
    private var audioManager: AudioManager? = null
    private var soundPool: SoundPool? = null
    private var ringingSoundId: Int = 0
    private var ringingStreamId: Int = 0
    private var disconnectSoundId: Int = 0

    init {
        // AudioManager audio settings for adjusting the volume
        audioManager = mContext?.getSystemService(AUDIO_SERVICE) as AudioManager
        actualVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f
        maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f
        volume = actualVolume / maxVolume

        // Load the sounds
        val maxStreams = 1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = SoundPool.Builder()
                .setMaxStreams(maxStreams)
                .build()
        } else {
            soundPool = SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0)
        }

        soundPool?.setOnLoadCompleteListener(SoundPool.OnLoadCompleteListener { soundPool, sampleId, status ->
            loaded = true
            if (playingCalled) {
                playRinging()
                playingCalled = false
            }
        })
        ringingSoundId = soundPool?.load(mContext, R.raw.incoming, 1) ?: 0
        disconnectSoundId = soundPool?.load(mContext, R.raw.disconnect, 1) ?: 0
    }

    fun playRinging() {
        if (loaded && !playing) {
            ringingStreamId = soundPool?.play(ringingSoundId, volume, volume, 1, -1, 1f) ?: 0
            playing = true
        } else {
            playingCalled = true
        }
    }

    fun stopRinging() {
        if (playing) {
            soundPool?.stop(ringingStreamId)
            playing = false
        }
    }

    fun playDisconnect() {
        if (loaded && !playing) {
            soundPool?.play(disconnectSoundId, volume, volume, 1, 0, 1f)
            playing = false
        }
    }

    fun release() {
        soundPool?.let {
            it.unload(ringingSoundId)
            it.unload(disconnectSoundId)
            it.release()
            soundPool = null
        }
        instance = null
    }
}