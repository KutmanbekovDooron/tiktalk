package com.andyshon.tiktalk.data.model.match

import com.andyshon.tiktalk.data.model.BaseModel
import com.andyshon.tiktalk.data.network.request.MatchActionRequest
import com.andyshon.tiktalk.data.network.request.ReportRequest
import com.andyshon.tiktalk.data.network.response.MatchActionUserResponse
import com.andyshon.tiktalk.data.network.response.MatchUsersResponse
import com.andyshon.tiktalk.data.network.response.ReportResponse
import com.andyshon.tiktalk.data.network.response.UserResponse
import com.andyshon.tiktalk.utils.extensions.applySchedulers
import io.reactivex.Single
import javax.inject.Inject

class MatchModel @Inject constructor(private val service: MatchService): BaseModel() {

    fun getMatchUsers(): Single<MatchUsersResponse/*List<UserResponse>*/> {
        return service.getMatchUsers()
//            .map { it.users }
            .checkApiErrorSingle()
            .applySchedulers()
    }

    fun matchAction(userId: Int, action: String): Single</*MatchActionUserResponse*/MatchActionUserResponse> {
        return service.matchAction(MatchActionRequest(userId, action))
            .checkApiErrorSingle()
            .applySchedulers()
    }

    fun report(userId: Int, action: String): Single<ReportResponse> {
        return service.report(ReportRequest(userId, action))
            .checkApiErrorSingle()
            .applySchedulers()
    }
}