package com.skysmyoo.publictalk.data.source.remote

import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.data.model.remote.Token
import com.skysmyoo.publictalk.di.ServiceLocator.moshi
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmClient {

    @POST("fcm/register")
    suspend fun registerToken(
        @Body token: Token
    ): Response<ResponseBody>

    companion object {
        private const val baseUrl = BuildConfig.FCM_BASE_URL

        fun create(): FcmClient {
            val logger = HttpLoggingInterceptor().apply {
                level =
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(FcmClient::class.java)
        }
    }
}