package com.andyshon.tiktalk.data.network.request

import com.google.gson.annotations.SerializedName

data class DeclineCallRequest(
    @SerializedName("group_name") val groupName: String,
    @SerializedName("declined_by_email") val declinedByEmail: String
)