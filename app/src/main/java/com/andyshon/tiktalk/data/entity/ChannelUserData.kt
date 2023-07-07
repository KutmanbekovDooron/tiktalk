package com.andyshon.tiktalk.data.entity

data class ChannelUserData(
    val userId1: Int = 0,
    val userName1: String = "",
    val userEmail1: String = "",
    val userPhoto1: String = "",
    val userPhone1: String = "",
    val userId2: Int = 0,
    val userName2: String = "",
    val userEmail2: String = "",
    val userPhoto2: String = "",
    val userPhone2: String = "",
    var lastMessageAuthor: String = ""
)