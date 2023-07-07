package com.andyshon.tiktalk.data.network.response

import com.andyshon.tiktalk.data.entity.PlacesResult
import com.google.gson.annotations.SerializedName

data class PlacesListResponse(
    @SerializedName("html_attributions") val htmlAttributions: ArrayList<String> = arrayListOf(),
    @SerializedName("next_page_token") val nextPageToken: String,
    @SerializedName("results") val places: List<PlacesResult>
)