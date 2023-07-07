package com.andyshon.tiktalk.ui.base.inject

import com.andyshon.tiktalk.App
import com.andyshon.tiktalk.di.component.PresentationComponent
import com.andyshon.tiktalk.di.module.PresentationModule
import com.andyshon.tiktalk.ui.base.BaseActivity
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.DialogProvider
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

abstract class BaseInjectActivity: BaseActivity() {

    @Inject
    lateinit var dialog: DialogProvider

    val presentationComponent: PresentationComponent by lazy {
        App[this].appComponent.plusPresentationComponent()
            .presentationModule(PresentationModule(this))
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        getPresenter()?.detachFromView()
        dialog.dismissProgress()
    }

    protected inline fun injectAsync(crossinline callback: (PresentationComponent) -> Unit,
                                     crossinline onComplete: () -> Unit) {

        Completable.create({ callback.invoke(presentationComponent); it.onComplete() })
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onComplete.invoke() }
            .addTo(getDestroyDisposable())
    }

    /**
     * Returns presenter if exists
     * to detach from view on activity destroy()
     *
     *
     */
    protected abstract fun getPresenter(): BaseContract.Presenter<*>?

    override fun showProgress(tag: Any?, message: String?) {
        if (dialog != null) dialog.showProgress(this)
    }

    override fun hideProgress(tag: Any?) {
        if (dialog != null) dialog.dismissProgress()
    }
}