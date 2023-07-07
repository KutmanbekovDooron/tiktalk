package com.andyshon.tiktalk.ui

import com.andyshon.tiktalk.ui.base.BaseContract

interface MainContract: BaseContract {

    interface View: BaseContract.View {

        fun onBackPressed()
        fun updateNotificationBadge(count: Int)
    }
}