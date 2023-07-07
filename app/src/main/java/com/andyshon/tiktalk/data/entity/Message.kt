package com.andyshon.tiktalk.data.entity

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("_id") val id: String,
    @SerializedName("text") val text: String,
    @SerializedName("image") val image: String? = null,
    @SerializedName("authorId") val authorId: Int,
    @SerializedName("createdAt") val createdAt: String
)