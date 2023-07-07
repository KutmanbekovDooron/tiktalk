package com.andyshon.tiktalk.ui.settings

import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.model.auth.AuthModel
import com.andyshon.tiktalk.data.model.settings.SettingsModel
import com.andyshon.tiktalk.data.network.request.UserNotificationsRequest
import com.andyshon.tiktalk.data.network.request.UserSettingsRequest
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import org.jetbrains.anko.alert
import timber.log.Timber
import javax.inject.Inject

class SettingsPresenter @Inject constructor(
    private val model: SettingsModel,
    private val authModel: AuthModel,
    private val prefs: PreferenceManager
) : BasePresenter<SettingsContract.View>() {

    var lookingForMale = false
    var lookingForFemale = false
    var minAge: Int = 18
    var maxAge: Int = 60
    var distance: Int = 10
    var showInApp = true
    var showInPlaces = true
    var birthDate = ""

    var changeNotifications = false

    fun initPrefs() {
//        if (prefs.has(Preference.KEY_USER_LOOKING_FOR_MALE).not()) {
            getUser()
//        }
//        else {
//            lookingForMale = prefs.getObject(Preference.KEY_USER_LOOKING_FOR_MALE, Boolean::class.java) ?: false
//            lookingForFemale = prefs.getObject(Preference.KEY_USER_LOOKING_FOR_FEMALE, Boolean::class.java) ?: false
//            minAge = prefs.getObject(Preference.KEY_USER_MIN_AGE, Int::class.java) ?: 18
//            maxAge = prefs.getObject(Preference.KEY_USER_MAX_AGE, Int::class.java) ?: 60
//            distance = prefs.getObject(Preference.KEY_USER_DISTANCE, Int::class.java) ?: 10
//            showInApp = prefs.getObject(Preference.KEY_USER_SHOW_IN_APP, Boolean::class.java) ?: true
//            showInPlaces = prefs.getObject(Preference.KEY_USER_SHOW_IN_PLACES, Boolean::class.java) ?: true
//
//            view?.fillPrefs()
//        }
    }

    private fun getUser() {
        model.getUser(UserMetadata.userId)
            .compose(applyProgressSingle())
            .subscribe({
                UserMetadata.twilioUserId = it.twilioUserId
                lookingForMale = it.preferGenderMale
                lookingForFemale = it.preferGenderFemale
                minAge = it.preferMinAge
                maxAge = it.preferMaxAge
                distance = it.preferLocationDistance
                showInApp = it.isShowInApp
                showInPlaces = it.isShowInPlaces
                birthDate = it.birthDate
                UserMetadata.photos = it.images
                UserMetadata.languages = it.languages
                val languages = mutableSetOf<String>()
                it.languages.forEach {
                    languages.add(it)
                }
                if (languages.isNotEmpty()) {
                    prefs.removeValue(Preference.KEY_USER_I_SPEAK)
                    prefs.putStringSet(Preference.KEY_USER_I_SPEAK, languages)
                }
                savePrefs()
                view?.fillPrefs()
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }

    private fun savePrefs() {
        prefs.putObject(Preference.KEY_USER_TWILIO_USER_ID, UserMetadata.twilioUserId, String::class.java)
        prefs.putObject(Preference.KEY_USER_LOOKING_FOR_MALE, lookingForMale, Boolean::class.java)
        prefs.putObject(Preference.KEY_USER_LOOKING_FOR_FEMALE, lookingForFemale, Boolean::class.java)
        prefs.putObject(Preference.KEY_USER_MIN_AGE, minAge, Int::class.java)
        prefs.putObject(Preference.KEY_USER_MAX_AGE, maxAge, Int::class.java)
        prefs.putObject(Preference.KEY_USER_DISTANCE, distance, Int::class.java)
        prefs.putObject(Preference.KEY_USER_SHOW_IN_APP, showInApp, Boolean::class.java)
        prefs.putObject(Preference.KEY_USER_SHOW_IN_PLACES, showInPlaces, Boolean::class.java)
        prefs.putObject(Preference.KEY_USER_BIRTH_DATE, birthDate, String::class.java)
        UserMetadata.birthday = birthDate
    }

    fun updateSettings() {
        if (compare()) {
            val pauseAll = prefs.getObject(Preference.Notifications.PAUSE_ALL, Boolean::class.java) ?: false
            val messages = prefs.getObject(Preference.Notifications.MESSAGES, Boolean::class.java) ?: true
            val newMatches = prefs.getObject(Preference.Notifications.NEW_MATCHES, Boolean::class.java) ?: true
            val likeYou = prefs.getObject(Preference.Notifications.SOMEBODY_LIKE_YOU, Boolean::class.java) ?: true
            val messageInPrivateRoom = prefs.getObject(Preference.Notifications.NEW_MESSAGES_IN_PRIVATE_ROOM, Boolean::class.java) ?: true
            val superLike = prefs.getObject(Preference.Notifications.SUPER_LIKE, Boolean::class.java) ?: true

            val userNotificationsRequest = UserNotificationsRequest(pauseAll, messages, newMatches, likeYou, messageInPrivateRoom, superLike)
            val request = UserSettingsRequest(preferGenderFemale = lookingForFemale, preferGenderMale = lookingForMale, preferMinAge = minAge, preferMaxAge = maxAge,
                preferLocationDistance = distance, isShowInApp = showInApp, isShowInPlaces = showInPlaces, notifications = userNotificationsRequest)
            model.updateSettings(request)
                .compose(applyProgressSingle())
                .subscribe({
                    savePrefs()
                    view?.updated()
                }, {
                    Timber.e("Error = ${it.message}")
                })
                .addTo(destroyDisposable)
        } else {
            view?.updated()
        }
    }

    private fun compare(): Boolean {
        return lookingForFemale != (prefs.getObject(Preference.KEY_USER_LOOKING_FOR_FEMALE, Boolean::class.java) == true) ||
                lookingForMale != (prefs.getObject(Preference.KEY_USER_LOOKING_FOR_MALE, Boolean::class.java) == true) ||
                minAge != (prefs.getObject(Preference.KEY_USER_MIN_AGE, Int::class.java) ?: 18) ||
                maxAge != (prefs.getObject(Preference.KEY_USER_MAX_AGE, Int::class.java) ?: 60) ||
                distance != (prefs.getObject(Preference.KEY_USER_DISTANCE, Int::class.java) ?: 10) ||
                showInApp != (prefs.getObject(Preference.KEY_USER_SHOW_IN_APP, Boolean::class.java) == true) ||
                showInPlaces != (prefs.getObject(Preference.KEY_USER_SHOW_IN_PLACES, Boolean::class.java) == true) ||
                changeNotifications
    }

    fun resetDislikes() {
        model.resetDislikes()
            .compose(applyProgressCompletable())
            .subscribe({
                view?.dislikesResetted()
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }

    fun logout() {
        authModel.logout()
            .compose(applyProgressCompletable())
            .subscribe({
                prefs.clear()
                view?.logout()
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }

    fun deleteAccount() {
        deleteAccountConfirmation {
            authModel.deleteAccount()
                .compose(applyProgressCompletable())
                .subscribe({
                    prefs.clear()
                    view?.deleteAccount()
                }, {
                    defaultErrorConsumer
                })
                .addTo(destroyDisposable)
        }
    }

    private fun deleteAccountConfirmation(callback: () -> Unit) {
        view?.let {
            it.getActivityContext().alert("Do you want to delete your account?") {
                isCancelable = true
                positiveButton("Delete") {
                    callback.invoke()
                }
                negativeButton("Cancel") { it.dismiss() }
            }.show()
        }
    }

}