package com.andyshon.tiktalk.ui.matches.chat

import com.andyshon.tiktalk.data.entity.ChannelModel
import com.twilio.chat.Channel

interface MatchesChatListener {
    fun closeMatchesChat()
    fun openMatchChat(channel: Channel, chatUser: ChannelModel)
}