package com.andyshon.tiktalk.ui.base.inject

import android.os.Bundle
import android.view.View
import com.andyshon.tiktalk.App
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.di.component.PresentationComponent
import com.andyshon.tiktalk.di.module.PresentationModule
import com.andyshon.tiktalk.ui.base.BaseContract
import com.andyshon.tiktalk.ui.base.BaseFragment
import com.andyshon.tiktalk.ui.base.DialogProvider
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import javax.inject.Inject

abstract class BaseInjectFragment : BaseFragment() {

    @Inject
    lateinit var dialog: DialogProvider

    @Inject
    lateinit var prefs: PreferenceManager

    val presentationComponent: PresentationComponent by lazy {
        App[context!!].appComponent.plusPresentationComponent()
            .presentationModule(PresentationModule(this))
            .build()
    }

    protected val onViewCreated: AsyncSubject<Boolean> by lazy { AsyncSubject.create<Boolean>() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onViewCreated.onNext(true)
        onViewCreated.onComplete()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        try {
            getPresenter()?.detachFromView()
            if (dialog != null) {
                dialog?.dismissProgress()
            }
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
        super.onDestroyView()
    }

    protected inline fun injectAsync(
        crossinline callback: (PresentationComponent) -> Unit,
        crossinline onComplete: () -> Unit
    ) {

        Completable.create { callback.invoke(presentationComponent); it.onComplete() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .andThen(onViewCreated)
            .subscribe { onComplete.invoke() }
            .addTo(getDestroyDisposable())
    }

    /**
     * Returns presenter if exists
     * to detach from view onDestroy()
     */
    protected abstract fun getPresenter(): BaseContract.Presenter<*>?

    override fun showProgress(tag: Any?, message: String?) {
        if (dialog != null) dialog.showProgress(this)
    }

    override fun hideProgress(tag: Any?) {
        if (dialog != null) dialog.dismissProgress()
    }
}

