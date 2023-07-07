package com.andyshon.tiktalk.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MyCancelledCallInvite(
    val declinedByEmail: String,
    val callerId: Int,
    val channelSid: String
): Parcelable