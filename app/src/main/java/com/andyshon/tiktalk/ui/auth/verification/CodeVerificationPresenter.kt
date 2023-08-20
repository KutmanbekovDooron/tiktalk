package com.andyshon.tiktalk.ui.auth.verification

import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.data.model.auth.AuthModel
import com.andyshon.tiktalk.data.network.error.ApiException
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.data.twilio.ClientSynchronization
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.twilio.chat.Channel
import com.twilio.chat.ChatClient
import com.twilio.chat.ChatClientListener
import com.twilio.chat.ErrorInfo
import io.reactivex.rxkotlin.addTo
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class CodeVerificationPresenter @Inject constructor(
    private val model: AuthModel,
    private val prefs: PreferenceManager
) : BasePresenter<CodeVerificationContract.View>() {

    fun verifyPhone(code: String, phone: String) {
        view?.showProgress()
        model.sendSMS(code)
//            .compose(applyProgressSingle())
            .subscribe({
                view?.hideProgress()
                prefs.putObject(Preference.KEY_USER_PHONE_VERIFIED, true, Boolean::class.java)
                val token = it.headers()["Auth-token"]
                Timber.e("User Token = $token")
                token?.let {
                    prefs.putObject(Preference.KEY_TOKEN, it, String::class.java)
                }
                Timber.e("verifyPhone return = $it, body = ${it.body()}")
                if (it.body()?.user != null) {
                    saveUser(it.body()?.user!!)
//                    view?.onUserReturn()
//                    getTwilioUser(true)
                    getTwilioToken(true)
                } else {
//                    Timber.e("Else, code = ${it.code()}, error = ${it.errorBody()?.string()}, ${it.message()}")
                    //  Else, code = 400, error = {"message":"Incorect code"}, Bad Request
                    if (it.code() == 400) {
                        val json = JSONObject(it.errorBody()?.string())
                        Timber.e("json = $json")
                        if (json.has("message")) {
                            val messageBody = json.getString("message")
                            view?.onIncorrectCode()
                        } else {
                            view?.onIncorrectCode()
                        }
                    } else {
                        prefs.putObject(Preference.KEY_USER_PHONE_NUMBER, phone, String::class.java)
//                    view?.onPhoneVerified()
                        val twilioUser =
                            prefs.getObject(Preference.KEY_USER_TWILIO_USER_ID, String::class.java)
                                ?: ""
                        Timber.e("twilioUser = $twilioUser")
                        if (twilioUser.isNotEmpty()) {
                            getTwilioUser(false)
                        } else {
                            view?.onPhoneVerified()
                        }
                    }
                }
            }, {
                view?.hideProgress()
                Timber.e("Phone verify error: $it")
                when (it) {
                    is ApiException -> {
                        if (it.mMessage.contains("blocked", true)) {
                            view?.onBlockedAccount()
                        } else {
                            view?.onIncorrectCode()
                        }
                    }

                    else -> defaultErrorConsumer
                }
            })
            .addTo(destroyDisposable)
    }

    //TODO call only for the first sign up to application
    private fun getTwilioUser(b: Boolean) {
//        view?.showProgress()
        model.getTwilioUser().subscribe({
                Timber.e("Success get twilio user")
                prefs.putObject(Preference.KEY_USER_TWILIO_USER_ID, it.sid, String::class.java)
                getTwilioToken(b)
            }, {
                view?.hideProgress()
                Timber.e("Error == ${it.message}")

                //    error = [HTTP 409] 50201 : Unable to create record
                //    User already exists
            })
            .addTo(destroyDisposable)
    }

    private fun getTwilioToken(b: Boolean) {
        view?.showProgress()
        model.getTwilioToken()
//            .compose(applyProgressSingle())
            .subscribe({
                Timber.e("Get twilio token === ${it.token}")
                TwilioSingleton.instance.connect(getActivityContext(), it.token,
                    object : ClientSynchronization {
                        override fun onSync() {
                            view?.hideProgress()
                            if (b.not()) {
                                view?.onPhoneVerified()
                            } else {
                                view?.onUserReturn()
                            }
                        }
                    })
            }, {
                view?.hideProgress()
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }

    private fun saveUser(user: User) {
        prefs.putObject(Preference.KEY_USER_TWILIO_USER_ID, user.twilioUserId, String::class.java)
        prefs.putObject(Preference.KEY_USER_EMAIL, user.email, String::class.java)
        prefs.putObject(Preference.KEY_USER_ID, user.id, Int::class.java)
        prefs.putObject(Preference.KEY_USER_NAME, user.name, String::class.java)
        prefs.putObject(Preference.KEY_USER_WORK, user.work ?: "", String::class.java)
        prefs.putObject(Preference.KEY_USER_ABOUT, user.aboutYou ?: "", String::class.java)
        prefs.putObject(Preference.KEY_USER_GENDER, user.gender, String::class.java)
        prefs.putObject(Preference.KEY_USER_PHONE_NUMBER, user.phoneNumber, String::class.java)
        prefs.putObject(Preference.KEY_USER_CODE_COUNTRY, user.codeCountry, String::class.java)
        prefs.putObject(Preference.KEY_USER_BIRTH_DATE, user.birthDate, String::class.java)
        prefs.putObject(Preference.KEY_USER_COUNTRY, user.country, String::class.java)
        prefs.putObject(Preference.KEY_USER_CITY, user.city, String::class.java)
        prefs.putObject(
            Preference.KEY_USER_IS_ACCOUNT_BLOCKED,
            user.accountBlock,
            Boolean::class.java
        )
        prefs.putObject(Preference.KEY_USER_MAIN_PHOTO, user.images.first().url, String::class.java)

        prefs.putObject(Preference.KEY_USER_RELATIONSHIP, user.relationship, String::class.java)
        prefs.putObject(Preference.KEY_USER_SEXUALITY, user.sexuality, String::class.java)
        prefs.putObject(Preference.KEY_USER_HEIGHT, user.height ?: "", String::class.java)
        prefs.putObject(Preference.KEY_USER_LIVING, user.living, String::class.java)
        prefs.putObject(Preference.KEY_USER_CHILDREN, user.children, String::class.java)
        prefs.putObject(Preference.KEY_USER_SMOKING, user.smoking, String::class.java)
        prefs.putObject(Preference.KEY_USER_DRINKING, user.drinking, String::class.java)
//        prefs.putObject(Preference.KEY_USER_I_SPEAK, user.speak, String::class.java)
        prefs.putObject(Preference.KEY_USER_LOCKER_TYPE, user.lockerType ?: "", String::class.java)
        prefs.putObject(
            Preference.KEY_USER_LOCKER_VALUE,
            user.lockerValue ?: "",
            String::class.java
        )

        UserMetadata.userId = user.id
        UserMetadata.userName = user.name
        UserMetadata.userEmail = user.email
        UserMetadata.photos = user.images
        UserMetadata.userPhone = user.phoneNumber
        UserMetadata.birthday = user.birthDate
        UserMetadata.lockerType = user.lockerType ?: ""
        UserMetadata.lockerValue = user.lockerValue ?: ""
    }
}