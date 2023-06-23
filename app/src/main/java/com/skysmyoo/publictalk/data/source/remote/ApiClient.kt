package com.skysmyoo.publictalk.data.source.remote

import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.di.ServiceLocator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiClient {

    @POST("users.json")
    suspend fun putUser(
        @Query("auth") auth: String,
        @Body user: User
    ): Response<Map<String, String>>

    companion object {
        private const val baseUrl = BuildConfig.BASE_URL

        fun create(): ApiClient {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(ServiceLocator.moshi))
                .build()
                .create(ApiClient::class.java)
        }
    }
}