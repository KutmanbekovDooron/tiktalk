package com.andyshon.tiktalk.di.module

import android.content.Context
import com.andyshon.tiktalk.App
import com.andyshon.tiktalk.di.qualifier.ApplicationContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val app: App) {

    @Provides
    @Singleton
    @ApplicationContext
    fun provideApplicationContext(): Context {
        return app
    }

    @Provides
    @Singleton
    fun provideApplication(): App {
        return app
    }

}