package com.skysmyoo.publictalk.data.source.remote

import com.skysmyoo.publictalk.data.source.remote.response.ApiResponse
import javax.inject.Inject

class TranslateDataSource @Inject constructor(private val papagoClient: TranslateClient) {
    suspend fun translateText(
        sourceLanguage: String,
        targetLanguage: String,
        body: String
    ): ApiResponse<TranslationResponse> {
        return papagoClient.translateText(sourceLanguage, targetLanguage, body)
    }
}