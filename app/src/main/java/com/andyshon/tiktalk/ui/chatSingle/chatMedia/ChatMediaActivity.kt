package com.andyshon.tiktalk.ui.chatSingle.chatMedia

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.SurfaceHolder
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.utils.extensions.*
import com.thefuntasty.hauler.DragDirection
import kotlinx.android.synthetic.main.activity_chat_media.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class ChatMediaActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context, photo: String, video: String, messageDuration: String) {
            val intent = Intent(context, ChatMediaActivity::class.java)
            intent.putExtra("photo", photo)
            intent.putExtra("video", video)
            intent.putExtra("messageDuration", messageDuration)
            context.startActivity(intent)
        }
    }

    private var canPlay = false
    private var isRun = false
    private var messageDuration = ""
    private val handler = Handler()

    private var mMediaPlayer: MediaPlayer? = null
    private var mSurfaceHolder: SurfaceHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_chat_media)

        initListeners()

        val videoPath = intent?.getStringExtra("video") ?: ""
        val mediaSid = intent?.getStringExtra("photo") ?: ""
        messageDuration = intent?.getStringExtra("messageDuration") ?: ""

        Timber.e("mediaSid = $mediaSid")
        Timber.e("videoPath = $videoPath")

        if (mediaSid.isNotEmpty()) {
            image.show()
            videoLayout.hide()

            if (mediaSid.contains("amazonaws")) {
                image.loadRoundCornersImage(url = mediaSid)
            } else {
                val file = File(cacheDir, mediaSid)
                image.loadRoundCornersImage(uri = Uri.fromFile(file))
            }
        }
        else {
            videoLayout.show()
            image.hide()

            mSurfaceHolder = surfaceVideo.holder
            mSurfaceHolder?.addCallback(object: SurfaceHolder.Callback {
                override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {}
                override fun surfaceDestroyed(p0: SurfaceHolder?) {}
                override fun surfaceCreated(p0: SurfaceHolder?) {
                    mMediaPlayer = MediaPlayer()
                    mMediaPlayer?.setDisplay(mSurfaceHolder)

                    mMediaPlayer?.setOnCompletionListener {
                        canPlay = false
                        isRun = false
                        btnPlayVideo.show()
//                        tvVideoDuration.text = messageDuration.plus(" / ").plus(messageDuration)
                    }
                    try {
                        mMediaPlayer?.setDataSource(videoPath)
                        mMediaPlayer?.prepareAsync()

                        mMediaPlayer?.setOnPreparedListener {
                            play()
                        }
                        seekBarVideo.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                                Timber.e("onProgressChanged = $progress")

                                if(fromUser){
                                    mMediaPlayer?.seekTo(progress)
                                }

                                tvVideoDuration.text = calcDurationTime(TimeUnit.MILLISECONDS.toSeconds(progress.toLong()).toInt(), messageDuration)
                            }

                            override fun onStartTrackingTouch(p0: SeekBar?) {}
                            override fun onStopTrackingTouch(p0: SeekBar?) {}
                        })

                        mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            })

            surfaceVideo.setOnClickListener {
                canPlay = canPlay.not()
                if (canPlay) {
                    play()
                } else {
                    pause()
                }
            }
        }
    }

    private fun pause() {
        mMediaPlayer?.pause()
        isRun = false
        btnPlayVideo.show()
    }

    private fun play() {
        mMediaPlayer?.start()
        isRun = true
        btnPlayVideo.hide()
        seekBarVideo.show()
        seekBarVideo.max = mMediaPlayer?.duration?:0
        seekBarVideo.progress = 0

        runOnUiThread(object: Runnable {
            override fun run() {
                if(isRun) {
                    val mCurrentPosition = mMediaPlayer?.currentPosition?:0 / 1000
                    seekBarVideo.progress = mCurrentPosition

                    tvVideoDuration.text = calcDurationTime(TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition.toLong()).toInt(), messageDuration)

                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    override fun onDestroy() {
        isRun = false
        super.onDestroy()
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
    }

    private fun initListeners() {
        btnClose.setOnClickListener { finish() }
        haulerView.setOnDragDismissedListener { dragDirection ->
            when (dragDirection) {
                DragDirection.DOWN -> {
                    finish()
                    overridePendingTransition(0, R.anim.fragment_pop_top_to_bottom)
                }
                DragDirection.UP -> {
                    finish()
                    overridePendingTransition(0, R.anim.fragment_pop_bottom_to_top)
                }
            }
        }
    }
}
