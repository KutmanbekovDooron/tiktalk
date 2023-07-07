package com.andyshon.tiktalk.data.network.request

import com.google.gson.annotations.SerializedName

data class MatchActionRequest(
    @SerializedName("receiver_id") val userId: Int,
    @SerializedName("status") val status: String
)