package com.andyshon.tiktalk.ui.auth.createProfile

import com.andyshon.tiktalk.ui.base.BaseContract

interface CreateProfileContract: BaseContract {

    interface View: BaseContract.View {

        fun onUserParamsVerified()
        fun emailTaken()
        fun emailFree()
    }
}