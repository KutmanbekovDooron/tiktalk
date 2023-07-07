package com.andyshon.tiktalk.data.network.request

import com.google.gson.annotations.SerializedName

data class PhoneVerifyRequest(
    @SerializedName("phone_number") val phone: String
)