package com.andyshon.tiktalk.data.model.match

import com.andyshon.tiktalk.data.network.request.MatchActionRequest
import com.andyshon.tiktalk.data.network.request.ReportRequest
import com.andyshon.tiktalk.data.network.response.MatchActionUserResponse
import com.andyshon.tiktalk.data.network.response.MatchUsersResponse
import com.andyshon.tiktalk.data.network.response.ReportResponse
import io.reactivex.Single
import retrofit2.http.*

interface MatchService {

    @GET("/v1/match_users")
    fun getMatchUsers(): Single<MatchUsersResponse>

    @POST("/v1/match_users/preference")
    fun matchAction(@Body matchActionRequest: MatchActionRequest): Single</*MatchActionUserResponse*/MatchActionUserResponse>

    @POST("/v1/reports")
    fun report(@Body reportRequest: ReportRequest): Single<ReportResponse>

}