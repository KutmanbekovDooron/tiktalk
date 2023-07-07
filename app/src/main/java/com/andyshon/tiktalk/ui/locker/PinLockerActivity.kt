package com.andyshon.tiktalk.ui.locker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.andyshon.tiktalk.R
import kotlinx.android.synthetic.main.activity_pin_locker.*
import androidx.core.widget.doAfterTextChanged
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.model.settings.SettingsModel
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.secretChats.SecretChatsActivity
import com.andyshon.tiktalk.utils.extensions.showKeyboard3
import kotlinx.android.synthetic.main.app_toolbar_only_btn_back.*
import timber.log.Timber
import javax.inject.Inject

class PinLockerActivity: BaseInjectActivity() {

    companion object {
        fun startActivity(context: Context, fromSecretChats: Boolean = false, viaVisibility: Boolean = false, channelSid: String = "") {
            val intent = Intent(context, PinLockerActivity::class.java)
            intent.putExtra("fromSecretChats", fromSecretChats)
            intent.putExtra("viaVisibility", viaVisibility)
            intent.putExtra("channelSid", channelSid)
            context.startActivity(intent)
        }
    }

    override fun getPresenter(): BaseContract.Presenter<*>? = null
    @Inject lateinit var prefs: PreferenceManager
    @Inject lateinit var settingsModel: SettingsModel
    private var wantToReset = false
    private var fromSecretChats = false
    private var viaVisibility = false
    private var channelSid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_pin_locker)

        wantToReset = intent?.getBooleanExtra("wantToReset", false) ?: false
        fromSecretChats = intent?.getBooleanExtra("fromSecretChats", false) ?: false
        viaVisibility = intent?.getBooleanExtra("viaVisibility", false) ?: false
        channelSid = intent?.getStringExtra("channelSid") ?: ""
        etMask.showKeyboard3()
        initListeners()
    }

    private fun initListeners() {
        toolbarOnlyBtnBack.setOnClickListener {
            finish()
        }

        layoutPin.setOnTouchListener { view, motionEvent ->
            Handler().postDelayed({
                val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.showSoftInput(etMask, InputMethodManager.SHOW_IMPLICIT)
            }, 20)
        }

        etMask.doAfterTextChanged {
            when(it?.toString()?.trim()?.length) {
                1 -> {
                    pin1.setImageResource(R.drawable.bg_round_purple)
                    pin2.setImageResource(R.drawable.bg_round_purple)
                    pin3.setImageResource(R.drawable.bg_round_purple)
                    pin4.setImageResource(R.drawable.bg_round_purple)
                }
                2 -> {
                    pin1.setImageResource(R.drawable.bg_round_purple_filled)
                    pin2.setImageResource(R.drawable.bg_round_purple)
                    pin3.setImageResource(R.drawable.bg_round_purple)
                    pin4.setImageResource(R.drawable.bg_round_purple)
                }
                3 -> {
                    pin1.setImageResource(R.drawable.bg_round_purple_filled)
                    pin2.setImageResource(R.drawable.bg_round_purple_filled)
                    pin3.setImageResource(R.drawable.bg_round_purple)
                    pin4.setImageResource(R.drawable.bg_round_purple)
                }
                4 -> {
                    pin1.setImageResource(R.drawable.bg_round_purple_filled)
                    pin2.setImageResource(R.drawable.bg_round_purple_filled)
                    pin3.setImageResource(R.drawable.bg_round_purple_filled)
                    pin4.setImageResource(R.drawable.bg_round_purple)
                }
                5 -> {
                    pin1.setImageResource(R.drawable.bg_round_purple_filled)
                    pin2.setImageResource(R.drawable.bg_round_purple_filled)
                    pin3.setImageResource(R.drawable.bg_round_purple_filled)
                    pin4.setImageResource(R.drawable.bg_round_purple_filled)

                    val pin = it.toString().substring(0,it.length-1).trim()
                    Handler().postDelayed({
                        if (UserMetadata.lockerValue.isNotEmpty()) {
                            if (pin == UserMetadata.lockerValue) {
                                when {
                                    wantToReset -> {
                                        setResult(Activity.RESULT_OK)
                                        finish()
                                    }
                                    viaVisibility -> {
                                        TwilioSingleton.instance.updateChannelSecret(channelSid) {
                                            setResult(Activity.RESULT_OK)
                                            finish()
                                        }
                                    }
                                    else -> {
                                        prefs.putObject(Preference.KEY_USER_LOCKER_VALUE, pin, String::class.java)
                                        prefs.putObject(Preference.KEY_USER_LOCKER_TYPE, Constants.Locker.PIN, String::class.java)
                                        setResult(Activity.RESULT_OK)
                                        finish()
                                        SecretChatsActivity.startActivity(this)
                                    }
                                }
                            } else {
                                pin1.setImageResource(R.drawable.bg_round_red_filled)
                                pin2.setImageResource(R.drawable.bg_round_red_filled)
                                pin3.setImageResource(R.drawable.bg_round_red_filled)
                                pin4.setImageResource(R.drawable.bg_round_red_filled)
                            }
                        }
                        else {
                            SaveLockerAsync(settingsModel, Constants.Locker.PIN, pin)
                            UserMetadata.lockerValue = pin
                            UserMetadata.lockerType = Constants.Locker.PIN
                            prefs.putObject(Preference.KEY_USER_LOCKER_VALUE, pin, String::class.java)
                            prefs.putObject(Preference.KEY_USER_LOCKER_TYPE, Constants.Locker.PIN, String::class.java)
                            Timber.e("wantToReset = $wantToReset, fromSecretChats = $fromSecretChats")
                            if (fromSecretChats) {
                                val intent = Intent().apply { putExtra("notOpen", true) }
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
                            else {
                                finish()
                                SecretChatsActivity.startActivity(this)
                            }
                        }
                    }, 150)
                }
            }
        }
    }
}
