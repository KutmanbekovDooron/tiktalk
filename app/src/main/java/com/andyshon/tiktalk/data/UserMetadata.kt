package com.andyshon.tiktalk.data

import com.andyshon.tiktalk.data.entity.Image
import timber.log.Timber

object UserMetadata {
    var twilioUserId = ""
    var firebaseToken = ""
    var userId = -1
    var userName = ""
    var userGender = "male"
    var userEmail = ""
    var userPhone = ""
    var lastCMI = -1L
    var birthday = ""
    var lockerType = ""
    var lockerValue = ""

    var languages = arrayListOf<String>()
    var photos: ArrayList<Image> = arrayListOf(Image(0,""),Image(0,""),Image(0,""),Image(0,""),Image(0,""),Image(0,""),Image(0,""),Image(0,""),Image(0,""))

    fun getIndexToInsert(): Int {
        var i = 0
        photos.forEach {
            if (it.url.isNotEmpty()) i++
        }
        return i
    }

    fun hasAtLeastOnePhoto(): Boolean {
        photos.forEach {
            Timber.e("url = ${it.url}, ${it.url.isNotEmpty()}")
            if (it.url.isNotEmpty())
                return true
        }
        return false
    }
}