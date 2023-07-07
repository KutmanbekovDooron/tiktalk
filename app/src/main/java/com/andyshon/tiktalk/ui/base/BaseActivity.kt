package com.andyshon.tiktalk.ui.base

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.andyshon.tiktalk.ui.base.presentation.PresentationComponentProvider
import com.andyshon.tiktalk.ui.base.presentation.PresentationDelegate
import com.andyshon.tiktalk.utils.extensions.setupTheme
import io.reactivex.disposables.CompositeDisposable

abstract class BaseActivity : AppCompatActivity(), PresentationComponentProvider, BaseContract.View {

    var supportThemes = true

    private val presentationDelegate by lazy {
        PresentationDelegate.Factory.create(this)
    }

    override fun onDestroy() {
        presentationDelegate.onDestroy()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (supportThemes) {
            setupTheme(this)
        }
        super.onCreate(savedInstanceState)
    }

    private var mLastClickTime = 0L

    fun canOpen(): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return false
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    override fun provideSupportFragmentManager(): FragmentManager {
        return supportFragmentManager
    }

    override fun provideActivity(): FragmentActivity {
        return this
    }

    override fun getDestroyDisposable(): CompositeDisposable {
        return presentationDelegate.getDestroyDisposable()
    }

    override fun showProgress(tag: Any?, message: String?) {
        presentationDelegate.showProgress(tag, message)
    }

    override fun hideProgress(tag: Any?) {
        presentationDelegate.hideProgress(tag)
    }

    override fun showMessage(messageRes: Int, tag: Any?) {
        presentationDelegate.showMessage(messageRes, tag)
    }

    override fun showMessage(message: String, tag: Any?) {
        presentationDelegate.showMessage(message, tag)
    }

    override fun getActivityContext(): Activity {
//        return presentationDelegate.getActivityContext()
        return this
    }

    override fun showOnConnectionStateChanged(isConnected: Boolean) {
        if (isConnected.not()) {
            presentationDelegate.showOnConnectionStateChanged(isConnected)
        }
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    protected open fun requestStatusBarOverlay(){
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    protected fun hideStatusBar(hide: Boolean) {
        val decorView = window.decorView
        // Hide the status bar.
        val uiOptions = if (!hide) View.SYSTEM_UI_FLAG_VISIBLE else View.SYSTEM_UI_FLAG_FULLSCREEN

        decorView.systemUiVisibility = uiOptions
    }
}