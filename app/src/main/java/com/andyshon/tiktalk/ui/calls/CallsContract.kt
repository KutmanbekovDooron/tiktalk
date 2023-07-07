package com.andyshon.tiktalk.ui.calls

import com.andyshon.tiktalk.ui.base.BaseContract

interface CallsContract: BaseContract {

    interface View: BaseContract.View

    interface Presenter: BaseContract.Presenter<View> {
        fun sendStatusCallMessage(callStatus: CallStatus, base: Long)
    }
}