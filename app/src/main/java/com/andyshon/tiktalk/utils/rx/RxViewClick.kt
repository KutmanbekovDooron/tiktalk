package com.andyshon.tiktalk.utils.rx

import android.view.View
import com.andyshon.tiktalk.utils.ExceptionUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.MainThreadDisposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class RxViewClick private constructor(view: View) : ObservableOnSubscribe<View> {

    private val weakView: WeakReference<View> = WeakReference(view)

    @Throws(Exception::class)
    override fun subscribe(subscriber: ObservableEmitter<View>) {

        val listener = View.OnClickListener { v ->
            if (!subscriber.isDisposed) {
                subscriber.onNext(v)
            }
        }

        if (weakView.get() != null) {
            weakView.get()!!.setOnClickListener(listener)
        }

        subscriber.setDisposable(object : MainThreadDisposable() {
            override fun onDispose() {
                if (weakView.get() != null) {
                    weakView.get()!!.setOnClickListener(null)
                }
                weakView.clear()
            }
        })
    }

    companion object {

        val TIME_DELAY = 400

        @JvmOverloads
        fun create(view: View, milliseconds: Int = TIME_DELAY): Observable<View> {
            ExceptionUtils.checkNull(view, View::class.java)
            return Observable.create(RxViewClick(view)).throttleFirst(milliseconds.toLong(), TimeUnit.MILLISECONDS)
        }

        @JvmOverloads
        fun create(onNext: Consumer<View>, millis: Int = TIME_DELAY): PublishSubject<View> {
            val publishSubject = PublishSubject.create<View>()
            publishSubject.throttleFirst(millis.toLong(), TimeUnit.MILLISECONDS)
                .subscribe(onNext)
            return publishSubject
        }
    }
}