package com.andyshon.tiktalk.ui.base

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.data.preference.Preference
import io.reactivex.disposables.CompositeDisposable
import com.andyshon.tiktalk.ui.base.presentation.PresentationComponentProvider
import com.andyshon.tiktalk.ui.base.presentation.PresentationDelegate
import timber.log.Timber

abstract class BaseFragment : Fragment(), PresentationComponentProvider, BaseContract.View {

    private lateinit var currentTheme: String
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity!!)
        currentTheme = sharedPref.getString(Preference.KEY_THEME, "default")!!
        Timber.e("onCreate, setAppTheme == $currentTheme")
//        setAppTheme(currentTheme)
    }

    private fun setAppTheme(currentTheme: String) {
        Timber.e("currentTheme = $currentTheme")
        when (currentTheme) {
            "default" -> activity!!.setTheme(R.style.AppTheme_Default)
            else -> activity!!.setTheme(R.style.AppTheme_Dark)
        }
    }

    override fun onResume() {
        super.onResume()
        val theme = sharedPref.getString(Preference.KEY_THEME, "default")
        println("theme = $theme, currentTheme = $currentTheme")
        if(currentTheme != theme) {
//            setAppTheme(theme)
            activity!!.recreate()
        }
    }

    protected val presentationDelegate by lazy { PresentationDelegate.Factory.create(this) }

    override fun onDestroyView() {
        presentationDelegate.onDestroy()
        super.onDestroyView()
    }

    override fun provideSupportFragmentManager(): FragmentManager {
        return fragmentManager!!
    }

    override fun provideActivity(): FragmentActivity {
        return activity!!
    }

    override fun getDestroyDisposable(): CompositeDisposable {
        return presentationDelegate.getDestroyDisposable()
    }

    override fun showProgress(tag: Any?, message: String?) {
        presentationDelegate.showProgress(tag)
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
        return activity!!
    }

    override fun showOnConnectionStateChanged(isConnected: Boolean) {
        if (isConnected.not()) {
//            presentationDelegate.showOnConnectionStateChanged(isConnected)
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
}