package com.andyshon.tiktalk.ui.zoneSingle

import com.andyshon.tiktalk.data.entity.UserPreview
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.events.PublicRoomOpenUserProfileEvent
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.andyshon.tiktalk.ui.chatSingle.entity.CommonMessageObject
import com.twilio.chat.CallbackListener
import com.twilio.chat.Channel
import com.twilio.chat.ErrorInfo
import io.reactivex.rxkotlin.addTo
import org.json.JSONArray
import timber.log.Timber
import javax.inject.Inject

class ZoneSinglePresenter @Inject constructor(private val rxEventBus: RxEventBus) : BasePresenter<ZoneSingleContract.View>() {

    var chats: ArrayList<CommonMessageObject> = arrayListOf()

    var channel: Channel? = null

    lateinit var usersIds: JSONArray
    lateinit var usersNames: JSONArray
    lateinit var usersEmails: JSONArray
    lateinit var usersPhotos: JSONArray
    lateinit var usersPhones: JSONArray

    val users = arrayListOf<UserPreview>()

    fun observe() {
        rxEventBus.filteredObservable(PublicRoomOpenUserProfileEvent::class.java)
            .subscribe({
                view?.openUserProfile(it.name, it.photo, it.phone)
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }

    fun getPlace(uniqueChannelName: String) {
        Timber.e("getPlace $uniqueChannelName")
        TwilioSingleton.instance.chatClient?.channels?.getChannel(uniqueChannelName, object: CallbackListener<Channel>() {
            override fun onSuccess(channel: Channel?) {
                this@ZoneSinglePresenter.channel = channel
                fillUsersData()
                view?.setMembersCount(channel?.members?.membersList?.size ?: 0)
                Timber.e("Get channel, sid = ${channel?.sid}, ${channel?.uniqueName}, ${channel?.members?.membersList?.size}, ${channel?.attributes}")
            }
            override fun onError(errorInfo: ErrorInfo?) {
                Timber.e("OnError = $errorInfo, ${errorInfo?.code}, ${errorInfo?.status}, ${errorInfo?.message}")
            }
        })
    }

    private fun fillUsersData() {
        channel?.let {
            Timber.e("atr = ${it.attributes}")
            usersIds = it.attributes.getJSONArray("usersIds")
            usersNames = it.attributes.getJSONArray("usersNames")
            usersEmails = it.attributes.getJSONArray("usersEmails")
            usersPhotos = it.attributes.getJSONArray("usersPhotos")
            usersPhones = it.attributes.getJSONArray("usersPhones")

            for(i in 0 until usersEmails.length()) {
                val id = usersIds.getString(i)
                val name = usersNames.getString(i)
                val email = usersEmails.getString(i)
                val photo = usersPhotos.getString(i)
                val phone = usersPhones.getString(i)

                val userPreview = UserPreview(id, name, email, photo, phone = phone)
                users.add(userPreview)
            }
        }
    }

}