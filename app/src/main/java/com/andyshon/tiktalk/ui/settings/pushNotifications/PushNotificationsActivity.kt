package com.andyshon.tiktalk.ui.settings.pushNotifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.utils.extensions.string
import kotlinx.android.synthetic.main.activity_push_notifications.*
import kotlinx.android.synthetic.main.app_toolbar_title_gradient.*
import javax.inject.Inject

class PushNotificationsActivity : BaseInjectActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, PushNotificationsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getPresenter(): BaseContract.Presenter<*>? = null
    @Inject lateinit var prefs: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_push_notifications)

        initListeners()
        setupUI()
    }

    private fun initListeners() {
        toolbarBtnBack.setOnClickListener {
            //save and move back
            savePrefs()
            finish()
        }
        toolbarTvTitle.text = this string R.string.view_contact_notifications

        btnPauseAll.setOnClickListener {
            switcherPauseAll.isChecked = switcherPauseAll.isChecked.not()
        }
        btnMessages.setOnClickListener {
            switcherMessages.isChecked = switcherMessages.isChecked.not()
        }
        btnNewMatches.setOnClickListener {
            switcherNewMatches.isChecked = switcherNewMatches.isChecked.not()
        }
        btnSomebodyLikeYou.setOnClickListener {
            switcherSomebodyLikeYou.isChecked = switcherSomebodyLikeYou.isChecked.not()
        }
        btnNewMsgInPrivateRoom.setOnClickListener {
            switcherNewMsgInPrivateRoom.isChecked = switcherNewMsgInPrivateRoom.isChecked.not()
        }
        btnSuperLikes.setOnClickListener {
            switcherSuperLikes.isChecked = switcherSuperLikes.isChecked.not()
        }
    }

    private fun setupUI() {
        restorePrefs()
    }

    private fun restorePrefs() {
        switcherPauseAll.isChecked = prefs.getObject(Preference.Notifications.PAUSE_ALL, Boolean::class.java) ?: false
        switcherMessages.isChecked = prefs.getObject(Preference.Notifications.MESSAGES, Boolean::class.java) ?: true
        switcherNewMatches.isChecked = prefs.getObject(Preference.Notifications.NEW_MATCHES, Boolean::class.java) ?: true
        switcherSomebodyLikeYou.isChecked = prefs.getObject(Preference.Notifications.SOMEBODY_LIKE_YOU, Boolean::class.java) ?: true
        switcherNewMsgInPrivateRoom.isChecked = prefs.getObject(Preference.Notifications.NEW_MESSAGES_IN_PRIVATE_ROOM, Boolean::class.java) ?: true
        switcherSuperLikes.isChecked = prefs.getObject(Preference.Notifications.SUPER_LIKE, Boolean::class.java) ?: true
    }

    private fun savePrefs() {
        prefs.putObject(Preference.Notifications.PAUSE_ALL, switcherPauseAll.isChecked, Boolean::class.java)
        prefs.putObject(Preference.Notifications.MESSAGES, switcherMessages.isChecked, Boolean::class.java)
        prefs.putObject(Preference.Notifications.NEW_MATCHES, switcherNewMatches.isChecked, Boolean::class.java)
        prefs.putObject(Preference.Notifications.SOMEBODY_LIKE_YOU, switcherSomebodyLikeYou.isChecked, Boolean::class.java)
        prefs.putObject(Preference.Notifications.NEW_MESSAGES_IN_PRIVATE_ROOM, switcherNewMsgInPrivateRoom.isChecked, Boolean::class.java)
        prefs.putObject(Preference.Notifications.SUPER_LIKE, switcherSuperLikes.isChecked, Boolean::class.java)
    }

    override fun onBackPressed() {
        toolbarBtnBack.performClick()
    }
}
