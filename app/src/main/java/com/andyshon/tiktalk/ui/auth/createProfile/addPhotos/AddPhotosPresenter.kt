package com.andyshon.tiktalk.ui.auth.createProfile.addPhotos

import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.model.auth.AuthModel
import com.andyshon.tiktalk.data.network.request.RegisterRequest
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.data.twilio.ClientSynchronization
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class AddPhotosPresenter @Inject constructor(
    private val authModel: AuthModel,
    private val model: AuthModel,
    private val prefs: PreferenceManager
) : BasePresenter<AddPhotosContract.View>(), ClientSynchronization {

    fun register(
        email: String,
        phoneNumber: String,
        countryCode: String,
        name: String,
        birthDate: String,
        country: String,
        city: String,
        gender: String
    ) {

        val photosMP = arrayListOf<MultipartBody.Part>()

        UserMetadata.photos.forEach {
            Timber.e("UserMetadata.photos, Photo = $it")
            if (it.url.isNotEmpty() && it.url.contains("amazonaws").not()) {
                val file = File(it.url)
                Timber.e("file exists = ${file.exists()}")
                if (file.exists()) {
                    val requestFile =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    val body = MultipartBody.Part.createFormData("images[]", file.name, requestFile)
                    photosMP.add(body)
                }
            }
        }

        authModel.register(
            photosMP,
            RegisterRequest(email, phoneNumber, countryCode, name, birthDate, country, city, gender)
        )
            .compose(applyProgressSingle())
            .subscribe({
                Timber.e("Response Headers: ${it.headers()}")
                val token = it.headers()["Auth-token"]
                Timber.e("Register success = $it, token = $token")
                //    Register success = Response{protocol=http/1.1, code=200, message=OK, url=https://tik-talk-staging-api.herokuapp.com/v1/users/auth}, token = 395427518c9f85d67a5de45480c8afe995930e49
                token?.let {
                    prefs.putObject(Preference.KEY_TOKEN, it, String::class.java)
                }

                val user = it.body()
                Timber.e("User body = ${it.body()}")
                user?.let {
                    prefs.putObject(
                        Preference.KEY_USER_TWILIO_USER_ID,
                        user.user.twilioUserId,
                        String::class.java
                    )
                    prefs.putObject(Preference.KEY_USER_EMAIL, user.user.email, String::class.java)
                    prefs.putObject(Preference.KEY_USER_ID, user.user.id, Int::class.java)
                    prefs.putObject(Preference.KEY_USER_NAME, user.user.name, String::class.java)
                    prefs.putObject(
                        Preference.KEY_USER_WORK,
                        user.user.work ?: "",
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_ABOUT,
                        user.user.aboutYou ?: "",
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_GENDER,
                        user.user.gender,
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_PHONE_NUMBER,
                        user.user.phoneNumber,
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_CODE_COUNTRY,
                        user.user.codeCountry,
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_BIRTH_DATE,
                        user.user.birthDate,
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_COUNTRY,
                        user.user.country,
                        String::class.java
                    )
                    prefs.putObject(Preference.KEY_USER_CITY, user.user.city, String::class.java)
                    prefs.putObject(
                        Preference.KEY_USER_IS_ACCOUNT_BLOCKED,
                        user.user.accountBlock,
                        Boolean::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_MAIN_PHOTO,
                        user.user.images.first().url,
                        String::class.java
                    )

                    prefs.putObject(
                        Preference.KEY_USER_RELATIONSHIP,
                        user.user.relationship,
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_SEXUALITY,
                        user.user.sexuality,
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_HEIGHT,
                        user.user.height ?: "",
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_LIVING,
                        user.user.living,
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_CHILDREN,
                        user.user.children,
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_SMOKING,
                        user.user.smoking,
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_DRINKING,
                        user.user.drinking,
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_LOCKER_TYPE,
                        user.user.lockerType ?: "",
                        String::class.java
                    )
                    prefs.putObject(
                        Preference.KEY_USER_LOCKER_VALUE,
                        user.user.lockerValue ?: "",
                        String::class.java
                    )
//        prefs.putObject(Preference.KEY_USER_I_SPEAK, user.speak, String::class.java)

                    UserMetadata.userId = user.user.id
                    UserMetadata.userName = user.user.name
                    UserMetadata.userEmail = user.user.email
                    UserMetadata.photos = user.user.images
                    UserMetadata.userPhone = user.user.phoneNumber
                    UserMetadata.birthday = user.user.birthDate
                    UserMetadata.lockerType = user.user.lockerType ?: ""
                    UserMetadata.lockerValue = user.user.lockerValue ?: ""
                }

//                view?.onRegistered()
                getTwilioUser()
            }, {
                defaultErrorConsumer
            })
            .addTo(destroyDisposable)
    }


    //TODO call only for the first sign up to application
    private fun getTwilioUser() {
        Timber.e("getTwilioUser called")
        view?.showProgress()
        model.getTwilioUser()
            .subscribe({
                Timber.e("Success get twilio user")
                prefs.putObject(Preference.KEY_USER_TWILIO_USER_ID, it.sid, String::class.java)
                getTwilioToken()
            }, {
                view?.hideProgress()
                Timber.e("Error == ${it.message}")
                Timber.e("tokeeeen = ${prefs.getObject(Preference.KEY_TOKEN, String::class.java)}")
                getTwilioToken()

                //    error = [HTTP 409] 50201 : Unable to create record
                //    User already exists
            })
            .addTo(destroyDisposable)
    }

    private fun getTwilioToken() {
        Timber.e("getTwilioToken called")
        view?.showProgress()
        model.getTwilioToken()
//            .compose(applyProgressSingle())
            .subscribe({
                Timber.e("Get twilio token === ${it.token}")
                TwilioSingleton.instance.connect(getActivityContext(), it.token, this)
            }, {
                view?.hideProgress()
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }

    override fun onSync() {
        view?.hideProgress()
        view?.onRegistered()
    }
}