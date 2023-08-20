package com.andyshon.tiktalk.ui.calls

import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.twilio.chat.Message
import org.json.JSONObject
import javax.inject.Inject
import ChatCallbackListener
import com.andyshon.tiktalk.data.entity.CallToUser
import com.andyshon.tiktalk.data.model.calls.CallModel
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.utils.extensions.getVoiceCallDurationInFormat
import com.twilio.chat.Attributes
import io.reactivex.rxkotlin.addTo
import org.jetbrains.anko.longToast

class CallsPresenter @Inject constructor(
    prefs: PreferenceManager,
    private val callModel: CallModel
) : BasePresenter<CallsContract.View>(), CallsContract.Presenter {

    lateinit var callToUser: CallToUser
    var roomName = ""
    var participantIdentity: String? = null
    var userEmail: String = prefs.getObject(Preference.KEY_USER_EMAIL, String::class.java) ?: ""
    var userPhoto = ""
    var channelSid = ""

    override fun sendStatusCallMessage(callStatus: CallStatus, base: Long) {
        val options = Message.options()
        val json = JSONObject()

        json.put("callStatus", callStatus.status) // Call missed or Call ended

        when (callStatus) {
            CallStatus.MISSED, CallStatus.CANCELED -> {
                json.put("callDuration", "") // empty or in format: Lasted n hour(status) n minute(status) n second(status)
            }
            CallStatus.ENDED -> {
                json.put("callDuration", getVoiceCallDurationInFormat(base))
            }
        }

        options.withAttributes(Attributes(json))
//        view?.getActivityContext()?.longToast("json = $json, Twilio.chatClient = ${TwilioSingleton.instance.chatClient}")

        TwilioSingleton.instance.chatClient?.channels?.getChannel(channelSid, ChatCallbackListener { channel->
            channel.messages?.sendMessage(options, ChatCallbackListener { message->
//                view?.getActivityContext()?.longToast("sendStatusCallMessage, onSuccess: author = ${message.author}, ${message.channelSid}")
            })
        })
    }

    fun declineCall() {
        callModel.declineCall(roomName, userEmail).subscribe({},{}).addTo(destroyDisposable)
    }
}