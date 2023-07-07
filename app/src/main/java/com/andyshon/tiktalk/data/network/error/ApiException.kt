package com.andyshon.tiktalk.data.network.error

import com.google.gson.annotations.SerializedName

data class ApiException(@SerializedName("message") val mMessage: String) : Throwable()