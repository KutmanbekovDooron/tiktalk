package com.andyshon.tiktalk.ui.matches.chat

import ChatCallbackListener
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectFragment
import com.andyshon.tiktalk.ui.base.recycler.ItemChatClickListener
import com.andyshon.tiktalk.ui.messages.mainMessages.MessagesAdapter
import com.andyshon.tiktalk.utils.extensions.*
import kotlinx.android.synthetic.main.app_toolbar_matches_chat.*
import kotlinx.android.synthetic.main.app_toolbar_main_messages_tap.*
import kotlinx.android.synthetic.main.app_toolbar_search_simple.*
import kotlinx.android.synthetic.main.fragment_matches_chat.*
import kotlinx.android.synthetic.main.layout_empty_matches.*
import timber.log.Timber
import java.lang.StringBuilder
import javax.inject.Inject

private const val TOOLBAR_STATE_SIMPLE = 1
private const val TOOLBAR_STATE_TAPPED = 2

class MatchesChatFragment : BaseInjectFragment(), MatchesChatContract.View {

    @Inject lateinit var presenter: MatchesChatPresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var rxEventBus: RxEventBus

    private var adapter: MessagesAdapter? = null

    private var listener: MatchesChatListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_matches_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presentationComponent.inject(this)
        presenter.attachToView(this)

        initListeners()
        setupList()
    }

    override fun onResume() {
        super.onResume()
        presenter.setTwilioListener()
        presenter.listAllChannels()
    }

    override fun onChatsLoaded() {
        adapter?.notifyDataSetChanged()
        adapter?.setTempRealItems()
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

    fun search(query: String) {
        adapter?.search(query)
    }

    fun btnBackPressed() {
        Timber.e("btnBackPressed, adapter = $adapter")
        setEmptyMatches(false)
        adapter?.showFullList()
        adapter?.selectedModeOff()
        adapter?.notifyDataSetChanged()
        changeToolbarState(TOOLBAR_STATE_SIMPLE)
    }

    private fun initListeners() {
        toolbarOnlyBtnBack.setOnClickListener {
            listener?.closeMatchesChat()
        }
        toolbarBtnSearch.setOnClickListener {
            toolbarMatchesChat.hide()
            toolbarMatchesChatSearch.show()
            etSearch.setText("")
            etSearch.showKeyboard3()
        }
        toolbarBtnBack.setOnClickListener {
            setEmptyMatches(false)
            adapter?.selectedModeOff()
            adapter?.notifyDataSetChanged()
            changeToolbarState(TOOLBAR_STATE_SIMPLE)
        }
        toolbarBtnDelete.setOnClickListener {
            val name = StringBuilder()
            adapter?.names?.forEach {
                if (adapter?.names?.indexOf(it) != adapter?.names?.lastIndex)
                    name.append(it.name.plus(", "))
                else name.append(it)
            }
            presenter.deleteChat(name.toString().trim())
        }
        toolbarBtnSoundOff.setOnClickListener {
            presenter.muteNotifications()
        }
        toolbarBtnVisibilityOff.setOnClickListener {
            presenter.setVisibility(adapter?.names?.toString()?:"")
        }
        toolbarBtnPin.setOnClickListener {
            adapter?.pinedSelectedItems()
            changeToolbarState(TOOLBAR_STATE_SIMPLE)
        }
        toolbarSearchSimpleBtnBack.setOnClickListener {
            toolbarMatchesChat.show()
            toolbarMatchesChatSearch.hide()
            changeToolbarState(TOOLBAR_STATE_SIMPLE)
            etSearch.hideKeyboard()
            adapter?.showFullList()
        }
        etSearch.doAfterTextChanged {
            it?.let { adapter?.search(it.toString().trim()) }
        }
    }

    private fun setupList() {
        chatsRecyclerView?.let { it.layoutManager = LinearLayoutManager(getActivityContext()) }
//        adapter = MessagesAdapter(presenter.chats, setClickListener(), isSecret = false)
        adapter = MessagesAdapter(presenter.chats, setClickListener(), isSecret = false, rxEventBus = rxEventBus)
        chatsRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    private fun setClickListener(): ItemChatClickListener<ChannelModel> {
        return object : ItemChatClickListener<ChannelModel> {

            override fun onItemClick(view: View, pos: Int, item: ChannelModel) {
                item.getChannel(ChatCallbackListener { channel ->
                    listener?.openMatchChat(channel,item)
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
                toolbarMatchesChat.show()
                toolbarMainMessagesTap.hide()
            }
            TOOLBAR_STATE_TAPPED -> {
                changePinRes(isUserPinned)
                toolbarMatchesChat.hide()
                toolbarMatchesChatSearch.hide()
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
            setEmptyMatches(true)
        }
    }

    private fun setEmptyMatches(empty: Boolean) {
        if (empty) {
            layoutEmptyMatches.show()
            chatsRecyclerView.hide()
        }
        else {
            layoutEmptyMatches.hide()
            chatsRecyclerView.show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MatchesChatListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}