package com.andyshon.tiktalk.ui.secretChats

import com.andyshon.tiktalk.ui.base.BaseContract

interface SecretChatsContract: BaseContract {

    interface View: BaseContract.View {

        fun onChatsLoaded()
        fun onChatDelete()
        fun updateAdapter()
        fun itemChanged(pos:Int)
        fun chatDeleted(pos: Int)
    }
}