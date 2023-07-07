package com.andyshon.tiktalk.ui.viewContact

import com.andyshon.tiktalk.ui.base.BaseContract

interface ViewContactContract: BaseContract {

    interface View: BaseContract.View {

        fun sharedMediaLoaded()
        fun blocked()
        fun edited()
        fun deleted()
    }
}