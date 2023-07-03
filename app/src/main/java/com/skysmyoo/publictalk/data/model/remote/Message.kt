package com.skysmyoo.publictalk.data.model.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    val sender: String = "",
    val receiver: String = "",
    val body: String = "",
    val isReading: Boolean = false,
    val createdAt: String = "",
)