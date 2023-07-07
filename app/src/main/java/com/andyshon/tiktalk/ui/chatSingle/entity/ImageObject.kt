package com.andyshon.tiktalk.ui.chatSingle.entity

import com.twilio.chat.Message

class ImageObject(
    var hideProgress: Boolean = false,
    msg: Message
): CommonMessageObject(msg)