package com.andyshon.tiktalk.data.network.request

import com.google.gson.annotations.SerializedName

data class FriendsRequest(
    @SerializedName("friends") val friends: ArrayList<Friend>
)

data class Friend(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("country_code") val countryCode: String,
    var name: String = ""
)