package com.andyshon.tiktalk.ui.chatSingle

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import ChatCallbackListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.Media
import com.twilio.chat.*
import org.json.JSONObject
import java.io.FileOutputStream
import java.nio.file.Files.delete



class VoiceMessageHelper(val listener: VoiceMessageListener) {

     private var recorder: MediaRecorder? = null
     var path :String = ""

    init {
        this.path = sanitizePath(path)
        Timber.e("this.path = ${this.path}")
    }

    private fun sanitizePath(path:String):String {
        var path = path
        if (!path.startsWith("/")) {
            path = "/$path"
        }
        path = "/MyMessageVoice_01"
        return Environment.getExternalStorageDirectory().absolutePath + path
    }

    private var name = ""
    private var channelSid = ""

    fun start(name: String, channelSid: String) {
        this.name = name
        this.channelSid = channelSid
        val state = Environment.getExternalStorageState()
        if (state != Environment.MEDIA_MOUNTED) {
            throw IOException("SD Card is not mounted.  It is $state.")
        }

         // make sure the directory we plan to store the recording in exists
        val directory = File(path).parentFile
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Path to file could not be created.")
        }

        Timber.e("recorder = $recorder")
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder?.setOutputFile(path)
        recorder?.prepare()
        recorder?.start()


        /*val*/ mainHandler = Handler(Looper.getMainLooper())
        var time = 0f

        mainHandler?.post(object : Runnable {
            override fun run() {
                time += 0.10f
                Timber.e("Time = $time")
                listener.updateTime(time)
//                tvRecordingTime.text = time.toString()
                mainHandler?.postDelayed(this, 10)
            }
        })
    }

    private var mainHandler: Handler? = null

    fun stopRecord() {
        mainHandler = null

        try {
            recorder?.stop()
        } catch (e: RuntimeException) {
            Timber.e("must delete the outputfile when the recorder stop failed")

            File(path).delete()  //you must delete the outputfile when the recorder stop failed.
        } finally {
            recorder?.release()
            recorder = null
        }

        Timber.e("Path ====== $path")
    }

    fun send(context: Context) {
        val file2 = File(path)
        Timber.e("Voice message file2 = $file2, media name = $name")
        val fileInputStream = FileInputStream(file2)

        val media = Media(name, Constants.Chat.Media.TYPE_VOICE, fileInputStream)

        val json = JSONObject()
        json.put("userId", UserMetadata.userId)
        json.put("userName", UserMetadata.userName)
        json.put("userEmail", UserMetadata.userEmail)
        json.put("userPhoto", UserMetadata.photos.first().url)
        json.put("userPhone", UserMetadata.userPhone)

        val options = Message.options()
            .withAttributes(json)
            .withMediaFileName(media.name)
            .withMedia(media.stream, media.type)
            .withMediaProgressListener(object : ProgressListener() {
                override fun onStarted() = Timber.e("Start voice media upload")
                override fun onProgress(bytes: Long) = Timber.e("Voice Media upload progress - bytes done: $bytes")
                override fun onCompleted(mediaSid: String) = Timber.e("Voice Media upload completed")
            })

        TwilioSingleton.instance.chatClient?.channels?.getChannel(channelSid, ChatCallbackListener<Channel> {
            it.messages.sendMessage(options, ChatCallbackListener<Message> {
                Timber.e("Voice Media message sent - sid: ${it.sid}, type: ${it.type}")

                val file = File(context.cacheDir, it.media.sid)
                if (file.exists().not() || file.length() == 0L) {
                    val outStream = FileOutputStream(file)
                    it.media.download(outStream, object: StatusListener() {
                        override fun onSuccess() {
                            Timber.e("download after sent, onSuccess")
                        }
                        override fun onError(errorInfo: ErrorInfo?) {
                            Timber.e("download after sent, onError = $errorInfo")
                        }
                    }, object: ProgressListener() {
                        override fun onStarted() {
                            Timber.e("onStarted after sent")
                        }
                        override fun onProgress(p0: Long) {
                            Timber.e("onProgress after sent, $p0")
                        }
                        override fun onCompleted(p0: String?) {
                            Timber.e("onCompleted after sent = $p0")
//                            view?.notifyItem(pos)
                        }
                    })
                }
            })
        })
    }

    private fun pauseOrResume(pos:Int) {
        mediaPlayer?.let {
            if (it.isPlaying) {
                isRunning = false
                it.pause()
                listener.paused(pos)
                Timber.e("Pause = ${currentDuration.toFloat()}")
                listener.setProgress(pos, currentDuration.toFloat())
            } else {
                isRunning = true
                it.start()
                Timber.e("Resume = ${currentDuration.toFloat()}")
                listener.setProgress(pos, currentDuration.toFloat())
                play2()
                listener.resumed(pos)
            }
        }
    }

    var currentAudio = ""

    var currentPosition = -1
    private var mediaPlayer: MediaPlayer? = null

    var totalDuration = 0

    var currentDuration = 0
    var isRunning = true

    fun playFromPath(path:String, pos:Int) {
        Timber.e("Path = $path")

        if (currentAudio != path) {
            if (currentPosition != -1) {
                listener.paused(currentPosition)
            }
//            mediaPlayer?.stopRecord()
            mediaPlayer?.release()

            val file = File(path)
            if (file.exists()) {
                currentAudio = path
                currentPosition = pos
                listener.newAudioSetted(pos)

                play2()

                mediaPlayer = MediaPlayer()
                mediaPlayer?.setOnCompletionListener {
                    isRunning = false
                    listener.onEnded(pos)
                    currentAudio = ""
                    currentPosition = -1
                }
                try {
                    mediaPlayer?.setDataSource(path)
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                    isRunning = true
                }
                catch (e: IOException) {
                    Timber.e("java.lang.RuntimeException: java.lang.reflect.InvocationTargetException, Caused by: java.io.IOException: Prepare failed.: status=0x1")
                }





                Timber.e("Duration of audio = ${mediaPlayer?.duration}")
                mediaPlayer?.let {
                    listener.setDuration(pos, it.duration)
                    Timber.e("Seconds of audio = ${it.duration}") //9340
                }
            }
            else {
                Timber.e("File doesn't exists $path")
            }
        }
        else {
            pauseOrResume(pos)
        }
    }

    fun stopPlay() {
        isRunning = false
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        catch (il: IllegalStateException) {
            Timber.e("IllegalStateException stopPlay called in an invalid state: 1")
        }
    }

    private fun play2() {
        val seekHandler = Handler()

        val updateSeekBar = object : Runnable {
            override fun run() {
                Timber.e("MP = $mediaPlayer")
                if (isRunning) {
                    /*val*/ totalDuration = mediaPlayer?.duration ?: 0
                    /*val*/ currentDuration = mediaPlayer?.currentPosition ?: 0

                Timber.e("totalDuration = $totalDuration")
                Timber.e("currentDuration = $currentDuration")

                // Call this thread again after 15 milliseconds => ~ 1000/60fps
                    seekHandler.postDelayed(this, /*15*/150)
                }
            }
        }

        // Updating progress bar
        if (isRunning) {
            seekHandler.postDelayed(updateSeekBar, /*15*/150);
        }
    }
}