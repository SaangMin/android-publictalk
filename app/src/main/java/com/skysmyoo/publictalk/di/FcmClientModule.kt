package com.skysmyoo.publictalk.di

import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.data.source.remote.FcmClient
import com.skysmyoo.publictalk.data.source.remote.response.ApiCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FcmClientModule {

    @Singleton
    @Provides
    @Named("FcmOkHttpClient")
    fun provideFcmOkHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    @Singleton
    @Provides
    @Named("FcmRetrofit")
    fun provideFcmRetrofit(@Named("FcmOkHttpClient") client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.FCM_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(ApiCallAdapterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideFcmClient(@Named("FcmRetrofit") retrofit: Retrofit): FcmClient {
        return retrofit.create(FcmClient::class.java)
    }
}