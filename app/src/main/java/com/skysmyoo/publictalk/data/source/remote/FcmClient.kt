package com.skysmyoo.publictalk.data.source.remote

import com.skysmyoo.publictalk.data.model.remote.Token
import com.skysmyoo.publictalk.data.source.remote.response.ApiResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmClient {

    @POST("fcm/register")
    suspend fun registerToken(
        @Body token: Token
    ): ApiResponse<ResponseBody>
}