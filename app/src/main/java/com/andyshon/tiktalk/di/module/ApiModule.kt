package com.andyshon.tiktalk.di.module

import com.andyshon.tiktalk.BuildConfig
import com.andyshon.tiktalk.data.model.auth.AuthService
import com.andyshon.tiktalk.data.model.calls.CallService
import com.andyshon.tiktalk.data.model.match.MatchService
import com.andyshon.tiktalk.data.model.messages.MessagesService
import com.andyshon.tiktalk.data.model.places.PlacesService
import com.andyshon.tiktalk.data.model.settings.SettingsService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApiModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        @Singleton
        @Named("basic")
        fun provideApiClient(@Named("basicClient") client: OkHttpClient, @Named("basic") gson: Gson): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build()
        }

        @Provides
        @JvmStatic
        @Singleton
        @Named("places")
        fun provideApiClientPlaces(@Named("basicClient") client: OkHttpClient, @Named("basic") gson: Gson): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL_PLACES)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build()
        }

        @Provides
        @JvmStatic
        @Singleton
        @Named("nulls")
        fun provideNullsApiClient(@Named("basicClient") client: OkHttpClient, @Named("nulls") gson: Gson): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build()
        }

        @Provides
        @JvmStatic
        @Singleton
        fun provideAuthService(@Named("basic") retrofit: Retrofit): AuthService =
            retrofit.create(AuthService::class.java)

        @Provides
        @JvmStatic
        @Singleton
        fun provideMessagesService(@Named("basic") retrofit: Retrofit): MessagesService =
            retrofit.create(MessagesService::class.java)

        @Provides
        @JvmStatic
        @Singleton
        fun provideMatchService(@Named("basic") retrofit: Retrofit): MatchService =
            retrofit.create(MatchService::class.java)

        @Provides
        @JvmStatic
        @Singleton
        fun provideSettingsService(@Named("basic") retrofit: Retrofit): SettingsService =
            retrofit.create(SettingsService::class.java)

        @Provides
        @JvmStatic
        @Singleton
        fun providePlacesService(@Named("places") retrofit: Retrofit): PlacesService =
            retrofit.create(PlacesService::class.java)

        @Provides
        @JvmStatic
        @Singleton
        fun provideCallService(@Named("basic") retrofit: Retrofit): CallService =
                retrofit.create(CallService::class.java)

    }
}