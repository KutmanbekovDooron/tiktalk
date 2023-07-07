package com.andyshon.tiktalk.ui.matches.chat

import com.andyshon.tiktalk.data.entity.*
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.andyshon.tiktalk.utils.extensions.*
import com.andyshon.tiktalk.utils.phone.contactExists
import timber.log.Timber
import javax.inject.Inject
import ChatCallbackListener
import com.twilio.chat.*
import com.twilio.chat.User

class MatchesChatPresenter @Inject constructor() : BasePresenter<MatchesChatContract.View>(), ChatClientListener {

    var chats: ArrayList<ChannelModel> = arrayListOf()

    var checkedChats = 0

    fun setTwilioListener() {
        TwilioSingleton.instance.chatClient?.setListener(this@MatchesChatPresenter)
    }

    fun listAllChannels() {
        TwilioSingleton.instance.chatClient?.channels?.getUserChannelsList(ChatCallbackListener<Paginator<ChannelDescriptor>> { channelPaginator ->
            chats.clear()
            Timber.e("channels size = ${channelPaginator.items.size}")
            for (channelD in channelPaginator.items) {
                channelD.getChannel(ChatCallbackListener<Channel> { channel ->
                    if (channelD.attributes.length() != 0 && channelD.attributes.has("userId1")) {
                        Timber.e("channelD.attributes size = ${channelD.attributes.length()}")
                        val userId1 = channelD.attributes.getInt("userId1")
                        val userName1 = channelD.attributes.getString("userName1")
                        val userEmail = channelD.attributes.getString("userEmail1")
                        val userPhoto1 = channelD.attributes.getString("userPhoto1")
                        val userPhone1 = channelD.attributes.getString("userPhone1")
                        val userId2 = channelD.attributes.getInt("userId2")
                        val userName2 = channelD.attributes.getString("userName2") ?:""
                        val userEmail2 = channelD.attributes.getString("userEmail2") ?:""
                        val userPhoto2 = channelD.attributes.getString("userPhoto2")?:""
                        val userPhone2 = channelD.attributes.getString("userPhone2")

                        val channelUserData = ChannelUserData(userId1, userName1, userEmail, userPhoto1, userPhone1, userId2, userName2, userEmail2, userPhoto2, userPhone2)

                        Timber.e("userId1 = $userId1, userName1 = $userName1, userEmail1 = $userEmail, userPhoto1 = $userPhoto1, userPhone1 = $userPhone1")
                        Timber.e("userId2 = $userId2, userName2 = $userName2, userEmail2 = $userEmail2, userPhoto2 = $userPhoto2, userPhone2 = $userPhone2")
                        Timber.e("Channel names: ${channelD.sid}, ${channelD.friendlyName}, ${channelD.uniqueName}, ${channelD.membersCount}, ${channelD.messagesCount}, ${channelD.unconsumedMessagesCount}")

                        Timber.e("channel = $channel")
                        Timber.e("channel?.messages = ${channel.messages}")


                        val members = channel.members?.membersList
                        Timber.e("Members === ${members?.size}")
                        members?.forEach {
                            Timber.e("Member === ${it.identity}, ${it.channel}, ${it.sid}, ${it.lastConsumedMessageIndex}, ${it.lastConsumptionTimestamp}")

                            it.getUserDescriptor(ChatCallbackListener<UserDescriptor> { user ->
                                Timber.e("Member Descriptor = $user, ${user.isOnline}, ${user.isNotifiable}, ${user.identity}, ${user.attributes}")
                            })
                        }

                        val opponentUserPhone = TwilioSingleton.instance.getOpponentPhone(channelUserData)

                        if (channel.messages == null) {
                            //todo: in case to test on emulator
//                        if (contactExists(getActivityContext(), opponentUserPhone).not()) {
                            chats.add(ChannelModel(channel, channelUserData))
                            view?.updateAdapter()
                            view?.onChatsLoaded()
//                        }
                        }
                        else {
                            val messages = channel.messages
                            messages?.getLastMessages(1, ChatCallbackListener<List<com.twilio.chat.Message>> { list ->
                                Timber.e("messages size = ${list.size}")

                                //todo: in case to test on emulator
//                            if (contactExists(getActivityContext(), opponentUserPhone).not()) {
                                if (list.isNotEmpty()) {
                                    val lastMessageAuthor = list.last().author
                                    channelUserData.lastMessageAuthor = lastMessageAuthor
                                }
                                chats.add(ChannelModel(channel, channelUserData))
//                            }

                                view?.updateAdapter()
                                view?.onChatsLoaded()
                            })
                        }
                    }
                })
            }
        })
    }


    override fun onChannelDeleted(p0: Channel?) {

    }

    override fun onInvitedToChannelNotification(p0: String?) {
    }

    override fun onClientSynchronization(p0: ChatClient.SynchronizationStatus?) {
    }

    override fun onNotificationSubscribed() {
    }

    override fun onUserSubscribed(p0: User?) {
    }

    override fun onChannelUpdated(channel: Channel?, p1: Channel.UpdateReason?) {
        Timber.e("onChannelUpdated, UpdateReason = ${p1?.value}, channel = ${channel?.friendlyName}, ${channel?.friendlyName}, ${channel?.members?.membersList?.size}, ${channel?.sid}, ${channel?.messages?.lastConsumedMessageIndex}")
//                listAllChannels()
        channel?.let { channel->
            if (channel.attributes.length() != 0 && channel.attributes.has("userId1")) {
                val userId1 = channel.attributes.getInt("userId1")
                val userName1 = channel.attributes.getString("userName1")
                val userEmail = channel.attributes.getString("userEmail1")
                val userPhoto1 = channel.attributes.getString("userPhoto1")
                val userPhone1 = channel.attributes.getString("userPhone1")
                val userId2 = channel.attributes.getInt("userId2") ?:0
                val userName2 = channel.attributes.getString("userName2") ?:""
                val userEmail2 = channel.attributes.getString("userEmail2") ?:""
                val userPhoto2 = channel.attributes.getString("userPhoto2")?:""
                val userPhone2 = channel.attributes.getString("userPhone2")?:""

                val channelUserData = ChannelUserData(userId1, userName1, userEmail, userPhoto1, userPhone1, userId2, userName2, userEmail2, userPhoto2, userPhone2)

                chats.forEach {
                    Timber.e("Sidd = ${it.sid} vs ${channel.sid}")
                    if (it.sid == channel.sid) {
                        chats.set(chats.indexOf(it), ChannelModel(channel, channelUserData))
//                        chats.add(ChannelModel(p0, userData))
                        view?.itemChanged(chats.indexOf(it))
                        return@forEach
                    }
                }
            }
        }
    }

    override fun onRemovedFromChannelNotification(p0: String?) {
    }

    override fun onNotificationFailed(p0: ErrorInfo?) {
    }

    override fun onTokenExpired() {
    }

    override fun onChannelJoined(p0: Channel?) {
    }

    override fun onChannelAdded(p0: Channel?) {
    }

    override fun onChannelSynchronizationChange(p0: Channel?) {
    }

    override fun onUserUnsubscribed(p0: User?) {
    }

    override fun onAddedToChannelNotification(p0: String?) {
    }

    override fun onChannelInvited(p0: Channel?) {
    }

    override fun onNewMessageNotification(p0: String?, p1: String?, p2: Long) {
    }

    override fun onConnectionStateChange(p0: ChatClient.ConnectionState?) {
    }

    override fun onError(p0: ErrorInfo?) {
    }

    override fun onUserUpdated(user: User?, p1: User.UpdateReason?) {
        Timber.e("onUserUpdated user = $user, ${user?.isSubscribed}, ${user?.isOnline}, ${user?.isNotifiable}, ${user?.identity}, ${user?.attributes}")
//        chats.forEach {
//            if (it)
//        }
        view?.updateAdapter()
    }

    override fun onTokenAboutToExpire() {
    }

    fun muteNotifications() {
        view?.let {
            showMuteNotificationsDialog(it.getActivityContext()) {
                when (it) {
                    "2_hours" -> {

                    }
                    "8_hours" -> {

                    }
                    "1_week" -> {

                    }
                    "1_year" -> {

                    }
                }
            }
        }
    }

    fun setVisibility(name: String) {
        view?.let {
            showVisibilityDialog(it.getActivityContext(), name) {
                when (it) {
                    "Pattern" -> {

                    }
                    "PIN" -> {

                    }
                    "Fingerprint" -> {

                    }
                }
            }
        }
    }

    fun deleteChat(name: String) {
        view?.let {
            showDeleteChatDialog(it.getActivityContext(), name) {
                it.onChatDelete()
            }
        }
    }


    /*fun getPlaces(isAvailable: Boolean, isScrollToLast: Boolean = false) {
        model.getPlaces(isAvailable)
            .compose(applyProgressSingle())
            .subscribe({
                places.clear()
                if (it.isNotEmpty()) {
                    places.addAll(it)
                }
                view?.showChats(isScrollToLast)
            }, {
                defaultErrorConsumer
            })
            .addTo(destroyDisposable)
    }*/
}