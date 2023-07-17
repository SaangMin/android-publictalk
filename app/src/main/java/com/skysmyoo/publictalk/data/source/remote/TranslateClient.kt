package com.skysmyoo.publictalk.data.source.remote

import com.skysmyoo.publictalk.data.source.remote.response.ApiResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface TranslateClient {

    @FormUrlEncoded
    @POST("/v1/papago/n2mt")
    suspend fun translateText(
        @Field("source") sourceLanguage: String,
        @Field("target") targetLanguage: String,
        @Field("text") text: String
    ): ApiResponse<TranslationResponse>
}