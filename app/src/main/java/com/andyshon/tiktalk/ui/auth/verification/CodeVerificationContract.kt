package com.andyshon.tiktalk.ui.auth.verification

import com.andyshon.tiktalk.ui.base.BaseContract

interface CodeVerificationContract: BaseContract {

    interface View: BaseContract.View {

        fun onPhoneVerified()
        fun onUserReturn()
        fun onIncorrectCode()
        fun onBlockedAccount()
    }
}