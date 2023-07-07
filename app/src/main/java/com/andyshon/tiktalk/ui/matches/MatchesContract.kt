package com.andyshon.tiktalk.ui.matches

import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.ui.base.BaseContract

interface MatchesContract: BaseContract {

    interface View: BaseContract.View {

        fun onMatchUsersLoaded()
        fun noOneNewAroundYou()
        fun onReported()
        fun onLiked()
        fun onDisliked()
        fun onSuperliked()
        fun onMatched(user: User)
    }
}