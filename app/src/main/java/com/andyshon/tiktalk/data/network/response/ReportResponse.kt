package com.andyshon.tiktalk.data.network.response

import com.google.gson.annotations.SerializedName

data class ReportResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("report_type") val reportType: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("receiver_id") val receiverId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)