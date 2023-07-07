package com.andyshon.tiktalk.data.entity

import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("id") val id: Int,
    @SerializedName("url") val url: String
)