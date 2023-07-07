package com.andyshon.tiktalk.data.network.response

import com.andyshon.tiktalk.data.entity.User
import com.google.gson.annotations.SerializedName

data class UserResponseCodeVerify(@SerializedName("user") val user: User)