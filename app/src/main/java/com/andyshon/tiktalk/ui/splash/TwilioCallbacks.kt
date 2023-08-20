package com.andyshon.tiktalk.ui.splash

import com.twilio.chat.*

abstract class TwilioCallbacks: CallbackListener<ChatClient>() {

    abstract fun mOnClientSynchronization(status: ChatClient.SynchronizationStatus?)

    override fun onSuccess(client: ChatClient) {
        client.addListener(object : ChatClientListener {
            override fun onChannelDeleted(p0: Channel?) {

            }

            override fun onInvitedToChannelNotification(p0: String?) {
            }

            override fun onClientSynchronization(p0: ChatClient.SynchronizationStatus?) {
                mOnClientSynchronization(p0)
            }

            override fun onNotificationSubscribed() {
            }

            override fun onUserSubscribed(p0: User?) {
            }

            override fun onChannelUpdated(p0: Channel?, p1: Channel.UpdateReason?) {
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

            override fun onUserUpdated(p0: User?, p1: User.UpdateReason?) {
            }

            override fun onTokenAboutToExpire() {
            }

        })
    }

    override fun onError(errorInfo: ErrorInfo?) {
        super.onError(errorInfo)
    }
}