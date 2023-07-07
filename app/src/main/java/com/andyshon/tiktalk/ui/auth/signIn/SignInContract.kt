package com.andyshon.tiktalk.ui.auth.signIn

import com.andyshon.tiktalk.ui.base.BaseContract

interface SignInContract: BaseContract {

    interface View: BaseContract.View {

        fun onSmsSent()
        fun onError()
    }
}