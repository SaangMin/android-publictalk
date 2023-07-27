package com.skysmyoo.publictalk.data.source.remote

import com.skysmyoo.publictalk.data.model.remote.FcmNotification
import com.skysmyoo.publictalk.data.source.remote.response.ApiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FcmClient {

    @POST("fcm/send")
    suspend fun sendNotification(
        @Header("Authorization") serverKey: String,
        @Body notification: FcmNotification
    ): ApiResponse<Unit>
}