package com.andyshon.tiktalk.ui.calls.voice

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.MyCallInvite
import com.andyshon.tiktalk.data.model.auth.AuthModel
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.calls.CallStatus
import com.andyshon.tiktalk.ui.calls.CallsPresenter
import com.andyshon.tiktalk.ui.calls.CallsContract
import com.andyshon.tiktalk.ui.calls.SoundPoolManager
import com.andyshon.tiktalk.utils.extensions.*
import com.tbruyelle.rxpermissions2.RxPermissions
import com.twilio.video.*
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_voice_call.*
import timber.log.Timber
import javax.inject.Inject

class VoiceCallActivity : BaseInjectActivity(), CallsContract.View {

    companion object {
        const val CALL_TO_USER = "CALL_TO_USER"
        const val INCOMING_CALL_INVITE = "INCOMING_CALL_INVITE"
        const val CANCELLED_CALL_INVITE = "CANCELLED_CALL_INVITE"
        const val INCOMING_CALL_NOTIFICATION_ID = "INCOMING_CALL_NOTIFICATION_ID"
        const val ACTION_START_ACTIVITY = "ACTION_START_ACTIVITY"
        const val ACTION_INCOMING_CALL = "ACTION_INCOMING_CALL"
        const val ACTION_CANCEL_CALL = "ACTION_CANCEL_CALL"
        const val ACTION_FCM_TOKEN = "ACTION_FCM_TOKEN"
    }

    @Inject lateinit var presenter: CallsPresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var authModel: AuthModel
    @Inject lateinit var prefs: PreferenceManager

    private var permissionsDisposable: Disposable? = null


    private val audioCodec: AudioCodec by lazy { OpusCodec() }
    private val videoCodec: VideoCodec by lazy { Vp8Codec() }//todo: check H264
    private val encodingParameters: EncodingParameters by lazy {
        EncodingParameters(0, 0)
    }

    private val enableAutomaticSubscription: Boolean
        get() {
//            return sharedPreferences.getBoolean(SettingsActivity.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION, SettingsActivity.PREF_ENABLE_AUTOMATIC_SUBCRIPTION_DEFAULT)
            return true
        }

    private var isMuted = false

    private var chatRoom: Room? = null
    private var localParticipant: LocalParticipant? = null

    private var soundPoolManager: SoundPoolManager? = null

    private var localAudioTrack: LocalAudioTrack? = null

    private val audioManager by lazy {
        this@VoiceCallActivity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var isSpeakerPhoneEnabled = true
    private var disconnectedFromOnDestroy = false

    private var previousAudioMode = 0
    private var previousMicrophoneMute = false

    private var isReceiverRegistered = false
    private lateinit var voiceBroadcastReceiver: VoiceBroadcastReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        supportThemes = false
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_call)


        hideNavigationBar()
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                window.decorView.post {
                    hideNavigationBar()
                }
            } else { /*The navigation bar is NOT visible*/ }
        }


        // These flags ensure that the activity can be launched when the screen is locked.
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )


        /*
         * Setup the broadcast receiver to be notified of FCM Token updates
         * or incoming call invite in this Activity.
         */
        voiceBroadcastReceiver = VoiceBroadcastReceiver()
        registerReceiver()


        soundPoolManager = SoundPoolManager.getInstance(this)


        /*
         * Enable changing the volume using the up/down keys during a conversation
         */
        volumeControlStream = AudioManager.STREAM_VOICE_CALL

        /*
         * Needed for setting/abandoning audio focus during call
         */
        audioManager.isSpeakerphoneOn = true


        setCommonCallListeners()

        RxPermissions(this)
            .request(Manifest.permission.RECORD_AUDIO)
            .subscribe { granted ->
                if (granted) {
                    createAudioAndVideoTracks()
                    handleIntent(intent)
                }
            }
            .addTo(getDestroyDisposable())
    }


    private fun hideNavigationBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE

    }


    override fun onResume() {
        super.onResume()
        registerReceiver()
        /*
         * Update encoding parameters if they have changed.
         */
        localParticipant?.setEncodingParameters(encodingParameters)

        /*
         * Route audio through cached value.
         */
        audioManager.isSpeakerphoneOn = isSpeakerPhoneEnabled

        /*
         * Update reconnecting UI
         */
        chatRoom?.let {
            reconnectingProgressBar.visibility = if (it.state != Room.State.RECONNECTING)
                View.GONE else
                View.VISIBLE
        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.e("onNewIntent, intent = ${intent?.action}")
        handleIntent(intent)
    }


    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }


    override fun onDestroy() {
        disconnectFromChatRoom()
        super.onDestroy()
    }


    private fun registerReceiver() {
        Timber.e("registerReceiver")
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_INCOMING_CALL)
            intentFilter.addAction(ACTION_CANCEL_CALL)
            intentFilter.addAction(ACTION_FCM_TOKEN)
            LocalBroadcastManager.getInstance(this).registerReceiver(
                voiceBroadcastReceiver, intentFilter
            )
            isReceiverRegistered = true
        }
    }


    private fun unregisterReceiver() {
        Timber.e("unregisterReceiver")
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(voiceBroadcastReceiver)
            isReceiverRegistered = false
        }
    }


    private inner class VoiceBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == ACTION_INCOMING_CALL || action == ACTION_CANCEL_CALL) {
                /*
                 * Handle the incoming or cancelled call invite
                 */
                handleIntent(intent)
            }
        }
    }


    private fun handleIntent(intent: Intent?) {
        if (intent != null && intent.action != null) {
            when (intent.action) {
                ACTION_START_ACTIVITY -> {  // when user initiate call
                    presenter.callToUser = intent.getParcelableExtra(CALL_TO_USER)!!
                    presenter.channelSid = presenter.callToUser.channelSid
                    Timber.e("callToUser = ${presenter.callToUser}")

                    setCallingUI()
                    connectToRoom(presenter.callToUser.email, false)
                }
                ACTION_INCOMING_CALL -> {
                    val callInvite = intent.getParcelableExtra<Parcelable>(INCOMING_CALL_INVITE) as MyCallInvite
                    setIncomingCallUI(callInvite)
                }
                ACTION_CANCEL_CALL -> {
//                    val canceledCall = intent.getParcelableExtra<Parcelable>(CANCELLED_CALL_INVITE) as MyCancelledCallInvite
                    soundPoolManager?.stopRinging()
                    chronometer.stop()
                    finish()
                }
                ACTION_FCM_TOKEN -> {
//                    retrieveAccessToken()
                }
            }
        }
    }


    private fun disconnectFromChatRoom() {
        Timber.e("disconnectFromChatRoom")

        soundPoolManager?.release()

        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        chatRoom?.disconnect()
        chatRoom = null
        disconnectedFromOnDestroy = true

        /*
         * Release the local audio and video tracks ensuring any memory allocated to audio
         * or video is freed.
         */
        localAudioTrack?.release()
        localAudioTrack = null
    }


    private fun createAudioAndVideoTracks() {
        // Share your microphone
        localAudioTrack = LocalAudioTrack.create(this, true)
    }



    private fun connectToRoom(callToEmail: String, b: Boolean) {
        Timber.e("connectToRoom")
        fun connect(token: String) {
            configureAudio(true)
            presenter.roomName = if (b) presenter.userEmail.plus("\\\\").plus(callToEmail).plus("\\\\").plus(presenter.channelSid)
            else callToEmail.plus("\\\\").plus(presenter.userEmail).plus("\\\\").plus(presenter.channelSid)
            Timber.e("Connect to room token = $token, room name = ${presenter.roomName}")
            val connectOptionsBuilder = ConnectOptions.Builder(token)
                .roomName(presenter.roomName)

            /*
             * Add local audio track to connect options to share with participants.
             */
            localAudioTrack?.let { connectOptionsBuilder.audioTracks(listOf(it)) }

            /*
            * Set the preferred audio and video codec for media.
            */
            connectOptionsBuilder.preferAudioCodecs(listOf(audioCodec))
            connectOptionsBuilder.preferVideoCodecs(listOf(videoCodec))

            /*
             * Set the sender side encoding parameters.
             */
            connectOptionsBuilder.encodingParameters(encodingParameters)

            /*
             * Toggles automatic track subscription. If set to false, the LocalParticipant will receive
             * notifications of track publish events, but will not automatically subscribe to them. If
             * set to true, the LocalParticipant will automatically subscribe to tracks as they are
             * published. If unset, the default is true. Note: This feature is only available for Group
             * Rooms. Toggling the flag in a P2P room does not modify subscription behavior.
             */
            connectOptionsBuilder.enableAutomaticSubscription(enableAutomaticSubscription)

            val room = Video.connect(this, connectOptionsBuilder.build(), roomListener)
            chatRoom = room
        }

        permissionsDisposable = RxPermissions(this)
            .request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe {
                if (it) {
                    authModel.getTwilioVideoToken()
                        .subscribe({
                            connect(it.token)
//                            getUserLocation()
                        }, {
                            Timber.e("Errror = ${it.message}")
                        })
                        .addTo(getDestroyDisposable())
                } else {
                    Toast.makeText(this, "Location permissions needed", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }


    private fun muteClickListener(): View.OnClickListener {
        return View.OnClickListener {
            /*
             * Enable/disable the local audio track. The results of this operation are
             * signaled to other Participants in the same Room. When an audio track is
             * disabled, the audio is muted.
             */
            localAudioTrack?.let {
                val enable = it.isEnabled.not()
                isMuted = enable
                it.enable(enable)
                if (enable)
                    btnMute.setImageResource(R.drawable.ic_mic_white_24dp)
                else
                    btnMute.setImageResource(R.drawable.ic_mic_white_off_24dp)
            }
        }
    }


    private fun soundClickListener(): View.OnClickListener {
        return View.OnClickListener {
            /*
             * Enable/disable the local audio track. The results of this operation are
             * signaled to other Participants in the same Room. When an audio track is
             * disabled, the audio is muted.
             */
            soundPoolManager?.stopRinging()
            localAudioTrack?.let {
                val enable = it.isEnabled.not()
                isMuted = enable
                it.enable(enable)
                if (enable)
                    btnSoundOff.setImageResource(R.drawable.ic_sound_on)
                else
                    btnSoundOff.setImageResource(R.drawable.ic_sound_off)
            }
        }
    }



    //region UI states

    private fun setCommonCallListeners() {
        btnMute.setOnClickListener(muteClickListener())
        btnSoundOff.setOnClickListener(soundClickListener())
        btnSendSms.setOnClickListener {}
    }


    /*
     * The UI state when there is an active call
     */
    private fun setCallUI() {
        soundPoolManager?.stopRinging()
        tvCallStatus.hide()
        chronometer.show()
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
        btnEndCall.show()
        tvSwipeToAnswer.hide()
        btnAcceptCall.hide()
        btnDeclineCall.hide()
        btnSendSms.hide()

        btnEndCall.setOnClickListener {
            chronometer.stop()
            soundPoolManager?.stopRinging()
            soundPoolManager?.playDisconnect()

//            callModel.declineCall(presenter.roomName, presenter.userEmail).subscribe {  }
//            chatRoom?.disconnect()

            if (presenter.participantIdentity?:"" == presenter.userEmail) {
                disconnectFromChatRoom()
//                finish()

//                presenter.sendStatusCallMessage(CallStatus.ENDED, chronometer.base)
                presenter.declineCall()
            }
            else {
                finish()

//                presenter.sendStatusCallMessage(CallStatus.ENDED, chronometer.base)
            }
        }
    }


    /*
     * The UI state when there is an calling
     */
    private fun setCallingUI() {
        soundPoolManager?.playRinging()
        tvCallStatus.text = this string R.string.call_calling
//        callerAvatar.loadImage(presenter.callToUser.photo)
        callerAvatar.loadRoundCornersImageWithFallback(
            radius = resources.getDimensionPixelSize(R.dimen.radius_10),
            url = presenter.callToUser.photo
        )
        tvName.text = presenter.callToUser.name
        btnEndCall.show()
        tvSwipeToAnswer.hide()
        btnAcceptCall.hide()
        btnDeclineCall.hide()
        btnSendSms.hide()

        btnEndCall.setOnClickListener {
            soundPoolManager?.stopRinging()
            soundPoolManager?.playDisconnect()
            btnEndCall.postDelayed({
                finish()
            }, 350)

            presenter.declineCall()
            presenter.sendStatusCallMessage(CallStatus.MISSED, chronometer.base)
        }
    }


    /*
     * The UI state when there is an incoming call
     */
    private fun setIncomingCallUI(callInvite: MyCallInvite) {
        soundPoolManager?.playRinging()
        presenter.roomName = presenter.userEmail.plus("\\\\").plus(callInvite.callerEmail).plus("\\\\").plus(presenter.channelSid)
        btnAcceptCall.setOnClickListener {
            connectToRoom(callInvite.callerEmail, true)
            setCallUI()
        }
        btnDeclineCall.setOnClickListener {
            soundPoolManager?.stopRinging()
            soundPoolManager?.playDisconnect()

            Timber.e("setIncomingCallUI, roomName = ${presenter.roomName}")
//            disconnectFromChatRoom()

            btnDeclineCall.postDelayed({
                finish()
            }, 350)

            presenter.declineCall()
            presenter.sendStatusCallMessage(CallStatus.CANCELED, chronometer.base)
        }

        btnEndCall.hide()
        tvSwipeToAnswer.show()
        btnAcceptCall.show()
        btnDeclineCall.show()
        btnSendSms.show()
        tvCallStatus.text = this string R.string.call_incoming
        callerAvatar.loadImage(callInvite.callerAvatar)
        tvName.text = callInvite.callerName
    }

    // endregion



    /*
     * Called when participant joins the room
     */
    private fun addRemoteParticipant(remoteParticipant: RemoteParticipant) {
        Timber.e("addRemoteParticipant ${remoteParticipant.identity}")
        presenter.participantIdentity = remoteParticipant.identity

        configureAudio(true)
        setCallUI()

        /*
         * Start listening for participant events
         */
        remoteParticipant.setListener(participantListener)
    }


    /*
     * Called when participant leaves the room
     */
    private fun removeRemoteParticipant(remoteParticipant: RemoteParticipant) {
        if (remoteParticipant.identity != presenter.participantIdentity) {
            return
        }

//        callModel.declineCall(presenter.roomName, presenter.userEmail).subscribe {  }
        Timber.e("removeRemoteParticipant = ${remoteParticipant.identity}, ${presenter.participantIdentity}")
        soundPoolManager?.stopRinging()
        chronometer.stop()
        finish()

        presenter.sendStatusCallMessage(CallStatus.ENDED, chronometer.base)
    }


    private fun configureAudio(enable: Boolean) {
        with(audioManager) {
            if (enable) {
                previousAudioMode = audioManager.mode
                // Request audio focus before making any device switch
                requestAudioFocus()
                /*
                 * Use MODE_IN_COMMUNICATION as the default audio mode. It is required
                 * to be in this mode when playout and/or recording starts for the best
                 * possible VoIP performance. Some devices have difficulties with
                 * speaker mode if this is not set.
                 */
                mode = AudioManager.MODE_IN_COMMUNICATION
                /*
                 * Always disable microphone mute during a WebRTC call.
                 */
                previousMicrophoneMute = isMicrophoneMute
                isMicrophoneMute = false
            } else {
                mode = previousAudioMode
                abandonAudioFocus(null)
                isMicrophoneMute = previousMicrophoneMute
            }
        }
    }

    /*private fun setAudioFocus(setFocus: Boolean) {
        if (audioManager != null) {
            if (setFocus) {
                savedAudioMode = audioManager.getMode()
                // Request audio focus before making any device switch.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val playbackAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(playbackAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener { }
                        .build()
                    audioManager.requestAudioFocus(focusRequest)
                } else {
                    val focusRequestResult = audioManager.requestAudioFocus(
                        AudioManager.OnAudioFocusChangeListener { }, AudioManager.STREAM_VOICE_CALL,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                    )
                }
                *//*
                 * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
                 * required to be in this mode when playout and/or recording starts for
                 * best possible VoIP performance. Some devices have difficulties with speaker mode
                 * if this is not set.
                 *//*
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
            } else {
                audioManager.setMode(savedAudioMode)
                audioManager.abandonAudioFocus(null)
            }
        }
    }*/


    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { }
                .build()
            audioManager.requestAudioFocus(focusRequest)
        } else {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }


    /*
     * RemoteParticipant events listener
     */
    @SuppressLint("BinaryOperationInTimber")
    private val participantListener = object : RemoteParticipant.Listener {
        override fun onAudioTrackPublished(remoteParticipant: RemoteParticipant,
                                           remoteAudioTrackPublication: RemoteAudioTrackPublication) {
            Timber.e("onAudioTrackPublished: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteAudioTrackPublication: sid=${remoteAudioTrackPublication.trackSid}, " +
                    "enabled=${remoteAudioTrackPublication.isTrackEnabled}, " +
                    "subscribed=${remoteAudioTrackPublication.isTrackSubscribed}, " +
                    "name=${remoteAudioTrackPublication.trackName}]")
        }

        override fun onAudioTrackUnpublished(remoteParticipant: RemoteParticipant,
                                             remoteAudioTrackPublication: RemoteAudioTrackPublication) {
            Timber.e("onAudioTrackUnpublished: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteAudioTrackPublication: sid=${remoteAudioTrackPublication.trackSid}, " +
                    "enabled=${remoteAudioTrackPublication.isTrackEnabled}, " +
                    "subscribed=${remoteAudioTrackPublication.isTrackSubscribed}, " +
                    "name=${remoteAudioTrackPublication.trackName}]")
        }

        override fun onDataTrackPublished(remoteParticipant: RemoteParticipant,
                                          remoteDataTrackPublication: RemoteDataTrackPublication) {
            Timber.e("onDataTrackPublished: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteDataTrackPublication: sid=${remoteDataTrackPublication.trackSid}, " +
                    "enabled=${remoteDataTrackPublication.isTrackEnabled}, " +
                    "subscribed=${remoteDataTrackPublication.isTrackSubscribed}, " +
                    "name=${remoteDataTrackPublication.trackName}]")
        }

        override fun onDataTrackUnpublished(remoteParticipant: RemoteParticipant,
                                            remoteDataTrackPublication: RemoteDataTrackPublication) {
            Timber.e("onDataTrackUnpublished: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteDataTrackPublication: sid=${remoteDataTrackPublication.trackSid}, " +
                    "enabled=${remoteDataTrackPublication.isTrackEnabled}, " +
                    "subscribed=${remoteDataTrackPublication.isTrackSubscribed}, " +
                    "name=${remoteDataTrackPublication.trackName}]")
        }

        override fun onVideoTrackPublished(remoteParticipant: RemoteParticipant,
                                           remoteVideoTrackPublication: RemoteVideoTrackPublication) {
        }

        override fun onVideoTrackUnpublished(remoteParticipant: RemoteParticipant,
                                             remoteVideoTrackPublication: RemoteVideoTrackPublication) {
        }

        override fun onAudioTrackSubscribed(remoteParticipant: RemoteParticipant,
                                            remoteAudioTrackPublication: RemoteAudioTrackPublication,
                                            remoteAudioTrack: RemoteAudioTrack) {
            Timber.e("onAudioTrackSubscribed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteAudioTrack: enabled=${remoteAudioTrack.isEnabled}, " +
                    "playbackEnabled=${remoteAudioTrack.isPlaybackEnabled}, " +
                    "name=${remoteAudioTrack.name}]")
        }

        override fun onAudioTrackUnsubscribed(remoteParticipant: RemoteParticipant,
                                              remoteAudioTrackPublication: RemoteAudioTrackPublication,
                                              remoteAudioTrack: RemoteAudioTrack) {
            Timber.e("onAudioTrackUnsubscribed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteAudioTrack: enabled=${remoteAudioTrack.isEnabled}, " +
                    "playbackEnabled=${remoteAudioTrack.isPlaybackEnabled}, " +
                    "name=${remoteAudioTrack.name}]")
        }

        override fun onAudioTrackSubscriptionFailed(remoteParticipant: RemoteParticipant,
                                                    remoteAudioTrackPublication: RemoteAudioTrackPublication,
                                                    twilioException: TwilioException) {
            Timber.e("onAudioTrackSubscriptionFailed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteAudioTrackPublication: sid=${remoteAudioTrackPublication.trackSid}, " +
                    "name=${remoteAudioTrackPublication.trackName}]" +
                    "[TwilioException: code=${twilioException.code}, " +
                    "message=${twilioException.message}]")
        }

        override fun onDataTrackSubscribed(remoteParticipant: RemoteParticipant,
                                           remoteDataTrackPublication: RemoteDataTrackPublication,
                                           remoteDataTrack: RemoteDataTrack) {
            Timber.e("onDataTrackSubscribed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteDataTrack: enabled=${remoteDataTrack.isEnabled}, " +
                    "name=${remoteDataTrack.name}]")
        }

        override fun onDataTrackUnsubscribed(remoteParticipant: RemoteParticipant,
                                             remoteDataTrackPublication: RemoteDataTrackPublication,
                                             remoteDataTrack: RemoteDataTrack) {
            Timber.e("onDataTrackUnsubscribed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteDataTrack: enabled=${remoteDataTrack.isEnabled}, " +
                    "name=${remoteDataTrack.name}]")
        }

        override fun onDataTrackSubscriptionFailed(remoteParticipant: RemoteParticipant,
                                                   remoteDataTrackPublication: RemoteDataTrackPublication,
                                                   twilioException: TwilioException) {
            Timber.e("onDataTrackSubscriptionFailed: " +
                    "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                    "[RemoteDataTrackPublication: sid=${remoteDataTrackPublication.trackSid}, " +
                    "name=${remoteDataTrackPublication.trackName}]" +
                    "[TwilioException: code=${twilioException.code}, " +
                    "message=${twilioException.message}]")
        }

        override fun onVideoTrackSubscribed(remoteParticipant: RemoteParticipant,
                                            remoteVideoTrackPublication: RemoteVideoTrackPublication,
                                            remoteVideoTrack: RemoteVideoTrack) {
        }

        override fun onVideoTrackUnsubscribed(remoteParticipant: RemoteParticipant,
                                              remoteVideoTrackPublication: RemoteVideoTrackPublication,
                                              remoteVideoTrack: RemoteVideoTrack) {
        }

        override fun onVideoTrackSubscriptionFailed(remoteParticipant: RemoteParticipant,
                                                    remoteVideoTrackPublication: RemoteVideoTrackPublication,
                                                    twilioException: TwilioException) {
        }

        override fun onAudioTrackEnabled(remoteParticipant: RemoteParticipant,
                                         remoteAudioTrackPublication: RemoteAudioTrackPublication) {
            Timber.e("onAudioTrackEnabled, remoteParticipant = ${remoteParticipant.isConnected}")
        }

        override fun onVideoTrackEnabled(remoteParticipant: RemoteParticipant,
                                         remoteVideoTrackPublication: RemoteVideoTrackPublication) {
        }

        override fun onVideoTrackDisabled(remoteParticipant: RemoteParticipant,
                                          remoteVideoTrackPublication: RemoteVideoTrackPublication) {
        }

        override fun onAudioTrackDisabled(remoteParticipant: RemoteParticipant,
                                          remoteAudioTrackPublication: RemoteAudioTrackPublication) {
            Timber.e("onAudioTrackDisabled, remoteParticipant = ${remoteParticipant.isConnected}")
        }
    }




    /*
    * Room events listener
    */
    private val roomListener = object : Room.Listener {
        override fun onConnected(room: Room) {
            Timber.e("roomListener, onConnected, room = ${room.name}, ${room.state}, remoteParticipants = ${room.remoteParticipants.size}, remoteParticipants = ${room.localParticipant?.identity}}")
            reconnectingProgressBar.hide()
            soundPoolManager?.stopRinging()
            localParticipant = room.localParticipant

            // Only one participant is supported
            room.remoteParticipants.firstOrNull()?.let { addRemoteParticipant(it) }
        }

        override fun onReconnected(room: Room) {
            Timber.e("roomListener, onReconnected")

            reconnectingProgressBar.hide()
        }

        override fun onReconnecting(room: Room, twilioException: TwilioException) {
            Timber.e("roomListener, onReconnecting")
            reconnectingProgressBar.show()
        }

        override fun onConnectFailure(room: Room, e: TwilioException) {
            Timber.e("roomListener, onConnectFailure, room = $room, ex = ${e.code}, ${e.explanation}, ${e.message}")
            configureAudio(false)
        }

        override fun onDisconnected(room: Room, e: TwilioException?) {
            Timber.e("roomListener, onDisconnected")
            localParticipant = null
            // Only reinitialize the UI if disconnect was not called from onDestroy()
            if (!disconnectedFromOnDestroy) {
                configureAudio(false)
//                initializeUI()
            }
        }

        override fun onParticipantConnected(room: Room, participant: RemoteParticipant) {
            Timber.e("roomListener, onParticipantConnected")
            addRemoteParticipant(participant)
        }

        override fun onParticipantDisconnected(room: Room, participant: RemoteParticipant) {
            Timber.e("roomListener, onParticipantDisconnected")
            removeRemoteParticipant(participant)
        }

        override fun onRecordingStarted(room: Room) {}

        override fun onRecordingStopped(room: Room) {}
    }
}
