package com.andyshon.tiktalk.ui.chatSingle.entity

import com.twilio.chat.Message

class FileObject(
    var hideProgress: Boolean = false,
    msg: Message
): CommonMessageObject(msg)