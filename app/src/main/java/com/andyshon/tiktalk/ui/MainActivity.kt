package com.andyshon.tiktalk.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.data.entity.PlacesResult
import com.andyshon.tiktalk.data.entity.User
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.events.OnBackPressedEvent
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.chatSingle.ChatSingleActivity
import com.andyshon.tiktalk.ui.locker.FingerprintLockerActivity
import com.andyshon.tiktalk.ui.locker.PatternLockerActivity
import com.andyshon.tiktalk.ui.messages.mainMessages.MessagesListener
import com.andyshon.tiktalk.ui.matches.MatchesListener
import com.andyshon.tiktalk.ui.matches.itsMatch.ItIsMatchActivity
import com.andyshon.tiktalk.ui.secretChats.SecretChatsActivity
import com.andyshon.tiktalk.ui.selectContact.SelectContactActivity
import com.andyshon.tiktalk.ui.zoneSingle.ZoneSingleActivity
import com.andyshon.tiktalk.ui.zones.ZoneListListener
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.longToast
import timber.log.Timber
import javax.inject.Inject
import com.andyshon.tiktalk.ui.locker.PinLockerActivity
import com.andyshon.tiktalk.ui.matches.chat.MatchesChatListener
import com.andyshon.tiktalk.ui.matches.share.MatchesShareActivity
import com.andyshon.tiktalk.ui.settings.SettingsActivity
import com.twilio.chat.Channel
import org.jetbrains.anko.startActivity
import android.view.LayoutInflater
import android.widget.TextView
import com.andyshon.tiktalk.data.entity.ChannelUserData
import com.andyshon.tiktalk.utils.extensions.hide
import com.andyshon.tiktalk.utils.extensions.show
import com.google.android.material.bottomnavigation.BottomNavigationItemView

private const val RC_SELECT_CONTACT = 101
private const val RC_IT_IS_MATCH = 102

class MainActivity : BaseInjectActivity(), MainContract.View, MessagesListener, ZoneListListener, MatchesListener, MatchesChatListener {

    @Inject lateinit var rxEventBus: RxEventBus
    private lateinit var navController: NavController
    @Inject lateinit var prefs: PreferenceManager

    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var presenter: MainPresenter

    private lateinit var tvNotificationBadge: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

//        presenter.setTwilioListener()

        Timber.e("KEY_USER_NAME = ${prefs.getObject(Preference.KEY_USER_NAME, String::class.java)}")
        Timber.e("KEY_USER_MAIN_PHOTO = ${prefs.getObject(Preference.KEY_USER_MAIN_PHOTO, String::class.java)}")

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(navigationView, navController)

        presenter.observe()
        initNotificationBadge()

        Timber.e("UserMetadata.userId = ${UserMetadata.userId}, ${UserMetadata.userName}, ${UserMetadata.userEmail}")
    }

    private fun initNotificationBadge() {
        val notificationsTab = navigationView.findViewById<BottomNavigationItemView>(R.id.messagesFragment)
        val badge = LayoutInflater.from(this).inflate(R.layout.tabbar_badge, notificationsTab, false)
        tvNotificationBadge = badge.findViewById(R.id.notificationsBadgeTextView)
        tvNotificationBadge.hide()
        notificationsTab.addView(badge)
    }

    override fun updateNotificationBadge(count: Int) {
        tvNotificationBadge.text = count.toString()
        if (count > 0)
            tvNotificationBadge.show()
        else
            tvNotificationBadge.hide()
    }

    override fun closeMatchesChat() {
        onBackPressed()
    }

    override fun openMatchChat(chatUser: ChannelModel) {
//        openSingleChat(chatUser, isFromMatches = true)
    }

    override fun openSelectContact() {
        RxPermissions(this)
            .request(Manifest.permission.READ_CONTACTS)
            .subscribe ({ granted ->
                if (granted) {
                    if (canOpen()) {
                        val intent = Intent(this@MainActivity, SelectContactActivity::class.java)
                        startActivityForResult(intent, RC_SELECT_CONTACT)
                    }
                }
                else {
                    longToast("You should give access to your contacts")
                }
            }, {
                Timber.e("onError = ${it.message}")
            })
            .addTo(getDestroyDisposable())
    }

    override fun openMatchesChat() {
        navController.navigate(R.id.matchesChatFragment)
    }

    override fun openSingleChat(channel: Channel, item: ChannelModel, isFromMatches: Boolean) {
        val userName = if (TwilioSingleton.instance.myIdentity() == item.userData?.userEmail1) item.userData?.userName2
        else item.userData?.userName1
        val userPhoto = if (TwilioSingleton.instance.myIdentity() == item.userData?.userEmail1) item.userData?.userPhoto2
        else item.userData?.userPhoto1
        val userPhone = if (TwilioSingleton.instance.myIdentity() == item.userData?.userEmail1) item.userData?.userPhone2
        else item.userData?.userPhone1

        if (canOpen()) {
            //send channel sid to update badge counter
            presenter.reCountBadgeCounter(channel.sid)
            startActivity<ChatSingleActivity>(
                Constants.EXTRA_CHANNEL to channel,
                Constants.EXTRA_CHANNEL_SID to channel.sid,
                Constants.EXTRA_CHANNEL_OPPONENT_NAME to userName,
                Constants.EXTRA_CHANNEL_OPPONENT_PHOTO to userPhoto,
                Constants.EXTRA_CHANNEL_OPPONENT_PHONE to userPhone
            )
        }
    }

    override fun openSecretChats() {
        if (canOpen())
            SecretChatsActivity.startActivity(this)
    }

    override fun openSingleZone(zone: PlacesResult) {
        if (canOpen())
            ZoneSingleActivity.startActivity(this, zone)
    }

    override fun openItIsMatch(user: User) {
        if (canOpen()) {
            val intent = Intent(this, ItIsMatchActivity::class.java).apply {
                putExtra("id", user.id)
                putExtra("photo", user.images.first().url)
            }
            startActivityForResult(intent, RC_IT_IS_MATCH)
        }
    }

    override fun openShare(photo: String) {
        if (canOpen())
            MatchesShareActivity.startActivity(this, photo)
    }

    override fun openPattern() {
        if (canOpen())
            PatternLockerActivity.startActivity(this)
    }

    override fun openPIN() {
        if (canOpen())
            PinLockerActivity.startActivity(this)
    }

    override fun openFingerprint() {
        if (canOpen())
            FingerprintLockerActivity.startActivity(this)
    }

    override fun openSettings() {
        if (canOpen())
            SettingsActivity.startActivity(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                RC_SELECT_CONTACT -> {  // create channel with conversationStarted = false
                    data?.let {
                        showProgress()
                        val id = it.getIntExtra("id", 0)
                        val name = it.getStringExtra("name")!!
                        val email = it.getStringExtra("email")!!
                        val photo = it.getStringExtra("photo")!!
                        val phone = it.getStringExtra("phone")!!
                        longToast(name.plus(", ").plus(phone))

                        TwilioSingleton.instance.createChannel(
                            false,
                            UserMetadata.userId,
                            UserMetadata.userName,
                            UserMetadata.userEmail,
                            UserMetadata.photos.first().url,
                            UserMetadata.userPhone,
                            id,
                            name,
                            email,
                            photo,
                            phone,
                            channelCreated = {
                                Timber.e("Create channel with conversationStarted = false, and joined success!")
                                hideProgress()

                                it?.let {
                                    val channelUserData = ChannelUserData(UserMetadata.userId, UserMetadata.userName, UserMetadata.userEmail, UserMetadata.photos.first().url,
                                        UserMetadata.userPhone, id, name, email, photo, phone)
                                    val channelModel = ChannelModel(it, channelUserData)
                                    openSingleChat(channel = it, item = channelModel)
                                }
                            },
                            channelCreatedError = {
                                hideProgress()
                            }
                        )
                    }
                }
                RC_IT_IS_MATCH -> {
                    data?.let {
                        when(it.getIntExtra("type", 1)) {
                            //todo: need to check whether the user contact exists in user's phones book and route to messagesFragment or matchesChatFragment
                            1 -> navController.navigate(R.id.messagesFragment)
                            else -> navController.navigate(R.id.messagesFragment)
                        }
                    }
                }
            }
        }
    }

    override fun pressedBack() {
        onBackPressed()
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.label != "MessagesFragment") {
            super.onBackPressed()
        }
        else if (navController.currentDestination?.label == "MessagesFragment") {
            rxEventBus.post(OnBackPressedEvent())
        }
    }
}
