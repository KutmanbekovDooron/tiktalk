package com.andyshon.tiktalk.data.network.request

import com.google.gson.annotations.SerializedName

data class ReportRequest(
    @SerializedName("receiver_id") val userId: Int,
    @SerializedName("report_type") val reportType: String
)