package com.andyshon.tiktalk.ui.editProfile.userPageWatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.utils.extensions.loadRoundCornersImage
import kotlinx.android.synthetic.main.activity_user_page_watch.*
import kotlinx.android.synthetic.main.layout_user_page_preferences.*
import timber.log.Timber
import javax.inject.Inject

class UserPageWatchActivity : BaseInjectActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, UserPageWatchActivity::class.java)
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
        setContentView(R.layout.activity_user_page_watch)

        initListeners()
        loadAvatar()
        restorePrefs()
    }

    private fun initListeners() {
        toolbarBtnClose.setOnClickListener {
            finish()
        }
    }

    private fun loadAvatar() {
        UserAvatar.loadRoundCornersImage(
            radius = resources.getDimensionPixelSize(R.dimen.radius_10),
            url = UserMetadata.photos[0].url
        )
    }

    private fun restorePrefs() {
        val work = prefs.getObject(Preference.KEY_USER_WORK, String::class.java) ?: "Manager"
        if (work.isNotEmpty()) {
            tvWork.text = work
        } else {
            tvWork.text = "No answer"
        }
        tvName.text = prefs.getObject(Preference.KEY_USER_NAME, String::class.java) ?: UserMetadata.userName
        val about = prefs.getObject(Preference.KEY_USER_ABOUT, String::class.java) ?: "A beautiful woman feels beautiful within, from the love she gives to her ideas and the creative ways she expresses her soul."
        if (about.isNotEmpty()) {
            tvAbout.text = about
        } else {
            tvAbout.text = "No answer"
        }

        Timber.e("HEIGHT = ${prefs.getObject(Preference.KEY_USER_HEIGHT, String::class.java) ?: "0 cm"}")
        val height = prefs.getObject(Preference.KEY_USER_HEIGHT, String::class.java) ?: "0 cm"
        if (height.isNotEmpty()) {
            view1.setText(height)
        } else {
            view1.setText("144 cm")
        }
        view1.select()
        view2.setText(prefs.getObject(Preference.KEY_USER_LIVING, String::class.java) ?: "SOCIALLY")
        view2.select()
        view3.setText(prefs.getObject(Preference.KEY_USER_CHILDREN, String::class.java) ?: "MEN")
        view3.select()
        view4.setText(prefs.getObject(Preference.KEY_USER_SMOKING, String::class.java) ?: "NEVER")
        view4.select()
        view5.setText(prefs.getObject(Preference.KEY_USER_DRINKING, String::class.java) ?: "SOMETHING CASUAL")
        view5.select()
        view6.setText(prefs.getObject(Preference.KEY_USER_RELATIONSHIP, String::class.java) ?: "SINGLE")
        view6.select()
        view7.setText(prefs.getObject(Preference.KEY_USER_SEXUALITY, String::class.java) ?: "SAGITTARIUS")
        view7.select()
        view8.setText(prefs.getObject(Preference.KEY_USER_CHILDREN, String::class.java) ?: "WANT SOMEDAY")
        view8.select()
    }
}
