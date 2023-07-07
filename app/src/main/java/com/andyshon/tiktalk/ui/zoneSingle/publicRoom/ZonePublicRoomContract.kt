package com.andyshon.tiktalk.ui.zoneSingle.publicRoom

import com.andyshon.tiktalk.ui.base.BaseContract
import com.twilio.chat.Member
import com.twilio.chat.Message

interface ZonePublicRoomContract: BaseContract {

    interface View: BaseContract.View {

        fun onMessagesLoaded()
        fun onTypingEnded(member: Member?)
        fun onTypingStarted(member: Member?)
        fun onMessageAdded(message: Message?)
        fun onMessageDeleted(pos: Int)
        fun onCopied()

        fun notifyItem(pos:Int)
        fun notifyDataSetChanged()

        fun notifyItemDeleted(pos:Int)

        fun hideRecycler(b:Boolean)
    }
}