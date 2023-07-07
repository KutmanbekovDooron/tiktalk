package com.andyshon.tiktalk.ui.base.presentation

import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.widget.dialog.MessageDialog
import com.andyshon.tiktalk.ui.widget.dialog.ProgressDialog
import io.reactivex.disposables.CompositeDisposable
import java.lang.ref.WeakReference

class PresentationDelegateImpl(provider: PresentationComponentProvider) : PresentationDelegate {

    private val providerRef = WeakReference(provider)
    private var lastTime = 0L

    private val disposable by lazy {
        CompositeDisposable()
    }

    private val progressDialog by lazy {
        ProgressDialog.newInstance(provider.provideActivity().getString(R.string.dialog_action_loading))
    }

    override fun onDestroy() {
        disposable.clear()
    }

    override fun showMessage(messageRes: Int, tag: Any?) {
        if (providerRef.get() != null) {
            showMessage(providerRef.get()!!.provideActivity().getString(messageRes), tag)
        }
    }

    override fun showMessage(message: String, tag: Any?) {
        hideProgress()

        if (providerRef.get() != null && System.currentTimeMillis() - lastTime > 100) {
            lastTime = System.currentTimeMillis()
            MessageDialog.newInstance(message).show(providerRef.get()!!.provideSupportFragmentManager())
        }
    }

    override fun showOnConnectionStateChanged(connected: Boolean) {
        hideProgress()

        if (providerRef.get() != null && System.currentTimeMillis() - lastTime > 100) {
            lastTime = System.currentTimeMillis()
            MessageDialog.newInstance("No Internet connection!").show(providerRef.get()!!.provideSupportFragmentManager())
        }
    }

    override fun showProgress(tag: Any?, message: String?) {
        if (providerRef.get() != null && !progressDialog.isAdded) {
            if (message != null) {
                progressDialog.setDescription(message)
            }

            if (!progressDialog.isShowing) {
                progressDialog.show(providerRef.get()!!.provideSupportFragmentManager())
            }
        }
    }

    override fun hideProgress(tag: Any?) {
        try {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        } catch (e: Exception) {
        }
    }

    override fun getDestroyDisposable(): CompositeDisposable {
        return disposable
    }
}