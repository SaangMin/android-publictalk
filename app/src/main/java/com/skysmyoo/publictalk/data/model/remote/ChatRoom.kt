package com.skysmyoo.publictalk.data.model.remote

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class ChatRoom(
    val me: String = "",
    val other: User? = null,
    val messages: List<Message> = emptyList(),
    val chatCreatedAt: String = "",
): Serializable