package com.andyshon.tiktalk.data.network.response

import com.google.gson.annotations.SerializedName

data class TwilioUserResponse(
    @SerializedName("sid") val sid: String,
    @SerializedName("identity") val identity: String,
    @SerializedName("is_online") val isOnline: Boolean? = false,
    @SerializedName("is_notifiable") val isNotifiable: Boolean? = true,
    @SerializedName("joined_channels_count") val joinedChannelsCount: Int,
    @SerializedName("date_created") val dateCreated: String,
    @SerializedName("date_updated") val dateUpdated: String
)