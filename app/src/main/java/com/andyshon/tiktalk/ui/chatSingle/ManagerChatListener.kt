package com.andyshon.tiktalk.ui.chatSingle

import com.twilio.chat.Message

interface ManagerChatListener {

    fun onMessageSent(p0: Message?)

}