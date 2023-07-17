package com.skysmyoo.publictalk.di

import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.data.source.remote.TranslateClient
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
object TranslateClientModule {

    @Singleton
    @Provides
    @Named("PapagoOkHttpClient")
    fun providePapagoApiOkHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("X-Naver-Client-Id", BuildConfig.PAPAGO_CLIENT_ID)
                    .addHeader("X-Naver-Client-Secret", BuildConfig.PAPAGO_CLIENT_SECRET)
                    .build()
                chain.proceed(newRequest)
            }
            .build()
    }

    @Singleton
    @Provides
    @Named("PapagoRetrofit")
    fun providePapagoRetrofit(@Named("PapagoOkHttpClient") client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.PAPAGO_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(ApiCallAdapterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun providePapagoClient(@Named("PapagoRetrofit") retrofit: Retrofit): TranslateClient {
        return retrofit.create(TranslateClient::class.java)
    }
}