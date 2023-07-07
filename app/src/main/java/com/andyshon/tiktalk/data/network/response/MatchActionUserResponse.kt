package com.andyshon.tiktalk.data.network.response

import com.andyshon.tiktalk.data.entity.User

data class MatchActionUserResponse(
    val success: Boolean?=null,
    val user: User?=null
)