package com.andyshon.tiktalk.firebase

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.MyCancelledCallInvite
import com.andyshon.tiktalk.data.model.calls.CallModel
import com.andyshon.tiktalk.data.model.settings.SettingsModel
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.ui.calls.video.VideoCallActivity
import com.andyshon.tiktalk.ui.calls.SoundPoolManager
import com.andyshon.tiktalk.ui.calls.voice.VoiceCallActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        var model: SettingsModel? = null
        var prefs: PreferenceManager? = null
        var callModel: CallModel? = null
    }

    private val NOTIFICATION_ID_KEY = "NOTIFICATION_ID"
    private val CALL_SID_KEY = "CALL_SID"
    private val VOICE_CHANNEL = "default"

    private lateinit var notificationManager: NotificationManager


    override fun onNewToken(token: String?) {
        Timber.e("onNewToken $token")

        token?.let{
            PreferenceManager(applicationContext, Gson()).putObject(Preference.KEY_FIREBASE_ID, it, String::class.java)
            UserMetadata.firebaseToken = it

            model?.updateFirebaseToken(it)
                ?.subscribe(
                    {
                        Timber.e("update new firebase token to ${it.firebase_token}")
                        prefs?.putObject(Preference.KEY_FIREBASE_ID, it.firebase_token, String::class.java)
                    },
                    { Timber.e("error while update locker ${it.message}") }
                )
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Timber.e("Received remoteMessage = ${remoteMessage?.data?.toString()}")
        //Received remoteMessage = {caller_id=15, type=onCallInvite, caller_avatar=https://tik-talk.s3.eu-central-1.amazonaws.com/attachment/image/15/102385.jpg, only_audio=false, caller_name=Eva, caller_email=eva@gmail.com, caller_phone=+380957148782}
        //Received remoteMessage = {type=onCancelledCallInvite, declined_by_email=eva@gmail.com}

        remoteMessage?.data?.let { data->
            // Check if message contains a data payload.
            if (data.isNotEmpty()) {
                val notificationId = System.currentTimeMillis().toInt()

                if (data.containsKey("type")) {
                    when(data["type"] ?: "onCallInvite") {
                        "onCallInvite" -> {
                            val callerId = (data["caller_id"] ?: "0").toInt()
                            val callerName = data["caller_name"] ?: ""
                            val callerEmail = data["caller_email"] ?: ""
                            val callerPhone = data["caller_phone"] ?: ""
                            val callerAvatar = data["caller_avatar"] ?: ""
                            val channelSid = data["channel_sid"] ?: ""
                            val callInvite = com.andyshon.tiktalk.data.entity.MyCallInvite(callerId, callerName, callerEmail, callerPhone, callerAvatar, channelSid)

                            val onlyAudio = data["only_audio"]?.toBoolean() ?: true
                            this@MyFirebaseMessagingService.notify(callInvite, notificationId)
                            if (onlyAudio) {
                                this@MyFirebaseMessagingService.sendCallInviteToActivity(callInvite, notificationId)
                            }
                            else {
                                this@MyFirebaseMessagingService.sendVideoCallInviteToActivity(callInvite, notificationId)
                            }
                        }
                        "onCancelledCallInvite" -> {
                            if (data.containsKey("declined_by_email")) {
                                val declinedByEmail = (data["declined_by_email"] ?: "")
                                val callerId = (data["caller_id"] ?: "0").toInt()
                                val channelSid = (data["channel_sid"] ?: "")
                                val cancelledCallInvite = MyCancelledCallInvite(declinedByEmail, callerId, channelSid)

                                val onlyAudio = data["only_audio"]?.toBoolean() ?: true
                                this@MyFirebaseMessagingService.cancelNotification(cancelledCallInvite)
                                if (onlyAudio) {
                                    this@MyFirebaseMessagingService.sendCancelledCallInviteToActivity(cancelledCallInvite)
                                }
                                else {
                                    this@MyFirebaseMessagingService.sendCancelledVideoCallInviteToActivity(cancelledCallInvite)
                                }
                            }
                        }
                    }
                }
                else {
                    Timber.e("The message doesn't contain a key type: ${remoteMessage.data}")
                }
            }
        }
    }

    /*
     * Send the MyCallInvite to the VoiceActivity. Start the activity if it is not running already.
     */
    private fun sendCallInviteToActivity(myCallInvite: com.andyshon.tiktalk.data.entity.MyCallInvite, notificationId: Int) {
        val intent = Intent(this, VoiceCallActivity::class.java)
        intent.action = VoiceCallActivity.ACTION_INCOMING_CALL
        intent.putExtra(VoiceCallActivity.INCOMING_CALL_NOTIFICATION_ID, notificationId)
        intent.putExtra(VoiceCallActivity.INCOMING_CALL_INVITE, myCallInvite)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
    }

    /*
     * Send the MyCallInvite to the VideoCallActivity. Start the activity if it is not running already.
     */
    private fun sendVideoCallInviteToActivity(myCallInvite: com.andyshon.tiktalk.data.entity.MyCallInvite, notificationId: Int) {
        val intent = Intent(this, VideoCallActivity::class.java)
        intent.action = VideoCallActivity.ACTION_INCOMING_CALL
        intent.putExtra(VideoCallActivity.INCOMING_CALL_NOTIFICATION_ID, notificationId)
        intent.putExtra(VideoCallActivity.INCOMING_CALL_INVITE, myCallInvite)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
    }

    private fun notify(myCallInvite: com.andyshon.tiktalk.data.entity.MyCallInvite, notificationId: Int) {
        val intent = Intent(this, VoiceCallActivity::class.java)
        intent.action = VoiceCallActivity.ACTION_INCOMING_CALL
        intent.putExtra(VoiceCallActivity.INCOMING_CALL_NOTIFICATION_ID, notificationId)
        intent.putExtra(VoiceCallActivity.INCOMING_CALL_INVITE, myCallInvite)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        /*
         * Pass the notification id and call sid to use as an identifier to cancel the
         * notification later
         */
        val extras = Bundle()
        extras.putInt(NOTIFICATION_ID_KEY, notificationId)
        extras.putString(CALL_SID_KEY, myCallInvite.callerId.toString())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val callInviteChannel = NotificationChannel(
                VOICE_CHANNEL,
                "Primary Voice Channel", NotificationManager.IMPORTANCE_DEFAULT
            )
            callInviteChannel.lightColor = Color.GREEN
            callInviteChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(callInviteChannel)

            val notification = buildNotification(
                myCallInvite.callerName + " is calling.",
                pendingIntent,
                extras
            )
            notificationManager.notify(notificationId, notification)
        } else {
            val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_call_end_white_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(myCallInvite.callerName + " is calling.")
                .setAutoCancel(true)
                .setExtras(extras)
                .setContentIntent(pendingIntent)
                .setGroup("test_app_notification")
                .setColor(Color.rgb(214, 10, 37))

            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }

    /*
     * Send the CancelledCallInvite to the VoiceActivity
     */
    private fun sendCancelledCallInviteToActivity(cancelledCallInvite: MyCancelledCallInvite) {
        val intent = Intent(VoiceCallActivity.ACTION_CANCEL_CALL)
        intent.putExtra(VoiceCallActivity.CANCELLED_CALL_INVITE, cancelledCallInvite)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /*
     * Send the CancelledVideoCallInvite to the VideoCallActivity
     */
    private fun sendCancelledVideoCallInviteToActivity(cancelledCallInvite: MyCancelledCallInvite) {
        val intent = Intent(VideoCallActivity.ACTION_CANCEL_CALL)
        intent.putExtra(VideoCallActivity.CANCELLED_CALL_INVITE, cancelledCallInvite)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun cancelNotification(myCancelledCallInvite: MyCancelledCallInvite) {
        SoundPoolManager.getInstance(this).stopRinging()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            /*
             * If the incoming call was cancelled then remove the notification by matching
             * it with the call sid from the list of notifications in the notification drawer.
             */
            val activeNotifications = notificationManager.activeNotifications
            for (statusBarNotification in activeNotifications) {
                val notification = statusBarNotification.notification
                val extras = notification.extras
                val notificationCallSid = extras.getString(CALL_SID_KEY)

                if (myCancelledCallInvite.callerId.toString() == notificationCallSid) {
                    notificationManager.cancel(extras.getInt(NOTIFICATION_ID_KEY))
                }
            }
        } else {
            /*
             * Prior to Android M the notification manager did not provide a list of
             * active notifications so we lazily clear all the notifications when
             * receiving a CancelledCallInvite.
             *
             * In order to properly cancel a notification using
             * NotificationManager.cancel(notificationId) we should store the call sid &
             * notification id of any incoming calls using shared preferences or some other form
             * of persistent storage.
             */
            notificationManager.cancelAll()
        }
    }

    /**
     * Build a notification.
     *
     * @param text          the text of the notification
     * @param pendingIntent the body, pending intent for the notification
     * @param extras        extras passed with the notification
     * @return the builder
     */
    @TargetApi(Build.VERSION_CODES.O)
    fun buildNotification(text: String, pendingIntent: PendingIntent, extras: Bundle): Notification {
        return Notification.Builder(applicationContext, VOICE_CHANNEL)
            .setSmallIcon(R.drawable.ic_call_end_white_24dp)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setExtras(extras)
            .setAutoCancel(true)
            .build()
    }
}