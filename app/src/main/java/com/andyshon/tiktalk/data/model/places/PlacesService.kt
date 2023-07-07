package com.andyshon.tiktalk.data.model.places

import com.andyshon.tiktalk.data.network.response.PlacesListResponse
import io.reactivex.Single
import retrofit2.http.*

interface PlacesService {

    @GET("json")
    fun getPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("sensor") sensor: Boolean = true,
        @Query("key") key: String,
        @Query("pagetoken") pageToken: String = ""
    ): Single<PlacesListResponse>

}