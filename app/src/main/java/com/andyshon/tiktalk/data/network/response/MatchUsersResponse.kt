package com.andyshon.tiktalk.data.network.response

import com.andyshon.tiktalk.data.entity.ChatUser

data class MatchUsersResponse(val message: String? = null, val users: List<ChatUser>)