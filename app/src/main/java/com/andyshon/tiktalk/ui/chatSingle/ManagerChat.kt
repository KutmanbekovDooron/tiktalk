package com.andyshon.tiktalk.ui.chatSingle

import com.twilio.chat.Attributes
import com.twilio.chat.CallbackListener
import com.twilio.chat.Channel
import com.twilio.chat.ErrorInfo
import com.twilio.chat.Message
import org.json.JSONObject
import timber.log.Timber

class ManagerChat(var mChannel: Channel?, val listener: ManagerChatListener) {

    fun sendMessage(text: String) {
//        if (channelJoined) {
        //etMessageField.setText("")

        //https://i.pinimg.com/originals/65/95/85/6595856323f822a5e9b6411c5d415b49.jpg
        val json = JSONObject()
//            json.put("mediaImage", "https://i.pinimg.com/originals/65/95/85/6595856323f822a5e9b6411c5d415b49.jpg")
        json.put("mediaImage", "https://i.kinja-img.com/gawker-media/image/upload/s--hfr5d2Ov--/c_scale,f_auto,fl_progressive,q_80,w_800/vqbtvwlnlatl0par1bvh.jpg")


        val options = Message.options()
        options.withBody(text)
//            options.withMediaFileName("https://i.pinimg.com/originals/65/95/85/6595856323f822a5e9b6411c5d415b49.jpg")
        if (text == "media") {
            options.withAttributes(Attributes(json))
        }


        mChannel?.messages?.sendMessage(options, object : CallbackListener<Message>() {
            override fun onSuccess(p0: Message?) {
                Timber.e("sendMessage, onSuccess: ${p0?.messageBody}, author = ${p0?.author}, ${p0?.channel?.friendlyName}, ${p0?.channelSid}, ${p0?.dateCreated}, ${p0?.dateCreatedAsDate}")

                listener.onMessageSent(p0)
            }

            override fun onError(errorInfo: ErrorInfo?) {
                Timber.e("sendMessage, onError: ${errorInfo?.status}, ${errorInfo?.message}, ${errorInfo?.code}")
            }
        })
//        }
//        else {
//            Timber.e("You must Join Channel!")
//        }
    }

}