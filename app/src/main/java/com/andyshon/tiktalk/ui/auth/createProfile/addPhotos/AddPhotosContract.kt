package com.andyshon.tiktalk.ui.auth.createProfile.addPhotos

import com.andyshon.tiktalk.ui.base.BaseContract

interface AddPhotosContract: BaseContract {

    interface View: BaseContract.View {

        fun onPhotosLoaded()
        fun onRegistered()
    }
}