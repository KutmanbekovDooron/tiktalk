package com.andyshon.tiktalk.ui.chatSingle

import com.andyshon.tiktalk.ui.base.BaseContract
import com.twilio.chat.Member
import com.twilio.chat.Message

interface ChatSingleContract: BaseContract {

    interface View: BaseContract.View {

        fun onMessagesLoaded()
        fun onTypingEnded(member: Member?)
        fun onTypingStarted(member: Member?)
        fun onMessageAdded(message: Message?)
        fun onMessageDeleted(pos: Int)
        fun onCopied()

        fun onEmptyMatchesLayout(empty: Int)

        fun notifyItem(pos:Int)
        fun notifyDataSetChanged()

        fun loadSearchResult()

        fun notifyItemDeleted(pos:Int)

        fun openPattern(viaVisibility: Boolean = false)
        fun openPIN(viaVisibility: Boolean = false)
        fun openFingerprint(viaVisibility: Boolean = false )
    }
}