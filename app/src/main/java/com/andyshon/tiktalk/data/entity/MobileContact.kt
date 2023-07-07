package com.andyshon.tiktalk.data.entity

data class MobileContact(
    val name: String,
    val phoneNumber: String,
    var isChecked: Boolean = false
)