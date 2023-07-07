package com.andyshon.tiktalk.ui.messages.mainMessages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.recycler.ItemChatClickListener
import com.andyshon.tiktalk.utils.extensions.*
import kotlinx.android.synthetic.main.item_main_messages.view.*
import timber.log.Timber
import kotlin.collections.ArrayList
import ChatCallbackListener
import android.util.TypedValue
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.andyshon.tiktalk.Constants
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.events.UpdateLessMessagesCounterEvent
import com.twilio.chat.*

class MessagesAdapter(
    private var chats: ArrayList<ChannelModel>,
    private val itemClicksListener: ItemChatClickListener<ChannelModel>,
    private val isSecret: Boolean,
    private val disableLongClick: Boolean = false,
    private val rxEventBus: RxEventBus
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var tempRealItems = arrayListOf<ChannelModel>()

    fun setTempRealItems() {
        tempRealItems.clear()
        tempRealItems.addAll(chats)
    }

    fun search(query: String) {
        Timber.e("Search query = $query, size all = ${tempRealItems.size}")
        val list = arrayListOf<ChannelModel>()
        tempRealItems.forEach { channelModel ->
            val opponentName = TwilioSingleton.instance.getOpponentName(channelModel)
            Timber.e("opponentName = $opponentName")
            if (opponentName.contains(query, ignoreCase = true)) {
                list.add(channelModel)
            }
        }
        chats.clear()
        chats.addAll(list)
        notifyDataSetChanged()
    }

    fun showFullList() {
        chats.clear()
        chats.addAll(tempRealItems)
        notifyDataSetChanged()
    }

    var names = arrayListOf</*String*/ChatToDelete>()
    private var selectedMode = false

    fun selectedModeOff() {
        chats.forEach {
            it.isChecked = false
        }
        names.clear()
        selectedMode = false
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < chats.size) {
            chats.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun removeSelectedItems() {
        for (i in (0..chats.lastIndex).reversed()) {
            if (chats[i].isChecked) {
                chats.removeAt(i)
                notifyItemRemoved(i)
            }
        }
    }

    fun pinedSelectedItems() {
        for (i in (0..chats.lastIndex)) {
            if (chats[i].isChecked) {
                chats[i].isPined = chats[i].isPined.not()
//                notifyItemChanged(i)
            }
        }
        selectedModeOff()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = ChatsViewHolder.create(parent)
        if (disableLongClick.not()) {
            holder.itemView.setOnLongClickListener {
                val position = holder.adapterPosition
                selectedMode = true
                names.add(ChatToDelete(chats[position].userData?.userName2?:"",chats[position].sid))
                chats[position].isChecked = chats[position].isChecked.not()
                itemClicksListener.onItemLongClick(it, position, chats[position], chats[position].isChecked)
                true
            }
        }
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (selectedMode) {
//                names.add(chats[position].userData?.userName2?:"")
                names.add(ChatToDelete(chats[position].userData?.userName2?:"",chats[position].sid))
                chats[position].isChecked = chats[position].isChecked.not()
                itemClicksListener.onItemLongClick(it, position, chats[position], chats[position].isChecked)
            }
            else {
                itemClicksListener.onItemClick(it, position, chats[position])
            }
        }
        return holder
    }

    override fun getItemCount(): Int = chats.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ChatsViewHolder).bind(chats[position], isSecret, rxEventBus)
    }


    class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup?): ChatsViewHolder {
                val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_main_messages, parent, false)
                return ChatsViewHolder(v)
            }
        }

        fun bind(channel: ChannelModel, secret: Boolean, rxEventBus: RxEventBus) {

            channel.getUnconsumedMessagesCount(ChatCallbackListener { unread ->
                Timber.e("$unread messages still unread, ss = ${channel.userData?.lastMessageAuthor}, ${UserMetadata.userEmail}")
                if (unread >= 1) {
                    if (channel.userData?.lastMessageAuthor?: "" != UserMetadata.userEmail) {
                        itemView.tvMessageCounter.text = unread.toString()
                        itemView.tvMessageCounter.show()
                    }
                    else {
                        itemView.tvMessageCounter.hide()
                    }
                } else {
                    rxEventBus.post(UpdateLessMessagesCounterEvent(channel.sid))
                    itemView.tvMessageCounter.hide()
                }
            })

            val me = TwilioSingleton.instance.chatClient?.myIdentity?:""
            Timber.e("ME = $me, userEmail1 = ${channel.userData?.userEmail1}, userEmail2 = ${channel.userData?.userEmail2}")

            if (me == channel.userData?.userEmail1) {
                if (channel.userData?.userPhoto2?.isNotEmpty() != false) {
                    itemView.ivAvatar.loadRoundCornersImageWithFallback(channel.userData?.userPhoto2?:"")
                }
            } else {
                if (channel.userData?.userPhoto1?.isNotEmpty() != false) {
                    itemView.ivAvatar.loadRoundCornersImageWithFallback(channel.userData?.userPhoto1?:"")
                }
            }

            if (channel.newMatch) {
                itemView.tvUserName.show()
                itemView.tvUserMessage.text = itemView.context string R.string.new_match
                itemView.tvUserMessage.setTextColor(itemView.context color R.color.colorBtnViolet)
            } else {
//                itemView.tvUserMessage.setTextColor(itemView.context color R.color.colorBlack)
                val typedValue = TypedValue()
                val theme = itemView.context.theme
                theme.resolveAttribute(R.attr.primaryTextColor, typedValue, true)
                @ColorInt val color = typedValue.data
                itemView.tvUserMessage.setTextColor(color)

                channel.getChannel(ChatCallbackListener { chan->
                    chan.messages?.getLastMessages(1, ChatCallbackListener<List<Message>> { messages->
                        if (messages.isNotEmpty()) {
                            itemView.tvUserName.show()
                            if (channel.userData?.userEmail1 == me/*messages.first().author*/) {
                                itemView.tvUserName.text = channel.userData?.userName2 ?: "-"
                            } else {
                                itemView.tvUserName.text = channel.userData?.userName1 ?: "-"
                            }
                            if (messages[0].hasMedia()) {
                                when {
                                    messages[0].media.type == Constants.Chat.Media.TYPE_VOICE -> itemView.tvUserMessage.text = itemView.context string R.string.voice_message
                                    messages[0].media.type == Constants.Chat.Media.TYPE_VIDEO -> itemView.tvUserMessage.text = itemView.context string R.string.video
                                    messages[0].media.type == Constants.Chat.Media.TYPE_IMAGE -> itemView.tvUserMessage.text = itemView.context string R.string.photo
                                    messages[0].media.type == Constants.Chat.Media.TYPE_FILE -> itemView.tvUserMessage.text = itemView.context string R.string.file
                                    messages[0].media.type == Constants.Chat.Media.TYPE_MUSIC -> itemView.tvUserMessage.text = itemView.context string R.string.music
                                    else -> itemView.tvUserMessage.text = itemView.context string R.string.media
                                }
                            } else {
                                if (messages[0].attributes != null) {
                                    when {
                                        messages[0].attributes.has("replyName") ->
                                            itemView.tvUserMessage.text = (itemView.context string R.string.reply_).plus(messages[0].messageBody ?: "-")
                                        messages[0].attributes.has("contactName") ->
                                            itemView.tvUserMessage.text = (itemView.context string R.string.contact_).plus(messages[0].attributes.getString("contactName"))
                                        messages[0].attributes.has("callStatus") -> itemView.tvUserMessage.text = messages[0].attributes.getString("callStatus")
                                        else -> itemView.tvUserMessage.text = messages[0].messageBody ?: "-"
                                    }
                                } else {
                                    itemView.tvUserMessage.text = messages[0].messageBody ?: "-"
                                }
                            }
                        } else {
                            itemView.tvUserName.hide()
                            itemView.tvUserMessage.text = itemView.context string R.string.no_messages_yet
                        }
                    })
                })
            }

            if (channel.lastMessageDate == null) {  // if channel was created and doesn't has any messages yet
                itemView.tvMessageTime.text = getTimeForMessageDate(channel.dateCreatedAsDate.toString())
            }
            else {
                itemView.tvMessageTime.text = getTimeForMessageDate(channel.lastMessageDate.toString())
            }

            if (channel.isChecked) {
                itemView.btnAvatarCheckMark.show()
                itemView.layoutChatsListItemRoot.setBackgroundColor(itemView.context color R.color.colorLongClickBg)
            } else {
                itemView.btnAvatarCheckMark.hide()
                itemView.layoutChatsListItemRoot.setBackgroundColor(itemView.context color R.color.colorTransparent)
            }

            if (channel.isPined) {
                itemView.ivPin.show()
            } else {
                itemView.ivPin.hide()
            }

            if (secret) {
                itemView.ivSecret.show()
            } else {
                itemView.ivSecret.hide()
            }


            channel.getChannel(ChatCallbackListener { ch->
                ch.members?.membersList?.let {
                    for (member in it) {
                        Timber.e("member = ${member.identity}")
                        if (member.identity != UserMetadata.userEmail) {
                            fillUserReachAbility(itemView.ivOnlineIndicator, member)
                        }
                    }
                }
            })
        }

        private fun fillUserReachAbility(reachabilityView: ImageView, member: Member) {
            if (TwilioSingleton.instance.chatClient?.isReachabilityEnabled?.not() != false) {
                reachabilityView.hide()
            } else {
                member.getAndSubscribeUser(ChatCallbackListener<User> { user->
                    Timber.e("getAndSubscribeUser, user = ${user.identity}, isOnline = ${user.isOnline}, isNotifiable = ${user.isNotifiable}")
                    if (user.isOnline) {
                        reachabilityView.show()
                    } else {
                        reachabilityView.hide()
                    }
                })
            }
        }
    }
}