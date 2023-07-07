package com.andyshon.tiktalk.ui.chatSingle

interface VoiceMessageListener {
    fun recorded(path: String)
    fun onEnded(pos: Int)
    fun newAudioSetted(pos: Int)
    fun paused(pos: Int)
    fun resumed(pos: Int)
    fun updateTime(time: Float)
    fun setDuration(pos: Int, seconds: Int)
    fun setProgress(pos: Int, progress: Float)
}