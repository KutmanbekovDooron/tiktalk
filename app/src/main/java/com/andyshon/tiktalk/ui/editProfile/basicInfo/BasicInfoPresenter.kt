package com.andyshon.tiktalk.ui.editProfile.basicInfo

import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.data.model.settings.SettingsModel
import com.andyshon.tiktalk.data.network.request.UserProfileRequest
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.andyshon.tiktalk.utils.extensions.showChangeNameDialog
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BasicInfoPresenter @Inject constructor(
    private val model: SettingsModel,
    private val prefs: PreferenceManager
) : BasePresenter<BasicInfoContract.View>() {

    var name = ""
    var gender = ""
    var birthDate = ""

    fun changeName(setName: (s:String) -> Unit) {
        view?.let { view ->
            showChangeNameDialog(view.getActivityContext(), UserMetadata.userName) { newName ->
                    setName.invoke(newName)
            }
        }
    }

    fun updateUser() {
        TwilioSingleton.instance.updateUserNameInAllChannels(name) {
            val request = UserProfileRequest(name = name, gender = gender, birthDate = birthDate)
            model.updateUserProfile(request)
                .compose(applyProgressSingle())
                .subscribe({
                    savePrefs(it)
                    view?.updatedUser()
                }, {
                    Timber.e("Error = ${it.message}")
                })
                .addTo(destroyDisposable)
        }
    }

    private fun savePrefs(user: User) {
        prefs.putObject(Preference.KEY_USER_NAME, user.name, String::class.java)
        prefs.putObject(Preference.KEY_USER_GENDER, user.gender, String::class.java)
        prefs.putObject(Preference.KEY_USER_BIRTH_DATE, user.birthDate, String::class.java)
        UserMetadata.userName = user.name
        UserMetadata.userGender = user.gender
        UserMetadata.birthday = user.birthDate
        UserMetadata.photos = user.images
    }

}