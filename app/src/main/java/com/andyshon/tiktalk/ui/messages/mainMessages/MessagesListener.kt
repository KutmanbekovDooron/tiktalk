package com.andyshon.tiktalk.ui.messages.mainMessages

import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.ui.base.BaseFragmentListener
import com.twilio.chat.Channel

interface MessagesListener: BaseFragmentListener {
    fun openSelectContact()
    fun openSingleChat(channel: Channel, item: ChannelModel, isFromMatches: Boolean = false)
    fun openSecretChats()
    fun openPattern()
    fun openPIN()
    fun openFingerprint()
    fun openSettings()
}