package com.andyshon.tiktalk.ui.chatSingle.entity

import com.twilio.chat.Message

class VoiceObject(
    val path: String,
    var playing: Boolean = false,
    var bytesArray: ByteArray,
    var duration: Int,
    var progress: Float,
    msg: Message
): CommonMessageObject(msg)