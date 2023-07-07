package com.andyshon.tiktalk

import android.app.Application
import android.content.Context
import com.andyshon.DaggerComponentProvider
import com.google.firebase.FirebaseApp
import timber.log.Timber

class App: Application() {
    companion object {

        operator fun get(context: Context): DaggerComponentProvider {
            return (context.applicationContext as App).componentProvider
        }
    }

    private val componentProvider: DaggerComponentProvider = DaggerComponentProvider(this)

    override fun onCreate() {
        super.onCreate()

//        FirebaseApp.initializeApp(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}