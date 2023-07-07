package com.andyshon.tiktalk.events

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class RxEventBus {

    private val mBusSubject: PublishRelay<Any> = PublishRelay.create<Any>()

    /**
    * Posts an object (usually an Event) to the bus
    */
    fun post(event: Any) = mBusSubject.accept(event)

    /**
    * Observable that will emmit everything posted to the event bus.
    */
    fun observable(): Observable<Any> = mBusSubject

    /**
    * Observable that only emits events of a specific class.
    * Use this if you only want to subscribe to one type of events.
    */
    fun <T> filteredObservable(eventClass: Class<T>): Observable<T> = mBusSubject.ofType(eventClass)
}