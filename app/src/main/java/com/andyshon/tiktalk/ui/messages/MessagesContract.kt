package com.andyshon.tiktalk.ui.messages

import com.andyshon.tiktalk.ui.base.BaseContract

interface MessagesContract: BaseContract {

    interface View: BaseContract.View {

        fun onChatsLoaded()
        fun onChatDelete()
        fun updateAdapter()
        fun itemChanged(pos:Int)
        fun chatDeleted(pos: Int)

        fun setEmptyChats(empty: Boolean)
    }
}