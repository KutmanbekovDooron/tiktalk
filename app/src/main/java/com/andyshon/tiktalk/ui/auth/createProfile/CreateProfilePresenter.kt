package com.andyshon.tiktalk.ui.auth.createProfile

import android.widget.EditText
import com.andyshon.tiktalk.data.model.auth.AuthModel
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.rxkotlin.addTo
import java.util.*
import javax.inject.Inject

class CreateProfilePresenter @Inject constructor(private val model: AuthModel, private val prefs: PreferenceManager) : BasePresenter<CreateProfileContract.View>() {

    fun checkIfEmailExists(email: String) {
        model.getEmailStatus(email)
            .subscribe({
                view?.emailTaken()
            }, {
                view?.emailFree()
            })
            .addTo(destroyDisposable)
    }

    fun getUserPhoneNumber(): String =
        prefs.getObject(Preference.KEY_USER_PHONE_NUMBER, String::class.java) ?: ""

    fun getUserCodeCountry(): String =
        prefs.getObject(Preference.KEY_USER_CODE_COUNTRY, String::class.java) ?: ""


    fun validateDOB(year: Int, month: Int, day: Int, etDateOfBirth: EditText): Boolean {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val differenceInMillis = Calendar.getInstance().timeInMillis - calendar.timeInMillis
        val differenceInHours = ((differenceInMillis / 1000) / 60) / 60
        val differenceInYears = differenceInHours / 8670
        var valid = true
        if (differenceInYears < 16) {
            valid = false
        }

        if (valid.not()) {
            (etDateOfBirth.parent.parent as TextInputLayout).isErrorEnabled = true
            (etDateOfBirth.parent.parent as TextInputLayout).error = "You need to be at least 16 years old to use this app"
        }
        else {
            (etDateOfBirth.parent.parent as TextInputLayout).error = null
            (etDateOfBirth.parent.parent as TextInputLayout).isErrorEnabled = false
        }
        return valid
    }
}