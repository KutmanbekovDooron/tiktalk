package com.andyshon.tiktalk.ui.locker

import android.annotation.SuppressLint
import android.os.AsyncTask
import com.andyshon.tiktalk.data.model.settings.SettingsModel
import com.andyshon.tiktalk.data.network.request.UserProfileRequest
import timber.log.Timber

class SaveLockerAsync(
    private val model: SettingsModel,
    private val lockerType: String,
    private val lockerValue: String
) : AsyncTask<Void, Void, Void>() {
    init {
        execute()
    }

    @SuppressLint("CheckResult")
    override fun doInBackground(vararg p0: Void?): Void? {
        model.updateUserProfile(UserProfileRequest(lockerType = lockerType, lockerValue = lockerValue))
            .subscribe({}, { Timber.e("error while update locker ${it.message}") })
        return null
    }
}