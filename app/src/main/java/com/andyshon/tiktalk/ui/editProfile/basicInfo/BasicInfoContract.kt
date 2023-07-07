package com.andyshon.tiktalk.ui.editProfile.basicInfo

import com.andyshon.tiktalk.ui.base.BaseContract

interface BasicInfoContract: BaseContract {

    interface View: BaseContract.View {

        fun updatedUser()
    }
}