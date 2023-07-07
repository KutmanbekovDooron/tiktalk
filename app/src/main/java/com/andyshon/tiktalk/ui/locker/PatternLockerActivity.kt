package com.andyshon.tiktalk.ui.locker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.andrognito.patternlockview.PatternLockView
import com.andyshon.tiktalk.R
import com.andrognito.rxpatternlockview.events.PatternLockCompoundEvent
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.andrognito.rxpatternlockview.RxPatternLockView
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.model.settings.SettingsModel
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.secretChats.SecretChatsActivity
import kotlinx.android.synthetic.main.activity_pattern_locker.*
import kotlinx.android.synthetic.main.app_toolbar_only_btn_back.*
import javax.inject.Inject

class PatternLockerActivity : BaseInjectActivity() {

    companion object {
        fun startActivity(context: Context, fromSecretChats: Boolean = false) {
            val intent = Intent(context, PatternLockerActivity::class.java)
            intent.putExtra("fromSecretChats", fromSecretChats)
            context.startActivity(intent)
        }
    }

    override fun getPresenter(): BaseContract.Presenter<*>? = null
    @Inject lateinit var prefs: PreferenceManager
    @Inject lateinit var settingsModel: SettingsModel
    private var wantToReset = false
    private var fromSecretChats = false

    private var patternLock = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_pattern_locker)

        wantToReset = intent?.getBooleanExtra("wantToReset", false) ?: false
        fromSecretChats = intent?.getBooleanExtra("fromSecretChats", false) ?: false
        initListeners()
    }

    @SuppressLint("CheckResult")
    private fun initListeners() {
        toolbarOnlyBtnBack.setOnClickListener { finish() }

        pattern_lock_view.isTactileFeedbackEnabled = true

        RxPatternLockView.patternChanges(pattern_lock_view)
            .subscribe { event ->
                when {
                    event.eventType == PatternLockCompoundEvent.EventType.PATTERN_STARTED -> { }
                    event.eventType == PatternLockCompoundEvent.EventType.PATTERN_PROGRESS -> {
                        pattern_lock_view.setViewMode(PatternLockView.PatternViewMode.CORRECT)
                    }
                    event.eventType == PatternLockCompoundEvent.EventType.PATTERN_COMPLETE -> {
                        patternLock = PatternLockUtils.patternToString(pattern_lock_view, event.pattern)
                        if (UserMetadata.lockerValue.isNotEmpty()) {
                            if (patternLock == UserMetadata.lockerValue) {
                                if (wantToReset) {
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                                else {
                                    pattern_lock_view.setViewMode(PatternLockView.PatternViewMode.CORRECT)
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                    SecretChatsActivity.startActivity(this)
                                }
                            } else {
                                pattern_lock_view.setViewMode(PatternLockView.PatternViewMode.WRONG)
                            }
                        }
                        else {
                            SaveLockerAsync(settingsModel, Constants.Locker.PATTERN, patternLock)
                            UserMetadata.lockerValue = patternLock
                            UserMetadata.lockerType = Constants.Locker.PATTERN
                            prefs.putObject(Preference.KEY_USER_LOCKER_VALUE, patternLock, String::class.java)
                            prefs.putObject(Preference.KEY_USER_LOCKER_TYPE, Constants.Locker.PATTERN, String::class.java)
                            pattern_lock_view.setViewMode(PatternLockView.PatternViewMode.CORRECT)
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
                    }
                    event.eventType == PatternLockCompoundEvent.EventType.PATTERN_CLEARED -> { }
                }
            }
    }
}
