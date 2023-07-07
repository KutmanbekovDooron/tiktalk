package com.andyshon.tiktalk.ui.locker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.model.settings.SettingsModel
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.secretChats.SecretChatsActivity
import kotlinx.android.synthetic.main.activity_fingerprint_locker.*
import kotlinx.android.synthetic.main.app_toolbar_only_btn_back.*
import javax.inject.Inject

class FingerprintLockerActivity : BaseInjectActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, FingerprintLockerActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getPresenter(): BaseContract.Presenter<*>? = null
    @Inject lateinit var prefs: PreferenceManager
    @Inject lateinit var settingsModel: SettingsModel
    private var lockerValue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_fingerprint_locker)

        lockerValue = intent?.getStringExtra("lockerValue") ?: ""
        initListeners()
    }

    private fun initListeners() {
        toolbarOnlyBtnBack.setOnClickListener {
            finish()
        }
        btnSetFingerprint.setOnClickListener {
            finish()
            SecretChatsActivity.startActivity(this)
        }
    }
}
