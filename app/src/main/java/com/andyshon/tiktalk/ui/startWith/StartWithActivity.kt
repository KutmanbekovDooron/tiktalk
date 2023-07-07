package com.andyshon.tiktalk.ui.startWith

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.util.Log
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.auth.signIn.SignInActivity
import com.andyshon.tiktalk.utils.extensions.addClickableSpannable
import com.andyshon.tiktalk.utils.extensions.color
import com.andyshon.tiktalk.utils.extensions.string
import com.twilio.chat.*
import kotlinx.android.synthetic.main.activity_start_with.*
import timber.log.Timber
import com.twilio.chat.ErrorInfo

class StartWithActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_with)

        initListeners()
        setAgreementClickableSpan()


//        initTwillio()
    }

    private lateinit var mChatClient: ChatClient

    private fun initTwillio() {

        val props = ChatClient.Properties.Builder()
            .createProperties()

        /*mChatClient = */ChatClient.create(this,
//            "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJTSzU4NDdiNTFiNmQ5Yzg0MmUzZWQwYmRlZTZmZWI5YWQ0LTE1NjI1ODgzOTgiLCJncmFudHMiOnsiaWRlbnRpdHkiOiJ1c2VyQGdtYWlsLmNvbSIsImNoYXQiOnsic2VydmljZV9zaWQiOiJJUzA5ZTA4MGVjM2RjYTRhODQ4MDU3MDcyMzdiYWI2ZjZjIn19LCJpc3MiOiJTSzU4NDdiNTFiNmQ5Yzg0MmUzZWQwYmRlZTZmZWI5YWQ0IiwibmJmIjoxNTYyNTg4Mzk4LCJleHAiOjE1NjI1OTE5OTgsInN1YiI6IkFDY2JiODFlZDA5ZGE3YjE2ZjYwZmMzMjI5YWI5YTA2YTEifQ.sTKwABp_OKmG-Ss1RCR0oWeHZhTlkI7S2dmlbaGQOek",
//            "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJTSzU4NDdiNTFiNmQ5Yzg0MmUzZWQwYmRlZTZmZWI5YWQ0LTE1NjI1ODkwMzEiLCJncmFudHMiOnsiaWRlbnRpdHkiOiJzZCIsImNoYXQiOnsic2VydmljZV9zaWQiOiJJUzA5ZTA4MGVjM2RjYTRhODQ4MDU3MDcyMzdiYWI2ZjZjIn19LCJpc3MiOiJTSzU4NDdiNTFiNmQ5Yzg0MmUzZWQwYmRlZTZmZWI5YWQ0IiwibmJmIjoxNTYyNTg5MDMxLCJleHAiOjE1NjI1OTI2MzEsInN1YiI6IkFDY2JiODFlZDA5ZGE3YjE2ZjYwZmMzMjI5YWI5YTA2YTEifQ.2b_yL2NwUL0d8MGD2ThV63fC4FJ8no_E6QVNCa5cp7I",
//            "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJTSzU4NDdiNTFiNmQ5Yzg0MmUzZWQwYmRlZTZmZWI5YWQ0LTE1NjI1ODkyOTIiLCJncmFudHMiOnsiaWRlbnRpdHkiOiJ1c2VyQGdtYWlsLmNvbSIsImNoYXQiOnsic2VydmljZV9zaWQiOiJJUzA5ZTA4MGVjM2RjYTRhODQ4MDU3MDcyMzdiYWI2ZjZjIn19LCJpc3MiOiJTSzU4NDdiNTFiNmQ5Yzg0MmUzZWQwYmRlZTZmZWI5YWQ0IiwibmJmIjoxNTYyNTg5MjkyLCJleHAiOjE1NjI1OTI4OTIsInN1YiI6IkFDY2JiODFlZDA5ZGE3YjE2ZjYwZmMzMjI5YWI5YTA2YTEifQ.iy4ysZZ62CH0p5FwWJdg21k8uRQ8fEuH5SxE5r-D714",
            "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJTSzU4NDdiNTFiNmQ5Yzg0MmUzZWQwYmRlZTZmZWI5YWQ0LTE1NjI1ODk3MjMiLCJncmFudHMiOnsiaWRlbnRpdHkiOiJ1c2VyQGdtYWlsLmNvbSIsImNoYXQiOnsic2VydmljZV9zaWQiOiJJUzA5ZTA4MGVjM2RjYTRhODQ4MDU3MDcyMzdiYWI2ZjZjIn19LCJpc3MiOiJTSzU4NDdiNTFiNmQ5Yzg0MmUzZWQwYmRlZTZmZWI5YWQ0IiwibmJmIjoxNTYyNTg5NzIzLCJleHAiOjE1NjI1OTMzMjMsInN1YiI6IkFDY2JiODFlZDA5ZGE3YjE2ZjYwZmMzMjI5YWI5YTA2YTEifQ.yTOsR8h2U7KcvjQ-xQkY8WlQC6R2WDB0XpeUtSzrBLA",
            props,
            object : CallbackListener<ChatClient>() {
                override fun onSuccess(client: ChatClient) {
                    client.setListener(object : ChatClientListener {
                        // save client for future use here
                        override fun onClientSynchronization(status: ChatClient.SynchronizationStatus) {
                            Timber.e("onClientSynchronization, status = $status, value = ${status.value}, client = ${client}, ${client.channels}, ${client.users}")
                            if (status == ChatClient.SynchronizationStatus.COMPLETED) {
                                // Client is now ready for business, start working

                                client.channels.channelBuilder()
                                    .withFriendlyName("Channel_test_02")
                                    .withType(Channel.ChannelType.PUBLIC)
                                    .build(object: CallbackListener<Channel>() {
                                        override fun onSuccess(channel: Channel?) {
                                            if (channel != null) {
                                                Timber.e("Success creating channel $channel, ${channel.sid}, ${channel.status}")
                                                joinChannel(channel)


//                                                val channels = client.getChannels().getSubscribedChannels()
//                                                channels.first().
//                                                for (channel in channels) {
//                                                    Log.d(
//                                                        FragmentActivity.TAG,
//                                                        "Channel named: " + channel.getFriendlyName()
//                                                    )
//                                                }
                                            }
                                            else {
                                                Timber.e("Error while creating channel")
                                            }
                                        }
                                        override fun onError(errorInfo: ErrorInfo?) {
//                                            super.onError(errorInfo)
                                            Timber.e("Error while creating channel 2 = $errorInfo, ${errorInfo?.code}, ${errorInfo?.message}, ${errorInfo?.status}")

                                            //  Error while creating channel 2 = Error 0:50107 User unauthorized for command, 50107
                                        }
                                    });
                            }
                        }

                        override fun onChannelDeleted(p0: Channel?) {
                            Timber.e("onChannelDeleted = ${p0}")
                        }

                        override fun onInvitedToChannelNotification(p0: String?) {
                            Timber.e("onInvitedToChannelNotification = ${p0}")
                        }

                        override fun onNotificationSubscribed() {
                            Timber.e("onNotificationSubscribed")
                        }

                        override fun onUserSubscribed(p0: User?) {
                            Timber.e("onUserSubscribed = ${p0}")
                            Timber.e("onUserSubscribed = ${p0}, ${p0?.attributes}, ${p0?.friendlyName}, ${p0?.identity}, ${p0?.isNotifiable}, ${p0?.isOnline}, ${p0?.isSubscribed}")
                        }

                        override fun onChannelUpdated(p0: Channel?, p1: Channel.UpdateReason?) {
                            Timber.e("onChannelUpdated = ${p0}, ${p1}")
                        }

                        override fun onRemovedFromChannelNotification(p0: String?) {
                            Timber.e("onRemovedFromChannelNotification = ${p0}")
                        }

                        override fun onNotificationFailed(p0: ErrorInfo?) {
                            Timber.e("onNotificationFailed = ${p0}")
                        }

                        override fun onTokenExpired() {
                            Timber.e("onTokenExpired")
                        }

                        override fun onChannelJoined(p0: Channel?) {
                            Timber.e("onChannelJoined = ${p0}")
                        }

                        override fun onChannelAdded(p0: Channel?) {
                            Timber.e("onChannelAdded = ${p0}")
                        }

                        override fun onChannelSynchronizationChange(p0: Channel?) {
                            Timber.e("onChannelSynchronizationChange = ${p0}")
                        }

                        override fun onUserUnsubscribed(p0: User?) {
                            Timber.e("onUserUnsubscribed = ${p0}, ${p0?.attributes}, ${p0?.friendlyName}, ${p0?.identity}, ${p0?.isNotifiable}, ${p0?.isOnline}, ${p0?.isSubscribed}")
                        }

                        override fun onAddedToChannelNotification(p0: String?) {
                            Timber.e("onAddedToChannelNotification = ${p0}")
                        }

                        override fun onChannelInvited(p0: Channel?) {
                            Timber.e("onChannelInvited = ${p0}")
                        }

                        override fun onNewMessageNotification(p0: String?, p1: String?, p2: Long) {
                            Timber.e("onNewMessageNotification = ${p0}, ${p1}, ${p2}")
                        }

                        override fun onConnectionStateChange(p0: ChatClient.ConnectionState?) {
                            Timber.e("onConnectionStateChange = ${p0}")
                        }

                        override fun onError(p0: ErrorInfo?) {
                            Timber.e("onError = ${p0}")
                        }

                        override fun onUserUpdated(p0: User?, p1: User.UpdateReason?) {
                            Timber.e("onUserUpdated = ${p0}, ${p1}")
                        }

                        override fun onTokenAboutToExpire() {
                            Timber.e("onTokenAboutToExpire")
                        }
                    })
                }
            })
    }

    private fun joinChannel(channel: Channel) {
        Timber.e("Joining Channel: ${channel.uniqueName}")
        channel.join(object : StatusListener() {
            override fun onSuccess() {
                Timber.e("Joined channel")

                channel.messages.sendMessage(Message.options().withBody("OPA CHIRIK 4"), object : CallbackListener<Message>() {
                    override fun onSuccess(p0: Message?) {
                        Timber.e("onSuccess: ${p0}, ${p0?.author}, ${p0?.channel}, ${p0?.channelSid}, ${p0?.dateCreated}, ${p0?.dateCreatedAsDate}")
                    }

                    override fun onError(errorInfo: ErrorInfo?) {
                        Timber.e("onError: ${errorInfo?.status}, ${errorInfo?.message}, ${errorInfo?.code}")
                    }

                })

                /*channel.messages.sendMessage("test", object : StatusListener() {
                    override fun onSuccess() {
                        Timber.e("Message sent successfully")
                    }

                    override fun onError(errorInfo: ErrorInfo) {
                        Timber.e("Error sending message: $errorInfo")
                    }
                })*/
            }

            override fun onError(errorInfo: ErrorInfo) {
                Timber.e("Error joining channel: $errorInfo")
            }
        })
    }



    private var mLastClickTime = 0L

    private fun initListeners() {
        btnStartWithPhone.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            mLastClickTime = SystemClock.elapsedRealtime()

            val intent = Intent(this@StartWithActivity, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setAgreementClickableSpan() {
        val spannableText = SpannableStringBuilder()
        spannableText.append(this string R.string.start_with_terms)
        val termsOfUse = this string R.string.start_with_terms_part1
        spannableText.addClickableSpannable(termsOfUse, this color R.color.colorGrey) {
            //termsOfUse click
        }
        val copyright = this string R.string.start_with_terms_part2
        spannableText.addClickableSpannable(copyright, this color R.color.colorGrey) {
            //copyright click
        }
        if (copyright == "Terms of Service") {
            spannableText.setSpan(UnderlineSpan(), spannableText.length-37, spannableText.length-23, 0)
            spannableText.setSpan(UnderlineSpan(), spannableText.length-15, spannableText.length, 0)
        }
        else {
            spannableText.setSpan(UnderlineSpan(), spannableText.length-35, spannableText.length-19, 0)
            spannableText.setSpan(UnderlineSpan(), spannableText.length-14, spannableText.length, 0)
        }
        tvTerms.text = spannableText
        tvTerms.movementMethod = LinkMovementMethod.getInstance()
    }
}
