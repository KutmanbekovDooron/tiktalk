package com.andyshon.tiktalk.data.entity

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("code_country") val codeCountry: String,
    @SerializedName("birth_date") val birthDate: String,
    @SerializedName("country") val country: String,
    @SerializedName("city") val city: String,
    @SerializedName("images") val images: ArrayList<Image> = arrayListOf(),
    @SerializedName("twilio_user_id") val twilioUserId: String,
    @SerializedName("instagram_photos_url") val instagramPhotosUrl: ArrayList<String> = arrayListOf(),
    @SerializedName("is_account_block") val accountBlock: Boolean = false,
    @SerializedName("firebase_token") val firebaseToken: String?=null,

    @SerializedName("prefer_gender_male") val preferGenderMale: Boolean,
    @SerializedName("prefer_gender_female") val preferGenderFemale: Boolean,
    @SerializedName("prefer_min_age") val preferMinAge: Int,
    @SerializedName("prefer_max_age") val preferMaxAge: Int,
    @SerializedName("prefer_location_distance") val preferLocationDistance: Int,
    @SerializedName("is_show_in_app") val isShowInApp: Boolean = true,
    @SerializedName("is_show_in_places") val isShowInPlaces: Boolean = true,

    @SerializedName("work") val work: String?=null,
    @SerializedName("education") val education: String?=null,
    @SerializedName("about_you") val aboutYou: String?=null,
    @SerializedName("relationship") val relationship: String,
    @SerializedName("sexuality") val sexuality: String,
    @SerializedName("height") val height: String?=null,
    @SerializedName("living") val living: String,
    @SerializedName("children") val children: String,
    @SerializedName("smoking") val smoking: String,
    @SerializedName("drinking") val drinking: String,
    @SerializedName("languages") val languages: ArrayList<String>,

    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,


    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,

    @SerializedName("locker_type") val lockerType: String?=null,
    @SerializedName("locker_value") val lockerValue: String?=null
)