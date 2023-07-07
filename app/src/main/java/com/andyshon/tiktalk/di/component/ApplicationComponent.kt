package com.andyshon.tiktalk.di.component

import com.andyshon.tiktalk.App
import com.andyshon.tiktalk.di.module.ApiModule
import com.andyshon.tiktalk.di.module.ApplicationModule
import com.andyshon.tiktalk.di.module.NetworkModule
import com.andyshon.tiktalk.di.module.UtilModule
import com.andyshon.tiktalk.data.twilio.TwilioSingleton
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, NetworkModule::class, ApiModule::class, UtilModule::class])
interface ApplicationComponent {

    fun plusPresentationComponent(): PresentationComponent.Builder

    fun inject(app: App)

    fun inject(twilioSingleton: TwilioSingleton)

}