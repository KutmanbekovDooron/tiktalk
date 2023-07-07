package com.andyshon.tiktalk.di.module

import com.andyshon.tiktalk.App
import com.andyshon.tiktalk.BuildConfig
import com.andyshon.tiktalk.data.preference.Preference
import com.andyshon.tiktalk.data.preference.PreferenceManager
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.github.simonpercic.oklog3.OkLogInterceptor
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule {

    @Module
    companion object {

        @Provides
        @Singleton
        @JvmStatic
        @Named("basic")
        fun provideGson(): Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

//        @Provides
//        @Singleton
//        @JvmStatic
//        @Named("places")
//        fun providePlacesGson(): Gson = GsonBuilder()
//            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//            .create()

        @Provides
        @Singleton
        @JvmStatic
        @Named("nulls")
        fun provideNullableGson(): Gson = GsonBuilder()
            .serializeNulls()
            .create()

        @Provides
        @Singleton
        @JvmStatic
        @Named("cache")
        fun provideHttpCache(app: App): Cache {
            val cacheSize = 10 * 1024 * 1024
            return Cache(app.cacheDir, cacheSize.toLong())
        }

        @Provides
        @Singleton
        @JvmStatic
        @Named("basicClient")
        fun provideHttpClient(
            preferences: PreferenceManager,
            @Named("cache") cache: Cache
        ): OkHttpClient {
            val clientBuilder = OkHttpClient.Builder()
            val requestLogger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            clientBuilder.cache(cache)


            clientBuilder.addNetworkInterceptor {
                val origin = it.request()
                val requestBuilder = origin.newBuilder()

                val token = preferences.getString(Preference.KEY_TOKEN, "")
                Timber.e("token = $token -> ${token.replace("\"","")}")
                if (token.isNotEmpty()) {
//                    requestBuilder.addHeader("Authorization", "Bearer ${token.replace("\"","")}")
                    requestBuilder.addHeader("Auth-token", token.replace("\"",""))
                    requestBuilder.addHeader("Content-Type", "application/json")
                }
                it.proceed(requestBuilder.build())
            }

            clientBuilder.addNetworkInterceptor {
                val origin = it.proceed(it.request())
                val cacheControl = origin.header("Cache-Control")
                if (cacheControl == null || cacheControl.contains("no-store")
                    || cacheControl.contains("no-cache")
                    || cacheControl.contains("must-revalidate")
                    || cacheControl.contains("max-age=0")
                ) {
                    origin.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, max-age=" + 1)
                        .build()
                } else {
                    origin
                }
            }

            val okLogInterceptor = OkLogInterceptor.builder()
                .setBaseUrl("http://oklog.responseecho.com")
                .withRequestHeaders(true)
                .shortenInfoUrl(true)
                .build()

            clientBuilder.readTimeout(180, TimeUnit.SECONDS)
            clientBuilder.connectTimeout(20, TimeUnit.SECONDS)
            clientBuilder.writeTimeout(360, TimeUnit.SECONDS)

            if (BuildConfig.DEBUG) {
                clientBuilder.addInterceptor(okLogInterceptor)
                clientBuilder.addNetworkInterceptor(StethoInterceptor())
                clientBuilder.addInterceptor(requestLogger)
            }

            return clientBuilder.build()
        }
    }

}