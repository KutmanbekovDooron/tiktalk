package com.andyshon.tiktalk.data.model.messages

import com.andyshon.tiktalk.data.model.BaseModel
import com.andyshon.tiktalk.utils.extensions.applySchedulers
import io.reactivex.Single
import javax.inject.Inject

class MessagesModel @Inject constructor(private val service: MessagesService): BaseModel() {

    /*fun getChats(): Single<List<ChannelModel>> {
        return service.getChats()
            .checkApiErrorSingle()
            .applySchedulers()
    }*/
}