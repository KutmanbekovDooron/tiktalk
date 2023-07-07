package com.andyshon.tiktalk.ui

import com.andyshon.tiktalk.events.RxEventBus
import com.andyshon.tiktalk.events.UpdateLessMessagesCounterEvent
import com.andyshon.tiktalk.events.UpdateMessagesCounterEvent
import com.andyshon.tiktalk.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class MainPresenter @Inject constructor() : BasePresenter<MainContract.View>() {

    @Inject lateinit var rxEventBus: RxEventBus
    private var messagesBadgeCounter = mutableSetOf<String>()

    fun observe() {
        rxEventBus.filteredObservable(UpdateMessagesCounterEvent::class.java)
            .subscribe({
                if (messagesBadgeCounter.contains(it.channelSid).not()) {
                    messagesBadgeCounter.add(it.channelSid)
                    view?.updateNotificationBadge(messagesBadgeCounter.size)
                }
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)

        rxEventBus.filteredObservable(UpdateLessMessagesCounterEvent::class.java)
            .subscribe({
                Timber.e("UpdateLessMessagesCounterEvent called, remove sid: ${it.channelSid}")
                messagesBadgeCounter.remove(it.channelSid)
                view?.updateNotificationBadge(messagesBadgeCounter.size)
            }, {
                Timber.e("Error = ${it.message}")
            })
            .addTo(destroyDisposable)
    }

    fun reCountBadgeCounter(channelSid: String) {
        messagesBadgeCounter.reversed().forEach {
            if (it == channelSid) {
                messagesBadgeCounter.remove(it)
                view?.updateNotificationBadge(messagesBadgeCounter.size)
                return@forEach
            }
        }
    }
}