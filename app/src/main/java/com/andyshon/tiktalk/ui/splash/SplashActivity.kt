package com.andyshon.tiktalk.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.Image
import com.andyshon.tiktalk.data.model.auth.AuthModel
import com.andyshon.tiktalk.data.model.calls.CallModel
import com.andyshon.tiktalk.data.model.settings.SettingsModel
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.firebase.MyFirebaseMessagingService
import com.andyshon.tiktalk.ui.MainActivity
import com.andyshon.tiktalk.ui.auth.signIn.SignInActivity
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.startWith.StartWithActivity
import com.andyshon.tiktalk.utils.phone.initRegionNameForEmojiesList
import com.google.android.gms.tasks.*
import com.google.firebase.messaging.FirebaseMessaging
import com.twilio.chat.*
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject


class SplashActivity : BaseInjectActivity() {

    override fun getPresenter(): BaseContract.Presenter<*>? = null

    @Inject
    lateinit var prefs: PreferenceManager

    @Inject
    lateinit var model: SettingsModel

    @Inject
    lateinit var callModel: CallModel

    @Inject
    lateinit var authModel: AuthModel

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        MyFirebaseMessagingService.model = model
        MyFirebaseMessagingService.prefs = prefs
        MyFirebaseMessagingService.callModel = callModel

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE)

        Timber.e("user token = ${prefs.getObject(Preference.KEY_TOKEN, String::class.java)}")
        Timber.e(
            "isPhoneVerified = ${
                prefs.getObject(
                    Preference.KEY_USER_PHONE_VERIFIED,
                    Boolean::class.java
                )
            }"
        )
        Timber.e("push token = ${FirebaseMessaging.getInstance().token}")

        initRegionNameForEmojiesList()

//        CreateProfileActivity.startActivity(this)

//        val intent = Intent(this@SplashActivity, SignInActivity::class.java)
//        startActivity(intent)

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token: String ->
            if (!TextUtils.isEmpty(token)) {
                Timber.e("firebaseToken = $token")
                token.let {
                    val oldToken =
                        prefs.getObject(Preference.KEY_FIREBASE_ID, String::class.java) ?: ""
//            if (oldToken.isNotEmpty() && oldToken != firebaseToken) {
//                prefs.putObject(Preference.KEY_FIREBASE_ID, it, String::class.java)
                    UserMetadata.firebaseToken = token



                    model.updateFirebaseToken(token)
                        .subscribe(
                            {
                                Timber.e("update new firebase token to ${it.firebase_token}")
                                prefs.putObject(
                                    Preference.KEY_FIREBASE_ID,
                                    it.firebase_token,
                                    String::class.java
                                )
                            },
                            { Timber.e("error while update locker ${it.message}") }
                        )
                }
            }
        }.addOnFailureListener { e: Exception? -> print("error" + e?.message) }


        val twilioUserToken = prefs.getObject(Preference.KEY_USER_TWILIO_USER_ID, String::class.java) ?: ""
        UserMetadata.twilioUserId = twilioUserToken

        val userToken = prefs.getObject(Preference.KEY_TOKEN, String::class.java) ?: ""
        Timber.e("user token = $userToken")
        if (userToken.isNotEmpty()) {
            getTokenTwilio()
        } else {
            Handler().postDelayed({
                val intent = Intent(this@SplashActivity, StartWithActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }, 2000)
        }
    }

    private fun getTokenTwilio() {
        authModel.getTwilioToken()
            .subscribe({
                Timber.e("Get twilio token === ${it.token}")
                TwilioSingleton.instance.connect(this, it.token, object : ChatClientListener {
                    override fun onClientSynchronization(status: ChatClient.SynchronizationStatus?) {
                        Timber.e("onClientSynchronization, status === $status, ${status?.value}")

                        if (status == ChatClient.SynchronizationStatus.COMPLETED) {
                            restoreUserMetadata()
                            val intent = Intent(this@SplashActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                    }

                    override fun onChannelDeleted(p0: Channel?) {}
                    override fun onInvitedToChannelNotification(p0: String?) {}
                    override fun onNotificationSubscribed() {}
                    override fun onUserSubscribed(p0: User?) {}
                    override fun onChannelUpdated(p0: Channel?, p1: Channel.UpdateReason?) {}
                    override fun onRemovedFromChannelNotification(p0: String?) {}
                    override fun onNotificationFailed(p0: ErrorInfo?) {}
                    override fun onTokenExpired() {}
                    override fun onChannelJoined(p0: Channel?) {}
                    override fun onChannelAdded(p0: Channel?) {}
                    override fun onChannelSynchronizationChange(p0: Channel?) {}
                    override fun onUserUnsubscribed(p0: User?) {}
                    override fun onAddedToChannelNotification(p0: String?) {}
                    override fun onChannelInvited(p0: Channel?) {}
                    override fun onNewMessageNotification(p0: String?, p1: String?, p2: Long) {}
                    override fun onConnectionStateChange(p0: ChatClient.ConnectionState?) {}
                    override fun onError(p0: ErrorInfo?) {}
                    override fun onUserUpdated(p0: User?, p1: User.UpdateReason?) {}
                    override fun onTokenAboutToExpire() {}
                })
            }, {
                Timber.e("getTokenTwilio, Error = ${it.message}")
                //todo: no network
                //  Error = Unable to resolve host "tik-talk-staging-api.herokuapp.com": No address associated with hostname
            })
            .addTo(getDestroyDisposable())
    }

    private fun restoreUserMetadata() {
        val userId = prefs.getObject(Preference.KEY_USER_ID, Int::class.java) ?: -1
        val userName = prefs.getObject(Preference.KEY_USER_NAME, String::class.java) ?: ""
        val userEmail = prefs.getObject(Preference.KEY_USER_EMAIL, String::class.java) ?: ""
        val userMainPhoto =
            prefs.getObject(Preference.KEY_USER_MAIN_PHOTO, String::class.java) ?: ""
        val userPhone = prefs.getObject(Preference.KEY_USER_PHONE_NUMBER, String::class.java) ?: ""
        val userBirthday = prefs.getObject(Preference.KEY_USER_BIRTH_DATE, String::class.java) ?: ""
        val userLockerType =
            prefs.getObject(Preference.KEY_USER_LOCKER_TYPE, String::class.java) ?: ""
        val userLockerValue =
            prefs.getObject(Preference.KEY_USER_LOCKER_VALUE, String::class.java) ?: ""

        UserMetadata.userId = userId
        UserMetadata.userName = userName
        UserMetadata.userEmail = userEmail
        UserMetadata.photos[0] = Image(0, userMainPhoto)
        UserMetadata.userPhone = userPhone
        UserMetadata.birthday = userBirthday
        UserMetadata.lockerType = userLockerType
        UserMetadata.lockerValue = userLockerValue
    }
}
