package com.andyshon.tiktalk.ui.editProfile.moreLanguages

import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.model.settings.SettingsModel
import com.andyshon.tiktalk.data.network.request.UserProfileRequest
import com.andyshon.tiktalk.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class MoreLanguagesPresenter @Inject constructor(private val model: SettingsModel) : BasePresenter<MoreLanguagesContract.View>() {

    var selectedLanguages = mutableSetOf<String>()
    var set = mutableSetOf<String>()

    fun updateLanguages() {
        val request = UserProfileRequest(languages = selectedLanguages.toList())
        model.updateUserProfile(request)
            .compose(applyProgressSingle())
            .subscribe({
                UserMetadata.languages = it.languages
                view?.updatedUser()
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }
}