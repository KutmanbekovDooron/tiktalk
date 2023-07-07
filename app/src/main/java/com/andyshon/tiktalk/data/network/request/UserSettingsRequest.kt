package com.andyshon.tiktalk.data.network.request

import com.google.gson.annotations.SerializedName

data class UserSettingsRequest(
    @SerializedName("prefer_gender_male") val preferGenderMale: Boolean = true,
    @SerializedName("prefer_gender_female") val preferGenderFemale: Boolean = true,
    @SerializedName("prefer_min_age") val preferMinAge: Int = 18,
    @SerializedName("prefer_max_age") val preferMaxAge: Int = 25,
    @SerializedName("prefer_location_distance") val preferLocationDistance: Int = 10,
    @SerializedName("is_show_in_app") val isShowInApp: Boolean = true,
    @SerializedName("is_show_in_places") val isShowInPlaces: Boolean = true,

    @SerializedName("notifications") val notifications: UserNotificationsRequest?=null
)