package com.andyshon.tiktalk.ui.zones

import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.PlacesResult
import com.andyshon.tiktalk.data.model.places.PlacesModel
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.andyshon.tiktalk.utils.extensions.generateUserColor
import com.twilio.chat.ErrorInfo
import com.twilio.chat.StatusListener
import io.reactivex.rxkotlin.addTo
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import ChatCallbackListener
import com.andyshon.tiktalk.ui.zoneSingle.publicRoom.PublicRoomMetadata
import com.twilio.chat.Attributes
import com.twilio.chat.Channel

class ZoneListPresenter @Inject constructor(private val model: PlacesModel) : BasePresenter<ZoneListContract.View>() {

    var places: ArrayList<PlacesResult> = arrayListOf()

    fun getPlaces(location: String, radius: Int) {
        model.getPlaces(location, radius, pageToken = "")
            .compose(applyProgressSingle())
            .subscribe({
                places.clear()
                Timber.e("Get places size = ${it.places.size}")
                if (it.places.isNotEmpty()) {
                    for(place in it.places) {
                        places.add(place)
                    }
                    view?.showPlaces()
                    scanPlaces()
                }
            }, {
                defaultErrorConsumer
            })
            .addTo(destroyDisposable)
    }

    private fun scanPlaces() {
        //create channel if it not exists yet
        for (place in places) {
            TwilioSingleton.instance.createPlaceChannel(place.placeId, place.name) { channel ->
                Timber.e("Create new or get existing channel ${channel?.sid}, ${channel?.uniqueName}, attributes = ${channel?.attributes}")

                view?.updatePlaceUsersCount(places.indexOf(place), channel?.members?.membersList?.size ?: 0)
            }
        }
    }


    fun openPlace(placesResult: PlacesResult) {
        view?.showProgress()
        TwilioSingleton.instance.chatClient?.channels?.getChannel(placesResult.name.plus("_").plus(placesResult.placeId), ChatCallbackListener { channel ->

            val attrs = channel?.attributes
            Timber.e("attrs object 1 = $attrs")
            if (attrs != null && attrs.jsonObject?.has("usersIds") == true) {
                val usersEmails = attrs.jsonObject?.getJSONArray("usersEmails")
                val usersColors = attrs.jsonObject?.getJSONArray("usersColors")

                PublicRoomMetadata.userColors = usersColors
                PublicRoomMetadata.usersEmails = usersEmails
            }

            channel?.join(object: StatusListener() {
                override fun onSuccess() {  // user join place first time
                    var atr = JSONObject()

                    if (channel.attributes != null && channel.attributes.jsonObject?.has("usersIds") == true) { // there is at least 1 user in channel
                        atr = channel.attributes.jsonObject!!
                        val usersIds = atr.getJSONArray("usersIds")
                        val usersEmails = atr.getJSONArray("usersEmails")
                        val usersNames = atr.getJSONArray("usersNames")
                        val usersPhotos = atr.getJSONArray("usersPhotos")
                        val usersPhones = atr.getJSONArray("usersPhones")
                        val usersGenders = atr.getJSONArray("usersGenders")
                        val usersLastViewingTime = atr.getJSONArray("usersLastViewingTime")
                        val usersColors = atr.getJSONArray("usersColors")

                        usersIds.put(UserMetadata.userId)
                        usersEmails.put(UserMetadata.userEmail)
                        usersNames.put(UserMetadata.userName)
                        usersPhotos.put(UserMetadata.photos.first().url)
                        usersPhones.put(UserMetadata.userPhone)
                        usersGenders.put(UserMetadata.userGender)
                        usersLastViewingTime.put(Calendar.getInstance().time)
                        usersColors.put(generateUserColor(atr))

                        atr.put("usersIds", usersIds)
                        atr.put("usersNames", usersNames)
                        atr.put("usersPhotos", usersPhotos)
                        atr.put("usersPhones", usersPhones)
                        atr.put("usersGenders", usersGenders)
                        atr.put("usersEmails", usersEmails)
                        atr.put("usersLastViewingTime", usersLastViewingTime)
                        atr.put("usersColors", usersColors)
                    }
                    else {  // there are no users in channel yet
                        atr = fillAttributes(atr)
                    }

                    setAttributesToChannel(channel, atr, placesResult)
                }

                override fun onError(errorInfo: ErrorInfo?) {
                    errorInfo?.let {
                        if (it.code == 50404) { //Member already exists

                            if (channel.attributes != null && channel.attributes.jsonObject?.has("usersIds") == true) {
                                Timber.e("Member already exists, error code ${it.code}")
                                val atr = updateUserAttributes(channel.attributes.jsonObject!!)
                                setAttributesToChannel(channel, atr, placesResult)
                            }
                            else {
                                // never triggers here
                            }
                        }
                        else {
                            view?.hideProgress()
                        }
                    }
                }
            })

            /*channel?.leave(object: StatusListener() {
                override fun onSuccess() {
                    Timber.e("leaved chat success")
                }
                override fun onError(errorInfo: ErrorInfo?) {
                    Timber.e("leaved chat error ${errorInfo?.code}, ${errorInfo?.status}, ${errorInfo?.message}")
                }
            })*/
            /*channel?.destroy(object: StatusListener() {
                override fun onSuccess() {
                    Timber.e("deleted chat success")
                    view?.hideProgress()
                }
                override fun onError(errorInfo: ErrorInfo?) {
                    Timber.e("deleted chat error ${errorInfo?.code}, ${errorInfo?.status}, ${errorInfo?.message}")
                    view?.hideProgress()
                }
            })*/
        })
    }

    private fun setAttributesToChannel(channel: Channel, atr: JSONObject, placesResult: PlacesResult) {
        channel.setAttributes(Attributes(atr) , object: StatusListener() {
            override fun onSuccess() {
                view?.hideProgress()
                Timber.e("onSuccess, setAttributes for channel ${channel.uniqueName}")
                placesResult.usersCount++
                view?.openSingleZone(placesResult)
            }
        })
    }

    private fun updateUserAttributes(atr: JSONObject): JSONObject {
        val usersIds = atr.getJSONArray("usersIds")
        val usersEmails = atr.getJSONArray("usersEmails")
        val usersNames = atr.getJSONArray("usersNames")
        val usersPhotos = atr.getJSONArray("usersPhotos")
        val usersPhones = atr.getJSONArray("usersPhones")
        val usersGenders = atr.getJSONArray("usersGenders")
        val usersLastViewingTime = atr.getJSONArray("usersLastViewingTime")
        val usersColors = atr.getJSONArray("usersColors")

        for(i in 0 until usersEmails.length()) {
            val email = usersEmails.getString(i)
            if (email == UserMetadata.userEmail) {

                //if the current user is in the channel -> update its attributes

                usersIds.put(i, UserMetadata.userId)
                usersEmails.put(i, UserMetadata.userEmail)
                usersNames.put(i, UserMetadata.userName)
                usersPhotos.put(i, UserMetadata.photos.first().url)
                usersGenders.put(i, UserMetadata.userGender)
                usersLastViewingTime.put(i, Calendar.getInstance().time)

                usersPhones.put(i, UserMetadata.userPhone)
                usersColors.put(i, usersColors[i])

                atr.put("usersIds", usersIds)
                atr.put("usersNames", usersNames)
                atr.put("usersPhotos", usersPhotos)
                atr.put("usersPhones", usersPhones)
                atr.put("usersGenders", usersGenders)
                atr.put("usersEmails", usersEmails)
                atr.put("usersLastViewingTime", usersLastViewingTime)
                atr.put("usersColors", usersColors)

                break
            }
        }
        Timber.e("attrs object = $atr")
        return atr
    }

    private fun fillAttributes(atr: JSONObject): JSONObject {
        val usersIds = JSONArray()
        usersIds.put(UserMetadata.userId)

        val usersNames = JSONArray()
        usersNames.put(UserMetadata.userName)

        val usersPhotos = JSONArray()
        usersPhotos.put(UserMetadata.photos.first().url)

        val usersPhones = JSONArray()
        usersPhones.put(UserMetadata.userPhone)

        val usersGenders = JSONArray()
        usersGenders.put(UserMetadata.userGender)

        val usersEmails = JSONArray()
        usersEmails.put(UserMetadata.userEmail)

        val usersLastViewingTime = JSONArray()
        usersLastViewingTime.put(Calendar.getInstance().time)

        val usersColors = JSONArray()
        usersColors.put(generateUserColor(atr))

        atr.put("usersIds", usersIds)
        atr.put("usersNames", usersNames)
        atr.put("usersPhotos", usersPhotos)
        atr.put("usersPhones", usersPhones)
        atr.put("usersGenders", usersGenders)
        atr.put("usersEmails", usersEmails)
        atr.put("usersLastViewingTime", usersLastViewingTime)
        atr.put("usersColors", usersColors)

        Timber.e("json object = $atr")

        return atr
    }
}