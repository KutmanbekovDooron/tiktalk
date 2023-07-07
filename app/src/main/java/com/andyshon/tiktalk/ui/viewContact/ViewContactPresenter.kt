package com.andyshon.tiktalk.ui.viewContact

import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import com.andyshon.tiktalk.ui.base.BasePresenter
import com.andyshon.tiktalk.utils.extensions.showBlockUserDialog
import com.andyshon.tiktalk.utils.extensions.showChangeNameDialog
import com.andyshon.tiktalk.utils.extensions.showDeleteContactDialog
import com.twilio.chat.Channel
import javax.inject.Inject
import ChatCallbackListener
import com.andyshon.tiktalk.Constants
import com.twilio.chat.Message
import timber.log.Timber

class ViewContactPresenter @Inject constructor() : BasePresenter<ViewContactContract.View>() {

    var channel: Channel? = null

    var media = arrayListOf("","","","","","","","","","")

    fun loadSharedMedia() {
        channel?.let {
            TwilioSingleton.instance.chatClient?.channels?.getChannel(it.sid, ChatCallbackListener<Channel> {
//                this@ChatSinglePresenter.channel = it
//                channel?.addListener(this@ChatSinglePresenter)

                media.clear()

                val messages = channel?.messages
                messages?.getLastMessages(100, ChatCallbackListener<List<Message>> { list ->
                    Timber.e("messages size = ${list.size}")

                    list.forEach { msg ->
                        Timber.e("Message: ${msg.author}, ${msg.messageBody}, hasMedia = ${msg.hasMedia()}")

                        if (msg.hasMedia()) {
                            if (msg.media.type == Constants.Chat.Media.TYPE_IMAGE) {
                                media.add(msg.media.sid)
                            }
                        }
                    }
                    view?.sharedMediaLoaded()
                })
            })
        }
    }

    fun block(name: String) {
        view?.let {
            showBlockUserDialog(it.getActivityContext(), name) {
                it.blocked()
            }
        }
    }

    fun edit(name: String) {
        view?.let {
            showChangeNameDialog(it.getActivityContext(), name) {
                newVal -> it.edited()
            }
        }
    }

    fun delete(name: String) {
        view?.let {
            showDeleteContactDialog(it.getActivityContext(), name) {
                it.deleted()
            }
        }
    }
}