package com.andyshon.tiktalk.utils.rx

import android.view.View
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.MainThreadDisposable
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class RxViewsClick private constructor(vararg view: View) : ObservableOnSubscribe<View> {

    private val weakViews: WeakReference<Array<View>> = WeakReference<Array<View>>(view as Array<View>?)

    @Throws(Exception::class)
    override fun subscribe(subscriber: ObservableEmitter<View>) {

        val listener = View.OnClickListener { v ->
            if (!subscriber.isDisposed) {
                subscriber.onNext(v)
            }
        }

        if (weakViews.get() != null) {
            for (view in weakViews.get()!!) {
                view.setOnClickListener(listener)
            }
        }

        subscriber.setDisposable(object : MainThreadDisposable() {
            override fun onDispose() {
                if (weakViews.get() != null) {
                    for (view in weakViews.get()!!) {
                        view.setOnClickListener(null)
                    }
                }
                weakViews.clear()
            }
        })
    }

    companion object {

        fun create(vararg views: View): Observable<View> {
            return create(RxViewClick.TIME_DELAY, *views)
        }

        fun create(milliseconds: Int, vararg views: View): Observable<View> {
            return if (views == null || views.isEmpty()) Observable.empty()
            else Observable.create(RxViewsClick(*views))
                .throttleFirst(milliseconds.toLong(), TimeUnit.MILLISECONDS)

        }
    }
}