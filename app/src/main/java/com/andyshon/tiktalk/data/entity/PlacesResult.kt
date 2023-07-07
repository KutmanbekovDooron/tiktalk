package com.andyshon.tiktalk.data.entity

import com.google.gson.annotations.SerializedName

data class PlacesResult(
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("icon") val icon: String,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("photos") val photos: List<Photos>,
    @SerializedName("place_id") val placeId: String,
    @SerializedName("reference") val reference: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("types") val types: List<String>,
    @SerializedName("vicinity") val vicinity: String,

    var usersCount: Int = 0
)

data class Geometry(
    @SerializedName("location") val location: GeometryLocation?
)

data class GeometryLocation(@SerializedName("lat") val lat: String, @SerializedName("lng") val lng: String)

data class Photos(
    @SerializedName("height") val height: Int,
    @SerializedName("html_attributions") val htmlAttributions: List<String>,
    @SerializedName("photo_reference") val photoReference: String,
    @SerializedName("width") val width: Int
)