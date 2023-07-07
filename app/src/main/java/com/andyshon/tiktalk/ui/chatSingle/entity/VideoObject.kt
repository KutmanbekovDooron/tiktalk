package com.andyshon.tiktalk.ui.chatSingle.entity

import com.twilio.chat.Message

class VideoObject(
    val path: String,
    val name: String,
    val title: String,
    var duration: String,
    var musicSizeInBytes: Long = 0,
    var playing: Boolean = false,
    msg: Message
): CommonMessageObject(msg)