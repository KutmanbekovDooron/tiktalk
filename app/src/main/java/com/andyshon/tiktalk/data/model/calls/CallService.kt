package com.andyshon.tiktalk.data.model.calls

import com.andyshon.tiktalk.data.network.request.DeclineCallRequest
import io.reactivex.Completable
import retrofit2.http.*
import retrofit2.http.POST

interface CallService {

    @POST("/v1/video_calls/decline_call")
    fun declineCall(@Body declineCallRequest: DeclineCallRequest): Completable

}