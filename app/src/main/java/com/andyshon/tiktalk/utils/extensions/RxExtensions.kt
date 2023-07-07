package com.andyshon.tiktalk.utils.extensions

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


private const val REQUEST_DELAY = 400L

//fun Disposable.addTo(compositeDisposable: CompositeDisposable): Disposable {
//    compositeDisposable.add(this)
//    return this
//}

fun <T> Single<T>.addRequestDelay(): Single<T> {
    return zipWith(Single.timer(REQUEST_DELAY, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()), BiFunction { t1, _ -> t1 })
}

fun <T> Observable<T>.applySchedulers(): Observable<T> {
    return observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
}

fun <T> Maybe<T>.applySchedulers(): Maybe<T> {
    return observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
}

fun <T> Single<T>.applySchedulers(): Single<T> {
    return observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
}

fun Completable.applySchedulers(): Completable {
    return observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
}