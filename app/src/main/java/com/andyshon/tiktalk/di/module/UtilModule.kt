package com.andyshon.tiktalk.di.module

import android.content.Context
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.andyshon.tiktalk.di.qualifier.ApplicationContext
import com.andyshon.tiktalk.events.RxEventBus
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class UtilModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        @Singleton
        fun provideSharedPreferences(
            @ApplicationContext context: Context,
            @Named("basic") gson: Gson
        ): PreferenceManager {
            return PreferenceManager(context, gson)
        }

        @Provides
        @Singleton
        @JvmStatic
        fun provideRxEventBus(): RxEventBus {
            return RxEventBus()
        }
    }

}