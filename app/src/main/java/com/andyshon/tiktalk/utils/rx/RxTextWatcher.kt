package com.andyshon.tiktalk.utils.rx

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.MainThreadDisposable
import io.reactivex.android.MainThreadDisposable.verifyMainThread
import java.lang.ref.WeakReference

class RxTextWatcher private constructor(view: TextView) : ObservableOnSubscribe<CharSequence> {

    private val weakView: WeakReference<TextView> = WeakReference(view)

    @Throws(Exception::class)
    override fun subscribe(subscriber: ObservableEmitter<CharSequence>) {
        verifyMainThread()

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!subscriber.isDisposed) {
                    subscriber.onNext(s)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        }

        subscriber.setDisposable(object : MainThreadDisposable() {
            override fun onDispose() {
                if (weakView.get() != null) weakView.get()!!.removeTextChangedListener(watcher)
            }
        })

        if (weakView.get() != null) weakView.get()!!.addTextChangedListener(watcher)
    }

    companion object {

        fun create(view: TextView): Observable<CharSequence> {
            return Observable.create(RxTextWatcher(view))
        }
    }
}