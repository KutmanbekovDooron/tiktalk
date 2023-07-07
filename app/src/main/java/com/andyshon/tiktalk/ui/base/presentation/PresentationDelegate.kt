package com.andyshon.tiktalk.ui.base.presentation

import androidx.annotation.StringRes
import io.reactivex.disposables.CompositeDisposable

interface PresentationDelegate {

    fun onDestroy()

    fun showMessage(message: String, tag: Any? = null)

    fun showMessage(@StringRes messageRes: Int, tag: Any? = null)

    fun showProgress(tag: Any? = null, message: String? = null)

    fun hideProgress(tag: Any? = null)

    fun showOnConnectionStateChanged(connected: Boolean)

    fun getDestroyDisposable(): CompositeDisposable

    class Factory {

        companion object {

            fun create(provider: PresentationComponentProvider): PresentationDelegate = PresentationDelegateImpl(provider)

        }
    }
}