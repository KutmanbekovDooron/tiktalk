package com.andyshon.tiktalk.ui.secretChats

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectActivity
import com.andyshon.tiktalk.ui.base.recycler.ItemChatClickListener
import com.andyshon.tiktalk.ui.chatSingle.ChatSingleActivity
import com.andyshon.tiktalk.ui.locker.FingerprintLockerActivity
import com.andyshon.tiktalk.ui.locker.PatternLockerActivity
import com.andyshon.tiktalk.ui.locker.PinLockerActivity
import com.andyshon.tiktalk.ui.messages.mainMessages.MessagesAdapter
import com.andyshon.tiktalk.utils.extensions.*
import kotlinx.android.synthetic.main.activity_secret_chats.*
import kotlinx.android.synthetic.main.app_toolbar_main_messages_tap.*
import kotlinx.android.synthetic.main.app_toolbar_search_simple.*
import kotlinx.android.synthetic.main.app_toolbar_secret_chat.*
import org.jetbrains.anko.startActivity
import javax.inject.Inject
import ChatCallbackListener
import androidx.core.view.isVisible
import java.lang.StringBuilder

private const val TOOLBAR_STATE_SIMPLE = 1
private const val TOOLBAR_STATE_TAPPED = 2

class SecretChatsActivity : BaseInjectActivity(), SecretChatsContract.View {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, SecretChatsActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject lateinit var presenter: SecretChatsPresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var rxEventBus: RxEventBus

    private var adapter: MessagesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        presenter.attachToView(this)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_secret_chats)

        initListeners()
        setupList()
    }

    override fun onResume() {
        super.onResume()

        presenter.setTwilioListener()
        presenter.listAllChannels()
    }

    override fun itemChanged(pos: Int) {
        adapter?.notifyDataSetChanged()
    }

    override fun updateAdapter() {
        adapter?.notifyDataSetChanged()
    }

    override fun chatDeleted(pos: Int) {
        changeToolbarState(TOOLBAR_STATE_SIMPLE)
    }

    override fun onChatsLoaded() {
        adapter?.notifyDataSetChanged()
        adapter?.setTempRealItems()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                228 -> {
                    if (data != null) {

                    }
                    else {
                        UserMetadata.lockerValue = ""
                        Handler().postDelayed({
                            showChooseLockDialog(getActivityContext()) {
                                when (it) {
                                    "Pattern" -> {
                                        PatternLockerActivity.startActivity(this, true)
                                    }
                                    "PIN" -> {
                                        PinLockerActivity.startActivity(this, true)
                                    }
                                    "Fingerprint" -> {
                                        FingerprintLockerActivity.startActivity(this)
                                    }
                                }
                            }
                        }, 100)
                    }
                }
            }
        }
    }

    private fun initListeners() {
        toolbarBtnKey.setOnClickListener {
            when (UserMetadata.lockerType) {
                "pattern" -> {
                    if (canOpen()) {
                        val intent = Intent(this, PatternLockerActivity::class.java)
                        intent.putExtra("wantToReset", true)
                        startActivityForResult(intent, 228)
                    }
                }
                "pin" -> {
                    if (canOpen()) {
                        val intent = Intent(this, PinLockerActivity::class.java)
                        intent.putExtra("wantToReset", true)
                        startActivityForResult(intent, 228)
                    }
                }
                "fingerprint" -> {
                    if (canOpen()) {
                        val intent = Intent(this, FingerprintLockerActivity::class.java)
                        intent.putExtra("wantToReset", true)
                        startActivityForResult(intent, 228)
                    }
                }
            }
        }
        toolbarBtnSearch.setOnClickListener {
            //            setEmptyChats(true)
            toolbarSecretChat.hide()
            toolbarSecretChatTap.hide()
            toolbarSecretChatSearch.show()
            etSearch.setText("")
            etSearch.showKeyboard3()
        }
        toolbarSecretBtnBack.setOnClickListener {
            finish()
        }
        btnHideSecretChats.setOnClickListener {
            finish()
        }
        toolbarBtnSearch.setOnClickListener {
            //            setEmptyChats(true)
            toolbarSecretChat.hide()
            toolbarSecretChatTap.hide()
            toolbarSecretChatSearch.show()
            etSearch.setText("")
            etSearch.showKeyboard3()
        }
        toolbarBtnBack.setOnClickListener {
//            setEmptyChats(false)
            adapter?.selectedModeOff()
            adapter?.notifyDataSetChanged()
            changeToolbarState(TOOLBAR_STATE_SIMPLE)
        }
        toolbarBtnDelete.setOnClickListener {
            val name = StringBuilder()
            val secretChatSids = mutableListOf<String>()
            adapter?.names?.forEach {
                if (adapter?.names?.indexOf(it) != adapter?.names?.lastIndex) {
                    name.append(it.name.plus(", "))
                    secretChatSids.add(it.chatSid)
                } else {
                    name.append(it.name)
                    secretChatSids.add(it.chatSid)
                }
            }
            presenter.deleteChat(name.toString().trim(),secretChatSids)
        }
        toolbarBtnSoundOff.setOnClickListener {
            presenter.muteNotifications()
        }
        toolbarBtnVisibilityOff.isVisible = false
//        toolbarBtnVisibilityOff.setOnClickListener {
//            val name = adapter?.names?.first()?.name ?: ""
//            presenter.setVisibility(/*adapter?.names?.toString()?:""*/name)
//        }
        toolbarBtnPin.setOnClickListener {
            adapter?.pinedSelectedItems()
            changeToolbarState(TOOLBAR_STATE_SIMPLE)
        }
        toolbarSearchSimpleBtnBack.setOnClickListener {
            toolbarSecretChatSearch.hide()
            changeToolbarState(TOOLBAR_STATE_SIMPLE)
            etSearch.hideKeyboard()
            adapter?.showFullList()
        }
        etSearch.doAfterTextChanged {
            it?.let { adapter?.search(it.toString().trim()) }
        }
    }

    private fun setupList() {
        secretChatsRecyclerView?.let { it.layoutManager = LinearLayoutManager(this) }
        adapter = MessagesAdapter(
            presenter.chats,
            setClickListener(),
            isSecret = true,
            rxEventBus = rxEventBus
        )
        secretChatsRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    private fun setClickListener(): ItemChatClickListener<ChannelModel> {
        return object : ItemChatClickListener<ChannelModel> {

            override fun onItemClick(view: View, pos: Int, item: ChannelModel) {
                val userName = if (TwilioSingleton.instance.myIdentity() == item.userData?.userEmail1) item.userData?.userName2
                else item.userData?.userName1
                val userPhoto = if (TwilioSingleton.instance.myIdentity() == item.userData?.userEmail1) item.userData?.userPhoto2
                else item.userData?.userPhoto1
                val userPhone = if (TwilioSingleton.instance.myIdentity() == item.userData?.userEmail1) item.userData?.userPhone2
                else item.userData?.userPhone1

                if (canOpen()) {
                    item.getChannel(ChatCallbackListener { channel ->
                        startActivity<ChatSingleActivity>(
                            Constants.EXTRA_CHANNEL to channel,
                            Constants.EXTRA_CHANNEL_SID to channel.sid,
                            Constants.EXTRA_CHANNEL_OPPONENT_NAME to userName,
                            Constants.EXTRA_CHANNEL_OPPONENT_PHOTO to userPhoto,
                            Constants.EXTRA_CHANNEL_OPPONENT_PHONE to userPhone
                        )
                    })
                }
            }

            override fun onItemLongClick(view: View, pos: Int, item: ChannelModel, plusOrMinus: Boolean) {
                if (plusOrMinus) {
                    presenter.checkedChats++
                } else {
                    presenter.checkedChats--
                }
                if (presenter.checkedChats >= 1) {
                    changeToolbarState(TOOLBAR_STATE_TAPPED, isUserPinned = item.isPined)
                    tvNumberCounter.text = presenter.checkedChats.toString()
                } else {
                    adapter?.selectedModeOff()
                    changeToolbarState(TOOLBAR_STATE_SIMPLE)
                }
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun changeToolbarState(state: Int, isUserPinned: Boolean=false) {
        when(state) {
            TOOLBAR_STATE_SIMPLE -> {
                presenter.checkedChats = 0
                toolbarSecretChat.show()
                toolbarSecretChatTap.hide()
            }
            TOOLBAR_STATE_TAPPED -> {
//                changePinRes(isUserPinned)
                toolbarSecretChat.hide()
                toolbarSecretChatTap.show()
            }
        }
    }

    override fun onChatDelete() {
        adapter?.removeSelectedItems()
        adapter?.selectedModeOff()
        changeToolbarState(TOOLBAR_STATE_SIMPLE)
    }
}
