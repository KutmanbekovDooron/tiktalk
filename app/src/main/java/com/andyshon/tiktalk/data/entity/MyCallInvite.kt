package com.andyshon.tiktalk.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MyCallInvite(
    val callerId: Int = 0,
    val callerName: String = "",
    val callerEmail: String = "",
    val callerPhone: String = "",
    val callerAvatar: String = "",
    val channelSid: String = ""
): Parcelable