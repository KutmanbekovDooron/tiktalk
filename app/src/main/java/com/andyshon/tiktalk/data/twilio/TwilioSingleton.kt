package com.andyshon.tiktalk.data.twilio

import ChatCallbackListener
import ChatStatusListener
import android.annotation.SuppressLint
import android.content.Context
import com.andyshon.tiktalk.App
import com.andyshon.tiktalk.data.UserMetadata
import com.andyshon.tiktalk.data.entity.ChannelModel
import com.andyshon.tiktalk.data.entity.ChannelUserData
import com.andyshon.tiktalk.data.model.auth.AuthModel
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.twilio.chat.*
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class TwilioSingleton @Inject constructor() : ChatClientListener, CallbackListener<ChatClient>() {

    @Inject
    lateinit var authModel: AuthModel

    @Inject
    lateinit var prefs: PreferenceManager

    var chatClient: ChatClient? = null

    private var clientSynchronization: ClientSynchronization? = null

    companion object {
        val instance = TwilioSingleton()
    }

    fun disconnect() {
        Timber.e("disconnect called")
//        chatClient.shutdown()
//        chatClient.removeListener()
    }

    @SuppressLint("CheckResult")
    fun updateToken(context: Context) {
        context.let { App[it].appComponent.inject(this) }
        authModel.getTwilioToken()
            .subscribe({
                chatClient?.updateToken(it.token, object : StatusListener() {
                    override fun onSuccess() {
                        Timber.e("Twilio Chat token successfully updated!")
                    }
                })
            }, { Timber.e("Error = ${it.message}") })
    }

    fun myIdentity(): String {
        return chatClient?.myIdentity ?: ""
    }

    fun getNameByEmail(channel: Channel?, author: String): String {
        return if (channel != null) ({
            val name1 = channel.attributes.jsonObject?.getString("userName1")
            val name2 = channel.attributes.jsonObject?.getString("userName2")
            val email1 = channel.attributes.jsonObject?.getString("userEmail1")

            if (author == email1) name1 else name2
        }).toString() else author
    }

    fun getOpponentName(channelModel: ChannelModel): String {
        return if (UserMetadata.userEmail == channelModel.userData?.userEmail1)
            channelModel.userData?.userName2 ?: ""
        else channelModel.userData?.userName1 ?: ""
    }

    fun getOpponentPhone(channelUserData: ChannelUserData): String {
        return if (UserMetadata.userEmail == channelUserData.userEmail1)
            channelUserData.userPhone2
        else channelUserData.userPhone1
    }

    fun connect(context: Context, twilioUserToken: String, listener: ClientSynchronization) {
        Timber.e("TWILIO INSTANCE = $instance")
        ChatClient.setLogLevel(2)
        clientSynchronization = listener
        val props = ChatClient.Properties
            .Builder()
            .setDeferCertificateTrustToPlatform(true)
            .createProperties()

        ChatClient.create(
            context.applicationContext,
            twilioUserToken,
            props,
            this
        )
    }

    fun createPlaceChannel(
        placeId: String,
        placeName: String,
        channelCreated: (channel: Channel?) -> Unit
    ) {

        val channelName = placeName.plus("_").plus(placeId)
        Timber.e("Name for place channel = $channelName")

        chatClient?.let { chatClient ->
            chatClient.channels.getChannel(channelName, object : CallbackListener<Channel>() {
                override fun onSuccess(channel: Channel?) {
                    Timber.e("getChannel with name = $channelName, onSuccess ${channel?.sid}, ${channel?.friendlyName}, ${channel?.uniqueName}, ${channel?.members?.membersList?.size}")
                    channel?.let {
                        showMemberOfChannel(it)
                        channelCreated.invoke(channel)
                    }
                }

                override fun onError(errorInfo: ErrorInfo?) {
                    Timber.e("getChannel with name = $channelName, onError ${errorInfo?.message}, ${errorInfo?.status}, ${errorInfo?.code}")
                    // channel not found
                    chatClient.channels.channelBuilder()
                        .withFriendlyName(channelName)
                        .withUniqueName(channelName)
                        .withType(Channel.ChannelType.PUBLIC)
                        .build(ChatCallbackListener<Channel> { channel ->
                            channel.let {
                                channelCreated.invoke(channel)
                            }
                        })
                }
            })
        }
    }

    fun createChannel(
        conversationStarted: Boolean,
        userId1: Int,
        userName1: String,
        userEmail1: String,
        userPhoto1: String,
        userPhone1: String,
        userId: Int,
        userName: String,
        userEmail: String,
        userPhoto: String,
        userPhone2: String,
        channelCreated: (channel: Channel?) -> Unit,
        channelCreatedError: ((errorInfo: ErrorInfo?) -> Unit)? = null
    ) {

        val ownName = UserMetadata.userName
        val channelName = ownName.plus("_").plus(userName)
        Timber.e("channelName = $channelName")

        val json = JSONObject()
        json.put("conversationStarted", conversationStarted)
        json.put("isSecret", false)
        json.put("userId1", userId1)
        json.put("userName1", userName1)
        json.put("userEmail1", userEmail1)
        json.put("userPhoto1", userPhoto1)
        json.put("userPhone1", userPhone1)
        json.put("userId2", userId)
        json.put("userName2", userName)
        json.put("userEmail2", userEmail)
        json.put("userPhoto2", userPhoto)
        json.put("userPhone2", userPhone2)

        chatClient?.let { chatClient ->
            chatClient.channels.channelBuilder()
                .withFriendlyName(channelName)
                .withType(Channel.ChannelType.PUBLIC)
                .withAttributes(Attributes(json))
                .build(object : CallbackListener<Channel>() {
                    override fun onSuccess(channel: Channel?) {
                        channel?.let {
                            showMemberOfChannel(it)

                            it.join(ChatStatusListener {
                                Timber.e("onSuccess joined channel!")
                                channel.members.addByIdentity(userEmail, ChatStatusListener {
                                    Timber.e("addByIdentity $userEmail successfully added to a channel!")

                                    showMemberOfChannel(it)

                                    channelCreated.invoke(channel)
                                })
                            })
                        }
                    }

                    override fun onError(errorInfo: ErrorInfo?) {
                        channelCreatedError?.invoke(errorInfo)
                    }
                })
        }
    }

    private fun showMemberOfChannel(channel: Channel) {
        channel.members?.let {
            val members = it.membersList
            Timber.e("Members = ${members.size}")
            members.forEach {
                Timber.e("Member = ${it.identity}, ${it.channel}, ${it.sid}, ${it.lastConsumedMessageIndex}")
            }
        }
    }

    fun updateChannelSecret(channelSid: String, success: () -> Unit) {
        chatClient!!.channels.getChannel(channelSid, ChatCallbackListener { channel ->
            val attrs = channel.attributes
            val secret = attrs.jsonObject?.getBoolean("isSecret")
            attrs.jsonObject?.put("isSecret", secret?.not())
            channel.setAttributes(attrs, ChatStatusListener {
                success.invoke()
            })
        })
    }

    fun updateUserNameInAllChannels(newName: String, updated: () -> Unit) {
        TwilioSingleton.instance.chatClient?.channels?.getUserChannelsList(object :
            CallbackListener<Paginator<ChannelDescriptor>>() {
            override fun onSuccess(channelPaginator: Paginator<ChannelDescriptor>?) {
                channelPaginator?.items?.let {
                    if (it.isNotEmpty()) {
                        for (channelD in channelPaginator.items) {
                            channelD.getChannel(object : CallbackListener<Channel>() {
                                override fun onSuccess(channel: Channel?) {
                                    channel?.let {
                                        if (it.attributes.jsonObject?.has("userId1") == true) {
                                            Timber.e("old attributes size = ${it.attributes.jsonObject?.length()}, channel name: ${it.uniqueName}, ${it.friendlyName}")
                                            Timber.e("old attrs = ${channel.attributes}")

                                            val userEmail1 = it.attributes.jsonObject?.getString("userEmail1")
                                            Timber.e("DATA: userEmail1 = $userEmail1, UserMetadata.userEmail = ${UserMetadata.userEmail}")

                                            val attrs = it.attributes
                                            if (userEmail1 == UserMetadata.userEmail)
                                                attrs.jsonObject?.put("userName1", newName)
                                            else
                                                attrs.jsonObject?.put("userName2", newName)
                                            Timber.e("new attrs = $attrs")

                                            it.setAttributes(attrs, object : StatusListener() {
                                                override fun onSuccess() {
                                                    Timber.e("Success update user's name!")
                                                    updated.invoke()
                                                }

                                                override fun onError(errorInfo: ErrorInfo?) {
                                                    Timber.e("OnError 00000 = $errorInfo, ${errorInfo?.code}, ${errorInfo?.status}, ${errorInfo?.message}")
                                                    //  OnError 00000 = Error 0:50107 User unauthorized for command, 50107, 0, User unauthorized for command
                                                }
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
            }

            override fun onError(errorInfo: ErrorInfo?) {
                Timber.e("OnError 22 = $errorInfo, ${errorInfo?.code}, ${errorInfo?.status}, ${errorInfo?.message}")
                if (errorInfo?.code == 20500) {
                    // reconnect the twilio
                }
            }
        })
    }

    override fun onSuccess(client: ChatClient?) {
        chatClient = client
        client?.addListener(this)
    }

    override fun onClientSynchronization(status: ChatClient.SynchronizationStatus?) {
        if (status == ChatClient.SynchronizationStatus.COMPLETED) {
            clientSynchronization?.onSync()
        }
    }

    override fun onChannelAdded(p0: Channel?) = Unit
    override fun onChannelDeleted(p0: Channel?) = Unit
    override fun onChannelInvited(p0: Channel?) = Unit
    override fun onChannelJoined(p0: Channel?) = Unit
    override fun onChannelSynchronizationChange(p0: Channel?) = Unit
    override fun onError(p0: ErrorInfo?) {
    }

    override fun onChannelUpdated(p0: Channel?, p1: Channel.UpdateReason?) = Unit
    override fun onNewMessageNotification(p0: String?, p1: String?, p2: Long) = Unit
    override fun onConnectionStateChange(p0: ChatClient.ConnectionState?) = Unit
    override fun onTokenExpired() = Unit
    override fun onTokenAboutToExpire() = Unit
    override fun onRemovedFromChannelNotification(p0: String?) = Unit
    override fun onNotificationSubscribed() = Unit
    override fun onNotificationFailed(p0: ErrorInfo?) = Unit
    override fun onUserSubscribed(p0: User?) = Unit
    override fun onUserUnsubscribed(p0: User?) = Unit
    override fun onUserUpdated(p0: User?, p1: User.UpdateReason?) = Unit
    override fun onAddedToChannelNotification(p0: String?) = Unit
    override fun onInvitedToChannelNotification(p0: String?) = Unit
}