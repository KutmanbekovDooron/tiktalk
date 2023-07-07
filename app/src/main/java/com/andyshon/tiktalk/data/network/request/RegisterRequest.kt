package com.andyshon.tiktalk.data.network.request

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("code_country") val codeCountry: String,
    @SerializedName("name") val name: String,
    @SerializedName("birth_date") val birthDate: String,
    @SerializedName("country") val country: String,
    @SerializedName("city") val city: String,
    @SerializedName("gender") val gender: String
)