package com.andyshon.tiktalk.ui.zoneSingle.privateRoom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.data.entity.UserPreview
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.inject.BaseInjectFragment
import com.andyshon.tiktalk.ui.base.recycler.ItemChatClickListener
import com.andyshon.tiktalk.ui.messages.MessagesContract
import com.andyshon.tiktalk.ui.messages.mainMessages.MessagesAdapter
import com.andyshon.tiktalk.ui.zoneSingle.ZoneSingleListener
import kotlinx.android.synthetic.main.fragment_zone_private_room.*
import org.jetbrains.anko.support.v4.longToast
import timber.log.Timber
import javax.inject.Inject

class ZonePrivateFragment: BaseInjectFragment(), MessagesContract.View {

    @Inject lateinit var presenter: ZonePrivatePresenter
    override fun getPresenter(): BaseContract.Presenter<*>? = presenter
    @Inject lateinit var rxEventBus: RxEventBus

    private var adapter: MessagesAdapter? = null

    private var listener: ZoneSingleListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_zone_private_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presentationComponent.inject(this)
        presenter.attachToView(this)

        setupList()
        presenter.setTwilioListener()
        presenter.listAllChannels()
    }

    fun show() {
//        presenter.listAllChannels()
    }

    override fun chatDeleted(pos: Int) {
        if (presenter.chats.isEmpty()) {
            setEmptyChats(true)
        }
    }

    override fun updateAdapter() {
        adapter?.notifyDataSetChanged()
    }

    override fun setEmptyChats(empty: Boolean) {
        if (empty) {
            longToast("There is no one other people in that place")
        }
    }

    override fun itemChanged(pos: Int) {
        adapter?.notifyDataSetChanged()
    }

    override fun onChatsLoaded() {
        Timber.e("onChatsLoaded called, size = ${presenter.chats.size}")
        adapter?.notifyDataSetChanged()
        adapter?.setTempRealItems()
    }

    fun setUsers(users: ArrayList<UserPreview>) {
        presenter.users = users
    }

    private fun setupList() {
        zonePrivateRoomRecyclerView?.let { it.layoutManager = LinearLayoutManager(getActivityContext()) }
        adapter = MessagesAdapter(
            presenter.chats,
            setClickListener(),
            isSecret = false,
            disableLongClick = true,
            rxEventBus = rxEventBus
        )
        zonePrivateRoomRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    private fun setClickListener(): ItemChatClickListener<ChannelModel> {
        return object : ItemChatClickListener<ChannelModel> {

            override fun onItemClick(view: View, pos: Int, item: ChannelModel) {
                listener?.openSingleChat(item)
            }

            override fun onItemLongClick(view: View, pos: Int, item: ChannelModel, plusOrMinus: Boolean) {
                // nothing
            }
        }
    }

    override fun onChatDelete() {
        adapter?.removeSelectedItems()
        adapter?.selectedModeOff()
        if (presenter.chats.isEmpty()) {
            setEmptyChats(true)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ZoneSingleListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ZoneSingleListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}