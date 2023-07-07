package com.andyshon.tiktalk.ui.chatSingle.entity

import com.twilio.chat.Message

open class CommonMessageObject (
    val message: Message,
    var isChecked : Boolean = false,
    var select: Boolean = false
    /*, var hideProgress: Boolean = false*/
)