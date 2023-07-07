package com.andyshon.tiktalk.ui.secretChats

import android.Manifest
import android.widget.Toast
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.data.entity.ChannelUserData
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.events.UpdateMessagesCounterEvent
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.andyshon.tiktalk.utils.extensions.*
import com.tbruyelle.rxpermissions2.RxPermissions
import com.twilio.chat.*
import io.reactivex.rxkotlin.addTo
import org.jetbrains.anko.alert
import timber.log.Timber
import javax.inject.Inject
import ChatCallbackListener
import com.andyshon.tiktalk.events.RxEventBus

class SecretChatsPresenter @Inject constructor(
    private val rxEventBus: RxEventBus
) : BasePresenter<SecretChatsContract.View>(), ChatClientListener {

    var chats: ArrayList<ChannelModel> = arrayListOf()

    var checkedChats = 0

    fun setTwilioListener() {
        TwilioSingleton.instance.chatClient!!.setListener(this@SecretChatsPresenter)
    }

    fun listAllChannels() {
        view?.let {
            RxPermissions(it.getActivityContext())
                .request(Manifest.permission.READ_CONTACTS)
                .subscribe ({ granted ->
                    if (granted) {
                        TwilioSingleton.instance.chatClient!!.channels.getUserChannelsList(object: CallbackListener<Paginator<ChannelDescriptor>>() {
                            override fun onSuccess(channelPaginator: Paginator<ChannelDescriptor>?) {
                                chats.clear()
                                Timber.e("channels size = ${channelPaginator!!.items.size}")
                                if (channelPaginator.items?.isEmpty() != false) {
                                } else {
                                    for (channelD in channelPaginator.items) {
                                        channelD.getChannel(object: CallbackListener<Channel>() {
                                            override fun onSuccess(channel: Channel?) {
                                                Timber.e("CHCH = ${channel?.friendlyName}, ${channel?.uniqueName}, attrs = ${channel?.attributes}")
                                                Timber.e("channelD.attributes size = ${channelD.attributes.length()}")

                                                // show channels only with attributes and with conversationStarted = true

                                                if (channelD.attributes.length() != 0 &&
                                                    channelD.attributes.has("userId1") &&
                                                    channelD.attributes.getBoolean("conversationStarted") &&
                                                    channelD.attributes.getBoolean("isSecret")
                                                ) {

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

                                                    Timber.e("channel = ${channel?.sid}")
                                                    Timber.e("channel?.messages = ${channel!!.messages}")


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
//                                                        if (contactExists(getActivityContext(), opponentUserPhone)) {
                                                        chats.add(ChannelModel(channel, channelUserData))
                                                        view?.updateAdapter()
                                                        view?.onChatsLoaded()
//                                                        }
                                                    } else {
                                                        val messages = channel.messages
                                                        messages?.getLastMessages(1, ChatCallbackListener<List<Message>> { list ->
                                                            Timber.e("messages size = ${list.size}")

                                                            //todo: in case to test on emulator
//                                                        if (contactExists(getActivityContext(), opponentUserPhone)) {
                                                            if (list.isNotEmpty()) {
                                                                val lastMessageAuthor = list.last().author
                                                                channelUserData.lastMessageAuthor = lastMessageAuthor

                                                                channel.getUnconsumedMessagesCount(ChatCallbackListener { unread ->
                                                                    Timber.e("$unread messages still unread")
                                                                    if (unread >= 1) {
                                                                        if (channelUserData.lastMessageAuthor != UserMetadata.userEmail) {
                                                                            rxEventBus.post(UpdateMessagesCounterEvent(channel.sid))
                                                                        }
                                                                    }
                                                                })
                                                            }
                                                            chats.add(ChannelModel(channel, channelUserData))
//                                                        }

                                                            Timber.e("Posttt")
                                                            view?.updateAdapter()
                                                            view?.onChatsLoaded()
                                                        })
                                                    }
                                                }
                                            }

                                            override fun onError(errorInfo: ErrorInfo?) {
                                                Timber.e("OnError 11 = $errorInfo, ${errorInfo?.code}, ${errorInfo?.status}, ${errorInfo?.message}")
                                            }
                                        })
                                    }
                                }
                            }

                            override fun onError(errorInfo: ErrorInfo?) {
                                Timber.e("OnError 22 = $errorInfo, ${errorInfo?.code}, ${errorInfo?.status}, ${errorInfo?.message}")
                                if (errorInfo?.code == 20500) {
                                    // reconnect the twilio
                                }
                            }
                        })
                    }
                    else {
                        it.getActivityContext().alert("Чтобы продолжить необходимо разрешить доступ к контактам") {
                            isCancelable = false
                            positiveButton("OK") { }
                        }.show()
                    }
                }, {
                    Timber.e("onError = ${it.message}")
                    Toast.makeText(getActivityContext(), "error ${it.message}", Toast.LENGTH_LONG).show()
                }).addTo(destroyDisposable)
        }
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

    override fun onChannelUpdated(channel: Channel?, reason: Channel.UpdateReason?) {
        Timber.e("onChannelUpdated, UpdateReason = ${reason?.value}, channel = ${channel?.friendlyName}, ${channel?.friendlyName}, ${channel?.members?.membersList?.size}, ${channel?.sid}, ${channel?.messages?.lastConsumedMessageIndex}")
//                listAllChannels()
        if (channel?.attributes != null && channel.attributes?.has("userId1") != false) {
            val userId1 = channel!!.attributes.getInt("userId1")
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

            reason?.let {
                if (it != Channel.UpdateReason.ATTRIBUTES) {
                    rxEventBus.post(UpdateMessagesCounterEvent(channel.sid))
                }
            }

            chats.forEach {
                Timber.e("Sidd = ${it.sid} vs ${channel.sid}")
                if (it.sid == channel.sid) {
                    chats.set(chats.indexOf(it), ChannelModel(channel, channelUserData))
//                        chats.add(ChannelModel(channel, userData))
                    view?.itemChanged(chats.indexOf(it))
                    return@forEach
                }
            }
        }
    }

    override fun onRemovedFromChannelNotification(p0: String?) {
    }

    override fun onNotificationFailed(p0: ErrorInfo?) {
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
        view?.updateAdapter()
    }

    override fun onTokenAboutToExpire() {
        Timber.e("onTokenAboutToExpire")
        TwilioSingleton.instance.updateToken(getActivityContext())
    }

    override fun onTokenExpired() {
        Timber.e("onTokenExpired")
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
}