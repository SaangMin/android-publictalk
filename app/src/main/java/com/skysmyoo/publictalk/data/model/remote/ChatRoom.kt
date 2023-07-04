package com.skysmyoo.publictalk.data.model.remote

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class ChatRoom(
    val me: String = "",
    val other: User? = null,
    val messages: Map<String, Message> = emptyMap(),
    val chatCreatedAt: String = "",
) : Serializable