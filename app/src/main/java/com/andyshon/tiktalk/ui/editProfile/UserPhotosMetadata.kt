package com.andyshon.tiktalk.ui.editProfile

object UserPhotosMetadata {

    val photos: ArrayList<String> = arrayListOf("","","","","","")

    fun getIndexToInsert(): Int {
        var i = 0
        photos.forEach {
            if (it.isNotEmpty()) i++
        }
        return i
    }

}