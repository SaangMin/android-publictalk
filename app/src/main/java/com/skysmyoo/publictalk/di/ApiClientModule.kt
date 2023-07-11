package com.skysmyoo.publictalk.di

import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.data.source.remote.ApiClient
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
object ApiClientModule {

    @Singleton
    @Provides
    @Named("ApiOkHttpClient")
    fun provideApiOkHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    @Singleton
    @Provides
    @Named("ApiRetrofit")
    fun provideApiRetrofit(@Named("ApiOkHttpClient") client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(ApiCallAdapterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideApiClient(@Named("ApiRetrofit") retrofit: Retrofit): ApiClient {
        return retrofit.create(ApiClient::class.java)
    }
}