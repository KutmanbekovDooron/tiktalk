package com.andyshon.tiktalk.ui.zoneSingle

import com.andyshon.tiktalk.ui.base.BaseContract

interface ZoneSingleContract: BaseContract {

    interface View: BaseContract.View {

        fun setMembersCount(members: Int)
        fun openUserProfile(name: String, photo: String, phone: String)
    }
}