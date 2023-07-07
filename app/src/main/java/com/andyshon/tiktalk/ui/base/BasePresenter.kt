package com.andyshon.tiktalk.ui.base

import android.app.Activity
import com.andyshon.tiktalk.data.network.error.ApiErrorHandler
import com.andyshon.tiktalk.data.network.error.ApiException
import io.reactivex.CompletableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import timber.log.Timber

abstract class BasePresenter<T : BaseContract.View> : BaseContract.Presenter<T> {

    protected val destroyDisposable: CompositeDisposable = CompositeDisposable()

    protected var view: T? = null
        private set

    protected val defaultErrorConsumer: Consumer<Throwable> by lazy {
        ApiErrorHandler.throwableConsumer { throwable ->
            showDefaultErrors(throwable)
            return@throwableConsumer view
        }
    }

    override fun attachToView(view: T) {
        this.view = view
    }

    override fun detachFromView() {
        if (!destroyDisposable.isDisposed) {
            destroyDisposable.dispose()
        }
        view = null
    }

    protected fun getActivityContext(): Activity = view!!.getActivityContext()

    protected fun onClearedSubscribe(): Consumer<in Disposable> {
        return Consumer { disposable -> destroyDisposable.add(disposable) }
    }

    protected fun <T> applyProgressObservable(tag: Any? = null): ObservableTransformer<T, T> =
        ObservableTransformer {
            it.doOnSubscribe { view?.showProgress(tag) }.doFinally { view?.hideProgress(tag) }
        }

    protected fun applyProgressCompletable(tag: Any? = null): CompletableTransformer =
        CompletableTransformer {
            it.doOnSubscribe { view?.showProgress(tag) }.doFinally { view?.hideProgress(tag) }
        }

    protected fun <T> applyProgressSingle(tag: Any? = null): SingleTransformer<T, T> =
        SingleTransformer {
            it.doOnSubscribe { view?.showProgress(tag) }.doFinally {
                Timber.d("applyProgressSingle SingleTransformer doFinally")
                view?.hideProgress(tag) }
        }

    private fun showDefaultErrors(throwable: Throwable?, tag: Any? = null) {
        view?.hideProgress(tag)
        Timber.e("Throwable === ${throwable}")
        if (throwable is ApiException) {
            view?.showMessage(throwable.mMessage)
        }
        else if (throwable is Error) {
            view?.showMessage(throwable.message!!)
        }
        else {
            throwable?.message?.let { view?.showMessage(it) }
        }
    }
}