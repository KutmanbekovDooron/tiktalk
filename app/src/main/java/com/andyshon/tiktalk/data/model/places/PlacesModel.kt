package com.andyshon.tiktalk.data.model.places

import com.andyshon.tiktalk.BuildConfig
import com.andyshon.tiktalk.data.model.BaseModel
import com.andyshon.tiktalk.data.network.response.PlacesListResponse
import com.andyshon.tiktalk.utils.extensions.applySchedulers
import io.reactivex.Single
import javax.inject.Inject

class PlacesModel @Inject constructor(private val service: PlacesService): BaseModel() {

    fun getPlaces(location: String, radius: Int, pageToken: String): Single<PlacesListResponse> {
        return service.getPlaces(location, radius, sensor = true, key = BuildConfig.PLACES_KEY, pageToken = pageToken)
            .checkApiErrorSingle()
            .applySchedulers()
    }
}