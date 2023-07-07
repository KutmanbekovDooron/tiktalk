package com.andyshon.tiktalk.ui.settings

import com.andyshon.tiktalk.ui.base.BaseContract

interface SettingsContract: BaseContract {

    interface View: BaseContract.View {

        fun dislikesResetted()
        fun fillPrefs()
        fun updated()
        fun logout()
        fun deleteAccount()
    }
}