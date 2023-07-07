package com.andyshon.tiktalk.ui.selectContact

import com.andyshon.tiktalk.ui.base.BaseContract

interface SelectContactContract: BaseContract {

    interface View: BaseContract.View {

        fun showFriends()
    }
}