package com.andyshon.tiktalk.data.network.request

import com.google.gson.annotations.SerializedName

data class UserNotificationsRequest(
    @SerializedName("pause_all") val pauseAll: Boolean = false,
    @SerializedName("messages") val messages: Boolean = true,
    @SerializedName("new_matches") val newMatches: Boolean = true,
    @SerializedName("like_you") val likeYou: Boolean = true,
    @SerializedName("message_in_private_room") val messageInPrivateRoom: Boolean = true,
    @SerializedName("super_like") val superLike: Boolean = true
)