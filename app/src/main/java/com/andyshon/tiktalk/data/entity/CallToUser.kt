package com.andyshon.tiktalk.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CallToUser(
    val email: String = "",
    val name: String = "",
    val photo: String = "",
    val channelSid: String = ""
): Parcelable