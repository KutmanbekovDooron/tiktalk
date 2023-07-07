package com.andyshon.tiktalk.ui.matches.chat

import com.andyshon.tiktalk.ui.base.BaseContract

interface MatchesChatContract: BaseContract {

    interface View: BaseContract.View {

        fun onChatsLoaded()
        fun onChatDelete()
        fun updateAdapter()
        fun itemChanged(pos:Int)
    }
}