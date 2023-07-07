package com.andyshon.tiktalk.ui.editProfile.moreLanguages

import com.andyshon.tiktalk.ui.base.BaseContract

interface MoreLanguagesContract: BaseContract {

    interface View: BaseContract.View {

        fun updatedUser()
    }
}