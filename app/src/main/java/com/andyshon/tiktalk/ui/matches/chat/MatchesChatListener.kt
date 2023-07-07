package com.andyshon.tiktalk.ui.matches.chat

import com.andyshon.tiktalk.data.entity.ChannelModel

interface MatchesChatListener {
    fun closeMatchesChat()
    fun openMatchChat(chatUser: ChannelModel)
}