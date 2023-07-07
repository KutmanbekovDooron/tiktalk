package com.andyshon.tiktalk.data.model.calls

import com.andyshon.tiktalk.data.model.BaseModel
import com.andyshon.tiktalk.data.network.request.DeclineCallRequest
import com.andyshon.tiktalk.utils.extensions.applySchedulers
import io.reactivex.Completable
import javax.inject.Inject

class CallModel @Inject constructor(private val service: CallService): BaseModel() {

    fun declineCall(groupName: String, declineByEmail: String): Completable {
        return service.declineCall(DeclineCallRequest(groupName, declineByEmail))
            .checkApiErrorCompletable()
            .applySchedulers()
    }

}