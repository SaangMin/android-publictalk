package com.skysmyoo.publictalk.data.source.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TranslationResponse(
    val message: MessageResponse
)

@JsonClass(generateAdapter = true)
data class MessageResponse(
    val result: ResultResponse
)

@JsonClass(generateAdapter = true)
data class ResultResponse(
    val translatedText: String
)