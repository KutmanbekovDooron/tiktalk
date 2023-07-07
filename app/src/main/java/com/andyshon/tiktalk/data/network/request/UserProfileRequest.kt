package com.andyshon.tiktalk.data.network.request

import com.google.gson.annotations.SerializedName

data class UserProfileRequest(
    @SerializedName("name") val name: String?=null,
    @SerializedName("gender") val gender: String?=null,
    @SerializedName("birth_date") val birthDate: String?=null,

    @SerializedName("work") val work: String?=null,
    @SerializedName("education") val education: String?=null,
    @SerializedName("about_you") val aboutYou: String?=null,
    @SerializedName("relationship") val relationship: String?=null,
    @SerializedName("sexuality") val sexuality: String?=null,
    @SerializedName("height") val height: String?=null,
    @SerializedName("living") val living: String?=null,
    @SerializedName("children") val children: String?=null,
    @SerializedName("smoking") val smoking: String?=null,
    @SerializedName("drinking") val drinking: String?=null,
    @SerializedName("zodiac") val zodiac: String?=null,

    @SerializedName("languages") val languages: List<String>? = null,

    @SerializedName("add_images") val addImages: List<String>? = null,
    @SerializedName("delete_images") val deleteImages: List<Int>? = null,

    @SerializedName("locker_type") val lockerType: String? = null,
    @SerializedName("locker_value") val lockerValue: String? = null
)