package com.andyshon.tiktalk.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.ui.auth.signIn.SignInActivity
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.dialogs.ShareAppDialog
import com.andyshon.tiktalk.ui.editProfile.EditProfileActivity
import com.andyshon.tiktalk.ui.settings.pushNotifications.PushNotificationsActivity
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.app_toolbar_title_gradient.*
import org.jetbrains.anko.longToast
import javax.inject.Inject
import android.widget.Toast
import android.content.pm.PackageManager
import com.andyshon.tiktalk.ui.payments.PaymentSettingsActivity
import com.andyshon.tiktalk.utils.extensions.loadRoundCornersImage

class SettingsActivity : BaseInjectActivity(), SettingsContract.View, ShareAppDialog.ShareAppClickListener {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var presenter: SettingsPresenter

    private var shareAppDialog = ShareAppDialog.newInstance()
    private lateinit var sharedPref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_settings)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        Handler().postDelayed({
            initUI()
            initListeners()
            presenter.initPrefs()
        }, 100)
    }

    override fun onResume() {
        super.onResume()
        initUI()
    }

    private fun initUI() {
        avatar.loadRoundCornersImage(
            radius = getActivityContext().resources.getDimensionPixelSize(R.dimen.radius_100),
            url = UserMetadata.photos.first().url
        )
    }

    override fun fillPrefs() {
        switcherMale.isChecked = presenter.lookingForMale
        switcherFemale.isChecked = presenter.lookingForFemale
        if (presenter.minAge > 0) {
            seekBarAge.setProgress(presenter.minAge.toFloat(), presenter.maxAge.toFloat())
        }
        if (presenter.distance > 0) {
            seekBarLocation.setProgress(presenter.distance.toFloat())
        }
//        seekBarAge.setProgress(18F, 60F)
//        seekBarLocation.setProgress(10F)
        switcherShowMeInTikTalk.isChecked = presenter.showInApp
        switcherShowMeInPlaces.isChecked = presenter.showInPlaces
    }

    override fun updated() {
//        if (connectivityManager.activeNetwork)
        finish()
    }

    override fun logout() {
        val intent = Intent(this@SettingsActivity, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun deleteAccount() {
        val intent = Intent(this@SettingsActivity, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun editProfile(v: View) {
        EditProfileActivity.startActivity(this)
    }

    private fun initListeners() {
        toolbarBtnBack.setOnClickListener {
            if (canOpen())
                presenter.updateSettings()
        }
        btnResetDislikes.setOnClickListener {
            presenter.resetDislikes()
        }
        btnShareApp.setOnClickListener {
            shareAppDialog.show(supportFragmentManager)
        }
        btnPushNotifications.setOnClickListener {
            presenter.changeNotifications = true
            PushNotificationsActivity.startActivity(this)
        }
        btnMale.setOnClickListener {
            switcherMale.isChecked = switcherMale.isChecked.not()
            presenter.lookingForMale = switcherMale.isChecked
        }
        btnFemale.setOnClickListener {
            switcherFemale.isChecked = switcherFemale.isChecked.not()
            presenter.lookingForFemale = switcherFemale.isChecked
        }
        btnShowMeInApp.setOnClickListener {
            switcherShowMeInTikTalk.isChecked = switcherShowMeInTikTalk.isChecked.not()
            presenter.showInApp = switcherShowMeInTikTalk.isChecked
        }
        btnShowMeInPlaces.setOnClickListener {
            switcherShowMeInPlaces.isChecked = switcherShowMeInPlaces.isChecked.not()
            presenter.showInPlaces = switcherShowMeInPlaces.isChecked
        }
        btnPaymentSettings.setOnClickListener {
            if (canOpen())
                PaymentSettingsActivity.startActivity(this)
        }
        btnLogout.setOnClickListener {
            if (canOpen())
                presenter.logout()
        }
        btnDeleteAccount.setOnClickListener {
            if (canOpen())
                presenter.deleteAccount()
        }

        switcherFemale.setOnCheckedChangeListener { compoundButton, b ->
            presenter.lookingForFemale = b
        }
        switcherMale.setOnCheckedChangeListener { compoundButton, b ->
            presenter.lookingForMale = b
        }
        switcherShowMeInTikTalk.setOnCheckedChangeListener { compoundButton, b ->
            presenter.showInApp = b
        }
        switcherShowMeInPlaces.setOnCheckedChangeListener { compoundButton, b ->
            presenter.showInPlaces = b
        }

        val curTheme = sharedPref.getString(Preference.KEY_THEME, "default")
        switcherNightThemeMode.isChecked = curTheme == "dark"

        btnChangeTheme.setOnClickListener {
            switcherNightThemeMode.isChecked = switcherNightThemeMode.isChecked.not()
            changeTheme(curTheme)
        }
        switcherNightThemeMode.setOnClickListener {
            changeTheme(curTheme)
        }

        seekBarLocation.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                presenter.distance = leftValue.toInt()
//                normalizeMinMax()
                tvLocation.text = presenter.distance.toString().plus("km")
//                if (minDistance > maxDistance) {
//                    seekBarMaxDistance.setProgress(minDistance + 10)
//                }
            }
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
        })

        seekBarAge.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                presenter.minAge = leftValue.toInt()
                presenter.maxAge = rightValue.toInt()
                tvAges.text = presenter.minAge.toString().plus("-").plus(presenter.maxAge)
//                presenter.distance = leftValue.toInt()
//                normalizeMinMax()
//                tvLocation.text = presenter.distance.toString().plus("km")
//                if (minDistance > maxDistance) {
//                    seekBarMaxDistance.setProgress(minDistance + 10)
//                }
            }
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
        })
    }

    private fun changeTheme(curTheme: String) {
        val bb = switcherNightThemeMode.isChecked
        if (bb) {
            if (curTheme != "dark") {
                sharedPref.edit().putString(Preference.KEY_THEME, "dark").apply()
                setTheme(R.style.AppTheme_Dark)
                recreate()
            }
        }
        else {
            if (curTheme != "default") {
                sharedPref.edit().putString(Preference.KEY_THEME, "default").apply()
                setTheme(R.style.AppTheme_Default)
                recreate()
            }
        }
    }

    override fun dislikesResetted() {
        longToast("Your dislikes were resetted.")
    }

    private fun shareChooser() {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL")
        i.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.skyline.tiktalk")
        startActivity(Intent.createChooser(i, "Share Tik Talk"))
    }

    override fun shareWhatsApp() {
        shareViaSocialMedia("com.whatsapp", "Whatsapp not Installed")
    }

    override fun shareTelegram() {
        shareViaSocialMedia("org.telegram.messenger", "Telegram not Installed")
    }

    override fun shareSkype() {
        shareViaSocialMedia("com.skype.raider", "Skype not Installed")
    }

    override fun shareMail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
//        emailIntent.data = Uri.parse("mailto:")
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, "mystatcontrol@gmail.com")
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Tik Talk")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.doneit.emiltonia&hl=ru")
        startActivity(Intent.createChooser(emailIntent, "Share Tik Talk with"))
    }

    override fun shareMessenger() {
        shareViaSocialMedia("com.facebook.orca", "Messenger not Installed")
    }

    override fun shareSMS() {
        val smsIntent = Intent(Intent.ACTION_VIEW)
        smsIntent.type = "vnd.android-dir/mms-sms"
        smsIntent.putExtra("sms_body", "https://play.google.com/store/apps/details?id=com.doneit.emiltonia&hl=ru")
        try {
            startActivity(smsIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No sms handler found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareViaSocialMedia(appName: String, errorMsg: String) {
        val isAppInstalled = isAppAvailable(this, appName)
        if (isAppInstalled) {
            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.type = "text/plain"
            myIntent.setPackage(appName)
            myIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.doneit.emiltonia&hl=ru")
            startActivity(Intent.createChooser(myIntent, "Share Tik Talk with"))
        } else {
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAppAvailable(context: Context, appName: String): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(appName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

    }

    override fun onBackPressed() {
        toolbarBtnBack.performClick()
    }
}
