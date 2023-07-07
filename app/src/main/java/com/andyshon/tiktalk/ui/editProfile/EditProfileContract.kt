package com.andyshon.tiktalk.ui.editProfile

import com.andyshon.tiktalk.ui.base.BaseContract

interface EditProfileContract: BaseContract {

    interface View: BaseContract.View {

        fun updated()

        fun setEducation(status: String)
        fun setWork(status: String)
        fun setAbout(status: String)
        fun setRelationship(status: String)
        fun setSexuality(status: String)
        fun setHeight(height: Int)
        fun setLiving(status: String)
        fun setChildren(status: String)
        fun setSmoking(status: String)
        fun setDrinking(status: String)
        fun setZodiac(status: String)
    }
}