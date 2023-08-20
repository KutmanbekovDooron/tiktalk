package com.andyshon.tiktalk.ui.messages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.events.OnBackPressedEvent
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectFragment
import com.andyshon.tiktalk.ui.base.recycler.ItemChatClickListener
import com.andyshon.tiktalk.ui.messages.mainMessages.MessagesAdapter
import com.andyshon.tiktalk.ui.messages.mainMessages.MessagesListener
import com.andyshon.tiktalk.utils.extensions.*
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.app_toolbar_main_messages.*
import kotlinx.android.synthetic.main.app_toolbar_main_messages_tap.*
import kotlinx.android.synthetic.main.app_toolbar_search_simple.*
import kotlinx.android.synthetic.main.fragment_messages.toolbarMainMessages
import kotlinx.android.synthetic.main.fragment_messages.toolbarMainMessagesSearch
import kotlinx.android.synthetic.main.fragment_messages.toolbarMainMessagesTap
import kotlinx.android.synthetic.main.fragment_messages.*
import kotlinx.android.synthetic.main.layout_empty_chats.*
import timber.log.Timber
import java.lang.StringBuilder
import javax.inject.Inject
import ChatCallbackListener

private const val TOOLBAR_STATE_SIMPLE = 1
private const val TOOLBAR_STATE_TAPPED = 2

class MessagesFragment: BaseInjectFragment(), MessagesContract.View {

    @Inject lateinit var presenter: MessagesPresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter

    @Inject lateinit var rxEventBus: RxEventBus

    private var adapter: MessagesAdapter? = null

    private var listener: MessagesListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presentationComponent.inject(this)
        presenter.attachToView(this)
        initListeners()
        setupList()
        setupEvents()

        presenter.setTwilioListener()
        presenter.listAllChannels()
    }

    override fun onResume() {
        super.onResume()
        presenter.setTwilioListener()
        presenter.listAllChannels()
        setupToolbar()
//        adapter?.notifyDataSetChanged() // need in case to update messages badge when user return from chat
    }

    override fun itemChanged(pos: Int) {
//        adapter?.notifyItemChanged(pos)
//        adapter?.notifyItemInserted(presenter.chats.size-1)
//        adapter?.notifyItemInserted(presenter.chats.size)
        adapter?.notifyDataSetChanged()
    }

    override fun updateAdapter() {
        adapter?.notifyDataSetChanged()
    }

    private fun setupEvents() {
        rxEventBus.filteredObservable(OnBackPressedEvent::class.java)
            .subscribe({
                toolbarSearchSimpleBtnBack.performClick()
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(getDestroyDisposable())
    }

    private fun setupToolbar() {
        toolbarUserAvatar.loadRoundCornersImage(
            radius = getActivityContext().resources.getDimensionPixelSize(R.dimen.radius_100),
            url = UserMetadata.photos.first().url
        )
    }

    private fun initListeners() {
        toolbarUserAvatar.setOnClickListener {
            listener?.openSettings()
        }
        btnShowSecretChats.setOnClickListener {
            Timber.e("Current locker = ${UserMetadata.lockerType}, locker value = ${UserMetadata.lockerValue}")
            when (UserMetadata.lockerType) {
                "pattern" -> {
                    listener?.openPattern()
                }
                "pin" -> {
                    listener?.openPIN()
                }
                "fingerprint" -> {
                    listener?.openFingerprint()
                }
                else -> {
                    showChooseLockDialog(getActivityContext()) {
                        when (it) {
                            "Pattern" -> {
                                listener?.openPattern()
                            }
                            "PIN" -> {
                                listener?.openPIN()
                            }
                            "Fingerprint" -> {
                                listener?.openFingerprint()
                            }
                        }
                    }
                }
            }
        }
        toolbarBtnSearch.setOnClickListener {
            toolbarMainMessages.hide()
            toolbarMainMessagesSearch.show()
            etSearch.setText("")
            etSearch.showKeyboard3()
        }
        toolbarBtnBack.setOnClickListener {
            adapter?.selectedModeOff()
            adapter?.notifyDataSetChanged()
            changeToolbarState(TOOLBAR_STATE_SIMPLE)
        }
        toolbarBtnDelete.setOnClickListener {
            val name = StringBuilder()
            adapter?.names?.forEach {
                if (adapter?.names?.indexOf(it) != adapter?.names?.lastIndex)
                    name.append(it.name.plus(", "))
                else name.append(it.name)
            }
            presenter.deleteChat(name.toString().trim())
//            adapter?.selectedModeOff()
//            changeToolbarState(TOOLBAR_STATE_SIMPLE)
//            if (presenter.chats.isEmpty()) {
//                setEmptyChats(true)
//            }
        }
        toolbarBtnSoundOff.setOnClickListener {
            presenter.muteNotifications()
        }
        toolbarBtnVisibilityOff.setOnClickListener {
            val name = adapter?.names?.first()?.name ?: ""
            presenter.setVisibility(/*adapter?.names?.toString()?:""*/name)
        }
        toolbarBtnPin.setOnClickListener {
            adapter?.pinedSelectedItems()
            changeToolbarState(TOOLBAR_STATE_SIMPLE)
        }
        btnSelectContact.setOnClickListener {
            listener?.openSelectContact()
        }
        toolbarSearchSimpleBtnBack.setOnClickListener {
            toolbarMainMessagesSearch.hide()
            changeToolbarState(TOOLBAR_STATE_SIMPLE)
            etSearch.hideKeyboard()
            adapter?.selectedModeOff()
            adapter?.showFullList()
        }
        etSearch.doAfterTextChanged {
            it?.let { adapter?.search(it.toString().trim()) }
        }
    }

    private fun setupList() {
        chatsRecyclerView?.let { it.layoutManager = LinearLayoutManager(getActivityContext()) }
        adapter = MessagesAdapter(presenter.chats, setClickListener(), isSecret = false, rxEventBus = rxEventBus)
        chatsRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    override fun onChatsLoaded() {
        adapter?.notifyDataSetChanged()
        adapter?.setTempRealItems()
    }

    private fun setClickListener(): ItemChatClickListener<ChannelModel> {
        return object : ItemChatClickListener<ChannelModel> {

            override fun onItemClick(view: View, pos: Int, item: ChannelModel) {
                item.getChannel(ChatCallbackListener { channel ->
                    listener?.openSingleChat(channel, item)
                })
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
//                adapter?.notifyItemChanged(pos)
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun changeToolbarState(state: Int, isUserPinned: Boolean=false) {
        when(state) {
            TOOLBAR_STATE_SIMPLE -> {
                presenter.checkedChats = 0
                toolbarMainMessages.show()
                toolbarMainMessagesTap.hide()
            }
            TOOLBAR_STATE_TAPPED -> {
                changePinRes(isUserPinned)
                toolbarMainMessages.hide()
                toolbarMainMessagesSearch.hide()
                toolbarMainMessagesTap.show()
            }
        }
    }

    private fun changePinRes(b:Boolean) {
        if (b) {
            toolbarBtnPin.setImageResource(R.drawable.ic_pin_off)
        }
        else {
            toolbarBtnPin.setImageResource(R.drawable.ic_pin)
        }
    }

    override fun onChatDelete() {
        adapter?.removeSelectedItems()
        adapter?.selectedModeOff()
        changeToolbarState(TOOLBAR_STATE_SIMPLE)
        if (presenter.chats.isEmpty()) {
            setEmptyChats(true)
        }
    }

    override fun chatDeleted(pos: Int) {
//        adapter?.removeSelectedItems()
//        adapter?.selectedModeOff()
        changeToolbarState(TOOLBAR_STATE_SIMPLE)
        if (presenter.chats.isEmpty()) {
            setEmptyChats(true)
        }
    }

    override fun setEmptyChats(empty: Boolean) {
        if (empty) {
            layoutEmptyChats.show()
            chatsRecyclerView.hide()
        }
        else {
            layoutEmptyChats.hide()
            chatsRecyclerView.show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MessagesListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement MessagesListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}