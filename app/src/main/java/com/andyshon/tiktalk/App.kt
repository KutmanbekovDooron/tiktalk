package com.andyshon.tiktalk

import android.app.Application
import android.content.Context
import com.andrognito.patternlockview.BuildConfig
import com.andyshon.DaggerComponentProvider
import timber.log.Timber

class App : Application() {

    private val componentProvider: DaggerComponentProvider = DaggerComponentProvider(this)

    override fun onCreate() {
        super.onCreate()
//        FirebaseApp.initializeApp(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {

        operator fun get(context: Context): DaggerComponentProvider {
            return (context.applicationContext as App).componentProvider
        }
    }

}